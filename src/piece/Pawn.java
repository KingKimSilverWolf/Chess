package piece;

import main.GamePanel;
import main.Type;

public class Pawn extends Piece {

    public Pawn(int color, int col, int row) {
        super(color, col, row);
        type = Type.PAWN;

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/PWF");
        } else {
            image = getImage("/piece/PBF");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {
        if (isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {

            // Define the move value/direction based on its color
            int moveValue;
            if (color == GamePanel.WHITE) {
                moveValue = -1; // going up
            } else {
                moveValue = 1; // going down
            }

            // Check the hitting piece
            hittingP = getHittingP(targetCol, targetRow);

            // 1 square for forward move
            if (targetCol == preCol && targetRow == preRow + moveValue && hittingP == null) {
                return true;
            }

            // 2 Square movement for the first move (special move for pawns)
            if (targetCol == preCol && targetRow == preRow + moveValue * 2 && hittingP == null && !moved &&
                      !pieceIsOnStraightLine(targetCol, targetRow)) {
                return true;
            }

            // Diagonal capture & movement (if a piece is on a square diagonally in front of it)
            if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingP != null &&
                      hittingP.color != color) {
                return true;
            }

            // En Passant
            if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue) {
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == targetCol && piece.row == preRow && piece.twoStepped) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
