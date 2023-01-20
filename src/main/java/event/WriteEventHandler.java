package event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class WriteEventHandler implements EventHandler{
	private static final Logger LOGGER = LoggerFactory.getLogger(WriteEventHandler.class);

	@Override
	public void execute(Selector selector, SelectionKey selectionKey) {
		LOGGER.info("write event");
		try {
			final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
			final ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
			int bytesRead = socketChannel.read(buffer);

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
//			String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());
//			//Building response
//			String response = "Message from client: " + clientMessage + ", server time = " + System.currentTimeMillis();
//			//Writing  response to buffer
//			buffer.clear();
//			buffer.put(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
//			buffer.flip();
	}
}