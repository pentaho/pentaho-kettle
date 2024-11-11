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
