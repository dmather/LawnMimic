/*
 * Class to establish communication over bluetooth
 */

//import lejos.hardware.RemoteBTDevice;
//import lejos.hardware.Brick;
//import lejos.hardware.BrickFinder;
//import lejos.hardware.ev3.EV3;
//import lejos.remote.ev3.RemoteEV3;
//import lejos.hardware.lcd.TextLCD;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Communication
{
	//private RemoteEV3 Marth;
	private ServerSocket serv;
	private DataInputStream in;
	private DataOutputStream out;
	private String message;
	// Establish a port to connect to
	private final int tcp_port = 9124;
    private String remote_device;
	
	public Communication(String EV3)
	{
		try
		{
			this.remote_device = EV3;
            //this.Marth = new RemoteEV3(EV3);
		}
		// TODO: Find out what exception this needs to catch
		catch(Exception e)
		{
			//this.Marth = null;
		}
	}
	
	public String get_stuff()
	{
		// Open a new TCP socket on port 9124 (exact port shouldn't matter)
		try
		{
			serv = new ServerSocket(tcp_port);
			Socket s = serv.accept();
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
			message = in.readUTF();
			serv.close();
		}
		catch(IOException ioExcep)
		{
			// If we have an exception set message to null
			message = null;
		}
		
		// Close the socket
		
		return message;
	}
	
	public void send_stuff(String message)
	{
		// Open a new TCP socket on port 9124 (exact port shouldn't matter)
		try
		{
            Socket s = new Socket(remote_device, tcp_port);
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
			out.writeUTF(message);
		}
		catch(IOException ioExcep)
		{
					
		}
	}
}
