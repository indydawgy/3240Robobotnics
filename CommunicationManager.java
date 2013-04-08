import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTCommandConnector;
import lejos.pc.comm.NXTInfo;

public class CommunicationManager extends Thread {

	public static String ACKSYMBOL = "ack";
	public static String SUCCESSSYMBOL = "success";
	public static String ERRORSYMBOL = "error";

	NXTComm connection;
	DataInputStream incoming;
	DataOutputStream outgoing;
	boolean running;
	byte[] bytebuffer;
	private Queue<String> tobesent = new LinkedList<String>();
	public Hashtable<Integer, String> sent = new Hashtable<Integer, String>();
	public Hashtable<Integer, String> acknowledged = new Hashtable<Integer, String>();
	public Hashtable<Integer, String> completed = new Hashtable<Integer, String>();

	public void sendCommand(String commandString) {
		tobesent.add(commandString);
	}

	public void run() {
		// initialize connection
		try {
			initialize();
		} catch (NXTCommException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (running) {
			/*
			try {
				this.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			update();

		}

		destroy();
	}

	void update() {
		// listen
		listen();
		// send
		try {
			send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void listen() {
		// TODO Auto-generated method stub
		byte [] incomingbytes = null;
		try {
			
if(incoming.available()>0)
{
				incomingbytes = new byte[incoming.available()];
				incoming.readFully(incomingbytes);
				System.out.println("Recieving: " + new String(incomingbytes));
			// pop event for GUI
}
		} catch (Exception e) {
			// do nothing
			System.out.println(e.toString());
		}

		if (incomingbytes != null)
		{
			
			String[] output = new String(incomingbytes).split("\0");
			for (String x : output) {
				String[] split = x.split(" ");
				int commandID = Integer.parseInt(split[1]);
				String restOfString = "";
				for (int i = 2; i < split.length; i++) {
					restOfString += split[i];
				}
				if (split[0] == this.ACKSYMBOL) {
					acknowledged.put(commandID, restOfString);
				} else if (acknowledged.containsKey(commandID)) {
					acknowledged.remove(commandID);
					completed.put(commandID, restOfString);

				}

				// debug text
				System.out.println(x);
			}
		}
	}

	private void send() throws IOException {
		// TODO Auto-generated method stub
		while (!tobesent.isEmpty()) {
			// send over
			String sent = tobesent.poll();
			System.out.println("Sending" +sent );
			byte [] bytes = sent.getBytes();
			outgoing.write(bytes);
			outgoing.flush();
		}
	}

	public void destroy() {
		try {
			incoming.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			outgoing.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initialize() throws NXTCommException, IOException {
		connection = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
		NXTInfo[] nxtInfo = connection.search(null, NXTCommFactory.BLUETOOTH);
		connection.open(nxtInfo[0], NXTCommFactory.BLUETOOTH);
		// open up datainput and output streams;
		

		outgoing = new DataOutputStream(connection.getOutputStream());
		incoming = new DataInputStream(connection.getInputStream());
		bytebuffer = new byte[256];

		running = true;
	}
}