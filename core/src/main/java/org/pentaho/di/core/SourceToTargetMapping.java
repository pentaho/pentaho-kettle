/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
