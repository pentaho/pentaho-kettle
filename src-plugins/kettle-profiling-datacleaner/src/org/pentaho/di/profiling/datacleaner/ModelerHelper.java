package org.pentaho.di.profiling.datacleaner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.bootstrap.Bootstrap;
import org.eobjects.datacleaner.bootstrap.BootstrapOptions;
import org.eobjects.datacleaner.bootstrap.ExitActionListener;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.ISpoonMenuController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class ModelerHelper extends AbstractXulEventHandler implements ISpoonMenuController {

  private static ModelerHelper instance = null;

  // private static Logger logger =
  // LoggerFactory.getLogger(ModelerHelper.class);

  private ModelerHelper() {
  }

  public static ModelerHelper getInstance() {
    if (instance == null) {
      instance = new ModelerHelper();
      Spoon spoon = ((Spoon) SpoonFactory.getInstance());
      spoon.addSpoonMenuController(instance);
    }
    return instance;
  }

  /**
   * this method is used to see if a valid TableOutput step has been selected in
   * a trans graph before attempting to model or quick vis
   * 
   * @return true if valid
   */
  public static boolean isValidStepSelected() {
    StepMeta stepMeta = getCurrentStep();
    if (stepMeta == null) {
      return false;
    }

    if (stepMeta.getStepMetaInterface() instanceof CsvInputMeta)
      return true;

    return false;
  }

  public static StepMeta getCurrentStep() {
    Spoon spoon = ((Spoon) SpoonFactory.getInstance());
    TransMeta transMeta = spoon.getActiveTransformation();
    if (transMeta == null || spoon.getActiveTransGraph() == null) {
      return null;
    }
    StepMeta stepMeta = spoon.getActiveTransGraph().getCurrentStep();
    return stepMeta;
  }

  public String getName() {
    return "profiler"; //$NON-NLS-1$
  }

  public void profileStep() {
    Spoon spoon = ((Spoon) SpoonFactory.getInstance());
    final TransMeta transMeta = spoon.getActiveTransformation();
    if (transMeta == null || spoon.getActiveTransGraph() == null) {
      return;
    }
    if (transMeta.hasChanged()) {
      MessageBox box = new MessageBox(spoon.getShell(), SWT.ICON_ERROR | SWT.CLOSE);
      box.setText("Save first...");
      box.setMessage("Please save your transformation first.  To run the profiling job, we need to run the transformation.");
      box.open();
      return;
    }
    
    StepMeta stepMeta = spoon.getActiveTransGraph().getCurrentStep();
    if (stepMeta==null) {
      return;
    }
    
    try {
      // Now that we have the transformation and everything we can profile it...
      //
      final String url;
      final String driverClass;
      if (!Const.isEmpty(transMeta.getFilename())) {
        String filename = KettleVFS.getFileObject(transMeta.getFilename()).toString();
        url = "jdbc:kettle:"+filename;
        driverClass = "org.pentaho.di.jdbc.KettleDriver";
        
      } else {
        MessageBox box = new MessageBox(spoon.getShell(), SWT.ICON_ERROR | SWT.CLOSE);
        box.setText("TODO");
        box.setMessage("Ask the developers to implement profiling a transformation stored in a repository (lazy bunch, you might need to bribe them!).");
        box.open();
        return;
      }
      
      BootstrapOptions bootstrapOptions = new BootstrapOptions() {

        public Datastore getSingleDatastore(DatastoreCatalog catalog) {
            return new JdbcDatastore(transMeta.getName(), 
                url, 
                driverClass, 
                "", 
                ""
              );
        };

        @Override
        public boolean isSingleDatastoreMode() {
          return true;
        }

        @Override
        public boolean isCommandLineMode() {
          return false;
        }

        @Override
        public ExitActionListener getExitActionListener() {
          return null;
        }

        @Override
        public CliArguments getCommandLineArguments() {
          // TODO Auto-generated method stub
          return null;
        }
      };
      Bootstrap bootstrap = new Bootstrap(bootstrapOptions);
      bootstrap.run();

      
    } catch(Exception e) {
      new ErrorDialog(spoon.getShell(), "Error", "Error profiling:", e);
    }
    
  }

  @Override
  public void updateMenu(Document doc) {
    // TODO Auto-generated method stub

  }
}
