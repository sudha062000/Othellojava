import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            // Connect to the server at localhost on port 12345
            Socket socket = new Socket("localhost", 12345);
            System.out.println("Connected to server!");

            // Input and Output streams to send and receive messages
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send a message to the server
            out.println("Hello, Server!");

            // Read the response from the server
            String serverResponse = in.readLine();
            System.out.println("Received from server: " + serverResponse);

            // Close the connection
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
