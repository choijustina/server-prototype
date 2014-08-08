import java.io.IOException;

import java.util.Scanner;

import com.rabbitmq.client.ConnectionFactory;

import com.rabbitmq.client.Connection;

import com.rabbitmq.client.Channel;

import com.rabbitmq.client.MessageProperties;



public class SEND extends Abstract {

	// BASIC CONSOLE PRODUCER

	@Override

	public void getData(Channel channel, Connection connection) {

		Scanner scanner = new Scanner(System.in);

		System.out.println("NEW PRODUCER: Press enter after every message you would like to" 				

			+ " send.\n"+ "Specific commands: '" + CLOSE_PRODUCER + "', '" + CLOSE_CONSUMER 			

			+ "' and 'clear'"); 

		

		messageData = scanner.next() + scanner.nextLine();

		

		try {

			while (!(messageData.equals(CLOSE_PRODUCER))) {

				channel.basicPublish(EXCHANGE_NAME, "", 

					MessageProperties.PERSISTENT_TEXT_PLAIN, messageData.getBytes());

				System.out.println("[x] Sent " + messageData + "'");

				messageData = scanner.next() + scanner.nextLine();

			}

		} catch (IOException e) {

			e.printStackTrace();

		}

		

		System.out.println("closing producer");

		scanner.close();

		closeQueue(channel, connection);

	}



  public static void main(String[] argv) throws Exception {

		SEND s = new SEND();

		s.createQueue();

		System.out.println("EOF");

  }

  

}



