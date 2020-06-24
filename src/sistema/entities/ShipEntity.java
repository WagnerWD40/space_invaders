package sistema.entities;

import sistema.Game;

public class ShipEntity extends Entity {

    private Game game;

    public ShipEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);

        this.game = game;
    }

    public void move(long delta) {
        // if we're moving left and have reached the left hand side of the screen, don't move
        if ((dx < 0) && (x < 10)) {
            return;
        }

        // if we're moving right and have reached the right hand side of the screen, don't move
        if ((dx > 0) && (x > 750)) {
            return;
        }

        super.move(delta);
    }

    public void collidedWith(Entity other) {
        // if its an alien, notify the game that the player is dead
        if (other instanceof AlienEntity) {
            game.notifyDeath();
        }
    }
}
