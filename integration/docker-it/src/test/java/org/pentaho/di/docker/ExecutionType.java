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
 * Execution modes. PAN runs Pan in the PDI container. CARTE runs Carte in the PDI container. SERVER runs the KTR
 * through the Carte endpoint of the Pentaho Server container.
 */
public enum ExecutionType {
  PAN,
  CARTE,
  SERVER
}
