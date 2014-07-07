package sse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class TextfileProducer implements ProducerInterface {

	@Override
	public void createQueue() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		
		try {
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.queueDeclare(QUEUE_NAME, MSG_DURABLE, false, false, null);
			getData(channel, connection);
		} catch (IOException exception) {
			exception.getStackTrace();
		}
	}

	@Override
	public void getData(Channel channel, Connection connection) {
		String str = null;
		String filename = "longfile_10.txt";
		
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
				str = s.nextLine();
				channel.basicPublish("", QUEUE_NAME, null, str.getBytes());
				System.out.println("  [x] Sent '" + str + "'");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		TextfileProducer tp = new TextfileProducer();
		tp.createQueue();
		System.out.println("EOF");
	}

}
