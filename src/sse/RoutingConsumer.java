/**
 * File: RoutingConsumer.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: July 21, 2014
 * Notes: RabbitMQ Consumer; modified version of SSE_Rabbit.java
 */

package sse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@WebServlet("/RoutingConsumer")
public class RoutingConsumer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final boolean MSG_ACK = false;			// message acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
	protected static final int RECONNECT_TIME = 3000;		// delay in milliseconds until auto-reconnect 
	private static final String REDIRECT_FILE = "index.html";
	
	private static Connection connection = null;  //only one RabbitMQ connection
	private static Channel channel = null;
	private static QueueingConsumer queueingConsumer = null;
	
	private static Map<Integer, Consumer> consumerMap = new HashMap<Integer, Consumer>();
	private static int numberOfConsumers = 0;
	
	private static int counter = 0;
	public RoutingConsumer() {  // testing if only one instantiated
		counter++;
	}
	
	protected void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String str = request.getParameter("name");
		if (str==null)
			isEventStream(request, response);
		else {
			try {
				isHTML(request, response);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * isHTML - after search page, goes to this method to get filter criteria data then redirects to REDIRECT_FILE
	 */
	protected void isHTML(HttpServletRequest request, HttpServletResponse response) 
			throws IOException, InterruptedException, ServletException {

		String name = request.getParameter("name");
		
		// TODO: if request.getParam returns null, then don't search for it
		// TODO: add getParamater for other filter criteria
		
		Consumer c = new Consumer(name);
		
		if (numberOfConsumers==0)
			numberOfConsumers = 1;
		else
			numberOfConsumers++;
		
		consumerMap.put(numberOfConsumers, c);
		
		RequestDispatcher view = request.getRequestDispatcher(REDIRECT_FILE);
		view.forward(request, response);
		
	}
	
	/*
	 * isEventStream - when newSSE.html loads, connects to RoutingConsumer as an EventSource
	 */
	private void isEventStream(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		
		PrintWriter out = response.getWriter();
		out.print("retry: " + RECONNECT_TIME + "\n\n");

		out.print("event: consumerKey\n");
		out.print("data: " + numberOfConsumers + "\n\n");
		
		if (connection==null)
			initRabbitMQ();
		else
			out.print("data: RabbitMQ already initialized - double check\n\n");
		
		out.print("data: [*] Waiting for messages\n\n");
		
		out.print("data: numberOfConsumers counter: " + numberOfConsumers + "\n\n");
		out.print("data: size of consumerMap: " + consumerMap.size() + "\n\n");

		/*
		for (int i = 1; i <= numberOfConsumers; i++) {
			//out.print("data: consumer " + i + ": " + consumerMap.get(i) + "\n\n");
			Consumer consumer = consumerMap.get(i);
			String n = consumer.getName();
			out.print("data: consumer " + i + " is searching for: " + n + "\n\n");
		}
		*/
		Consumer consumer = consumerMap.get(numberOfConsumers);
		String n = consumer.getName();
		out.print("data: consumer " + numberOfConsumers + " is searching for " + n + "\n\n");
		out.flush();
		
		while (true) {
			try {
				QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
				
				//String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());
				
				if (message.equals(AbstractProducer.CLOSE_CONSUMER))
					break;			
		
				// assumes is a json object
				out.print("event: jsonobject\n");
				
				JSONParser parser = new JSONParser();
				JSONObject jsonObject = (JSONObject) parser.parse(message);
								
				String eventName = (String) jsonObject.get("name");
//				String eventID = (String) jsonObject.get("id");
//				String eventMsgType = (String) jsonObject.get("msgtype");
//				String eventDocType = (String) jsonObject.get("doctype");
//				String eventData = (String) jsonObject.get("data");
				
				// check if the event matches any of the filter criteria
				for (int i = 1; i <= numberOfConsumers; i++) {
					Consumer c = consumerMap.get(i);
					String s = c.getName();
					
					if (s.equals(eventName)) {
						jsonObject.put("consumerKey", i);
						out.print("data: " + jsonObject + "\n\n");
					}

					out.flush();
				}
				
				
				/*
				if (message.equals(AbstractProducer.CLOSE_CONSUMER))
					break;
				else {
					if (message.equals("clear")) {
						out.print("event: clear\n");
						out.print("data: clearing the client display\n\n");
					} else if (routingKey.equals("json")){
						out.print("event: jsonobject\n");
						out.print("data: " + message + "\n\n");
						
//						JSONParser parser = new JSONParser();
//						JSONObject jsonObject = (JSONObject) parser.parse(message);
//						String eventName = (String) jsonObject.get("name");
//						String eventID = (String) jsonObject.get("id");
//						String eventMsgType = (String) jsonObject.get("msgtype");
//						String eventDocType = (String) jsonObject.get("doctype");
//						String eventData = (String) jsonObject.get("data");
						
						//out.print("data: name is " + eventName + "\n\n");
					} else {
						out.print("data: " + routingKey + " - " + message + "\n\n");
					}
					//out.print("data: " + message + "\n\n");
					out.flush();
				}
				*/
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		out.close();
		closeRabbitMQ();
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
		channel.queueDeclare(AbstractProducer.QUEUE_NAME, true, false, false, null);
		channel.queueBind(AbstractProducer.QUEUE_NAME, AbstractProducer.EXCHANGE_NAME, "");
		
		queueingConsumer = new QueueingConsumer(channel);
		channel.basicConsume(AbstractProducer.QUEUE_NAME, MSG_ACK, queueingConsumer);
	}
	
	private void closeRabbitMQ() throws IOException {
		connection.close();
		channel.close();
	}

}
