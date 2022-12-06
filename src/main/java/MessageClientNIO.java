import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageClientNIO {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageClientNIO.class);
    private static final int BUFFER_SIZE = 1024;

    private final String HOST = "127.0.0.1";
    private final int PORT = 4887;
    private static String[] messages =
            {"First message", "Second message", "Third message", "Fourth message", "*exit*"};

    public static void main(String[] args) {
        new MessageClientNIO().startClient();
    }

    private void startClient() {
        try {
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            LOGGER.info(String.format("Trying to connect to %s:%d...", HOST, PORT));

            for (String msg : messages) {
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                buffer.put(msg.getBytes());
                buffer.flip();
                int bytesWritten = socketChannel.write(buffer);
                LOGGER.info(String.format("Sending Message...: %s\nbytesWritten...: %d", msg, bytesWritten));
            }
            LOGGER.info("Closing Client connection...");
            socketChannel.close();
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
        }
    }
}
