package event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class WriteEventHandler implements EventHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(WriteEventHandler.class);

	@Override
	public void execute(Selector selector, SelectionKey selectionKey) {
		LOGGER.info("write event for server side");
		try {
			final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
			final ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
			int bytesRead = socketChannel.read(buffer);

			//Building response
			String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());
			String response = "\r\n" + "Read message from client: " + clientMessage + "\r\n";
			//Writing  response to buffer
			buffer.clear();
			buffer.put(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
			buffer.flip();

			socketChannel.write(buffer);
			buffer.clear();

			if (bytesRead == -1) {
				LOGGER.info("connection closed " + socketChannel.getRemoteAddress());
				socketChannel.close();
			}
			socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}
}
//PrepostHandler concurrecy pattern щоб не дублювалися writeHandler для client and server