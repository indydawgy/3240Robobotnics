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
	InputStream incoming;
	OutputStream outgoing;
	boolean running;
	byte[] bytebuffer;
	private Queue<String> tobesent = new LinkedList<String>();
	public Hashtable<Integer, String> sent = new Hashtable<Integer, String>();
	public Hashtable<Integer, String> acknowledged = new Hashtable<Integer, String>();
	public Hashtable<Integer, String> completed = new Hashtable<Integer, String>();

	public void sendCommand(String commandString) {
		tobesent.add(commandString);
	}

	private void generateCommandID() {

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
			try {
				this.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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

		destroy();
	}

	private void listen() {
		// TODO Auto-generated method stub
		try {
			incoming.read(bytebuffer);

			// pop event for GUI

		} catch (Exception e) {
			// do nothing
		}
		int count = 0;
		for (int x = 0; x < bytebuffer.length; x++) {
			count += bytebuffer[x];
		}
		if (count > 0) {
			String[] output = new String(bytebuffer).split("\0");
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
				System.out.println(new String(bytebuffer));
			}
		}
	}

	private void send() throws IOException {
		// TODO Auto-generated method stub
		while (!tobesent.isEmpty()) {
			// send over
			String sent = tobesent.poll();
			System.out.println("Sending" +sent );
			outgoing.write(sent.getBytes());
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

	private void initialize() throws NXTCommException, IOException {
		connection = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
		NXTInfo[] nxtInfo = connection.search(null, NXTCommFactory.BLUETOOTH);
		connection.open(nxtInfo[0]);
		// open up datainput and output streams;
		incoming = connection.getInputStream();
		outgoing = connection.getOutputStream();

		bytebuffer = new byte[256];

		running = true;
	}
}