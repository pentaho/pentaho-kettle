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

package org.pentaho.di.core.compress;

import java.util.Collection;

public interface CompressionProviderFactoryInterface {

  CompressionProvider createCompressionProviderInstance( String name );

  Collection<CompressionProvider> getCompressionProviders();

  String[] getCompressionProviderNames();

  CompressionProvider getCompressionProviderByName( String name );
}
