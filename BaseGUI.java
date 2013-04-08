import java.io.IOException;
import java.util.Hashtable;
import java.util.Scanner;

import lejos.pc.comm.NXTCommException;


public class BaseGUI {
	public static Hashtable<Integer, String> commandPool = new Hashtable<Integer,String>();
	private static CommunicationManager ComManager;
	private static int counter;
	static Scanner myScanner;
	public static void issueCommand(String mycommand)
	{
		
		commandPool.put(counter, mycommand);
		counter++;
		ComManager.sendCommand(counter + " " + mycommand);
	}
	 public static void main(String[] args) throws IOException, NXTCommException, InterruptedException { 
		 
		 	myScanner = new Scanner(System.in);
		 	(ComManager = new CommunicationManager()).start();
		 	
		 	 //update loop
		 	while (true)
		 	{
		
		 	issueCommand(myScanner.next());
		 		 ComManager.join();
		 		
		 	}
		 	
	 }
}
