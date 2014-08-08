package sse;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	// FUNCTION THAT TAKES MONGO LOG QUERIES AND OUTPUTS THEM IN MESSAGE FORMAT /////////////////////////////////////////////
	public String messageGenerator(String msgtype, String name, String id, String date) {
		String output = null;

		if (msgtype.equals("request_generated"))
			//0 - request_generated: [DATE] Letter request for NAME (business key: ID) was generated.
			output = "<font color=\"#0080CF\">[" 
					+ date + "]</font> Letter request for <font color=\"#CF0061\">" 
					+ name + "</font> (business key: " 
					+ id + ") was <font color=\"#08A66C\">generated</font>; file opened.";
		
		else if (msgtype.equals("request_received"))
			//1 - request_received: [DATE] Letter request for NAME (business key: ID) was received.
			output = "<font color=\"#0080CF\">[" 
					+ date + "]</font> Letter request for <font color=\"#CF0061\">" 
					+ name + "</font> (business key: " 
					+ id + ") was <font color=\"#08A66C\">received</font>.";
		
		else if (msgtype.equals("request_validated"))
			//2 - request_validated: [DATE] Letter request file for NAME (business key: ID) was validated.
			output = "<font color=\"#0080CF\">[" 
					+ date + "]</font> Letter request file for <font color=\"#CF0061\">" 
					+ name + "</font> business key: " 
					+ id + ") was <font color=\"#08A66C\">validated</font>.";
		
		else if (msgtype.equals("letter_generated"))
			//3 - letter_generated: [DATE] Letter for NAME (business key: ID) was generated.
			output = "<font color=\"#0080CF\">[" 
					+ date + "]</font> Letter for <font color=\"#CF0061\">" 
					+ name + "</font> (business key: " 
					+ id + ") was <font color=\"#08A66C\">generated</font>.";
		
		else if (msgtype.equals("awaiting_approval"))
			//4 - awaiting_approval: [DATE] Letter for NAME (business key: ID) is awaiting quality assurance approval.
			output = "<font color=\"#0080CF\">[" 
					+ date + "]</font> Letter for <font color=\"#CF0061\">" 
					+ name + "</font> (business key: " 
					+ id + ") is <font color=\"#08A66C\">awaiting quality assurance approval</font>.";

		else if (msgtype.equals("letter_approved"))
			//5 - letter_approved: [DATE] Letter for NAME (business key: ID) has been approved.
			output = "<font color=\"#0080CF\">[" 
					+ date + "]</font> Letter for <font color=\"#CF0061\">" 
					+ name + "</font> (business key: " 
					+ id + ") has been <font color=\"#08A66C\">approved</font>.";
		
		else if (msgtype.equals("letter_dispatched"))
			//6 - letter_dispatched: [DATE] Letter for NAME (business key: ID) has been dispatched and is out for delivery.
			output = "<font color=\"#0080CF\">[" 
					+ date + "]</font> Letter for <font color=\"#CF0061\">" 
					+ name + "</font> (business key: " 
					+ id + ") has been <font color=\"#08A66C\">dispatched and is out for delivery</font>.";
		
		else if (msgtype.equals("letter_delivered"))
			//7 - letter_delivered: [DATE] Letter for NAME (business key: ID) has been delivered.
			output = "<font color=\"#0080CF\">[" 
					+ date + "]</font> Letter for <font color=\"#CF0061\">" 
					+ name + "</font> (business key: " 
					+ id + ") has been <font color=\"#08A66C\">delivered</font>.";
		
		else if (msgtype.equals("delivery_confirmed"))
			//8 - delivery_confirmed: [DATE] Letter delivery for NAME (business key: ID) has been confirmed.
			output = "<font color=\"#0080CF\">[" 
					+ date + "]</font> Letter for <font color=\"#CF0061\">" 
					+ name + "</font> (business key: " 
					+ id + ") has been <font color=\"#08A66C\">confirmed</font>.";

		else if (msgtype.equals("file_closed"))
			//9 - file_closed: [DATE] Letter process for NAME (business key: ID) is complete; file closed.
			output = "<font color=\"#0080CF\">[" 
					+ date + "]</font> Letter for <font color=\"#CF0061\">" 
					+ name + "</font> (business key: " 
					+ id + ") is <font color=\"#08A66C\">complete</font>; file closed.";
		
		return output;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, UnknownHostException {
		// DB CONNECTION /////////////////////////////////////////////
		//connect to local db server & get db
		// database: JSON
		// collection: SSE
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("JSONDB");
		DBCollection collection;
		collection = db.getCollection("SSE");
		
		PrintWriter out = response.getWriter();	
		
		// PARAMETERS FROM SEARCH FORM ////////////////////////////////////////////////
		String name = request.getParameter("name");
		String businessKey = request.getParameter("businessKey"); // must be changed to int format for queries
		int bKey = (businessKey.length() == 0) ? -1 : Integer.parseInt(businessKey); 
		String documentType = request.getParameter("documentType");
		String dString = request.getParameter("date");
		
		//initialize final query var
		BasicDBObject query = new BasicDBObject();
		
		//create queryList (list of filter items entered in search)
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		if (name.length() > 0) queryList.add(new BasicDBObject("name", name));
		if (bKey >= 0) queryList.add(new BasicDBObject("id", bKey));
		if (documentType.length() > 0) queryList.add(new BasicDBObject("doctype", documentType)); 
		if (dString.length() > 0) { // must be changed from mm/dd/yy format to epoch date format, and then from dec -> hex
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
			while (cursor.hasNext()){
				DBObject temp = cursor.next();
				//getting different fields from each search obj.
				String searchName = temp.get("name").toString();
				String searchDate = temp.get("startDate").toString();
				String searchMsg = temp.get("msgtype").toString();
				String searchId = temp.get("id").toString();
				//put together into a final string (sentence)
				String result =  messageGenerator(searchMsg, searchName, searchId, searchDate);
				htmlResults += "<h4>" + result + "</h4>";

			}

		} finally {
			cursor.close();
		}
		
/* IF YOU WANT TO PRINT RESULTS STRAIGHT UP AS JSON	
		try {
			while (cursor.hasNext()) {
				htmlResults+= "<h4>"  + cursor.next() + "</h4>";
			}
		} finally {
			cursor.close();
		}
*/		
		htmlResults += "</html>";
		out.print(htmlResults);
		out.flush();
	}
}
