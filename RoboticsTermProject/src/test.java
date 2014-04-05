/*
 * Author: Daniel Mather
 * Purpose: Some testing code for our COMP-3012 term project...
 * testing out more advanced concepts. 
 */

// Giant block of imports
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.Color;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.RangeFinderAdaptor;
import lejos.robotics.mapping.OccupancyGridMap;
import lejos.robotics.objectdetection.RangeFeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.hardware.sensor.HiTechnicCompass;
import lejos.robotics.DirectionFinder;
import lejos.robotics.DirectionFinderAdaptor;
import java.util.HashMap;
import java.lang.Math;

public class test
{
	/*
	 * Initialize all constant variables here. All of these should be in CM or
	 * CM/S.
	 */
	// track width is distance between wheels
	static final double TRACK_WIDTH = 16f;
	// Track length is wheel diameter
	// Officially the track is about 29cm long (unwrapped)
	// 29/pi = 9.2309cm diameter if the track were round
	// Note it doesn't behave this way. Differential pilot and tracks
	// do not play well.
	static final double TRACK_LENGTH = 30f;
	static final int ARENA_LENGTH = 115;
	static final int ARENA_WIDTH = 115;
	static final int ARENA_PRECISION = 5;
	static final int MOVE_SPEED = 25;
	static final int MOTOR_ACCELERATION = 400;
	static final int ROTATE_SPEED = 100;
	static final int BOT_WIDTH = 20;
	static final int BOT_LENGTH = 30;
	static final int OCCUPIED_THRESHOLD = 1;
	static final int FREE_THRESHOLD = 0;
	// Hash map for rotation angles
	static HashMap<Integer, Double> rotation; 
	static Thread mapper;
	static private double heading;
	// Offset is negative because the sensor is reading too far
	static final double RANGE_SENSOR_OFFSET = -4.25;
	// Readings around 90 are just too flaky, we also have to account for the
	// offset in this as well.
	static final double SENSOR_MAX_RANGE = 80 + RANGE_SENSOR_OFFSET;
	// Bot origin is the bots center point in respect to origin (0,0)
	// this is in x, y
	static final double[] BOT_ORIGIN = {BOT_WIDTH/2, BOT_LENGTH/2};
	static double[] POS = BOT_ORIGIN;

	public static void main(String[] args)
	{
		// We define the initial grid of the our arena (115x115cm)
		// We define that the precision is 5cm
		OccupancyGridMap map = new OccupancyGridMap(ARENA_LENGTH, ARENA_WIDTH,
				OCCUPIED_THRESHOLD, FREE_THRESHOLD, ARENA_PRECISION);
		
		rotation = new HashMap<Integer, Double>();
		// We're gonna have use this 90 to map out our iterations or 90
		// rotations. I'd like to know why this number is 900 something
		// but I suppose I'll probably never know.
		rotation.put(90, new Double(931.25));
		
		// Instantiate a new differential pilot
		// It should be noted that the differential pilot
		// was not designed for use with tracked vehicles
		DifferentialPilot pilot = new DifferentialPilot(TRACK_LENGTH,
				TRACK_WIDTH, Motor.B, Motor.C, true);
		
		// Create our basic IR sensor for range keeping purposes
		EV3IRSensor range = new EV3IRSensor(SensorPort.S2);

		// The following steps are setup for the IR range sensor
		// Set up the range adapter using the IR sensors distance mode
		RangeFinderAdaptor rangeAdapter = new RangeFinderAdaptor(
				range.getDistanceMode());
		
		// Initialize these values so they can be used in the loop
		int cur_x = -1;
		int cur_y = -1;
		double obj_range = -1;
		int fixed_travel_amount = 5;
		
		// This loop will move forward (or in the height attribute for our map)
		for(double distance_moved = 0; distance_moved + BOT_LENGTH + 5 < map.getHeight(); distance_moved += fixed_travel_amount)
		{
			LCD.drawString("Dist Moved: " + distance_moved, 0, 2);
			cur_x =  (int)POS[0]/5;
			cur_y = (int)POS[1]/5;
			obj_range = convert_dm_to_cm(rangeAdapter.getRange()) + RANGE_SENSOR_OFFSET;
			if(obj_range < SENSOR_MAX_RANGE)
			{
				LCD.drawString("Range: " + obj_range, 0, 4);
				LCD.drawString("POS: " + POS[0] + "," + POS[1], 0, 3);
				// Assume increments are in 5cm
				for(int i = cur_x + (int)obj_range/5; i<map.getWidth()/5; i+=5)
				{
					try
					{
						// Sleep for half a second
						Thread.sleep(500);
					}
					catch(InterruptedException e)
					{
						return;
					}
					LCD.clear(5);
					map.setOccupied(i, cur_y, 1);
					LCD.drawString("Pos: " + i + "," + cur_y + " occupid", 0, 5);
				}
			}
			pilot.travel(convert_cm_to_mm(-fixed_travel_amount));
			moved_y(fixed_travel_amount);
			LCD.clear(4);
			LCD.clear(3);
			LCD.clear(2);
		}
		
		// 
		pilot.rotate(rotation.get(90)*-1);
		
		// This loop will move forward (or in the height attribute for our map)
				for(double distance_moved = 0; distance_moved + BOT_WIDTH + 15 < map.getWidth(); distance_moved += fixed_travel_amount)
				{
					LCD.drawString("Dist Moved: " + distance_moved, 0, 2);
					cur_x =  (int)POS[0]/5;
					cur_y = (int)POS[1]/5;
					obj_range = convert_dm_to_cm(rangeAdapter.getRange()) + RANGE_SENSOR_OFFSET;
					if(obj_range < SENSOR_MAX_RANGE)
					{
						LCD.drawString("Range: " + obj_range, 0, 4);
						LCD.drawString("POS: " + cur_x + "," + cur_y, 0, 3);
						// Assume increments are in 5cm
						for(int i = cur_y + (int)obj_range/5; i<map.getHeight()/5; i+=5)
						{
							try
							{
								// Sleep for half a second
								Thread.sleep(500);
							}
							catch(InterruptedException e)
							{
								return;
							}
							LCD.clear(5);
							map.setOccupied(cur_x, i, 1);
							LCD.drawString("Pos: " + i + "," + cur_y + " occupid", 0, 5);
						}
					}
					pilot.travel(convert_cm_to_mm(-fixed_travel_amount));
					moved_x(fixed_travel_amount);
					LCD.clear(4);
					LCD.clear(3);
					LCD.clear(2);
				}
		
		range.close();
		
		// Need return so we don't run other code...
		if(Button.waitForAnyPress() > 0)
		{
			return;
		}
			
		if(obj_range < SENSOR_MAX_RANGE)
		{
			LCD.drawString("Range: " + obj_range, 0, 6);
			Button.waitForAnyPress();
			range.close();
			return;
		}
		
		//while(true)
		//{
			// TODO: Find out what the hell units this returns, it's definitely not CM
			// It appears to be in decimeters, and is somewhat accurate at closer ranges
			// perhaps +/- 10%.
			//double myRange = rangeAdapter.getRange();
			//LCD.drawString("Range: " + myRange, 0, 5);
			//int button = Button.waitForAnyPress();
			//if(button != 2)
			//	break;
			//LCD.clear();
		//}
		
		// Set up a feature detector with the range adapter and a min/max
		// distance
		// Searches for an object with a max distance of 100cm every 400ms.
		//RangeFeatureDetector features = new RangeFeatureDetector(rangeAdapter,
		//		100.0f, 400);
		
		//FeatureListener listener = new DetectedObjectListener(pilot, map, 
		//		BOT_WIDTH, BOT_LENGTH);
		// Add a lister to the feature detector
		//features.addListener(listener);

		//double length = rangeAdapter.getRange();
		//double current_pos = length;
		//LCD.drawString("Dist to edge: " + length, 0, 1);

		// On our bot backward is forward.
		//pilot.backward();
		//pilot.rotate(90);
		//pilot.travel(-100);
		//LCD.drawString("Moved: " + pilot.getMovementIncrement(), 0, 6);
		//LCD.drawString("Range Aft: " + rangeAdapter.getRange(), 0, 4);
		// Forward is backward
		//pilot.backward();
		
		HiTechnicCompass compass = new HiTechnicCompass(SensorPort.S4);
		// Instantiate a new compass adaptor so we can get a direction.
		DirectionFinderAdaptor dir = new DirectionFinderAdaptor(compass.getCompassMode());
		
		calibrate_compass(dir, pilot);
		
		//while(true)
		//{
		//	
		//	LCD.drawString("Direction: " + dir.getDegreesCartesian(), 0, 6);
		//	if(Button.waitForAnyPress() == 2)
		//		break;
		//	LCD.clear(6);
		//}
		
		// Set speed to 10cm/s and then move 50cm
		pilot.setAcceleration(MOTOR_ACCELERATION);
		pilot.setRotateSpeed(ROTATE_SPEED);
		pilot.setTravelSpeed(MOVE_SPEED);
		
		//mapper = new Thread(new mymapper(rangeAdapter, map, pilot));
		
		try
		{
			// Sleep for two seconds
			Thread.sleep(10000);
		}
		catch(InterruptedException e)
		{
			return;
		}
		
		// Set zero to be going forward (all directions now reference this
		// as being zero).
		dir.resetCartesianZero();
		set_direction(0);
		
		// Units are in cm
		for(int i = 0; i<15; i++)
		{
			LCD.clear(6);
			pilot.travel(convert_cm_to_mm(-15));
			float rot = 90;
			//set_direction(rot);
			pilot.rotate(rotation.get(90));
			// Make any corrections
			correct_angle(pilot, dir, rot);
			LCD.drawString("Direction: " + dir.getDegreesCartesian(), 0, 6);
		}
		
		//mapper.run();
		// Positive rotations are CW and negative are CCW
		
		//pilot.travel(convert_cm_to_mm(-1));
		//pilot.rotate(-1*rotation.get(90));
		
		Button.waitForAnyPress();
		
		// close the sensors that we're using
		compass.close();
		range.close();
	}
	
	public static void moved_x(double amount)
	{
		POS[0] = BOT_ORIGIN[0] + amount;
	}
	
	public static void moved_y(double amount)
	{
		POS[1] = BOT_ORIGIN[1] + amount;
	}
	
	public static double[] get_cur_pos()
	{
		return POS;
	}
	
	// A method that will be able to be used to make course corrections
	public static void correct_angle(DifferentialPilot pilot,
			DirectionFinderAdaptor dir, double angle)
	{
		pilot.setRotateSpeed(400);
		double t_direc = Math.abs(get_direction()+angle);
		LCD.drawString("Dir: " + t_direc, 0, 4);
		
		double cur_direc = dir.getDegreesCartesian();
		
		while(cur_direc < t_direc-5 || cur_direc > t_direc+5)
		{
			LCD.clear(4);
			// Try and correct itself in small increments, it may go all the way around in
			// a circle.
			// rotation is a hashmap that contains an approximate value for 90 degrees
			pilot.rotate(rotation.get(90)/18);
			cur_direc = dir.getDegreesCartesian();
			LCD.drawString("Dir: " + t_direc, 0, 4);
		}
		set_direction(dir.getDegreesCartesian());
	}
	
	// Perform the steps to calibrate the compass before we can use it
	public static void calibrate_compass(DirectionFinderAdaptor dir,
			DifferentialPilot pilot)
	{
		// Slow down our rotation for calibration
		pilot.setRotateSpeed(90);
		// Start the calibration, we need to rotate at least 2 times, in 40 seconds
		dir.startCalibration();
		// Rotate two full circles
		// 720 is 8 90 degree rotations, look at the 90 degree declaration in
		// rotate to see what 90 degrees actually is.
		pilot.rotate(8*rotation.get(90));
		dir.stopCalibration();
				
		try
		{
			// Sleep for two seconds
			Thread.sleep(2000);
		}
		catch(InterruptedException e)
		{
			return;
		}
	}
	
	public static double get_direction()
	{
		return heading;
	}
	
	public static void set_direction(float dir)
	{
		heading = dir;
	}
	
	public static double convert_mm_to_cm(double mm)
	{
		return mm/10;
	}
	
	public static double convert_cm_to_mm(double cm)
	{
		return cm*10;
	}
	
	public static double convert_dm_to_cm(double dm)
	{
		// Centimeters
		return dm*10;
	}
	
	public static int[] get_grid_location(RangeFinderAdaptor range,
			DifferentialPilot pilot)
	{
		int[] grid_pos = new int[2];
		double distance_y = range.getRange();
		// Rotate 90 degrees
		pilot.rotate(rotation.get(90));
		double distance_x = range.getRange();
		
		// Make my grid position
		grid_pos[0] = (int)distance_x/5;
		grid_pos[1] = (int)distance_y/5;
		return grid_pos;
	}
}
