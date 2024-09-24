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

package org.pentaho.di.core;

public class SourceToTargetMapping {
  private int sourcePosition;
  private int targetPosition;

  /**
   * Creates a new source-to-target mapping
   *
   * @param sourcePosition
   * @param targetPosition
   */
  public SourceToTargetMapping( int sourcePosition, int targetPosition ) {
    this.sourcePosition = sourcePosition;
    this.targetPosition = targetPosition;
  }

  /**
   * @return Returns the sourcePosition.
   */
  public int getSourcePosition() {
    return sourcePosition;
  }

  /**
   * @param sourcePosition
   *          The sourcePosition to set.
   */
  public void setSourcePosition( int sourcePosition ) {
    this.sourcePosition = sourcePosition;
  }

  /**
   * @return Returns the targetPosition.
   */
  public int getTargetPosition() {
    return targetPosition;
  }

  /**
   * @param targetPosition
   *          The targetPosition to set.
   */
  public void setTargetPosition( int targetPosition ) {
    this.targetPosition = targetPosition;
  }

  public String getSourceString( String[] source ) {
    return source[sourcePosition];
  }

  public String getTargetString( String[] target ) {
    return target[targetPosition];
  }

}
