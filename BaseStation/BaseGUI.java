import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

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

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;

import lejos.pc.comm.NXTCommException;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import java.awt.Font;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BaseGUI extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */

	public static Hashtable<Integer, String> commandPool = new Hashtable<Integer, String>();
	public static ArrayList<KeyEvent> keys = new ArrayList<KeyEvent>();
	private static CommunicationManager ComManager;
	private static int counter = 0;
	static Scanner myScanner;
	private JTextField speedField;
	private JTextField locationField;
	private JTextField directionField;
	private JTextField collisionField;
	private JProgressBar ultrasoundProgressBar;
	private static BaseGUI frame;

	private static DefaultTableModel tableData;

	private static int locationUpdate = -1000000;
	private static int directionUpdate = -100000;
	private static int ultrasoundUpdate = -10000;
	private static int touchUpdate = -10000;

	private static double speed;
	private static int framesElapsed = 0;

	private static final int SPEED_MAX = 15;
	private static final int SPEED_MIN = 0;
	private static final String SUCCESS_RETURN = "success";
	private static final int UPDATE_FRAME_COUNT = 10000;

	private static boolean wPressed = false;
	private static boolean aPressed = false;
	private static boolean sPressed = false;
	private static boolean dPressed = false;

	private static final String UPDATE_ULTRASOUND = "sensor ultrasound";
	private static final String UPDATE_TOUCH = "sensor touch";
	private static final String UPDATE_SPEED = "sensor speed";
	private static final String UPDATE_SOUND = "sensor sound";
	private static final int MAX_COMMAND_COUNT = 25;

	private JTextField textField;
	private JTable commandTable;

	// private ImageIcon x = new ImageIcon();

	public static void issueCommand(String mycommand) {
		/*
		 * System.out.println("Message Recieved: " + mycommand); String[]
		 * command = mycommand.split(" "); if (command.length == 2) { if
		 * (command[0] == "Compass" && command[1] == "Position") {
		 * locationUpdate = counter; } else if (command[0] == "Compass" &&
		 * command[1] == "Direction") { directionUpdate = counter; } else if
		 * (command[0] == "Ultrasound" && command[1] == "Value") {
		 * ultrasoundUpdate = counter; } else if (command[0] == "Touch" &&
		 * command[1] == "Value") { touchUpdate = counter; } }
		 */
		String[] row = { mycommand, "No", "No" };
		tableData.insertRow(counter, row);
		/*
		 * tableData.setValueAt(mycommand, counter % MAX_COMMAND_COUNT, 0);
		 * tableData.setValueAt("No", counter % MAX_COMMAND_COUNT, 1);
		 * tableData.setValueAt("No", counter % MAX_COMMAND_COUNT, 2);
		 */

		commandPool.put(counter, mycommand);

		ComManager.sendCommand(counter + " " + mycommand);
		counter++;
	}

	public static void main(String[] args) throws NXTCommException, IOException {

		(ComManager = new CommunicationManager()).start();
		speed = RobotState.GetInstance().getSpeed();
		// ComManager = new CommunicationManager();
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
		while (!ComManager.running) {

		}
		RobotState.GetInstance().setUltrasound(250);
		// ComManager.initialize();
		long lastUpdate = System.nanoTime() / 1000000000;
		while (true) {
			Set<Integer> keyset = ComManager.acknowledged.keySet();
			for (int i : keyset) {
				tableData.setValueAt("Yes", i, 1);

			}

			for (Entry<Integer, String> i : ComManager.getCompletedEntries()) {
				tableData.setValueAt("Yes", i.getKey(), 1);
				tableData.setValueAt("Yes", i.getKey(), 2);
				String[] split = i.getValue().split(" ");
				if (split[0].compareTo(SUCCESS_RETURN) == 0) {
					switch (commandPool.get(i.getKey()).toLowerCase()) {
					case (UPDATE_SOUND):
						RobotState.GetInstance().setSound(
								Integer.parseInt(split[1]));
						break;
					case (UPDATE_SPEED):
						RobotState.GetInstance().setSpeed(
								Double.parseDouble(split[1]));
						break;
					case (UPDATE_ULTRASOUND):
						RobotState.GetInstance().setUltrasound(
								Integer.parseInt(split[1]));
						break;
					case (UPDATE_TOUCH):
						RobotState.GetInstance().setTouch(
								Boolean.parseBoolean(split[1]));
						break;

					}
				}
				commandPool.remove(i);
			}
			// ComManager.update();
			// System.out.println(System.nanoTime());
			if (lastUpdate < System.nanoTime() / 1000000000) {
				System.out.println("Update" + lastUpdate);
				lastUpdate = System.nanoTime() / 1000000000;

				framesElapsed++;
				if (framesElapsed >= UPDATE_FRAME_COUNT) {
					System.out.println("Sending Update Messages");
					issueCommand(UPDATE_ULTRASOUND);
					issueCommand(UPDATE_TOUCH);

					framesElapsed = 0;
				}

		

			String command;
			if (wPressed) {
				command = "Pilot forward";
				issueCommand(command);
			} else if (sPressed) {
				command = "Pilot Move -.1";
				issueCommand(command);
			} else if (aPressed) {
				command = "Pilot rotate 3";
				issueCommand(command);
			} else if (dPressed) {
				command = "Pilot rotate -3";
				issueCommand(command);
			}

			if (frame != null) {
				frame.speedField.setText(String.valueOf(RobotState
						.GetInstance().getSpeed()));
				frame.locationField.setText("("
						+ RobotState.GetInstance().getLocationX() + ", "
						+ RobotState.GetInstance().getLocationY() + ")");
				frame.directionField.setText(String.valueOf(RobotState
						.GetInstance().getDirection()));
				frame.ultrasoundProgressBar.setValue(RobotState.GetInstance()
						.getUltrasound());
				if (RobotState.GetInstance().getUltrasound() < 150
						&& RobotState.GetInstance().getUltrasound() > 50) {
					frame.ultrasoundProgressBar.setForeground(Color.YELLOW);
				} else if (RobotState.GetInstance().getUltrasound() < 50) {
					frame.ultrasoundProgressBar.setForeground(Color.RED);
				}
				frame.collisionField.setText(String.valueOf(RobotState
						.GetInstance().getTouch()));
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
				// System.out.println(input.getKeyChar());
				if (input.getKeyCode() == KeyEvent.VK_W) {
					wPressed = true;
				} else if (input.getKeyCode() == KeyEvent.VK_S) {
					sPressed = true;
				} else if (input.getKeyCode() == KeyEvent.VK_A) {
					aPressed = true;
				} else if (input.getKeyCode() == KeyEvent.VK_D) {
					dPressed = true;
				} else if (input.getKeyCode() == KeyEvent.VK_Z) {
					if (speed < SPEED_MAX) {
						speed++;
						RobotState.GetInstance().setSpeed(speed);
						String command = "Pilot ChangeSpeed " + speed;
						issueCommand(command);
					}
				} else if (input.getKeyCode() == KeyEvent.VK_X) {
					if (speed > SPEED_MIN) {
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
				if (input.getKeyCode() == KeyEvent.VK_W) {
					wPressed = false;
				} else if (input.getKeyCode() == KeyEvent.VK_S) {
					sPressed = false;
				} else if (input.getKeyCode() == KeyEvent.VK_A) {
					aPressed = false;
				} else if (input.getKeyCode() == KeyEvent.VK_D) {
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
		panel_1.setLayout(null);

		JLabel lblCommandLog = new JLabel("Command Log");
		lblCommandLog.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblCommandLog.setBounds(12, 10, 126, 26);
		lblCommandLog.setFocusable(false);
		panel_1.add(lblCommandLog);

		JLabel lblEnterCommand = new JLabel("Command");
		lblEnterCommand.setBounds(12, 486, 99, 16);
		panel_1.add(lblEnterCommand);
		
		  textField = new JTextField(); textField.setFont(new Font("Tahoma",
		  Font.PLAIN, 14)); textField.setBounds(82, 483, 257, 22);
		  panel_1.add(textField); textField.setColumns(10);
		 
		JButton btnNewButton = new JButton("Enter");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				issueCommand(textField.getText());
				textField.setText("");
			}
		});
		btnNewButton.setBounds(351, 472, 56, 47);
		panel_1.add(btnNewButton);

		String[] columnNames = { "Command", "Acknowledged", "Completed" };
		tableData = new DefaultTableModel(columnNames, 0);

		JScrollPane scroll = new JScrollPane();
		scroll.setBounds(12, 41, 395, 429);
		panel_1.add(scroll);

		commandTable = new JTable(tableData);
		commandTable.setBounds(0, 0, 395, 429);
		scroll.setViewportView(commandTable);

		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblSpeed = new JLabel("Speed:");
		lblSpeed.setBounds(27, 13, 74, 43);
		lblSpeed.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblSpeed);

		speedField = new JTextField();
		speedField.setEditable(false);
		speedField.setBounds(111, 15, 67, 38);
		speedField.setText("0");
		speedField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		panel.add(speedField);
		speedField.setColumns(10);

		JLabel lblLocationxy = new JLabel("Location (X,Y):");
		lblLocationxy.setBounds(203, 5, 86, 58);
		lblLocationxy.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblLocationxy);

		locationField = new JTextField();
		locationField.setBounds(301, 13, 86, 43);
		locationField.setEditable(false);
		locationField.setText("0");
		locationField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		panel.add(locationField);
		locationField.setColumns(10);

		JLabel lblDirection = new JLabel("Direction:");
		lblDirection.setBounds(27, 69, 86, 43);
		lblDirection.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblDirection);

		directionField = new JTextField();
		directionField.setBounds(111, 71, 67, 38);
		directionField.setEditable(false);
		directionField.setText("0");
		directionField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		panel.add(directionField);
		directionField.setColumns(10);

		JLabel lblUltrasound = new JLabel("Ultrasound:");
		lblUltrasound.setBounds(27, 125, 92, 43);
		lblUltrasound.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblUltrasound);

		ultrasoundProgressBar = new JProgressBar();
		ultrasoundProgressBar.setValue(52);
		ultrasoundProgressBar.setMaximum(250);
		ultrasoundProgressBar.setBounds(121, 125, 247, 43);
		ultrasoundProgressBar.setForeground(Color.GREEN);
		ultrasoundProgressBar.setBackground(Color.GRAY);
		panel.add(ultrasoundProgressBar);

		JLabel lblCollision = new JLabel("Collision:");
		lblCollision.setBounds(183, 61, 86, 58);
		lblCollision.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblCollision);

		collisionField = new JTextField();
		collisionField.setBounds(301, 71, 67, 38);
		collisionField.setEditable(false);
		collisionField.setText("False");
		collisionField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		panel.add(collisionField);
		collisionField.setColumns(10);
	}
}
