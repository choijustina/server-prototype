/**
 * File: Consumer.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: July 24, 2014
 * Related files: RoutingConsumer.java
 * Notes:
 */

package sse;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class Consumer {
	protected HttpServletResponse response;
	protected PrintWriter out;
	protected JSONObject object;
	//private static int counter = 0;

	public Consumer(HttpServletResponse r1) {
		//this.counter = this.counter + 1;
		this.response = r1;
		try {
			this.out = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected void addJSONObject(JSONObject obj) {
		this.object = obj;
	}
	
	protected void start() {
		out.print("retry: 1000\n");
		out.print("data: Consumer.java number " + "\n\n");
		out.print("data: [*] Waiting for messages\n\n");
		out.flush();
	}
	
	protected void printData(String data) {
		if (data.equals("clear")) {
			out.print("event: clear\n");
			out.print("data: clears the client display\n\n");
		} else {
			out.print("event: jsonobject\n");
			out.print("data: " + data + "\n\n");
		}
		out.flush();
	}
	
	protected void close() {
		out.close();
	}
	

}
