package piece;

import main.Board;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Superclass for all pieces
public class Piece {

    public BufferedImage image;
    public int x, y;
    public int col, row, preCol, preRow;
    public int color;

    public Piece(int color, int col, int row) {
        this.color = color;
        this.col = col;
        this.row = row;
        x = getX(col);
        y = getY(row);
        preCol = col;
        preRow = row;
    }

    public BufferedImage getImage(String imagePath) {

        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass().getResourceAsStream(imagePath+".png"));
        } catch (IOException e){
            e.printStackTrace();
        }

        return image;
    }

    // Getters for x and y coordinates
    public int getX(int col) {
        return col * Board.SQUARE_SIZE;
    }
    public int getY(int row) {
        return row * Board.SQUARE_SIZE;
    }

    public int getCol(int x) {
        return (x + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE;
    }
    public int getRow(int y) {
        return (y + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE;
    }

    public void updatePosition() {
        x = getX(col);
        y = getY(row);
        preCol = getCol(x);
        preRow = getRow(y);
    }

    public void draw(Graphics2D g2) {
        // Calculate the scaling factor to fit the piece within the square
        double scaleFactor = Math.min(
                (double) Board.SQUARE_SIZE / image.getWidth(),
                (double) Board.SQUARE_SIZE / image.getHeight()
        );

        // Calculate the new width and height after scaling
        int scaledWidth = (int) (image.getWidth() * scaleFactor);
        int scaledHeight = (int) (image.getHeight() * scaleFactor);

        // Calculate the position to center the piece within the square
        int offsetX = (Board.SQUARE_SIZE - scaledWidth) / 2;
        int offsetY = (Board.SQUARE_SIZE - scaledHeight) / 2;

        // Draw the scaled and centered piece
        g2.drawImage(image, x + offsetX, y + offsetY, scaledWidth, scaledHeight, null);
    }
}
