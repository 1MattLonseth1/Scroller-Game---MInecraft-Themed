import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.net.URL;


//Handles drawing the game window and all of the graphics inside of it
//Also has the code to capture key presses/mouse clicks by the player
//Which ultimately are retrieved and reacted to by SSGEngine/SimpleGame classes












//************************************************************************************
//*                                                                                  *
//*                                                                                  *
//*                    YOU ARE NOT ALLOWED TO MODIFY THIS CLASS                      *
//*           YOU ALSO DON'T NEED TO READ/TRACE/CALL ANY METHODS IN HERE             *
//*         (But feel free to take a look if you're feeling extra curious!)          *
//*                                                                                  *
//*                                                                                  *
//************************************************************************************













public class Window extends JComponent implements KeyListener, MouseListener{
    
    
    //Constants related to game timing
    //target frames rendered per second
    private static final int TARGET_FPS = 60;  
    //number of nano seconds in 1 second
    private static final long NANOS_IN_SECOND = 1000000000; 
    //Ideal time (in nano secs) to wait between rendering frames per the target FPS
    private static final long TARGET_FRAMETIME = NANOS_IN_SECOND / TARGET_FPS;
    //The default game speed, represented as a percentage (100 = 100% speed)
    private static final int DEFAULT_GAME_SPEED = 100;
    
    
    //Manages the color and thickness of hitboxes when drawn (if debug mode enabled)
    private static final int BORDER_THICKNESS = 2;
    private static final Color HITBOX_COLOR = Color.MAGENTA;
    
    
    //Related to the color, size, and positioning of debug text, drawn when debug enabled.
    private static final int DEBUG_X_PADDING = 5;
    private static final int DEBUG_Y_PADDING = 20;
    //Text drawn before any user specified debug text
    private static final String DEBUG_PREFIX = "[DEBUG ENABLED] ";
    
    
    //Window object containing game content
    private static JFrame window;  
    
    //Collection of all entities to be drawn in the window on each refresh
    //Linked to the Collection of the associated SSGEngine object
    private DisplayList toDraw;    
    
    //A HashSet containing any keys pertaining to player movement
    //Used in handle keypress to allow for smooth, continuous movement
    private static HashSet<Integer> movementKeys;
    
    //A HashSet maintaining the key codes of all keys currently pressed
    //In order to ensure smooth movement, movement keys remain in the set until they are released
    //Non-movement keys are removed from the set after each call to getKeysPressed()
    private HashSet<Integer> keysCurrentlyPressed;
    //used by debug text to display last keyboard key pressed
    private KeyEvent debugLastKeyPressed = null;    
    
    //Current game speed, as percentage (ex: 150 = 150% speed, ie 50% faster than normal)
    private int gameSpeed = DEFAULT_GAME_SPEED;
    
    //Hashmap of images loaded into memory
    //Key = image file name, value = Image object to be rendered
    private HashMap<String, Image> loadedImages;
    
    //The filenames and Image objects of the currently drawn background/splash images
    private String bgImageFile, splashImageFile;
    private Image backgroundImage, splashImage;
    
    //Tracks the last mouse click
    private MouseEvent lastMouseEvent, debugLastMouseEvent;
    
    //Tracks the System.nanoTime() of the last completed render, used in the game timing
    private long lastPaintTime;
    
    //Label used to display debug text
    private JLabel debugLabel;

    //tracks if debug text is currently enabled or not
    private boolean isDebugEnabled;      
    
    //tracks if the window is currently painting, forcing the game logic to wait
    private static boolean isPainting;
    
    //tracks if the window is currently painting, forcing the game logic to wait
    private static boolean isFetchingKeys;

  
    
    
    
    
    
    public Window(int windowWidth, int windowHeight, DisplayList toDraw){
        this.toDraw = toDraw; //Link this toDraw to the respective SSGEngine
        
        initLogic();
        initWindow(windowWidth, windowHeight);
        initDebugText();
    }
    
    //Initialize Logical components of this Window
    private void initLogic(){
        this.loadedImages = new HashMap<String, Image>();
        movementKeys = new HashSet<Integer>();
        //Create a set of all the movement keys, as defined in SSGEngine
        for (int key : SSGEngine.MOVEMENT_KEYS)
            movementKeys.add(key);     
        keysCurrentlyPressed = new HashSet<Integer>();
        lastPaintTime = System.nanoTime();
        isPainting = false;
        isFetchingKeys = false;
        //No background or splash image to start with
        bgImageFile = null;
        splashImageFile = null;
    }
    
    //Initializes the game window
    private void initWindow(int windowWidth, int windowHeight){                
        window = new JFrame("Super Scroller Game!");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(windowWidth, windowHeight);
        window.add(this);
        
        
        //Fix Windows listener bug(?)
        this.setFocusable(true);
        //window.requestFocus();
        this.requestFocusInWindow();
        
        
        this.setOpaque(true);
        window.setVisible(true);
        window.setResizable(false);
        
        
        //Lets the window know that the methods to react to keyboard/mouse actions
        //are implemented in this class        
        addKeyListener(this);
        addMouseListener(this);  
    }
    
    //Initialize the debug text (only drawn when Debug Mode enabled)
    private void initDebugText(){
        debugLabel = new JLabel(DEBUG_PREFIX);
        debugLabel.setBounds(DEBUG_X_PADDING, 0, this.getWidth(), DEBUG_Y_PADDING);
        debugLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        debugLabel.setForeground(Color.MAGENTA);
        Font debugFont = null;
        /* Possible font stutter fix??! 
        //Nevermind... fixed this elsewhere by reorderign some of the rendering tasks....
         try{
         debugFont = Font.createFont(Font.TRUETYPE_FONT, new File(getClass().getResource(DEBUG_FONT_FILE).getFile()));
         }
         catch (FontFormatException | IOException e){
         // System.err.println("Could not open debug font file at: " + DEBUG_FONT_FILE);
         }
         */
        debugLabel.setFont(debugFont);
        debugLabel.setText("");
        debugLabel.setVisible(true);
        this.isDebugEnabled = false;
        add(debugLabel);    
    }
    
    
    //Called by SSGEngine to redraw the window and all of the Entities in it   
    public void refresh(){
        this.repaint();
        this.adjustFrameTiming();
    }
    
    //called once the game ends
    public void endGame(){
        //Keep repainting the window to show any last changes in splash screen/etc
        while(true)
            this.refresh();
    }
    
    //Called everytime the game window is "repainted"
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(getForeground());
        
        
        //set isPainting to true to ensure logic stops updating until painting is complete
        isPainting = true; 
        //Make sure all the graphics that need to be drawn are loaded into memory
        ensureImagesLoaded(); 
        
        drawBackground(g);//first draw the background
        drawEntities(g);//then the entities ontop of that
        if (isDebugEnabled()){ //if debug mode is on, draw hitboxes and update debug text
            drawHitboxes(g);
            updateDebugText();
        }
        drawSplash(g);//then the splash screen if enabled               
        
        //now we're done repainting and the game logic can update!
        isPainting = false;
    }
    
    //Draws all the Entities in the display list to the game window
    private void drawEntities(Graphics g){
        for (int i = 0; i < toDraw.size(); i++){
            Entity e = toDraw.get(i);
            try{
                Image img = loadedImages.get(e.getImg());
                //Draw each entity per its image name, coordinates, and dimensions
                g.drawImage(img, e.getX(), e.getY(), e.getWidth(), e.getHeight(), null); 
            }
            catch(RuntimeException re){
                //just in case... since students will be modifying the DisplayList 
            }
        }
    }
    
    //Ensures that the images for every Entity in the DisplayList
    //have been loaded into memory
    private void ensureImagesLoaded(){
        for (int i = 0; i < toDraw.size(); i++){
            Entity e = toDraw.get(i);
            try{
                String imageName = e.getImg();
                if (imageName != null && !loadedImages.containsKey(imageName))
                    loadedImages.put(imageName, Window.readImage(imageName));
            }
            catch(RuntimeException re){
                //just in case... since students will be modifying the arraylist 
            }           
        }        
    }
    
    //Can be used to preemptively load a collection of image files
    public void preloadImages(ArrayList<String> imageFileNames){
        for (int i = 0; i < imageFileNames.size(); i++){
            try{
                String fname = imageFileNames.get(i);
                loadedImages.put(fname, Window.readImage(fname));  
            }
            catch(RuntimeException re){
                //just in case... since students will be modifying the DisplayList 
            }
        }
    }
    
    //Draws the current background image underneath everything else in the game window
    private void drawBackground(Graphics g){
        //checks if there's currently a background image set
        //Graphics2D g2D = (Graphics2D) g;
        // RectangularShape backgroundColor = new Rectangle2D.Double(e.getX(), e.getY(), e.getHeight(), e.getWidth());
        // g2D.draw(backgroundColor);
        if (backgroundImage != null)
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(),null);        
    }
    
    //Draws the current splash image ontop of everything else in the game window
    private void drawSplash(Graphics g){  
        //checks if there's currently a splash image set
        if (splashImage != null)
            g.drawImage(splashImage, 0, 0, this.getWidth(), this.getHeight(),null);
    }
    
    //Draws the hitbox around all of the images currently in the game window
    //Only occurs if debug mode is enabled
    private void drawHitboxes(Graphics g){ 
        Graphics2D g2D = (Graphics2D) g;
        g2D.setStroke(new BasicStroke(BORDER_THICKNESS));
        g2D.setPaint(HITBOX_COLOR);
        
        for (int i = 0; i < toDraw.size(); i++){
            Entity e = toDraw.get(i);
            try{
                //Draw hitboxes per each entity's current coordinates/dimensions
                RectangularShape hitbox = new Rectangle2D.Double(e.getX(), e.getY(), e.getWidth(), e.getHeight());
                g2D.draw(hitbox);      
            }
            catch(RuntimeException re){
                //just in case... since students will be modifying the DisplayList 
                
            }
        }
    }  
    
    //Updates the debug text displayed when debug mode is enabled
    public void updateDebugText(){
        StringBuilder debugText = new StringBuilder(DEBUG_PREFIX + "DisplayList Size: " + toDraw.size());
        debugText.append(", Game Speed: " + this.gameSpeed + "%"); 
        debugText.append(", Last Key Press: ");
        if (debugLastKeyPressed == null)
            debugText.append(" null ");
        else
            debugText.append(KeyEvent.getKeyText(debugLastKeyPressed.getKeyCode()));
        debugText.append(", Last Mouse Click: "); 
        if (debugLastMouseEvent == null)
            debugText.append("null"); 
        else{
            debugText.append("(x: " + debugLastMouseEvent.getX() + " y:" + debugLastMouseEvent.getY() + ")");
        }
        debugLabel.setText(debugText.toString());    
                   
    }
    
    //Adjusts the game timing to ensure that it doesn't run too fast or slow
    //Makes sure the game maintains its target frames per second (FPS)
    //1 frame = 1 "redraw" of the game window
    public void adjustFrameTiming(){
        //Wait for the game to finish painting
        while(isPainting){
            sleep(100);
        }
        //Figure out how much longer we need to wait to hit our target frametime
        //ex: if we want 60 fps, we want to make sure there's a 1/60 second delay
        //between each frame painted
        long target = (TARGET_FRAMETIME * 100 / gameSpeed);
        long delta = System.nanoTime() - lastPaintTime;
        long delayNeeded = (target - delta);
        sleep(delayNeeded);
        
        //log the current system time for the next frame's timing
        lastPaintTime = System.nanoTime(); 
    }    
    
    
    //Forces the execution of the program to stop and wait
    //For the specified number of nano seconds
    private void sleep(long nanosToSleep){
        if (nanosToSleep <= 0)
            return;
        long start = System.nanoTime();
        while (System.nanoTime() - start < nanosToSleep){
            try {
                Thread.sleep(0, 5000);
            } 
            catch(Exception e) { 
                //shouldn't ever reach here, but try/catch is necessary due to 
                //Java's implementation of Thread.sleep function
            }
        }
    }      
    
    //Toggles  the current state of Debug mode
    public void setDebugMode(boolean isDebugEnabled){
        this.isDebugEnabled = isDebugEnabled;
        //Debug label is always visible and being drawn (to prevent rendering stutter),
        //so when debug mode is disabled, this text is just set to an empty String.
        if (!this.isDebugEnabled)
            this.debugLabel.setText("");
    }
    
    //The game speed is represented as integer indicating a percentage.
    //a value of 100 indicates the game is running at 100% speed, ie the default game speed.
    //150 and 50 would indicate game running 50% faster and 50% slower, respectively.    
    public boolean isDebugEnabled(){
        return this.isDebugEnabled;
    }
    

    //Collects and Sets the current game speed
    //The game speed is represented as integer indicating a percentage.
    //a value of 100 indicates the game is running at 100% speed, ie the default game speed.
    //150 and 50 would indicate game running 50% faster and 50% slower, respectively.    
    public int getUniversalSpeed(){
        return gameSpeed;
    }
    
    public void setUniversalSpeed(int newGameSpeed){
        this.gameSpeed = newGameSpeed;
    }
    
    
    //Sets and retrieves the current background Color
    //This background color won't be seen if there is a bg image (unless it has transparency)
    public void setBackgroundColor(Color c){
        this.setBackground(c);
    }
    
    public Color getBackgroundColor(){
        return this.getBackground();
    }
    
    
    //Called automatically whenever the mouse is clicked inside the game window
    //Updates the data of the last mouse event
    public void mousePressed(MouseEvent event) { 
        lastMouseEvent = event;
        //seperate variable used for debug text
        debugLastMouseEvent = event;
    }   
    
    //Returns the data of the last mouse click, 
    //or null if the mouse wasn't clicked since last check
    public MouseEvent getLastMousePress(){
        MouseEvent toReturn = lastMouseEvent;
        //Set lastMouseEvent to null for subsequent calls to getLastMousePress
        //to ensure that a mouse click isn't registered multiple times.
        lastMouseEvent = null;
        return toReturn;
    }        
    
    //Returns a collection of all keys currently being pressed down
    //Non-Movement keys are removed so they aren't re-registered on subsequent checks
    //Movement keys only removed from keysPressed Set when they are released on the keyboard
    public Collection<Integer> getKeysPressed(){
        isFetchingKeys = true;
        try{
            ArrayList<Integer> toReturn = new ArrayList<Integer>(keysCurrentlyPressed);
            HashSet<Integer> onlyMovementKeys = new HashSet<Integer>();
            //create a copy of what's currently pressed to avoid concurrent modification
            HashSet<Integer> currentPressCopy = new HashSet<Integer>(keysCurrentlyPressed);
            isFetchingKeys = false;
            //Remove any non-movement keys from the Set of keys currently pushed down
            //so they are not registered on subsequent checks by the game
            //Movement-specific keys remain in the Set to ensure smooth/reactive movement
            for (Integer key : currentPressCopy){
                if (movementKeys.contains(key))
                    onlyMovementKeys.add(key);
            }
            keysCurrentlyPressed = onlyMovementKeys;
            return toReturn;
        }
        catch(RuntimeException re){            
            //just in case we get a one-in-a-million race condition between retrieving/fetching keys
            isFetchingKeys=false;
            return new ArrayList<Integer>();
        }
    }
    
    //Called automatically whenever a key on the keyboard is pushed down/released  
    public void keyPressed(KeyEvent event) { 
        //Wait for the game to finish retrieving keys pressed
        while(isFetchingKeys){
            sleep(100);
        }        
        keysCurrentlyPressed.add(event.getKeyCode());
        debugLastKeyPressed = event;
        int keyCode = event.getKeyCode();
        if (keyCode == SSGEngine.KEY_QUIT_GAME)
            System.exit(0);    
        else if (keyCode == SSGEngine.KEY_TOGGLE_DEBUG)
            this.setDebugMode(!this.isDebugEnabled());           
    }
    
    public void keyReleased(KeyEvent event) { 
        keysCurrentlyPressed.remove(event.getKeyCode());
    }    
    
    
    
    //Collect and set current background image
    //bgImageFile is null if no background image is set
    public void setBackgroundImg(String filename){
        bgImageFile = filename;
        backgroundImage = Window.readImage(bgImageFile);             
    }
    
    public String getBackgroundImg(){
        return bgImageFile;
    }
    
    
    //Collect and set current splash image
    //splashImageFile is null if no splash image is set    
    public void setSplashImg(String filename){
        splashImage = Window.readImage(filename);  
        splashImageFile = filename;
    }
    
    public String getSplashImg(){
        return splashImageFile;
    }    
    
    
    //Loads an image into memory -- returns the Image object
    public static Image readImage(String filename){
        if (filename == null)
            return null;
        //need to use URL to allow for animated gifs to animate properly
        URL imageResource = Window.class.getResource(filename);
        if (imageResource == null){
            System.err.println("Unable to read image file: " + filename); 
            return null;
        }
        return new ImageIcon(imageResource).getImage(); 
    }
    
    //Sets the title text on the top of the game window
    public void setTitle(String text){
        window.setTitle(text);
    }
    
    
    
    
    
    
    
    
    
    //These functions are required by various interfaces, but are not used
    public void mouseReleased(MouseEvent event) { }
    public void mouseClicked(MouseEvent event) { }
    public void mouseEntered(MouseEvent event) { }
    public void mouseExited(MouseEvent event) { }
    public void mouseMoved(MouseEvent event) { }
    public void keyTyped(KeyEvent event) {}    
    
    
}
