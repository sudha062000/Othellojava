import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class OthelloClient extends JFrame {
    private static final int SIZE = 8;
    private JButton[][] buttons = new JButton[SIZE][SIZE];
    private char[][] board = new char[SIZE][SIZE];
    private char playerSymbol = ' ';
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public OthelloClient() {
        setTitle("Othello (Reversi)");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeGUI();
    }

    private void initializeGUI() {
        JPanel boardPanel = new JPanel(new GridLayout(SIZE, SIZE));
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 20));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].setBackground(Color.GREEN);
                buttons[i][j].setBorder(new LineBorder(Color.BLACK, 1));
                int row = i;
                int col = j;
                buttons[i][j].addActionListener(e -> handleMove(row, col));
                boardPanel.add(buttons[i][j]);
            }
        }

        add(boardPanel, BorderLayout.CENTER);
        setResizable(false);
    }

    private void handleMove(int row, int col) {
        if (playerSymbol == ' ') {
            JOptionPane.showMessageDialog(this, "Waiting for another player...");
            return;
        }

        out.println(row + "," + col);
    }

    private void updateBoard(char[][] board) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 'X') {
                    buttons[i][j].setText("X");
                    buttons[i][j].setForeground(Color.RED);
                } else if (board[i][j] == 'O') {
                    buttons[i][j].setText("O");
                    buttons[i][j].setForeground(Color.BLUE);
                } else {
                    buttons[i][j].setText("");
                }
            }
        }
    }

    private void startClient() {
        try {
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Start a thread to listen for updates from the server
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.length() == 1) {
                            playerSymbol = line.charAt(0);
                        } else if (line.startsWith("-") || line.startsWith("X") || line.startsWith("O")) {
                            updateBoard(parseBoard(line));
                        } else {
                            JOptionPane.showMessageDialog(this, line);
                        }
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
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private char[][] parseBoard(String boardState) {
        char[][] board = new char[SIZE][SIZE];
        String[] rows = boardState.split("\n");
        for (int i = 0; i < SIZE; i++) {
            String[] cells = rows[i].split(" ");
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = cells[j].charAt(0);
            }
        }
        return board;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OthelloClient client = new OthelloClient();
            client.setVisible(true);
            client.startClient();
        });
    }
}
