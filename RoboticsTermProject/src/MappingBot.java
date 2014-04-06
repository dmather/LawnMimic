/*
 * Author: Daniel Mather
 * Purpose: A very basic robot that does some mapping (albeit poor,
 * but mapping nonetheless).
 */

// Giant block of imports
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
//import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
//import lejos.hardware.sensor.EV3ColorSensor;
//import lejos.robotics.Color;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.RangeFinderAdaptor;
import lejos.robotics.mapping.OccupancyGridMap;

import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class MappingBot
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
	// Offset is negative because the sensor is readings are usually too far
	static final double RANGE_SENSOR_OFFSET = -4.25;
	// Readings around 90 are just too crappy, we also have to account for the
	// offset in this as well.
	static final double SENSOR_MAX_RANGE = 50 + RANGE_SENSOR_OFFSET;
	// Bot origin is the bots center point in respect to origin (0,0)
	// this is in x, y
	static final double[] BOT_ORIGIN = {BOT_WIDTH/2, BOT_LENGTH/2};
	static double[] POS = BOT_ORIGIN;
	
	static String MAP_FILE = "/map.csv";
	// Hash map for rotation angles
	static HashMap<Integer, Double> rotation; 
	// I wasn't able to do mapping this way...oh well maybe  next time.
	//static Thread mapper;
	// 10.0.1.5 is the fixed IP of my laptop on bluetooth
	static Communication comm = new Communication("10.0.1.5");

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
		// Yes I know this loop and the next loop are basically the same thing
		// and are basically duplicate code, I just needed to get this working
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
			// Just print out the distance that the robot thinks that it has
			// moved.
			LCD.drawString("Dist Moved: " + distance_moved, 0, 2);
			
			cur_x =  (int)POS[0]/5;
			cur_y = (int)POS[1]/5;
			// Offset the range by our offset factor, this was done
			// with a bunch of tests for distance.
			obj_range = convert_dm_to_cm(rangeAdapter.getRange()) + RANGE_SENSOR_OFFSET;
			
			// If the distance is greater than the max range (out of the
			// max range most of the readings are lost in the noise)
			// Then proceed, otherwise move forward one 5cm increment.
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
						// If a spot isn't occupied set it to be the first
						// level of occupied.
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
		
		// Closing the sensor resource..lets avoid resource leakages if we can
		range.close();
	}
	
	// After the robot has moved this updates the correct
	// axis that it moved in.
	public static void moved_x(double amount)
	{
		POS[0] = BOT_ORIGIN[0] + amount;
	}
	
	// After the robot has moved this updates the correct
	// axis that it moved in.
	public static void moved_y(double amount)
	{
		POS[1] = BOT_ORIGIN[1] + amount;
	}
	
	// Get our bot's current position (this method isn't really
	// necessary since it's a global object).
	public static double[] get_cur_pos()
	{
		return POS;
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
	
	// Write out a .csv file with obstacles mapped (as best as we could)
	// Our map is almost like a heat map, where the hotter the temperature
	// the bigger the number, in this case it's the more likely the spot
	// is occupied the larger the number is.
	public static void output_map(OccupancyGridMap map)
	{
		PrintWriter writer = null;
		int x;
		int y;
		// We're just appending to this string so it can be initialized blank.
		// This is specifically for the csv string that contains the map
		// information.
		String results = "";
		
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
					// This duplication is actually to ensure that the map
					// is saved both locally on EV3 and remotely on my laptop
					// if it is listening to the TCP socket.
					results += occupied + ",";
					writer.print(occupied + ",");
				}
				// Windows newline don't just use the newline character
				// and must be declared as carriage-return newline instead
				// of just newline.
				results += "\r\n";
				writer.write("\n");
			}
		}
		// Sending results or mapping data over the Bluetooth PAN connection
		// that is established on my laptop.
		comm.send_stuff(results);
		
		writer.close();
	}
}