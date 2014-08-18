/**
 * File: AbstractProducer.java
 * @author Justina Choi (choi.justina@gmail.com)
 * Date: July 9, 2014
 * Sources: http://www.rabbitmq.com/tutorials/tutorial-two-java.html
 * Subclasses: ConsoleProducer.java, TextfileProducer.java
 */

package sse;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public abstract class AbstractProducer {
	public static final String EXCHANGE_NAME = "topicexchange";
	public static final String EXCHANGE_TYPE = "topic";
	public static final String QUEUE_NAME = "ServerSent_Events";
	public static final String CLOSE_PRODUCER = "closeproducer";
	public static final String CLOSE_CONSUMER = "closeconsumer";
	//public static final boolean MSG_DURABLE = true;  // so message doesn't get lost if consumer dies
	
	public static String bindingKey = "";
	public static String messageData = "";

	abstract void getData(Channel channel, Connection connection);
	
	protected void createQueue() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		
		try {
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			
			channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);		// a durable, non-autodelete exchange, type is second parameter
			
			getData(channel, connection);
			closeQueue(channel, connection);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	protected void closeQueue(Channel channel, Connection connection) {
		try {
			channel.close();
			connection.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

}