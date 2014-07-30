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
	
	protected static String loanNumber;
	protected static String name;
	protected static String documentType;
	protected static String loanType;
	
	protected void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		loanNumber = request.getParameter("loanNum");
        name = request.getParameter("name");
		documentType = request.getParameter("docType");
		loanType = request.getParameter("loanType");
		
        response.sendRedirect("index.html");
		
	}
	

}
