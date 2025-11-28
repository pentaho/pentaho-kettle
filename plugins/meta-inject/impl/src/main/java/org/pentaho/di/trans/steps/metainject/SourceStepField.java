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


package org.pentaho.di.trans.steps.metainject;

import java.util.Objects;

public class SourceStepField {

  private String stepname;
  private String field;

  /**
   * @param stepname
   * @param field
   */
  public SourceStepField( String stepname, String field ) {
    this.stepname = stepname;
    this.field = field;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( !( obj instanceof SourceStepField ) ) {
      return false;
    }
    if ( obj == this ) {
      return true;
    }

    SourceStepField source = (SourceStepField) obj;
    return stepname.equalsIgnoreCase( source.getStepname() ) && field.equals( source.getField() );
  }

  @Override
  public int hashCode() {
    return Objects.hash(stepname, field);
  }

  /**
   * @return the stepname
   */
  public String getStepname() {
    return stepname;
  }

  /**
   * @param stepname
   *          the stepname to set
   */
  public void setStepname( String stepname ) {
    this.stepname = stepname;
  }

  /**
   * @return the field
   */
  public String getField() {
    return field;
  }

  /**
   * @param field
   *          the field to set
   */
  public void setField( String field ) {
    this.field = field;
  }

}
