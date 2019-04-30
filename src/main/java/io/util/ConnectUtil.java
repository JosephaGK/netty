package io.util;

import java.io.IOException;
import java.nio.channels.Channel;

public class ConnectUtil {
	public static void closeChannel(Channel channel){
		if(channel!=null){
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
