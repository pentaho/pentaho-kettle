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
 * Outcome of one KTR run.
 *
 * @param success true when the KTR finished without errors.
 * @param detail  log or status text, shown when a test fails.
 */
record ExecutionResult( boolean success, String detail ) {
}
