/*
 * Author: Daniel Mather
 * Description:
 * This a test class that tries to map
 */

import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.hardware.lcd.LCD;
import lejos.robotics.mapping.OccupancyGridMap;
 
public class DetectedObjectListener implements FeatureListener
{
    private DifferentialPilot pilot;
    private OccupancyGridMap map;
    private double bot_width;
    private double bot_length;
    
 
    public DetectedObjectListener(final DifferentialPilot pilot, 
    		final OccupancyGridMap map, double bot_width, double bot_length)
    {
    	// Give me local copies of things I need to use in this class
        this.pilot = pilot;
        this.map = map;
        this.bot_width = bot_width;
        this.bot_length = bot_length;
    }
 
    @Override
    public void featureDetected(final Feature feature, 
    		final FeatureDetector detector)
    {
        int range = (int)feature.getRangeReading().getRange();
        if(range <= 20)
        {
        	LCD.drawString("Distance: " + range, 0, 0);
            if(range <= 15)
            {
                System.exit(0);
            }
            double distance = Math.abs(pilot.getMovementIncrement() + bot_length*2);
            LCD.drawString("Travel Dist: " + distance, 0, 3);
            pilot.stop();
            
            // Iterate through the map...
            for(int x = 0; x <= bot_width; x += 5)
            {
            	for(int y = 0; y <= map.getHeight(); y += 5)
            	{
            		if(y <= distance/5)
            		{
            			LCD.clear(4);
            			map.setOccupied(x, y, 0);
            			LCD.drawString("Not Occ: " + x + "," + y, 0, 4);
            		}
            		else
            		{
            			LCD.clear(5);
            			map.setOccupied(x, (int)(distance/5)+1, 1);
            			LCD.drawString("Occ" + x + "," + (int)(distance/5)+1,
            					0, 5);
            		}
            	}
            }
            // Avoid obstacle...sorta.. (note this doesn't work for walls
            // (obviously)
            pilot.rotate(90);
            pilot.travel(bot_length*1.5*-1);
            pilot.rotate(-90);
            pilot.travel(bot_length*1.5*-1);
            pilot.rotate(-90);
            pilot.travel(bot_length*1.5*-1);
            pilot.rotate(90);
            pilot.backward();
        }
    }
}