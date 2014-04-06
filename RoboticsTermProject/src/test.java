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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

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
	static final int ARENA_LENGTH = 115/5;
	static final int ARENA_WIDTH = 115/5;
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
	static final double SENSOR_MAX_RANGE = 50 + RANGE_SENSOR_OFFSET;
	// Bot origin is the bots center point in respect to origin (0,0)
	// this is in x, y
	static final double[] BOT_ORIGIN = {BOT_WIDTH/2, BOT_LENGTH/2};
	static double[] POS = BOT_ORIGIN;
	static String MAP_FILE = "/map.csv";

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
		for(double distance_moved = 0; distance_moved + BOT_LENGTH + 5 < map.getHeight()*5; distance_moved += fixed_travel_amount)
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
				for(int i = (cur_x + (int)obj_range)/5; i<map.getWidth(); i++)
				{
					try
					{
						// Sleep for 1/10 of a second
						Thread.sleep(100);
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
		
		int occupied_num;
		
		// This loop will move forward (or in the width attribute
		// for our map) + a certain amount of error
		for(double distance_moved = 0; distance_moved + BOT_WIDTH + 20 < map.getWidth()*5; distance_moved += fixed_travel_amount)
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
				for(int i = (cur_y - (int)obj_range)/5; i>0; i--)
				{
					try
					{
						// Sleep for 1/10 of a second
						Thread.sleep(100);
					}
					catch(InterruptedException e)
					{
						return;
					}
					LCD.clear(5);
					occupied_num = map.getOccupied(cur_x, i);
					if(occupied_num > 0)
					{
						// Increase the assurance that a spot is occupied
						occupied_num += 1;
						map.setOccupied(cur_x, i, occupied_num);
						LCD.drawString("Pos: " + i + "," + cur_y + " occupied", 0, 5);
					}
					else
					{
						map.setOccupied(cur_x, i, 1);
					}
				}
			}
			pilot.travel(convert_cm_to_mm(-fixed_travel_amount));
			moved_x(fixed_travel_amount);
			LCD.clear(4);
			LCD.clear(3);
			LCD.clear(2);
		}
		
		output_map(map);
		
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
	
	// Write out a csv file with obstacles mapped (as best we could)
	public static void output_map(OccupancyGridMap map)
	{
		PrintWriter writer = null;
		int x;
		int y;
		
		try
		{
			writer = new PrintWriter(MAP_FILE, "UTF-8");
		} 
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int occupied;
		if(writer != null)
		{
			for(x = 0; x<map.getWidth(); x++)
			{
				for(y = 0; y<map.getHeight(); y++)
				{
					occupied = map.getOccupied(x,y);
					writer.print(occupied + ",");
				}
				writer.write("\n");
			}
		}
		writer.close();
	}
}
