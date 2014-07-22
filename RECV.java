import java.io.InputStream;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class RECV {

  private static final String EXCHANGE_NAME = "logs";

  public static void main(String[] argv) throws Exception {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    String queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName, EXCHANGE_NAME, "");
    
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(queueName, true, consumer);

    while (true) {
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      String message = new String(delivery.getBody());
      
      JSONObject json = (JSONObject) JSONSerializer.toJSON(message);
    	int id = json.getInt( "id" );
    	String msgtype = json.getString( "msgtype");
    	String name = json.getString( "name" );
    	String doctype = json.getString( "doctype" );
    	String data = json.getString( "data" ); 	
    	
    	System.out.println( "ID: " + id );
    	System.out.println( "MSGTYPE: " + msgtype );
    	System.out.println( "NAME: " + name );
    	System.out.println( "DOCTYPE: " + doctype );
    	System.out.println( "DATA: " + data + "\n");

      System.out.println(" [x] Received '" + message + "'");   
    }
  }
}

