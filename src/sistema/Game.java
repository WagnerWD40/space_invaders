package sistema;

import sistema.entities.AlienEntity;
import sistema.entities.Entity;
import sistema.entities.ShipEntity;
import sistema.entities.ShotEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

public class Game extends Canvas {

    /** The stragey that allows us to use accelerate page flipping */
    private BufferStrategy strategy;

    /** True if the game is currently "running", i.e. the game loop is looping */
    private boolean gameRunning = true;

    /** The list of all the entities that exists in our game */
    private ArrayList<Entity> entities = new ArrayList<>();

    /**  The list of entities that need to be removed from the game this loop */
    private ArrayList<Entity> removeList = new ArrayList<>();

    /** The entity representing the player */
    private Entity ship;

    /* The speed at which the player's ship should move (pixels/sec) */
    private double moveSpeed = 300;

    /**  The time at which the player last fired a shot */
    private long lastFire = 0;

    /**  The interval between our players shot (ms) */
    private long firingInterval = 500;

    /** True if we're holding up game play until a key has been pressed */
    private boolean waitingForKeyPress = true;

    /**  True if game logic needs to be applied this loop, normally as a result of a game event */
    private boolean logicRequiredThisLoop = false;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean firePressed = false;

    /** The message to display which waiting for a key press */
    private String message = "";

    private int alienCount;

    public Game() {
        // create a frame to contain our game
        JFrame container = new JFrame("Space Invaders 101");

        // get hold the content of the frame and set up the resolution of the game
        JPanel panel = (JPanel) container.getContentPane();

        panel.setPreferredSize(new Dimension(800, 600));
        panel.setLayout(null);

        // setup our canvas size and put it into the content of the frame
        setBounds(0, 0, 800, 600);
        panel.add(this);

        // Tell AWT not to bother repainting our canvas since we're going to do that our self in accelerated mode
        setIgnoreRepaint(true);

        // finally make the window visible
        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        // add a listener to respond to the user closing the window. If they do we'd like to exit the game
        container.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
               // super.windowClosing(e);
                System.exit(0);
            }
        });

        // add a key input system (defined below) to our canvas to manage our accelerated graphics
        addKeyListener(new KeyInputHandler());

        // request the focus so key events come to us
        requestFocus();

        // create the buffering strategy which will allow AWT to manage our accelerated graphics
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        initEntities();
    }

    public void gameLoop() {

        long lastLoopTime = System.currentTimeMillis();

        while (gameRunning) {
            // work out how long its been since the last update, this
            // will be used to calculate how far the entities should
            // move this loop
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();

            // Get hold of a graphics context for the accelerated
            // surface and blank it out
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setColor(Color.black);
            g.fillRect(0, 0, 800, 600);

            // cycle round asking each entity to move itself
            if (!waitingForKeyPress) {
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = (Entity) entities.get(i);

                    entity.move(delta);
                }
            }

            // cycle round drawing all the entities we have in the game
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = (Entity) entities.get(i);

                entity.draw(g);
            }

            // brute force collisions, compare every entity against every other entity,
            // If any of them collide notify both entities that the collision has occurred
            for (int p = 0; p < entities.size(); p++) {
                for (int s = p + 1; s < entities.size(); s++) {
                    Entity me = (Entity) entities.get(p);
                    Entity him = (Entity) entities.get(s);

                    if (me.collidesWith(him)) {
                        me.collidedWith(him);
                        him.collidedWith(me);
                    }
                }
            }

            entities.removeAll(removeList);
            removeList.clear();

            // if a game event has indicated that game logic should be resolved, cycle round every entity requesting
            // that their personal logic should be considered.
            if (logicRequiredThisLoop) {
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = (Entity) entities.get(i);
                    entity.doLogic();
                }

                logicRequiredThisLoop = false;
            }

            // if we're waiting for an "any key" press then draw the current message
            if (waitingForKeyPress) {
                g.setColor(Color.white);
                g.drawString(
                        message,
                        (800 - g.getFontMetrics().stringWidth(message)) / 2
                        ,300
                );
                g.drawString(
                        "Press any key",
                        (800 - g.getFontMetrics().stringWidth("Press any key")) / 2,
                        300
                );
            }

            // finally, we've completed drawing so clear up the graphics
            // and flip the buffer over
            g.dispose();
            strategy.show();


            // resolve the movement of the ship. First assume the ship isn't moving.
            //If either cursor key is pressed then update the movement appropriately
            ship.setHorizontalMovement(0.0);

            if ((leftPressed) && (!rightPressed)) {
                ship.setHorizontalMovement(-moveSpeed);
            } else if ((rightPressed) && (!leftPressed)) {
                ship.setHorizontalMovement(moveSpeed);
            }

            // if we're pressing fire, attempt to fire
            if (firePressed) {
                tryToFire();
            }

            // finally pause for a bit. Note: this should run us at about
            // 100 fps but on windows this might vary each loop due to
            // a bad implementation of timer
            try { Thread.sleep(10); } catch (Exception e) {}
        }

    }

    private void initEntities() {
        // create the player ship and place it roughly in the center of screen
        ship = new ShipEntity(this, "sprites/ship.gif", 370, 550);
        entities.add(ship);

        // create a block of aliens (5 rows, by 12 aliens, spaced evenly)
        alienCount = 0;
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 12; column++) {
                Entity alien = new AlienEntity(
                        this,
                        "sprites/alien.gif",
                        100 + (column * 50),
                        (50) + row * 30
                );

                entities.add(alien);
                alienCount++;
            }
        }
    }

    public void removeEntity(Entity entity) {
        removeList.add(entity);
    }

    public void updateLogic() {
        logicRequiredThisLoop = true;
    }

    private class KeyInputHandler extends KeyAdapter {

        /** The number of key presses we've had while waiting for an "any key press */
        private int pressCount = 1;

        public void  keyPressed(KeyEvent e) {

            //if we're waiting for an "any key" typed then we don't waitn to do anything with just a "press"
            if (waitingForKeyPress) {
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                firePressed = true;
            }
        }

        public void keyReleased(KeyEvent e) {

            //if we're waiting for an "any key" typed then we don't waitn to do anything with just a "released"
            if (waitingForKeyPress) {
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                firePressed = false;
            }
        }

        public void keyTyped(KeyEvent e) {

            if (waitingForKeyPress) {
                if (pressCount == 1) {
                    waitingForKeyPress = false;
                    startGame();
                    pressCount = 0;
                } else {
                    pressCount++;
                }
            }

            // if we hit escape, then quit the game
            if (e.getKeyChar() == 27) {
                System.exit(0);
            }
        }

    }

    /**
     * Attempt to fire a shot from the player. Its called "try" since we must first check that the player
     * can fire at this point, i.e. he/she waited long enough between shots
     */
    public void tryToFire() {
        // check that we have awaited long enough to fire
        if (System.currentTimeMillis() - lastFire < firingInterval) {
            return;
        }

        // if we waited long enough, create the shot entity, and record the time.
        lastFire = System.currentTimeMillis();
        ShotEntity shot = new ShotEntity(
                this,
                "sprites/shot.gif",
                (int) ship.getX() + 10,
                (int) ship.getY() -30
        );

        entities.add(shot);
    }

    public void notifyDeath() {
        message = "Oh no! They got you, try again?";
        waitingForKeyPress = true;
    }

    public void notifyAlienKilled() {
        // reduce the alien count, if there are none left, the player has won!
        alienCount--;

        if (alienCount == 0) {
            notifyWin();
        }

        // if there are still some aliens left then they all need to get faster, so speed up all existing aliens
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = (Entity) entities.get(i);

            if (entity instanceof  AlienEntity) {
                // speed up by 2%
                entity.setHorizontalMovement((entity.getsetHorizontalMovement() * 1.02));
            }
        }
    }

    public void notifyWin() {
        message = "Well done! You Win!";
        waitingForKeyPress = true;
    }

    private void startGame() {
        // clear out any existing entities and initialise a new set
        entities.clear();
        initEntities();


        // blank out any keyboard settings we might currently have
        leftPressed = false;
        rightPressed = false;
        firePressed = false;
    }

}
