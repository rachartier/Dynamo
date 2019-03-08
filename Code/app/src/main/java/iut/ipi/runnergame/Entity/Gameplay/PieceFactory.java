package iut.ipi.runnergame.Entity.Gameplay;

import android.content.Context;

import iut.ipi.runnergame.R;
import iut.ipi.runnergame.Util.Point.AbstractPoint;

public class PieceFactory {

    // comme ca instanciation impossible
    private PieceFactory() {}

    public static Piece create(Context context, AbstractPoint point, PieceType type) {
        switch (type) {
            case LOW:
                return new Piece(context, point, R.drawable.sprite_piece_copper_1, Piece.VALUE_LOW);
            case NORMAL:
                return new Piece(context, point, R.drawable.sprite_piece_silver_1, Piece.VALUE_NORMAL);
            case HIGH:
                return new Piece(context, point, R.drawable.sprite_piece_gold_1, Piece.VALUE_HIGH);
        }

        return null;
    }
}