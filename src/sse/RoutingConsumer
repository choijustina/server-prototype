package sse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;

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
	private static final boolean MSG_ACK = false;			// msg acknowledgment off when true; receipts of messages are sent back from consumer telling okay to delete
	private static final String BINDING_KEY = "json";			
	protected static final int RECONNECT_TIME = 10000;		// millisecond delay until auto-reconnect 
	
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
		
		channel.exchangeDeclare(AbstractProducer.EXCHANGE_NAME, AbstractProducer.EXCHANGE_TYPE);
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, AbstractProducer.EXCHANGE_NAME, BINDING_KEY);
		
		PrintWriter out = response.getWriter();
		out.print("retry: "+ RECONNECT_TIME + "\n");
		out.print("data: " + "RoutingConsumer.java\n\n");
		out.print("data: binding key: " + BINDING_KEY + "\n\n");
		out.print("data: [*] Waiting for messages.\n\n");
		out.flush();
		
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, MSG_ACK, consumer);
		
		while (true) {
			try {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				//String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());
				
				if (message.equals(AbstractProducer.CLOSE_CONSUMER)) {
					break;
				}
				
				JSONObject json = (JSONObject) JSONSerializer.toJSON(message);
		    	int ids = json.getInt( "id" );
//		    	out.print("json: " + message + "\n");
				out.print("data: " + message + "\n\n");
				out.flush();
				
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
