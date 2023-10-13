package javaio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javaio.controller.HTTPserver;

public class Simple_DS1820_API
{

	// startup
	public static void main(String args[]) throws IOException
	{
		// ServerSocket server = new ServerSocket(5000, 10,)
		// InetAddress.getByName("127.0.0.1"));
		ServerSocket server = null;
		try
		{
			server = new ServerSocket(5000, 10);

			System.out.println("HTTPServer started on port 5000");
			/**
			 * loop to keep the application alive - a new HTTPServer object is created for
			 * each client
			 */
			while (true)
			{
				Socket connected = server.accept();
				HTTPserver httpServer = new HTTPserver(connected);
				httpServer.start();
			}
		} 
		catch (IOException e)
		{
			System.out.println("Error setup local server");
			e.printStackTrace();
		} 
		finally
		{
			server.close();
		}

	}

}
