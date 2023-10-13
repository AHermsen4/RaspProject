package javaio.controller;

/**
*
* @author Leo Korbee (c), Graafschap College <l.korbee@graafschapcollege.nl>
*/


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

import javaio.driver.DS1820;

public class HTTPserver extends Thread
{

   // instance variables
   private Socket client = null;
   private BufferedReader inClient = null;
   private DataOutputStream outClient = null;

   
   // constructor
   public HTTPserver(Socket cl)
   {
       client = cl;
   }
   
   /**
    * Method used to compose the home page response to the client
    *
    * @throws Exception
    */
   public void homePage() throws Exception
   {
       StringBuffer responseBuffer = new StringBuffer();
       
       // read temperature from sensor
       DS1820 temp = new DS1820();
       
       
       // response with data
       responseBuffer.append("<temperature>" + temp.getTemperature() + "</temperature><br>\n");
       
       responseBuffer.append("<temperature>" + temp.apiGetTemp() + "</temperature><br>\n");
       
       responseBuffer.append("<temperature> The last temperated that has been recorded is: " + temp.apiGetLastTemp() + "</temperature><br>\n");
       
       sendResponse(200, responseBuffer.toString());
   }
   
   
   public void run()
   {
       try
       {
           System.out.println("The Client " + client.getInetAddress() + ":" + client.getPort() + " is connected");

           inClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
           outClient = new DataOutputStream(client.getOutputStream());

           String requestString = inClient.readLine();
           String headerLine = requestString;

           /**
            * sometimes headerLine is null
            */
           if (headerLine == null)
           {
               return;
           }

           StringTokenizer tokenizer = new StringTokenizer(headerLine);
           String httpMethod = tokenizer.nextToken();
           String httpQueryString = tokenizer.nextToken();

           System.out.println("HTTP request processed");
           while (inClient.ready())
           {
               /**
                * Read the HTTP request until the end
                */
               // System.out.println(requestString);
               requestString = inClient.readLine();
           }

           
           // catch all GET methods
           if (httpMethod.equals("GET"))
           {
               if (httpQueryString.equals("/"))
               {
                   
                   // return the home page
                   homePage();
               } 
               else
               {
                   sendResponse(404, "<b>The Requested resource not found.</b>");
               }
           } else
           {
               sendResponse(404, "<b>The Requested resource not found.</b>");
           }
       } catch (Exception e)
       {
           e.printStackTrace();
       }
   }

   /**
    * Method used to compose the response back to the client.
    *
    * @param statusCode
    * @param responseString
    * @throws Exception
    */
   public void sendResponse(int statusCode, String responseString) throws Exception
   {

       String HTML_START = "<html><title>HTTP Server in Java</title><body>";
       String HTML_END = "</body></html>";
       String NEW_LINE = "\r\n";

       String statusLine = null;
       String serverdetails = Headers.SERVER + ": Java Server";
       String contentLengthLine = null;
       String contentTypeLine = Headers.CONTENT_TYPE + ": text/html" + NEW_LINE;

       if (statusCode == 200)
       {
           statusLine = Status.HTTP_200;
       } else
       {
           statusLine = Status.HTTP_404;
       }

       statusLine += NEW_LINE;
       responseString = HTML_START + responseString + HTML_END;
       contentLengthLine = Headers.CONTENT_LENGTH + responseString.length() + NEW_LINE;

       outClient.writeBytes(statusLine);
       outClient.writeBytes(serverdetails);
       outClient.writeBytes(contentTypeLine);
       outClient.writeBytes(contentLengthLine);
       outClient.writeBytes(Headers.CONNECTION + ": close" + NEW_LINE);

       /**
        * adding the new line between header and body
        */
       outClient.writeBytes(NEW_LINE);

       outClient.writeBytes(responseString);

       outClient.close();
   }

 

   /**
    * class used to store headers constants
    *
    */
   private static class Headers
   {

       public static final String SERVER = "Server";
       public static final String CONNECTION = "Connection";
       public static final String CONTENT_LENGTH = "Content-Length";
       public static final String CONTENT_TYPE = "Content-Type";
   }

   /**
    * class used to store status line constants
    *
    */
   private static class Status
   {
       public static final String HTTP_200 = "HTTP/1.1 200 OK";
       public static final String HTTP_404 = "HTTP/1.1 404 Not Found";
   }

}


