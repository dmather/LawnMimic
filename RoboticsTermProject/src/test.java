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
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.RangeFinderAdaptor;
import lejos.robotics.mapping.OccupancyGridMap;
import lejos.robotics.objectdetection.RangeFeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;

public class test
{
	/*
	 * Initialize all constant variables here. All of these should be in CM or
	 * CM/S.
	 */
	static final double TRACK_WIDTH = 16.5f;
	static final double TRACK_LENGTH = 12;
	static final int ARENA_LENGTH = 150;
	static final int ARENA_WIDTH = 150;
	static final int ARENA_PRECISION = 5;
	static final int MOVE_SPEED = 100;
	static final int MOTOR_ACCELERATION = 400;
	static final int ROTATE_SPEED = 400;

	public static void main(String[] args)
	{
		// We define the initial grid of the our arena (150x150cm)
		// We define that the precision is 5cm
		OccupancyGridMap map = new OccupancyGridMap(ARENA_LENGTH, ARENA_WIDTH,
				20, 20, ARENA_PRECISION);
		// Instantiate a new differential pilot
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
				100.0f, 500);
		
		FeatureListener listener = new DetectedObjectListener(pilot);
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
