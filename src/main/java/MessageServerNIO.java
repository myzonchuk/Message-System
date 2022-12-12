import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class MessageServerNIO {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageServerNIO.class);
	private static final int PORT = 4888;
	private static final String HOST = "127.0.0.1";
	private static final int BUFFER_SIZE = 1024;

	public static void main(String[] args) {
		Selector selector;
		LOGGER.info("starting message server ");
		try {
			LOGGER.info(String.format("trying to accept connections on %s:%d ", HOST, PORT));
			selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(HOST, PORT));

			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

			while (true) {
				selector.select();
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				iterator.forEachRemaining(selectionKey -> {
					if (selectionKey.isAcceptable()) {
						processAcceptEvent(serverSocketChannel, selector);
					} else if (selectionKey.isReadable()) {
						processReadEvent(selectionKey, selector);
					}
					iterator.remove();
				});
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	private static void processAcceptEvent(ServerSocketChannel serverSocketChannel, Selector selector) {
		try {
			LOGGER.info("connection accepted ");

			// Accept the connection and make it non-blocking
			SocketChannel socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);

			// Register interest in reading this channel
			socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	private static void processReadEvent(SelectionKey selectionKey, Selector selector) {
		try {
			// create a ServerSocketChannel to read the request
			SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

			// Set up out 1k buffer to read data into
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			int bytesRead = socketChannel.read(buffer);

			//Reading client message from channel
			final var data = new String(buffer.array()).trim();
			LOGGER.info("read data from client " + data);

			buffer.flip();
			String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());

			//Building response
			String response = "Message from client: " + clientMessage + ", server time = " + System.currentTimeMillis();

			//Writing  response to buffer
			buffer.clear();
			buffer.put(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
			buffer.flip();
			socketChannel.write(buffer);

			if (bytesRead == -1) {
				LOGGER.info("connection closed " + socketChannel.getRemoteAddress());
				socketChannel.close();
			}
			// Detecting end of message
			if (bytesRead > 0 && buffer.get(buffer.position() - 1) == '\n') {
				socketChannel.register(selector, SelectionKey.OP_WRITE);
				LOGGER.info("detecting end of message ");
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}
}
