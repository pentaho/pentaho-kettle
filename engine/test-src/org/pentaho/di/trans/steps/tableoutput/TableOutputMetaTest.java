package org.pentaho.di.trans.steps.tableoutput;

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

public class TableOutputMetaTest {

  private List<DatabaseMeta> databases;
  private IMetaStore metaStore;

  @SuppressWarnings( "unchecked" )
  @Before
  public void setUp() {
    databases = mock( List.class );
    metaStore = mock( IMetaStore.class );
  }

  /**
   * @see 
   *     <a href=http://jira.pentaho.com/browse/BACKLOG-377>http://jira.pentaho.com/browse/BACKLOG-377</a>
   * @throws KettleException
   */
  @Test
  public void testReadRep() throws KettleException {

    //check variable
    String commitSize = "${test}";

    Repository rep = new MemoryRepository();
    rep.saveStepAttribute( null, null, "commit", commitSize );

    TableOutputMeta tableOutputMeta = new TableOutputMeta();
    tableOutputMeta.readRep( rep, metaStore, null, databases );

    assertEquals( commitSize, tableOutputMeta.getCommitSize() );

    //check integer size
    int  commitSizeInt = anyInt();
    Repository rep2 = new MemoryRepository();
    rep2.saveStepAttribute( null, null, "commit", commitSizeInt );

    TableOutputMeta tableOutputMeta2 = new TableOutputMeta();
    tableOutputMeta2.readRep( rep2, metaStore, null, databases );

    assertEquals( String.valueOf( commitSizeInt ), tableOutputMeta2.getCommitSize() );
  }

}
