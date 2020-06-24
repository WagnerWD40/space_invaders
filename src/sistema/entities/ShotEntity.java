package sistema.entities;

import sistema.Game;

public class ShotEntity extends Entity {

    private Game game;

    private double moveSpeed = -300;

    /**
     * True if this shot has been "used", i.e. its hit something
     */
    private boolean used = false;

    public ShotEntity(Game game, String sprite, int x, int y) {
        super(sprite, x, y);

        this.game = game;

        dy = moveSpeed;

    }

    public void move(long delta) {
        // proceed with normal move
        super.move(delta);

        // if we shot off the screen, remove ourselves
        if (y < -100) {
            game.removeEntity(this);
        }
    }

    public void collidedWith(Entity other) {
        // if we've hit an alien, kill it!
        if (other instanceof AlienEntity) {
            // remove the affected entities
            game.removeEntity(this);
            game.removeEntity(other);

            // notify the game that the alien has been killed
            game.notifyAlienKilled();
        }
    }
}