import java.io.*;
import java.net.*;
import java.util.*;

public class OthelloServer {
    private static final int PORT = 12345;
    private static final int SIZE = 8;
    private static List<PlayerHandler> players = new ArrayList<>();
    private static char[][] board = new char[SIZE][SIZE];
    private static char currentPlayer = 'X';

    public static void main(String[] args) {
        initializeBoard();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Player connected.");
                PlayerHandler playerHandler = new PlayerHandler(socket);
                players.add(playerHandler);
                new Thread(playerHandler).start();
                if (players.size() == 2) {
                    broadcastMessage("Game starting! Player 1 is X, Player 2 is O.");
                    break;  // Start the game once both players are connected
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = '-';
            }
        }
        board[3][3] = 'O';
        board[3][4] = 'X';
        board[4][3] = 'X';
        board[4][4] = 'O';
    }

    private static synchronized void makeMove(int row, int col, char player) {
        if (isValidMove(row, col, player)) {
            placePiece(row, col, player);
            currentPlayer = (player == 'X') ? 'O' : 'X';
            broadcastBoard();
        }
    }

    private static boolean isValidMove(int row, int col, char player) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE || board[row][col] != '-') {
            return false;
        }
        // Check for flipping pieces in all directions
        return canFlip(row, col, player);
    }

    private static boolean canFlip(int row, int col, char player) {
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) continue;
                if (checkDirection(row, col, dRow, dCol, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkDirection(int row, int col, int dRow, int dCol, char player) {
        int i = row + dRow, j = col + dCol;
        boolean opponentFound = false;

        while (i >= 0 && i < SIZE && j >= 0 && j < SIZE) {
            if (board[i][j] == '-') break;
            if (board[i][j] == player) {
                return opponentFound;
            }
            opponentFound = true;
            i += dRow;
            j += dCol;
        }
        return false;
    }

    private static void placePiece(int row, int col, char player) {
        board[row][col] = player;
        flipPieces(row, col, player);
    }

    private static void flipPieces(int row, int col, char player) {
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) continue;
                flipInDirection(row, col, dRow, dCol, player);
            }
        }
    }

    private static void flipInDirection(int row, int col, int dRow, int dCol, char player) {
        int i = row + dRow, j = col + dCol;
        List<int[]> toFlip = new ArrayList<>();

        while (i >= 0 && i < SIZE && j >= 0 && j < SIZE) {
            if (board[i][j] == '-') break;
            if (board[i][j] == player) {
                for (int[] pos : toFlip) {
                    board[pos[0]][pos[1]] = player;
                }
                return;
            }
            toFlip.add(new int[]{i, j});
            i += dRow;
            j += dCol;
        }
    }

    private static synchronized void broadcastBoard() {
        StringBuilder boardState = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boardState.append(board[i][j]).append(" ");
            }
            boardState.append("\n");
        }
        for (PlayerHandler player : players) {
            if (player.socket.isClosed()) {
                continue; // Skip closed sockets
            }
            player.sendMessage(boardState.toString());
        }
    }

    private static synchronized void broadcastMessage(String message) {
        for (PlayerHandler player : players) {
            if (player.socket.isClosed()) {
                continue; // Skip closed sockets
            }
            player.sendMessage(message);
        }
    }

    private static class PlayerHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public PlayerHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // Assign player
                char playerSymbol = (players.size() == 1) ? 'X' : 'O';
                out.println(playerSymbol);

                String move;
                while ((move = in.readLine()) != null) {
                    String[] parts = move.split(",");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    makeMove(row, col, playerSymbol);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
