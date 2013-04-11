import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.LCD;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;



public class OnBoard {

	static NXTConnection connection;
	static DataInputStream incoming;
	static DataOutputStream outgoing;
	
	public static void main(String [] args) throws IOException, InterruptedException
	{
		LCD.drawString("Waiting for bt",0, 0);
		      connection = Bluetooth.waitForConnection();


		      outgoing = connection.openDataOutputStream();

		      incoming = connection.openDataInputStream();
		      
		      while (true)
		      {
		    	  LCD.drawString("Waiting for input",0, 0);
		    	  String command = incoming.readUTF();
		    	  LCD.drawString("foundInput",0, 0);
		    	  int commandID = Integer.parseInt(command.substring(0, command.indexOf(" ")));
		    	  outgoing.writeUTF(commandID + " ack");
		    	  outgoing.flush();
		    	  LCD.drawString("sentAck",0, 0);
		    	  Thread.sleep(2000);
		    	  outgoing.writeUTF(commandID + " success 101");
		    	  outgoing.flush();
		    	  LCD.drawString("sentSuccess",0, 0);
		    	  Thread.sleep(2000);
		      }
		   
	}
}
