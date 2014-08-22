/**
 * File: RoutingConsumer.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: August 18, 2014
 * Notes: RabbitMQ Consumer; modified version of SSE_Rabbit.java
 * 			Consumer.java
 */

package sse;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

public class RoutingConsumer {
	private static final boolean MSG_ACK = false;			// TODO: message acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
	private static final String bindingKey = "";
	
	private static Connection connection = null;					//only one RabbitMQ connection
	private static Channel channel = null;
	private static QueueingConsumer queueingConsumer = null;
	
	protected static Map<Integer, ConsumerObject> consumerMap = new HashMap<Integer, ConsumerObject>();
	protected static int numberOfConsumers = 0;
	
	private static final boolean DEBUG = true;
	
	public RoutingConsumer() throws IOException {
		
//		PrintStream out = null;
		if (DEBUG) {
			BasicConsumer.recvMsg("DEBUG MODE IS ON");
			BasicConsumer.recvMsg("number of consumers: " + numberOfConsumers);
			
			for (int i = 1; i <= numberOfConsumers; i++) {
				ConsumerObject thisConsumer = consumerMap.get(i);
				String str = thisConsumer.printConsumer();
				BasicConsumer.recvMsg(str);
			}
		}
		
		if (connection==null) {
			initRabbitMQ();  // may throw IOException
			if (DEBUG) BasicConsumer.recvMsg("initialized RabbitMQ connection\n\n");
		} else
			if (DEBUG) BasicConsumer.recvMsg("RabbitMQ already initialized - double check\n\n");
		
		while (true) {  // waiting for items in RabbitMQ queue
			try {
				
				QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
				
				// TODO because fanout exchange shouldn't have routingkey
//				String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				
				if (message.equals(AbstractProducer.CLOSE_CONSUMER)) {
//					BasicConsumer.recvMsg("CLOSING THE CONSUMER\n\n");
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				}
				
				Date date = new Date();
//				if (DEBUG) BasicConsumer.recvMsg("Recvd: " + date + " - " + message + "\n\n");
				
				// loop through all of the consumers
				for (int i = 1; i <= RoutingConsumer.numberOfConsumers; i++) {
					ConsumerObject thisConsumer = RoutingConsumer.consumerMap.get(i);
					thisConsumer.recvMsg(10, message);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (DEBUG) BasicConsumer.recvMsg("CLOSING CONSUMER if doesn't auto-reconnect within 5 seconds\n\n");
		try {
			Thread.currentThread().sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// closes the connection if doesn't try to auto-reconnect 
		closeRabbitMQ();	// may throw IOException
		//out.close();
	}
	
	/*
	 * initRabbitMQ - initializes only one RabbitMQ connection
	 */
	private void initRabbitMQ() throws IOException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		connection = factory.newConnection();
		channel = connection.createChannel();
		
		channel.exchangeDeclare(AbstractProducer.EXCHANGE_NAME, AbstractProducer.EXCHANGE_TYPE);
		
		// server-generated queue names
//		String queueName = channel.queueDeclare().getQueue();
		// already defined queue name (in AbstractProducer.java)
		String queueName = channel.queueDeclare(AbstractProducer.QUEUE_NAME, true, false, false, null).getQueue();
		
		channel.queueBind(queueName, AbstractProducer.EXCHANGE_NAME, bindingKey);
		
		queueingConsumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, MSG_ACK, queueingConsumer);
	}
	
	private void closeRabbitMQ() throws IOException {
		connection.close();
		channel.close();
	}
	
	/*
	public static void main(String[] args) {
		try {
			RoutingConsumer rc = new RoutingConsumer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
}
