package org.pentaho.di.repository.kdr;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.OracleDatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryConnectionDelegate;

public class KettleDatabaseRepositoryCreationHelperTest {
  LogChannelInterface log = LogChannel.GENERAL;
  KettleDatabaseRepositoryCreationHelper helper;
  KettleDatabaseRepository repository;
  static String INDEX = "INDEX ";

  private AnswerSecondArgument lan = new AnswerSecondArgument();

  @Before
  public void setUp() throws Exception {
    KettleLogStore.init();
    KettleDatabaseRepositoryConnectionDelegate delegate =
        Mockito.mock( KettleDatabaseRepositoryConnectionDelegate.class );
    repository = Mockito.mock( KettleDatabaseRepository.class );
    repository.connectionDelegate = delegate;
    helper = new KettleDatabaseRepositoryCreationHelper( repository );

    Mockito.when( repository.getLog() ).thenReturn( log );
  }

  /**
   * PDI-10237 test index name length.
   * 
   * @throws KettleException
   */
  @Test
  public void testCreateIndexLenghts() throws KettleException {
    DatabaseMeta meta = Mockito.mock( DatabaseMeta.class );
    Mockito.when( meta.getStartQuote() ).thenReturn( "" );
    Mockito.when( meta.getEndQuote() ).thenReturn( "" );
    Mockito.when( meta.getQuotedSchemaTableCombination( Mockito.anyString(), Mockito.anyString() ) ).thenAnswer(
        new Answer<String>() {
          @Override
          public String answer( InvocationOnMock invocation ) throws Throwable {
            return invocation.getArguments()[1].toString();
          }
        } );
    Mockito.when( meta.getDatabaseInterface() ).thenReturn( new OracleDatabaseMeta() );

    Database db = Mockito.mock( Database.class );
    // database.getDatabaseMeta().getDatabaseInterface()
    Mockito.when( db.getDatabaseMeta() ).thenReturn( meta );
    // always return some create sql.
    Mockito.when(
        db.getDDL( Mockito.anyString(), Mockito.any( RowMetaInterface.class ), Mockito.anyString(), Mockito
            .anyBoolean(), Mockito.anyString(), Mockito.anyBoolean() ) ).thenReturn( "### CREATE TABLE;" );
    Mockito.when( repository.getDatabase() ).thenReturn( db );
    Mockito.when( repository.getDatabaseMeta() ).thenReturn( meta );

    Mockito.when(
        db.getCreateIndexStatement( Mockito.anyString(), Mockito.anyString(), Mockito.any( String[].class ), Mockito
            .anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean() ) ).thenAnswer( lan );

    KettleDatabaseRepositoryCreationHelper helper = new KettleDatabaseRepositoryCreationHelper( repository );

    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init( true );
    String passwordEncoderPluginID = Const.NVL(
        EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );

    List<String> statements = new ArrayList<String>();
    helper.createRepositorySchema( null, false, statements, true );

    for ( String st : statements ) {
      if ( st == null || st.startsWith( "#" ) ) {
        continue;
      }
      Assert.assertTrue( "INdex name is not overlenght!: " + st, st.length() <= 30 );
    }
  }

  static class AnswerSecondArgument implements Answer<String> {
    @Override
    public String answer( InvocationOnMock invocation ) throws Throwable {
      if ( invocation.getArguments().length < 2 ) {
        throw new RuntimeException( "no cookies!" );
      }
      return String.valueOf( invocation.getArguments()[1] );
    }
  }
}
