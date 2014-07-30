/**
 * File: LogProducer.java
 */

package sse;

import java.io.IOException;
import java.lang.String;
import java.util.logging.*;
import java.util.Scanner;
import java.util.Date;
import java.text.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

public class LogProducer extends AbstractProducer {
	private final static Logger logger = Logger.getLogger("LogProducer");
	String d = "";
	
	public static String getDate(){
		Date d = new Date();
		return d.toString();	
	}
	
	@Override
	public void getData(Channel channel, Connection connection) {
		Scanner scanner = new Scanner(System.in);
				System.out.println("NEW PRODUCER: Press enter after every message you would like to send.\n"
				+ "Prints to logfile 'LOG_year-month-day_hour-minute-seconds.log" + "\n" 
				+ "Specific commands: '" + CLOSE_PRODUCER + "', '" + CLOSE_CONSUMER + "' and 'clear'"); 
		
		try {
			while (!(scanner.hasNext(CLOSE_PRODUCER))) {
				bindingKey = scanner.next();
				messageData = scanner.next() + scanner.nextLine();
				
				channel.basicPublish(EXCHANGE_NAME, bindingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageData.getBytes());
				System.out.println("  [x] Sent " + bindingKey + " : '" + messageData + "'");
				String d = getDate();
				logger.info(d + " : " + bindingKey + " : '" + messageData + "'");
			}
		} catch (IOException exception) {
			exception.printStackTrace(); 
		}
		System.out.println("closing producer");
		scanner.close();
		closeQueue(channel, connection);
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		logger.setUseParentHandlers(false);
		Handler consoleHandler = new ConsoleHandler();
	
		
		consoleHandler.setFormatter(new Formatter() {
			public String format(LogRecord record) {
				return record.getLevel() + " : " 
					+ record.getMessage() + "\n";
			}
		});

		Date blah = new Date();
		SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String pattern = String.format("LOG_" + dformat.format(blah) + ".log");
//		String pattern = String.format("LOG_" + timeStamp + ".log");
		Handler fileHandler = new FileHandler(pattern);	
		
		fileHandler.setFormatter(new Formatter() {
			public String format(LogRecord record) {
				return record.getLevel() + " : " 
					+ record.getMessage() + "\n";
			}
		});
	
		logger.addHandler(consoleHandler);
		logger.addHandler(fileHandler);
		
		LogProducer lp = new LogProducer();
		lp.createQueue();
		System.out.println("EOF");
		
	}

}