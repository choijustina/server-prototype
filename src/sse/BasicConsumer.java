package sse;

public class BasicConsumer extends Consumer {
	private String name = null;
	private String businessKey = null;
	private String documentType = null;
	private String date = null;

	public BasicConsumer() {
		super(ConsumerType.BASIC);
		construct();
	}

	@Override
	protected void construct() {
		System.out.println("building basic consumer");
	}
	
	// SET
	protected void setName(String str) {
		this.name = str;
	}
	protected void setBusinessKey(String str) {
		this.businessKey = str;
	}
	protected void setDocumentType(String str) {
		this.documentType = str;
	}
	protected void setDate(String str) {
		this.date = str;
	}
	
	//GET
	protected String getName() {
		return this.name;
	}
	protected String getBusinessKey() {
		return this.businessKey;
	}
	protected String getDocumentType() {
		return this.documentType;
	}
	protected String getDate() {
		return this.date;
	}
	

}
