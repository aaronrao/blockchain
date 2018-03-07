package com.blockchain.block;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.security.CryptoUtil;

/**
 * 区块链测试
 */
public class BlockServiceTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testBlockMine() throws Exception {
		
		
		
	}
	
	
	
	
	
	

	@Test
	public void testBlockMineBack() throws Exception {
		List<Block> blockchain = new ArrayList<>();
		blockchain.add(new Block(1, System.currentTimeMillis(), new ArrayList<Transaction>(), 1, "1", "1"));
		System.out.println(JSON.toJSON(blockchain));
		
		for (int i = 0; i < 3; i++) {
			List<Transaction> txs = new ArrayList<>();
			//生成的新用户交易
			Transaction userTx1 = new Transaction();
			Transaction userTx2 = new Transaction();
			txs.add(userTx1);
			txs.add(userTx2);
			
			//系统奖励的BTC交易
			Transaction sysTx = new Transaction();
			txs.add(sysTx);
			
			Block latestBlock = blockchain.get(blockchain.size() - 1);
			int nonce = 1;
			String hash = "";
			while(true){
				hash = CryptoUtil.SHA256(latestBlock.getHash() + JSON.toJSONString(txs) + nonce);
				if (hash.startsWith("0")) {
					System.out.println("=========计算成功,正确的hash:" + hash);
					break;
	            }
				nonce ++;
				System.out.println("计算失败,错误的hash:" + hash);
			}
			
			Block newBlock = new Block(latestBlock.getIndex() + 1, System.currentTimeMillis(), txs, nonce, latestBlock.getHash(), hash);
			blockchain.add(newBlock);
		}
		
		System.out.println(JSON.toJSONString(blockchain));
		
		Assert.assertTrue(blockchain.size() == 4);

	}

}
