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
	//private static final String BINDING_KEY = "json";			
	protected static final int RECONNECT_TIME = 10000;		// delay in milliseconds until auto-reconnect 
	private static final String REDIRECT_FILE = "newSSE.html";
	
	private static Map<Integer, Consumer> consumerMap = new HashMap<Integer, Consumer>();
	private static int numberOfConsumers = 0;
	
	// mapping connectionID -> Consumer
	
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
		String str = request.getParameter("searchName");
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
		/*
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Search Results</title>");
		out.println("<script type=\"text/javascript\">");
		out.println("</script>");
		out.println("</head>");
		
		out.println("<body>");
		out.println("<h1>RoutingConsumer.java</h1><br>");
		
		String str = request.getParameter("searchName");
		out.println("<p>searchName is: " + str + "</p><br>");
		
		out.println("<p>Should redirect after " + RECONNECT_TIME + " milliseconds</p><br>");
		
//		out.println("</body>");
//		out.println("</html>");
//		out.flush();
//		out.close();
		
//		Thread.currentThread().sleep(RECONNECT_TIME);
		*/
		
		String name = request.getParameter("searchName");
		Consumer c = new Consumer(name);
		
		if (numberOfConsumers==0)
			numberOfConsumers = 1;
		else
			numberOfConsumers++;
		
		consumerMap.put(numberOfConsumers, c);
		
		RequestDispatcher view = request.getRequestDispatcher(REDIRECT_FILE);
		view.forward(request, response);
		
		
//		out.println("</body>");
//		out.println("</html>");
//		out.flush();
//		out.close();
	}
	
	/*
	 * isEventStream - when newSSE.html loads, connects to RoutingConsumer as an EventSource
	 */
	private void isEventStream(HttpServletRequest request, HttpServletResponse response)
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
		
		// RABBITMQ
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(AbstractProducer.EXCHANGE_NAME, AbstractProducer.EXCHANGE_TYPE);
		channel.queueDeclare(AbstractProducer.QUEUE_NAME, true, false, false, null);
		channel.queueBind(AbstractProducer.QUEUE_NAME, AbstractProducer.EXCHANGE_NAME, "");
		
		PrintWriter out = response.getWriter();
		out.print("retry: " + RECONNECT_TIME + "\n\n");
		/*
		out.print("event: consumerKey\n");
		out.print("data: " + consumerKey + "\n\n");
		
		out.print("event: " + consumerKey + "\n");
		out.print("data: to confirm that consumerKey works as an event name\n\n");
		*/
		
		/*
		String str = request.getParameter("searchName");
		if (str==null)
			out.print("data: (Java) searchName parameter doesn't exist\n\n");
		else
			out.print("data: (Java) using request.getParameter to get searchName - " + str + "\n\n");
		*/
		
		out.print("data: [*] Waiting for messages\n\n");
		
		out.print("data: numberOfConsumers counter: " + numberOfConsumers + "\n\n");
		out.print("data: size of consumerMap: " + consumerMap.size() + "\n\n");
		
		for (int i = 1; i <= numberOfConsumers; i++) {
			//out.print("data: consumer " + i + ": " + consumerMap.get(i) + "\n\n");
			Consumer consumer = consumerMap.get(i);
			String n = consumer.name;
			out.print("data: consumer " + i + " is searching for: " + n + "\n\n");
		}
		
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
						/*
						JSONParser parser = new JSONParser();
						JSONObject jsonObject = (JSONObject) parser.parse(message);
						String eventName = (String) jsonObject.get("name");
						String eventID = (String) jsonObject.get("id");
						String eventMsgType = (String) jsonObject.get("msgtype");
						String eventDocType = (String) jsonObject.get("doctype");
						String eventData = (String) jsonObject.get("data");
						*/
						//out.print("data: name is " + eventName + "\n\n");
					} else {
						out.print("data: " + routingKey + " - " + message + "\n\n");
					}
					//out.print("data: " + message + "\n\n");
					out.flush();
				}
				
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} /*catch (ParseException e) {
				e.printStackTrace();
			}*/
		}
		connection.close();
		channel.close();
		out.close();
		
	}

}
