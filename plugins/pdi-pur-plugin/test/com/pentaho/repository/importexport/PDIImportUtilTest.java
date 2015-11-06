package com.pentaho.repository.importexport;

import org.junit.Test;
import org.pentaho.di.repository.utils.IRepositoryFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by nbaker on 11/5/15.
 */
public class PDIImportUtilTest {

  @Test
  public void testConnectToRepository() throws Exception {
    IRepositoryFactory mock = mock( IRepositoryFactory.class );
    PDIImportUtil.setRepositoryFactory( mock );

    PDIImportUtil.connectToRepository( "foo" );

    verify( mock, times(1) ).connect( "foo" );
  }
}