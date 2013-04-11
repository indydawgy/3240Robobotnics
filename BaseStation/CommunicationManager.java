import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
	private Listener myListener ;
	public class Listener extends Thread {

		ArrayList<String> listenedStrings = new ArrayList<String>();
		DataInputStream myincoming;

		public Listener(DataInputStream incoming) throws IOException {
			myincoming = incoming;
		}

		public void run() {

			while (true) {
				try {
					listenedStrings.add(incoming.readUTF());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		public synchronized String[] readLines() {
			String[] returnedStrings = new String[listenedStrings.size()];
			returnedStrings = listenedStrings.toArray(returnedStrings);
			listenedStrings.clear();
			return returnedStrings;
		}
	}

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
			 * try { this.sleep(100); } catch (InterruptedException e1) { //
			 * TODO Auto-generated catch block e1.printStackTrace(); }
			 */
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

			for (String x : myListener.readLines()) {
				String[] split = x.split(" ");
				int commandID = Integer.parseInt(split[0]);
				String restOfString = "";
				for (int i = 2; i < split.length; i++) {
					restOfString += split[i];
				}
				if (split[1] == this.ACKSYMBOL) {
					acknowledged.put(commandID, restOfString);
				} else if (acknowledged.containsKey(commandID)) {
					acknowledged.remove(commandID);
					completed.put(commandID, restOfString);

				}

				// debug text
				System.out.println(x);
			}
		
	}

	private void send() throws IOException {
		// TODO Auto-generated method stub
		while (!tobesent.isEmpty()) {
			// send over
			String sent = tobesent.poll();
			outgoing.writeUTF(sent);
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
		connection.open(nxtInfo[0]);
		// open up datainput and output streams;
		//start up listener

		outgoing = new DataOutputStream(connection.getOutputStream());
		incoming = new DataInputStream(connection.getInputStream());
		
		(myListener = new Listener(incoming)).start();
		bytebuffer = new byte[256];

		running = true;
	}
}