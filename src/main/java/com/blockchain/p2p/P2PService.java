package com.blockchain.p2p;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.java_websocket.WebSocket;

import com.alibaba.fastjson.JSON;
import com.blockchain.block.BlockService;
import com.blockchain.model.Block;

/**
 * p2p公用服务类
 * @author aaron
 *
 */
public class P2PService {
    private BlockService    blockService;
    private final static int QUERY_LATEST        = 0;
    private final static int QUERY_ALL           = 1;
    private final static int RESPONSE_BLOCKCHAIN = 2;

    public P2PService(BlockService blockService) {
        this.blockService = blockService;
    }

    public void handleMessage(WebSocket webSocket, String s, List<WebSocket> sockets) {
        try {
            Message message = JSON.parseObject(s, Message.class);
            System.out.println("Received message" + JSON.toJSONString(message));
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
            }
        } catch (Exception e) {
            System.out.println("hanle message is error:" + e.getMessage());
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
                System.out.println("We can append the received block to our chain");
                blockService.addBlock(latestBlockReceived);
                broatcast(responseLatestMsg(), sockets);
            } else if (receiveBlocks.size() == 1) {
                System.out.println("We have to query the chain from our peer");
                broatcast(queryAllMsg(), sockets);
            } else {
                blockService.replaceChain(receiveBlocks);
            }
        } else {
            System.out.println("received blockchain is not longer than received blockchain. Do nothing");
        }
    }

    public void write(WebSocket ws, String message) {
        ws.send(message);
    }

    public void broatcast(String message, List<WebSocket> sockets) {
        for (WebSocket socket : sockets) {
            this.write(socket, message);
        }
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
        Block[] blocks = {blockService.getLatestBlock()};
        return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blocks)));
    }

}
