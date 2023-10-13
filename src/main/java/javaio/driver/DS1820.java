package javaio.driver;

/* using libraries:
pi4j-core.jar
pi4j-device.jar 
version 1.1 (march 2018)

Hardware connection: 
1-Wire interface on connector pin 7: 
Broadcom port GPIO4
WiringPi port 07
4k7 pull up resistor

*/

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Leo Korbee (c), Graafschap College <l.korbee@graafschapcollege.nl>
 */
public class DS1820
{
	// Create a GPIO controller
    final GpioController gpio = GpioFactory.getInstance();

    // Provision the pin
    final GpioPinDigitalOutput fanPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "Fan", PinState.LOW);
	
	public void apiSetTemp(double temp)
	{
		try {
		URL url = new URL("http://127.0.0.1:8000/api/temperatures?value=" + temp);
		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection)con;
		http.setRequestMethod("POST");
		http.setDoOutput(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public double apiGetLastTemp()
	{
		try {
		URL apiUrl = new URL("http://127.0.0.1:8000/api/lastTemp");

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("GET");

        // Get the response
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = in.readLine(); // Assuming the response is a single line with a double value
        in.close();

        // Parse the response as a double
        JSONObject jsonObject = new JSONObject(response);
        double value = jsonObject.getDouble("value");
        return value;
    } catch (Exception e) {
        e.printStackTrace();
        return Double.NaN; // Return NaN in case of an error
    }
	}
	
	public double apiGetTemp()
	{
		double lastValue = 0;
		try {
            // URL of the endpoint
            URL url = new URL("http://LAPTOP-BOTB5QRH:8000/api/temperatures"); // Replace with your actual URL

            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Get the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Parse JSON
            JSONArray jsonArray = new JSONArray(content.toString());
            JSONObject lastObject = jsonArray.getJSONObject(jsonArray.length() - 1);
            lastValue = lastObject.getDouble("value");

            // Use the last value
            System.out.println("Last value: " + lastValue);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		return lastValue;
	}
	
	// method to get temperature
		public double getTemperature()
		{
			// default -1 as error status;
			double value = -1.0;

			W1Master master = new W1Master();
			List<W1Device> w1Devices = master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE);

			for (W1Device device : w1Devices)
			{
				// this line is enough if you want to read the temperature
				// returns the temperature as double rounded to one decimal place after the
				// point
				value = ((TemperatureSensor) device).getTemperature();
			}
			
			apiSetTemp(value);


			return value;
		}
		
		public void turnOnFan()
		{
			fanPin.high();
		}
		
		public void turnOffFan()
		{
			fanPin.low();
		}

	// method to test DS1820 temperature sensor
	public void test()
	{

		W1Master master = new W1Master();
		List<W1Device> w1Devices = master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE);

		for (W1Device device : w1Devices)
		{
			// this line is enought if you want to read the temperature
			// returns the temperature as double rounded to one decimal place after the
			// point
			System.out.println("Temperature: " + ((TemperatureSensor) device).getTemperature());

			try
			{
				// returns the ID of the Sensor and the full text of the virtual file
				System.out.println("1-Wire ID: " + device.getId() + " value: " + device.getValue());
			} catch (IOException e)
			{
				e.printStackTrace();
			}

		}
	}

	

}
