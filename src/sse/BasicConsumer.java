package sse;

public class BasicConsumer extends Consumer {
	private String loanNumber = null;
	private String name = null;
	private String documentType = null;
	private String loanType = null;

	public BasicConsumer() {
		super(ConsumerType.BASIC);
		construct();
	}

	@Override
	protected void construct() {
		System.out.println("building basic consumer");
	}
	
	// SET
	protected void setLoanNumber(String str) {
		this.loanNumber = str;
	}
	protected void setName(String str) {
		this.name = str;
	}
	protected void setDocumentType(String str) {
		this.documentType = str;
	}
	protected void setLoanType(String str) {
		this.loanType = str;
	}
	
	//GET
	protected String getLoanNumber() {
		return this.loanNumber;
	}
	protected String getName() {
		return this.name;
	}
	protected String getDocumentType() {
		return this.documentType;
	}
	protected String getLoanType() {
		return this.loanType;
	}
	

}
