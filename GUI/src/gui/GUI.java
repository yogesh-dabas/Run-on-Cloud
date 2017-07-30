package gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import javax.swing.JProgressBar;

public class GUI extends javax.swing.JFrame implements Runnable {

    Socket s;
    String clientId = "GUI";
    String ip;
    int port;

    int noOfPrograms;

    HashMap<String, String> programNameToNumber;
    JLabel pName[];//Program Description
    JButton pExec[];//Button for execution
    JButton pStatus[];//status of program
    JProgressBar pTimeElapsed[];//time for execution

    String selectedClientIdForStatistics = null;

    MyJPanel stat;

    Banner banner;

    public GUI(Socket s, String ip, int port, String clientId) throws IOException {
        super("Run-On-Cloud GUI");
        Image img = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("CDOT_logo.gif"));
        this.setIconImage(img);

        this.s = s;
        this.ip = ip;
        this.port = port;
        this.clientId = clientId;
        
        initComponents();
        
        guiClientId.setText(clientId);

        //client = new JButton[SharedData.maximumClientsConnectionLimit];
        stat = new MyJPanel();

        stat.setBounds(340, 330, 420, 200);
        basePanel.add(stat);
        basePanel.validate();

        for (int i = 0; i < MyJPanel.numberOfSeconds + 1; i++) {
            stat.l[i] = new MyLine();
        }
        programNameToNumber = new HashMap();

        Thread t4 = new Thread(stat);
        t4.start();

        banner = new Banner(bannerLabel);
        banner.setBanner("Welcome to Run-On-Cloud GUI!!");
        Thread bannerThread = new Thread(banner);
        bannerThread.start();

        /*this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    Socket s = new Socket(ip, port);
                    PrintStream ps = new PrintStream(s.getOutputStream());

                    ps.println(clientId);
                    ps.println(Thread.currentThread().getName());

                    //Request the server for Programs list
                    ps.println("windowClosing");
                    s.close();
                    ps.close();
                } catch (IOException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });*/
    }

    public void run() {

        String currentThread = Thread.currentThread().getName();

        //Initialize the Programs List
        if (currentThread.equals("tasklistThread")) {
            try {
                Socket s = new Socket(ip, port);
                PrintStream ps = new PrintStream(s.getOutputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                ps.println(clientId);
                ps.println(Thread.currentThread().getName());

                //Request the server for Programs list
                ps.println("tasklist");

                noOfPrograms = Integer.parseInt(br.readLine());

                programs.setLayout(new GridLayout(noOfPrograms, 4, 5, 5));

                pName = new JLabel[noOfPrograms];
                pExec = new JButton[noOfPrograms];
                pStatus = new JButton[noOfPrograms];
                pTimeElapsed = new JProgressBar[noOfPrograms];

                //Read the tasklist sent by server
                String msg;
                int i = 0;
                while ((msg = br.readLine()) != null && i < noOfPrograms) {

                    //update status to FrontEnd
                    //System.out.println(i);
                    ///System.out.println(msg);
                    int comma = msg.indexOf(',');
                    int colon = msg.indexOf(':');

                    String programMainClass = msg.substring(0, comma);//main class name
                    String programName = msg.substring(comma + 1, colon);//Description
                    int maxComputationTime = Integer.parseInt(msg.substring(colon + 1, msg.length()));//in milli seconds

                    pName[i] = new JLabel();
                    pName[i].setText("P." + (i + 1) + ": " + programName);
                    pName[i].setToolTipText(programName);

                    programNameToNumber.put(programMainClass, i + "");

                    programs.add(pName[i]);

                    pExec[i] = new JButton("Load Program");
                    pExec[i].setName(msg.substring(0, msg.indexOf(',')));
                    pExec[i].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ev) {
                            programExecRequest(ev);
                        }
                    });
                    programs.add(pExec[i]);

                    pStatus[i] = new JButton("-");
                    pStatus[i].setOpaque(true);//display color if any
                    pStatus[i].setBackground(Color.LIGHT_GRAY);
                    programs.add(pStatus[i]);

                    try {
                        pTimeElapsed[i] = new JProgressBar();
                    } catch (ClassCastException e) {
                        System.out.println("ERROR!! " + e.getMessage());
                    }
                    programs.add(pTimeElapsed[i]);
                    pTimeElapsed[i].setMaximum(maxComputationTime);

                    programs.validate();

                    i++;
                }

                s.close();
                ps.close();
                br.close();
            } catch (NumberFormatException e) {
                System.out.println("ERROR!! " + e.getMessage());
            } catch (ConnectException e) {
                System.out.println("ERROR!! Server is down or not available currently!!");
            } catch (IOException e) {
                System.out.println("ERROR!! " + e.getMessage());
            }

            Thread t6 = new Thread(this, "timeElapsedMonitorThread");
            t6.start();

        } else if (currentThread.equals("statisticsThread")) {
            //Request the server for a particular client's utilization statistics

            while (true) {
                if (selectedClientIdForStatistics == null) {
                    stat.setStatus("Click on the client to view its statistics!!");
                } else {
                    try {
                        Socket s = new Socket(ip, port);
                        PrintStream ps = new PrintStream(s.getOutputStream());
                        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                        ps.println(clientId);
                        ps.println(Thread.currentThread().getName());

                        //Send statistics request to server
                        ps.println("statistics");

                        ps.println(selectedClientIdForStatistics);

                        String msg = br.readLine();
                        if (msg.equals("disconnected")) {
                            stat.setStatus(selectedClientIdForStatistics + " is disconnected!!");
                        } else {
                            stat.setStatus("Client: " + selectedClientIdForStatistics);
                            String[] cu = msg.split(" ");
                            stat.setCpu(Double.parseDouble(cu[0]));
                            stat.setMem(Double.parseDouble(cu[1]));
                            stat.setDsk(Double.parseDouble(cu[2]));
                        }

                        s.close();
                        ps.close();
                        br.close();
                    } catch (ConnectException e) {
                        System.out.println("ERROR!! Server is down or not available currently!! ");
                        return;
                    } catch (IOException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Error->" + e.getMessage());
                }
            }
        } else if (currentThread.equals("newClientCountThread")) {
            while (true) {
                try {
                    Socket s = new Socket(ip, port);
                    PrintStream ps = new PrintStream(s.getOutputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                    ps.println(clientId);
                    ps.println(Thread.currentThread().getName());

                    //Send client Increment request to server
                    ps.println("newClientCount");

                    int noOfClients = Integer.parseInt(br.readLine());
                    for (int i = 0; i < noOfClients; i++) {
                        String msg = br.readLine();
                        if (!SharedData.clientData.containsKey(msg)) {
                            banner.setBanner("New Client: " + msg + " got connected!!");
                            SharedData.clientData.put(msg, new JButton(msg));
                            SharedData.clientData.get(msg).setName(msg);
                            SharedData.clientData.get(msg).addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    synchronized (this) {
                                        //Update selected Client for Utilization Graph
                                        selectedClientIdForStatistics = ((JButton) e.getSource()).getName();

                                        //Flush existing Utilization Graph
                                        for (int i = 0; i < MyJPanel.numberOfSeconds + 1; i++) {
                                            stat.l[i] = new MyLine();
                                        }
                                    }
                                }
                            });
                            noOfClientsConnected.setText((SharedData.clientData.size() + ""));
                            clientDisplay.add(SharedData.clientData.get(msg));
                            clientDisplay.validate();
                        }
                    }
                } catch (ConnectException e) {
                    System.out.println("ERROR!! Server is down or currently not available !!");
                    JOptionPane.showMessageDialog(rootPane, "Clients Updation Failed! Server is down or not available currently!!", "Updation Failed", ERROR_MESSAGE);
                } catch (IOException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.out.println("Error->" + e.getMessage());
                }
            }

        } else if (currentThread.equals("disconnectedClientCountThread")) {
            while (true) {
                try {
                    Socket s = new Socket(ip, port);
                    PrintStream ps = new PrintStream(s.getOutputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                    ps.println(clientId);
                    ps.println(Thread.currentThread().getName());

                    //Send client Increment request to server
                    ps.println("disconnectedClientCount");

                    int noOfClients = Integer.parseInt(br.readLine());
                    for (int i = 0; i < noOfClients; i++) {
                        String msg = br.readLine();
                        if (SharedData.clientData.containsKey(msg)) {
                            if (msg != null) {
                                System.out.println("Disconnected: " + msg);
                                banner.setBanner("Client: " + msg + " got disconnected!!");
                                SharedData.clientData.get(msg).setVisible(false);
                                clientDisplay.remove(SharedData.clientData.get(msg));
                                SharedData.clientData.remove(msg);
                                clientDisplay.validate();
                            }
                        }
                    }
                    noOfClientsConnected.setText(SharedData.clientData.size() + "");

                } catch (ConnectException e) {
                    System.out.println("ERROR!! Server is down or not available currently!!");
                    JOptionPane.showMessageDialog(rootPane, "Clients Updation Failed! Server is down or not available currently!!", "Updation Successful", ERROR_MESSAGE);
                } catch (IOException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.out.println("Error->" + e.getMessage());
                }
            }
        } /*
        
         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                
         Get the Program's Front End from Server and Compile it
                
         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         */ else if (currentThread.equals("programLoadingThread")) {

            while (true) {
                Object prog[] = SharedData.programsForLoading.toArray();
                for (int i = 0; i < prog.length; i++) {
                    try {
                        String program = (String) prog[i];
                        System.out.println("Load " + program);

                        Socket s = new Socket(ip, port);
                        PrintStream ps = new PrintStream(s.getOutputStream());
                        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                        ps.println(clientId);
                        ps.println(Thread.currentThread().getName());

                        ps.println("Load:" + program);

                        //LOADING Front End
                        String msg;
                        BufferedWriter out = new BufferedWriter(new FileWriter(program + "_FE.java"));
                        out.write("package tmp;");
                        while (!(msg = br.readLine()).equals("stop")) {
                            out.write(msg);
                            out.write('\r');
                            out.write('\n');
                        }
                        out.close();

                        //LOADING Data Structure
                        out = new BufferedWriter(new FileWriter(program + "_DS.java"));
                        out.write("package tmp;");
                        while (!(msg = br.readLine()).equals("stop")) {
                            out.write(msg);
                            out.write('\r');
                            out.write('\n');
                        }
                        out.close();

                        /*~~~~~~~~~~~~~~~~~~~~COMPILING  Data Structure~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                        Runtime r = Runtime.getRuntime();
                        String st = "javac -d . " + program + "_DS.java";
                        Process p = r.exec(st);
                        BufferedReader errorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        String l = errorStream.readLine();


                        /*~~~~~~~~~~~~~~~~~~~~COMPILING Front End~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                        Runtime r1 = Runtime.getRuntime();
                        String st1 = "javac -d . " + program + "_FE.java";
                        Process p1 = r1.exec(st1);
                        BufferedReader errorStream1 = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
                        String l1 = errorStream1.readLine();

                        int tmp = Integer.parseInt(programNameToNumber.get(program));
                        if (l == null && l1 == null) {
                            SharedData.programsForLoading.remove(program);
                            pExec[tmp].setText("Execute");
                            pStatus[tmp].setText("Ready for Execution");
                            pStatus[tmp].setBackground(Color.MAGENTA);
                        } else {
                            pExec[tmp].setEnabled(false);
                            pStatus[tmp].setText("Compile Time Error");
                            pStatus[tmp].setBackground(Color.RED);
                        }

                        //Flushing temporary files
                        File f = new File(program + "_FE.java");
                        if (f.exists()) {
                            f.delete();
                        }

                        f = new File(program + "_DS.java");
                        if (f.exists()) {
                            f.delete();
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        } else if (currentThread.startsWith("runProgramFrontEndThreadFor")) {
            String program = currentThread.substring(27, currentThread.length());
            Process p1;
            try {
                p1 = Runtime.getRuntime().exec("java tmp." + program + "_FE");

                BufferedReader br1 = new BufferedReader(new InputStreamReader(p1.getInputStream()));
                String msg;
                while ((msg = br1.readLine()) != null) {
                    if (msg.equals("EXIT")) {
                        //Add Program to "programsForExecution" for execution
                        SharedData.programsForExecution.put(program, null);
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (currentThread.startsWith("ProgramExecutionThread")) {

            while (true) {

                try {
                    Set set = SharedData.programsForExecution.keySet();
                    Object prog[] = set.toArray();
                    for (int i = 0; i < prog.length; i++) {

                        String program = (String) prog[i];

                        Socket s = new Socket(ip, port);
                        PrintStream ps = new PrintStream(s.getOutputStream());
                        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                        ps.println(clientId);
                        ps.println(Thread.currentThread().getName());

                        //Send Request to Server for Program Execution
                        ps.println("Run:" + program);

                        String ack = br.readLine();
                        int tmp = Integer.parseInt(programNameToNumber.get(program));
                        System.out.println("Req: " + program + " " + ack);
                        if (ack != null && ack.equals("noclients")) {
                            //JOptionPane.showMessageDialog(rootPane, "Server is down or None of the clients are available for Execution!! Please try again later!!", "ERROR", ERROR_MESSAGE);
                            pStatus[tmp].setText("Waiting for Client");
                            pStatus[tmp].setBackground(Color.YELLOW);
                        } else if (ack != null && ack.startsWith("clientAssigned")) {
                            SharedData.programsForExecution.remove(program);
                            String client = ack.substring(15, ack.length());
                            pStatus[tmp].setName(client);
                            pStatus[tmp].setText("Running at " + client);
                            pStatus[tmp].setBackground(Color.ORANGE);

                            //Send the Program DS Obj to Server
                            FileInputStream fin = new FileInputStream("tmp\\" + program + "_DS_Obj");
                            BufferedInputStream bin = new BufferedInputStream(fin);
                            /*int ch;
                             while ((ch = bin.read()) != -1) {
                             ps.print((char) ch);
                             }*/
                            int ch;
                            String temp = "";
                            while ((ch = bin.read()) != -1) {
                                temp += ch + " ";
                            }
                            ps.println(temp);

                            JOptionPane.showMessageDialog(rootPane, "PROGRAM NO : " + program + " assigned to CLIENT: " + client + " for execution.", "Execution Started", INFORMATION_MESSAGE);

                        }

                        s.close();
                        ps.close();
                    }
                } catch (ConnectException e) {
                    JOptionPane.showMessageDialog(rootPane, "Server is down or currently not available!! Please connect again!!", "ERROR", ERROR_MESSAGE);
                    dispose();
                    GUIWelcome.main(new String[0]);
                } catch (IOException e) {
                    System.out.println("ERROR!! " + e.getMessage());
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

            }

        } else if (currentThread.equals("timeElapsedMonitorThread")) {
            while (true) {
                for (int i = 0; i < noOfPrograms; i++) {
                    if (pStatus[i].getText().startsWith("Running")) {
                        pTimeElapsed[i].setValue(pTimeElapsed[i].getValue() + 1);
                    } else if (pStatus[i].getText().startsWith("Loading")) {
                        pTimeElapsed[i].setValue(pTimeElapsed[i].getValue() + 1);
                    } else {
                        pTimeElapsed[i].setValue(0);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (currentThread.equals("programFeedbackThread")) {
            while (true) {
                try {

                    Socket s = new Socket(ip, port);
                    PrintStream ps = new PrintStream(s.getOutputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                    ps.println(clientId);
                    ps.println(Thread.currentThread().getName());

                    //Send programFeedback request to server
                    ps.println("programFeedback");

                    //Instantly read the feedback report sent from serevr when it is made available from server and update status
                    String msg = br.readLine();
                    if (msg != null) {
                        int i = msg.indexOf(':');
                        String feedback = msg.substring(i + 1, msg.length());
                        if (!feedback.equals("waiting")) {
                            String program = msg.substring(0, i);
                            System.out.println(program + ":" + feedback);
                            int tmp = Integer.parseInt(programNameToNumber.get(program));
                            String clientUsedForExecution = pStatus[tmp].getName();

                            if (feedback.startsWith("success")) {
                                pStatus[tmp].setBackground(Color.GREEN);
                                pStatus[tmp].setText("Successfully Executed");
                                pStatus[tmp].setToolTipText("Click to see Results!!");
                                pStatus[tmp].addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        JOptionPane.showMessageDialog(rootPane, feedback.substring(feedback.indexOf(':') + 1, feedback.length()).replaceAll(",", "\n"), "Results", INFORMATION_MESSAGE);
                                    }
                                });

                                banner.setBanner(program + " executed successfully at CLIENT: " + clientUsedForExecution);
                                System.out.println(feedback);
                            } else {
                                pStatus[tmp].setBackground(Color.RED);
                                pStatus[tmp].setText(feedback.substring(0, feedback.indexOf(':')));
                                pStatus[tmp].setToolTipText("Click to see Error Report!!");
                                pStatus[tmp].addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        JOptionPane.showMessageDialog(rootPane, feedback.substring(feedback.indexOf(':') + 1, feedback.length()).replaceAll(",", "\n"), "ERROR", ERROR_MESSAGE);
                                    }
                                });
                                banner.setBanner(program + " execution failed at CLIENT: " + clientUsedForExecution);
                                System.out.println(feedback);
                            }
                            programs.validate();
                        }
                    }
                } catch (ConnectException e) {
                    System.out.println("ERROR!! Server is down or not available currently!! ");
                } catch (IOException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    public void programExecRequest(ActionEvent ev) {

        String program = ((JButton) ev.getSource()).getName();
        JButton button = ((JButton) ev.getSource());

        /*
         Loading Of Program Front End for 1st time Execution
         */
        if (button.getText().equals("Load Program")) {
            button.setText("Loading");
            int tmp = Integer.parseInt(programNameToNumber.get(program));
            pStatus[tmp].setText("Loading");
            pStatus[tmp].setBackground(Color.YELLOW);
            SharedData.programsForLoading.add(program);

        }//~~~~~~~~~~~~~~~~~~~~~~Running~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        else if (button.getText().equals("Execute")) {
            int tmp = Integer.parseInt(programNameToNumber.get(program));
            ActionListener a[] = pStatus[tmp].getActionListeners();
            if (a.length > 0) {
                pStatus[tmp].removeActionListener(a[0]);
            }
            Thread t7 = new Thread(this, "runProgramFrontEndThreadFor" + program);
            t7.start();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        basePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        guiClientId = new javax.swing.JLabel();
        bannerLabel = new javax.swing.JLabel();
        clientDisplay = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        noOfClientsConnected = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        programs = new javax.swing.JPanel();
        background = new javax.swing.JLabel();
        title = new javax.swing.JLabel();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        basePanel.setBackground(new java.awt.Color(204, 255, 204));
        basePanel.setLayout(null);

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Run-On-Cloud GUI");
        basePanel.add(jLabel1);
        jLabel1.setBounds(300, 3, 240, 30);

        guiClientId.setFont(new java.awt.Font("Verdana", 0, 24)); // NOI18N
        guiClientId.setForeground(new java.awt.Color(255, 255, 255));
        basePanel.add(guiClientId);
        guiClientId.setBounds(0, 0, 120, 40);

        bannerLabel.setBackground(new java.awt.Color(255, 255, 153));
        bannerLabel.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        bannerLabel.setForeground(new java.awt.Color(0, 0, 204));
        bannerLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        bannerLabel.setOpaque(true);
        basePanel.add(bannerLabel);
        bannerLabel.setBounds(0, 40, 820, 40);

        clientDisplay.setBackground(new java.awt.Color(255, 255, 204));
        clientDisplay.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        basePanel.add(clientDisplay);
        clientDisplay.setBounds(30, 360, 280, 170);

        jLabel2.setFont(new java.awt.Font("Segoe Print", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 153));
        jLabel2.setText("No of Clients Connected:");
        basePanel.add(jLabel2);
        jLabel2.setBounds(30, 320, 180, 40);

        noOfClientsConnected.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        noOfClientsConnected.setForeground(new java.awt.Color(0, 0, 204));
        noOfClientsConnected.setText("0");
        basePanel.add(noOfClientsConnected);
        noOfClientsConnected.setBounds(220, 320, 80, 40);

        programs.setBackground(new java.awt.Color(153, 153, 255));
        programs.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane1.setViewportView(programs);

        basePanel.add(jScrollPane1);
        jScrollPane1.setBounds(30, 110, 730, 190);

        background.setBackground(new java.awt.Color(255, 255, 255));
        background.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/clouds.jpg"))); // NOI18N
        background.setText("jLabel3");
        basePanel.add(background);
        background.setBounds(0, 80, 790, 470);

        title.setBackground(new java.awt.Color(102, 102, 255));
        title.setOpaque(true);
        basePanel.add(title);
        title.setBounds(0, 0, 790, 40);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(basePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 787, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(basePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel background;
    private javax.swing.JLabel bannerLabel;
    private javax.swing.JPanel basePanel;
    private javax.swing.JPanel clientDisplay;
    private javax.swing.JLabel guiClientId;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel noOfClientsConnected;
    private javax.swing.JPanel programs;
    private javax.swing.JLabel title;
    // End of variables declaration//GEN-END:variables

}
