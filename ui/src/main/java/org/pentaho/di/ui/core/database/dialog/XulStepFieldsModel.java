/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.core.database.dialog;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelList;

public class XulStepFieldsModel extends XulEventSourceAdapter {

  private String stepName;
  private FieldsCollection stepFields;

  public XulStepFieldsModel() {
    this.stepFields = new FieldsCollection();
  }

  public FieldsCollection getStepFields() {
    return this.stepFields;
  }

  public void setStepFields( FieldsCollection aStepFields ) {
    this.stepFields = aStepFields;
  }

  public String toString() {
    return "Step Fields Node";
  }

  public void setStepName( String aStepName ) {
    this.stepName = aStepName;
  }

  public String getStepName() {
    return this.stepName;
  }

  public void addStepField( StepFieldNode aStepField ) {
    this.stepFields.add( aStepField );
  }

  public static class FieldsCollection extends AbstractModelList<StepFieldNode> {
    private static final long serialVersionUID = -2489107137334871323L;
  }
}
