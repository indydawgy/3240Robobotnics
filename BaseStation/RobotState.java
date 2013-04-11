
//This is the RobotState class that holds the data values from the onboard system and is used by the GUI
//to update its shown data values

public class RobotState {
private int ultrasound;
private boolean touch;
private int speed;
private int locationX;
private int locationY;
private double direction;
private static RobotState instance = null;

private RobotState(){
	ultrasound = 0;
	touch = false;
	speed = 1;
	locationX = 0;
	locationY = 0;
	direction = 150;
}

public static RobotState GetInstance()
{
	if (instance == null)
	{
		instance = new RobotState();
	}
	
	return instance;
}

public int getUltrasound(){
	return ultrasound;
}
public boolean getTouch(){
	return touch;
}
public int getSpeed(){
	return speed;
}
public int getLocationX(){
	return locationX;
}
public int getLocationY(){
	return locationY;
}
public double getDirection(){
	return direction;
}
public void setUltrasound(int value){
	ultrasound = value;
}
public void setTouch(boolean value){
	touch = value;
}
public void setSpeed(int value){
	speed = value;
}
public void setLocationX(int value){
	locationX = value;
}
public void setLocationY(int value){
	locationY = value;
}
public void setDirection(double value){
	direction = value;
}
public String get(String key){
	if(key.equals("ultrasound"))
		return Integer.toString(getUltrasound());
	else if(key.equals("touch"))
		return Boolean.toString(getTouch());
	else if(key.equals("speed"))
		return Integer.toString(getSpeed());
	else if(key.equals("locationX"))
		return Integer.toString(getLocationX());
	else if(key.equals("locationY"))
		return Integer.toString(getLocationY());
	else if(key.equals("direction"))
		return Double.toString(getDirection());
	else
		return "";
}
public void set(String key, String value){
	if(key.equals("ultrasound"))
		setUltrasound(Integer.parseInt(value));
	else if(key.equals("touch"))
		setTouch(Boolean.parseBoolean(value));
	else if(key.equals("speed"))
		setSpeed(Integer.parseInt(value));
	else if(key.equals("locationX"))
		setLocationX(Integer.parseInt(value));
	else if(key.equals("locationY"))
		setLocationY(Integer.parseInt(value));
	else if(key.equals("direction"))
		setDirection(Double.parseDouble(value));
	else
		System.out.println("");
}
public boolean acquire(String key){
	return true;
}
public boolean release(String key){
	return true;
}
public static void main(String[] args){
	RobotState R1 = new RobotState();
	R1.setUltrasound(32);
	System.out.println("Ultrasound value is: " + R1.getUltrasound());
	R1.setTouch(true);
	System.out.println("Touch sensor has value of " + R1.getTouch());
	R1.setLocationX(13);
	R1.setLocationY(4);
	System.out.println("The Robot's location on the grid is (" + R1.getLocationX()+ ", " + R1.getLocationY() + ")");
	R1.setSpeed(32);
	System.out.println("The Robot's speed is now currently: " + R1.getSpeed());
	R1.set("direction", "40.5");
	//System.out.println("The Robot is moving in the direction of angle " + R1.getDirection() + " degrees to the normal");
	System.out.println("The robot is moving in the direction of angle " + R1.get("direction") + " degrees to the normal");
}
}
