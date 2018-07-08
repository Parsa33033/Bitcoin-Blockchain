package blockchain.controller;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blockchain.view.layoutController;

public class Wallet {
	//field
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private byte[] tmpSign;
	//methods
	public Wallet() {
	}
	
	public void generateKeyPair() {
		try {
			if(layoutController.sqlite.alreadyHaveKeys()) {
				String[] keys = layoutController.sqlite.getKeys();
				this.publicKey = BlockchainUtils.getKeyFromString(keys[0]);
				this.privateKey = BlockchainUtils.getPrivateKeyFromString(keys[1]);
			}else {
				KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
				keyPairGen.initialize(2048);
				KeyPair keyPair = keyPairGen.generateKeyPair();
				this.privateKey = keyPair.getPrivate();
				this.publicKey = keyPair.getPublic();
				layoutController.sqlite.insertKeys(BlockchainUtils.getKeyString(publicKey), BlockchainUtils.getKeyString(privateKey));	
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generateKeyPairw2() {
		try {
				KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
				keyPairGen.initialize(2048);
				KeyPair keyPair = keyPairGen.generateKeyPair();
				this.privateKey = keyPair.getPrivate();
				this.publicKey = keyPair.getPublic();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public byte[] getTransactionSignature() {
		return tmpSign;
	}
	
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	public PublicKey getPublicKey() {
		return publicKey;
	}
	
	public void setPublicKey(PublicKey key) {
		this.publicKey = key;
	}
	
	public Transaction sendBitcoin(PublicKey recipient, double value) {
		//if balance is valid
		if(getBalance()<value) {
			System.out.println("There is not Enough bitcoin in account");
			return new Transaction();
		}
		
		List<TransactionInput> inputs = new ArrayList<>();
		
		//create transaction
		Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
		newTransaction.sign(privateKey);
		tmpSign = newTransaction.getSignature();
		
		return newTransaction;
	}
	
	public double getBalance() {
		double total = 0;
		for(Map.Entry<String, TransactionOutput> i : Blockchain.UTXOs.entrySet()) {
			if(i.getValue().myTransactionOutput(publicKey)) {
				total += i.getValue().getValue();
			}
		}
		return total;		
	}
}
