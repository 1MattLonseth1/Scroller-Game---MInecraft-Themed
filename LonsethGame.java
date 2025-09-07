import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class LonsethGame extends SimpleGame {

    public static final String INTRO_SPLASH_FILE = "assets/splash-minecraft.gif";

    private static final String PLAYER_IMAGE_FILE = "assets/player-steve.gif";

    private static final String AVOID_IMAGE_FILE = "assets/avoid-zombie.gif";

    private static final String SAVOID_IMAGE_FILE = "assets/special_avoid-slime.gif";

    private static final String COLLECT_IMAGE_FILE = "assets/collect-diamond.gif";

    private static final String SCOLLECT_IMAGE_FILE = "assets/special_collect-tnt.gif";

    private static final String LOSE_SCREEN_FILE = "assets/lose-screen.gif";

    private static final String WIN_SCREEN_FILE = "assets/win-screen.gif";

    private static final String BACKGROUND_IMG_FILE = "assets/background-minecraft.gif";

    protected static final int SPAWN_INTERVAL = 55;

    protected static final int MAX_HP = 5;

    protected static final int MIN_HP = 0;

    protected static final int SCOLLECT_POINT_VALUE = 5;

    protected static final int BOUNCE_SPEED = 8;

    protected static final int attemptedSpawns = 4;


    private boolean isBouncing = false;

    public LonsethGame(){
        super();
    }

    protected void pregame(){
        this.setSplashImg(INTRO_SPLASH_FILE);
        this.setBackgroundImg(BACKGROUND_IMG_FILE);
        this.player = new Player(STARTING_PLAYER_X, STARTING_PLAYER_Y, PLAYER_IMAGE_FILE);
        this.toDraw.add(player);
        this.score = 0;
    }

    protected void postgame(){
        if (player.getHP() <= MIN_HP) {
            super.setTitle("Game over! You LOSE!");
            this.setSplashImg(LOSE_SCREEN_FILE);
        }
        else {
            super.setTitle("Game over! You WON!");
            this.setSplashImg(WIN_SCREEN_FILE);
        }
    }

    protected void performSpawning(){
        ArrayList<Entity> spawned = new ArrayList<Entity>();
        Entity E = null;

        for (int i = 0; i < attemptedSpawns; i++){
            E = getRandomEntity(E);

            E.setY((int)(Math.random() * (getWindowHeight() - E.getHeight())));
            E.setX(getWindowWidth());

            spawned.add(E);

            super.removeCollisions(spawned);

            toDraw.add(E);
        }
    }

    private Entity getRandomEntity(Entity E){
        int randNum = (int)(Math.random() * 101);
        if (randNum <= 40){
            E = new Avoid(0, 0, 80, 80, AVOID_IMAGE_FILE);
        }
        else if (randNum <= 75){
            E = new SpecialAvoid(0, 0, 50, 200, SAVOID_IMAGE_FILE);
        }
        else if (randNum <= 85){
            E = new SpecialCollect(0, 0, 50, 50, SCOLLECT_IMAGE_FILE);
        }
        else if (randNum <= 100){
            E = new Collect(0, 0, 40, 40, COLLECT_IMAGE_FILE);
        }
        return E;
    }

    protected void gameUpdate(){
        //scroll all AutoScroller Entities on the game board
        performScrolling();
        Collection<Entity> collisions = findAllCollisions(player);
        for (Entity E : collisions){
            if (!(E instanceof SpecialCollect)){
                E.flagForGC(true);
                score += ((CollisionReactive)E).getScoreChange();
                player.modifyHP(((CollisionReactive)E).getHPChange());
                if (E instanceof SpecialAvoid){
                   isBouncing = true;
                }
            }
        }
        if (isBouncing){
            if (player.getX() <= 0){
                isBouncing = false;
                player.setX(0);
            }
            else {
            bounce();
            }
        }

        //Spawn new entities only at a certain interval
        if (super.getTicksElapsed() % SPAWN_INTERVAL == 0){
            performSpawning();
            gcOffscreenEntities();
        }
        updateTitleText();

    }

    protected void bounce(){
        player.setX(player.getX() - BOUNCE_SPEED);
    }

    protected MouseEvent reactToMouseClick(MouseEvent click){
        Entity E;
        for (int j = 1; j < toDraw.size(); j++){
            E = toDraw.get(j);
            if (E instanceof SpecialCollect){
                if (E.containsPoint(click.getX(), click.getY())){
                    if (player.getHP() < MAX_HP)
                        player.modifyHP(((CollisionReactive)E).getHPChange());
                    score += ((CollisionReactive)E).getScoreChange(SCOLLECT_POINT_VALUE);
                    for (int i = 1; i < toDraw.size(); i++){
                        E = toDraw.get(i);
                        E.flagForGC(true);
                    }
                    break;
                }
            }
        }
        return click;
    }

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
            else if (key == LEFT_KEY && player.getX() > 0 && !isBouncing)
                player.setX(player.getX() - player.getMoveSpeed());
            else if (key == RIGHT_KEY && player.getX() < (getWindowWidth() - player.getWidth()) && !isBouncing)
                player.setX(player.getX() + player.getMoveSpeed());
        }
    }
}
