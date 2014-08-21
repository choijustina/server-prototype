/**
 * File: ConsoleProducer.java
 * @author: Justina Choi (choi.justina@gmail.com)
 * Date: July 9, 2014
 * Notes: RabbitMQ Producer; subclass of ProducerAbstract.java
 */

package sse;

import java.io.IOException;
import java.util.Scanner;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

public class ConsoleProducer extends AbstractProducer {

	@Override
	protected void getData(Channel channel, Connection connection) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("NEW PRODUCER: Press enter after every message you would like to send.\n"
				+ "Format: <BINDINGKEY> <message or command text>\n"
				+ "Specific commands: '" + CLOSE_PRODUCER + "', '" + CLOSE_CONSUMER + "' and 'clear'\n"
				+ "\tWhen closing consumer/clearing consumer screen, be sure to follow format and specify binding key of queue"); 
		
		try {
			while (!(scanner.hasNext(CLOSE_PRODUCER))) {
				
				// SENDING WITHOUT A BINDING KEY
				messageData = scanner.nextLine();
				channel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_TEXT_PLAIN, messageData.getBytes());
				System.out.println("  [x] Sent " + " : '" + messageData + "'");
				
				//SENDING WITH A BINDING KEY
/*				bindingKey = scanner.next();
				messageData = scanner.next() + scanner.nextLine();
				channel.basicPublish(EXCHANGE_NAME, bindingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageData.getBytes());
				System.out.println("  [x] Sent " + bindingKey + " - '" + messageData + "'");
				*/			
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		System.out.println("closing producer");
		scanner.close();
		closeQueue(channel, connection);
	}
	
	
	public static void main (String[] argv) {
		ConsoleProducer cp = new ConsoleProducer(); 
		cp.createQueue();
		System.out.println("EOF");
	}
}
