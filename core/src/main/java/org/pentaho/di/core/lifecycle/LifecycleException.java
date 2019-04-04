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

package org.pentaho.di.core.lifecycle;

public class LifecycleException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Indicates an error that prevents the application from starting succesfully.
   */
  private boolean severe;

  /**
   *
   * @param severe
   *          true if Spoon should quit because of this exception
   */
  public LifecycleException( boolean severe ) {
    this.severe = severe;
  }

  /**
   *
   * @param message
   *          The (localized) message
   * @param severe
   *          true if Spoon should quit because of this exception
   */
  public LifecycleException( String message, boolean severe ) {
    super( message );
    this.severe = severe;
  }

  /**
   *
   * @param cause
   * @param severe
   *          true if Spoon should quit because of this exception
   */
  public LifecycleException( Throwable cause, boolean severe ) {
    super( cause );
    this.severe = severe;
  }

  /**
   *
   * @param message
   * @param cause
   * @param severe
   *          true if Spoon should quit because of this exception
   */
  public LifecycleException( String message, Throwable cause, boolean severe ) {
    super( message, cause );
    this.severe = severe;
  }

  /**
   *
   * @return true if Spoon should quit because of this exception
   */
  public boolean isSevere() {
    return severe;
  }

}
