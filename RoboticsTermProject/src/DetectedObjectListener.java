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
            pilot.backward();
        }
    }
}