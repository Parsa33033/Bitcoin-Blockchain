package blockchain.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteConnection {
	//field
	
	//methods
	
	public void insertKeys(String publicKey, String privateKey) {
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("select * from wallet");
			resultSet = preparedStatement.executeQuery();
			resultSet.next();
			if(resultSet.getRow()==0) {
				preparedStatement = con.prepareStatement("insert into wallet(publickey,privatekey,init) values(?,?,?)");
				preparedStatement.setString(1, publicKey);
				preparedStatement.setString(2, privateKey);
				preparedStatement.setString(3, "1");
				preparedStatement.execute();
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				resultSet.close();
				preparedStatement.cancel();
				con.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public String[] getKeys() {
		String[] keys = new String[3];
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("select * from wallet");
			resultSet = preparedStatement.executeQuery();
			resultSet.next();
			keys[0] = resultSet.getString(1);
			keys[1] = resultSet.getString(2);
			keys[2] = resultSet.getString(3);
			return keys;
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				resultSet.close();
				preparedStatement.cancel();
				con.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public boolean alreadyHaveKeys() {
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("select * from wallet");
			resultSet = preparedStatement.executeQuery();
			resultSet.next();
			if(resultSet.getRow()==0) {
				return false;
			}else {
				return true;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				resultSet.close();
				preparedStatement.cancel();
				con.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean getWalletInit() {
		String init = "";
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		if(alreadyHaveKeys()) {
			try {
				con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
				preparedStatement = con.prepareStatement("select init from wallet");
				resultSet = preparedStatement.executeQuery();
				resultSet.next();
				init = resultSet.getString(1);
				preparedStatement = con.prepareStatement("update wallet set init = ?");
				preparedStatement.setString(1, "0");
				preparedStatement.execute();
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				try {
					resultSet.close();
					preparedStatement.cancel();
					con.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		if(init.equals("1")) {
			return true;
		}else if(init.equals("0")) {
			return false;
		}
		return false;
	}
	
	
	public void insertTransactionOutput(String recipient, String value, String parent) {
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("insert into UTXOs(recipient, value, parent) values(?,?,?)");
			preparedStatement.setString(1, recipient);
			preparedStatement.setString(2, value);
			preparedStatement.setString(3, parent);
			preparedStatement.execute();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				resultSet.close();
				preparedStatement.cancel();
				con.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	

	public void  deleteTransactionOutput(String recipient, String value, String parent) {
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("delete from UTXOs where recipient = ? and value = ? and parent = ?");
			preparedStatement.setString(1, recipient);
			preparedStatement.setString(2, value);
			preparedStatement.setString(3, parent);
			preparedStatement.execute();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				resultSet.close();
				preparedStatement.cancel();
				con.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public Map<String, TransactionOutput> getUTXOs(){
		Map<String, TransactionOutput> utxos = new HashMap<String, TransactionOutput>();
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("select * from UTXOs");
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				TransactionOutput output = new TransactionOutput(
						BlockchainUtils.getKeyFromString(resultSet.getString(1))
						,Double.valueOf(resultSet.getString(2))
						,resultSet.getString(3));
				utxos.put(output.getId(), output);
			}
			return utxos;
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				resultSet.close();
				preparedStatement.cancel();
				con.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public List<ObservableUTXOs> getObservableUTXOs(){
		List<ObservableUTXOs> outxos = new ArrayList<ObservableUTXOs>();
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("select * from UTXOs");
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				outxos.add(new ObservableUTXOs(resultSet.getString(2), resultSet.getString(1), resultSet.getString(3)));
			}
			return outxos;
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				resultSet.close();
				preparedStatement.cancel();
				con.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	public void addBlockHash(String hash) {
		Connection con = null;
		PreparedStatement preparedStatement = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("insert into blockchain(blockhash) values(?)");
			preparedStatement.setString(1, hash);
			preparedStatement.execute();

		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				preparedStatement.cancel();
				con.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getLastBlockHash() {
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("select * from blockchain order by id desc limit 1");
			resultSet = preparedStatement.executeQuery();
			resultSet.next();
			if(resultSet.getRow() != 0 ) {
				return resultSet.getString(2);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				preparedStatement.cancel();
				con.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	
	public List<String> getBlockchain(){
		List<String> bc = new ArrayList<String>();
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("select * from blockchain");
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				bc.add(resultSet.getString(2));
				System.out.println(resultSet.getString(2));
			}
			return bc;
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				preparedStatement.cancel();
				con.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}	
	
	
	public List<ObservableBlockchain> getObservableBlockchain(){
		List<ObservableBlockchain> bc = new ArrayList<ObservableBlockchain>();
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("select * from blockchain");
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				bc.add(new ObservableBlockchain(String.valueOf(resultSet.getInt(1)), resultSet.getString(2)));
			}
			return bc;
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				preparedStatement.cancel();
				con.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}	
	
	
	public void insertIntoBlocks(String markleRoot) {
		Connection con = null;
		PreparedStatement preparedStatement = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("insert into blocks(markleroot,flag) values(?,?)");
			preparedStatement.setString(1,markleRoot);
			preparedStatement.setString(2, "");
			preparedStatement.execute();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				preparedStatement.cancel();
				con.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deleteBlocks(String blockId, String markleRoot) {
		Connection con = null;
		PreparedStatement preparedStatement = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("delete from blocks where id = ? and markleroot = ?");
			preparedStatement.setString(1,blockId);
			preparedStatement.setString(2,markleRoot);
			preparedStatement.execute();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				preparedStatement.cancel();
				con.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addMinedBlocks(String markleroot) {
		Connection con = null;
		PreparedStatement preparedStatement = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("insert into minedblocks(minedmarkleroot) values(?)");
			preparedStatement.setString(1,markleroot);
			preparedStatement.execute();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				preparedStatement.cancel();
				con.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<ObservableBlocks> getObservableBlocks(){
		List<ObservableBlocks> ob = new ArrayList<ObservableBlocks>();
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("select * from blocks");
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				ob.add(new ObservableBlocks(String.valueOf(resultSet.getInt(1)), resultSet.getString(2)));
			}
			return ob;
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				resultSet.close();
				preparedStatement.cancel();
				con.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return ob;
	}
	
	
	
	
	public List<ObservableMinedBlocks> getObservableMinedBlocks(){
		List<ObservableMinedBlocks> ob = new ArrayList<ObservableMinedBlocks>();
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:blockchain.sqlite");
			preparedStatement = con.prepareStatement("select * from minedblocks");
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				ob.add(new ObservableMinedBlocks(String.valueOf(resultSet.getInt(1)), resultSet.getString(2)));
			}
			return ob;
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				resultSet.close();
				preparedStatement.cancel();
				con.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return ob;
	}
}
