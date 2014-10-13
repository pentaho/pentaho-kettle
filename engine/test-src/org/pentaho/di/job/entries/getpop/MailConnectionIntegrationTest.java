package org.pentaho.di.job.entries.getpop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;

public class MailConnectionIntegrationTest {

  private enum MailServerThreadState {
    STARTED, FAIL, STOPPING;
  }

  private enum MailServerType {
    GOOGLE( "googleServerAnswers.txt" ), MSN( "msnServerAnswers.txt" ), YAHOO( "yahooServerAnswers.txt" ), YANDEX(
        "yandexServerAnswers.txt" );

    private String resourceName;

    private MailServerType( String resourceName ) {
      this.resourceName = resourceName;
    }

    public String getResourceName() {
      return resourceName;
    }

  }

  private static final class FakeMailServer implements Runnable {

    private volatile MailServerThreadState serverState;
    private ServerSocket serverSocket;

    private MailServerType mailServerType;

    public FakeMailServer() throws IOException {
      serverSocket = new ServerSocket( 0 );
      serverState = MailServerThreadState.STARTED;
    }

    @Override
    public void run() {
      try {
        while ( serverState != MailServerThreadState.STOPPING ) {
          PrintWriter out = null;
          BufferedReader answersIn = null;
          BufferedReader requestIn = null;
          String responseLine = null;
          try {
            Socket clientSocket = serverSocket.accept();
            if ( mailServerType != null ) {
              InputStream in =
                  MailConnectionIntegrationTest.class.getResourceAsStream( mailServerType.getResourceName() );
              answersIn = new BufferedReader( new InputStreamReader( in ) );
              out = new PrintWriter( clientSocket.getOutputStream(), true );
              requestIn = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );

              responseLine = answersIn.readLine();
              while ( responseLine != null ) {
                if ( responseLine.equals( RESPONSE_DELIMITER ) ) {
                  out.flush();
                  requestIn.readLine();
                } else {
                  out.write( responseLine + "\r\n" );
                }
                responseLine = answersIn.readLine();
              }
            } else {
              throw new Exception( "Type of answers was not set" );
            }
          } catch ( SocketException e ) {
            // ignore socket exception
          } finally {
            if ( answersIn != null ) {
              try {
                answersIn.close();
              } catch ( IOException e ) {
                serverState = MailServerThreadState.FAIL;
                e.printStackTrace();
              }
            }
            if ( requestIn != null ) {
              try {
                requestIn.close();
              } catch ( IOException e ) {
                serverState = MailServerThreadState.FAIL;
                e.printStackTrace();
              }
            }
            if ( out != null ) {
              out.close();
            }

          }
        }

      } catch ( Exception e ) {
        serverState = MailServerThreadState.FAIL;
        e.printStackTrace();
      }
    }

    public MailServerThreadState getState() {
      return serverState;
    }

    public int getListenPort() {
      return serverSocket.getLocalPort();
    }

    public void setMailServerType( MailServerType type ) {
      this.mailServerType = type;
    }

    public void startServer() {
      Executors.defaultThreadFactory().newThread( this ).start();
    }

    public void stopServer() {
      serverState = MailServerThreadState.STOPPING;
      try {
        if ( serverSocket != null ) {
          serverSocket.close();
        }
      } catch ( IOException e ) {
      }
    }
  }

  private static final String SERVER_NAME = "localhost";

  private static final String RESPONSE_DELIMITER = "DELIMITER";

  private FakeMailServer serverObject;

  @Before
  public void beforeExec() throws IOException {
    serverObject = new FakeMailServer();
    serverObject.startServer();
  }

  @After
  public void afterExec() {
    if ( serverObject != null ) {
      serverObject.stopServer();
    }
  }

  private void testTemplate( MailServerType serverType, String login, String password, String folderName )
    throws Exception {
    LogChannelInterface log = new LogChannel( new Object() );
    serverObject.setMailServerType( serverType );

    if ( serverObject.getState() == MailServerThreadState.STARTED ) {
      MailConnection mailConnection =
          new MailConnection( log, MailConnectionMeta.PROTOCOL_IMAP, SERVER_NAME, serverObject.getListenPort(), login,
              password, false, false, "" );
      mailConnection.connect();
      mailConnection.openFolder( folderName, true );
      mailConnection.retrieveMessages();
      mailConnection.fetchNext();
      mailConnection.saveMessageContentToFile( "testMailFile", "ram://" );
      mailConnection.saveAttachedFiles( "ram://", null );
      mailConnection.disconnect();
      Assert.assertTrue( "File not exist", KettleVFS
          .fileExists( "ram://Пример ВЭД Китая 42 поля_27132000_201001-201109impexp.csv" ) );
    } else {
      throw new Exception( "Mail server thread could not start properly" );
    }
  }

  @Test
  public void saveAttachedFilesToMailServer() throws Exception {
    testTemplate( MailServerType.MSN, "pent.qa@hotmail.com", "pentaho2014", "Sent" );
    testTemplate( MailServerType.YANDEX, "pent.qa@yandex.com", "pentaho2014", "Отправленные" );
    testTemplate( MailServerType.GOOGLE, "pent.qa@gmail.com", "pentaho2014", "[Gmail]/Отправленные" );
    testTemplate( MailServerType.YAHOO, "pent_qa@yahoo.com", "Pentaho2014", "Sent" );

  }

}
