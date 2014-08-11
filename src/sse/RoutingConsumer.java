/**
 * File: RoutingConsumer.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: July 21, 2014
 * Notes: RabbitMQ Consumer; modified version of SSE_Rabbit.java
 */

package sse;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

@WebServlet("/RoutingConsumer")
public class RoutingConsumer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final boolean MSG_ACK = false;			// message acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
	//private static final String BINDING_KEY = "json";			
	protected static final int RECONNECT_TIME = 3000;		// delay in milliseconds until auto-reconnect 
	
	//private static Connection connection;
	//private static Channel channel;
	//private int initialized = 0; // 0 false, 1 true 
	
	private String consumerKey = "abc";  // TODO: create a way to make this unique for each consumer initialized
	
	private static int counter = 0;
	public RoutingConsumer() {  // only one instantiated
		counter++;
	}
	/*
	private Connection initConnection(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
		// RABBITMQ
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		connection = factory.newConnection();
		return connection;
	}
	private Channel initChannel(Connection connection) throws IOException {
		channel = connection.createChannel();
		return channel;
	}*/
	
	protected void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		/*
		if (initialized==0) {
			initialized = 1;
			connection = initConnection(request, response);
			channel = initChannel(connection);
		}*/
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		channel.exchangeDeclare(AbstractProducer.EXCHANGE_NAME, AbstractProducer.EXCHANGE_TYPE);
		channel.queueDeclare(AbstractProducer.QUEUE_NAME, true, false, false, null);
		channel.queueBind(AbstractProducer.QUEUE_NAME, AbstractProducer.EXCHANGE_NAME, "");
		
		PrintWriter out = response.getWriter();
		out.print("retry: " + RECONNECT_TIME + "\n\n");
		
		out.print("event: consumerKey\n");
		out.print("data: " + consumerKey + "\n\n");
		
		/*
		out.print("event: " + consumerKey + "\n");
		out.print("data: to confirm that consumerKey works as an event name\n\n");
		*/
		
		out.print("data: [*] Waiting for messages\n\n");
		out.flush();
		
		QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
		channel.basicConsume(AbstractProducer.QUEUE_NAME, MSG_ACK, queueingConsumer);
		
		while (true) {
			try {
				QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
				
				String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());
				
				if (message.equals(AbstractProducer.CLOSE_CONSUMER))
					break;
				else {
					
					if (message.equals("clear")) {
						out.print("event: clear\n");
						out.print("data: clearing the client display\n\n");
					} else if (routingKey.equals("json")){
						out.print("event: jsonobject\n");
						out.print("data: " + message + "\n\n");
					} else {
						out.print("data: " + routingKey + " - " + message + "\n\n");
					}
					//out.print("data: " + message + "\n\n");
					out.flush();
				}
				
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		connection.close();
		channel.close();
		out.close();
	}

}
