package blockchain.controller;

public class ObservableBlocks {
	private String id;
	private String markleRoot;
	
	public ObservableBlocks(String id, String markleRoot) {
		this.id = id;
		this.markleRoot = markleRoot;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getMarkleRoot() {
		return this.markleRoot;
	}
}
