package io.nio;

import io.util.ConnectUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioPoolServer {
	ExecutorService pool = Executors.newFixedThreadPool(50);
	Selector selector;

	public static void main(String[] args) throws IOException {
		NioPoolServer server = new NioPoolServer();
		server.initServer(8888);
		server.listen();
	}

	private void initServer(int port) throws IOException {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("服务器启动成功");
	}

	private void listen() throws IOException {
		//轮询访问selector
		while (true) {
			selector.select();
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isAcceptable()) {
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					SocketChannel socketChannel = server.accept();
					socketChannel.configureBlocking(false);
					socketChannel.register(selector, SelectionKey.OP_READ);
				} else if (key.isReadable()) {
					key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
					pool.execute(new ThreadHandlerChannel(key));
				}
			}
		}

	}

	private class ThreadHandlerChannel extends Thread {
		private SelectionKey key;

		ThreadHandlerChannel(SelectionKey key) {
			this.key = key;
		}

		@Override
		public void run() {
			SocketChannel channel = (SocketChannel) key.channel();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				int size = 0;
				while ((size = channel.read(buffer)) > 0) {
					buffer.flip();
					baos.write(buffer.array(), 0, size);
					buffer.clear();
				}
				baos.close();
				byte[] content = baos.toByteArray();
				ByteBuffer writeBuf = ByteBuffer.allocate(content.length);
				writeBuf.put(content);
				writeBuf.flip();
				channel.write(writeBuf);
				if(size==-1){
					ConnectUtil.closeChannel(channel);
				}else{
					key.interestOps(key.interestOps()|SelectionKey.OP_READ);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
