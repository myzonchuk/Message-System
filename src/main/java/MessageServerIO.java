import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageServerIO {
    private final static int PORT = 10000;

    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started, waiting for connection");
            Socket socket = serverSocket.accept();
            System.out.println("Accepted . " + socket.getInetAddress());
            try (
                    InputStream in = socket.getInputStream();
                    OutputStream out = socket.getOutputStream()) {
                byte[] buf = new byte[2048];
                int readBytes = in.read(buf);
                String line = new String(buf, 0, readBytes);
                System.out.printf("Client>%s", line);
                out.write(line.getBytes());
                out.flush();
            }

        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}