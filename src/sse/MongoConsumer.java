package sse;

public class MongoConsumer extends Consumer {

	public MongoConsumer() {
		super(ConsumerType.MONGO);
		construct();
	}
	
	@Override
	protected void construct() {
		System.out.println("building mongo consumer");
	}

}
