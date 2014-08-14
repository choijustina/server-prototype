package sse;

public class Consumer {
	private String id;
	private String name;
	private String msgtype;
	private String doctype;
	private String data;

	public Consumer(String strName) {
		this.name = strName;
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

}
