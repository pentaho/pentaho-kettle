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
