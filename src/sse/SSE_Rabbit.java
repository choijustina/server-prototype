package sse;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * File: SSE_Rabbit.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: July 1, 2014
 * Sources: http://www.rabbitmq.com/tutorials/tutorial-two-java.html
 * Notes: RabbitMQ consumer
 */

@WebServlet("/SSE_Rabbit")
public class SSE_Rabbit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final static String QUEUE_NAME = "queue";
	private final static boolean MSG_DURABLE = true;  // so message doesn't get lost if consumer dies
	private final static int PREFETCH_COUNT = 1;      // limits the number of messages a consumer has at a time
	private final static boolean MSG_ACK = true;     // auto msg acknowledgement on when true; receipts of messages are sent back from consumer telling okay to delete
	
	protected void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");

		PrintWriter out = response.getWriter();
		out.print("data: SSE_Rabbit.java with queue name: " + QUEUE_NAME + "\n\n");
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		channel.queueDeclare(QUEUE_NAME, MSG_DURABLE, false, false, null);
		out.print("data: [*] Waiting for messages.\n\n");
		out.flush();
		//channel.basicQos(PREFETCH_COUNT);
		
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(QUEUE_NAME, MSG_ACK, consumer);
		
		while (true) {
			try {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String message = new String(delivery.getBody());
				if (message.equals("close consumer")) {
					out.print("data: breaking out of loop to close\n\n");
					out.flush();
					break;
				}
				else {
					out.print("data: [x] Received '" + message + "'" + "\n\n");
					out.flush();
					if (!MSG_ACK) {
						out.print("data: basicAck now\n\n");
						out.flush();
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					}
				}
			} catch (InterruptedException e) {
				e.getStackTrace();
			}
		}
		connection.close();
		channel.close();
		out.close();
	}
	
	
}