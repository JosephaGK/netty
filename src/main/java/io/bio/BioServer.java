package io.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BioServer {
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind( new InetSocketAddress("127.0.0.1",8888));
		while (true){
			//阻塞方法
			Socket accept = serverSocket.accept();
			new Thread(()->{
				handle(accept);
			}).start();

		}

	}

	private static void handle(Socket accept) {
		try {
			byte[] bytes = new byte[1024];
			int len = accept.getInputStream().read(bytes);
			System.out.println(new String(bytes, 0, len));

			accept.getOutputStream().write(bytes,0,len);
			accept.getOutputStream().flush();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

}
