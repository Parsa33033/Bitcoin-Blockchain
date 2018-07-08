package blockchain.controller;

import java.util.ArrayList;
import java.util.List;

import blockchain.view.layoutController;

public class Block {
	//field
	private String previousHash;
	public String hash;
	private List<Transaction> transactions = new ArrayList<>();
	private String markleRoot;
	private int nonce = -1;
	private boolean genesis = false;
	private int capacityOfBlock = 1;
	//methods
	public Block() {
		this.previousHash = "";
		this.markleRoot = "";
	}
	
	public void setMarkleRoot(String markleRoot) {
		this.markleRoot = markleRoot;
	}
	
	public void setPreviousHash(String prevHash) {
		this.previousHash = prevHash;
	}
	
	public String getHash() {
		hash();
		return this.hash;
	}
	
	public void isGenesis() {
		this.genesis = true;
	}
	
	public String hash() {
		this.hash = BlockchainUtils.digest(markleRoot + previousHash +  nonce);
		return this.hash;
	}
	
	public String getPreviousHash() {
		return this.previousHash;
	}
	
	public String mine(int difficulty) {
		String firstZeros = new String(new char[difficulty]).replaceAll(".", "0");
		hash();
		while(!hash.substring(0, difficulty).equals(firstZeros)) {
			nonce++;
			hash();
		}
		return this.hash;
	}
	
	public boolean addTransaction(Transaction transaction) {
		if(transaction == null) {
			return false;
		}
		if(!genesis) {
			if(!transaction.validateTransaction()) {
				System.out.println("The Transaction Failed to Validate");
				return false;
			}
			this.transactions.add(transaction);
		}else {
			this.transactions.add(transaction);
		}
		System.out.println(this.transactions.size());
		if(this.transactions.size()>= this.capacityOfBlock) {
			System.out.println("added block to db");
			addBlockToDB();
		}
		return true;
	}
	
	public void addBlockToDB() {
		markleRoot = BlockchainUtils.markleRoot(transactions);
		layoutController.sqlite.insertIntoBlocks(this.markleRoot);
	}
}
