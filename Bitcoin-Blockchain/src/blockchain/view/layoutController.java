package blockchain.view;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONObject;

import blockchain.controller.Block;
import blockchain.controller.Blockchain;
import blockchain.controller.BlockchainUtils;
import blockchain.controller.ObservableBlockchain;
import blockchain.controller.ObservableBlocks;
import blockchain.controller.ObservableMinedBlocks;
import blockchain.controller.ObservableUTXOs;
import blockchain.controller.Receive;
import blockchain.controller.SQLiteConnection;
import blockchain.controller.Send;
import blockchain.controller.Transaction;
import blockchain.controller.TransactionInput;
import blockchain.controller.TransactionOutput;
import blockchain.controller.Wallet;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

public class layoutController {
	//field
	@FXML
	private TextArea recipientPK;
	@FXML
	private TextField valueTextField;
	@FXML
	private Button initFundButton;
	@FXML
	private Label balanceLabel;
	@FXML
	private TextArea myPublicKeyText;
	@FXML
	private TableView UTXOTable;
	@FXML
	private TableColumn valueCol;
	@FXML
	private TableColumn recipientCol;
	@FXML
	private TableColumn parentCol;
	@FXML
	private TableView blockchainTable;
	@FXML
	private TableColumn idCol;
	@FXML
	private TableColumn blockHashCol;
	@FXML
	private TableView blockTable;
	@FXML
	private TableColumn blockIdCol;
	@FXML
	private TableColumn markleRootCol;
	@FXML
	private TextField miningTextField;
	@FXML
	private TableView minedBlocksTable;
	@FXML
	private TableColumn minedBlockIdCol;
	@FXML
	private TableColumn minedMarkleRootCol;
	
	
	private Wallet myWallet = new Wallet();
	private Blockchain blockchain;
	private Wallet genW = new Wallet();
	private Transaction genesis;
	private JSONObject json1;
	private String recipientPKStr;
	private String valueStr;
	Wallet w2 = new Wallet();
	private Block block1;
	private ExecutorService es = Executors.newCachedThreadPool();
	public static SQLiteConnection sqlite = new SQLiteConnection();
	private int count = 0;
	private boolean gen = true;
	private List<String> blockHashs = new ArrayList<>();
	private final int difficulty = 2;
	//methods
	public layoutController() {
		blockchain = new Blockchain(difficulty);
	}
	
	@FXML
    public void initialize() {
		//initialize public and private keys for wallet
		genW.generateKeyPairw2();
		myWallet.generateKeyPair();
		w2.generateKeyPairw2();
		//get UTXOs from database and update the Blockchains UTXOs hash map
		Blockchain.UTXOs = layoutController.sqlite.getUTXOs();
		System.out.println("w2 : "+BlockchainUtils.getKeyString(w2.getPublicKey()));
		System.out.println("mywallet:"+BlockchainUtils.getKeyString(myWallet.getPublicKey()));
		myPublicKeyText.setText(BlockchainUtils.getKeyString(myWallet.getPublicKey()));
		myPublicKeyText.setWrapText(true);
		
		
		//public key broadcast
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					String publicKeyBroadcast = "";
					Receive rcv = new Receive();
				
					while(true) {
						try {
							Future<String> future1 = es.submit(rcv);
							publicKeyBroadcast = future1.get();
							if(publicKeyBroadcast.contains("publickeybroadcast")) {
								JSONObject json = new JSONObject(publicKeyBroadcast);
								System.out.println(json.getString("publickeybroadcast"));
							}
							
						}catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
			
			// add mined blocks to the blockchain
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String transactionStr = "";
						Receive rcv = new Receive();
						
						while(true) {
							Future<String> future1 = es.submit(rcv);
	
							transactionStr = future1.get();
							JSONObject json = new JSONObject(transactionStr);
							
							if(!transactionStr.equals("") && transactionStr.contains("miner") && json.getString("type").equals("mined block")
									&& BlockchainUtils.getKeyFromString(json.getString("miner")).hashCode() != myWallet.getPublicKey().hashCode()) {
								System.out.println("mined: "+ json);
								Block block = new Block();
								block.setMarkleRoot(json.getString("markleroot"));
								block.setPreviousHash(layoutController.sqlite.getLastBlockHash());
								String hashedBlockWithProofOfWork = block.mine(difficulty);
								//delete the block from database
								layoutController.sqlite.deleteBlocks(json.getString("blockid"), json.getString("markleroot"));
								layoutController.sqlite.addMinedBlocks(json.getString("markleroot"));
								blockchain.addBlock(hashedBlockWithProofOfWork);
								
							}
						}
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			
			//create a receiver thread for receiving transactions
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String transactionStr = "";
						Receive rcv = new Receive();
						
						//polling for receiving transactions
						while(true) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									balanceLabel.setText(String.valueOf(myWallet.getBalance()));
									getUTXOTableView();
									getBlockchainTableView();
									getBlockTableView();
									getMinedBlocksTableView();
								}
							});
						
							Future<String> future1 = es.submit(rcv);
							
							//get the transaction by string
							transactionStr = future1.get();
							JSONObject json = new JSONObject(transactionStr);
							if(!transactionStr.equals("") && !transactionStr.contains("type") && !transactionStr.contains("publickeybroadcast")) {

								
								//get sender and receiver public key from the transaction received by json
								PublicKey s = BlockchainUtils.getKeyFromString(json.getString("sender"));
								PublicKey k = BlockchainUtils.getKeyFromString(json.getString("receiver"));
								List<TransactionInput> inputs = new ArrayList<>();
								
								//if the sender public key are not myWallet and my genesis wallet(for giving an initial value to my account) public key then do the following
								if((s.hashCode() != myWallet.getPublicKey().hashCode()) && (s.hashCode() != genW.getPublicKey().hashCode())) {
									
									//create block with the previous hash gotten from the json transaction
									block1 = new Block();
									block1.setPreviousHash(json.getString("hash"));
									
									//create the transaction
									Transaction t = new Transaction(s, k, Double.valueOf(json.get("value").toString()), inputs);
									t.setSignature(getEncodedByteArr(json.getString("signature")));
									
									//add transaction to the block
									block1.addTransaction(t);
									
								}
								System.out.println("w2 balance is: " + w2.getBalance());
								System.out.println("myWallet balance is: " + myWallet.getBalance());
							}
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									balanceLabel.setText(String.valueOf(myWallet.getBalance()));
									getUTXOTableView();
									getBlockchainTableView();
									getBlockTableView();
									getMinedBlocksTableView();
								}
							});
						}	
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
    }
	
	
	@FXML
	public void sendFund() {
		try {
			recipientPKStr = recipientPK.getText();
			valueStr = valueTextField.getText();
			PublicKey recipientKey = BlockchainUtils.getKeyFromString(recipientPKStr);
			
			//send fund by creating a block first
			block1 = new Block();
			block1.setPreviousHash(layoutController.sqlite.getLastBlockHash());
			
			//send fund from my wallet
			block1.addTransaction(myWallet.sendBitcoin(recipientKey, Double.valueOf(valueStr)));
			byte[] walletTransactionSign = myWallet.getTransactionSignature();
		
			//send the transaction
			ExecutorService es1 = Executors.newCachedThreadPool();
			Send send1 = new Send(jsonTransaction(myWallet.getPublicKey() , BlockchainUtils.getKeyFromString(recipientPKStr) , Double.valueOf(valueStr) , null, walletTransactionSign,block1.getHash()));
			es1.execute(send1);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@FXML
	public void rcvFund() {
		
		//get the initial indicator that there is a genesis transaction left or not
		Boolean initValue = this.sqlite.getWalletInit();
		if(initValue) {
			
			//create genesis transaction if I haven't used it yet
			genesis = new Transaction(genW.getPublicKey()
					, myWallet.getPublicKey()
					, 100f
					, null);
			genesis.sign(genW.getPrivateKey());
			genesis.setTransactionId("0");
			genesis.addTransactionOutputs(new TransactionOutput(genesis.getReceiver(), genesis.getValue(), genesis.getTransactionId()));
			layoutController.sqlite.insertTransactionOutput(
					BlockchainUtils.getKeyString(genesis.getReceiver())
					, String.valueOf(genesis.getValue())
					, genesis.getTransactionId());
			blockchain.addUTXOs(genesis.getOutputs().get(0).getId(), genesis.getOutputs().get(0));
			
			//creating genesis block
			Block genBlock = new Block();
			genBlock.setPreviousHash(layoutController.sqlite.getLastBlockHash());
			
			genBlock.isGenesis();
			genBlock.addTransaction(genesis);
			
			//send the genesis transaction to all the nodes
			Send send = new Send(jsonTransaction(genW.getPublicKey() , myWallet.getPublicKey() , 100f , null, genesis.getSignature(),genBlock.getPreviousHash()));
			es.execute(send);
		
			//create first block for later use
			block1 = new Block();
			block1.setPreviousHash(layoutController.sqlite.getLastBlockHash());
			count++;
			gen = false;
		}else {
			this.initFundButton.setDisable(true);
		}
	}
	
	public byte[] getEncodedByteArr(String s) {
		return Base64.getDecoder().decode(s);
	}
	
	
	public String jsonTransaction(PublicKey sender, PublicKey receiver, double value, List<TransactionInput> inputs, byte[] signature, String hash) {
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("sender", BlockchainUtils.getKeyString(sender));
			jsonObj.put("receiver", BlockchainUtils.getKeyString(receiver));	
			jsonObj.put("value", String.valueOf(value));
			jsonObj.put("signature", Base64.getEncoder().encodeToString(signature));
			jsonObj.put("hash", hash);
			JSONObject jsonObjInputs = new JSONObject();
			if(inputs != null) {
				int count = 0;
				for(TransactionInput i : inputs) {
					JSONObject tmp = new JSONObject();
					tmp.put(i.getTransactionOutputId(), i.getTransactionOutput());
					jsonObjInputs.put(String.valueOf(count), tmp);
				}
			}
			jsonObj.put("inputs", jsonObjInputs);
			return jsonObj.toString();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	@FXML
	public void close() {
		System.exit(0);
	}
	
	public void getUTXOTableView() {
		
		List<ObservableUTXOs> to = layoutController.sqlite.getObservableUTXOs();
		
		ObservableList<ObservableUTXOs> ol = FXCollections.observableArrayList(to);
		valueCol.setCellValueFactory(new PropertyValueFactory<ObservableUTXOs, String>("value"));
		recipientCol.setCellValueFactory(new PropertyValueFactory<ObservableUTXOs, String>("recipient"));
		parentCol.setCellValueFactory(new PropertyValueFactory<ObservableUTXOs, String>("parent"));
		
		UTXOTable.setItems(ol);
	}
	
	public void getBlockchainTableView() {
		List<ObservableBlockchain> lbc = layoutController.sqlite.getObservableBlockchain();
		
		ObservableList<ObservableBlockchain> olbc = FXCollections.observableArrayList(lbc);
		idCol.setCellValueFactory(new PropertyValueFactory<ObservableBlockchain, String>("id"));
		blockHashCol.setCellValueFactory(new PropertyValueFactory<ObservableBlockchain, String>("blockHash"));
		
		blockchainTable.setItems(olbc);
	}
	
	public void getBlockTableView() {
		List<ObservableBlocks> lb = layoutController.sqlite.getObservableBlocks();
		
		ObservableList<ObservableBlocks> olb = FXCollections.observableArrayList(lb);
		blockIdCol.setCellValueFactory(new PropertyValueFactory<ObservableBlocks, String>("id"));
		markleRootCol.setCellValueFactory(new PropertyValueFactory<ObservableBlocks, String>("markleRoot"));
		
		blockTable.setItems(olb);
	}
	
	public void getMinedBlocksTableView() {
		
		List<ObservableMinedBlocks> lb = layoutController.sqlite.getObservableMinedBlocks();
		ObservableList<ObservableMinedBlocks> olb = FXCollections.observableArrayList(lb);
		minedBlockIdCol.setCellValueFactory(new PropertyValueFactory<ObservableMinedBlocks, String>("minedBlockId"));
		minedMarkleRootCol.setCellValueFactory(new PropertyValueFactory<ObservableMinedBlocks, String>("minedMarkleRoot"));
		
		minedBlocksTable.setItems(olb);
	}
	
	
	@FXML
	public void clickBlockForMine(MouseEvent event) {
		if(event.getClickCount() == 1) {
			ObservableBlocks s = (ObservableBlocks) blockTable.getSelectionModel().getSelectedItems().get(0);
			miningTextField.setText(s.getMarkleRoot());
		}
	}
	
	
	@FXML
	public void mineBlock() {
		ObservableBlocks ss = (ObservableBlocks) blockTable.getSelectionModel().getSelectedItems().get(0);
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Mine Block?");
		String s = "Do you want to continue by mining the block!!!";
		alert.setContentText(s);
		Optional<ButtonType> result = alert.showAndWait();
		if ((result.isPresent()) && (result.get() == ButtonType.OK)) {

			System.out.println(ss.getMarkleRoot());
			sendMindedBlock(ss.getId(), ss.getMarkleRoot());
			
			Wallet genW = new Wallet();
			genW.generateKeyPairw2();
			//create genesis transaction if I haven't used it yet
			
			Transaction genesis = new Transaction(genW.getPublicKey()
					, myWallet.getPublicKey()
					, 50f
					, null);
			genesis.sign(genW.getPrivateKey());
			genesis.setTransactionId("0");
			genesis.addTransactionOutputs(new TransactionOutput(genesis.getReceiver(), genesis.getValue(), genesis.getTransactionId()));
			layoutController.sqlite.insertTransactionOutput(
					BlockchainUtils.getKeyString(genesis.getReceiver())
					, String.valueOf(genesis.getValue())
					, genesis.getTransactionId());
			blockchain.addUTXOs(genesis.getOutputs().get(0).getId(), genesis.getOutputs().get(0));
			
			//creating genesis block
			Block genBlock = new Block();
			genBlock.setPreviousHash(layoutController.sqlite.getLastBlockHash());
			
			genBlock.isGenesis();
			genBlock.addTransaction(genesis);
			
			//send the genesis transaction to all the nodes
			Send send = new Send(jsonTransaction(genW.getPublicKey() , myWallet.getPublicKey() , 50f , null, genesis.getSignature(),genBlock.getPreviousHash()));
			es.execute(send);
		
		}
	}
	
	public void sendMindedBlock(String blockId, String markleRoot) {
		Block block = new Block();
		block.setMarkleRoot(markleRoot);
		block.setPreviousHash(layoutController.sqlite.getLastBlockHash());
		String hashedBlockWithProofOfWork = block.mine(difficulty);
		System.out.println("proofOfWork: "+hashedBlockWithProofOfWork);
		
		String json = jsonMinedBlock(this.myWallet.getPublicKey()
					,blockId
					,markleRoot
					,hashedBlockWithProofOfWork);
		
		Send send = new Send(json);
		es.execute(send);
		
		
		//delete the block from database
		layoutController.sqlite.deleteBlocks(blockId, markleRoot);
		layoutController.sqlite.addMinedBlocks(markleRoot);
		blockchain.addBlock(hashedBlockWithProofOfWork);
		
		
	}
	
	public String jsonMinedBlock(PublicKey miner,String blockId, String markleRoot, String hash) {
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("miner", BlockchainUtils.getKeyString(miner));
			jsonObj.put("blockid", blockId);	
			jsonObj.put("markleroot", markleRoot);
			jsonObj.put("hash", hash);
			jsonObj.put("type", "mined block");
			return jsonObj.toString();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	@FXML
	public void broadcastPublicKey() {
		
		try {
			JSONObject json = new JSONObject();
			json.put("publickeybroadcast", myPublicKeyText.getText());
			Send send = new Send(json.toString());
			es.execute(send);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
