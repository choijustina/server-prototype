/**
 * File: RoutingConsumer.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: August 18, 2014
 * Purpose: Gets the messages from the RabbitMQ queue "ServerSent_Events" and sends to BasicConsumer
 * Notes: RabbitMQ Consumer; modified version of SSE_Rabbit.java
 */

package sse;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RoutingConsumer {
	
	private static final boolean MSG_ACK = false;			// TODO: message acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
	
	private static Connection connection = null;					//only one RabbitMQ connection
	private static Channel channel = null;
	private static QueueingConsumer queueingConsumer = null;
	
	protected static Map<Integer, ConsumerObject> consumerMap = new HashMap<Integer, ConsumerObject>();
	protected static int numberOfConsumers = 0;
	
	private static final boolean DEBUG = true;
	
	public RoutingConsumer() throws IOException {
		BasicConsumer bc = null;

		if (DEBUG) {
			for (int i = 1; i <= numberOfConsumers; i++) {
				ConsumerObject consumerobj = consumerMap.get(i);
				String str = consumerobj.printConsumer();
				bc = consumerobj.getBasicConsumer();
				bc.receiveMessage("RC-" + str);
			}
			bc.receiveMessage("RC-number of consumers: " + numberOfConsumers);
		}
		
		if (connection==null) {
			initRabbitMQ();  // may throw IOException
			if (DEBUG) bc.receiveMessage("RC-initialized RabbitMQ connection\n\n");
		} else
			if (DEBUG) bc.receiveMessage("RC-RabbitMQ already initialized - double check\n\n");
		
		while (true) {  // waiting for items in RabbitMQ queue
			try {
				
				QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
				String message = new String(delivery.getBody());
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				
				if (message.equals(AbstractProducer.CLOSE_CONSUMER)) {
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				}
				
				// assumes message is a JSON object
				JSONParser parser = new JSONParser();
				JSONObject jsonObject = (JSONObject) parser.parse(message);
				
				String eventName = (String) jsonObject.get("name");
//				String eventID = (String) jsonObject.get("id");
				String eventMsgType = (String) jsonObject.get("msgtype");
				String eventDocType = (String) jsonObject.get("doctype");
//				String eventData = (String) jsonObject.get("data");
								
				// loop through all of the consumers in consumerMap
				for (int i = 1; i <= RoutingConsumer.numberOfConsumers; i++) {
					ConsumerObject consumerobj = RoutingConsumer.consumerMap.get(i);
					String consumerName = consumerobj.getName();
					String consumerMsgType = consumerobj.getMsgType();
					String consumerDocType = consumerobj.getDocType();
					BasicConsumer basicconsumer = consumerobj.getBasicConsumer();
					
					// FILTER LOGIC
					int caseNum = 0; 
					if (consumerMsgType.isEmpty() && consumerDocType.isEmpty())
						caseNum = 2;						// searching name
					else if (consumerMsgType.isEmpty())
						caseNum = 3;						// searching name & document type
					else if (consumerDocType.isEmpty())
						caseNum = 4;						// searching name & message type
					else
						caseNum = 1;						// searching name, message type and document type (ALL OPTIONS)
					
					switch(caseNum) {
					case 0:
						basicconsumer.receiveMessage("ERROR - UNEXPECTED CASE NUMBER");
					case 1:				// searching name, message type and document type
						if ( (consumerName.equals(eventName)) && (consumerMsgType.equals(eventMsgType)) && (consumerDocType.equals(eventDocType)) ) {
							basicconsumer.sendJSON(jsonObject);
						}
						break;
					case 2:				// searching name
						if (consumerName.equals(eventName)) {
							basicconsumer.sendJSON(jsonObject);
						}
						break;
					case 3:				// searching name & document type
						if ( (consumerName.equals(eventName)) && (consumerDocType.equals(eventDocType)) ) {
							basicconsumer.sendJSON(jsonObject);
						}
						break;
					case 4:				// searching name & message type
						if ( (consumerName.equals(eventName)) && (consumerMsgType.equals(eventMsgType)) ) {
							basicconsumer.sendJSON(jsonObject);
						}
						break;
					}
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
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
		String queueName = channel.queueDeclare(AbstractProducer.QUEUE_NAME, true, false, false, null).getQueue();
		
		channel.queueBind(queueName, AbstractProducer.EXCHANGE_NAME, "");
		
		queueingConsumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, MSG_ACK, queueingConsumer);
	}
	
	private void closeRabbitMQ() throws IOException {
		connection.close();
		channel.close();
	}
	
}
