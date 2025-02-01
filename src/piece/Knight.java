package piece;

import main.GamePanel;

public class Knight extends Piece {

    public Knight(int color, int col, int row) {
        super(color, col, row);

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/HWF");
        } else {
            image = getImage("/piece/HBF");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {
        if (isWithinBoard(targetCol, targetRow)) {

            // Knight can move in an L shape 1:2 or 2:1
            if (Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 2) {

                if (isValidSquare(targetCol, targetRow)) {
                    return true;
                }
            }
        }

        return false;
    }
}
