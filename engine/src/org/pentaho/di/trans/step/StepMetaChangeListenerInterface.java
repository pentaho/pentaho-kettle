package org.pentaho.di.trans.step;

import org.pentaho.di.trans.TransMeta;

public interface StepMetaChangeListenerInterface {
  /**
   * 
   * 
   */
  public void onStepChange(TransMeta transMeta, StepMeta oldMeta, StepMeta newMeta);

  public StepMetaChangeListenerInterface clone();

  public Object getStepMeta();
  
}
