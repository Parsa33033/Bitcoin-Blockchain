package blockchain.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blockchain.view.layoutController;

public class Blockchain {
	//field
	private List<String> blockchain = new ArrayList<>();
	public static Map<String,TransactionOutput> UTXOs = new HashMap<>();
	private int difficulty;
	//methods
	public Blockchain(int difficulty) {
		this.difficulty = difficulty;
		this.blockchain = layoutController.sqlite.getBlockchain();
	}
	
	public void addBlock(String hash) {
		String firstZeros = new String(new char[difficulty]).replaceAll(".", "0");
		if(hash.substring(0,difficulty).equals(firstZeros)) {
//			blockchain.add(block.getHash());
			layoutController.sqlite.addBlockHash(hash);
		}
	}
	
	public List<String> getBlockchain(){
		return this.blockchain;
	}
	
	public Map<String, TransactionOutput> getUTXOs(){
		return this.UTXOs;
	}
	
	public void addUTXOs(String key, TransactionOutput output) {
		UTXOs.put(key, output);
	}
	
//	public boolean checkBlock(){
//		for(int i = 1 ; i<this.blockchain.size(); i++ ){
//			String currentBlock = blockchain.get(i);
//			String previousBlock = blockchain.get(i-1);
//			if(currentBlock.equals(currentBlock.hash())){
//				if(previousBlock.equals(currentBlock.getPreviousHash())){
//					return true;
//				}
//			}
//		}
//		return false;
//	}
}
