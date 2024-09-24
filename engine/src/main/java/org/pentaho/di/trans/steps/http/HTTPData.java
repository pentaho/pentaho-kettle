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

package org.pentaho.di.trans.steps.http;

import org.apache.http.NameValuePair;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class HTTPData extends BaseStepData implements StepDataInterface {
  public int[] argnrs;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface inputRowMeta;
  public int indexOfUrlField;
  public String realUrl;
  public String realProxyHost;
  public int realProxyPort;
  public String realHttpLogin;
  public String realHttpPassword;
  public int[] header_parameters_nrs;
  public boolean useHeaderParameters;
  public NameValuePair[] headerParameters;

  public int realSocketTimeout;
  public int realConnectionTimeout;
  public int realcloseIdleConnectionsTime;

  /**
   * Default constructor.
   */
  public HTTPData() {
    super();
    indexOfUrlField = -1;
    realProxyHost = null;
    realProxyPort = 8080;
    realHttpLogin = null;
    realHttpPassword = null;
  }
}
