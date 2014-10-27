package org.pentaho.di.trans.steps.sort;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.metastore.api.IMetaStore;

public class SortRowsMetaUnit {

  private List<DatabaseMeta> databases;
  private IMetaStore metaStore;

  @SuppressWarnings( "unchecked" )
  @Before
  public void setUp() {
    databases = mock( List.class );
    metaStore = mock( IMetaStore.class );
  }

  /**
   * @see <a href=http://jira.pentaho.com/browse/BACKLOG-377>http://jira.pentaho.com/browse/BACKLOG-377</a>
   * @throws KettleException
   */
  @Test
  public void testReadRep() throws KettleException {

    // check variable
    String sortSize = "${test}";

    Repository rep = new MemoryRepository();
    rep.saveStepAttribute( null, null, "sort_size", sortSize );

    SortRowsMeta sortRowsMeta = new SortRowsMeta();
    sortRowsMeta.readRep( rep, metaStore, null, databases );

    assertEquals( sortSize, sortRowsMeta.getSortSize() );

    // check integer size
    int sortSizeInt = anyInt();
    Repository rep2 = new MemoryRepository();
    rep2.saveStepAttribute( null, null, "sort_size", sortSizeInt );

    SortRowsMeta sortRowsMeta2 = new SortRowsMeta();
    sortRowsMeta2.readRep( rep2, metaStore, null, databases );

    assertEquals( String.valueOf( sortSizeInt ), sortRowsMeta2.getSortSize() );
  }

}
