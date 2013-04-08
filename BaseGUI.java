import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

import javax.management.timer.Timer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;

import lejos.pc.comm.NXTCommException;


public class BaseGUI extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	
	public static Hashtable<Integer, String> commandPool = new Hashtable<Integer,String>();
	public static ArrayList<KeyEvent> keys = new ArrayList<KeyEvent>();
	private static CommunicationManager ComManager;
	private static int counter;
	static Scanner myScanner;
	private JTextField speedField;
	private JTextField locationField;
	private JTextField directionField;
	private JTextField ultrasoundField;
	private JTextField collisionField;
	private static BaseGUI frame;
	
	private static int locationUpdate = -1000000;
	private static int directionUpdate = -100000;
	private static int ultrasoundUpdate = -10000;
	private static int touchUpdate = -10000;
	
	private static int speed;
	private static int framesElapsed = 0;
	
	private static final int SPEED_MAX = 15;
	private static final int SPEED_MIN = 0;
	private static final String SUCCESS_RETURN = "success";
	private static final int UPDATE_FRAME_COUNT = 5;
	
	private static boolean wPressed = false;
	private static boolean aPressed = false;
	private static boolean sPressed = false;
	private static boolean dPressed = false;
	
	private static final String UPDATE_LOCATION = "Compass Location";
	private static final String UPDATE_DIRECTION = "Compass Direction";
	private static final String UPDATE_ULTRASOUND = "Ultrasound Value";
	private static final String UPDATE_TOUCH = "Touch Value";
	
	
	
	public static void issueCommand(String mycommand)
	{
		System.out.println("Message Recieved: " + mycommand);
		String[] command = mycommand.split(" ");
		if (command.length > 2)
		{
			if (command[0] == "Compass" && command[1] == "Position")
			{
				locationUpdate = counter;
			}
			else if (command[0] == "Compass" && command[1] == "Direction")
			{
				directionUpdate = counter;
			}
			else if (command[0] == "Ultrasound" && command[1] == "Value")
			{
				ultrasoundUpdate = counter;
			}
			else if (command[0] == "Touch" && command[1] == "Value")
			{
				touchUpdate = counter;
			}
		}
		commandPool.put(counter, mycommand);
		counter++;
		ComManager.sendCommand(counter + " " + mycommand);
	}
	
	public static void main(String[] args) throws NXTCommException, IOException {
		
		//(ComManager = new CommunicationManager()).start();
		speed = RobotState.GetInstance().getSpeed();
		ComManager = new CommunicationManager();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new BaseGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		ComManager.initialize();
		long lastUpdate = System.nanoTime() / 1000000000;
		while (true)
		{
			ComManager.update();
			//System.out.println(System.nanoTime());
			if (lastUpdate < System.nanoTime() / 1000000000)
			{
				System.out.println("Update" + lastUpdate);
				lastUpdate = System.nanoTime() / 1000000000;
				
				framesElapsed++;
				if (framesElapsed >= UPDATE_FRAME_COUNT)
				{
					System.out.println("Sending Update Messages");
					issueCommand(UPDATE_LOCATION);
					issueCommand(UPDATE_DIRECTION);
					issueCommand(UPDATE_ULTRASOUND);
					issueCommand(UPDATE_TOUCH);
					
					framesElapsed = 0;
				}
				
				String command;
				if (wPressed)
				{
					command = "Pilot Move .1";
					issueCommand (command);
				}
				else if(sPressed)
				{
					command = "Pilot Move -.1";
					issueCommand(command);
				}
				else if(aPressed)
				{
					command = "Pilot ChangeDirection 3";
					issueCommand(command);
				}
				else if(dPressed)
				{
					command = "Pilot ChangeDirection -3";
					issueCommand(command);
				}
				
				if (ComManager.completed.containsKey(locationUpdate))
				{
					String returnMessage = ComManager.completed.get(locationUpdate);
					String[] splitReturnMessage = returnMessage.split(" ");
					if (splitReturnMessage[1] == SUCCESS_RETURN)
					{
						String location = splitReturnMessage[2];
						location = location.substring(1, location.length() - 1);
						String[] splitLocation = location.split(",");
						
						RobotState.GetInstance().setLocationX(Integer.parseInt(splitLocation[0]));
						RobotState.GetInstance().setLocationY(Integer.parseInt(splitLocation[1]));
					}
				}
	//			else if (ComManager.completed.containsKey(directionUpdate))
	//			{
	//				String returnMessage = ComManager.completed.get(directionUpdate);
	//				String[] splitReturnMessage = returnMessage.split(" ");
	//				if (splitReturnMessage[1] == SUCCESS_RETURN)
	//				{
	//					RobotState.GetInstance().setDirection(Double.parseDouble(splitReturnMessage[2]));
	//				}
	//			}
				if (ComManager.completed.containsKey(ultrasoundUpdate))
				{
					String returnMessage = ComManager.completed.get(ultrasoundUpdate);
					String[] splitReturnMessage = returnMessage.split(" ");
					if (splitReturnMessage[1] == SUCCESS_RETURN)
					{
						RobotState.GetInstance().setUltrasound(Integer.parseInt(splitReturnMessage[2]));
					}
				}
	//			else if (ComManager.completed.containsKey(touchUpdate))
	//			{
	//				String returnMessage = ComManager.completed.get(touchUpdate);
	//				String[] splitReturnMessage = returnMessage.split(" ");
	//				if (splitReturnMessage[1] == SUCCESS_RETURN)
	//				{
	//					RobotState.GetInstance().setTouch(Boolean.parseBoolean(splitReturnMessage[2]));
	//				}
	//			}
				
				if (frame != null)
				{
					frame.speedField.setText(String.valueOf(RobotState.GetInstance().getSpeed()));
					frame.locationField.setText("(" + RobotState.GetInstance().getLocationX() + ", " + RobotState.GetInstance().getLocationY() + ")");
					frame.directionField.setText(String.valueOf(RobotState.GetInstance().getDirection()));
					frame.ultrasoundField.setText(String.valueOf(RobotState.GetInstance().getUltrasound()));
					frame.collisionField.setText(String.valueOf(RobotState.GetInstance().getTouch()));
				}
			}
		}
	}

	/**
	 * Create the frame.
	 */
	public BaseGUI() {
		setTitle("Base Station");
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent input) {
				//System.out.println(input.getKeyChar());
				if (input.getKeyCode() == KeyEvent.VK_W)
				{
					wPressed = true;
				}
				else if(input.getKeyCode() == KeyEvent.VK_S)
				{
					sPressed = true;
				}
				else if(input.getKeyCode() == KeyEvent.VK_A)
				{
					aPressed = true;
				}
				else if(input.getKeyCode() == KeyEvent.VK_D)
				{
					dPressed = true;
				}
				else if(input.getKeyCode() == KeyEvent.VK_Z)
				{
					if (speed < SPEED_MAX)
					{
						speed++;
						RobotState.GetInstance().setSpeed(speed);
						String command = "Pilot ChangeSpeed " + speed;
						issueCommand(command);
					}
				}
				else if(input.getKeyCode() == KeyEvent.VK_X)
				{
					if (speed > SPEED_MIN)
					{
						speed--;
						RobotState.GetInstance().setSpeed(speed);
						String command = "Pilot ChangeSpeed " + speed;
						issueCommand(command);
					}
				}
			}
			@Override
			public void keyReleased(KeyEvent input) {
				System.out.println("the Fuck?");
				if (input.getKeyCode() == KeyEvent.VK_W)
				{
					wPressed = false;
				}
				else if(input.getKeyCode() == KeyEvent.VK_S)
				{
					sPressed = false;
				}
				else if(input.getKeyCode() == KeyEvent.VK_A)
				{
					aPressed = false;
				}
				else if(input.getKeyCode() == KeyEvent.VK_D)
				{
					dPressed = false;
				}
			}
		});
		setFocusable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 871, 585);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 2, 5, 5));
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1);
		
		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JLabel label = new JLabel("Speed");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(label);
		
		speedField = new JTextField();
		speedField.setText("0");
		speedField.setEditable(false);
		panel.add(speedField);
		speedField.setColumns(10);
		
		JLabel lblLocationxy = new JLabel("Location (X,Y)");
		lblLocationxy.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblLocationxy);
		
		locationField = new JTextField();
		locationField.setEditable(false);
		locationField.setText("0");
		panel.add(locationField);
		locationField.setColumns(10);
		
		JLabel lblDirection = new JLabel("Direction");
		lblDirection.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblDirection);
		
		directionField = new JTextField();
		directionField.setEditable(false);
		directionField.setText("0");
		panel.add(directionField);
		directionField.setColumns(10);
		
		JLabel lblUltrasound = new JLabel("Ultrasound");
		lblUltrasound.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblUltrasound);
		
		ultrasoundField = new JTextField();
		ultrasoundField.setText("0");
		ultrasoundField.setEditable(false);
		panel.add(ultrasoundField);
		ultrasoundField.setColumns(10);
		
		JLabel lblCollision = new JLabel("Collision");
		lblCollision.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblCollision);
		
		collisionField = new JTextField();
		collisionField.setEditable(false);
		collisionField.setText("False");
		panel.add(collisionField);
		collisionField.setColumns(10);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2);
	}

}
