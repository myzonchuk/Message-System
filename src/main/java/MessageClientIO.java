import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MessageClientIO {
    private final static String HOST = "localhost";
    private final static int PORT = 10000;

    public static void main(String[] args) throws IOException {

        Socket socket = null;

        try {
            socket = new Socket(HOST, PORT);
            try (
                    InputStream in = socket.getInputStream();
                    OutputStream out = socket.getOutputStream()) {

                String line = "Hello from Client";
                out.write(line.getBytes());
                out.flush();

                byte[] data = new byte[1024];
                int readBytes = in.read(data);
                System.out.printf("Server>%s", new String(data, 0, readBytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}