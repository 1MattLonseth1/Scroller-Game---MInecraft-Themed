import java.util.ArrayList;

//A collection of Entities to be drawn in the game window
public class DisplayList{
    
    //Collection of Entities to be drawn
    //the element at index 0 will be drawn UNDERNEATH all other Entities
    //the element at index size-1 will be drawn ON TOP of all other Entities
    private ArrayList<Entity> entities;
    
    
    public DisplayList(){
        entities = new ArrayList<Entity>();        
    }


    //retrieves the Entity at the requested index
    public Entity get(int idx){
        if (idx < 0 || idx >= entities.size())
            throw new IndexOutOfBoundsException("Cannot retrieve Entity at index: " + idx + 
                 " (entities size = " + entities.size() + ")");
        
        return entities.get(idx);
    }
    

    //appends argument Entity to the end of the DisplayList
    public void add(Entity toAdd){
        add(toAdd, entities.size());
    }

    //inserts the argument Entity at the specified index of the DisplayList
    public void add(Entity toAdd, int idx){
        if (idx < 0 || idx > entities.size())
            throw new IndexOutOfBoundsException("Cannot insert Entity at index: " + idx + 
                 " (entities size = " + entities.size() + ")");        
        entities.add(idx, toAdd);
    }    


    //retrieves the number of Entities in the DisplayList
    public int size(){
        return entities.size();
    }    
 


    //moves the Entity at the argument index back one index (ex: from index idx to idx - 1)
    public void moveBack(int idx){
        if (idx < 0 || idx >= entities.size())
            throw new IndexOutOfBoundsException("Cannot move Entity at index: " + idx + 
                 " (entities size = " + entities.size() + ")");
        if (idx != 0)  
            entities.add(idx-1, entities.remove(idx));
    }     


    //moves the Entity at the argument index forward one index (ex: from index idx to idx + 1)
    public void moveForward(int idx){
        if (idx < 0 || idx >= entities.size())
            throw new IndexOutOfBoundsException("Cannot move Entity at index: " + idx + 
                 " (entities size = " + entities.size() + ")");
        if (idx != entities.size()-1)  
            entities.add(idx-1, entities.remove(idx));
    }     


    //moves the Entity at the argument index to the start (i.e.: index 0) of the DisplayList
    public void moveToStart(int idx){
        if (idx < 0 || idx >= entities.size())
            throw new IndexOutOfBoundsException("Cannot move Entity at index: " + idx + 
                 " (entities size = " + entities.size() + ")");
        entities.add(0, entities.remove(idx));
    }      


    //moves the Entity at the argument index to the end (i.e.: index size-1) of the DisplayList
    public void moveToEnd(int idx){
        if (idx < 0 || idx >= entities.size())
            throw new IndexOutOfBoundsException("Cannot move Entity at index: " + idx + 
                 " (entities size = " + entities.size() + ")");
        entities.add(entities.remove(idx));
    }



    //removes any Entitys flagged to be garbage collected from the DisplayList
    public void performGC(){
        for (int i = 0; i < entities.size(); i++){
            if (entities.get(i).isFlaggedForGC()){
                entities.remove(i);
                i--;
            }
        }
    }

    
    public String toString(){
        return entities.toString();
    }
}
