package be.ibridge.kettle.trans;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import be.ibridge.kettle.cluster.SlaveServer;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;

public class TransExecutionConfiguration implements Cloneable
{
    private boolean executingLocally;
    private boolean     localPreviewing;
    
    private boolean executingRemotely;
    private SlaveServer remoteServer;
    
    private boolean executingClustered;
    private boolean     clusterPosting;
    private boolean     clusterPreparing;
    private boolean     clusterStarting;
    private boolean     clusterShowingTransformation;
    
    private Row  arguments;
    private Row  variables;
    
    private String[] previewSteps;
    private int[]    previewSizes;
    private Date     replayDate;
    private boolean  safeModeEnabled;
    private int      logLevel;

    public TransExecutionConfiguration()
    {
        clusterPosting = true;
        clusterPreparing = true;
        clusterStarting = true;
        clusterShowingTransformation = true;
        
        arguments = new Row();
        variables = new Row();
        
        previewSteps = new String[0];
        previewSizes = new int[0];
    }
    
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * @return the arguments
     */
    public Row getArguments()
    {
        return arguments;
    }

    /**
     * @param arguments the arguments to set
     */
    public void setArguments(Row arguments)
    {
        this.arguments = arguments;
    }

    /**
     * @return the clusteredExecution
     */
    public boolean isExecutingClustered()
    {
        return executingClustered;
    }

    /**
     * @param clusteredExecution the clusteredExecution to set
     */
    public void setExecutingClustered(boolean clusteredExecution)
    {
        this.executingClustered = clusteredExecution;
    }

    /**
     * @return the notExecuting
     */
    public boolean isClusterStarting()
    {
        return clusterStarting;
    }

    /**
     * @param notExecuting the notExecuting to set
     */
    public void setClusterStarting(boolean notExecuting)
    {
        this.clusterStarting = notExecuting;
    }

    /**
     * @return the previewTransformation
     */
    public boolean isLocalPreviewing()
    {
        return localPreviewing;
    }

    /**
     * @param previewTransformation the previewTransformation to set
     */
    public void setLocalPreviewing(boolean previewTransformation)
    {
        this.localPreviewing = previewTransformation;
    }

    /**
     * @return the showingTransformations
     */
    public boolean isClusterShowingTransformation()
    {
        return clusterShowingTransformation;
    }

    /**
     * @param showingTransformations the showingTransformations to set
     */
    public void setClusterShowingTransformation(boolean showingTransformations)
    {
        this.clusterShowingTransformation = showingTransformations;
    }

    /**
     * @return the variables
     */
    public Row getVariables()
    {
        return variables;
    }

    /**
     * @param variables the variables to set
     */
    public void setVariables(Row variables)
    {
        this.variables = variables;
    }

    /**
     * @return the remoteExecution
     */
    public boolean isExecutingRemotely()
    {
        return executingRemotely;
    }

    /**
     * @param remoteExecution the remoteExecution to set
     */
    public void setExecutingRemotely(boolean remoteExecution)
    {
        this.executingRemotely = remoteExecution;
    }

    /**
     * @return the clusterPosting
     */
    public boolean isClusterPosting()
    {
        return clusterPosting;
    }

    /**
     * @param clusterPosting the clusterPosting to set
     */
    public void setClusterPosting(boolean clusterPosting)
    {
        this.clusterPosting = clusterPosting;
    }

    /**
     * @return the localExecution
     */
    public boolean isExecutingLocally()
    {
        return executingLocally;
    }

    /**
     * @param localExecution the localExecution to set
     */
    public void setExecutingLocally(boolean localExecution)
    {
        this.executingLocally = localExecution;
    }

    /**
     * @return the clusterPreparing
     */
    public boolean isClusterPreparing()
    {
        return clusterPreparing;
    }

    /**
     * @param clusterPreparing the clusterPreparing to set
     */
    public void setClusterPreparing(boolean clusterPreparing)
    {
        this.clusterPreparing = clusterPreparing;
    }

    /**
     * @return the remoteServer
     */
    public SlaveServer getRemoteServer()
    {
        return remoteServer;
    }

    /**
     * @param remoteServer the remoteServer to set
     */
    public void setRemoteServer(SlaveServer remoteServer)
    {
        this.remoteServer = remoteServer;
    }
    
    public void getUsedVariables(TransMeta transMeta)
    {
        Properties sp = new Properties();
        KettleVariables kettleVariables = KettleVariables.getInstance();
        sp.putAll(kettleVariables.getProperties());
        sp.putAll(System.getProperties());
 
        List vars = transMeta.getUsedVariables();
        if (vars!=null && vars.size()>0)
        {
            variables = new Row();
            for (int i=0;i<vars.size();i++) 
            {
                String varname = (String)vars.get(i);
                if (!varname.startsWith(Const.INTERNAL_VARIABLE_PREFIX))
                {
                    Value varval = new Value(varname, sp.getProperty(varname, ""));
                    variables.addValue( varval );
                }
            }
        }
    }
    
    public void getUsedArguments(TransMeta transMeta, String[] commandLineArguments)
    {
        // OK, see if we need to ask for some arguments first...
        //
        arguments = transMeta.getUsedArguments(commandLineArguments);
    }

    public void setPreviewStepSizes(String[] previewSteps, int[] previewSizes)
    {
        this.previewSteps = previewSteps;
        this.previewSizes = previewSizes;
    }

    /**
     * @return the previewSizes
     */
    public int[] getPreviewSizes()
    {
        return previewSizes;
    }

    /**
     * @param previewSizes the previewSizes to set
     */
    public void setPreviewSizes(int[] previewSizes)
    {
        this.previewSizes = previewSizes;
    }

    /**
     * @return the previewSteps
     */
    public String[] getPreviewSteps()
    {
        return previewSteps;
    }

    /**
     * @param previewSteps the previewSteps to set
     */
    public void setPreviewSteps(String[] previewSteps)
    {
        this.previewSteps = previewSteps;
    }

    /**
     * @return the replayDate
     */
    public Date getReplayDate()
    {
        return replayDate;
    }

    /**
     * @param replayDate the replayDate to set
     */
    public void setReplayDate(Date replayDate)
    {
        this.replayDate = replayDate;
    }

    /**
     * @return the usingSafeMode
     */
    public boolean isSafeModeEnabled()
    {
        return safeModeEnabled;
    }

    /**
     * @param usingSafeMode the usingSafeMode to set
     */
    public void setSafeModeEnabled(boolean usingSafeMode)
    {
        this.safeModeEnabled = usingSafeMode;
    }

    /**
     * @return the logLevel
     */
    public int getLogLevel()
    {
        return logLevel;
    }

    /**
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(int logLevel)
    {
        this.logLevel = logLevel;
    }
}
