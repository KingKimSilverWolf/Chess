package main;

import java.awt.*;

public class Board {

    final int MAX_COL = 8;
    final int MAX_ROW = 8;
    public static final int SQUARE_SIZE = 100; // 1 square = 100 pixels
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE / 2;

    public void draw(Graphics2D g2) {

        int color = 0; //

        for (int row = 0; row < MAX_ROW; row++) {

            for (int col = 0; col < MAX_COL; col++) {

                // Squares are drawn in alternating colors
                if (color == 0) {
                    g2.setColor(new Color(240, 217, 181)); // Cream (Maple Wood)
                    color = 1;
                } else {
                    g2.setColor(new Color(181, 136, 99)); // Brown (Walnut)
                    color = 0;
                }

                g2.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }

            // So that the next row is drawn in the opposite color
            if (color == 0) {
                color = 1;
            } else {
                color = 0;
            }
        }
    }
}
