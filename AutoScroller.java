//Interface to be implemented by any Entities which are "AutoScroller"
//AutoScroller means the implementing Entity is moved by their respective
//scroll speed each time the game updates.
//Scrolling happens at a regular interval so long as the game is being played.
public interface AutoScroller {
     
    //Retrieve the distance the AutoScroller moves (in pixels) when scrolled
    public int getScrollSpeed();
    
    //Sets the scroll speed to the argument amount
    public void setScrollSpeed(int newSpeed);
        
    //Move the AutoScroller by its respective scroll speed
    public void scroll();

}
