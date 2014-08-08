import java.io.IOException;
import java.util.Scanner;
import java.net.MalformedURLException;
import java.net.URL;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;


public class SENDJSON extends Abstract {
	//make sure to put a json.txt in your war file to be uploaded to tomcat
	String filename = "json.txt";

	@Override
	public void getData(Channel channel, Connection connection) {
		try {

			URL url = new URL("http://localhost:8080/SSE/" + filename);
			Scanner scanner = new Scanner(url.openStream());
			messageData = scanner.nextLine();
		
			while (scanner.hasNextLine()) {
				channel.basicPublish(EXCHANGE_NAME, "", 
						MessageProperties.PERSISTENT_TEXT_PLAIN, messageData.getBytes());
				Thread.currentThread().sleep(250);
				System.out.println("[x] Sent " + messageData + "'");
				messageData = scanner.nextLine();
			}
			scanner.close();
			
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {		// channel.basicPublish() method
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("closing producer");
		closeQueue(channel, connection);
	}

  public static void main(String[] argv) throws Exception {
		SENDJSON sj = new SENDJSON();
		sj.createQueue();
		System.out.println("EOF");
  }
  
}

