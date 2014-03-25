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
	
	// Constructor for this thread, we need some local copies of the
	// differential pilot, the range adaptor, and the map. (We'll 
	// see how this ends up working).
	public mymapper(RangeFinderAdaptor range_adapter, OccupancyGridMap map,
			DifferentialPilot pilot)
	{
		this.myMap = map;
		this.myRange = range_adapter;
		this.myPilot = pilot;
	}
	
	// Method that is actually called to run a thread
	public void run()
	{
		// Get me the distance since we started moving..
		while(true)
		{
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
			
			double distance = myPilot.getMovementIncrement();
			double dist_to_object = myRange.getRange();
			
			System.out.println("This is a thread!");
		}
	}
}
