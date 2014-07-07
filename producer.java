import java.io.Console;
import java.util.*;
import java.io.*;
import java.lang.*;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

class producer {

	private final static String QUEUE_NAME = "queue";
	private static final String EXCHANGE_NAME = "exchange";
	private final static boolean MSG_DURABLE = true;  // so message doesn't get lost if consumer dies
	
	public static void main(String[] args) throws java.io.IOException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

		Console console = System.console();
		String request = console.readLine("What would you like to send? (message, date, numbers): ");
		getData data = new getData();
		
		int state = 2;
		
		while(true) {
			switch (state) {
				case 1: {
							request = console.readLine("What would you like to send? (message, date, numbers): ");
							state = 2;
							break;
						}
				case 2: {
							if (request.equals("message")) {
								state = 3;
							} else if (request.equals("date")){
								String date = data.getDate();
								channel.basicPublish(EXCHANGE_NAME, "", null, date.getBytes());
								System.out.println("[x] Sent '" + date + "'");							
								state = 1;
							} else {
								System.out.println("Invalid, try again.");
								state = 1;						
							}
							break;
						}
				case 3: {
							System.out.println("Let's send messages! Send 'stop' to stop");
							String message = console.readLine("Message: ");
							while(!message.equals("stop")) {
								channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
								System.out.println("[x] Sent '" + message + "'");
								message = console.readLine("Message: ");
							}
							state = 1;
							break;
						}
	
		
			}		
		}
		
	/*	
		if (request.equals("message")) {
			System.out.println("Let's send messages! Send 'stop' to stop");
			String message = console.readLine("Message: ");
			while(!message.equals("stop")) {
				channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
				System.out.println("[x] Sent '" + message + "'");
				message = console.readLine("Message: ");
			}
		} else if (request.equals("date")) {
			String date = data.getDate();
			channel.basicPublish(EXCHANGE_NAME, "", null, date.getBytes());
			System.out.println("[x] Sent '" + date + "'");
		} else if (request.equals("numbers")) {
			String numin = console.readLine("End number?: ");
		
		} else {
			System.out.println("invalid.");
		}
	*/	
		
		
//		channel.close();
//		connection.close();
	}
	
/*	private static void printString (String[] strings) {
		int length = strings.length;
		System.out.println("length is: " + length);
		for (int i = 0; i < length; i++) {
			System.out.print(strings[i] + ", ");
		}
		System.out.println();
	}*/
}
