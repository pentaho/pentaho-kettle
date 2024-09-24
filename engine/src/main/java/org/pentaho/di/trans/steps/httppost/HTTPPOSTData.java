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

package org.pentaho.di.trans.steps.httppost;


import org.apache.http.NameValuePair;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class HTTPPOSTData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface inputRowMeta;
  public String realEncoding;
  public int[] header_parameters_nrs;
  public int[] body_parameters_nrs;
  public int[] query_parameters_nrs;
  public int indexOfUrlField;
  public String realUrl;
  public NameValuePair[] headerParameters;
  public NameValuePair[] bodyParameters;
  public NameValuePair[] queryParameters;
  public boolean useHeaderParameters;
  public boolean contentTypeHeaderOverwrite;
  public boolean useBodyParameters;
  public boolean useQueryParameters;
  public int indexOfRequestEntity;

  public String realProxyHost;
  public int realProxyPort;
  public String realHttpLogin;
  public String realHttpPassword;

  public int realSocketTimeout;
  public int realConnectionTimeout;
  public int realcloseIdleConnectionsTime;

  public HTTPPOSTData() {
    super();
    indexOfUrlField = -1;
    useHeaderParameters = false;
    contentTypeHeaderOverwrite = false;
    useBodyParameters = false;
    useQueryParameters = false;
    indexOfRequestEntity = -1;
    realEncoding = null;
    realProxyHost = null;
    realProxyPort = 8080;
    realHttpLogin = null;
    realHttpPassword = null;
  }

}
