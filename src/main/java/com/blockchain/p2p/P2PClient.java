package com.blockchain.p2p;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * p2p客户端
 * 
 * @author aaron
 *
 */
public class P2PClient {
	private List<WebSocket> sockets;
	private P2PService p2pService;

	public P2PClient(P2PService p2pService) {
		this.p2pService = p2pService;
		this.sockets = new ArrayList<WebSocket>();
	}

	public void connectToPeer(String peer) {
		try {
			final WebSocketClient socketClient = new WebSocketClient(new URI(peer)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					p2pService.write(this, p2pService.queryChainLengthMsg());
					sockets.add(this);
				}

				@Override
				public void onMessage(String msg) {
					p2pService.handleMessage(this, msg, sockets);
				}

				@Override
				public void onClose(int i, String msg, boolean b) {
					System.out.println("connection failed");
					sockets.remove(this);
				}

				@Override
				public void onError(Exception e) {
					System.out.println("connection failed");
					sockets.remove(this);
				}
			};
			socketClient.connect();
		} catch (URISyntaxException e) {
			System.out.println("p2p connect is error:" + e.getMessage());
		}
	}

	public List<WebSocket> getSockets() {
		return sockets;
	}

	public void write(WebSocket ws, String message) {
		System.out.println("发送给" + ws.getRemoteSocketAddress().getPort() + "的p2p消息:" + message);
		ws.send(message);
	}

	public void broatcast(String message) {
		if (sockets.size() == 0) {
			return;
		}
		System.out.println("======广播消息开始：");
		for (WebSocket socket : sockets) {
			this.write(socket, message);
		}
		System.out.println("======广播消息结束");
	}
}
