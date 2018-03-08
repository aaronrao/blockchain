package com.blockchain.p2p;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * p2p服务端
 * 
 * @author aaron
 *
 */
public class P2PServer {
	private List<WebSocket> sockets;
	private P2PService p2pService;

	public P2PServer(P2PService p2pService) {
		this.p2pService = p2pService;
		this.sockets = new ArrayList<WebSocket>();
	}

	public void initP2PServer(int port) {
		final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {
			public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
				p2pService.write(webSocket, p2pService.queryLatestBlockMsg());
				sockets.add(webSocket);
			}

			public void onClose(WebSocket webSocket, int i, String s, boolean b) {
				System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
				sockets.remove(webSocket);
			}

			public void onMessage(WebSocket webSocket, String msg) {
				p2pService.handleMessage(webSocket, msg, sockets);
			}

			public void onError(WebSocket webSocket, Exception e) {
				System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
				sockets.remove(webSocket);
			}

			public void onStart() {

			}
		};
		socketServer.start();
		System.out.println("listening websocket p2p port on: " + port);
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
