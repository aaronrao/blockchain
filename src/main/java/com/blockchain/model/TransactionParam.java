package com.blockchain.model;

/**
 * 交易参数
 * @author aaron
 *
 */
public class TransactionParam {
	
	private String sender;   
    private String recipient;
    private int Amount;
    
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public int getAmount() {
		return Amount;
	}
	public void setAmount(int amount) {
		Amount = amount;
	}
    
}
