package org.pentaho.di.trans.steps.rowgenerator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.metastore.api.IMetaStore;

public class RowGeneratorMetaTest {

  private Repository rep;

  private ObjectId id_step;

  private final String launchVariable = "${ROW_LIMIT}";

  private final String rowGeneratorRowLimitCode = "limit";

  @Before
  public void setUp() throws KettleException {
    rep = new MemoryRepository();
    id_step = new StringObjectId( anyString() );
    rep.saveStepAttribute( any(ObjectId.class ), id_step, rowGeneratorRowLimitCode, launchVariable );
  }

  /**
   * If we can read row limit as string from repository then we can run row generator.
   * @see RowGeneratorTest
   * @throws KettleException
   */
  @Test
  public void testReadRowLimitAsStringFromRepository() throws KettleException {
    RowGeneratorMeta rowGeneratorMeta = new RowGeneratorMeta();
    rowGeneratorMeta.readRep( rep, any(IMetaStore.class ), id_step, anyListOf( DatabaseMeta.class ) );
    assertEquals( rowGeneratorMeta.getRowLimit(),  launchVariable );
  }

}
