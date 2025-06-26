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

import java.util.HashMap;
import java.util.Map;

/**
 * The DefaultTransManager is used to manage the registration of {@link TransFactory} and
 * generate the {@link Trans} Object for respective RunConfiguration
 */
public class DefaultTransFactoryManager implements TransFactoryMananger {

  private static DefaultTransFactoryManager instance;

  private Map<String, TransFactory> mapConfigurationNameTransFactory;


  private DefaultTransFactoryManager(){
    mapConfigurationNameTransFactory = new HashMap<>();
  }

  public static synchronized DefaultTransFactoryManager getInstance() {
    if ( null == instance ) {
      instance = new DefaultTransFactoryManager();
    }
    return instance;
  }

  /**
   * Registers/Adds a {@link TransFactory} using the provided Run Configuration Name
   * @param runConfigurationName
   * @param transFactory
   */
  @Override
  public void registerFactory( String runConfigurationName, TransFactory transFactory ) {
    mapConfigurationNameTransFactory.put( runConfigurationName, transFactory );
  }

  /**
   * Fetches a {@link TransFactory} or Returns {@link DefaultTransFactory} using the provided Run Configuration Name
   * @param runConfigurationName
   * @return TransFactory
   */
  @Override
  public TransFactory getTransFactory( String runConfigurationName ) {
    return mapConfigurationNameTransFactory.getOrDefault( runConfigurationName, new DefaultTransFactory() );
  }
}
