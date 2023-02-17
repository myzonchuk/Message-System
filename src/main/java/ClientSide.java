import event.ReadEventHandler;
import event.WriteEventHandlerForClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poller.PollerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ClientSide {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientSide.class);
	private static final int PORT = 4888;
	private static final String HOST = "127.0.0.1";
	private final PollerImpl poller = new PollerImpl();

	public ClientSide() throws IOException {
	}

	public static void main(String[] args) throws IOException {
		ClientSide client = new ClientSide();
		client.run();
	}

	//poller must run in standalone mode (executor)

	private void run() {
		Executor executor = Executors.newFixedThreadPool(10);
		executor.execute(() -> {
			LOGGER.info("starting message client");
			try {
				final WriteEventHandlerForClient writeEventHandlerForClient = new WriteEventHandlerForClient();
				final Selector selector = poller.getSelector();
				SocketChannel clientSocket = SocketChannel.open(new InetSocketAddress(HOST, PORT));
				clientSocket.configureBlocking(false);

				poller.registerChannel(clientSocket, SelectionKey.OP_CONNECT);

				poller.registerEvent(SelectionKey.OP_READ, new ReadEventHandler());
				poller.registerEvent(SelectionKey.OP_WRITE, new WriteEventHandlerForClient());

				SelectionKey selectionKey = clientSocket.keyFor(selector);

				writeEventHandlerForClient.execute(selector, selectionKey);

				poller.poll();

			} catch (IOException e) {
				LOGGER.info(e.getMessage());
			}
		});
	}
}
