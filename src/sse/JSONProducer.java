/**
 * File: JSONProducer.java
 */

package sse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONProducer extends AbstractProducer {
	String filename = "json.txt";

	@Override
	protected void getData(Channel channel, Connection connection) {
		
		try {
			//System.out.println("Press enter after every message you would like to send.\n" + "Specific commands: '" + CLOSE_PRODUCER + "', '" + CLOSE_CONSUMER + "' and 'clear'");
			//Scanner scanner = new Scanner(System.in);
			//messageData = scanner.nextLine();
			URL url = new URL("http://localhost:8080/SSE/" + filename);
			Scanner scanner = new Scanner(url.openStream());
			messageData = scanner.nextLine();
			
			//while (!(messageData.equals(CLOSE_PRODUCER))) {
			while (scanner.hasNextLine()) {
				bindingKey = "json";
				
				channel.basicPublish(EXCHANGE_NAME, bindingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageData.getBytes());
				
				Thread.currentThread().sleep(250);
				
				System.out.println("  [x] Sent:  " + messageData);
				messageData = scanner.nextLine();
			}
			scanner.close();
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {		// channel.basicPublish() method
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("closing producer");
		closeQueue(channel, connection);
		
	}
	
	public static void main (String[] argv) {
		JSONProducer jp = new JSONProducer();
		jp.createQueue();
		System.out.print("EOF");
	}

}
