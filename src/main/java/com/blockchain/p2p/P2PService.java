package com.blockchain.p2p;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.java_websocket.WebSocket;

import com.alibaba.fastjson.JSON;
import com.blockchain.block.BlockService;
import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.model.Wallet;

/**
 * p2p公用服务类
 * 
 * @author aaron
 *
 */
public class P2PService {
	private BlockService blockService;
	public final static int QUERY_LATEST = 0;
	public final static int QUERY_ALL = 1;
	public final static int RESPONSE_BLOCKCHAIN = 2;
	public final static int RESPONSE_WALLET = 3;
	public final static int RESPONSE_Transaction = 4;

	public P2PService(BlockService blockService) {
		this.blockService = blockService;
	}

	public void handleMessage(WebSocket webSocket, String s, List<WebSocket> sockets) {
		try {
			Message message = JSON.parseObject(s, Message.class);
			System.out.println("接收到" + webSocket.getRemoteSocketAddress().getPort() + "的p2p消息"
			        + JSON.toJSONString(message));
			switch (message.getType()) {
			case QUERY_LATEST:
				write(webSocket, responseLatestMsg());
				break;
			case QUERY_ALL:
				write(webSocket, responseChainMsg());
				break;
			case RESPONSE_BLOCKCHAIN:
				handleBlockChainResponse(message.getData(), sockets);
				break;
			case RESPONSE_WALLET:
				handleWalletResponse(message.getData());
				break;
			case RESPONSE_Transaction:
				handleTransactionResponse(message.getData());
				break;
			}
		} catch (Exception e) {
			System.out.println("处理p2p消息错误:" + e.getMessage());
		}
	}

	public void handleBlockChainResponse(String message, List<WebSocket> sockets) {
		List<Block> receiveBlocks = JSON.parseArray(message, Block.class);
		Collections.sort(receiveBlocks, new Comparator<Block>() {
			public int compare(Block o1, Block o2) {
				return o1.getIndex() - o1.getIndex();
			}
		});

		Block latestBlockReceived = receiveBlocks.get(receiveBlocks.size() - 1);
		Block latestBlock = blockService.getLatestBlock();
		if (latestBlockReceived.getIndex() > latestBlock.getIndex()) {
			if (latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
				System.out.println("将新接收到的区块加入到本地的区块链");
				if (blockService.addBlock(latestBlockReceived)) {
					broatcast(responseLatestMsg(), sockets);
				}
			} else if (receiveBlocks.size() == 1) {
				System.out.println("查询所有通讯节点上的区块链");
				broatcast(queryAllMsg(), sockets);
			} else {
				// 用长链替换本地的短链
				blockService.replaceChain(receiveBlocks);
			}
		} else {
			System.out.println("接收到的区块链不比本地区块链长，不处理");
		}
	}

	public void handleWalletResponse(String message) {
		Wallet wallet = JSON.parseObject(message, Wallet.class);
		blockService.getOtherWalletMap().put(wallet.getAddress(), wallet);
	}

	public void handleTransactionResponse(String message) {
		Transaction tx = JSON.parseObject(message, Transaction.class);
		blockService.getAllTransactions().add(tx);
	}

	public void write(WebSocket ws, String message) {
		System.out.println("发送给" + ws.getRemoteSocketAddress().getPort() + "的p2p消息:" + message);
		ws.send(message);
	}

	public void broatcast(String message, List<WebSocket> sockets) {
		if (sockets.size() == 0) {
			return;
		}
		System.out.println("======广播消息开始：");
		for (WebSocket socket : sockets) {
			this.write(socket, message);
		}
		System.out.println("======广播消息结束");
	}

	public String queryAllMsg() {
		return JSON.toJSONString(new Message(QUERY_ALL));
	}

	public String queryChainLengthMsg() {
		return JSON.toJSONString(new Message(QUERY_LATEST));
	}

	public String responseChainMsg() {
		return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blockService.getBlockchain())));
	}

	public String responseLatestMsg() {
		Block[] blocks = { blockService.getLatestBlock() };
		return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blocks)));
	}

}
