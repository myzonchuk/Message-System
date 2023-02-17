package old_solution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageClientNIO {
	private static final int PORT = 4888;
	private static final String HOST = "127.0.0.1";
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageClientNIO.class);

	public static void main(String[] args) throws IOException, InterruptedException {
		InetSocketAddress hostAddress = new InetSocketAddress(HOST, PORT);
		SocketChannel client = SocketChannel.open(hostAddress);

		LOGGER.info("client started");
		String threadName = Thread.currentThread().getName();

		// Send messages to server
		String[] messages = new String[]
				{threadName + ": test1", threadName + ": test2", threadName + ": test3"};

		for (int i = 0; i < messages.length; i++) {
			byte[] message = messages[i].getBytes();
			ByteBuffer buffer = ByteBuffer.wrap(message);
			client.write(buffer);
			LOGGER.info(messages[i]);
			buffer.clear();
			Thread.sleep(2000);
		}
		client.close();
	}
}