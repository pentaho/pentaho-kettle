package be.ibridge.kettle.test.newgrid;

/*
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Jun 11, 2003 by lgauthier@opnworks.com
 *
 */

public interface ITaskListViewer {
    
    /**
     * Update the view to reflect the fact that a task was added 
     * to the task list
     * 
     * @param task
     */
    public void addTask(ExampleTask task);
    
    /**
     * Update the view to reflect the fact that a task was removed 
     * from the task list
     * 
     * @param task
     */
    public void removeTask(ExampleTask task);
    
    /**
     * Update the view to reflect the fact that one of the tasks
     * was modified 
     * 
     * @param task
     */
    public void updateTask(ExampleTask task);
}
