package sse;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, UnknownHostException {
		// DB CONNECTION /////////////////////////////////////////////
		//response.sendRedirect("history.html");
		//connect to local db server & get db (JSONDB)
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("JSONDB");
		DBCollection collection;
		collection = db.getCollection("SSE");
		
		PrintWriter out = response.getWriter();	
		
		// PARAMETERS ////////////////////////////////////////////////
		String name = request.getParameter("name");
		String businessKey = request.getParameter("businessKey");
		String documentType = request.getParameter("documentType");
		String dString = request.getParameter("date");
		
		int id = (businessKey.length() == 0) ? -1 : Integer.parseInt(businessKey);
		//documentType = (documentType.length() > 0) ? documentType : null;

		//BasicDBObject nameQuery = new BasicDBObject("name", name);
		//BasicDBObject idQuery = new BasicDBObject("id", id);
		//BasicDBObject docQuery = new BasicDBObject("doctype", documentType);
		//BasicDBObject dateQuery = new BasicDBObject("_id", new BasicDBObject("$gt", new ObjectId(hexString)));
		
		//initialize query
		BasicDBObject query = new BasicDBObject();
		
		//create queryList (list of filter items entered in search)
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		if (name.length() > 0) queryList.add(new BasicDBObject("name", name));
		if (id >= 0) queryList.add(new BasicDBObject("id", id));
		if (documentType.length() > 0) queryList.add(new BasicDBObject("doctype", documentType)); 
		if (dString.length() > 0) {
			Date date = null;
			try {
				date = new SimpleDateFormat("MM/dd/yy").parse(dString);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			//parse as epoch date and convert to hex
			long epoch = date.getTime();
			String hexString = Long.toHexString(epoch/1000);
			hexString += "0000000000000000";
			queryList.add(new BasicDBObject("_id", new BasicDBObject("$gt", new ObjectId(hexString))));
		}
		
		query.put("$and", queryList);
		DBCursor cursor = collection.find(query);
		
		String htmlResults = "<html>";
		
		try {
			while (cursor.hasNext()) {
				htmlResults+= "<h4>"  + cursor.next() + "</h4>";
			}
		} finally {
			cursor.close();
		}
		
		htmlResults += "</html>";
		out.print(htmlResults);
		out.flush();
		
	}
}
