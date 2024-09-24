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

package org.pentaho.di.job;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;

public class DelegationAdapter implements DelegationListener {

  @Override
  public void jobDelegationStarted( Job delegatedJob, JobExecutionConfiguration jobExecutionConfiguration ) {
  }

  @Override
  public void transformationDelegationStarted( Trans delegatedTrans,
    TransExecutionConfiguration transExecutionConfiguration ) {
  }

}
