package sse;

public class ConsumerObject {
	private String id;
	private String name;
	private String msgtype;
	private String doctype;
	private String data;
	
	public ConsumerObject (String strName, String msgType, String docType) {
		this.name = strName;
		this.msgtype = msgType;
		this.doctype = docType;
	}
	
	protected String getID() {
		return this.id;
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
	
	/*
	protected String printConsumer() {
		return "Consumer - ID: " + this.id + ", Name: " + this.name + ", Message Type: " + this.msgtype
				+ ", Document Type: " + this.doctype + ", Data: " + this.data + "\n\n";
	}*/
}
