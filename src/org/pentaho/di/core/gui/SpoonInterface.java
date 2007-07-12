package org.pentaho.di.core.gui;

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.spoon.Messages;
import org.pentaho.di.trans.TransMeta;

public interface SpoonInterface {

	public static final String STRING_TRANSFORMATIONS = Messages.getString("Spoon.STRING_TRANSFORMATIONS"); // Transformations
    public static final String STRING_JOBS            = Messages.getString("Spoon.STRING_JOBS");            // Jobs
    public static final String STRING_BUILDING_BLOCKS = Messages.getString("Spoon.STRING_BUILDING_BLOCKS"); // Building blocks
    public static final String STRING_ELEMENTS        = Messages.getString("Spoon.STRING_ELEMENTS");        // Model elements
    public static final String STRING_CONNECTIONS     = Messages.getString("Spoon.STRING_CONNECTIONS");     // Connections
    public static final String STRING_STEPS           = Messages.getString("Spoon.STRING_STEPS");           // Steps
    public static final String STRING_JOB_ENTRIES     = Messages.getString("Spoon.STRING_JOB_ENTRIES");     // Job entries
    public static final String STRING_HOPS            = Messages.getString("Spoon.STRING_HOPS");            // Hops
    public static final String STRING_PARTITIONS      = Messages.getString("Spoon.STRING_PARTITIONS");      // Database Partition schemas
    public static final String STRING_SLAVES          = Messages.getString("Spoon.STRING_SLAVES");          // Slave servers
    public static final String STRING_CLUSTERS        = Messages.getString("Spoon.STRING_CLUSTERS");        // Cluster Schemas
    public static final String STRING_TRANS_BASE      = Messages.getString("Spoon.STRING_BASE");            // Base step types
    public static final String STRING_JOB_BASE        = Messages.getString("Spoon.STRING_JOBENTRY_BASE");   // Base job entry types
    public static final String STRING_HISTORY         = Messages.getString("Spoon.STRING_HISTORY");         // Step creation history

    public static final String STRING_TRANS_NO_NAME   = Messages.getString("Spoon.STRING_TRANS_NO_NAME");   // <unnamed transformation>
    public static final String STRING_JOB_NO_NAME     = Messages.getString("Spoon.STRING_JOB_NO_NAME");     // <unnamed job>

    public static final String STRING_TRANSFORMATION  = Messages.getString("Spoon.STRING_TRANSFORMATION");  // Transformation
    public static final String STRING_JOB             = Messages.getString("Spoon.STRING_JOB");             // Job

    
    public static final int STATE_CORE_OBJECTS_NONE     = 1;   // No core objects
    public static final int STATE_CORE_OBJECTS_CHEF     = 2;   // Chef state: job entries
    public static final int STATE_CORE_OBJECTS_SPOON    = 3;   // Spoon state: steps

	public static final String XUL_FILE_MENUBAR = "ui/menubar.xul";

	public static final String XUL_FILE_MENUS = "ui/menus.xul";

	public static final String XUL_FILE_MENU_PROPERTIES = "ui/menubar.properties";
	
    public boolean addSpoonBrowser(String name, String urlString);

    public void addTransGraph(TransMeta transMeta);

    public Object[] messageDialogWithToggle( String dialogTitle, Image image, String message, int dialogImageType, String buttonLabels[], int defaultIndex, String toggleMessage, boolean toggleState );


}
