/**
 * File: ProducerAbstract.java
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

public abstract class ProducerAbstract {
	public final String EXCHANGE_NAME = "topicexchange";
	//public final String QUEUE_NAME = "queue";		// uses server-generated queue names
	public final String CLOSE_PRODUCER = "close producer";
	public final String CLOSE_CONSUMER = "close consumer";
	//public final boolean MSG_DURABLE = true;  // so message doesn't get lost if consumer dies
	public String ROUTING_KEY = "";
	public String MSG_DATA = "";

	abstract void getData(Channel channel, Connection connection);
	
	public void createQueue() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		
		try {
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.exchangeDeclare(EXCHANGE_NAME, "topic");		// a durable, non-autodelete exchange of "topic" type
			//channel.queueDeclare(QUEUE_NAME, MSG_DURABLE, false, false, null);
			
			getData(channel, connection);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public void closeQueue(Channel channel, Connection connection) {
		try {
			channel.close();
			connection.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

}