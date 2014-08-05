import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public abstract class Abstract {
	public static final String EXCHANGE_NAME = "fanoutexchange";
	public static final String EXCHANGE_TYPE = "fanout";
	public static final String QUEUE_NAME = "SSE_EVENTS";		// uses server-generated queue names
	public static final String CLOSE_PRODUCER = "closeproducer";
	public static final String CLOSE_CONSUMER = "closeconsumer";
	//public static final boolean MSG_DURABLE = true;  // so message doesn't get lost if consumer dies
	
	public static String messageData = "";

	abstract void getData(Channel channel, Connection connection);
	
	protected void createQueue() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		
		try {
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.queueDeclare(QUEUE_NAME, true, false, false, null);
			channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE, true);		// a durable, non-autodelete exchange, type is second parameter
			
			getData(channel, connection);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	protected void closeQueue(Channel channel, Connection connection) {
		// TODO delete the queue/exchange?
		
		try {
			channel.close();
			connection.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

}