package be.ibridge.kettle.test.newgrid;

/**
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Apr 2, 2003 by lgauthier@opnworks.com
 * 
 */

/**
 * Class used as a trivial case of a Task 
 * Serves as the business object for the TableViewer Example.
 * 
 * A Task has the following properties: completed, description,
 * owner and percentComplete 
 * 
 * @author Laurent
 *
 * 
 */
public class ExampleTask {

    private boolean completed   = false;
    private String description  = "";
    private String owner        = "?";
    private int percentComplete = 0;  

    /**
     * Create a task with an initial description
     * 
     * @param string
     */
    public ExampleTask(String string) {
        
        super();
        setDescription(string);
    }

    /**
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * @return String task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return String task owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @return int percent completed
     * 
     */
    public int getPercentComplete() {
        return percentComplete;
    }

    /**
     * Set the 'completed' property
     * 
     * @param b
     */
    public void setCompleted(boolean b) {
        completed = b;
    }

    /**
     * Set the 'description' property
     * 
     * @param string
     */
    public void setDescription(String string) {
        description = string;
    }

    /**
     * Set the 'owner' property
     * 
     * @param string
     */
    public void setOwner(String string) {
        owner = string;
    }

    /**
     * Set the 'percentComplete' property
     * 
     * @param i
     */
    public void setPercentComplete(int i) {
        percentComplete = i;
    }

}
