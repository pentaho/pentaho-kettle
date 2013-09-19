package org.pentaho.di.trans.steps.ldapinput;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;

public class LdapProtocolFactoryTest {
  private LdapMeta mockLdapMeta;
  
  @Before
  public void setup() {
    mockLdapMeta = mock(LdapMeta.class);
  }
  
  @Test
  public void testLdapProtocolFactoryGetConnectionTypesReturnsAllProtocolNames() {
    List<String> types = LdapProtocolFactory.getConnectionTypes(null);
    assertEquals(3, types.size());
    assertTrue(types.contains(LdapProtocol.getName()));
    assertTrue(types.contains(LdapSslProtocol.getName()));
    assertTrue(types.contains(LdapTlsProtocol.getName()));
  }

  @Test
  public void testLdapProtocolFactoryReturnsLdapProtocolForName() throws KettleException {
    when(mockLdapMeta.getProtocol()).thenReturn(LdapProtocol.getName());
    LdapProtocol protocol = new LdapProtocolFactory(null).createLdapProtocol(mock(VariableSpace.class), mockLdapMeta, null);
    assertTrue(protocol.getClass().equals(LdapProtocol.class));
  }
  
  @Test
  public void testLdapProtocolFactoryReturnsLdapSslProtocolForName() throws KettleException {
    when(mockLdapMeta.getProtocol()).thenReturn(LdapSslProtocol.getName());
    LdapProtocol protocol = new LdapProtocolFactory(null).createLdapProtocol(mock(VariableSpace.class), mockLdapMeta, null);
    assertTrue(protocol.getClass().equals(LdapSslProtocol.class));
  }
  
  @Test
  public void testLdapProtocolFactoryReturnsLdapTlsProtocolForName() throws KettleException {
    when(mockLdapMeta.getProtocol()).thenReturn(LdapTlsProtocol.getName());
    LdapProtocol protocol = new LdapProtocolFactory(null).createLdapProtocol(mock(VariableSpace.class), mockLdapMeta, null);
    assertTrue(protocol.getClass().equals(LdapTlsProtocol.class));
  }
}
