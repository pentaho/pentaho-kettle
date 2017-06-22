/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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
