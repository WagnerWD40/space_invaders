package sistema.entities;

import sistema.Sprite;
import sistema.SpriteStore;

import java.awt.*;

/**
 * An entity represents any element that appears in the game. The entity is responsible for
 * resolving collisions and movement based on a set of properties defined either by subclass
 * or externally.
 *
 * Note that doubles are used for positions. This way seem strange given that pixels locations
 * are integers. However, using double means that an entity can move a partial pixel. It doesn't
 * of course mean that they will be displayed half way through a pixel but allows us to not lose
 * accuracy as we move.
 */

public abstract class Entity {

    protected double x;
    protected double y;
    protected Sprite sprite;
    /** The current speed of this entity horizontally (pixels/sec) */
    protected double dx;
    /** The current speed of this entity vertically (pixels/sec) */
    protected double dy;

    /** The rectangle used fro this entity during collisions resolution */
    private Rectangle me = new Rectangle();

    /** The rectangle used for other entities during collision resolution */
    private Rectangle him = new Rectangle();

    public Entity(String ref, int x, int y) {
        this.sprite = SpriteStore.get().getSprite(ref);
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getsetHorizontalMovement() {
        return dx;
    }

    public void setHorizontalMovement(double dx) {
        this.dx = dx;
    }

    public double getVerticalMovement() {
        return dy;
    }

    public void setVerticalMovement(double dy) {
        this.dy = dy;
    }

    public void move(long delta) {
        // update the location of the entity based on move speeds;
        // The division by 1000 is to adjust for the fact that the movement value is specified in pixels
        // per second, but the time is specified in milliseconds.
        x += (delta * dx) / 1000;
        y += (delta * dy) / 1000;
    }

    public void draw(Graphics g) {
        sprite.draw(g, (int) x, (int) y);
    }

    public void doLogic() {

    }

    // Check if this entity collided with another.
    public boolean collidesWith(Entity other) {
        me.setBounds(
                (int) x,
                (int) y,
                sprite.getWidth(),
                sprite.getHeight()
        );

        him.setBounds(
                (int) other.x,
                (int) other.y,
                other.sprite.getWidth(),
                other.sprite.getHeight()
        );

        return me.intersects(him);
    }

    public abstract void collidedWith(Entity other);

}

