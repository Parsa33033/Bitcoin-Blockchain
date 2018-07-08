package blockchain.controller;

import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BlockchainUtils {
	//field
	
	//methods
	public static String digest(String str) {
		try{
			MessageDigest digest = MessageDigest.getInstance("sha-256");
			byte[] digested = digest.digest(str.getBytes());
			StringBuilder strBuilder = new StringBuilder();
			for(int i = 0 ; i<digested.length ; i++) {
				strBuilder.append(Integer.toHexString( 0xff & digested[i]));
			}
			return strBuilder.toString();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] sign(PrivateKey privateKey, String data) {
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initSign(privateKey);
			sig.update(data.getBytes());
			return sig.sign();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean verifySignature(PublicKey key, String data, byte[] signature) {
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initVerify(key);
			sig.update(data.getBytes());
			return sig.verify(signature);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static String markleRoot(List<Transaction> data) {
		if(data.size() <= 1) {
			return BlockchainUtils.digest(data.get(0).getTransactionId());
		}
		List<String> tempTree = new ArrayList<>();
		int size = data.size();
		for(Transaction i : data) {
			tempTree.add(i.getTransactionId());
		}
		List<String> tree = tempTree;
		while(size > 0) {
			tree = new ArrayList<String>();
			for(int i = 1; i<tempTree.size() ; i++) {
				tree.add(BlockchainUtils.digest(tempTree.get(i-1) + tempTree.get(i)));
			}
			tempTree = tree;
			size = tempTree.size();
		}
		return (tree.size()==1) ? tree.get(0) : "";
		
	}
	
	public static String getKeyString(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	public static PublicKey getKeyFromString(String keyStr) {

		try {
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(keyStr));
	        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
	        return publicKey;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static PrivateKey getPrivateKeyFromString(String keyStr) {

		try {
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyStr));
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        PrivateKey privateKey = kf.generatePrivate(keySpec);
	        return privateKey;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
