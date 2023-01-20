package event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ConnectEventHandler implements EventHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectEventHandler.class);

	@Override
	public void execute(Selector selector, SelectionKey selectionKey) {
		try {
			LOGGER.info("Start client connection");
			final SocketChannel channel = (SocketChannel) selectionKey.channel();

			channel.configureBlocking(false);

			// Register interest in reading this channel
			channel.register(selector, SelectionKey.OP_WRITE);

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}
}
