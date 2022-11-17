import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageServerNIO {
    private static int PORT = 8889;
    private static final Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); // реєструємо селектор в channel і в любий момент можна спитати в selector чи щось прийшло і він зганяє звірити
        System.out.println("Server started at port " +PORT + ". Waiting for connection");
        while (true) {
            selector.select();// Блокується до отримання подій хоча б на одному з каналів
            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.isValid()) {
                    try {
                        if (selectionKey.isAcceptable()) {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(true); // блокуючий  або неблокуючий режим на запис
                            System.out.println("Connected " + socketChannel.getRemoteAddress());
                            sockets.put(socketChannel, ByteBuffer.allocate(1000));
                            socketChannel.register(selector, SelectionKey.OP_READ);
                        } else if (selectionKey.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                            ByteBuffer buffer = sockets.get(socketChannel);
                            int bytesRead = socketChannel.read(buffer);
                            System.out.println("Reading from " + socketChannel.getRemoteAddress());
                        } else if (selectionKey.isWritable()) {
                            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                            ByteBuffer buffer = sockets.get(socketChannel);
                            //Reading client message from channel
                            buffer.flip();
                            String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());
                            //Building response
                            String response = clientMessage.replace("\r\n", "") + ", server time = " + System.currentTimeMillis();

                            //Writing  response to buffer
                            buffer.clear();
                            buffer.put(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
                            buffer.flip();

                            int byteWritten = socketChannel.write(buffer);
                            System.out.println("Writing to " + socketChannel.getRemoteAddress() + ", bytes written " + byteWritten);
                            if (!buffer.hasRemaining()) {
                                buffer.compact();
                                socketChannel.register(selector, SelectionKey.OP_READ);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}