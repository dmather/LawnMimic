import lejos.robotics.mapping.OccupancyGridMap;

/*
 * Author: Daniel Mather
 * I wanted to abstract some mapping stuff here, this is because there's some
 * tricky business to get the current position on the map, and calculate 
 * where an obstacle will be. Other mapping stuff will be handled by a specific
 * thread that will monitor position more readily. Since this is a class it can
 * be used in multiple places.
 */
public class myMap
{
	private final int OCCUPIED_CONST = 1;
	private OccupancyGridMap map;
	// Units for these are in cm, these are ints for now,
	// they will probably be passed in as options later.
	private final int bot_width = 20;
	private final int bot_height = 30;
	
	// TODO: figure out some way to maintain a current location for ourselves
	private int cur_pos_x;
	private int cur_pos_y;
	
	public myMap(OccupancyGridMap robot_map)
	{
		this.map = robot_map;
	}
	
	// Normally such a class would take in an x and y position, however we
	// have a problem, we cannot get the exact x position because we don't
	// have two sensors, so instead we'll assume the center of the bot,
	// i.e. we take the bot width and divide it by two.
	public void mark_map(int y)
	{
		// Our current position should be the center of the bot added to
		// an offset of our current position (this should be in grid squares).
		int x = (bot_width/2)+cur_pos_x;
		
		// For now this is the simple way of marking occupied...we're actually
		// going to mark a 1 grid space radius around this spot, since our 
		// sensors have a cone, and we aren't sure how accurate this is, I'd
		// like to use a dynamic method that as the bot is closer narrows down
		// the exact spot, this may happen, but doubtful.
		map.setOccupied(x, y, OCCUPIED_CONST);
	}
	
}
