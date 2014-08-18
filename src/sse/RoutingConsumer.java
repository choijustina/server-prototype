/**
 * File: RoutingConsumer.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: August 18, 2014
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
	private static final boolean MSG_ACK = false;			// TODO: message acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
	protected static final int RECONNECT_TIME = 3000;		// delay in milliseconds until auto-reconnect 
	private static final String REDIRECT_FILE = "index.html";
	private static final String bindingKey = "json";	
	
	private Connection connection = null;  //only one RabbitMQ connection
	private Channel channel = null;
	private QueueingConsumer queueingConsumer = null;
	
	private static Map<Integer, Consumer> consumerMap = new HashMap<Integer, Consumer>();
	private static int numberOfConsumers = 0;
	
	private static final boolean DEBUG = true;
	
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
		String msgType = request.getParameter("msgType");
		String docType = request.getParameter("docType");
		
		// TODO: if request.getParam returns null/empty string, then don't search for it
		// TODO: add getParamater for other filter criteria
		
//		Consumer c = new Consumer(name);
//		Consumer c = new Consumer(name, docType);
		Consumer c = new Consumer(name, msgType, docType);
		
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

		if (DEBUG) {
			out.print("data: DEBUG MODE IS ON\n\n");
			out.print("data: numberOfConsumers counter: " + numberOfConsumers + "\n\n");
			out.print("data: size of consumerMap: " + consumerMap.size() + "\n\n");

			Consumer consumer = consumerMap.get(numberOfConsumers);
			String a = consumer.getName();
			String b = consumer.getDocType();
			String c = consumer.getMsgType();
			
			if (c.equals(""))
				out.print("data: message type is an empty string for consumer " + numberOfConsumers + "\n\n");
			else
				out.print("data: consumer " + numberOfConsumers + " is searching for message type " + c + "\n\n");
			
			
			if (b.equals(""))
				out.print("data: document type is an empty string for consumer " + numberOfConsumers + "\n\n");
			else
				out.print("data: consumer " + numberOfConsumers + " is searching for document type " + b + "\n\n");
			
			out.print("data: consumer " + numberOfConsumers + " is searching for the name '" + a + "'\n\n");
			out.flush();
			
		}
		
		
		while (true) {
			try {
				QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
				
				//String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());

				if (message.equals(AbstractProducer.CLOSE_CONSUMER))
					break;			
				
				JSONParser parser = new JSONParser();
				JSONObject jsonObject = (JSONObject) parser.parse(message);
								
				String eventName = (String) jsonObject.get("name");
//				String eventID = (String) jsonObject.get("id");
				String eventMsgType = (String) jsonObject.get("msgtype");
				String eventDocType = (String) jsonObject.get("doctype");
//				String eventData = (String) jsonObject.get("data");
				
				// check if the event matches any of the filter criteria
				for (int i = 1; i <= numberOfConsumers; i++) {
					Consumer thisConsumer = consumerMap.get(i);
					String consumerName = thisConsumer.getName();
					String consumerMsgType = thisConsumer.getMsgType();
					String consumerDocType = thisConsumer.getDocType();
					
					int caseNum = 0; 
					if (consumerName.isEmpty())
						out.print("data: ERROR: must at least input a name to search\n\n");
					else if (consumerMsgType.isEmpty() && consumerDocType.isEmpty())
						caseNum = 2;						// searching name
					else if (consumerMsgType.isEmpty())
						caseNum = 3;						// searching name & document type
					else if (consumerDocType.isEmpty())
						caseNum = 4;						// searching name & message type
					else
						caseNum = 1;						// searching name, message type and document type (ALL OPTIONS)
					
					
					if (DEBUG) out.print("data: case number is " + caseNum + "\n\n");
					
					// assumes is a json object
//					out.print("event: jsonobject\n");
					
					switch(caseNum) {
					case 0:
						out.print("data: ERROR - UNEXPECTED CASE\n\n");
					case 1:				// searching name, message type and document type
						if ( (consumerName.equals(eventName)) && (consumerMsgType.equals(eventMsgType)) && (consumerDocType.equals(eventDocType)) ) {
							jsonObject.put("consumerKey", i);
							out.print("data: " + jsonObject + "\n\n");
						}
//						if (DEBUG) {
//							out.print("data: " + "consumerName: " + consumerName + "; eventName: " + eventName + "<br>\n\n");
//							out.print("data: " + "conumserMsgType: " + consumerMsgType + "; eventMsgType: " + eventMsgType + "\n\n");
//							out.print("data: " + "consumerDocType: " + consumerDocType + "; eventDocType: " + eventDocType + "\n\n");
//						}
					case 2:				// searching name
						if (consumerName.equals(eventName)) {
							jsonObject.put("consumerKey", i);
							out.print("data: " + jsonObject + "\n\n");
						}
					case 3:				// searching name & document type
						if ( (consumerName.equals(eventName)) && (consumerDocType.equals(eventDocType)) ) {
							jsonObject.put("consumerKey", i);
							out.print("data: " + jsonObject + "\n\n");
						}
					case 4:				// searching name & message type
						if ( (consumerName.equals(eventName)) && (consumerMsgType.equals(eventMsgType)) ) {
							jsonObject.put("consumerKey", i);
							out.print("data: " + jsonObject + "\n\n");
						}
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
//		channel.queueBind(AbstractProducer.QUEUE_NAME, AbstractProducer.EXCHANGE_NAME, "");
		channel.queueBind(AbstractProducer.QUEUE_NAME, AbstractProducer.EXCHANGE_NAME, bindingKey);
		
		
		queueingConsumer = new QueueingConsumer(channel);
		channel.basicConsume(AbstractProducer.QUEUE_NAME, MSG_ACK, queueingConsumer);
	}
	
	private void closeRabbitMQ() throws IOException {
		connection.close();
		channel.close();
	}

}
