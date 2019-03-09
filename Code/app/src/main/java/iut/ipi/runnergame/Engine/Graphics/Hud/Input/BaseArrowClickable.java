package iut.ipi.runnergame.Engine.Graphics.Hud.Input;

import iut.ipi.runnergame.Engine.Graphics.Hud.ArrowClickable;
import iut.ipi.runnergame.Engine.Graphics.Point.AbstractPoint;
import iut.ipi.runnergame.Entity.AbstractEntity;
import iut.ipi.runnergame.Entity.Collision.BaseCollisionBox;
import iut.ipi.runnergame.Entity.Collision.Collidable;
import iut.ipi.runnergame.Entity.Collision.Collision;

public class BaseArrowClickable extends AbstractEntity implements ArrowClickable, Collidable {
    private boolean isClicked = false;

    public Collision collision;

    public BaseArrowClickable(AbstractPoint pos, int width, int heigth) {
        super(pos, width, heigth);

        collision = new BaseCollisionBox(pos.x, pos.y, width, heigth);
    }

    public boolean pointInside(AbstractPoint point) {
        return getCollision().isInCollisionWithPoint(point);
    }

    public boolean getIsClicked() {
        return isClicked;
    }

    public void setIsClicked(boolean isClicked) {
        this.isClicked = isClicked;
    }

    @Override
    public AbstractPoint getPosition() {
        return super.getPosition();
    }

    @Override
    public void setCollision(Collision collision) {
        this.collision = collision;
    }

    @Override
    public Collision getCollision() {
        return collision;
    }

    @Override
    public void updateBecauseCollision() {
        // rien a faire car tout est gerer dans la cross
    }
}