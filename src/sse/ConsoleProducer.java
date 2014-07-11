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

public class ConsoleProducer extends ProducerAbstract {

	@Override
	public void getData(Channel channel, Connection connection) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("NEW PRODUCER: Press enter after every message you would like to send.\n"
				+ "Specific commands: '" + CLOSE_PRODUCER + "', '" + CLOSE_CONSUMER + "' and 'clear'"); 
		//String str = scanner.nextLine();
		
		
		// TODO check to make sure there are two tokens per line
		ROUTING_KEY = scanner.next();
		MSG_DATA = scanner.next() + scanner.nextLine();
		
		try {
			while (!(MSG_DATA.equals(CLOSE_PRODUCER))) {
				channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, MessageProperties.PERSISTENT_TEXT_PLAIN, MSG_DATA.getBytes());
				//channel.basicPublish(EXCHANGE_NAME, String routingKey, msg properties, str.getBytes());
				System.out.println("  [x] Sent " + ROUTING_KEY + " : '" + MSG_DATA + "'");
				ROUTING_KEY = scanner.next();
				MSG_DATA = scanner.next() + scanner.nextLine();
			}
		} catch (IOException exception) {
			exception.printStackTrace(); 
		}
		
		/*try {
			while (!(str.equals(CLOSE_PRODUCER))) {
				channel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_TEXT_PLAIN, str.getBytes());
				//channel.basicPublish(EXCHANGE_NAME, String routingKey, msg properties, str.getBytes());
				System.out.println("  [x] Sent '" + str + "'");
				str = scanner.nextLine();
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}*/
		
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
