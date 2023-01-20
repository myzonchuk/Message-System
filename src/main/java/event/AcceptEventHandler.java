package event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptEventHandler implements EventHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(AcceptEventHandler.class);

	@Override
	public void execute(Selector selector, SelectionKey selectionKey) {
		try {
			LOGGER.info("Connection accepted ");
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
}