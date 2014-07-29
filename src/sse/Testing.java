package sse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/test")
public class Testing extends HttpServlet {
	
	private static final long serialVersionUID = 1L;	
	protected static final int RECONNECT_TIME = 10000;		// delay in milliseconds how long until auto-reconnect 

	protected static String loanNumber;
	protected static String documentType;

	protected void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		//response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
        
		loanNumber = request.getParameter("loanNum");
        documentType = request.getParameter("docType");

        response.sendRedirect("index.html");
        
        PrintWriter out = response.getWriter();
		out.print("retry: 3000\n");
		out.print("data: after redirect...\n");
		out.print("data: RoutingConsumer.java\n\n");
		out.print("data: Loan number..." + loanNumber + "; document type..." + documentType + "\n\n");
		out.print("data: [*] Waiting for messages\n\n");
		out.flush();
		
        
        /*
        out.print("<!DOCTYPE html\n");
        out.print("<html>\n");
        out.print("<body>\n");
        out.print("<h1>hello world</h1>");
        out.print("Loan Number: " + loanNumber + "; Doc type: " + documentType + "\n");
        out.print("</body>\n</html>");
		out.flush();
		*/
	}

}
