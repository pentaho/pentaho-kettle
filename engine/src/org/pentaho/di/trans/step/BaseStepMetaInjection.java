package org.pentaho.di.trans.step;

public class BaseStepMetaInjection {

  protected StepInjectionMetaEntry createStepMetaInjectionEntry(StepMetaInjectionEnumEntry entry) {
    StepInjectionMetaEntry stepInjectionMetaEntry = new StepInjectionMetaEntry(entry.name(), entry.getValueType(), entry.getDescription());
    return stepInjectionMetaEntry;
  }
  
}
