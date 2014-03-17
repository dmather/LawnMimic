/*
 * Author: Daniel Mather
 * Purpose: Some testing code for our COMP-3012 term project...
 * testing out more advanced concepts.
 */

// Just some fooling around
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

public class test
{
	/*
	 * Initialize all constant variables here. All of these should be in CM or
	 * CM/S.
	 */
	// track width is distance between wheels
	static final double TRACK_WIDTH = 142f;
	// Track length is wheel diameter 
	static final double TRACK_LENGTH = 30;
	static final int ARENA_LENGTH = 1150;
	static final int ARENA_WIDTH = 1150;
	static final int ARENA_PRECISION = 5;
	static final int MOVE_SPEED = 50;
	static final int MOTOR_ACCELERATION = 400;
	static final int ROTATE_SPEED = 50;
	static final int BOT_WIDTH = 200;
	static final int BOT_LENGTH = 300;
	static final int OCCUPIED_THRESHOLD = 1;
	static final int FREE_THRESHOLD = 0;

	public static void main(String[] args)
	{
		// We define the initial grid of the our arena (150x150cm)
		// We define that the precision is 5cm
		OccupancyGridMap map = new OccupancyGridMap(ARENA_LENGTH, ARENA_WIDTH,
				OCCUPIED_THRESHOLD, FREE_THRESHOLD, ARENA_PRECISION);
		
		// 0 is not occupied and 1 is occupied
		// Mark the space that the bot occupies at the start as not occupied
		for(int x = 0; x <= BOT_WIDTH; x +=5)
		{
			for(int y = 0; y <= BOT_LENGTH; y +=5)
			{
				map.setOccupied(x, y, 0);
			}
		}		
		
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
		
		// Set up a feature detector with the range adapter and a min/max
		// distance
		// This class is tripped when...
		RangeFeatureDetector features = new RangeFeatureDetector(rangeAdapter,
				100.0f, 400);
		
		FeatureListener listener = new DetectedObjectListener(pilot, map, 
				BOT_WIDTH, BOT_LENGTH);
		// Add a lister to the feature detector
		features.addListener(listener);

		double length = rangeAdapter.getRange();
		double current_pos = length;
		LCD.drawString("Dist to edge: " + length, 0, 1);

		// Set speed to 10cm/s and then move 50cm
		pilot.setAcceleration(MOTOR_ACCELERATION);
		pilot.setRotateSpeed(ROTATE_SPEED);
		pilot.setTravelSpeed(MOVE_SPEED);
		
		// On our bot backward is forward.
		pilot.backward();

		Button.waitForAnyPress();
	}
}
