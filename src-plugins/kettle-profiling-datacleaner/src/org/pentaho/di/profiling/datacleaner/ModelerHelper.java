package org.pentaho.di.profiling.datacleaner;

import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.bootstrap.Bootstrap;
import org.eobjects.datacleaner.bootstrap.BootstrapOptions;
import org.eobjects.datacleaner.bootstrap.ExitActionListener;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
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

  private static StepMeta getCurrentStep() {
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

  public void openProfiler() {
    StepMeta stepMeta = getCurrentStep();
    if (stepMeta == null)
      return;
    // TODO: return;
  }

  @Override
  public void updateMenu(Document doc) {
    // TODO Auto-generated method stub

  }
}
