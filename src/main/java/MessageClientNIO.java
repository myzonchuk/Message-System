import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.nio.ByteBuffer.allocate;

public class MessageClientNIO {
    private ByteBuffer buffer = allocate(16);
    private final String HOST = "localhost";
    private final int PORT = 8889;

    private void startClient() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
        socketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        System.out.println("MessageClientNIO started at port " + PORT + ". Waiting for connection");

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);
        // Це потік , що працює на запис з екрану
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String inputLine = scanner.nextLine();
                if ("q".equals(inputLine)) {
                    System.exit(0);
                }
                try {
                    queue.put(inputLine);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Оскільки ми не можемо напряму працювати з каналом, то тут ми просимо в канала його селектор для спілкування
                SelectionKey selectionKey = socketChannel.keyFor(selector);
                // і додаємо подію , яка працює на запис
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        }).start();
        // Це головний потік , для роботи
        while (true) {
            selector.select();
            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.isConnectable()) {
                    socketChannel.finishConnect(); // закінчити процес підключення конекшина
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                } else if (selectionKey.isReadable()) {
                    buffer.clear();
                    socketChannel.read(buffer);
                    System.out.println("Received = " + new String(buffer.array()));
                } else if (selectionKey.isWritable()) {
                    String line = queue.poll();
                    if (line != null) {
                        socketChannel.write(ByteBuffer.wrap(line.getBytes()));
                    }
                }
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new MessageClientNIO().startClient();
    }
}
