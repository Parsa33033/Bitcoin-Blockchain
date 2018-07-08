package blockchain.controller;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import blockchain.view.layoutController;

public class Transaction {
	//field
	private String id;
	private PublicKey sender;
	private PublicKey receiver;
	private double value;
	private byte[] signature;
	private List<TransactionInput> inputs = new ArrayList<>();
	private List<TransactionOutput> outputs = new ArrayList<>();
	private int index = 0;
	//methods
	public Transaction() {
		
	}
	
	public Transaction(PublicKey sender, PublicKey receiver, double value, List<TransactionInput> inputs) {
		this.sender = sender;
		this.receiver = receiver;
		this.value = value;
		this.inputs = inputs;
	}
	
	public String getTransactionId() {
		return id;
	}
	
	public void setTransactionId(String id) {
		this.id = id;
	}
	
	public void addTransactionOutputs(TransactionOutput output) {
		this.outputs.add(output);
	}
	
	public List<TransactionOutput> getOutputs(){
		return this.outputs;
	}
	
	public PublicKey getReceiver() {
		return this.receiver;
	}
	
	public double getValue() {
		return this.value;
	}
	
	public void setSignature(byte[] s) {
		this.signature = s;
	}
	
	public void sign(PrivateKey privateKey) {
		String data = BlockchainUtils.getKeyString(sender) + BlockchainUtils.getKeyString(receiver) + String.valueOf(value);
		this.signature = BlockchainUtils.sign(privateKey, data);
	}
	
	public byte[] getSignature() {
		return this.signature;
	}
	
	public boolean verifySignature() {
		String data = BlockchainUtils.getKeyString(sender) + BlockchainUtils.getKeyString(receiver) + String.valueOf(value);
		return BlockchainUtils.verifySignature(sender , data, this.signature);
	}
	
	public String transactionHash() {
		return BlockchainUtils.digest(
				BlockchainUtils.getKeyString(sender)+
				BlockchainUtils.getKeyString(receiver)+
				String.valueOf(value)+
				String.valueOf(index)				
				);
	}
	
	public boolean validateTransaction() {
		//verify signature
		if(!verifySignature()) {
			System.out.println("Signature is not verified");
			return false;
		}
		
		//get the transaction inputs that are mine
		for(Map.Entry<String, TransactionOutput> i : Blockchain.UTXOs.entrySet()) {
			if(i == null) continue;
			if(i.getValue().myTransactionOutput(sender)) {
				inputs.add(new TransactionInput(i.getKey()));
				
			}
		}
	
		//get the transaction inputs' transaction output of my id that i extracted from UTXOs
		for(TransactionInput i : inputs) {
			i.setTransactionOutput(Blockchain.UTXOs.get(i.getTransactionOutputId()));
		}
		
		double leftover = getInputValue() - value;
		for(Map.Entry<String, TransactionOutput> i : Blockchain.UTXOs.entrySet()) {
			System.out.println(i.getKey()+ " ===> "+ i.getValue().getValue());
		}
		
		//make sure that there is no same key in the UTXOs
		for(int i = 0 ; i<Blockchain.UTXOs.size() ; i++) {
			id = transactionHash();
			for(Map.Entry<String, TransactionOutput> j : Blockchain.UTXOs.entrySet()) {
				if(j.getKey().equals((new TransactionOutput(receiver, value, id).getId()))) {
					index++;
					id = transactionHash();					
				}
			}
		}
		
		//create new Transaction outputs for sender and receiver
		id = transactionHash();
		index = 0;
		outputs.add(new TransactionOutput(receiver, value, id));
		outputs.add(new TransactionOutput(sender, leftover, id));
		
		//add the new transaction outputs for sender and receiver (leftovers and the real value) to UTXOs
		for(TransactionOutput i : outputs) {			
			Blockchain.UTXOs.put(i.getId(), i);
			layoutController.sqlite.insertTransactionOutput(
					BlockchainUtils.getKeyString(i.getRecipeint())
					,String.valueOf(i.getValue())
					, i.getParentOutputId());
		}
		
		//delete transaction inputs from UTXOs
		for(TransactionInput i : inputs) {
			if(i.getTransactionOutput() == null) continue;
			Blockchain.UTXOs.remove(i.getTransactionOutputId());
			layoutController.sqlite.deleteTransactionOutput(
					BlockchainUtils.getKeyString(i.getTransactionOutput().getRecipeint())
					,String.valueOf(i.getTransactionOutput().getValue())
					, i.getTransactionOutput().getParentOutputId());
		}
		return true;
	}
	
	public double getInputValue() {
		double total = 0;
		for(TransactionInput i : inputs) {
			if(i.getTransactionOutput() == null) continue;
			total += i.getTransactionOutput().getValue();
		}
		return total;
	}
	
}
