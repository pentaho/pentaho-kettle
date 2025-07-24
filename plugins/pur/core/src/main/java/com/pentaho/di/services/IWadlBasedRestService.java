/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package com.pentaho.di.services;

import java.net.URI;
import java.util.Map;

import jakarta.ws.rs.client.Client;

public interface IWadlBasedRestService {

  IWadlBasedRestService getService( String serviceClassName, Client client, URI baseURI );

  IWadlBasedRestService getService( String serviceClassName, Client client );

  Object runServiceWithOutput( IWadlBasedRestService restService, Map<String, Object> paramaterMap );
}
