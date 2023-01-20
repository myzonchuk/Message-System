import event.AcceptEventHandler;
import event.ConnectEventHandler;
import event.ReadEventHandler;
import event.WriteEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poller.PollerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

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

	private void run() {
		LOGGER.info("starting message client");
		try {
			SocketChannel clientSocket = SocketChannel.open(new InetSocketAddress(HOST, PORT));
			clientSocket.configureBlocking(false);

			poller.registerChannel(clientSocket, SelectionKey.OP_CONNECT);

			poller.registerEvent(SelectionKey.OP_CONNECT, new ConnectEventHandler());
			poller.registerEvent(SelectionKey.OP_READ, new ReadEventHandler());
			poller.registerEvent(SelectionKey.OP_WRITE, new WriteEventHandler());

			poller.poll();

		} catch (IOException e) {
			LOGGER.info(e.getMessage());
		}
	}
}
