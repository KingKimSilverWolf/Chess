package piece;

import main.GamePanel;
import main.Type;

public class Rook extends Piece {

    public Rook(int color, int col, int row) {
        super(color, col, row);
        type = Type.ROOK;

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/CWF");
        } else {
            image = getImage("/piece/CBF");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {
        if (isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {

            // Rook can move vertically or horizontally
            if (targetCol == preCol || targetRow == preRow) {

                if (isValidSquare(targetCol, targetRow) && !pieceIsOnStraightLine(targetCol, targetRow)) {
                    return true;
                }
            }
        }

        return false;
    }
}
