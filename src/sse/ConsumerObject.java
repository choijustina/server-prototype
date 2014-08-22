package sse;

public class ConsumerObject {
	private int consumerID;
//	private String eventID;
	private String name;
	private String msgtype;
	private String doctype;
	private String data;
	
	public ConsumerObject (int id, String strName, String msgType, String docType) {
		this.consumerID = id;
		this.name = strName;
		this.msgtype = msgType;
		this.doctype = docType;
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
	
//	protected void makeDataNull() {
//		this.data = null;
//	}
	
	protected String printConsumer() {
		return "Consumer - ID: " + this.consumerID + ", Name: " + this.name + ", Message Type: " + this.msgtype
				+ ", Document Type: " + this.doctype + ", Data: " + this.data + "\n\n";
	}
	
	protected void recvMsg(int num, String str) {
		BasicConsumer.out.print("data: " + str + "\n\n");
		BasicConsumer.out.flush();
	}
}
