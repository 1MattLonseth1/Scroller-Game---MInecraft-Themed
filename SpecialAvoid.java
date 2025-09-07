//A SpecialAvoid is a rare kind of Avoid that spawns more infrequently than the regular Avoid
//When collided with, SpecialAvoids lower the Player's HP by the same amount as a standard Avoid,
//but also decrease the Player's score.  Otherwise, behaves the same as a regular Avoid
public class SpecialAvoid extends Avoid{
    
    //Location of image file to be drawn for a SpecialAvoid
    private static final String SAVOID_IMAGE_FILE = "media_files/special_avoid.gif";

    //Dimensions of the Avoid  
    private static final int SAVOID_WIDTH = 40;
    private static final int SAVOID_HEIGHT = 125; 
    
    //The amount of points lost when the player collides with a SpecialAvoid
    private static final int SAVOID_POINTS_PENALTY = -50;

    public SpecialAvoid(){
        this(0, 0);        
    }
    
    public SpecialAvoid(int x, int y){
        this(x, y, SAVOID_IMAGE_FILE);  
    }

    public SpecialAvoid(int x, int y, String imageName){
        this(x, y, SAVOID_WIDTH, SAVOID_HEIGHT, imageName);  
    }  
    
    public SpecialAvoid(int x, int y, int width, int height, String imageName){
        super(x, y, width, height, imageName);
    }
    
    public int getScoreChange(){
        return SAVOID_POINTS_PENALTY;
    }
}
