
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.DiskUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import java.io.*;
import java.net.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

class ClientUsage implements Runnable {

    Sigar sig;
    String os;
    String ip;
    int port;
    String clientId;

    ClientUsage(String ip, int port, String id) throws IOException {
        this.ip = ip;
        this.port = port;
        clientId = id;

        os = os = System.getProperty("os.name");
        if (os.startsWith("Linux")) {
            Runtime.getRuntime().load("/home/yogeshdabas/Java Lab/sigar/libsigar-amd64-linux.so");
        }
        sig = new Sigar();
    }

    public void run() {

        //Client will send the statistics after every 5 seconds and wait for the acknoledgement from server
        try {
            if (Thread.currentThread().getName().equals("StatisticsSender")) {
                Socket s = new Socket(ip, port);
                PrintStream ps = new PrintStream(s.getOutputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                ps.println(clientId);
                ps.println(Thread.currentThread().getName());
				
                while (true) {
                    long pid = sig.getPid();	//Current Process ID

                    CpuPerc c = sig.getCpuPerc();
                    //double cpu=(100-(c.getIdle()*100));
                    double cpu = sig.getProcCpu(pid).getPercent();
                    //System.out.println("Total CPU Utilization % : "+cpu);
                    ps.println("cpu:" + cpu);

                    Mem m = sig.getMem();
                    //double mem=((m.getTotal()-m.getFree())/(double)m.getTotal()*100);
                    //System.out.println("RAM : "+sig.getProcMem(pid).getSize()+" "+ sig.getProcMem(pid).getResident());
                    double mem = (double) sig.getProcMem(pid).getResident() * 100 / sig.getProcMem(pid).getSize();
                    //System.out.println("RAM Utilization %: "+mem);
                    ps.println("mem:" + mem);

                    if (os.startsWith("Linux")) {
                        File f = new File("/");
                        DiskUsage du = sig.getDiskUsage("/");
                        long dsk = ((du.getWriteBytes() + du.getReadBytes()) / 2) * 100 / f.getTotalSpace();
                        //System.out.println("Disk Write: "+du.getWriteBytes());
                        //System.out.println("Disk Read: "+du.getReadBytes());
                        ps.println("dsk:" + dsk);
                    } else if (os.startsWith("Window")) {
                        File f = new File("C:");
                        DiskUsage du = sig.getDiskUsage("C:");
                        //long dsk=((du.getWriteBytes()+du.getReadBytes())/2)*100/f.getTotalSpace();
                        long dsk = (f.getUsableSpace() * 100) / f.getTotalSpace();
                        //System.out.println("Disk Write: "+du.getWriteBytes());
                        //System.out.println("Disk Read: "+dsk);
                        ps.println("dsk:" + dsk);
                    }

                    //sleep for 2 sec
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.out.println("Error->" + e.getMessage());
                    }
                }
            } else if (Thread.currentThread().getName().equals("ProgramExecRequestHandler")) {
                while (true) {
                    Socket s = new Socket(ip, port);
                    PrintStream ps = new PrintStream(s.getOutputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                    ps.println(clientId);
                    ps.println(Thread.currentThread().getName());
					
                    //Receive acknowledgement from server
                    String ack = br.readLine();

                    //Save the Program in temporary file and execute it
                    if (ack.startsWith("Program")) {

                        String programName = ack.substring(8, ack.length());

                        System.out.println(programName + " Execution Request Received.....");

                        String msg;
						
						//Receive Program BE Code
                        BufferedWriter out = new BufferedWriter(new FileWriter(programName + ".java"));
                        out.write("package tmp;");
						msg = br.readLine();
						msg = br.readLine();
						Client.computationTime=Integer.parseInt(msg.substring(2,msg.length()));
						
                        while (!(msg = br.readLine()).equals("stop")) {
                            out.write(msg);
                            out.write('\r');
                            out.write('\n');
                        }
                        out.close();
						
						//Receive Program DS Code
						out = new BufferedWriter(new FileWriter(programName + "_DS"+".java"));
                        out.write("package tmp;");
                        while (!(msg = br.readLine()).equals("stop")) {
                            out.write(msg);
                            out.write('\r');
                            out.write('\n');
                        }
                        out.close();

						//Receive Program DS_Obj Code
						FileOutputStream fout = new FileOutputStream("tmp\\"+
						programName + "_DS_Obj");
						String tmp=br.readLine();
						String t[]=tmp.split(" ");
						for(String k:t){
							fout.write((char)Integer.parseInt(k));
						}
                        fout.close();
						
                        
						/*~~~~~~~~~~~~~~~~~~~~COMPILING Program DS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                        Runtime r1 = Runtime.getRuntime();
                        String st1 = "javac -d . " + programName + "_DS.java";
                        Process p1 = r1.exec(st1);
                        BufferedReader errorStream1 = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
                        String l1 = errorStream1.readLine();
						
                        /*~~~~~~~~~~~~~~~~~~~~COMPILING Program BE~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                        Runtime r2 = Runtime.getRuntime();
                        String st2 = "javac -d . " + programName + ".java";
                        Process p2 = r2.exec(st2);
                        BufferedReader errorStream2 = new BufferedReader(new InputStreamReader(p2.getErrorStream()));
                        String l2 = errorStream2.readLine();
						
						
                        if (l1 != null || l2!=null) {
							//Send feedback to server
                            ps.println("compileTimeException:");
                            System.out.println("Compile Time Error in " + programName + ".... Execution Failed!!");
							
							//Send feedback to server
                            System.out.println(l1);
							ps.println(l1+",");
                            while ((l1 = (errorStream1.readLine())) != null) {
                                System.out.println(l1);
								ps.println(l1+",");
                            }
							System.out.println(l2);
							ps.println(l2+",");
                            while ((l2 = (errorStream2.readLine())) != null) {
                                System.out.println(l2);
								ps.println(l2+",");
                            }
                            
                        } else {
                            System.out.println("\nRunning the Program.....\n");
                            System.out.println("\n\n------------------   Running " + programName + "  ---------------------\n");
                            /*~~~~~~~~~~~~~~~~~~~~~~Running~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
							
								/*String[] params = null;
                                Class<?> cls = Class.forName("tmp." + programName);
                                Method m = cls.getDeclaredMethod("main", String[].class);
                                m.invoke(null, (Object) params);*/
								
								Runtime r3 = Runtime.getRuntime();
								String st3 = "java tmp." + programName;
								Client.p3 = r3.exec(st3);
								
								//Start the thread to kill process if TimeElapsed
								Thread t3=new Thread(this,"kill");
								t3.start();
								
								try{
									Client.p3.waitFor();
								}catch(InterruptedException ex){
									
								}
								
								//If process is killed due to time Elapsed  i.e exit value==1
								if(Client.killed){
									Client.killed=false;
									ps.println("Killed:Program Execution is killed,Since it exceeds a certain threshold Computation time limit!!");
								}else{
									//If process is terminated normally
								BufferedReader errorStream3 = new BufferedReader(new InputStreamReader(Client.p3.getErrorStream()));
								String l3 = errorStream3.readLine();
								if(l3!=null){
									//Send feedback to server
									ps.println("runTimeException:");
									System.out.println(l3);
									ps.println(l3+",");
									while ((l3 = (errorStream3.readLine())) != null) {
										System.out.println(l3);
										ps.println(l3+",");
									}
								}else{
									//Send feedback to server
									ps.println("success:");
									BufferedReader resultStream3 = new BufferedReader(new InputStreamReader(Client.p3.getInputStream()));
									String res;
									while((res= resultStream3.readLine())!=null){
										System.out.println(res);
										ps.println(res+",");
									}
                                }
								}
                        }
						s.close();

                        System.out.println("\n\n------------------   Exit " + programName + "  ---------------------\n");

                        //Flushing temporary files
                        File f = new File(programName + ".java");
                        if (f.exists()) {
                            f.delete();
                        }
						f = new File(programName + "_DS.java");
                        if (f.exists()) {
                            f.delete();
                        }
						

                        System.out.println("\n\nWaiting For Program's Request from Server................\n");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            System.out.println("Error->" + e.getMessage());
                        }
                    } else if (ack.equals("CarryOn")) {
                        //System.out.println("CarryOn");
                    }
                }

            }else if(Thread.currentThread().getName().equals("kill")){
				try {
                    Thread.sleep(Client.computationTime);
					if(Client.p3.isAlive()){
						Client.killed=true;
						Client.p3.destroy();
					}
                } catch (InterruptedException e) {
                    System.out.println("Error->" + e.getMessage());
                }
			}
        } catch (SigarException e) {
            System.out.println("Error->" + e.getMessage());

        } catch (ConnectException e) {
            System.out.println("Server is down or not available currently..... Please try again later.... ");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("ERROR!! " + e.getMessage());
            e.printStackTrace();
        }
    }
}
