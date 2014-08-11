package sse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * File: SseServer.java
 * Project: SSE
 * To access: http://localhost:8080/SSE/index_old.html
 * Source: http://milestonenext.blogspot.com/2013/07/html5-server-sent-events-sample-with.html
 * Documentation: http://docs.oracle.com/javaee/6/api/javax/servlet/http/package-summary.html
 * Notes: Version 1 of Server-side Event Project; used with index_old.html and SSE_old.html
 */

@WebServlet("/SseServer")
public class SseServer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		
		PrintWriter out = response.getWriter();
		out.print("data: " + "success in Server.java file\n\n");
		out.flush();
		
		String filename = "longfile_10.txt";
		URL url = new URL("http://localhost:8080/SSE/" + filename);
		
		out.print("data: " + "searching in " + filename + "\n\n");
		out.flush();
		
		Scanner s = new Scanner(url.openStream());
		int id = 0;
		String event = "";
		String msg = "";
		
		while (s.hasNext()) {
			s.next();
			id = s.nextInt();
			s.next();
			event = s.next();
			s.nextLine();
			s.next();
			msg = s.nextLine();
			
			Date date = new Date();
			out.print("event: " + event + "\n");
			out.print("data: " + id + "  " + event + " - " + msg + " accessed at " + date + "\n\n");
			out.flush();
			
			try {
				Thread.currentThread().sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		out.close();
		s.close();
	}
}