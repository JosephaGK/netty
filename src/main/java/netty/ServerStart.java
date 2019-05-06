package netty;

public class ServerStart {
	public static void main(String[] args) {
		new NettyServer(8888).serverStart();
	}
}
