import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class MessageServerNIO {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageServerNIO.class);
	private static int PORT = 4887;
	private static String HOST = "127.0.0.1";
	private static final int BUFFER_SIZE = 1024;
	private static Selector selector;

	public static void main(String[] args) {
		LOGGER.info("Starting MessageServerNIO...");
		try {
			LOGGER.info(String.format("Trying to accept connections on %s:%d...", HOST, PORT));
			selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(HOST, PORT));

			serverSocketChannel.configureBlocking(false);
			int ops = serverSocketChannel.validOps();
			serverSocketChannel.register(selector, ops, null);
			while (true) {
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> i = selectedKeys.iterator();

				while (i.hasNext()) {
					SelectionKey key = i.next();

					if (key.isAcceptable()) {
						processAcceptEvent(serverSocketChannel);
					} else if (key.isReadable()) {
						processReadEvent(key);
					}

					i.remove();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processAcceptEvent(ServerSocketChannel serverSocketChannel) throws IOException {

		LOGGER.info("Connection Accepted...");

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register interest in reading this channel
		socketChannel.register(selector, SelectionKey.OP_READ);
	}

	private static void processReadEvent(SelectionKey key)
			throws IOException {
		// create a ServerSocketChannel to read the request
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Set up out 1k buffer to read data into
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		int bytesRead = socketChannel.read(buffer);

		//Reading client message from channel
		String data = new String(buffer.array()).trim();
		LOGGER.info(" Read data from client : " + data);

		buffer.flip();
		String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());

		//Building response
		String response = " Message from client: " + clientMessage + ", server time = " + System.currentTimeMillis();

		//Writing  response to buffer
		buffer.clear();
		buffer.put(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
		buffer.flip();
		socketChannel.write(buffer);
		if (bytesRead == -1) {
			LOGGER.info("Connection closed " + socketChannel.getRemoteAddress());
			socketChannel.close();
		}
		// Detecting end of message
		if (bytesRead > 0 && buffer.get(buffer.position() - 1) == '\n') {
			socketChannel.register(selector, SelectionKey.OP_WRITE);
			LOGGER.info("Detecting end of message ");
		}
	}
}
