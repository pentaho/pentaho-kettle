/*******************************************************************************
 * Copyright (c) 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.widgets.Display;


/*
 * Replacement for ModalContext when running in JEE mode.
 */
public class JeeProgressRunner {

  private Display display;
  private ServerPushSession serverPushSession;

  public JeeProgressRunner( Display display ) {
    this.display = display;
    serverPushSession = new ServerPushSession();
  }

  public void run( IRunnableWithProgress operation,
                   IProgressMonitor monitor,
                   Runnable callback )
  {
    if( monitor != null ) {
      monitor.setCanceled( false );
    }
    serverPushSession.start();
    new ProgressRunnerThread( operation, monitor, callback ).start();
  }

  private class ProgressRunnerThread extends Thread {

    private IRunnableWithProgress runnable;
    private IProgressMonitor progressMonitor;
    private Thread callingThread;
    private Runnable callback;

    private ProgressRunnerThread( IRunnableWithProgress operation,
                                  IProgressMonitor monitor,
                                  Runnable callback )
    {
      super( "ProgressRunner" ); //$NON-NLS-1$
      Assert.isTrue( monitor != null && display != null );
      runnable = operation;
      progressMonitor = new AccumulatingProgressMonitor( monitor, display );
      this.callback = callback;
      this.callingThread = Thread.currentThread();
    }

    @Override
    public void run() {
      RWT.getUISession( display ).exec( new Runnable() {
        @Override
        public void run() {
          try {
            if( runnable != null ) {
              runnable.run( progressMonitor );
            }
          } catch( InvocationTargetException e ) {
            throw new RuntimeException( e );
          } catch( InterruptedException e ) {
            throw new RuntimeException( e );
          } finally {
            if( runnable instanceof IThreadListener ) {
              ( ( IThreadListener )runnable ).threadChange( callingThread );
            }
            display.asyncExec( callback );
            serverPushSession.stop();
          }
        }
      } );
    }
  }

  // Copy of (package-private) org.eclipse.jface.operation.AccumulatingProgressMonitor
  private static class AccumulatingProgressMonitor extends ProgressMonitorWrapper {

    /**
     * The display.
     */
    private Display display;
    /**
     * The collector, or <code>null</code> if none.
     */
    private Collector collector;
    private String currentTask = ""; //$NON-NLS-1$

    private class Collector implements Runnable {

      private String subTask;
      private double worked;
      private IProgressMonitor monitor;

      /**
       * Create a new collector.
       *
       * @param subTask
       * @param work
       * @param monitor
       */
      public Collector( String subTask, double work, IProgressMonitor monitor ) {
        this.subTask = subTask;
        this.worked = work;
        this.monitor = monitor;
      }

      /**
       * Add worked to the work.
       *
       * @param workedIncrement
       */
      public void worked( double workedIncrement ) {
        this.worked = this.worked + workedIncrement;
      }

      /**
       * Set the subTask name.
       *
       * @param subTaskName
       */
      public void subTask( String subTaskName ) {
        this.subTask = subTaskName;
      }

      /**
       * Run the collector.
       */
      @Override
      public void run() {
        clearCollector( this );
        if( subTask != null ) {
          monitor.subTask( subTask );
        }
        if( worked > 0 ) {
          monitor.internalWorked( worked );
        }
      }
    }

    /**
     * Creates an accumulating progress monitor wrapping the given one that uses the given display.
     *
     * @param monitor the actual progress monitor to be wrapped
     * @param display the SWT display used to forward the calls to the wrapped progress monitor
     */
    public AccumulatingProgressMonitor( IProgressMonitor monitor, Display display ) {
      super( monitor );
      Assert.isNotNull( display );
      this.display = display;
    }

    /*
     * (non-Javadoc) Method declared on IProgressMonitor.
     */
    @Override
    public void beginTask( final String name, final int totalWork ) {
      synchronized( this ) {
        collector = null;
      }
      display.asyncExec( new Runnable() {

        @Override
        public void run() {
          currentTask = name;
          getWrappedProgressMonitor().beginTask( name, totalWork );
        }
      } );
    }

    /**
     * Clears the collector object used to accumulate work and subtask calls if it matches the given
     * one.
     *
     * @param collectorToClear
     */
    private synchronized void clearCollector( Collector collectorToClear ) {
      // Check if the accumulator is still using the given collector.
      // If not, don't clear it.
      if( this.collector == collectorToClear ) {
        this.collector = null;
      }
    }

    /**
     * Creates a collector object to accumulate work and subtask calls.
     *
     * @param subTask
     * @param work
     */
    private void createCollector( String subTask, double work ) {
      collector = new Collector( subTask, work, getWrappedProgressMonitor() );
      display.asyncExec( collector );
    }

    /*
     * (non-Javadoc) Method declared on IProgressMonitor.
     */
    @Override
    public void done() {
      synchronized( this ) {
        collector = null;
      }
      display.asyncExec( new Runnable() {

        @Override
        public void run() {
          getWrappedProgressMonitor().done();
        }
      } );
    }

    /*
     * (non-Javadoc) Method declared on IProgressMonitor.
     */
    @Override
    public synchronized void internalWorked( final double work ) {
      if( collector == null ) {
        createCollector( null, work );
      } else {
        collector.worked( work );
      }
    }

    /*
     * (non-Javadoc) Method declared on IProgressMonitor.
     */
    @Override
    public void setTaskName( final String name ) {
      synchronized( this ) {
        collector = null;
      }
      display.asyncExec( new Runnable() {

        @Override
        public void run() {
          currentTask = name;
          getWrappedProgressMonitor().setTaskName( name );
        }
      } );
    }

    /*
     * (non-Javadoc) Method declared on IProgressMonitor.
     */
    @Override
    public synchronized void subTask( final String name ) {
      if( collector == null ) {
        createCollector( name, 0 );
      } else {
        collector.subTask( name );
      }
    }

    /*
     * (non-Javadoc) Method declared on IProgressMonitor.
     */
    @Override
    public synchronized void worked( int work ) {
      internalWorked( work );
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.ProgressMonitorWrapper#clearBlocked()
     */
    @Override
    public void clearBlocked() {
      // If this is a monitor that can report blocking do so.
      // Don't bother with a collector as this should only ever
      // happen once and prevent any more progress.
      final IProgressMonitor pm = getWrappedProgressMonitor();
      if( !( pm instanceof IProgressMonitorWithBlocking ) ) {
        return;
      }
      display.asyncExec( new Runnable() {

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
          ( ( IProgressMonitorWithBlocking )pm ).clearBlocked();
          Dialog.getBlockedHandler().clearBlocked();
        }
      } );
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.core.runtime.ProgressMonitorWrapper#setBlocked(org.eclipse.core.runtime.IStatus)
     */
    @Override
    public void setBlocked( final IStatus reason ) {
      // If this is a monitor that can report blocking do so.
      // Don't bother with a collector as this should only ever
      // happen once and prevent any more progress.
      final IProgressMonitor pm = getWrappedProgressMonitor();
      if( !( pm instanceof IProgressMonitorWithBlocking ) ) {
        return;
      }
      display.asyncExec( new Runnable() {

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
          ( ( IProgressMonitorWithBlocking )pm ).setBlocked( reason );
          // Do not give a shell as we want it to block until it opens.
          Dialog.getBlockedHandler().showBlocked( pm, reason, currentTask );
        }
      } );
    }
  }

}
