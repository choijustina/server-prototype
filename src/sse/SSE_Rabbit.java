/**
 * File: SSE_Rabbit.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: July 1, 2014
 * Sources: http://www.rabbitmq.com/tutorials/tutorial-two-java.html
 * Notes: RabbitMQ Consumer
 */

package sse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

@WebServlet("/SSE_Rabbit")
public class SSE_Rabbit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String EXCHANGE_NAME = "topicexchange";
	//private final static String QUEUE_NAME = "queue";
	//private final static boolean MSG_DURABLE = true;  // so message doesn't get lost if consumer dies
	//private final static int PREFETCH_COUNT = 1;     // maximum number of messages that the server will deliver
	private final static boolean MSG_ACK = false;    // msg acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
	private final String BINDING_KEY = "high.admin.#";
	
	protected void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
	
		channel.exchangeDeclare(EXCHANGE_NAME, "topic"); 	// a durable, non-autodelete exchange of "topic" type
		String queueName = channel.queueDeclare().getQueue();	    //get server-generated queue name
		channel.queueBind(queueName, EXCHANGE_NAME, BINDING_KEY);
//		channel.queueDeclare(QUEUE_NAME, MSG_DURABLE, false, false, null);
		
		PrintWriter out = response.getWriter();
		out.print("data: binding key: " + BINDING_KEY + "\n\n");
		out.print("data: [*] Waiting for messages.\n\n");
		out.flush();
		//channel.basicQos(PREFETCH_COUNT);
		
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, MSG_ACK, consumer);
		
		while (true) {
			try {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				//String message = new String(delivery.getBody());
				
				String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());
				
				if (message.equals("close consumer")) {
					break;
				}
				else if (message.equals("clear")) {
					out.print("event: " + "clear" + "\n");
					out.print("data: " + "binding key: " + BINDING_KEY + "\n\n");
					out.flush();
				}
				else {
					Date date = new Date();
					out.print("data: " + date.toString() + " [x] Received " + routingKey + " : '" + message + "'" + "\n\n");
					out.flush();
				}
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				
				//testing queue
				//Thread.currentThread().sleep(1000);
				
			} catch (InterruptedException e) {
				e.getStackTrace();
			}
		}
		connection.close();
		channel.close();
		out.close();
	}
	
	
}