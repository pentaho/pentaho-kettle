package org.pentaho.di.ui.repository.repositoryexplorer.model;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class UIRepositoryObjectTest {
  UIRepositoryObject.DateObjectComparator c = new UIRepositoryObject.DateObjectComparator();
  SimpleDateFormat fmt = new SimpleDateFormat( "yyyy-MM-dd" );

  private UIRepositoryObject createUIRepositoryObject( String name, Date date ) {
    UIRepositoryObject instance = mock( UIRepositoryObject.class );
    when( instance.getModifiedDate() ).thenReturn( date );
    when( instance.getName() ).thenReturn( name );

    return instance;
  }

  @Test
  public void test() throws Exception {
    UIRepositoryObject youngest = createUIRepositoryObject( "youngest", fmt.parse( "2010-09-20" ) );
    UIRepositoryObject young = createUIRepositoryObject( "young", fmt.parse( "2011-11-23" ) );
    UIRepositoryObject old1 = createUIRepositoryObject( "old1", fmt.parse( "2012-10-20" ) );
    UIRepositoryObject old2 = createUIRepositoryObject( "old2", fmt.parse( "2012-10-20" ) );
    UIRepositoryObject oldest = createUIRepositoryObject( "oldest", fmt.parse( "2013-09-22" ) );

    assertTrue( "youngest is less than young", c.compare( youngest, young ) < 0 );
    assertTrue( "oldest is more than old", c.compare( oldest, old1 ) > 0 );
    assertTrue( "old1 is equal to old2", c.compare( old1, old2 ) == 0 );
  }

}
