package sse;

import java.util.Scanner;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

//NOTE DOESN'T USE EXCHANGES
public class ServerProducer {
	public static final String QUEUE_NAME = "queue";
	public static final boolean MSG_DURABLE = true;  // so message doesn't get lost if consumer dies
	
	public static void main (String[] argv) throws java.io.IOException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, MSG_DURABLE, false, false, null);
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Opening a new producer, sending to queue entitled '" + QUEUE_NAME + "'\n"
				+ "Press enter after every message you would like to send.\n"
				+ "Specific commands: 'close producer', 'close consumer'");
		String str = scanner.nextLine();
		
		while (!(str.equals("close producer"))) {
			channel.basicPublish("", QUEUE_NAME, null, str.getBytes());
			System.out.println("  [x] Sent '" + str + "'");
			str = scanner.nextLine();
		}
		
		System.out.println("closing producer");
		scanner.close();
		channel.close();
		connection.close();
	}

}
