package old_solution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class MessageServerNIO {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageServerNIO.class);
	private static final int PORT = 4888;
	private static final String HOST = "127.0.0.1";
	private static final int BUFFER_SIZE = 1024;
	private static Selector selector;

	public static void main(String[] args) {
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
						acceptEvent(selectionKey);
					} else if (selectionKey.isReadable()) {
						readEvent(selectionKey);
					} else if (selectionKey.isWritable()) {
						writeEvent(selectionKey);
					}
					iterator.remove();
				});
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	private static void acceptEvent(SelectionKey selectionKey) {
		try {
			LOGGER.info("connection accepted ");
			final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();

			// Accept the connection and make it non-blocking
			SocketChannel socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);

			// Register interest in reading this channel
			socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	private static void readEvent(SelectionKey selectionKey) {
		LOGGER.info("read event");
		try {
			SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

			int bytesRead = socketChannel.read(buffer);

			LOGGER.info("read data from client " + new String(buffer.array()).trim());
			if (bytesRead == -1) {
				LOGGER.info("connection closed " + socketChannel.getRemoteAddress());
				socketChannel.close();
			}
			buffer.flip();
			socketChannel.register(selector, SelectionKey.OP_WRITE, buffer);

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	private static void writeEvent(SelectionKey selectionKey) {
		LOGGER.info("write event");
		try {
			final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

			final ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
			int bytesRead = socketChannel.read(buffer);

//			String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());
//
//			//Building response
//			String response = "Message from client: " + clientMessage + ", server time = " + System.currentTimeMillis();
//			//Writing  response to buffer
//			buffer.clear();
//			buffer.put(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
//			buffer.flip();
			socketChannel.write(buffer);

			if (bytesRead == -1) {
				LOGGER.info("connection closed " + socketChannel.getRemoteAddress());
				socketChannel.close();
			}
			socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			LOGGER.error("processWriteEvent error" + e.getMessage());
		}
	}
}