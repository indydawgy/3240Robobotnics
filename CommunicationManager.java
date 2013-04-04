import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import lejos.nxt.*;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class CommunicationManager {

	public class Listener extends Thread {
		
		NXTConnection connection;
		DataInputStream incoming;
		DataOutputStream outgoing;
		boolean running;
		byte[] bytebuffer; 
		
		public void run ()
		{
			//initialize connection
			initialize();
			
			while (running)
			{
				//listen
				
				//send
			}
		
		}
		private void initialize() {
			//make bluetooth connection
			connection = Bluetooth.waitForConnection();
			
			//open up datainput and output streams;
			incoming = connection.openDataInputStream();
			outgoing = connection.openDataOutputStream();

			bytebuffer = new byte[256];
			
			commandMap = new HashMap();
			while (true) {
				LCD.drawString(waiting, 0, 0);
				
				LCD.clear();
				LCD.drawString(connected, 0, 0);

				
				dis.readFully(bytebuffer);
				String n = bytebuffer.toString();
				dos.flush();

				dis.close();
				dos.close();

				LCD.clear();
				LCD.drawString(closing, 0, 0);

				connection.close();
				LCD.clear();
			}
		}
	}
	
	public class 

}
