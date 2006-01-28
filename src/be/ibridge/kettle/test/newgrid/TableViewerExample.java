package be.ibridge.kettle.test.newgrid;

/**
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Apr 2, 2003 by lgauthier@opnworks.com
 * 
 */

import java.util.Arrays;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * The TableViewerExample class is meant to be a fairly complete example
 * of the use of the org.eclipse.jface.viewers.TableViewer class to 
 * implement an editable table with text, combobox and image 
 * editors. 
 * 
 * The example application metaphor consists of a table to view and 
 * edit tasks in a task list. It is by no means a complete or truly 
 * usable application.
 * 
 * This example draws from sample code in the Eclipse
 * org.eclipse.ui.views.tasklist.TaskList class and some sample code 
 * in SWT fragments from the eclipse.org web site. 
 * 
 * Known issue: We were not able to get the images to be center aligned
 * in the checkbox column. 
 * 
 * @author Laurent Gauthier
 * @since Apr 2, 2003  
 */

public class TableViewerExample
{
    /**
     * @param parent
     */
    public TableViewerExample(Composite parent)
    {

        this.addChildControls(parent);
    }

    //  private Shell shell;
    private Table           table;

    private TableViewer     tableViewer;

    private Button          closeButton;

    // Create a ExampleTaskList and assign it to an instance variable
    private ExampleTaskList taskList           = new ExampleTaskList();

    // Set the table column property names
    private final String    COMPLETED_COLUMN   = "completed";

    private final String    DESCRIPTION_COLUMN = "description";

    private final String    OWNER_COLUMN       = "owner";

    private final String    PERCENT_COLUMN     = "percent";

    // Set column names
    private String[]        columnNames        = new String[] { COMPLETED_COLUMN, DESCRIPTION_COLUMN, OWNER_COLUMN, PERCENT_COLUMN };

    /**
     * Main method to launch the window 
     */
    public static void main(String[] args)
    {

        Shell shell = new Shell();
        shell.setText("Task List - TableViewer Example");

        // Set layout for shell
        GridLayout layout = new GridLayout();
        shell.setLayout(layout);

        // Create a composite to hold the children
        Composite composite = new Composite(shell, SWT.NONE);
        final TableViewerExample tableViewerExample = new TableViewerExample(composite);

        tableViewerExample.getControl().addDisposeListener(new DisposeListener()
        {

            public void widgetDisposed(DisposeEvent e)
            {
                tableViewerExample.dispose();
            }

        });

        // Ask the shell to display its content
        shell.open();
        tableViewerExample.run(shell);
    }

    /**
     * Run and wait for a close event
     * @param shell Instance of Shell
     */
    public void run(Shell shell)
    {
        // Add a listener for the close button
        closeButton.addSelectionListener(new SelectionAdapter()
        {
            // Close the view i.e. dispose of the composite's parent
            public void widgetSelected(SelectionEvent e)
            {
                table.getParent().getParent().dispose();
            }
        });

        Display display = shell.getDisplay();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    /**
     * Release resources
     */
    public void dispose()
    {

        // Tell the label provider to release its ressources
        tableViewer.getLabelProvider().dispose();
    }

    /**
     * Create a new shell, add the widgets, open the shell
     * @return the shell that was created    
     */
    private void addChildControls(Composite composite)
    {

        // Create a composite to hold the children
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH);
        composite.setLayoutData(gridData);

        // Set numColumns to 3 for the buttons 
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 4;
        composite.setLayout(layout);

        // Create the table 
        createTable(composite);

        // Create and setup the TableViewer
        createTableViewer();
        tableViewer.setContentProvider(new ExampleContentProvider());
        tableViewer.setLabelProvider(new ExampleLabelProvider());
        // The input for the table viewer is the instance of ExampleTaskList
        taskList = new ExampleTaskList();
        tableViewer.setInput(taskList);

        // Add the buttons
        createButtons(composite);
    }

    /**
     * Create the Table
     */
    private void createTable(Composite parent)
    {
        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

        table = new Table(parent, style);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = 3;
        table.setLayoutData(gridData);

        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        // 1st column with image/checkboxes - NOTE: The SWT.CENTER has no effect!!
        TableColumn column = new TableColumn(table, SWT.CENTER, 0);
        column.setText("!");
        column.setWidth(20);

        // 2nd column with task Description
        column = new TableColumn(table, SWT.LEFT, 1);
        column.setText("Description");
        column.setWidth(400);
        // Add listener to column so tasks are sorted by description when clicked 
        column.addSelectionListener(new SelectionAdapter()
        {

            public void widgetSelected(SelectionEvent e)
            {
                tableViewer.setSorter(new ExampleTaskSorter(ExampleTaskSorter.DESCRIPTION));
            }
        });

        // 3rd column with task Owner
        column = new TableColumn(table, SWT.LEFT, 2);
        column.setText("Owner");
        column.setWidth(100);
        // Add listener to column so tasks are sorted by owner when clicked
        column.addSelectionListener(new SelectionAdapter()
        {

            public void widgetSelected(SelectionEvent e)
            {
                tableViewer.setSorter(new ExampleTaskSorter(ExampleTaskSorter.OWNER));
            }
        });

        // 4th column with task PercentComplete 
        column = new TableColumn(table, SWT.CENTER, 3);
        column.setText("% Complete");
        column.setWidth(80);
        //  Add listener to column so tasks are sorted by percent when clicked
        column.addSelectionListener(new SelectionAdapter()
        {

            public void widgetSelected(SelectionEvent e)
            {
                tableViewer.setSorter(new ExampleTaskSorter(ExampleTaskSorter.PERCENT_COMPLETE));
            }
        });
    }

    /**
     * Create the TableViewer 
     */
    private void createTableViewer()
    {

        tableViewer = new TableViewer(table);
        tableViewer.setUseHashlookup(true);

        tableViewer.setColumnProperties(columnNames);

        // Create the cell editors
        CellEditor[] editors = new CellEditor[columnNames.length];

        // Column 1 : Completed (Checkbox)
        editors[0] = new CheckboxCellEditor(table);

        // Column 2 : Description (Free text)
        TextCellEditor textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).setTextLimit(60);
        editors[1] = textEditor;

        // Column 3 : Owner (Combo Box) 
        editors[2] = new ComboBoxCellEditor(table, taskList.getOwners(), SWT.READ_ONLY);

        // Column 4 : Percent complete (Text with digits only)
        textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).addVerifyListener(

        new VerifyListener()
        {
            public void verifyText(VerifyEvent e)
            {
                // Here, we could use a RegExp such as the following 
                // if using JRE1.4 such as  e.doit = e.text.matches("[\\-0-9]*");
                e.doit = "0123456789".indexOf(e.text) >= 0;
            }
        });
        editors[3] = textEditor;

        // Assign the cell editors to the viewer 
        tableViewer.setCellEditors(editors);
        // Set the cell modifier for the viewer
        tableViewer.setCellModifier(new ExampleCellModifier(this));
        // Set the default sorter for the viewer 
        tableViewer.setSorter(new ExampleTaskSorter(ExampleTaskSorter.DESCRIPTION));
    }

    /*
     * Close the window and dispose of resources
     */
    public void close()
    {
        Shell shell = table.getShell();

        if (shell != null && !shell.isDisposed()) shell.dispose();
    }

    /**
     * InnerClass that acts as a proxy for the ExampleTaskList 
     * providing content for the Table. It implements the ITaskListViewer 
     * interface since it must register changeListeners with the 
     * ExampleTaskList 
     */
    class ExampleContentProvider implements IStructuredContentProvider, ITaskListViewer
    {
        public void inputChanged(Viewer v, Object oldInput, Object newInput)
        {
            if (newInput != null) ((ExampleTaskList) newInput).addChangeListener(this);
            if (oldInput != null) ((ExampleTaskList) oldInput).removeChangeListener(this);
        }

        public void dispose()
        {
            taskList.removeChangeListener(this);
        }

        // Return the tasks as an array of Objects
        public Object[] getElements(Object parent)
        {
            return taskList.getTasks().toArray();
        }

        /* (non-Javadoc)
         * @see ITaskListViewer#addTask(ExampleTask)
         */
        public void addTask(ExampleTask task)
        {
            tableViewer.add(task);
        }

        /* (non-Javadoc)
         * @see ITaskListViewer#removeTask(ExampleTask)
         */
        public void removeTask(ExampleTask task)
        {
            tableViewer.remove(task);
        }

        /* (non-Javadoc)
         * @see ITaskListViewer#updateTask(ExampleTask)
         */
        public void updateTask(ExampleTask task)
        {
            tableViewer.update(task, null);
        }
    }

    /**
     * Return the array of choices for a multiple choice cell
     */
    public String[] getChoices(String property)
    {
        if (OWNER_COLUMN.equals(property))
            return taskList.getOwners(); // The ExampleTaskList knows about the choice of owners
        else
            return new String[] {};
    }

    /**
     * Add the "Add", "Delete" and "Close" buttons
     * @param parent the parent composite
     */
    private void createButtons(Composite parent)
    {

        // Create and configure the "Add" button
        Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
        add.setText("Add");

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = 80;
        add.setLayoutData(gridData);
        add.addSelectionListener(new SelectionAdapter()
        {

            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e)
            {
                taskList.addTask();
            }
        });

        //  Create and configure the "Delete" button
        Button delete = new Button(parent, SWT.PUSH | SWT.CENTER);
        delete.setText("Delete");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = 80;
        delete.setLayoutData(gridData);

        delete.addSelectionListener(new SelectionAdapter()
        {

            //  Remove the selection and refresh the view
            public void widgetSelected(SelectionEvent e)
            {
                ExampleTask task = (ExampleTask) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                if (task != null)
                {
                    taskList.removeTask(task);
                }
            }
        });

        //  Create and configure the "Close" button
        closeButton = new Button(parent, SWT.PUSH | SWT.CENTER);
        closeButton.setText("Close");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.widthHint = 80;
        closeButton.setLayoutData(gridData);
    }

    /**
     * Return the column names in a collection
     * 
     * @return List  containing column names
     */
    public java.util.List getColumnNames()
    {
        return Arrays.asList(columnNames);
    }

    /**
     * @return currently selected item
     */
    public ISelection getSelection()
    {
        return tableViewer.getSelection();
    }

    /**
     * Return the ExampleTaskList
     */
    public ExampleTaskList getTaskList()
    {
        return taskList;
    }

    /**
     * Return the parent composite
     */
    public Control getControl()
    {
        return table.getParent();
    }

    /**
     * Return the 'close' Button
     */
    public Button getCloseButton()
    {
        return closeButton;
    }
}
