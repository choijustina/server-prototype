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

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/RoutingConsumer")
public class RoutingConsumer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final boolean MSG_ACK = false;			// message acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
	//private static final String BINDING_KEY = "json";			
	protected static final int RECONNECT_TIME = 10000;		// delay in milliseconds until auto-reconnect 
	
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
		channel.queueDeclare(AbstractProducer.QUEUE_NAME, true, false, false, null);
		channel.queueBind(AbstractProducer.QUEUE_NAME, AbstractProducer.EXCHANGE_NAME, "");
		
		// merging changes
		//String queueName = channel.queueDeclare().getQueue();
		//channel.queueBind(queueName, AbstractProducer.EXCHANGE_NAME, BINDING_KEY);
		
		PrintWriter out = response.getWriter();
		out.print("retry: 3000\n");
		
		BasicConsumer c1 = (BasicConsumer) ConsumerFactory.buildConsumer(ConsumerType.BASIC);
		c1.setName(SearchParameters.name);
		c1.setBusinessKey(SearchParameters.businessKey);
		c1.setDocumentType(SearchParameters.documentType);
		c1.setDate(SearchParameters.date);
		
		out.print("data: created a basic consumer inside RoutingConsumer.java\n\n");
		out.print("data: name: " + c1.getName() + "\n\n");
		out.print("data: business key: " + c1.getBusinessKey() + "\n\n");
		out.print("data: document type: " + c1.getDocumentType() + "\n\n");
		out.print("data: date: " + c1.getDate() + "\n\n");		
		out.print("data: [*] Waiting for messages\n\n");
		out.flush();
		
		QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
		channel.basicConsume(AbstractProducer.QUEUE_NAME, MSG_ACK, queueingConsumer);
		
		while (true) {
			try {
				QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
				
				//String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());
				
				//JSONObject object = new JSONObject(message);
				//String name = object.getString("name");
				
				if (message.equals(AbstractProducer.CLOSE_CONSUMER))
					break;
				else {
					/*
					if (message.equals("clear")) {
						out.print("event: clear\n");
						out.print("data: clearing the client display\n\n");
					} else {
						out.print("event: jsonobject\n");
						out.print("data: " + message + "\n\n");
					}*/
					out.print("data: " + message + "\n\n");
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
