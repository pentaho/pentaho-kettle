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

package org.pentaho.di.trans;

/**
 * The TransManager Interface is used as parent for the different Trans Managers
 */

public interface TransFactoryMananger {

  /**
   * Registers/Adds a {@link TransFactory} using the provided Run Configuration Name
   * @param runConfigurationName
   * @param transFactory
   */
  void registerFactory( String runConfigurationName, TransFactory transFactory );

  /**
   * Fetches a {@link TransFactory} or Returns default {@link DefaultTransFactory} using the provided Run Configuration Name
   * @param runConfigurationName
   * @return TransFactory
   */
  TransFactory getTransFactory( String runConfigurationName );
}
