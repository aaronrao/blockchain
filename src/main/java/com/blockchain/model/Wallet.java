package com.blockchain.model;

import java.util.Map;

import com.blockchain.security.CryptoUtil;
import com.blockchain.security.RSACoder;

/**
 * 钱包
 * @author aaron
 *
 */
public class Wallet {
	
	private String publicKey;  
    private String privateKey; 
    
	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public Wallet() {
		Map<String, Object> initKey;
		try {
			initKey = RSACoder.initKey();
			this.publicKey = RSACoder.getPublicKey(initKey);
			this.privateKey = RSACoder.getPrivateKey(initKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getAddress() {
		String publicKeyHash = hashPubKey(publicKey);
		return CryptoUtil.MD5(publicKeyHash);
	}
	
	public static String getAddress(String publicKey) {
		String publicKeyHash = hashPubKey(publicKey);
		return CryptoUtil.MD5(publicKeyHash);
	}

	// HashPubKey hashes public key
	public String getHashPubKey() {
		return CryptoUtil.SHA256(publicKey);
	}
	
	// HashPubKey hashes public key
	public static String hashPubKey(String publicKey) {
		return CryptoUtil.SHA256(publicKey);
	}
	
}
