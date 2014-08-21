/**
 * File: TextfileProducer.java
 * @author: Justina Choi (choi.justina@gmail.com)
 * Date: July 9, 2014
 * Notes: RabbitMQ Producer; subclass of ProducerAbstract.java
 */

package sse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

public class TextfileProducer extends AbstractProducer {

	@Override
	protected void getData(Channel channel, Connection connection) {
		String filename = "Copylongfile_10.txt";
		
		URL url = null;
		try {
			url = new URL("http://localhost:8080/SSE/" + filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		Scanner s = null;
		try {
			s = new Scanner(url.openStream());
			
			while (s.hasNextLine()) {
//				bindingKey = "document";
				messageData = s.nextLine();
				
//				channel.basicPublish(EXCHANGE_NAME, bindingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageData.getBytes());
				channel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_TEXT_PLAIN, messageData.getBytes());			// 2nd parameter is binding key; none for fanout exchange
				
				System.out.println("  [x] Sent '" + messageData + "'");
				
				Thread.currentThread().sleep(300);
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}
	
	public static void main (String[] argv) {
		TextfileProducer tp = new TextfileProducer();
		tp.createQueue();
		System.out.println("EOF");
	}
}
