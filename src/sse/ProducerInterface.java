/**
 * 
 */
package sse;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * File: ProducerInterface.java
 * @author Justina Choi (choi.justina@gmail.com)
 * Date: July 7, 2014
 * Implemented by: ConsoleProducer.java, TextfileProducer.java
 *
 */
public interface ProducerInterface {
	public final String EXCHANGE_NAME = "exchange";
	public final String QUEUE_NAME = "queue";
	//public final boolean MSG_DURABLE = true;  // so message doesn't get lost if consumer dies
	
	public void createQueue();
	public void getData(Channel channel, Connection connection);
	public void closeQueue(Channel channel, Connection connection);
}
