package blockchain.controller;

public class ObservableMinedBlocks {
	private String minedBlockId;
	private String minedMarkleRoot;
	
	public ObservableMinedBlocks(String minedBlockId, String minedMarkleRoot) {
		this.minedBlockId = minedBlockId;
		this.minedMarkleRoot = minedMarkleRoot;
	}
	
	public String getMinedBlockId() {
		return this.minedBlockId;
	}
	
	public String getMinedMarkleRoot() {
		return this.minedMarkleRoot;
	}
}
