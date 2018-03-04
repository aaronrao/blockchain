package com.blockchain.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.blockchain.security.CryptoUtil;
import com.blockchain.security.RSACoder;

/**
 * 交易
 * @author aaron
 *
 */
public class Transaction {
	
	private String id;   
    private TransactionInput txIn;
    private TransactionOutput txOut;
    
	public Transaction(String id, TransactionInput txIn, TransactionOutput txOut) {
		super();
		this.id = id;
		this.txIn = txIn;
		this.txOut = txOut;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public TransactionInput getTxIn() {
		return txIn;
	}

	public void setTxIn(TransactionInput txIn) {
		this.txIn = txIn;
	}

	public TransactionOutput getTxOut() {
		return txOut;
	}

	public void setTxOut(TransactionOutput txOut) {
		this.txOut = txOut;
	}

	public boolean isCoinbase(){
		return getTxIn().getTxId().equals("0") && getTxIn().getValue() == -1;
	}

	// Hash returns the hash of the Transaction
	public String hash() {
		return CryptoUtil.SHA256(JSON.toJSONString(this));
	}

	// Sign signs each input of a Transaction
	public void sign(String privateKey, Transaction prevTx){
		if (isCoinbase()) {
			return;
		}

		if (!prevTx.getId().equals(getTxIn().getTxId())) {
			System.err.println("ERROR: Previous transaction is not correct");
		}
		
		Transaction txClone = cloneTx();
		txClone.getTxIn().setSignature(null);
		txClone.getTxIn().setPublicKey(prevTx.getTxOut().getPublicKeyHash());
		String sign = "";
		try {
			sign = RSACoder.sign(txClone.getId().getBytes(), privateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		txIn.setSignature(sign);
	}

	// TrimmedCopy creates a trimmed copy of Transaction to be used in signing
	public Transaction cloneTx()  {
		TransactionInput transactionInput = new TransactionInput(txIn.getTxId(), txIn.getValue(), null, null);
		TransactionOutput transactionOutput = new TransactionOutput(txOut.getValue(), txOut.getPublicKeyHash());
		return new Transaction(id, transactionInput, transactionOutput);
	}

	// Verify verifies signatures of Transaction inputs
	public boolean verify(Transaction prevTx){
		if (isCoinbase()) {
			return true;
		}

		if (!prevTx.getId().equals(txIn.getTxId())) {
			System.err.println("ERROR: Previous transaction is not correct");
		}
		
		Transaction txClone = cloneTx();
		txClone.getTxIn().setSignature(null);
		txClone.getTxIn().setPublicKey(prevTx.getTxOut().getPublicKeyHash());
		
		boolean result = false;
		try {
			result = RSACoder.verify(txClone.getId().getBytes(), txIn.getPublicKey(), txIn.getSignature());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
