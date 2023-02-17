package event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ReadEventHandler implements EventHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReadEventHandler.class);
	private static final int BUFFER_SIZE = 1024;

	@Override
	public void execute(Selector selector, SelectionKey selectionKey) {
		final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		LOGGER.info("read event handler");
		try {
			SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
			int bytesRead = socketChannel.read(buffer);

			LOGGER.info("read data from client: " + new String(buffer.array()).trim());
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
}