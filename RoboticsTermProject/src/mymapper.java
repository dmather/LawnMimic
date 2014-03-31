import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.robotics.mapping.OccupancyGridMap;
import lejos.robotics.RangeFinderAdaptor;
import lejos.robotics.navigation.DifferentialPilot;

/*
 * This is a thread that is supposed to keep track of
 * location and mapping tasks, while the main function
 * of the robot is to stay going forward.
 */
public class mymapper implements Runnable
{
	private RangeFinderAdaptor myRange;
	private OccupancyGridMap myMap;
	private DifferentialPilot myPilot;
	private volatile boolean finished = false;
	
	// Constructor for this thread, we need some local copies of the
	// differential pilot, the range adaptor, and the map. (We'll 
	// see how this ends up working).
	public mymapper(RangeFinderAdaptor range_adapter, OccupancyGridMap map,
			DifferentialPilot pilot)
	{
		this.myMap = map;
		this.myRange = range_adapter;
		//this.myPilot = pilot;
	}
	
	public void set_finished(boolean finished)
	{
		this.finished = finished;
	}
	
	// Method that is actually called to run a thread
	public void run()
	{
		// Get me the distance since we started moving..
		while(!finished)
		{
			LCD.clear(4);
			//LCD.clear(3);
			// Sleep for 250ms...should be enough for some movement
			// before recalculating position of obstruction.
			try
			{
				Thread.sleep(250);
			}
			catch(InterruptedException e)
			{
				return;
			}
			
			//double distance = myPilot.getMovementIncrement();
			double dist_to_object = myRange.getRange();
			
			//LCD.drawString("This is a thread!", 0, 4);
			LCD.drawString("Range: " + dist_to_object, 0 ,4);
			//LCD.drawString("Distance: " + distance, 0, 3);
		}
	}
}
