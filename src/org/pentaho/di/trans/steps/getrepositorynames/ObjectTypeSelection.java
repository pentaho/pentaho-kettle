package org.pentaho.di.trans.steps.getrepositorynames;

import org.pentaho.di.i18n.BaseMessages;

public enum ObjectTypeSelection {
  
  Transformations(BaseMessages.getString("System.ObjectTypeSelection.Description.Transformations")),
  Jobs(BaseMessages.getString("System.ObjectTypeSelection.Description.Jobs")),
  All(BaseMessages.getString("System.ObjectTypeSelection.Description.All")),
  ;
  
  private String description;
  private ObjectTypeSelection(String description) {
    this.description = description;
  }
  public String getDescription() {
    return description;
  }
  
  public boolean areTransformationsSelected() {
    return this==Transformations || this==All; 
  }

  public boolean areJobsSelected() {
    return this==Jobs || this==All; 
  }

  public static ObjectTypeSelection getObjectTypeSelectionByDescription(String description) {
    for (ObjectTypeSelection selection : values()) {
      if (selection.getDescription().equalsIgnoreCase(description)) return selection;
    }
    return All;
  }
}