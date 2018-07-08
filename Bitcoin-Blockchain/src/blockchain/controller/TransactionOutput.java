package blockchain.controller;

import java.security.PublicKey;

public class TransactionOutput {
	//field
	private PublicKey recipient;
	private double value;
	private String id;
	private String parentOutputId;
	//methods
	public TransactionOutput(PublicKey recipient, double value, String parent) {
		this.recipient = recipient;
		this.value = value;
		this.parentOutputId = parent;
		this.setId(BlockchainUtils.digest(BlockchainUtils.getKeyString(recipient) + Double.toString(value) + parentOutputId ));
	}
	
	public boolean myTransactionOutput(PublicKey key) {
		if(this.recipient.hashCode() == key.hashCode()) return true;
		return false;
	}
	
	public double getValue() {
		return value;
	}
	
	public String getParentOutputId() {
		return this.parentOutputId;
	}
	
	public void addSq() {
		this.setId(BlockchainUtils.digest(BlockchainUtils.getKeyString(recipient) + Double.toString(value) + parentOutputId ));
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public PublicKey getRecipeint() {
		return this.recipient;
	}
}
