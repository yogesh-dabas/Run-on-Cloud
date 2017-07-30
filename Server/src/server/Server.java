package server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import static server.ServerFrontEnd.status;

public class Server implements Runnable {

    /*~~~~~~~~~~~~~~~~~~~SOCKET FOR CONNECTION~~~~~~~~~~~~~~*/
    ServerSocket ss;

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     Currently connected client Info
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
    String clientId;
    String clientThreadName;

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     Stores ID of clients having minimum usage
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
    static String minCpuClient = null;
    static String minMemClient = null;
    static String minDskClient = null;

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     Storage of Clients Ulilization in Hash table
     key: clientId
     value: Object(cpu,mem,dsk)
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
    static Hashtable<String, ClientUtilization> dataBase = new Hashtable<>();

    /*~~~~~~~~~~~~~~~~~~~~Server LOG~~~~~~~~~~~~~~~~~~~~~~~~~*/
    ServerLog log;

    Server(ServerSocket ss) {
        this.ss = ss;
    }

    public void run() {

        //log
        log = new ServerLog(Thread.currentThread().getName());
        log.print("~~~~~~~~~~~~~~~~~~~~" + Thread.currentThread().getName() + " started!~~~~~~~~~~~~~~~~~~~~");

        //Continuously working of servers
        while (!Thread.currentThread().getName().equals(SharedData.serverClosedMsg)) {
            try {
                //Waiting for the client
                Socket s = ss.accept();

                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintStream ps = new PrintStream(s.getOutputStream());

                //Read the clientId
                clientId = br.readLine();
                clientThreadName = br.readLine();

                /*  If Client is a normal client and GUI is connected to -> handle it  */
                if (!clientId.startsWith("GUI")) {

                    if (clientThreadName.equals("StatisticsSender")) {
                        while (true) {
                            ClientUtilization cu;
                            //If the client is present in dataBase -> get its utilization
                            if (dataBase.containsKey(clientId)) {
                                cu = (ClientUtilization) dataBase.get(clientId);
                            } //else register that client in dataBase
                            else {
                                cu = new ClientUtilization();
                            }

                            /*Decode the message sent by client-> cpu:msg or mem:msg or dsk:msg
                             cpu: CPU Utilization
                             mem: RAM Utilization
                             dsk: Disk Utilization
                             */
                            String msg;
                            for (int i = 0; i < 3; i++) {
                                msg = br.readLine();
                                //When first client connected to server (NO other client is present)
                                if (minCpuClient == null && minMemClient == null && minDskClient == null) {

                                    cu.cpu = Double.parseDouble(msg.substring(4, msg.length()));
                                    minCpuClient = clientId;

                                    cu.mem = Double.parseDouble(msg.substring(4, msg.length()));
                                    minMemClient = clientId;

                                    cu.dsk = Double.parseDouble(msg.substring(4, msg.length()));
                                    minDskClient = clientId;

                                } //Compare new client with best resources clients
                                else if (((ClientUtilization) dataBase.get(minCpuClient)) != null && ((ClientUtilization) dataBase.get(minMemClient)) != null && ((ClientUtilization) dataBase.get(minDskClient)) != null) {
                                    if (msg.startsWith("cpu")) {
                                        cu.cpu = Double.parseDouble(msg.substring(4, msg.length()));
                                        if (cu.cpu < ((ClientUtilization) dataBase.get(minCpuClient)).cpu) {
                                            minCpuClient = clientId;
                                        }
                                    } else if (msg.startsWith("mem")) {
                                        cu.mem = Double.parseDouble(msg.substring(4, msg.length()));
                                        if (cu.mem < ((ClientUtilization) dataBase.get(minMemClient)).mem) {
                                            minMemClient = clientId;
                                        }
                                    } else if (msg.startsWith("dsk")) {
                                        cu.dsk = Double.parseDouble(msg.substring(4, msg.length()));
                                        if (cu.dsk < ((ClientUtilization) dataBase.get(minDskClient)).dsk) {
                                            minDskClient = clientId;
                                        }
                                    }
                                }
                            }

                            //If client is not present in database then its newly added client --> update status
                            if (!dataBase.containsKey(clientId)) {
                                status.setText(status.getText() + "\nClient:" + clientId + "(" + clientThreadName + ") connected to " + Thread.currentThread().getName());

                                //Add newly added client to SharedData to send their report to GUI
                                System.out.println("NEW CLIENT: " + clientId);
                                SharedData.newClients.add(clientId);

                                //log
                                log.print("\nClient:" + clientId + "(" + clientThreadName + ") connected to " + Thread.currentThread().getName());

                            }
                            dataBase.put(clientId, cu);
                        }
                    } /* 
                     Send the acknowledgement to client
                     If No Program Requests for client -> Send "CarryOn"
                     else -> Send "Program" 
                     followed by Program stream
                     */ else if (clientThreadName.equals("ProgramExecRequestHandler")) {
                        if ((ClientUtilization) dataBase.get(clientId) != null) {
                            String progReq = ((ClientUtilization) dataBase.get(clientId)).getProgramForExecIfAny();
                            if (progReq == null) {
                                ps.println("CarryOn");
                            } else {
                                System.out.println(progReq);//GUI ClientID: program
                                int tmp = progReq.indexOf(':');
                                String IdOfGuiRequestingProgramExecution = progReq.substring(0, tmp);
                                String p = progReq.substring(tmp + 1, progReq.length());
                                ps.println("Program:" + p);

                                //Send Program BackEnd .java code
                                FileInputStream fin = new FileInputStream(SharedData.programs_BE_Path + p + ".txt");
                                BufferedInputStream bin = new BufferedInputStream(fin);
                                int i;
                                String buffer = "";
                                while ((i = bin.read()) != -1) {
                                    if ((char) i == '\n') {
                                        ps.print(buffer);
                                        buffer = "";
                                    } else {
                                        buffer += (char) i;
                                    }
                                }
                                ps.println(buffer);
                                ps.println("stop");
                                fin.close();
                                bin.close();

                                //Send Program Data Structure Code
                                fin = new FileInputStream(SharedData.programs_DS_Path + p + "_DS.txt");
                                bin = new BufferedInputStream(fin);
                                buffer = "";
                                while ((i = bin.read()) != -1) {
                                    if ((char) i == '\n') {
                                        ps.print(buffer);
                                        buffer = "";
                                    } else {
                                        buffer += (char) i;
                                    }
                                }
                                ps.println(buffer);
                                ps.println("stop");
                                fin.close();
                                bin.close();

                                //Send Program DS_Obj Code
                                fin = new FileInputStream("tmp\\" + p + "_DS_Obj");
                                bin = new BufferedInputStream(fin);
                                int ch;
                                String temp = "";
                                while ((ch = bin.read()) != -1) {
                                    temp += ch + " ";
                                }
                                ps.println(temp);
                                fin.close();
                                bin.close();

                                /*~~~~~~~~~~~Receive acknowledgement from client~~~~~~~~~~~~~~~~*/
                                String feedback = IdOfGuiRequestingProgramExecution + ":";
                                String msg;
                                while ((msg = br.readLine()) != null) {
                                    feedback += msg;
                                }

                                //SharedData.serverAssignedForFeedbackReport.resume();
                                //synchronized (SharedData.feedbackMutex) {
                                SharedData.programFeedbackToGUI.put(p, feedback);
                                //SharedData.feedbackMutex.notify();
                                //}
                            }
                        } else {
                            ps.println("CarryOn");
                        }
                    }

                } else {
                    /*      
                     ~~~~~~~~~~~~~~GUI Client handling~~~~~~~~~~~~~~~       
                     */
                    String req = br.readLine();

                    /*
                     Request for Program List ---->   
                     1 Get the task list from File 
                     2 send it to GUI
                     */
                    if (req.equals("tasklist")) {

                        //log
                        log.print("Tasklist Request from " + clientId + ":- " + req);

                        FileInputStream fin = new FileInputStream(SharedData.metadataPath);
                        BufferedInputStream bin = new BufferedInputStream(fin);
                        int i;
                        String tmp = "";
                        while ((i = bin.read()) != -1) {
                            if ((char) i == '\n') {
                                ps.println(tmp.substring(0, tmp.length() - 1));
                                tmp = "";
                            } else {
                                tmp = tmp + (char) i;
                            }
                        }
                        fin.close();
                        bin.close();
                    } else if (req.startsWith("Load:")) {

                        //log
                        log.print("Request from " + clientId + ":- " + req);

                        //Sending Front End
                        FileInputStream fin = new FileInputStream(SharedData.programs_FE_Path + req.substring(5, req.length()) + "_FE.txt");
                        BufferedInputStream bin = new BufferedInputStream(fin);
                        int i;
                        String buffer = "";
                        while ((i = bin.read()) != -1) {
                            if ((char) i == '\n') {
                                ps.print(buffer);
                                buffer = "";
                            } else {
                                buffer += (char) i;
                            }
                        }
                        ps.println(buffer);
                        ps.println("stop");

                        //Sending Data Structure
                        fin = new FileInputStream(SharedData.programs_DS_Path + req.substring(5, req.length()) + "_DS.txt");
                        bin = new BufferedInputStream(fin);
                        buffer = "";
                        while ((i = bin.read()) != -1) {
                            if ((char) i == '\n') {
                                ps.print(buffer);
                                buffer = "";
                            } else {
                                buffer += (char) i;
                            }
                        }
                        ps.println(buffer);
                        ps.println("stop");

                    }/*
                     Request for Program Execution ---->   
                     1. Select the proper client machine based on algorithm
                     2. Send the program.txt to client for execution
                     3. Send the acknoledgement to GUI about Selected client machine for execution
                     */ else if (req.startsWith("Run:")) {

                        //log
                        log.print("Request from " + clientId + ":- " + req);

                        //If no clients available
                        if (minCpuClient == null && minCpuClient == null && minCpuClient == null) {
                            ps.println("noclients");
                        } else if (((ClientUtilization) dataBase.get(minCpuClient)) != null && ((ClientUtilization) dataBase.get(minMemClient)) != null && ((ClientUtilization) dataBase.get(minDskClient)) != null) {
                            String program = req.substring(4, req.length());

                            //Get Usage Requirement of Program
                            FileInputStream fin = new FileInputStream(SharedData.programs_BE_Path + program + ".txt");
                            BufferedInputStream bin = new BufferedInputStream(fin);
                            bin.skip(2);
                            int cpu = bin.read() - 48;
                            bin.skip(1);
                            int mem = bin.read() - 48;
                            bin.skip(1);
                            int dsk = bin.read() - 48;
                            fin.close();
                            bin.close();

                            //Add Program to Waiting Queue with the preferred Client
                            boolean isClientAssigned = false;
                            ClientUtilization cu = null;
                            if (cpu == 1) {
                                cu = dataBase.get(minCpuClient);
                                ps.println("clientAssigned:" + minCpuClient);
                                isClientAssigned = true;
                            } else if (mem == 1) {
                                cu = dataBase.get(minMemClient);
                                ps.println("clientAssigned:" + minMemClient);
                                isClientAssigned = true;
                            } else if (dsk == 1) {
                                cu = dataBase.get(minDskClient);
                                ps.println("clientAssigned:" + minDskClient);
                                isClientAssigned = true;
                            }

                            //If client is available to assign --> get the DS_Obj file from GUI
                            if (isClientAssigned) {
                                FileOutputStream fout = new FileOutputStream("tmp\\" + program + "_DS_Obj");
                                /*int ch;
                                 while ((ch = br.read()) != -1) {
                                 fout.write((char) ch);
                                 }
                                 fout.close();
                                 */

                                String tmp = br.readLine();
                                String t[] = tmp.split(" ");
                                for (String k : t) {
                                    fout.write((char) Integer.parseInt(k));
                                }
                                fout.close();

                            }

                            cu.addProgramExecRequest(clientId + ":" + program);//GUI client id : Program for execution

                            //Resume programFeedback to send the feedback report to client
                            //synchronized (SharedData.feedbackMutex) {
                            //Set feedback as waiting
                            SharedData.programFeedbackToGUI.put(program, clientId + ":waiting");
                            //SharedData.feedbackMutex.notifyAll();
                            //}
                        } else {
                            ps.println("noclients");
                        }

                    } else if (req.equals("programFeedback")) {

                        //while (!SharedData.guiClosed) {
                        Set set = SharedData.programFeedbackToGUI.keySet();
                        Object program[] = set.toArray();

                        for (int i = 0; i < program.length; i++) {
                            String feedback = SharedData.programFeedbackToGUI.get(program[i]);
                            int tmp = feedback.indexOf(':');
                            String IdOfGuiRequestingProgramExecution = feedback.substring(0, tmp);
                            if (clientId.equals(IdOfGuiRequestingProgramExecution)) {
                                String f = feedback.substring(tmp + 1, feedback.length());
                                ps.println(program[i] + ":" + f);
                                if (!feedback.equals("waiting")) {
                                    SharedData.programFeedbackToGUI.remove(program[i]);
                                }
                            }
                        }
                        /*synchronized (SharedData.feedbackMutex) {
                         try {
                         SharedData.feedbackMutex.wait();
                         } catch (InterruptedException ex) {
                         Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                         }
                         }*/
                        //}
                    } else if (req.equals("statistics")) {

                        String clientIdForStatistics = br.readLine();
                        System.out.println("stat: " + clientIdForStatistics);
                        if (dataBase.containsKey(clientIdForStatistics)) {
                            ClientUtilization cu = dataBase.get(clientIdForStatistics);
                            ps.println(cu.cpu + " " + cu.mem + " " + cu.dsk);
                        } else {
                            ps.println("disconnected");
                        }

                    } else if (req.equals("newClientCount")) {

                        //Send currently connected clients
                        Set set = dataBase.keySet();
                        ps.println(set.size());
                        Iterator it = set.iterator();
                        while (it.hasNext()) {
                            ps.println((String) it.next());
                        }

                    } else if (req.equals("disconnectedClientCount")) {
                        //while (!SharedData.guiClosed) {
                        ps.println(SharedData.disconnectedClients.size());
                        Iterator it = SharedData.disconnectedClients.iterator();
                        while (it.hasNext()) {
                            ps.println(it.next());
                        }
                    }

                }
                s.close();
            } catch (SocketException e) {

                //BUG::::::::::    Gui me exception aa rhi hai
                System.out.println(clientId);

                if (clientId != null && !clientId.equals("GUI") && clientThreadName.equals("StatisticsSender")) {
                    status.setText(status.getText() + "\nClient " + clientId + "(" + clientThreadName + ") disconnected! ");

                    //If client is set for minimum usage --> Reset Usage
                    if ((minCpuClient != null && minCpuClient.equals(clientId)) || (minMemClient != null && minMemClient.equals(clientId)) || (minDskClient != null && minDskClient.equals(clientId))) {
                        minCpuClient = null;
                        minMemClient = null;
                        minDskClient = null;
                    }

                    //Remove from database
                    dataBase.remove(clientId);

                    synchronized (this) {
                        SharedData.disconnectedClients.add(clientId);
                        /*synchronized (SharedData.disconnectedClientMutex) {
                         SharedData.disconnectedClientMutex.notify();
                         }*/
                    }

                    if (dataBase.isEmpty()) {
                        minCpuClient = null;
                        minMemClient = null;
                        minDskClient = null;
                    }
                }
            } catch (IOException e) {
                System.out.println("ERROR!!g " + e.getMessage());
            }
        }

        //log
        log.print("~~~~~~~~~~~~~~~~~~~~Server Closed!~~~~~~~~~~~~~~~~~~~~");
    }
}
