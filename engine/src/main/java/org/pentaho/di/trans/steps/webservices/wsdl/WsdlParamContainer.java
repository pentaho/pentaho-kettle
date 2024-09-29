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


package org.pentaho.di.trans.steps.webservices.wsdl;

public interface WsdlParamContainer {
  String getContainerName();

  String[] getParamNames();

  String getParamType( String paramName );

  String getItemName();

  boolean isArray();
}
