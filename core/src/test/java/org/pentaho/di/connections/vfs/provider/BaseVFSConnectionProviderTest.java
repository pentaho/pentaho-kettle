
package org.pentaho.di.connections.vfs.provider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.pentaho.di.connections.utils.VFSConnectionTestOptions;
import org.pentaho.di.connections.vfs.BaseVFSConnectionDetails;
import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.core.exception.KettleException;

import static org.mockito.Mockito.when;

public class BaseVFSConnectionProviderTest {
  @Mock
  BaseVFSConnectionDetails baseVFSConnectionDetails;

  @Before
  public void setup() {
    baseVFSConnectionDetails = Mockito.spy( BaseVFSConnectionDetails.class );
  }

  @Test
  public void testWithProvidedRootPath() throws KettleException {

    BaseVFSConnectionProvider<VFSConnectionDetails> baseVFSConnectionProvider = Mockito.mock( BaseVFSConnectionProvider.class, Answers.CALLS_REAL_METHODS );

    when( baseVFSConnectionProvider.test( baseVFSConnectionDetails ) ).thenReturn( true );
    when( baseVFSConnectionDetails.getRootPath() ).thenReturn( "xyz/" );
    Assert.assertFalse( baseVFSConnectionProvider.test( baseVFSConnectionDetails, getVFSTestOptionWhenNotIgnoringRootPath() ) );
  }

  @Test
  public void testWhenConnectionTestIsInvalid() throws KettleException {

    BaseVFSConnectionProvider<VFSConnectionDetails> baseVFSConnectionProvider = Mockito.mock( BaseVFSConnectionProvider.class, Answers.CALLS_REAL_METHODS );
    Assert.assertFalse( baseVFSConnectionProvider.test( baseVFSConnectionDetails, getVFSTestOptionWhenNotIgnoringRootPath() ) );
  }


  @Test
  public void testWhenConnectionDontSupportRootPath() throws KettleException {

    BaseVFSConnectionProvider<VFSConnectionDetails> baseVFSConnectionProvider = Mockito.mock( BaseVFSConnectionProvider.class, Answers.CALLS_REAL_METHODS );

    when( baseVFSConnectionProvider.test( baseVFSConnectionDetails ) ).thenReturn( true );
    when( baseVFSConnectionDetails.isSupportsRootPath() ).thenReturn( false );
    Assert.assertTrue( baseVFSConnectionProvider.test( baseVFSConnectionDetails, getVFSTestOptionWhenIgnoringRootPath() ) );
  }

  @Test
  public void testWithIgnoringRootPath() throws KettleException {

    BaseVFSConnectionProvider<VFSConnectionDetails> baseVFSConnectionProvider = Mockito.mock( BaseVFSConnectionProvider.class, Answers.CALLS_REAL_METHODS );

    when( baseVFSConnectionProvider.test( baseVFSConnectionDetails ) ).thenReturn( true );
    Assert.assertTrue( baseVFSConnectionProvider.test( baseVFSConnectionDetails, getVFSTestOptionWhenNotIgnoringRootPath() ) );
  }


  @Test
  public void testWithConnectionDomain() throws KettleException {

    BaseVFSConnectionProvider<VFSConnectionDetails> baseVFSConnectionProvider = Mockito.mock( BaseVFSConnectionProvider.class, Answers.CALLS_REAL_METHODS );

    when( baseVFSConnectionProvider.test( baseVFSConnectionDetails ) ).thenReturn( true );
    when( baseVFSConnectionDetails.getRootPath() ).thenReturn( "xyz/" );
    when( baseVFSConnectionDetails.getDomain() ).thenReturn( "local" );
    Assert.assertFalse( baseVFSConnectionProvider.test( baseVFSConnectionDetails, getVFSTestOptionWhenNotIgnoringRootPath() ) );
  }

  private VFSConnectionTestOptions getVFSTestOptionWhenNotIgnoringRootPath() {
    return new VFSConnectionTestOptions( false );
  }

  private VFSConnectionTestOptions getVFSTestOptionWhenIgnoringRootPath() {
    VFSConnectionTestOptions vfsConnectionTestOptions = new VFSConnectionTestOptions();
    vfsConnectionTestOptions.setIgnoreRootPath( true );
    return vfsConnectionTestOptions;
  }
}
