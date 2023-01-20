package poller;

import event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PollerImpl implements Poller {
	private static final Logger LOGGER = LoggerFactory.getLogger(PollerImpl.class);
	private final Selector selector;
	private final Map<Integer, EventHandler> events;

	public PollerImpl() throws IOException {
		this.selector = Selector.open();
		this.events = new HashMap<>();
	}

	public void registerChannel(SelectableChannel socketChannel, int selectionKey) throws ClosedChannelException {
		socketChannel.register(selector, selectionKey);
	}

	public void registerEvent(int selectionType, EventHandler eventHandler) {
		events.put(selectionType, eventHandler);
	}

	@Override
	public void poll() {
		while (true) {
			try {
				selector.select();
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				iterator.forEachRemaining(selectionKey -> {
					final EventHandler eventHandler = events.get(selectionKey.readyOps());
					eventHandler.execute(selector, selectionKey);
					iterator.remove();
				});
			} catch (IOException e) {
				LOGGER.info(e.getMessage());
			}
		}
	}
}
