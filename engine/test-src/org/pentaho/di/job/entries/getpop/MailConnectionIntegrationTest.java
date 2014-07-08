package org.pentaho.di.job.entries.getpop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;

public class MailConnectionIntegrationTest {

  private enum MailServerThread {
    INITIAL, STARTED, RUNNING, FAIL;
  }

  private static final String SERVER_NAME = "localhost";

  private static final String RESPONSE_DELIMITER = "DELIMITER";

  private ServerSocket serverSocket;

  private volatile MailServerThread serverState = MailServerThread.INITIAL;

  @Before
  public void beforeExec() throws IOException {

    // Starting port listener which will be return hardcoded answers
    Thread serverThread = Executors.defaultThreadFactory().newThread( new Runnable() {

      @Override
      public void run() {
        PrintWriter out = null;
        BufferedReader answersIn = null;
        BufferedReader requestIn = null;
        String responseLine = null;
        serverState = MailServerThread.RUNNING;
        try {
          InputStream in = MailConnectionIntegrationTest.class.getResourceAsStream( "serverAnswers.txt" );
          answersIn = new BufferedReader( new InputStreamReader( in ) );
          serverSocket = new ServerSocket( 0 );
          serverState = MailServerThread.STARTED;
          Socket clientSocket = serverSocket.accept();
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

        } catch ( IOException e ) {
          serverState = MailServerThread.FAIL;
          e.printStackTrace();
        } finally {
          if ( answersIn != null ) {
            try {
              answersIn.close();
            } catch ( IOException e ) {
              serverState = MailServerThread.FAIL;
              e.printStackTrace();
            }
          }

          if ( out != null ) {
            out.close();
          }
        }

      }
    } );
    serverThread.start();
  }

  @After
  public void afterExec() {

    try {
      if ( serverSocket != null ) {
        serverSocket.close();
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    }

  }

  @Test
  public void saveAttachedFiles() throws Exception {
    LogChannelInterface log = new LogChannel( new Object() );
    // Ждем пока забиндится ServerSocket
    while ( serverState == MailServerThread.INITIAL || serverState == MailServerThread.RUNNING ) {
      Thread.sleep( 100L );
    }

    if ( serverState == MailServerThread.STARTED ) {
      MailConnection mailConnection =
          new MailConnection( log, MailConnectionMeta.PROTOCOL_IMAP, SERVER_NAME, serverSocket.getLocalPort(),
              "pent.qa@yandex.com", "pentaho2014", false, false, "" );
      mailConnection.connect();
      mailConnection.openFolder( "Отправленные", true );
      mailConnection.retrieveMessages();
      mailConnection.fetchNext();
      mailConnection.saveMessageContentToFile( "testMailFile", "ram://" );
      mailConnection.saveAttachedFiles( "ram://", null );
      Assert.assertTrue( "File not exist", KettleVFS
          .fileExists( "ram://Пример ВЭД Китая 42 поля_27132000_201001-201109impexp.csv" ) );
    } else {
      throw new Exception( "Mail thread could not start properly" );
    }
  }
}
