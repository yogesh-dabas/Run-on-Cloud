import java.net.*;
import java.io.*;
import java.util.*;

class Client{
	
	public static Process p3;//Process for the execution of Programs
	public static int computationTime;
	public static boolean killed;
	
	public static void main(String args[]) throws IOException{
		Scanner sc =new Scanner(System.in);
		
		try{
			System.out.println("\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			System.out.println("               MINI-CLOUD CLIENT                   ");
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			
			System.out.print("Enter your Client ID: ");
			String id=sc.nextLine();
			System.out.print("\n\nEnter the Server IP Address: ");
			String ip=sc.nextLine();
			System.out.print("\n\nEnter the Port Number: ");
			int port=Integer.parseInt(sc.nextLine());
			
			
			/*String ip="localhost";
			
			int port=Integer.parseInt("8088");*/
			
			System.out.println("\n\nConnecting to Server............... Please Wait....\n\n");
			
			
			//Usage Thread
			StringBuffer waitTillEnd=new StringBuffer();
			
			Thread t1=new Thread(new ClientUsage(ip,port,id),"StatisticsSender");
			
			Thread t2=new Thread(new ClientUsage(ip,port,id),"ProgramExecRequestHandler");
			
			t1.setPriority(10);
			t1.start();
			
			t1.setPriority(1);
			t2.start();
			
		}catch(NumberFormatException e){
			System.out.println("ERROR!! Invalid Port Number Entered!!");
		}
	}
}
