/*
 * Author: Daniel Mather
 * Class to establish TCP communication using sockets, we sadly had to us
 * this over doing communication with bluetooth because I couldn't find
 * appropriate functions to be able to send string data to another EV3.
 * I also found that there was no way to pair the two EV3s...so that
 * entirely threw bluetooth communications out the window. Had we had
 * working WiFi on the two EV3s this communication method would work
 */

import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Communication
{
	private ServerSocket serv;
	private DataInputStream in;
	private DataOutputStream out;
	private String message;
	// Establish a port to connect to, I have no idea if this a service
	// reserved port or not, but given it's relatively low number it
	// could be. 
	private final int tcp_port = 9124;
    private String remote_device;
	
	public Communication(String EV3)
	{
		try
		{
			this.remote_device = EV3;
		}
		// TODO: Find out what exception this needs to catch
		catch(Exception e)
		{
			//this.Marth = null;
		}
	}
	
	// This method returns a string with the message sent by the
	// remote machine.
	public String get_stuff()
	{
		// Open a new TCP socket on port 9124 (exact port shouldn't matter)
		// As long as the other device is sending on the same port, and the
		// port selected isn't already used.
		try
		{
			serv = new ServerSocket(tcp_port);
			Socket s = serv.accept();
			// Sockets allow for bidirectional communication so I am opening
			// both an input stream and an output one, this is probably
			// not required.
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
	
	// This method sends a string to the remote machine.
	public void send_stuff(String message)
	{
		// Open a new TCP socket on port 9124 (exact port shouldn't matter)
		try
		{
            Socket s = new Socket(remote_device, tcp_port);
			// Again opening both an input and output stream even though
            // I'm only using the output stream.
            in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
			out.writeUTF(message);
			s.close();
		}
		catch(IOException ioExcep)
		{
			
		}
	}
}
