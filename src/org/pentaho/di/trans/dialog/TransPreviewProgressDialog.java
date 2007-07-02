/*
 *
 *
 */

package org.pentaho.di.trans.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.LocalVariables;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;



/**
 * Takes care of displaying a dialog that will handle the wait while previewing a transformation...
 * 
 * @author Matt
 * @since  13-jan-2006
 */
public class TransPreviewProgressDialog
{
    private Shell shell;
    private TransMeta transMeta;
    private String[] previewStepNames;
    private int[] previewSize;
    private Trans trans;
    
    private boolean cancelled;
    private String loggingText;
    private Thread parentThread;
    
    /**
     * Creates a new dialog that will handle the wait while previewing a transformation...
     */
    public TransPreviewProgressDialog(Shell shell, TransMeta transMeta, String previewStepNames[], int previewSize[])
    {
        this.shell = shell;
        this.transMeta = transMeta;
        this.previewStepNames = previewStepNames;
        this.previewSize = previewSize;
        
        cancelled = false;
        
        // Get the parent of the new thread that will start on "Open", *before* this thread starts.
        this.parentThread = Thread.currentThread();
    }
    
    public TransMeta open()
    {
        IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(), parentThread.getName(), true);
                doPreview(monitor);
            }
        };
        
        try
        {
            final ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
            
            // Run something in the background to cancel active database queries, forecably if needed!
            Runnable run = new Runnable()
            {
                public void run()
                {
                    IProgressMonitor monitor = pmd.getProgressMonitor();
                    while (pmd.getShell()==null || ( !pmd.getShell().isDisposed() && !monitor.isCanceled() ))
                    {
                        try { Thread.sleep(100); } catch(InterruptedException e) { };
                    }
                    
                    if (monitor.isCanceled()) // Disconnect and see what happens!
                    {
                        try { trans.stopAll(); } catch(Exception e) {};
                    }
                }
            };
            
            // Start the cancel tracker in the background!
            new Thread(run).start();
            
            pmd.run(true, true, op);
        }
        catch (InvocationTargetException e)
        {
            new ErrorDialog(shell, Messages.getString("TransPreviewProgressDialog.ErrorLoadingTransformation.DialogTitle"), Messages.getString("TransPreviewProgressDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
            transMeta = null;
        }
        catch (InterruptedException e)
        {
            new ErrorDialog(shell, Messages.getString("TransPreviewProgressDialog.ErrorLoadingTransformation.DialogTitle"), Messages.getString("TransPreviewProgressDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
            transMeta = null;
        }

        return transMeta;
    }
    
    private void doPreview(IProgressMonitor progressMonitor)
    {
        LogWriter log = LogWriter.getInstance();
        
        // How many rows do we need?
        int nrRows=0;
        for (int i=0 ; i<previewSize.length ; i++) nrRows+=previewSize[i];
        
        progressMonitor.beginTask(Messages.getString("TransPreviewProgressDialog.Monitor.BeginTask.Title"), 100); //$NON-NLS-1$
        
        // Log preview activity to a String:
        Log4jStringAppender stringAppender = LogWriter.createStringAppender();
        log.addAppender(stringAppender);
        
        // This transformation is ready to run in preview!
        trans = new Trans(log, transMeta, previewStepNames, previewSize);
        trans.execute(null);
        
        int previousPct = 0;
        while (!trans.previewComplete() && !trans.isFinished() && !progressMonitor.isCanceled())
        {
            // How many rows are done?
            int nrDone = 0;
            for (int i=0 ; i<previewSize.length ; i++)
            {
                List<Object[]> buffer = trans.getPreviewRows(previewStepNames[i], 0);
                nrDone+=buffer.size();
            }
            
            int pct = 100*nrDone/nrRows;
            
            int worked = pct - previousPct;
            
            if (worked>0) progressMonitor.worked(worked);
            previousPct = pct;
            
            // Change the percentage...
            try { Thread.sleep(500); } catch(InterruptedException e) {}
            
            if (progressMonitor.isCanceled())
            {
                cancelled=true;
                trans.stopAll();
            }
        }
        
        trans.stopAll();
        
        // Log preview activity to a String:
        log.removeAppender(stringAppender);
        loggingText = stringAppender.getBuffer().toString();
        
        progressMonitor.done();
    }
    
    /**
     * @param stepname the name of the step to get the preview rows for
     * @return A list of rows as the result of the preview run.
     */
    public List<Object[]> getPreviewRows(String stepname)
    {
        return trans.getPreviewRows(stepname, 0);
    }
    
    /**
     * @param stepname the name of the step to get the preview rows for
     * @return A description of the row (metadata)
     */
    public RowMetaInterface getPreviewRowsMeta(String stepname)
    {
        return trans.getPreviewRowsMeta(stepname, 0);
    }

    /**
     * @return true is the preview was cancelled by the user
     */
    public boolean isCancelled()
    {
        return cancelled;
    }
    
    /**
     * @return The logging text from the latest preview run
     */
    public String getLoggingText()
    {
        return loggingText;
    }
    
    /**
     * 
     * @return The transformation object that executed the preview TransMeta
     */
    public Trans getTrans()
    {
       return trans; 
    }
}
