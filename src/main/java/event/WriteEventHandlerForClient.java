package event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class WriteEventHandlerForClient implements EventHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(WriteEventHandlerForClient.class);
	private final static String MESSAGE = " TEST ";

	@Override
	public void execute(Selector selector, SelectionKey selectionKey) {
		LOGGER.info("write event");
		try {
			final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

			byte[] message = MESSAGE.getBytes();
			ByteBuffer buffer = ByteBuffer.wrap(message);
			socketChannel.write(buffer);
			buffer.clear();
			Thread.sleep(2000);

			socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (IOException | InterruptedException e) {
			LOGGER.error(e.getMessage());
		}
	}
}
