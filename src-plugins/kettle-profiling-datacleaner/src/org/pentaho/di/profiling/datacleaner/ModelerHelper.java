package org.pentaho.di.profiling.datacleaner;

import java.awt.Image;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eobjects.analyzer.beans.BooleanAnalyzer;
import org.eobjects.analyzer.beans.DateAndTimeAnalyzer;
import org.eobjects.analyzer.beans.NumberAnalyzer;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.bootstrap.Bootstrap;
import org.eobjects.datacleaner.bootstrap.BootstrapOptions;
import org.eobjects.datacleaner.bootstrap.ExitActionListener;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.schema.Column;
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

  public void openProfiler() throws Exception {

    BootstrapOptions bootstrapOptions = new BootstrapOptions() {
      public Image getWelcomeImage() {
        return null;
      }

      public Datastore getSingleDatastore(DatastoreCatalog catalog) {
        return null;
      };

      public boolean isSingleDatastoreMode() {
        return false;
      }

      public boolean isCommandLineMode() {
        return false;
      }

      public ExitActionListener getExitActionListener() {
        return null;
      }

      public CliArguments getCommandLineArguments() {
        return null;
      }

      public void initializeSingleDatastoreJob(AnalysisJobBuilder arg0, DataContext arg1) {
      }
    };
    Bootstrap bootstrap = new Bootstrap(bootstrapOptions);
    bootstrap.run();

  }

  public void profileStep() throws Exception {

    final Spoon spoon = ((Spoon) SpoonFactory.getInstance());

    try {

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
      if (stepMeta == null) {
        return;
      }

      final String schemaTable = stepMeta.getName();

      // Now that we have the transformation and everything we can profile it...
      //
      final String url;
      final String driverClass;
      if (!Const.isEmpty(transMeta.getFilename())) {
        String filename = KettleVFS.getFileObject(transMeta.getFilename()).toString();
        url = "jdbc:kettle:" + filename;
        driverClass = "org.pentaho.di.jdbc.KettleDriver";

      } else {
        MessageBox box = new MessageBox(spoon.getShell(), SWT.ICON_ERROR | SWT.CLOSE);
        box.setText("TODO");
        box.setMessage("Ask the developers to implement profiling a transformation stored in a repository (lazy bunch, you might need to bribe them!).");
        box.open();
        return;
      }

      BootstrapOptions bootstrapOptions = new BootstrapOptions() {

        public Image getWelcomeImage() {
          return null;
        }

        public Datastore getSingleDatastore(DatastoreCatalog catalog) {
          return new JdbcDatastore(transMeta.getName(), url, driverClass);
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

        @Override
        public void initializeSingleDatastoreJob(AnalysisJobBuilder analysisJobBuilder, DataContext dataContext) {
          // add all columns of a table
          Column[] customerColumns = dataContext.getTableByQualifiedLabel(schemaTable).getColumns();
          analysisJobBuilder.addSourceColumns(customerColumns);

          List<InputColumn<?>> numberColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.NUMBER);
          if (!numberColumns.isEmpty()) {
            analysisJobBuilder.addAnalyzer(NumberAnalyzer.class).addInputColumns(numberColumns);
          }

          List<InputColumn<?>> dateColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.DATE);
          if (!dateColumns.isEmpty()) {
            analysisJobBuilder.addAnalyzer(DateAndTimeAnalyzer.class).addInputColumns(dateColumns);
          }

          List<InputColumn<?>> booleanColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.BOOLEAN);
          if (!booleanColumns.isEmpty()) {
            analysisJobBuilder.addAnalyzer(BooleanAnalyzer.class).addInputColumns(booleanColumns);
          }

          List<InputColumn<?>> stringColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.STRING);
          if (!stringColumns.isEmpty()) {
            analysisJobBuilder.addAnalyzer(StringAnalyzer.class).addInputColumns(stringColumns);
          }
        }
      };
      Bootstrap bootstrap = new Bootstrap(bootstrapOptions);
      bootstrap.run();

    } catch (final Exception e) {
      new ErrorDialog(spoon.getShell(), "Error", "unexpected error occurred", e);
    } finally {
      //
    }

  }

  @Override
  public void updateMenu(Document doc) {
    // TODO Auto-generated method stub

  }
}
