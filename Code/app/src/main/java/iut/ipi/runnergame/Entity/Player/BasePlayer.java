package iut.ipi.runnergame.Entity.Player;

import android.content.Context;

import java.io.IOException;

import iut.ipi.runnergame.Engine.Graphics.Animation.AnimationManager;
import iut.ipi.runnergame.Engine.Graphics.Animation.SpriteSheetAnimation.BaseSpriteSheetAnimation;
import iut.ipi.runnergame.Engine.Graphics.Point.AbstractPoint;
import iut.ipi.runnergame.R;

public class BasePlayer extends Player{
    public BasePlayer(Context context, AbstractPoint pos, int scale) throws IOException {
        super(pos, new BaseSpriteSheetAnimation(context, R.drawable.sprite_player_1, scale, 4, Player.DEFAULT_FRAME_DURATION, 6, 4));
    }
}