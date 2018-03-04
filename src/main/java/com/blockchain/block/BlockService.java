package com.blockchain.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.model.TransactionInput;
import com.blockchain.model.TransactionOutput;
import com.blockchain.model.Wallet;
import com.blockchain.security.CryptoUtil;

/**
 * 区块链
 * @author aaron
 *
 */
public class BlockService {
	
	/**
     * 区块链存储结构，链表
     */
    private List<Block> blockchain = new LinkedList<Block>();
    
    /**
     * 当前节点钱包集合
     */
    private Map<String, Wallet> walletMap = new HashMap<>();
	/**
	 * 转账交易
	 */
	private List<Transaction> currentTransactions = new ArrayList<>();

	public Map<String, Wallet> getWalletMap() {
		return walletMap;
	}

	public void setWalletMap(Map<String, Wallet> walletMap) {
		this.walletMap = walletMap;
	}

	public Block getLastBlock() {
		return blockchain.size() > 0 ? blockchain.get(blockchain.size() - 1) : null;
	}

	public BlockService() {
        //新建创始区块
        Block genesisBlock = createNewBlock(100, "1", "1"); 
        System.out.println("生成创始区块：" + JSON.toJSONString(genesisBlock));
    }

    private boolean isValidChain(List<Block> chain)
    {
        Block block = null;
        Block lastBlock = chain.get(0);
        int currentIndex = 1;
        while (currentIndex < chain.size())
        {
            block = chain.get(currentIndex);
            System.out.println("lastBlock:" + lastBlock);
            System.out.println("block:" + block);
            System.out.println("----------------------------");

            //Check that the hash of the block is correct
            if (!block.getPreviousHash().equals(lastBlock.getHash()))
                return false;

            //Check that the Proof of Work is correct
            if (!isValidHash(block.getHash()))
                return false;

            lastBlock = block;
            currentIndex++;
        }
        return true;
    }

    private Block createNewBlock(int nonce, String previousHash, String hash)
    {
    	Block block = new Block(blockchain.size() + 1, System.currentTimeMillis(), new ArrayList(currentTransactions), nonce, previousHash, hash);
    	currentTransactions.clear();
        blockchain.add(block);
        return block;
    }

    private boolean isValidHash(String hash){
    	return hash.startsWith("0000");
    }

    private String getHash(String previousHash, List<Transaction> currentTransactions, int nonce) {
        return CryptoUtil.SHA256(previousHash + JSON.toJSONString(currentTransactions) + nonce);
    }
    
    /**
     * 挖矿
     * @return
     */
    public String mine(String toAddress) {
    	//验证所有交易是否有效，非常重要的一步，可以防止双花
    	List<Transaction> invalidTxs = new ArrayList<>();
    	for (Transaction tx : currentTransactions) {
			if (!verifyTransaction(tx)) {
				invalidTxs.add(tx);
			}
		}
    	
    	currentTransactions.removeAll(invalidTxs);
    	//获取当前区块链里最后一个区块
    	Block lastBlock = getLastBlock();
    	
        String hash = "";
        boolean isValidNonce = false;
        long start = System.currentTimeMillis();
        int nonce = 0;
        System.out.println("开始挖矿");
        while (!isValidNonce){
        	//计算新区块hash值
        	hash = getHash(lastBlock.getHash(), currentTransactions, nonce);
        	//校验hash值
        	isValidNonce = isValidHash(hash);
        	if (isValidNonce) {
        		System.out.println("挖矿完成，正确的hash值：" + hash);
        		System.out.println("挖矿耗费时间：" + (System.currentTimeMillis()-start) + "ms");
        		break;
			}
        	System.out.println("错误的hash值：" + hash);
        	nonce++;
        }
    	
        //创建系统奖励的交易
        currentTransactions.add(newCoinbaseTx(toAddress, ""));
        //创建新的区块
        Block block = createNewBlock(nonce, lastBlock.getHash(), hash);

        JSONObject result = new JSONObject();
        result.put("message", "new block gen");
        result.put("index", block.getIndex());
        result.put("transactions", block.getTransactions());
        result.put("nonce", block.getNonce());
        result.put("hash", block.getHash());
        result.put("previousHash", block.getPreviousHash());
        return result.toJSONString();
    }

    public String getFullChain() {
    	JSONObject result = new JSONObject();
    	result.put("chain", blockchain);
        result.put("length", blockchain.size());
        return result.toJSONString();
    }
    
    public Transaction newCoinbaseTx(String toAddress, String data){
		if ("".equals(data)){
	        data = "Reward to " + toAddress;
	    }
		
		TransactionInput txIn = new TransactionInput("0", -1, null, null);
		Wallet wallet = walletMap.get(toAddress);
		TransactionOutput txOut = new TransactionOutput(10, wallet.getHashPubKey());
		return new Transaction(CryptoUtil.UUID(), txIn, txOut);
	}

    public int createTransaction(String fromAddress, String toAddress, int amount)
    {
    	Wallet senderWallet = walletMap.get(fromAddress);
    	Wallet recipientWallet = walletMap.get(toAddress);
    	List<Transaction> unspentTxs = findUnspentTransactions(senderWallet.getAddress());
    	Transaction prevTx = null;
    	for (Transaction transaction : unspentTxs) {
    		if (transaction.getTxOut().getValue() == amount) {
    			prevTx = transaction;
    			break;
			}
		}
    	if (prevTx == null) {
			return 0;
		}
    	TransactionInput txIn = new TransactionInput(prevTx.getId(), amount, null, senderWallet.getPublicKey());
    	TransactionOutput txOut = new TransactionOutput(amount, recipientWallet.getHashPubKey());
    	Transaction transaction = new Transaction(CryptoUtil.UUID(), txIn, txOut);
    	transaction.sign(senderWallet.getPrivateKey(), prevTx);
        currentTransactions.add(transaction);
        Block lastBlock = getLastBlock();
        return lastBlock != null ? lastBlock.getIndex() + 1 : 0;
    }
    
    private List<Transaction> findUnspentTransactions(String address) {
    	List<Transaction> unspentTxs = new ArrayList<Transaction>();
    	Set<String> spentTxs = new HashSet<String>();
    	for (Block block : blockchain) {
			List<Transaction> transactions = block.getTransactions();
			for (Transaction tx : transactions) {
				if (address.equals(Wallet.getAddress(tx.getTxIn().getPublicKey()))) {
					spentTxs.add(tx.getTxIn().getTxId());
				}
			}
		}
    	
    	for (Block block : blockchain) {
    		List<Transaction> transactions = block.getTransactions();
			for (Transaction tx : transactions) {
				if (address.equals(CryptoUtil.MD5(tx.getTxOut().getPublicKeyHash()))) {
					if (!spentTxs.contains(tx.getId())) {
						unspentTxs.add(tx);
					}
				}
			}
    	}

	  return unspentTxs;
	}
    
    private Transaction findTransaction(String id) {
    	for(Transaction tx : currentTransactions) {
    		if (id.equals(tx.getId())) {
				return tx;
			}
    	}
        return null;
    }
    
    private boolean verifyTransaction(Transaction tx) {
    	if (tx.isCoinbase()) {
    		return true;
    	}
    	Transaction prevTx = findTransaction(tx.getTxIn().getTxId());
        return tx.verify(prevTx);
    }
    
    public String createWallet() {
    	Wallet wallet = new Wallet();
    	String address = wallet.getAddress();
    	walletMap.put(address, wallet);
    	return address;
    }

	public int getWalletBalance(String address) {
    	List<Transaction> unspentTxs = findUnspentTransactions(address);
    	int balance = 0;
    	for (Transaction transaction : unspentTxs) {
    		balance += transaction.getTxOut().getValue();
		}
    	return balance;
    }
    
   
    
}
