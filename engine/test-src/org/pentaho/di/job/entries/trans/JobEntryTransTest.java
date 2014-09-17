package org.pentaho.di.job.entries.trans;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

  //prepare xml for use
  public Node getEntryNode( boolean includeTransname ) throws ParserConfigurationException, SAXException, IOException {
    JobEntryTrans jobEntryTrans = new JobEntryTrans( JOB_ENTRY_TRANS_NAME );
    jobEntryTrans.setDescription( JOB_ENTRY_DESCRIPTION );
    jobEntryTrans.setFileName( JOB_ENTRY_FILE_NAME );
    jobEntryTrans.setDirectory( JOB_ENTRY_FILE_DIRECTORY );
    if ( includeTransname ) {
      jobEntryTrans.setTransname( JOB_ENTRY_FILE_NAME );
    }
    String string = "<job>" + jobEntryTrans.getXML() + "</job>";
    System.out.println( string );
    InputStream stream = new ByteArrayInputStream( string.getBytes( StandardCharsets.UTF_8 ) );
    DocumentBuilder db;
    Document doc;
    db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    doc = db.parse( stream );
    Node entryNode = doc.getFirstChild();
    return entryNode;
  }

  private JobEntryTrans getJobEntryTrans() {
    JobEntryTrans jobEntryTrans = new JobEntryTrans( "testChooseSpecMethodByRepository" );
    return jobEntryTrans;
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
  public void testChooseSpecMethodByRepositoryConnectionStatus() throws KettleXMLException, ParserConfigurationException, SAXException, IOException {
    List<DatabaseMeta> databases = mock( List.class );
    List<SlaveServer> slaveServers = mock( List.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    
    Repository rep = mock( Repository.class );
    when( rep.isConnected() ).thenReturn( true );

    //load when kettle is connected to repository
    JobEntryTrans jobEntryTrans = getJobEntryTrans();    
    jobEntryTrans.loadXML( getEntryNode( true ), databases, slaveServers, rep, metaStore );
    assertEquals( "If we connect to repository then we use rep_name method",
        ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jobEntryTrans.getSpecificationMethod() );

    //reload when kettle is not connected to repository
    // still should be REPOSITORY_BY_NAME because we have a transname (from previous instance)
    jobEntryTrans.loadXML( getEntryNode( true ), databases, slaveServers, null, metaStore );
    assertEquals( "If we connect to repository then we use rep_name method",
        ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jobEntryTrans.getSpecificationMethod() );
    
    jobEntryTrans = getJobEntryTrans();
    jobEntryTrans.loadXML( getEntryNode( false ), databases, slaveServers, null, metaStore );
    assertEquals( "If we connect to repository then we use rep_name method",
        ObjectLocationSpecificationMethod.FILENAME, jobEntryTrans.getSpecificationMethod() );
  }
}
