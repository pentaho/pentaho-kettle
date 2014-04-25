package org.pentaho.di.trans;

import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

public class TransTest {

  int count = 10000;
  Trans trans;
  TransMeta meta;

  @BeforeClass
  public static void beforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void beforeTest() throws KettleException {
    meta = new TransMeta();
    trans = new Trans( meta );
    trans.setLog( Mockito.mock( LogChannelInterface.class ) );
    trans.prepareExecution( null );
    trans.startThreads();
  }

  @Test
  public void testFindDatabaseWithEncodedConnectionName() {
    DatabaseMeta dbMeta1 = new DatabaseMeta( "encoded_DBConnection", "Oracle", "localhost", "access", "test", "111", "test", "test" );
    dbMeta1.setDisplayName( "encoded.DBConnection" );
    meta.addDatabase( dbMeta1 );

    DatabaseMeta dbMeta2 = new DatabaseMeta( "normalDBConnection", "Oracle", "localhost", "access", "test", "111", "test", "test" );
    dbMeta2.setDisplayName( "normalDBConnection" );
    meta.addDatabase( dbMeta2 );

    DatabaseMeta databaseMeta = meta.findDatabase( dbMeta1.getDisplayName() );
    Assert.assertNotNull( databaseMeta );
    Assert.assertEquals( databaseMeta.getName(), "encoded_DBConnection" );
    Assert.assertEquals( databaseMeta.getDisplayName(), "encoded.DBConnection" );
  }

  /**
   * PDI-5229 - ConcurrentModificationException when restarting transformation Test that listeners can be accessed
   * concurrently during transformation finish
   * 
   * @throws KettleException
   * @throws InterruptedException
   */
  @Test
  public void testTransFinishListenersConcurrentModification() throws KettleException, InterruptedException {
    CountDownLatch start = new CountDownLatch( 1 );
    TransFinishListenerAdder add = new TransFinishListenerAdder( trans, start );
    TransFinishListenerFirer firer = new TransFinishListenerFirer( trans, start );
    startThreads( add, firer, start );
    Assert.assertEquals( "All listeners are added: no ConcurrentModificationException", count, add.c );
    Assert.assertEquals( "All Finish listeners are iterated over: no ConcurrentModificationException", count, firer.c );
  }

  /**
   * Test that listeners can be accessed concurrently during transformation start
   * 
   * @throws InterruptedException
   */
  @Test
  public void testTransStartListenersConcurrentModification() throws InterruptedException {
    CountDownLatch start = new CountDownLatch( 1 );
    TransFinishListenerAdder add = new TransFinishListenerAdder( trans, start );
    TransStartListenerFirer starter = new TransStartListenerFirer( trans, start );
    startThreads( add, starter, start );
    Assert.assertEquals( "All listeners are added: no ConcurrentModificationException", count, add.c );
    Assert.assertEquals( "All Start listeners are iterated over: no ConcurrentModificationException", count, starter.c );
  }

  /**
   * Test that transformation stop listeners can be accessed concurrently
   * 
   * @throws InterruptedException
   */
  @Test
  public void testTransStoppedListenersConcurrentModification() throws InterruptedException {
    CountDownLatch start = new CountDownLatch( 1 );
    TransStoppedCaller stopper = new TransStoppedCaller( trans, start );
    TransStopListenerAdder adder = new TransStopListenerAdder( trans, start );
    startThreads( stopper, adder, start );
    Assert.assertEquals( "All transformation stop listeners is added", count, adder.c );
    Assert.assertEquals( "All stop call success", count, stopper.c );
  }

  private void startThreads( Runnable one, Runnable two, CountDownLatch start ) throws InterruptedException {
    Thread th = new Thread( one );
    Thread tt = new Thread( two );
    th.start();
    tt.start();
    start.countDown();
    th.join();
    tt.join();
  }

  private abstract class TransKicker implements Runnable {
    protected Trans tr;
    protected int c = 0;
    protected CountDownLatch start;
    protected int max = count;

    TransKicker( Trans tr, CountDownLatch start ) {
      this.tr = tr;
      this.start = start;
    }

    protected boolean isStopped() {
      c++;
      return c >= max;
    }
  }

  private class TransStoppedCaller extends TransKicker {
    TransStoppedCaller( Trans tr, CountDownLatch start ) {
      super( tr, start );
    }

    @Override
    public void run() {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      while ( !isStopped() ) {
        trans.stopAll();
      }
    }
  }

  private class TransStopListenerAdder extends TransKicker {
    TransStopListenerAdder( Trans tr, CountDownLatch start ) {
      super( tr, start );
    }

    @Override
    public void run() {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      while ( !isStopped() ) {
        trans.addTransStoppedListener( transStoppedListener );
      }
    }
  }

  private class TransFinishListenerAdder extends TransKicker {
    TransFinishListenerAdder( Trans tr, CountDownLatch start ) {
      super( tr, start );
    }

    @Override
    public void run() {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      // run
      while ( !isStopped() ) {
        tr.addTransListener( listener );
      }
    }
  }

  private class TransFinishListenerFirer extends TransKicker {
    TransFinishListenerFirer( Trans tr, CountDownLatch start ) {
      super( tr, start );
    }

    @Override
    public void run() {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      // run
      while ( !isStopped() ) {
        try {
          tr.fireTransFinishedListeners();
          // clean array blocking queue
          tr.waitUntilFinished();
        } catch ( KettleException e ) {
          throw new RuntimeException();
        }
      }
    }
  }

  private class TransStartListenerFirer extends TransKicker {
    TransStartListenerFirer( Trans tr, CountDownLatch start ) {
      super( tr, start );
    }

    @Override
    public void run() {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      // run
      while ( !isStopped() ) {
        try {
          tr.fireTransStartedListeners();
        } catch ( KettleException e ) {
          throw new RuntimeException();
        }
      }
    }
  }

  private final TransListener listener = new TransListener() {
    @Override
    public void transStarted( Trans trans ) throws KettleException {
    }

    @Override
    public void transActive( Trans trans ) {
    }

    @Override
    public void transFinished( Trans trans ) throws KettleException {
    }
  };

  private final TransStoppedListener transStoppedListener = new TransStoppedListener() {
    @Override
    public void transStopped( Trans trans ) {
    }

  };
}
