/*
 * File: ConsumerObject.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: August 20, 2014
 * Purpose: stores the filter criteria for each search instance
 */

package sse;

public class ConsumerObject {
	private String name;
	private String msgtype;
	private String doctype;
	private String data;
	private BasicConsumer basicConsumer;
	
	public ConsumerObject (String strName, String msgType, String docType, BasicConsumer bc) {
		this.name = strName;
		this.msgtype = msgType;
		this.doctype = docType;
		this.basicConsumer = bc;
	}
	
	protected String getName() {
		return this.name;
	}
	
	protected String getMsgType() {
		return this.msgtype;
	}
	
	protected String getDocType() {
		return this.doctype;
	}
	
	protected String getData() {
		return this.data;
	}
	
	protected BasicConsumer getBasicConsumer() {
		return this.basicConsumer;
	}
	
	protected String printConsumer() {
		return "Consumer - Name: " + this.name + ", Message Type: " + this.msgtype
				+ ", Document Type: " + this.doctype + ", Data: " + this.data + "\n\n";
	}
}
