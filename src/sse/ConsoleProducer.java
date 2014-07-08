package sse;

import java.io.IOException;
import java.util.Scanner;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

public class ConsoleProducer implements ProducerInterface {

	@Override
	public void createQueue() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		
		try {
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
			//channel.queueDeclare(QUEUE_NAME, MSG_DURABLE, false, false, null);
			getData(channel, connection);
		} catch (IOException exception) {
			exception.getStackTrace();
		}
	}

	@Override
	public void getData(Channel channel, Connection connection) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Opening a new producer, sending to queue entitled '" + QUEUE_NAME + "'\n"
				+ "Press enter after every message you would like to send.\n"
				+ "Specific commands: 'close producer', 'close consumer'");
		String str = scanner.nextLine();
		
		try {
			while (!(str.equals("close producer"))) {
				channel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_TEXT_PLAIN, str.getBytes());
				//channel.basicPublish(EXCHANGE_NAME, "", null, str.getBytes());
				//channel.basicPublish("", QUEUE_NAME, null, str.getBytes());
				System.out.println("  [x] Sent '" + str + "'");
				str = scanner.nextLine();
			}
		} catch (IOException exception) {
			exception.getStackTrace();
		}
		
		System.out.println("closing producer");
		scanner.close();
		closeQueue(channel, connection);
	}
	
	@Override
	public void closeQueue(Channel channel, Connection connection) {
		try {
			channel.close();
			connection.close();
		} catch (IOException exception) {
			exception.getStackTrace();
		}
	}
	
	public static void main (String[] argv) {
		ConsoleProducer cp = new ConsoleProducer(); 
		cp.createQueue();
		System.out.println("EOF");
	}

}
