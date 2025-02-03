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
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece activePiece, checkingP;
    public static Piece castlingPiece;

    // Piece Colors
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE; // In chess white always goes first

    // Booleans for piece movement
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;

    public boolean canPromote() {
        if (activePiece.type == Type.PAWN){
            if (currentColor == WHITE && activePiece.row == 0 || currentColor == BLACK && activePiece.row == 7) {
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor, 9, 2));
                promoPieces.add(new Knight(currentColor, 9, 3));
                promoPieces.add(new Bishop(currentColor, 9, 4));
                promoPieces.add(new Queen(currentColor, 9, 5));
                return true;
            }
        }

        return false;
    }

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

        //pieces.add(new Queen(WHITE, 4, 4)); // test piece

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

        if (promotion) {
            promoting();
        } else if (!gameOver) {

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
            if(!mouse.pressed) {
                if (activePiece != null) {
                    if (validSquare){

                        // MOVE CONFIRMED

                        // Update the pieces list in case a piece has been captured and removed
                        copyPieces(simPieces, pieces);
                        activePiece.updatePosition();

                        if (castlingPiece != null) {
                            castlingPiece.updatePosition();
                        }

                        // Check ifd opponent is in check
                        if (isKingInCheck() && isCheckmate()){
                            gameOver = true;
                        }
                        else { // The game is still going on

                            if (canPromote()){
                                promotion = true;
                            } else {
                                changePlayer();
                            }
                        }

                    } else {
                        // The move is not valid so reset everything
                        copyPieces(pieces, simPieces);
                        activePiece.resetPosition();
                        activePiece = null;
                    }

                }
            }
        }

    }

    private boolean opponentCanCaptureKing(){

        Piece king = getKing(false);

        for (Piece piece : simPieces) {
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck() {

        Piece king = getKing(true);

        if (activePiece.canMove(king.col, king.row)) {
            checkingP = activePiece;
            return true;
        } else {
            checkingP = null;
        }

        return false;
    }

    private Piece getKing(boolean opponent) {

        Piece king = null;

        for (Piece piece : simPieces) {
            if (opponent) {
                if (piece.type == Type.KING && piece.color != currentColor) {
                    king = piece;
                }
            }
            else {
                if (piece.type == Type.KING && piece.color == currentColor) {
                    king = piece;
                }
            }
        }

        return king;
    }

    private void promoting() {

        if (mouse.pressed){
            for (Piece piece : promoPieces) {
                if (piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE) {
                    switch (piece.type){
                        case ROOK: simPieces.add(new Rook(currentColor, activePiece.col, activePiece.row)); break;
                        case KNIGHT: simPieces.add(new Knight(currentColor, activePiece.col, activePiece.row)); break;
                        case BISHOP: simPieces.add(new Bishop(currentColor, activePiece.col, activePiece.row)); break;
                        case QUEEN: simPieces.add(new Queen(currentColor, activePiece.col, activePiece.row)); break;
                        default: break;
                    }
                    simPieces.remove(activePiece.getIndex());
                    copyPieces(simPieces, pieces);
                    activePiece = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }

    private void simulate() {

        canMove = false;
        validSquare = false;

        // Reset the piece list in every loop
        // This is basically for restoring the removed piece during the simulation
        copyPieces(pieces, simPieces);

        // Reset the castling piece's position
        if (castlingPiece != null) {
            castlingPiece.col = castlingPiece.preCol;
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
            castlingPiece = null;
        }

        // If a piece is clicked, move it to the mouse location
        activePiece.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activePiece.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activePiece.col = activePiece.getCol(activePiece.x);
        activePiece.row = activePiece.getRow(activePiece.y);

        // Check if piece is hovering over a valid square
        if (activePiece.canMove(activePiece.col, activePiece.row)) {

            canMove = true;

            // If hitting a piece then capture it
            if (activePiece.hittingP != null){

                simPieces.remove(activePiece.hittingP.getIndex());
            }

            checkCastling();

            if (!isIllegal(activePiece) && !opponentCanCaptureKing()) {
                validSquare = true;
            }
        }
    }

    private boolean isIllegal(Piece king) {

        if (king.type == Type.KING) {
            for (Piece piece : simPieces) {
                if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }

        return false;
    }

    // For checking for checkmate
    private boolean isCheckmate(){

        Piece king = getKing(true);

        if (kingCanMove(king)) {
            return false;
        } else {

            // You still can escape
            //  Check if you can block it with another piece

            // Check the position of the checking piece and the king in check
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);

            if (colDiff == 0) {
                // The checking piece is attacking vertically
                if (checkingP.row < king.row) {
                    // The checking piece is above the king
                    for (int row = checkingP.row; row < king.row; row++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.row > king.row) {
                    // The checking piece is below the king
                    for (int row = checkingP.row; row > king.row; row--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }

            } else if (rowDiff == 0) {
                // The checking piece is attacking horizontally
                if (checkingP.col < king.col) {
                    // The checking piece is to the left of the king
                    for (int col = checkingP.col; col < king.col; col++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.col > king.col) {
                    // The checking piece is to the right of the king
                    for (int col = checkingP.col; col > king.col; col--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }

            } else if (colDiff == rowDiff) {
                // The checking piece is attacking diagonally
                if (checkingP.row < king.row) {
                    // The checking piece is above the king
                    if (checkingP.col < king.col) {
                        // The checking piece is in the upper left
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }

                        }
                    }
                    if (checkingP.col > king.col) {
                        // The checking piece is in the upper right
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }

                }
                if (checkingP.row > king.row) {
                    // The checking piece is below the king
                    if (checkingP.col < king.col) {
                        // The checking piece is in the lower left
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                    if (checkingP.col > king.col) {
                        // The checking piece is in the lower right
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean kingCanMove(Piece king) {

        // Simulate if there is any square where the king can move to
        if (isValidMove(king, -1, -1)) return true;
        if (isValidMove(king, -1, 0)) return true;
        if (isValidMove(king, -1, 1)) return true;
        if (isValidMove(king, 0, -1)) return true;
        if (isValidMove(king, 0, 1)) return true;
        if (isValidMove(king, 1, -1)) return true;
        if (isValidMove(king, 1, 0)) return true;
        if (isValidMove(king, 1, 1)) return true;

        return false;
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus) {

        boolean isValidMove = false;

        // Update the kings position for a second
        king.col += colPlus;
        king.row += rowPlus;

        if (king.canMove(king.col, king.row)) {

            if (king.hittingP != null){
                simPieces.remove(king.hittingP.getIndex());
            }
            if (!isIllegal(king)){
                isValidMove = true;
            }
        }

        // Reset the kings position and restore the removed piece
        king.resetPosition();
        copyPieces(pieces, simPieces);

        return isValidMove;
    }

    private void checkCastling() {

        if (castlingPiece != null) {
            if (castlingPiece.col == 0){
                castlingPiece.col += 3;
            } else if (castlingPiece.col == 7){
                castlingPiece.col -= 2;
            }
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
        }
    }

    private void changePlayer() {

        if (currentColor == WHITE) {
            currentColor = BLACK;
            // Reset black's two stepped status
            for (Piece piece : pieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false;
                }
            }
        } else {
            currentColor = WHITE;
            // Reset white's two stepped status
            for (Piece piece : pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        }

        activePiece = null;
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
            if (canMove) {
                if (isIllegal(activePiece) || opponentCanCaptureKing()) {
                    // Keep the original rectangular highlighting for illegal moves
                    g2.setColor(Color.gray);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(
                            activePiece.col * Board.SQUARE_SIZE,
                            activePiece.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE
                    );
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                } else {
                    // Use smaller circular highlighting for legal moves
                    int circleSize = Board.SQUARE_SIZE / 2;
                    int circleOffset = (Board.SQUARE_SIZE - circleSize) / 2; // Center the circle

                    g2.setColor(Color.white);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillOval(
                            activePiece.col * Board.SQUARE_SIZE + circleOffset,
                            activePiece.row * Board.SQUARE_SIZE + circleOffset,
                            circleSize, circleSize
                    );
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
            }

            // Draw active piece
            activePiece.draw(g2);
        }

        // STATUS MESSAGES
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Georgia", Font.PLAIN, 40));
        g2.setColor(Color.white);

        if(promotion) {
            g2.drawString("Promote to:", 840, 150);
            for(Piece piece : promoPieces) {
                // Calculate the scaling factor to fit the piece within the square
                double scaleFactor = Math.min(
                        (double) Board.SQUARE_SIZE / piece.image.getWidth(),
                        (double) Board.SQUARE_SIZE / piece.image.getHeight()
                );

                // Calculate the new width and height after scaling
                int scaledWidth = (int) (piece.image.getWidth() * scaleFactor);
                int scaledHeight = (int) (piece.image.getHeight() * scaleFactor);

                // Calculate the position to center the piece within the square
                int offsetX = (Board.SQUARE_SIZE - scaledWidth) / 2;
                int offsetY = (Board.SQUARE_SIZE - scaledHeight) / 2;

                // Draw the scaled and centered piece
                g2.drawImage(piece.image,
                        piece.getX(piece.col) + offsetX, // X position + offset to center
                        piece.getY(piece.row) + offsetY, // Y position + offset to center
                        scaledWidth, scaledHeight, null);
            }
        } else {
            if (currentColor == WHITE) {
                g2.drawString("White's Turn", 840, 550);
                if (checkingP != null && checkingP.color == BLACK) {
                    g2.setColor(Color.red);
                    g2.drawString("The King", 840, 650);
                    g2.drawString("is in check", 840, 700);
                }
            } else {
                g2.drawString("Black's Turn", 840, 250);
                if (checkingP != null && checkingP.color == WHITE) {
                    g2.setColor(Color.red);
                    g2.drawString("The King", 840, 100);
                    g2.drawString("is in check", 840, 150);
                }
            }
        }

        if (gameOver) {
            String winnerText = (currentColor == WHITE) ? "WHITE WINS" : "BLACK WINS";

            // Set font and colors
            g2.setFont(new Font("Georgia", Font.BOLD, 80));
            g2.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black background

            // Define background rectangle dimensions
            int rectX = 180;
            int rectY = 340;
            int rectWidth = 750;
            int rectHeight = 120;

            // Draw background rectangle
            g2.fillRoundRect(rectX, rectY, rectWidth, rectHeight, 50, 50);

            // Set text color and draw text
            g2.setColor(Color.WHITE);
            g2.drawString(winnerText, 250, 420);

            // Add text shadow effect
            g2.setColor(Color.GREEN);
            g2.drawString(winnerText, 255, 425);
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
