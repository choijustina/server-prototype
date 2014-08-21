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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

@WebServlet("/RoutingConsumer")
public class RoutingConsumer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final boolean MSG_ACK = false;			// TODO: message acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
	private static final String bindingKey = "";
	private static final String REDIRECT_FILE = "index.html";
	
	private Connection connection = null;					//only one RabbitMQ connection
	private Channel channel = null;
	private QueueingConsumer queueingConsumer = null;
	
	protected static Map<Integer, ConsumerObject> consumerMap = new HashMap<Integer, ConsumerObject>();
	protected static int numberOfConsumers = 0;
	
	private static final boolean DEBUG = true;
	private static int counter = 0; // tests if only one instantiated
	
	protected void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = request.getParameter("name");
		String msgType = request.getParameter("msgType");
		String docType = request.getParameter("docType");
		
		ConsumerObject c = new ConsumerObject(name, msgType, docType);
		
		if (numberOfConsumers==0)
			numberOfConsumers = 1;
		else
			numberOfConsumers++;
		
		consumerMap.put(numberOfConsumers, c);
		
		RequestDispatcher view = request.getRequestDispatcher(REDIRECT_FILE);
		view.forward(request, response);
	}
	
	public RoutingConsumer() throws IOException {  
		counter++;
		
		// for debugging purposes
		PrintStream out = new PrintStream(new FileOutputStream("RoutingConsumerOutput.txt"));
		
		if (counter>1)
			out.print("ERROR - counter is greater than one >> RoutingConsumer instantiated more than once\n\n");
		
		if (connection==null) {
			initRabbitMQ();  // may throw IOException
			out.print("initialized RabbitMQ connection\n\n");
		} else
			out.print("RabbitMQ already initialized - double check\n\n");
		
		if (DEBUG) {
			out.print("DEBUG MODE IS ON\n\n");
		}
		
		out.print("[*] Waiting for messages\n\n");

		while (true) {  // waiting for items in RabbitMQ queue
			try {
				
				QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
				
				// TODO because fanout exchange shouldn't have routingkey
//				String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				
				if (message.equals(AbstractProducer.CLOSE_CONSUMER)) {
					out.print("CLOSING THE CONSUMER\n\n");
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				}
				
				Date date = new Date();
				out.print("Recvd: " + date + " - " + message + "\n\n");
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		out.print("CLOSING CONSUMER if doesn't auto-reconnect within 5 seconds\n\n");
		try {
			Thread.currentThread().sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// closes the connection if doesn't try to auto-reconnect 
		closeRabbitMQ();		// may throw IOException
		out.close();
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
