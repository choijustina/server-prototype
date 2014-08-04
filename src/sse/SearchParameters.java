package sse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/redirect")
public class SearchParameters extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected static String name = null;
	protected static String businessKey = null;
	protected static String documentType = null;
	protected static String date = null;
	
	protected void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		name = request.getParameter("name");
		businessKey = request.getParameter("businessKey");
		documentType = request.getParameter("docType");
		date = request.getParameter("date");
		
        response.sendRedirect("index.html");
		
	}
	

}