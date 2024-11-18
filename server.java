import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        try {
            // Create a ServerSocket that listens on port 12345
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server is listening on port 12345...");

            // Accept a client connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            // Input and Output streams to communicate with the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received from client: " + clientMessage);
                out.println("Server: " + clientMessage); // Echo the message back to the client
            }

            // Close the connections
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
