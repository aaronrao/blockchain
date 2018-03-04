package com.blockchain.http;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.alibaba.fastjson.JSON;
import com.blockchain.block.BlockService;
import com.blockchain.model.TransactionParam;
import com.blockchain.p2p.P2PService;

/**
 * @author aaron
 *
 */
public class HTTPService {
    private BlockService blockService;
    private P2PService   p2pService;

    public HTTPService(BlockService blockService, P2PService p2pService) {
        this.blockService = blockService;
        this.p2pService = p2pService;
    }

    public void initHTTPServer(int port) {
        try {
            Server server = new Server(port);
            System.out.println("listening http port on: " + port);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            
            //查询区块链
            context.addServlet(new ServletHolder(new ChainServlet()), "/chain");
            //挖矿
            context.addServlet(new ServletHolder(new MineServlet()), "/mine");
            //创建钱包
            context.addServlet(new ServletHolder(new CreateWalletServlet()), "/wallet/create");
            //转账交易
            context.addServlet(new ServletHolder(new NewTransactionServlet()), "/transactions/new");
            //查询钱包余额
            context.addServlet(new ServletHolder(new GetWalletBalanceServlet()), "/wallet/balance/get");
            
            server.start();
            server.join();
        } catch (Exception e) {
            System.out.println("init http server is error:" + e.getMessage());
        }
    }

    private class ChainServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        	resp.setCharacterEncoding("UTF-8");
        	resp.getWriter().print(blockService.getFullChain());
        }
    }
    
    private class MineServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        	resp.setCharacterEncoding("UTF-8");
        	String address = req.getParameter("address");
        	resp.getWriter().print(blockService.mine(address));
        }
    }
    
    private class CreateWalletServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        	resp.setCharacterEncoding("UTF-8");
            String address = blockService.createWallet();
            resp.getWriter().print("创建钱包： " + address);
        }
    }
    
    private class NewTransactionServlet extends HttpServlet {
    	@Override
    	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    		resp.setCharacterEncoding("UTF-8");
    		TransactionParam txParam = JSON.parseObject(getReqBody(req), TransactionParam.class); 
    		int blockId = blockService.createTransaction(txParam.getSender(), txParam.getRecipient(), txParam.getAmount());
    		resp.getWriter().print("你的交易将被加入区块 " + blockId);
    	}
    }
    
    private class GetWalletBalanceServlet extends HttpServlet {
    	@Override
    	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    		resp.setCharacterEncoding("UTF-8");
    		String address = req.getParameter("address");
    		resp.getWriter().print(blockService.getWalletBalance(address));
    	}
    }

    private String getReqBody(HttpServletRequest req) throws IOException {
		BufferedReader br = req.getReader();
    	String str, body = "";
    	while((str = br.readLine()) != null){
    		body += str;
    	}
		return body;
	}
}

