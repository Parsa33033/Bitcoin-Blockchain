package blockchain.controller;

public class TransactionInput {
	//field
	private String transactionOutputId;
	private TransactionOutput transactionOutput;
	//methods
	public TransactionInput(String id) {
		this.setTransactionOutputId(id);
	}
	public String getTransactionOutputId() {
		return transactionOutputId;
	}
	public void setTransactionOutputId(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
	public TransactionOutput getTransactionOutput() {
		return transactionOutput;
	}
	public void setTransactionOutput(TransactionOutput transactionOutput) {
		this.transactionOutput = transactionOutput;
	}
	
	
}
