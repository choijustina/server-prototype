/**
 * File: RoutingConsumer.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: August 18, 2014
 * Notes: RabbitMQ Consumer; modified version of SSE_Rabbit.java
 */

package sse;

import com.mongodb.BasicDBObject;
//import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
//import com.mongodb.DBCursor;
//import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@WebServlet("/RoutingConsumer")
public class RoutingConsumer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final boolean MSG_ACK = false;			// TODO: message acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
	private static final int RECONNECT_TIME = 3000;			// delay in milliseconds until auto-reconnect 
	private static final String REDIRECT_FILE = "index.html";
	private static final String bindingKey = "";
	
	private Connection connection = null;					//only one RabbitMQ connection
	private Channel channel = null;
	private QueueingConsumer queueingConsumer = null;
	
	private static Map<Integer, Consumer> consumerMap = new HashMap<Integer, Consumer>();
	private static int numberOfConsumers = 0;
	
	private DBCollection collection;
	
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
	 * isEventStream - when SSE.html loads, connects to RoutingConsumer as an EventSource
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
		
		out.print("data: inside RoutingConsumer.java\n\n");
		
		
		if (connection==null) {
			initRabbitMQ();
			out.print("data: initialized RabbitMQ connection\n\n");
		} else
			out.print("data: RabbitMQ already initialized - double check\n\n");

		if (DEBUG) {
			out.print("data: DEBUG MODE IS ON\n\n");
			out.print("data: numberOfConsumers counter: " + numberOfConsumers + "\n\n");
			out.print("data: size of consumerMap: " + consumerMap.size() + "\n\n");

			Consumer consumer = consumerMap.get(numberOfConsumers);
			String a = consumer.getName();
			String b = consumer.getDocType();
			String c = consumer.getMsgType();
			
			// MESSAGE TYPE
			if (c.equals(""))
				out.print("data: message type is an empty string for consumer " + numberOfConsumers + "\n\n");
			else
				out.print("data: consumer " + numberOfConsumers + " is searching for message type " + c + "\n\n");
			
			// DOCUMENT TYPE
			if (b.equals(""))
				out.print("data: document type is an empty string for consumer " + numberOfConsumers + "\n\n");
			else
				out.print("data: consumer " + numberOfConsumers + " is searching for document type " + b + "\n\n");
			
			out.print("data: consumer " + numberOfConsumers + " is searching for the name '" + a + "'\n\n");
		}
		
//		if (initMongo() && DEBUG)
//			out.print("data: successfully initialized Mongo database\n\n");
		
		out.print("data: [*] Waiting for messages\n\n");
		out.flush();
		
		while (true) {
			try {
				QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
				
				// when being sent from JSONProducer, always "json"
//				String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());
				
				if (message.equals(AbstractProducer.CLOSE_CONSUMER)) {
					out.print("data: CLOSING THE CONSUMER\n\n");
					out.flush();
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				}
				
//				if (addtoMongo(message) && DEBUG)
//					out.print("data: successfully added to MongoDB\n\n");
				
				JSONParser parser = new JSONParser();
				JSONObject jsonObject = (JSONObject) parser.parse(message);
								
				String eventName = (String) jsonObject.get("name");
//				String eventID = (String) jsonObject.get("id");
				String eventMsgType = (String) jsonObject.get("msgtype");
				String eventDocType = (String) jsonObject.get("doctype");
				String eventData = (String) jsonObject.get("data");
				
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
					
					
//					if (DEBUG) out.print("data: case number is " + caseNum + "\n\n");
					
					switch(caseNum) {
					case 0:
						out.print("data: ERROR - UNEXPECTED CASE\n\n");
					case 1:				// searching name, message type and document type
						if ( (consumerName.equals(eventName)) && (consumerMsgType.equals(eventMsgType)) && (consumerDocType.equals(eventDocType)) ) {
							jsonObject.put("consumerKey", i);
							if(DEBUG) out.print("data: case 1 & sending to consumerID: " + i + "; eventdata: " + eventData + "\n\n");
							out.print("event: jsonobject\n");
							out.print("data: " + jsonObject + "\n\n");
						}
						break;
					case 2:				// searching name
						if (consumerName.equals(eventName)) {
							jsonObject.put("consumerKey", i);
							if(DEBUG) out.print("data: case 2 & sending to consumerID: " + i + "; eventdata: " + eventData + "\n\n");
							out.print("event: jsonobject\n");
							out.print("data: " + jsonObject + "\n\n");
						}
						break;
					case 3:				// searching name & document type
						if ( (consumerName.equals(eventName)) && (consumerDocType.equals(eventDocType)) ) {
							jsonObject.put("consumerKey", i);
							if(DEBUG) out.print("data: case 3 & sending to consumerID: " + i + "; eventdata: " + eventData + "\n\n");
							out.print("event: jsonobject\n");
							out.print("data: " + jsonObject + "\n\n");
						}
						break;
					case 4:				// searching name & message type
						if ( (consumerName.equals(eventName)) && (consumerMsgType.equals(eventMsgType)) ) {
							jsonObject.put("consumerKey", i);
							if(DEBUG) out.print("data: case 4 & sending to consumerID: " + i + "; eventdata: " + eventData + "\n\n");
							out.print("event: jsonobject\n");
							out.print("data: " + jsonObject + "\n\n");
						}
						break;
					}
					
					out.flush();
				}
				
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		out.print("data: CLOSING CONSUMER if doesn't auto-reconnect within 5 seconds\n\n");
		out.flush();
		out.close();
		try {
			Thread.currentThread().sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// closes the connection if doesn't try to auto-reconnect 
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
	
	private boolean initMongo() {
		try {
			//connect to local db server
			MongoClient mongoClient = new MongoClient();
			
			//get handle to db "JSONDB"
			DB db = mongoClient.getDB("JSONDB");
			
			//authenticate (optional)
			//boolean auth = db.authenticate("user","pass");
			
			collection = db.getCollection("SSE");

			//one week = 604800 seconds
			//one day = 86400 seconds
			//one hour = 3600 seconds
			//one minute = 60 seconds

			//creates index startDate, setup so that stuff deletes after a week
			BasicDBObject index = new BasicDBObject("startDate", 1);
			BasicDBObject options = new BasicDBObject("expireAfterSeconds", 604800);
			collection.ensureIndex(index, options);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean addtoMongo(String msg) {
		//convert to DBObject, add startDate, insert into DB
		BasicDBObject dbObject = (BasicDBObject) JSON.parse(msg);
		dbObject = dbObject.append("startDate", new Date());
		collection.insert(dbObject);
		return true;
	}

}
