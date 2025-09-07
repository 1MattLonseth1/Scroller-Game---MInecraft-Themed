//A SpecialCollect is a rare kind of Collect that spawns more infrequently than the regular Collect
//When collided with, SpecialCollects increase the Player's score by the same amount as a standard Collect,
//but also increase the Player's HP.  Otherwise, behaves the same as a regular Collect.
public class SpecialCollect extends Collect{
    
    //Location of image file to be drawn for a SpecialCollect
    private static final String SCOLLECT_IMAGE_FILE = "media_files/special_collect.gif";

    //Dimensions of the Collect  
    private static final int SCOLLECT_WIDTH = 50;
    private static final int SCOLLECT_HEIGHT = 50;   

    //The amount of HP restored when the player collides with a SpecialCollect
    private static final int SCOLLECT_HP_RESTORED = 1;    


    public SpecialCollect(){
        this(0, 0);        
    }
    
    public SpecialCollect(int x, int y){
        this(x, y, SCOLLECT_IMAGE_FILE);  
    }

    public SpecialCollect(int x, int y, String imageName){
        this(x, y, SCOLLECT_WIDTH, SCOLLECT_HEIGHT, imageName);  
    }  
    
    public SpecialCollect(int x, int y, int width, int height, String imageName){
        super(x, y, width, height, imageName);
    }
    

    public int getHPChange(){
        return SCOLLECT_HP_RESTORED;
    }
   
}
