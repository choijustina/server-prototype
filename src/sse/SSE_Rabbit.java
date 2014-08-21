/**
 * File: SSE_Rabbit.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: July 1, 2014
 * Sources: http://www.rabbitmq.com/tutorials/tutorial-two-java.html
 * Notes: integrating RabbitMQ Consumer; used in version two of SSE.html
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
	//private static final boolean MSG_DURABLE = true;  // so message doesn't get lost if consumer dies
	//private static final int PREFETCH_COUNT = 1;      // maximum number of messages that the server will deliver
	private static final boolean MSG_ACK = false;       // msg acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
//	private static final String bindingKey = "document";	    // # can substitute for zero or more words; * for one word
	
	protected void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		
		// RABBITMQ
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		channel.exchangeDeclare(AbstractProducer.EXCHANGE_NAME, AbstractProducer.EXCHANGE_TYPE);

		// server-generated queue names
		String queueName = channel.queueDeclare().getQueue();
		// already defined queue name (in AbstractProducer.java)
//		String queueName = channel.queueDeclare(AbstractProducer.QUEUE_NAME, true, false, false, null).getQueue();

//		channel.queueBind(queueName, AbstractProducer.EXCHANGE_NAME, bindingKey);		
		channel.queueBind(queueName, AbstractProducer.EXCHANGE_NAME, "");		//3rd parameter is bindingKey; none for fanout exchange
		
		
		PrintWriter out = response.getWriter();
//		out.print("data: queueName is: " + queueName + "\n\n");
		out.print("data: inside SSE_Rabbit.java file\n\n");
//		out.print("data: binding key: " + bindingKey + "\n\n");
		out.print("data: [*] Waiting for messages.\n\n");
		out.flush();
		
		QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, MSG_ACK, queueingConsumer);
		
		Date date = null;
		while (true) {
			try {
				QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
//				String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());
				
				if (message.equals(AbstractProducer.CLOSE_CONSUMER)) {
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				}
				else if (message.equals("clear")) {
					out.print("event: " + "clear" + "\n");
					out.flush();
				}
				else {
					date = new Date();
					//out.print("data: " + date.toString() + " [x] Received " + routingKey + " : '" + message + "'" + "\n\n");
					out.print("data: " + date.toString() + " [x] Received msg: "  + message + "\n\n");
					out.flush();
				}
				
				// only when message acknowledgment is false?
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				
				//testing queue by throttling
				//Thread.currentThread().sleep(1000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		out.print("data: CLOSING CONSUMER in 10ms\n\n");
		out.flush();
		out.close();
		try {
			Thread.currentThread().sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// closes the connection if doesn't try to auto-reconnect 
		connection.close();
		channel.close();
	}
}