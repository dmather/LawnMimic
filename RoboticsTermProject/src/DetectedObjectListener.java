import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.hardware.lcd.LCD;
 
public class DetectedObjectListener implements FeatureListener
{
    private DifferentialPilot pilot;
 
    public DetectedObjectListener(final DifferentialPilot pilot)
    {
        this.pilot = pilot;
    }
 
    @Override
    public void featureDetected(final Feature feature, final FeatureDetector detector)
    {
        int range = (int)feature.getRangeReading().getRange();
        if(range <= 45)
        {
        	LCD.drawString("Distance: " + range, 0, 0);
            if(range <= 20)
            {
                System.exit(0);
            }
            pilot.stop();
            pilot.rotate(300);
            pilot.backward();
        }
    }
}