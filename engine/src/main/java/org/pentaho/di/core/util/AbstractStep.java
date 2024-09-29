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


package org.pentaho.di.core.util;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 *
 */
public abstract class AbstractStep extends BaseStep {

  /**
   * Constant for unexpected error.
   */
  public static final String UNEXPECTED_ERROR = "Unexpected error";

  public static final long DEFAULT_ERROR_CODE = 1L;

  /**
   * Constructor.
   *
   * @param stepMeta
   *          the stepMeta.
   * @param stepDataInterface
   *          the stepDataInterface.
   * @param copyNr
   *          the copyNr.
   * @param transMeta
   *          the transMeta.
   * @param trans
   *          the transaction.
   */
  public AbstractStep( final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int copyNr,
    final TransMeta transMeta, final Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Log exception.
   *
   * @param exception
   *          exception to log.
   */
  public void logUnexpectedError( final Throwable exception ) {
    this.logError( UNEXPECTED_ERROR, exception );
  }

  /**
   * Set default error code.
   */
  public void setDefaultError() {
    this.setErrors( DEFAULT_ERROR_CODE );
  }

}
