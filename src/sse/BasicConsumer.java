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

import org.json.simple.JSONObject;

@WebServlet("/BasicConsumer")
public class BasicConsumer extends HttpServlet {
	private static BasicConsumer instance = null;
	
	private static final long serialVersionUID = 1L;
	private static final int RECONNECT_TIME = 3000;			// delay in milliseconds until auto-reconnect
	private static final String REDIRECT_FILE = "index.html";
	
	protected static RoutingConsumer rc = null;
	protected static PrintWriter out;
	
	private static final boolean DEBUG = true;
	
	public BasicConsumer() { }
	
	public static BasicConsumer getInstance() {
		if (instance==null)
			instance = new BasicConsumer();
		return instance;
	}
	
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
		
		ConsumerObject consumerobj = new ConsumerObject(a, b, c, getInstance());
		
		if (RoutingConsumer.numberOfConsumers==0)
			RoutingConsumer.numberOfConsumers = 1;
		else
			RoutingConsumer.numberOfConsumers++;
		
		RoutingConsumer.consumerMap.put(RoutingConsumer.numberOfConsumers, consumerobj);
		
		RequestDispatcher view = request.getRequestDispatcher(REDIRECT_FILE);
		view.forward(request, response);
	}
	
	private void isEventStream(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		
		out = response.getWriter();				// IOException may occur here
		out.print("retry: " + RECONNECT_TIME + "\n\n");
		
		if (rc==null)
			rc = new RoutingConsumer();
		
		out.print("data: [*] Waiting for messages\n\n");
		out.flush();
		
		while(true) {
			// wait to receive messages
		}
	}
	
	protected void receiveMessage(String str) {
		out.print("data: " + str + "\n\n");
		out.flush();
	}
	
	protected void sendJSON(JSONObject jsonobj) {
		out.print("event: jsonobject\n");
		out.print("data: " + jsonobj + "\n\n");
		out.flush();
	}
}
