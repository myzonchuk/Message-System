import event.AcceptEventHandler;
import event.ReadEventHandler;
import event.WriteEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poller.PollerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class ServerSide {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerSide.class);
	private static final int PORT = 4888;
	private static final String HOST = "127.0.0.1";
	private final PollerImpl poller = new PollerImpl();

	public ServerSide() throws IOException {
	}

	public static void main(String[] args) throws Exception {
		ServerSide serverSide = new ServerSide();
		serverSide.run();
	}

	public void run() {
		LOGGER.info("starting message server");
		try {
			LOGGER.info(String.format("trying to accept connections on %s:%d ", HOST, PORT));
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(HOST, PORT));
			serverSocketChannel.configureBlocking(false);
			poller.registerChannel(serverSocketChannel, SelectionKey.OP_ACCEPT);

			poller.registerEvent(SelectionKey.OP_ACCEPT, new AcceptEventHandler());
			poller.registerEvent(SelectionKey.OP_READ, new ReadEventHandler());
			poller.registerEvent(SelectionKey.OP_WRITE, new WriteEventHandler());

			poller.poll();
		} catch (IOException e) {
			LOGGER.info(e.getMessage());
		}
	}
}
