/*
 *
 *
 */

package be.ibridge.kettle.repository.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.Repository;

/**
 * Takes care of displaying a dialog that will handle the wait while creating or upgrading a
 * transformation...
 * 
 * @author Matt
 * @since 13-mrt-2005
 */
public class UpgradeRepositoryProgressDialog
{
    private Shell shell;
    private Repository rep;
    private boolean upgrade;

    /**
     * @deprecated Use the constructor version without <i>log</i> or <i>props</i>
     */
    public UpgradeRepositoryProgressDialog(LogWriter log, Props props, Shell shell, Repository rep, boolean upgrade)
    {
        this(shell, rep, upgrade);
    }
    
    /**
     * Creates a new dialog that will handle the wait while upgrading or creating a repository...
     */
    public UpgradeRepositoryProgressDialog(Shell shell, Repository rep, boolean upgrade)
    {
        this.shell = shell;
        this.rep = rep;
        this.upgrade = upgrade;
    }

    public boolean open()
    {
        boolean retval = true;

        IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                // This is running in a new process: copy some KettleVariables info
                // LocalVariables.getInstance().createKettleVariables(Thread.currentThread(),
                // kettleVariables.getLocalThread(), true);
                // --> don't set variables if not running in different thread --> pmd.run(true,true,
                // op);

                try
                {
                    rep.createRepositorySchema(monitor, upgrade);
                }
                catch (KettleException e)
                {
                    e.printStackTrace();
                    throw new InvocationTargetException(e, Messages.getString("UpgradeRepositoryDialog.Error.CreateUpdate", e.getMessage()));
                }
            }
        };

        try
        {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
            pmd.run(false, false, op);
        }
        catch (InvocationTargetException e)
        {
            LogWriter.getInstance().logError(UpgradeRepositoryProgressDialog.class.toString(), "Error creating/updating repository: " + e.toString());
            e.printStackTrace();
            showErrorDialog(e);

            retval = false;
        }
        catch (InterruptedException e)
        {
            LogWriter.getInstance().logError(UpgradeRepositoryProgressDialog.class.toString(), "Error creating/updating repository: " + e.toString());
            e.printStackTrace();
            showErrorDialog(e);

            retval = false;
        }

        return retval;
    }

    private void showErrorDialog(Exception e)
    {
        String sTitle, sMessage;
        if (upgrade)
        {
            sTitle = Messages.getString("UpgradeRepositoryDialog.ErrorUpgrade.Title");
            sMessage = Messages.getString("UpgradeRepositoryDialog.ErrorUpgrade.Message");
        }
        else
        {
            sTitle = Messages.getString("UpgradeRepositoryDialog.ErrorCreate.Title");
            sMessage = Messages.getString("UpgradeRepositoryDialog.ErrorCreate.Message");
        }

        new ErrorDialog(shell, sTitle, sMessage, e);
    }
}
