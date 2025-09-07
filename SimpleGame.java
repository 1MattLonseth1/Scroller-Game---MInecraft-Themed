import java.awt.*;
import java.awt.event.*;
import java.util.*;

//A Simple version of the scrolling game, featuring Avoids, Collects, SpecialAvoids, and SpecialCollects
//Players must reach a score threshold to win.
//If player runs out of HP (via too many Avoid/SpecialAvoid collisions) they lose.
public class SimpleGame extends SSGEngine {
    
    
    //Starting Player coordinates
    protected static final int STARTING_PLAYER_X = 0;
    protected static final int STARTING_PLAYER_Y = 100;
    
    //Score needed to win the game
    protected static final int SCORE_TO_WIN = 300;
    
    //Maximum that the game speed can be increased to
    //(a percentage, ex: a value of 300 = 300% speed, or 3x regular speed)
    protected static final int MAX_GAME_SPEED = 300;
    //Interval that the speed changes when pressing speed up/down keys
    protected static final int SPEED_CHANGE_INTERVAL = 20;    
    
    public static final String INTRO_SPLASH_FILE = "media_files/splash.gif";
    //Key pressed to advance past the splash screen
    public static final int ADVANCE_SPLASH_KEY = KeyEvent.VK_ENTER;
    
    //Interval that Entities get spawned in the game window
    //ie: once every how many ticks does the game attempt to spawn new Entities
    protected static final int SPAWN_INTERVAL = 45;

    
    //A Random object for all your random number generation needs!
    public static final Random rand = new Random();
    
    //player's current score
    protected int score;
    
    
    //Stores a reference to game's Player object for quick reference (Though this Player presumably
    //is also in the DisplayList, but it will need to be referenced often)
    protected Player player;
    
    
    public SimpleGame(){
        super();
    }
    
    public SimpleGame(int gameWidth, int gameHeight){
        super(gameWidth, gameHeight);
    }
    
    
    //Performs all of the initialization operations that need to be done before the game starts
    protected void pregame(){
        this.setSplashImg(INTRO_SPLASH_FILE);
        this.setBackgroundColor(Color.BLACK);
        this.player = new Player(STARTING_PLAYER_X, STARTING_PLAYER_Y);
        this.toDraw.add(player); 
        this.score = 0;
    }
    
    //Called on each game tick
    protected void gameUpdate(){
        //scroll all AutoScroller Entities on the game board
        performScrolling();   
        Collection<Entity> collisions = findAllCollisions(player);
        for (Entity E : collisions){
            E.flagForGC(true);
            score += ((CollisionReactive)E).getScoreChange();
            player.modifyHP(((CollisionReactive)E).getHPChange());

        }
        //Spawn new entities only at a certain interval
        if (super.getTicksElapsed() % SPAWN_INTERVAL == 0){
            performSpawning();
            gcOffscreenEntities();
        }
        updateTitleText();
        
    }
    

    //Update the text at the top of the game window
    protected void updateTitleText(){
        setTitle("Health: " + player.getHP() + " Score: " + score);
        // setTitle("Health: " + health + " Score: " + score);
    }
    

    //Scroll all AutoScroller entities per their respective scroll speeds
    protected void performScrolling(){
        for (int i = 0; i < toDraw.size(); i++){
            Entity E = toDraw.get(i);
            if (E instanceof AutoScroller){
                ((AutoScroller)E).scroll();
            }
        }
    }

    
    //Handles "garbage collection" of the entities
    //Flags entities in the displaylist that are no longer relevant
    //(i.e. will no longer need to be drawn in the game window).
    protected void gcOffscreenEntities(){
        for (int i = 0; i < toDraw.size(); i++){
            Entity E = toDraw.get(i);
            if (E.getX() + E.getWidth() < 0){
                E.flagForGC(true);
            }
        }
    }
    
    
    
    //Spawn new Entities on the right edge of the game board
    protected void performSpawning(){
        int randNum = (int)(Math.random() * 3 + 2);
        ArrayList<Entity> spawned = new ArrayList<Entity>();
        Entity E = null;

        for (int i = 0; i < randNum; i++){
            E = getRandomEntity(E);

            E.setY((int)(Math.random() * (getWindowHeight() - E.getHeight())));
            E.setX(getWindowWidth());

            spawned.add(E);

            removeCollisions(spawned);

            toDraw.add(E);
        }
    }

    private Entity getRandomEntity(Entity E){
        int randNum = (int)(Math.random() * 101);
        if (randNum <= 35){
            E = new Avoid();
        }
        else if (randNum <= 50){
            E = new SpecialAvoid();
        }
        else if (randNum <= 65){
            E = new SpecialCollect();
        }
        else if (randNum <= 100){
            E = new Collect();
        }
        return E;
    }

    protected void removeCollisions(ArrayList<Entity> spawned){
        for (int j = 0; j < spawned.size(); j++){
            for (int k = j+1; k < spawned.size(); k++){
                if (spawned.get(j).isCollidingWith(spawned.get(k))){
                    spawned.get(k).flagForGC(true);
                }
            }
        }
    }

    
    //Called once the game is over, performs any end-of-game operations
    protected void postgame(){
        if (player.getHP() <= 0) {
            super.setTitle("Game over! You Lose!");
        }
        else {
            super.setTitle("Game over! You WON!");
        }
    }
    
    //Returns a boolean indicating if the game is over (true) or not (false)
    //Game can be over due to either a win or lose state
    protected boolean checkForGameOver(){
        if (player.getHP() <= 0 || score >= SCORE_TO_WIN)
            return true;
        return false;
    }
    
    //Reacts to a single key press on the keyboard
    protected void reactToKeyPress(int key){
        
        //if a splash screen is up, only react to the advance splash key
        if (getSplashImg() != null){
            if (key == ADVANCE_SPLASH_KEY)
                super.setSplashImg(null);
            return;
        }
        if (key == KEY_PAUSE_GAME)
            isPaused = !isPaused;

        if (key == SPEED_DOWN_KEY && getUniversalSpeed() != SPEED_CHANGE_INTERVAL)
            setUniversalSpeed(getUniversalSpeed() - SPEED_CHANGE_INTERVAL);
        else if (key == SPEED_UP_KEY && getUniversalSpeed() != MAX_GAME_SPEED)
            setUniversalSpeed(getUniversalSpeed() + SPEED_CHANGE_INTERVAL);


        if (!isPaused){
            if (key == UP_KEY && player.getY() > 0)
                player.setY(player.getY() - player.getMoveSpeed());
            else if (key == DOWN_KEY && player.getY() < (getWindowHeight() - player.getHeight()))
                player.setY(player.getY() + player.getMoveSpeed());
            else if (key == LEFT_KEY && player.getX() > 0)
                player.setX(player.getX() - player.getMoveSpeed());
            else if (key == RIGHT_KEY && player.getX() < (getWindowWidth() - player.getWidth()))
                player.setX(player.getX() + player.getMoveSpeed());
        }
    }
    
    
    
    //Handles reacting to a single mouse click in the game window
    protected MouseEvent reactToMouseClick(MouseEvent click){
        return click;//returns the mouse event for any child classes overriding this method
    }
    
    
    
}
