package main;

import piece.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread; // The thread that runs the game (less lag)
    Board board = new Board();
    Mouse mouse = new Mouse();

    // Pieces
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    Piece activePiece;

    // Piece Colors
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE; // In chess white always goes first

    // Booleans for piece movement
    boolean canMove;
    boolean validSquare;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        setPieces();
        copyPieces(pieces, simPieces);
    }

    public void launchGame() {
        gameThread = new Thread(this); // Basically calling the run method
        gameThread.start();
    }

    public void setPieces() {

        // White Pieces
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));

        // Black Pieces
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Queen(BLACK, 3, 0));
        pieces.add(new King(BLACK, 4, 0));
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {

        target.clear();
        for (int i = 0; i < source.size(); i++) {
            target.add(source.get(i));
        }
    }

    // Handles all logic related to the game
    private void update() {

        // Check if mouse is pressed
        if (mouse.pressed) {
            if (activePiece == null) {
                // If no piece is active, check if a piece is clicked
                for (Piece piece : simPieces) {
                    // Check if piece is clicked then set it as active
                    if (piece.color == currentColor &&
                            piece.col == mouse.x / Board.SQUARE_SIZE &&
                            piece.row == mouse.y / Board.SQUARE_SIZE) {

                        activePiece = piece;
                    }
                }
            } else {
                simulate();
            }
        }

        // Check if mouse is released
        if(mouse.pressed == false) {
            if (activePiece != null) {
               if (validSquare){
                   activePiece.updatePosition();
               } else {
                   activePiece.resetPosition();
                   activePiece = null;
               }

            }
        }
    }

    private void simulate() {

        canMove = false;
        validSquare = false;

        // If a piece is clicked, move it to the mouse location
        activePiece.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activePiece.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activePiece.col = activePiece.getCol(activePiece.x);
        activePiece.row = activePiece.getRow(activePiece.y);

        // Check if piece is hovering over a valid square
        if (activePiece.canMove(activePiece.col, activePiece.row)) {

            validSquare = true;
            canMove = true;
        }
    }

    // Handles all graphics related to the game e.g. Pieces and board
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g; // Cast g to Graphics2D

        // Draw board
        board.draw(g2);

        // Draw pieces
        for (Piece piece : simPieces) {
            piece.draw(g2);
        }

        if (activePiece != null) {
            if (canMove){
                g2.setColor(Color.white);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.fillRect(activePiece.col*Board.SQUARE_SIZE, activePiece.row*Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            // Draw active piece
            activePiece.draw(g2);
        }
    }

    @Override
    public void run() {
        // Create a game loop
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }
}
