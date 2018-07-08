package blockchain.controller;

public class ObservableUTXOs {
	private String value;
	private String recipient;
	private String parent;
	
	public ObservableUTXOs(String value, String recipient, String parent) {
		this.value = value;
		this.recipient = recipient;
		this.parent = parent;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public String getRecipient() {
		return this.recipient;
	}
	
	public String getParent() {
		return this.parent;
	}
}
