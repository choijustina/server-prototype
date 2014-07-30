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

	//private static final String BINDING_KEY = "json";
	@Override
	public void getData(Channel channel, Connection connection) {}
	
	public static void main(String[] argv) throws Exception, UnknownHostException {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
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
		
		//capped collection, max size is 10kbytes, or 500 documents
		DBCollection collection;
		collection = db.getCollection("SSE");
/*
		if(db.collectionExists("log")) {
			collection = db.getCollection("log");
//			collection = db.runCommand({"convertToCapped": "log", size: 10000, max: 10});
		} else {
			collection = db.createCollection("log");
		}
*/


		//one week = 604800 seconds
		//one day = 86400 seconds
		//one hour = 3600 seconds
		//one minute = 60 seconds

		BasicDBObject index = new BasicDBObject("startDate", 1);
		BasicDBObject options = new BasicDBObject("expireAfterSeconds", 86400);
		collection.ensureIndex(index, options);

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());
		
			JSONObject json = (JSONObject) JSONSerializer.toJSON(message);
			//convert to DBObject
			BasicDBObject dbObject = (BasicDBObject) JSON.parse(message);
			
			dbObject = dbObject.append("startDate", new Date());
			collection.insert(dbObject);
			  
/*
			DBCursor cursor = collection.find();
			while (cursor.hasNext()) {
				System.out.println(cursor.next());
			}
*/
			  
	/*		JSONObject json = (JSONObject) JSONSerializer.toJSON(message);
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

