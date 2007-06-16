package be.ibridge.kettle.test.newgrid;

/**
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Apr 2, 2003 by lgauthier@opnworks.com
 * 
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * Class that plays the role of the domain model in the TableViewerExample
 * In real life, this class would access a persistent store of some kind.
 * 
 */

public class ExampleTaskList {

    private final int COUNT = 10;
    private Vector tasks = new Vector(COUNT);
    private Set changeListeners = new HashSet();

    // Combo box choices
    static final String[] OWNERS_ARRAY = { "?", "Nancy", "Larry", "Joe" };
    
    /**
     * Constructor
     */
    public ExampleTaskList() {
        super();
        this.initData();
    }
    
    /*
     * Initialize the table data.
     * Create COUNT tasks and add them them to the 
     * collection of tasks
     */
    private void initData() {
        ExampleTask task;
        for (int i = 0; i < COUNT; i++) {
            task = new ExampleTask("Task "  + i);
            task.setOwner(OWNERS_ARRAY[i % 3]);
            tasks.add(task);
        }
    };

    /**
     * Return the array of owners   
     */
    public String[] getOwners() {
        return OWNERS_ARRAY;
    }
    
    /**
     * Return the collection of tasks
     */
    public Vector getTasks() {
        return tasks;
    }
    
    /**
     * Add a new task to the collection of tasks
     */
    public void addTask() {
        ExampleTask task = new ExampleTask("New task");
        tasks.add(tasks.size(), task);
        Iterator iterator = changeListeners.iterator();
        while (iterator.hasNext())
            ((ITaskListViewer) iterator.next()).addTask(task);
    }

    /**
     * @param task
     */
    public void removeTask(ExampleTask task) {
        tasks.remove(task);
        Iterator iterator = changeListeners.iterator();
        while (iterator.hasNext())
            ((ITaskListViewer) iterator.next()).removeTask(task);
    }

    /**
     * @param task
     */
    public void taskChanged(ExampleTask task) {
        Iterator iterator = changeListeners.iterator();
        while (iterator.hasNext())
            ((ITaskListViewer) iterator.next()).updateTask(task);
    }

    /**
     * @param viewer
     */
    public void removeChangeListener(ITaskListViewer viewer) {
        changeListeners.remove(viewer);
    }

    /**
     * @param viewer
     */
    public void addChangeListener(ITaskListViewer viewer) {
        changeListeners.add(viewer);
    }

}
