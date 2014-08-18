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

public class JSONProducer extends AbstractProducer {
	//String filename = "json.txt";
	String filename = "json3.txt";
	
	@Override
	protected void getData(Channel channel, Connection connection) {
		
		try {
			//System.out.println("Press enter after every message you would like to send.\n" + "Specific commands: '" + CLOSE_PRODUCER + "', '" + CLOSE_CONSUMER + "' and 'clear'");
			//Scanner scanner = new Scanner(System.in);
			//messageData = scanner.nextLine();
			URL url = new URL("http://localhost:8080/SSE/" + filename);
			Scanner scanner = new Scanner(url.openStream());
			
			while (scanner.hasNextLine()) {
				bindingKey = "json";
				messageData = scanner.nextLine();
				
				channel.basicPublish(EXCHANGE_NAME, bindingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageData.getBytes());
				System.out.println("  [x] Sent:  " + bindingKey + " - " + messageData);
				
				Thread.currentThread().sleep(250);
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
