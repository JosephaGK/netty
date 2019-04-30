package io.nio;

import io.util.ConnectUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioSingleServer {
	public static void main(String[] args) throws IOException {
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.socket().bind(new InetSocketAddress("127.0.0.1",8888));
		//设置nio为非阻塞
		ssc.configureBlocking(false);

		System.out.println("server started, listening on:" + ssc.getLocalAddress());
		Selector selector = Selector.open();
		ssc.register(selector, SelectionKey.OP_ACCEPT);

		while (true){
			selector.select();
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> it = keys.iterator();
			while (it.hasNext()){
				SelectionKey key = it.next();
				it.remove();
				handle(key);
			}
		}
	}

	private static void handle(SelectionKey key) {
		if(key.isAcceptable()){
			ServerSocketChannel ssc = null;
			try {
				ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				sc.register(key.selector(),SelectionKey.OP_READ);
			}catch (IOException e){
				e.printStackTrace();
			}finally {
				ConnectUtil.closeChannel(ssc);
			}
		}else if(key.isReadable()){
			SocketChannel sc = null;
			try {
				sc = (SocketChannel)key.channel();
				ByteBuffer buffer = ByteBuffer.allocate(512);
				buffer.clear();
				int len = sc.read(buffer);

				if(len!=-1){
					System.out.println(new String(buffer.array(), 0, len));
				}

				ByteBuffer bufferToWrite = ByteBuffer.wrap("HelloClient".getBytes());
				sc.write(bufferToWrite);
			}catch (IOException e){
				e.printStackTrace();
			}finally {
				ConnectUtil.closeChannel(sc);
			}
		}
	}
}
