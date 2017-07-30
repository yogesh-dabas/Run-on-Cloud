package server;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

/**
 *
 * @author Yogesh Dabas
 */
public class ServerFrontEnd extends javax.swing.JFrame {

    /*~~~~~~~~~~~~~~~~~~~SOCKET FOR CONNECTION~~~~~~~~~~~~~~*/
    private ServerSocket ss;

    /*~~~~~~~~~~~~~~~~~~~~~~Available / Allocated Servers~~~~~~~~~~~~~~~~~~~~~~~~~*/
    private int noOfAvailableServers = 100;
    private int noOfAllocatedServers = 0;
    private Thread t[];

    /*~~~~~~~~~~~~~~~~~~~~~~log~~~~~~~~~~~~~~~~~~~~*/
    ServerLog log = new ServerLog("FrontEnd");

    /*~~~~~~~~~~~~~~~~~~~~~~Constructor~~~~~~~~~~~~~~~~~~~~~~*/
    ServerFrontEnd() throws URISyntaxException {

        super("Run-On-Cloud Server");

        log.print("~~~~~~~~~~~~~~~~~~~Front End Started~~~~~~~~~~~~~~~~~~~");

        //Creating temporary folder
        File tmp = new File("tmp");
        if (!tmp.exists()) {
            tmp.mkdir();
        }

        //Set Logo
        Image img = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("CDOT_logo.gif"));
        this.setIconImage(img);
        initComponents();

        //Set Applications Paths if apps folder not present in current directory
        tmp = new File("apps");
        if (tmp.exists()) {
            appsChooser.setText("Selected");
            appsChooser.setEnabled(false);
        }

        //Adding action to start button to start servers
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (start.isSelected()) {
                    if (appsChooser.isEnabled()) {
                        JOptionPane.showMessageDialog(rootPane, "Please select the applications.", "ERROR", ERROR_MESSAGE);
                        start.setSelected(false);
                    } else {
                        try {
                            int port = Integer.parseInt(enteredPort.getText());
                            try {
                                noOfAllocatedServers = Integer.parseInt(enteredServerCount.getText());

                                if (noOfAllocatedServers > noOfAvailableServers) {
                                    JOptionPane.showMessageDialog(rootPane, "Number of Servers should be less than or equal to " + noOfAvailableServers, "ERROR", ERROR_MESSAGE);
                                    enteredServerCount.setText("");
                                    start.setSelected(false);
                                } else {
                                    try {
                                        ss = new ServerSocket(port);
                                        t = new Thread[noOfAllocatedServers];

                                        //initializing the server threads
                                        for (int i = 0; i < noOfAllocatedServers; i++) {
                                            t[i] = new Thread(new Server(ss), "Server " + i);
                                        }

                                        //stating the server threads
                                        for (int i = 0; i < noOfAllocatedServers; i++) {
                                            t[i].start();

                                            //log
                                            log.print("Server Thread " + i + " started!");
                                        }

                                        status.setText(status.getText() + "\n> Server Started! :" + noOfAllocatedServers);
                                        start.setText("Shut Down Server");

                                    } catch (IOException ex) {
                                        JOptionPane.showMessageDialog(rootPane, ex.getMessage(), "ERROR", ERROR_MESSAGE);
                                        enteredPort.setText("");
                                        enteredServerCount.setText("");
                                        start.setSelected(false);
                                    }
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(rootPane, "Invalid Server Count Entered!! ", "ERROR", ERROR_MESSAGE);

                                enteredServerCount.setText("");
                                start.setSelected(false);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(rootPane, "Invalid Port Entered!! ", "ERROR", ERROR_MESSAGE);
                            enteredPort.setText("");
                            enteredServerCount.setText("");
                            start.setSelected(false);
                        }
                    }

                } else {
                    try {
                        //stopping the server threads
                        for (int i = 0; i < noOfAllocatedServers; i++) {
                            t[i].setName(SharedData.serverClosedMsg);

                            //log
                            log.print("Server Thread " + i + " closed!");
                        }
                        ss.close();
                        status.setText(status.getText() + "\n> Server ShutDown! :" + noOfAllocatedServers);
                        start.setText("Start Server");
                    } catch (Exception ex) {
                        System.out.println("Error! " + ex.getMessage());
                    }
                }
            }
        });

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        enteredPort = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        enteredServerCount = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        start = new javax.swing.JToggleButton();
        appsChooser = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        statusPane = new javax.swing.JScrollPane();
        status = new javax.swing.JTextArea();
        background = new javax.swing.JLabel();

        jScrollPane1.setViewportView(jTextPane1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        jPanel2.setBackground(new java.awt.Color(153, 153, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 0));
        jLabel2.setText("Enter the Port Number");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 0));
        jLabel3.setText("Enter No. of Servers");

        enteredServerCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enteredServerCountActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 0));
        jLabel4.setText("Select Applications");

        start.setText("Start Server");
        start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startActionPerformed(evt);
            }
        });

        appsChooser.setText("Select");
        appsChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appsChooserActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(start, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                                .addGap(65, 65, 65)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(enteredServerCount)
                            .addComponent(enteredPort)
                            .addComponent(appsChooser, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE))))
                .addGap(16, 16, 16))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(appsChooser)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(enteredPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enteredServerCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(start)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2);
        jPanel2.setBounds(240, 30, 320, 160);

        statusLabel.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        statusLabel.setForeground(new java.awt.Color(0, 51, 255));
        statusLabel.setText("SERVER STATUS");
        getContentPane().add(statusLabel);
        statusLabel.setBounds(340, 360, 160, 30);

        status.setColumns(20);
        status.setRows(5);
        statusPane.setViewportView(status);

        getContentPane().add(statusPane);
        statusPane.setBounds(240, 390, 330, 96);

        background.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/serverImage.png"))); // NOI18N
        background.setMaximumSize(new java.awt.Dimension(1780, 1186));
        getContentPane().add(background);
        background.setBounds(0, 0, 800, 500);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_startActionPerformed

    private void enteredServerCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enteredServerCountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_enteredServerCountActionPerformed

    private void appsChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appsChooserActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int i = fc.showOpenDialog(rootPane);
        if (i == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            SharedData.metadataPath = f.getParentFile().getPath() + "\\" + SharedData.metadataPath;
            SharedData.programs_FE_Path = f.getParentFile().getPath() + "\\" + SharedData.programs_FE_Path;
            SharedData.programs_BE_Path = f.getParentFile().getPath() + "\\" + SharedData.programs_BE_Path;
            SharedData.programs_DS_Path = f.getParentFile().getPath() + "\\" + SharedData.programs_DS_Path;
            appsChooser.setText("Selected");
            appsChooser.setEnabled(false);
        }
    }//GEN-LAST:event_appsChooserActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ServerFrontEnd.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ServerFrontEnd.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ServerFrontEnd.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServerFrontEnd.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ServerFrontEnd f = new ServerFrontEnd();
                    f.setSize(800, 530);
                    f.setVisible(true);
                } catch (URISyntaxException ex) {
                    Logger.getLogger(ServerFrontEnd.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        /*Thread saveLog=new Thread(new SaveLog());
         saveLog.setPriority(Thread.MIN_PRIORITY);
         saveLog.setDaemon(true);
         saveLog.start();*/
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton appsChooser;
    private javax.swing.JLabel background;
    private javax.swing.JTextField enteredPort;
    private javax.swing.JTextField enteredServerCount;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JToggleButton start;
    public static javax.swing.JTextArea status;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JScrollPane statusPane;
    // End of variables declaration//GEN-END:variables

}
