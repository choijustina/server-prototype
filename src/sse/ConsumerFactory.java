package sse;

public class ConsumerFactory {
	public static Consumer buildConsumer(ConsumerType type) {
		Consumer consumer = null;
		switch(type) {
		case BASIC:
			consumer = new BasicConsumer();
			break;
			
		case MONGO:
			consumer = new MongoConsumer();
			break;
		}
		return consumer;
		
	}

}
