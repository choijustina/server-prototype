/*
 * File: BasicConsumer.java
 * Date: August 21, 2014
 * Author: Justina Choi (choi.justina@gmail.com)
 * Notes: basic consumer
 */

package sse;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Consumer")
public class BasicConsumer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int RECONNECT_TIME = 3000;			// delay in milliseconds until auto-reconnect
	private static final String REDIRECT_FILE = "index.html";
	
	protected static RoutingConsumer rc = null;
	private static int assignConsumerID = 10;
	private static int connectionID = 0;
	
	protected static PrintWriter out;
	
	private static final boolean DEBUG = true;
	
	protected void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String str = request.getParameter("name");
		if (str==null)
			isEventStream(request, response);
		else {
			try {
				getSearchForm(request, response);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void getSearchForm(HttpServletRequest request, HttpServletResponse response) 
			throws IOException, InterruptedException, ServletException {
		String a = request.getParameter("name");
		String b = request.getParameter("msgType");
		String c = request.getParameter("docType");
		
		ConsumerObject consumer = new ConsumerObject(assignConsumerID, a, b, c);
		assignConsumerID +=10;
		
		if (RoutingConsumer.numberOfConsumers==0)
			RoutingConsumer.numberOfConsumers = 1;
		else
			RoutingConsumer.numberOfConsumers++;
		
		connectionID = RoutingConsumer.numberOfConsumers;
		RoutingConsumer.consumerMap.put(RoutingConsumer.numberOfConsumers, consumer);
		
		RequestDispatcher view = request.getRequestDispatcher(REDIRECT_FILE);
		view.forward(request, response);
	}
	
	private void isEventStream(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		
		out = response.getWriter();				// IOException may occur here
		out.print("retry: " + RECONNECT_TIME + "\n\n");
		
//		out.print("event: ConnectionID\n");
//		out.print("data: " + assignConsumerID + "\n\n");
//		assignConsumerID +=10;
		
		out.print("data: the connectionID is.... " + connectionID + "\n\n");
		
		if (rc==null) {
			rc = new RoutingConsumer();
			if(DEBUG) out.print("data: RoutingConsumer instantiated successfully\n\n");
		} else
			if(DEBUG) out.print("data: RoutingConsumer already instantiated\n\n");
		
		if (DEBUG) {
			out.print("data: DEBUG MODE IS ON\n\n");
			out.print("data: inside BasicConsumer.java file\n\n");
			out.print("data: numberOfConsumers counter: " + RoutingConsumer.numberOfConsumers + "\n\n");
			out.print("data: size of consumerMap: " + RoutingConsumer.consumerMap.size() + "\n\n");
			out.print("data: listing all of the consumers in mapping\n\n");
			
			for (int i = 1; i <= RoutingConsumer.numberOfConsumers; i++) {
				ConsumerObject thisConsumer = RoutingConsumer.consumerMap.get(i);
				String str = thisConsumer.printConsumer();
				out.print("data: " + str + "\n\n");
			}
		}
		
		out.print("data: [*] Waiting for messages\n\n");
		out.flush();
		
		while(true) {
			// wait to receive messages
			
		}
		
		/*
		int i = 1;
		while(true) {
			
			out.print("data: this is the new message " + i + "\n\n");
			out.flush();
			i++;
			
			try {
				Thread.currentThread().sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (i>100)
				break;
		}
		
		out.close();
		*/
	}
	
	// debugging purposes
	protected static void recvMsg(String str) {
		out.print("data: " + str + "\n\n");
		out.flush();
	}
	
	protected static void recvMsg(int idNumber, String str) {
		out.print("data: " + str + "\n\n");
		out.flush();
	}
	

}
