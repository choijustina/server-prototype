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
 * @author swang
 * @version 10:15am 6/18/14
 * Source: http://milestonenext.blogspot.com/2013/07/html5-server-sent-events-sample-with.html
 * Documentation: http://docs.oracle.com/javaee/6/api/javax/servlet/http/package-summary.html
 * 
 */
@WebServlet("/SseServer")
public class SseServer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");

		PrintWriter out = response.getWriter();

		while (true) {
			Date date = new Date();
			out.print("data: current time is " + date + "\n\n");
			out.flush();
			
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}