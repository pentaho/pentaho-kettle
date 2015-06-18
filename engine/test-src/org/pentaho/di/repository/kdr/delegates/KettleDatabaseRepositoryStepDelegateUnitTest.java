package org.pentaho.di.repository.kdr.delegates;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
public class KettleDatabaseRepositoryStepDelegateUnitTest {

  @Test
  public void getStepTypeIDs_WhenNeedToUseNotAllValues() throws Exception {
    final int amount = 1;
    final String[] values = new String[] {"1", "2", "3"};

    KettleDatabaseRepository rep = new KettleDatabaseRepository();
    rep.connectionDelegate = mock( KettleDatabaseRepositoryConnectionDelegate.class );
    when( rep.connectionDelegate.getDatabaseMeta() ).thenReturn( mock( DatabaseMeta.class ) );

    KettleDatabaseRepositoryStepDelegate delegate = new KettleDatabaseRepositoryStepDelegate( rep );
    delegate.getStepTypeIDs( values, amount );

    verify( rep.connectionDelegate )
      .getIDsWithValues( anyString(), anyString(), anyString(), argThat( new BaseMatcher<String[]>() {

        @Override public boolean matches( Object item ) {
          return ( ( (String[]) item ).length == amount ) && ( ( (String[]) item )[ 0 ].equals( values[ 0 ] ) );
        }

        @Override public void describeTo( Description description ) {
        }
      } ) );
  }
}
