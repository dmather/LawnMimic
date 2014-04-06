/*
 * Author: Daniel Mather
 * This is a small program that is designed to be run as a listener to the
 * mapping robot, it should in theory store the map locally on my laptop
 * so that it can be displayed. 
 */

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class GetMap
{
	public static void main(String[] args)
	{
		PrintWriter writer = null;
		Communication EV3 = new Communication("192.168.74.1");
		String message;

		// This is probably a bad way to handle this
		try
		{
			writer = new PrintWriter("C:\\Users\\Daniel Mather\\My Documents\\map.csv", "UTF-8");
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
		System.out.println("Waiting for message...");
		if(writer != null)
		{
			message = EV3.get_stuff();
			System.out.println(message);
			writer.print(message);
		}
		// Don't leave file handles open...
		writer.close();

	}
}
