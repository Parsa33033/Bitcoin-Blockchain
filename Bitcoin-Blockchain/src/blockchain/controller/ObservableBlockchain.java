package blockchain.controller;

public class ObservableBlockchain {
	private String id;
	private String blockHash;
	
	public ObservableBlockchain(String id, String blockHash) {
		this.id = id;
		this.blockHash = blockHash;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getBlockHash() {
		return this.blockHash;
	}
}
