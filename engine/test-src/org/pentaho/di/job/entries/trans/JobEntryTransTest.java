package org.pentaho.di.job.entries.trans;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class JobEntryTransTest {

  private final String JOB_ENTRY_TRANS_NAME = "JobEntryTransName";
  private final String JOB_ENTRY_FILE_NAME = "JobEntryFileName";
  private final String JOB_ENTRY_FILE_DIRECTORY = "JobEntryFileDirectory";
  private final String JOB_ENTRY_DESCRIPTION = "JobEntryDescription";
  private Node entrynode;

  //prepare xml for use
  @Before
  public void before() throws ParserConfigurationException, SAXException, IOException {
    JobEntryTrans jobEntryTrans = new JobEntryTrans( JOB_ENTRY_TRANS_NAME );
    jobEntryTrans.setDescription( JOB_ENTRY_DESCRIPTION );
    jobEntryTrans.setFileName( JOB_ENTRY_FILE_NAME );
    jobEntryTrans.setDirectory( JOB_ENTRY_FILE_DIRECTORY );
    String string = "<job>" + jobEntryTrans.getXML() + "</job>";
    InputStream stream = new ByteArrayInputStream( string.getBytes( StandardCharsets.UTF_8 ) );
    DocumentBuilder db;
    Document doc;
    db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    doc = db.parse( stream );
    entrynode = doc;
  }

  /**
   * BACKLOG-179 - Exporting/Importing Jobs breaks Transformation specification when using "Specify by reference"
   * 
   * Test checks that we choose different {@link ObjectLocationSpecificationMethod} when connection to
   * {@link Repository} and disconnected. 
   * 
   * <b>Important!</b> You must rewrite test when change import logic
   * 
   * @throws KettleXMLException
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  @Test
  @SuppressWarnings( "unchecked" )
  public void testChooseSpecMethodByRepositoryConnectionStatus() throws KettleXMLException {
    JobEntryTrans jobEntryTrans = new JobEntryTrans( "testChooseSpecMethodByRepository" );
    List<DatabaseMeta> databases = mock( List.class );
    List<SlaveServer> slaveServers = mock( List.class );
    IMetaStore metaStore = mock( IMetaStore.class );

    Repository rep = mock( Repository.class );
    when( rep.isConnected() ).thenReturn( true );

    //load when kettle is connected to repository
    jobEntryTrans.loadXML( entrynode, databases, slaveServers, rep, metaStore );
    assertEquals( "If we connect to repository then we use rep_name method",
        ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jobEntryTrans.getSpecificationMethod() );

    //reload when kettle is not connected to repository
    jobEntryTrans.loadXML( entrynode, databases, slaveServers, null, metaStore );
    assertEquals( "If we connect to repository then we use rep_name method",
        ObjectLocationSpecificationMethod.FILENAME, jobEntryTrans.getSpecificationMethod() );
  }
}
