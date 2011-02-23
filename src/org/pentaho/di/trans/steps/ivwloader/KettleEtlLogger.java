package org.pentaho.di.trans.steps.ivwloader;

import org.pentaho.di.trans.step.BaseStep;

import com.infobright.logging.EtlLogger;

/**
 * Adapter between Ingres VectorWise EtlLogger and a Kettle BaseStep.
 *
 * @author geoffrey.falk@infobright.com
 */
public class KettleEtlLogger implements EtlLogger {

  private BaseStep step;
  
  public KettleEtlLogger(BaseStep step) {
    this.step = step;
  }
  
  //@Override
  public void debug(String s) {
    step.logDebug(s);
  }

  //@Override
  public void error(String s, Throwable cause) {
    step.logError(s + ": " + cause.getMessage());
  }

  //@Override
  public void error(String s) {
    step.logError(s);
  }

  //@Override
  public void info(String s) {
    step.logBasic(s);
  }

  //@Override
  public void trace(String s) {
    step.logRowlevel(s);
  }

  //@Override
  public void warn(String s) {
    step.logMinimal(s);
  }

  //@Override
  public void fatal(String s) {
    step.logError(s); // Kettle BaseStep does not have FATAL level 
  }

}
