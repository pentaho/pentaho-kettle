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

package org.pentaho.di.repository.pur;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class IRepositoryConnectorTest {

  @Test
  public void defaultSingleArgumentConnectDelegatesWithEmptyPassword() throws KettleException {
    RecordingConnector connector = new RecordingConnector();

    RepositoryConnectResult result = connector.connect( "alice" );

    assertEquals( "alice", connector.username );
    assertEquals( "", connector.password );
    assertEquals( connector.result, result );
  }

  private static final class RecordingConnector implements IRepositoryConnector {
    private final RepositoryConnectResult result = mock( RepositoryConnectResult.class );
    private String username;
    private String password;

    @Override
    public RepositoryConnectResult connect( String username, String password ) {
      this.username = username;
      this.password = password;
      return result;
    }

    @Override
    public void disconnect() {
      // no-op
    }

    @Override
    public LogChannelInterface getLog() {
      return null;
    }

    @Override
    public ServiceManager getServiceManager() {
      return null;
    }
  }
}
