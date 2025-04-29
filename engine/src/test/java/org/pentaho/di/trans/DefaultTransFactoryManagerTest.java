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

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.repository.Repository;

public class DefaultTransFactoryManagerTest {

  @Test
  public void getRandomRunConfigTest() {
    TransFactory transFactory = DefaultTransFactoryManager.getInstance().getTransFactory( "random" );
    Assert.assertTrue( transFactory instanceof DefaultTransFactory );
  }

  @Test
  public void registerFactoryTest(){
    DefaultTransFactoryManager.getInstance().registerFactory( "TestDefault", new DefaultTransFactory() );
    TransFactory transFactory = DefaultTransFactoryManager.getInstance().getTransFactory( "TestDefault" );
    Assert.assertTrue( transFactory instanceof DefaultTransFactory );
  }

  @Test
  public void registerNonDefaultFactoryTest() {
    DefaultTransFactoryManager.getInstance().registerFactory( "TestNonDefault", new TestNonDefaultTransFactory() );
    TransFactory transFactory = DefaultTransFactoryManager.getInstance().getTransFactory( "TestNonDefault" );
    Assert.assertTrue( transFactory instanceof TestNonDefaultTransFactory );
  }

  public static class TestNonDefaultTransFactory implements TransFactory {

    @Override
    public Trans create(TransMeta parent, Repository rep, String name, String dirname, String filename, TransMeta parentTransMeta) throws KettleException {
      return new Trans( parent, null );
    }

    @Override
    public Trans create(TransMeta transMeta, LoggingObjectInterface log) throws KettleException {
      return new Trans( transMeta, log );
    }
  }

}
