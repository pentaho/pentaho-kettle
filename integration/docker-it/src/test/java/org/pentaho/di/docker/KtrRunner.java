/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.docker;

/**
 * Runs one KTR in one execution mode.
 */
interface KtrRunner {

  /**
   * @param ktr the case. It gives the name for the run.
   * @param xml the KTR XML to run. It can differ from the file when the case rewrites endpoints.
   */
  ExecutionResult run( KtrCase ktr, byte[] xml ) throws Exception;
}
