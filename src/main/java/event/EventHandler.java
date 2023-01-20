package event;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public interface EventHandler {
	void execute(Selector selector, SelectionKey selectionKey);
}
