package org.pentaho.di.trans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.w3c.dom.Node;


public class TransExecutionConfiguration implements Cloneable
{
    public static final String XML_TAG = "transformation_execution_configuration";
    
    private boolean executingLocally;
    
    private boolean executingRemotely;
    private SlaveServer remoteServer;
    
    private boolean executingClustered;
    private boolean     clusterPosting;
    private boolean     clusterPreparing;
    private boolean     clusterStarting;
    private boolean     clusterShowingTransformation;
    
    private Map<String, String> arguments;
    private Map<String, String> variables;
    
    private Date     replayDate;
    private boolean  safeModeEnabled;
    private int      logLevel;
    
    private TransDebugMeta transDebugMeta;

    public TransExecutionConfiguration()
    {
    	executingLocally = true;
    	
        clusterPosting = true;
        clusterPreparing = true;
        clusterStarting = true;
        clusterShowingTransformation = false;
        
        arguments = new HashMap<String, String>();
        variables = new HashMap<String, String>();
        
        transDebugMeta = null;
        
        logLevel = LogWriter.LOG_LEVEL_BASIC;
    }
    
    public Object clone()
    {
        try
        {
            TransExecutionConfiguration configuration = (TransExecutionConfiguration) super.clone();
            
            configuration.arguments = new HashMap<String, String>();
            configuration.arguments.putAll(arguments);
            
            configuration.variables = new HashMap<String, String>();
            configuration.variables.putAll(variables);
            
            return configuration;
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * @return the arguments
     */
    public Map<String, String> getArguments()
    {
        return arguments;
    }

    /**
     * @param arguments the arguments to set
     */
    public void setArguments(Map<String, String> arguments)
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
    public Map<String, String> getVariables()
    {
        return variables;
    }

    /**
     * @param variables the variables to set
     */
    public void setVariables(Map<String, String> variables)
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
        VariableSpace space = Variables.getADefaultVariableSpace();
        
        String keys[] = space.listVariables();
        for ( int i=0; i<keys.length; i++ )
        {
            sp.put(keys[i], space.getVariable(keys[i]));
        }
 
        List<String> vars = transMeta.getUsedVariables();
        if (vars!=null && vars.size()>0)
        {
        	HashMap<String, String> newVariables = new HashMap<String, String>();
        	
            for (int i=0;i<vars.size();i++) 
            {
                String varname = (String)vars.get(i);
                if (!varname.startsWith(Const.INTERNAL_VARIABLE_PREFIX))
                {
                	newVariables.put(varname, Const.NVL(variables.get(varname), sp.getProperty(varname, "")));
                }
            }
            // variables.clear();
            variables.putAll(newVariables);
        }
    }
    
    public void getUsedArguments(TransMeta transMeta, String[] commandLineArguments)
    {
        // OK, see if we need to ask for some arguments first...
        //
        arguments = transMeta.getUsedArguments(commandLineArguments);
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
    
    public String getXML()
    {
        StringBuffer xml = new StringBuffer(160);
        
        xml.append("  <"+XML_TAG+">").append(Const.CR);
        
        xml.append("    ").append(XMLHandler.addTagValue("exec_local", executingLocally));
        
        xml.append("    ").append(XMLHandler.addTagValue("exec_remote", executingRemotely));
        if (remoteServer!=null)
        {
            xml.append("    ").append(remoteServer.getXML()).append(Const.CR);
        }
        
        xml.append("    ").append(XMLHandler.addTagValue("exec_cluster", executingClustered));
        xml.append("    ").append(XMLHandler.addTagValue("cluster_post", clusterPosting));
        xml.append("    ").append(XMLHandler.addTagValue("cluster_prepare", clusterPreparing));
        xml.append("    ").append(XMLHandler.addTagValue("cluster_start", clusterStarting));
        xml.append("    ").append(XMLHandler.addTagValue("cluster_show_trans", clusterShowingTransformation));

        // Serialize the variables...
        //
        xml.append("    <variables>").append(Const.CR);
        List<String> variableNames = new ArrayList<String>(variables.keySet());
        Collections.sort(variableNames);
        for (String name : variableNames) {
        	String value = variables.get(name);
        	xml.append("    <variable>");
        	xml.append(XMLHandler.addTagValue("name", name, false));
        	xml.append(XMLHandler.addTagValue("value", value, false));
        	xml.append("</variable>").append(Const.CR);
        }
        xml.append("    </variables>").append(Const.CR);

        // Serialize the variables...
        //
        xml.append("    <arguments>").append(Const.CR);
        List<String> argumentNames = new ArrayList<String>(arguments.keySet());
        Collections.sort(argumentNames);
        for (String name : argumentNames) {
        	String value = arguments.get(name);
        	xml.append("    <argument>");
        	xml.append(XMLHandler.addTagValue("name", name, false));
        	xml.append(XMLHandler.addTagValue("value", value, false));
        	xml.append("</argument>").append(Const.CR);
        }
        xml.append("    </arguments>").append(Const.CR);

        // IMPORTANT remote debugging is not yet supported
        //
        // xml.append(transDebugMeta.getXML());
        
        xml.append("    ").append(XMLHandler.addTagValue("replay_date", replayDate));
        xml.append("    ").append(XMLHandler.addTagValue("safe_mode", safeModeEnabled));
        xml.append("    ").append(XMLHandler.addTagValue("log_level", LogWriter.getLogLevelDesc(logLevel)));
        
        xml.append("</"+XML_TAG+">").append(Const.CR);
        return xml.toString();
    }
    
    public TransExecutionConfiguration(Node trecNode)
    {
    	this();
    	
        executingLocally = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "exec_local"));

        executingRemotely = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "exec_remote"));
        Node remoteHostNode = XMLHandler.getSubNode(trecNode, SlaveServer.XML_TAG);
        if (remoteHostNode!=null)
        {
            remoteServer = new SlaveServer(remoteHostNode);
        }

        executingClustered = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "exec_cluster"));
        clusterPosting = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "cluster_post"));
        clusterPreparing = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "cluster_prepare"));
        clusterStarting = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "cluster_start"));
        clusterShowingTransformation = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "cluster_show_trans"));

        // Read the variables...
        //
        Node varsNode = XMLHandler.getSubNode(trecNode, "variables");
        int nrVariables = XMLHandler.countNodes(varsNode, "variable");
        for (int i=0;i<nrVariables;i++) {
        	Node argNode = XMLHandler.getSubNodeByNr(varsNode, "variable", i);
        	String name = XMLHandler.getTagValue(argNode, "name");
        	String value = XMLHandler.getTagValue(argNode, "value");
        	if (!Const.isEmpty(name) && !Const.isEmpty(value)) {
        		variables.put(name, value);
        	}
        }
        
        // Read the arguments...
        //
        Node argsNode = XMLHandler.getSubNode(trecNode, "arguments");
        int nrArguments = XMLHandler.countNodes(argsNode, "argument");
        for (int i=0;i<nrArguments;i++) {
        	Node argNode = XMLHandler.getSubNodeByNr(argsNode, "argument", i);
        	String name = XMLHandler.getTagValue(argNode, "name");
        	String value = XMLHandler.getTagValue(argNode, "value");
        	if (!Const.isEmpty(name) && !Const.isEmpty(value)) {
        		arguments.put(name, value);
        	}
        }
        
        // IMPORTANT: remote preview and remote debugging is NOT yet supported.
        //

        replayDate = XMLHandler.stringToDate( XMLHandler.getTagValue(trecNode, "replay_date") );
        safeModeEnabled = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "safe_mode"));
        logLevel = LogWriter.getLogLevel( XMLHandler.getTagValue(trecNode, "log_level") );
    }

    
    public String[] getArgumentStrings()
    {
        if (arguments==null || arguments.size()==0) return null;
        
        String[] argNames = arguments.keySet().toArray(new String[arguments.size()]);
        Arrays.sort(argNames);
        
        String[] values = new String[argNames.length];
        for (int i=0;i<argNames.length;i++) {
        	if (argNames[i].equalsIgnoreCase(Props.STRING_ARGUMENT_NAME_PREFIX+(i+1))) {
        		values[i] = arguments.get(argNames[i]);
        	}
        }
        
        return values;
    }

	/**
	 * @return the transDebugMeta
	 */
	public TransDebugMeta getTransDebugMeta() {
		return transDebugMeta;
	}

	/**
	 * @param transDebugMeta the transDebugMeta to set
	 */
	public void setTransDebugMeta(TransDebugMeta transDebugMeta) {
		this.transDebugMeta = transDebugMeta;
	}

}
