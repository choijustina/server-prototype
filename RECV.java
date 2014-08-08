import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Date;
import java.util.Set;
import java.lang.Object;
import java.io.InputStream;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class RECV extends Abstract {
	//DATABASE CONSUMER (prints out JSON to terminal, sends JSON to MongoDB
	//database: JSONDB
	//collection: SSE


	//private static final String BINDING_KEY = "json";
	@Override
	public void getData(Channel channel, Connection connection) {}
	
	public static void main(String[] argv) throws Exception, UnknownHostException {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE, true);
		channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
		
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(QUEUE_NAME, true, consumer);

		//connect to local db server
		MongoClient mongoClient = new MongoClient();
		
		//get handle to db "JSONDB"
		DB db = mongoClient.getDB("JSONDB");
		
		//authenticate (optional)
		//boolean auth = db.authenticate("user","pass");
		
		DBCollection collection;
		collection = db.getCollection("SSE");

		//one week = 604800 seconds
		//one day = 86400 seconds
		//one hour = 3600 seconds
		//one minute = 60 seconds

		//creates index startDate, setup so that stuff deletes after a week
		BasicDBObject index = new BasicDBObject("startDate", 1);
		BasicDBObject options = new BasicDBObject("expireAfterSeconds", 604800);
		collection.ensureIndex(index, options);

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());
			
			//convert to DBObject, add startDate, insert into DB
			BasicDBObject dbObject = (BasicDBObject) JSON.parse(message);
			dbObject = dbObject.append("startDate", new Date());
			collection.insert(dbObject);
			  
/* LOOPS THROUGH AND PRINTS ALL OF CURSOR TO CONSOLE
			DBCursor cursor = collection.find();
			while (cursor.hasNext()) {
				System.out.println(cursor.next());
			}
*/
			  
/* PRINT PARSED JSON TO CONSOLE
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
*/
			  System.out.println(" [x] Received '" + message + "'");   
		}
  }
}

