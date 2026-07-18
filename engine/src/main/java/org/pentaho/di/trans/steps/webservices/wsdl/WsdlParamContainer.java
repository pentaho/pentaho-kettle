/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.webservices.wsdl;

public interface WsdlParamContainer {
  String getContainerName();

  String[] getParamNames();

  String getParamType( String paramName );

  String getItemName();

  boolean isArray();
}
