package org.pentaho.di.trans.steps.ssh;

import org.w3c.dom.Node;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/*
 * Created on 03-Juin-2008
 * 
 */

public class SSHMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = SSHMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	private static int DEFAULT_PORT= 22;
	
	private String command;
	private boolean dynamicCommandField;
    /** dynamic command fieldname */
    private String       commandfieldname;
    
	private String serverName;
	private String port;
    private String userName;
    private String password;
    // key
    private boolean usePrivateKey;
    private String keyFileName;
    private String passPhrase;

    private String stdOutFieldName;
    private String stdErrFieldName;
    private String timeOut;
    //Proxy
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    public SSHMeta()
    {
        super(); // allocate BaseStepMeta
    }

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

    public Object clone()
    {
        SSHMeta retval = (SSHMeta) super.clone();
       
        return retval;
    }

    public void setDefault()
    {
    	dynamicCommandField=false;
    	command=null;
        commandfieldname=null;
		port=String.valueOf(DEFAULT_PORT);
		serverName=null;
		userName=null;
		password=null;
		usePrivateKey=true;
		keyFileName=null;
		stdOutFieldName="stdOut";
		stdErrFieldName="stdErr";
		timeOut="0";
		proxyHost=null;
		proxyPort=null;
		proxyUsername=null;
		proxyPassword=null;
    }

	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return serverName;
	}

	/**
	 * @param serverName The serverName to set.
	 */
	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}
	/**
	 * @return Returns the userName.
	 */
	public String getuserName()
	{
		return userName;
	}
	/**
	 * @param userName The userName to set.
	 */
	public void setuserName(String userName)
	{
		this.userName = userName;
	}
	/**
	 * @param password The password to set.
	 */
	public void setpassword(String password)
	{
		this.password = password;
	}
	/**
	 * @return Returns the password.
	 */
	public String getpassword()
	{
		return password;
	}

	
	/**
	 * @param commandfieldname The commandfieldname to set.
	 */
	public void setcommandfieldname(String commandfieldname)
	{
		this.commandfieldname = commandfieldname;
	}
	/**
	 * @return Returns the commandfieldname.
	 */
	public String getcommandfieldname()
	{
		return commandfieldname;
	}
	
	/**
	 * @param command The commandfieldname to set.
	 */
	public void setCommand(String value)
	{
		this.command = value;
	}
	/**
	 * @return Returns the command.
	 */
	public String getCommand()
	{
		return command;
	}
	
	/**
	 * @param value The dynamicCommandField to set.
	 */
	public void setDynamicCommand(boolean value)
	{
		this.dynamicCommandField = value;
	}
	/**
	 * @return Returns the dynamicCommandField.
	 */
	public boolean isDynamicCommand()
	{
		return dynamicCommandField;
	}
	
	/**
	 * @return Returns the port.
	 */
	public String getPort()
	{
		return port;
	}

	/**
	 * @param port The port to set.
	 */
	public void setPort(String port)
	{
		this.port = port;
	}

	public void usePrivateKey(boolean value)
	{
		this.usePrivateKey=value;
	}
	/**
	 * @return Returns the usePrivateKey.
	 */
	public boolean isusePrivateKey()
	{
		return usePrivateKey;
	}
	/**
	 * @param value The keyFileName to set.
	 */
	public void setKeyFileName(String value)
	{
		this.keyFileName = value;
	}
	
	/**
	 * @return Returns the keyFileName.
	 */
	public String getKeyFileName()
	{
		return keyFileName;
	}
	/**
	 * @param value The passPhrase to set.
	 */
	public void setPassphrase(String value)
	{
		this.passPhrase = value;
	}
	
	/**
	 * @return Returns the passPhrase.
	 */
	public String getPassphrase()
	{
		return passPhrase;
	}
	/* @param timeOut The timeOut to set.
	 */
	public void setTimeOut(String timeOut)
	{
		this.timeOut = timeOut;
	}
	/**
	 * @return Returns the timeOut.
	 */
	public String getTimeOut()
	{
		return timeOut;
	}
	/**
	 * @param value The stdOutFieldName to set.
	 */
	public void setstdOutFieldName(String value)
	{
		this.stdOutFieldName = value;
	}
	
	/**
	 * @return Returns the stdOutFieldName.
	 */
	public String getStdOutFieldName()
	{
		return stdOutFieldName;
	}
	
	/**
	 * @param value The stdErrFieldName to set.
	 */
	public void setStdErrFieldName(String value)
	{
		this.stdErrFieldName = value;
	}
	
	/**
	 * @return Returns the stdErrFieldName.
	 */
	public String getStdErrFieldName()
	{
		return stdErrFieldName;
	}
	/**
	 * @param value The proxyHost to set.
	 */
	public void setProxyHost(String value)
	{
		this.proxyHost = value;
	}
	
	/**
	 * @return Returns the proxyHost.
	 */
	public String getProxyHost()
	{
		return proxyHost;
	}
	/**
	 * @param value The proxyPort to set.
	 */
	public void setProxyPort(String value)
	{
		this.proxyPort = value;
	}
	
	/**
	 * @return Returns the proxyPort.
	 */
	public String getProxyPort()
	{
		return proxyPort;
	}
	/**
	 * @param value The proxyUsername to set.
	 */
	public void setProxyUsername(String value)
	{
		this.proxyUsername = value;
	}
	
	/**
	 * @return Returns the proxyUsername.
	 */
	public String getProxyUsername()
	{
		return proxyUsername;
	}
	
	/**
	 * @param value The proxyPassword to set.
	 */
	public void setProxyPassword(String value)
	{
		this.proxyPassword = value;
	}
	
	/**
	 * @return Returns the proxyPassword.
	 */
	public String getProxyPassword()
	{
		return proxyPassword;
	}
	
    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        
        retval.append("    " + XMLHandler.addTagValue("dynamicCommandField",   dynamicCommandField));
		retval.append("    " + XMLHandler.addTagValue("command",   command));
        retval.append("    " + XMLHandler.addTagValue("commandfieldname", commandfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("port",   port));
		retval.append("    " + XMLHandler.addTagValue("servername",   serverName));
		retval.append("    " + XMLHandler.addTagValue("userName",   userName));
		retval.append("    ").append(XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(password)));
		retval.append("    " + XMLHandler.addTagValue("usePrivateKey",   usePrivateKey));
		retval.append("    " + XMLHandler.addTagValue("keyFileName",   keyFileName));
		retval.append("    " + XMLHandler.addTagValue("passPhrase",   passPhrase));
		retval.append("    " + XMLHandler.addTagValue("stdOutFieldName",   stdOutFieldName));
		retval.append("    " + XMLHandler.addTagValue("stdErrFieldName",   stdErrFieldName));
		retval.append("    " + XMLHandler.addTagValue("timeOut",   timeOut));
		retval.append("    " + XMLHandler.addTagValue("proxyHost",   proxyHost));
		retval.append("    " + XMLHandler.addTagValue("proxyPort",   proxyPort));
		retval.append("    " + XMLHandler.addTagValue("proxyUsername",   proxyUsername));
		retval.append("    ").append(XMLHandler.addTagValue("proxyPassword", Encr.encryptPasswordIfNotUsingVariables(proxyPassword)));
        return retval.toString();
    }

	private void readData(Node stepnode)
	throws KettleXMLException
	{
		try
		{
			dynamicCommandField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "dynamicCommandField"));
			command = XMLHandler.getTagValue(stepnode, "command"); 
			commandfieldname = XMLHandler.getTagValue(stepnode, "commandfieldname"); 
	      	port          = XMLHandler.getTagValue(stepnode, "port");
			serverName   = XMLHandler.getTagValue(stepnode, "servername");
			userName          = XMLHandler.getTagValue(stepnode, "userName");
			password=	Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "password"));

			usePrivateKey = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "usePrivateKey"));
			keyFileName = XMLHandler.getTagValue(stepnode, "keyFileName");
			passPhrase = XMLHandler.getTagValue(stepnode, "passPhrase");
			stdOutFieldName = XMLHandler.getTagValue(stepnode, "stdOutFieldName");
			stdErrFieldName = XMLHandler.getTagValue(stepnode, "stdErrFieldName");
			timeOut = XMLHandler.getTagValue(stepnode, "timeOut");
			proxyHost = XMLHandler.getTagValue(stepnode, "proxyHost");
			proxyPort = XMLHandler.getTagValue(stepnode, "proxyPort");
			proxyUsername = XMLHandler.getTagValue(stepnode, "proxyUsername");
			proxyPassword=	Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "proxyPassword"));

		}
	    catch (Exception e)
	    {
	        throw new KettleXMLException(BaseMessages.getString(PKG, "SSHMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
	    }
    }
   
    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
    	
        try
        {
        	dynamicCommandField=rep.getJobEntryAttributeBoolean(id_step, "dynamicCommandField");
			command       = rep.getJobEntryAttributeString(id_step, "command");
        	commandfieldname = rep.getStepAttributeString(id_step, "commandfieldname"); //$NON-NLS-1$
			serverName    = rep.getJobEntryAttributeString(id_step, "servername");
        	port          = rep.getJobEntryAttributeString(id_step, "port");
			userName      = rep.getJobEntryAttributeString(id_step, "userName");
			password              = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString (id_step, "password") );	
			
			usePrivateKey=rep.getJobEntryAttributeBoolean(id_step, "usePrivateKey");
			keyFileName=rep.getJobEntryAttributeString(id_step, "keyFileName");
			passPhrase=rep.getJobEntryAttributeString(id_step, "passPhrase");
			stdOutFieldName=rep.getJobEntryAttributeString(id_step, "stdOutFieldName");
			stdErrFieldName=rep.getJobEntryAttributeString(id_step, "stdErrFieldName");
			timeOut=rep.getJobEntryAttributeString(id_step, "timeOut");
			proxyHost=rep.getJobEntryAttributeString(id_step, "proxyHost");
			proxyPort=rep.getJobEntryAttributeString(id_step, "proxyPort");
			proxyUsername=rep.getJobEntryAttributeString(id_step, "proxyUsername");
			proxyPassword              = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString (id_step, "proxyPassword") );	
			
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "SSHMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
        try
        {
        	rep.saveJobEntryAttribute(id_transformation, id_step, "dynamicCommandField", dynamicCommandField);
			rep.saveJobEntryAttribute(id_transformation, id_step, "command",      command);
            rep.saveStepAttribute(id_transformation, id_step, "commandfieldname", commandfieldname); //$NON-NLS-1$
			rep.saveJobEntryAttribute(id_transformation, id_step, "port",      port);
			rep.saveJobEntryAttribute(id_transformation, id_step, "servername",      serverName);
			rep.saveJobEntryAttribute(id_transformation, id_step, "userName",      userName);
			rep.saveStepAttribute(id_transformation, id_step, "password", Encr.encryptPasswordIfNotUsingVariables(password));

			rep.saveJobEntryAttribute(id_transformation, id_step, "usePrivateKey", usePrivateKey);
			rep.saveJobEntryAttribute(id_transformation, id_step, "keyFileName", keyFileName);
			rep.saveJobEntryAttribute(id_transformation, id_step, "passPhrase", passPhrase);
			rep.saveJobEntryAttribute(id_transformation, id_step, "stdOutFieldName", stdOutFieldName);
			rep.saveJobEntryAttribute(id_transformation, id_step, "stdErrFieldName", stdErrFieldName);
			rep.saveJobEntryAttribute(id_transformation, id_step, "timeOut", timeOut);
			rep.saveJobEntryAttribute(id_transformation, id_step, "proxyHost", proxyHost);
			rep.saveJobEntryAttribute(id_transformation, id_step, "proxyPort", proxyPort);
			rep.saveJobEntryAttribute(id_transformation, id_step, "proxyUsername", proxyUsername);
			rep.saveStepAttribute(id_transformation, id_step, "proxyPassword", Encr.encryptPasswordIfNotUsingVariables(proxyPassword));

        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "SSHMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
    	
        CheckResult cr;
        String error_message = ""; //$NON-NLS-1$

        // Target hostname
        if (Const.isEmpty(getServerName()))
        {
            error_message = BaseMessages.getString(PKG, "SSHMeta.CheckResult.TargetHostMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "SSHMeta.CheckResult.TargetHostOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
            remarks.add(cr);
        }
        if(isusePrivateKey())
        {
        	String keyfilename = transMeta.environmentSubstitute(getKeyFileName());
        	if(Const.isEmpty(keyfilename))
        	{
                error_message = BaseMessages.getString(PKG, "SSHMeta.CheckResult.PrivateKeyFileNameMissing"); //$NON-NLS-1$
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
        	}
        	else
        	{
                error_message = BaseMessages.getString(PKG, "SSHMeta.CheckResult.PrivateKeyFileNameOK"); //$NON-NLS-1$
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
                remarks.add(cr);
                boolean keyFileExists=false;
                try {
                	keyFileExists=KettleVFS.fileExists(keyfilename);
                }catch(Exception e){};
                if(!keyFileExists)
                {
                    error_message = BaseMessages.getString(PKG, "SSHMeta.CheckResult.PrivateKeyFileNotExist", keyfilename); //$NON-NLS-1$
                    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                    remarks.add(cr);
                }
                else
                {
                	 error_message = BaseMessages.getString(PKG, "SSHMeta.CheckResult.PrivateKeyFileExists", keyfilename); //$NON-NLS-1$
                     cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
                     remarks.add(cr);
                }
        	}
        }
       
        
        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SSHMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SSHMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }

    }
	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
		
		if(!isDynamicCommand()) {
			row.clear();
		}
		ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getStdOutFieldName()), ValueMetaInterface.TYPE_STRING);
		v.setOrigin(name);
		row.addValueMeta( v );

        String stderrfield = space.environmentSubstitute(getStdErrFieldName());
        if (!Const.isEmpty(stderrfield))
        {
    		v = new ValueMeta(stderrfield, ValueMetaInterface.TYPE_BOOLEAN);
    		v.setOrigin(name);
    		row.addValueMeta( v );
        }
    }
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new SSH(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new SSHData();
    }

    public boolean supportsErrorHandling()
    {
        return true;
    }

	
	public static Connection OpenConnection(String serveur, int port, 
			String username, String password, boolean useKey, String keyFilename, String passPhrase, int timeOut,
			VariableSpace space,
			String proxyhost, int proxyport, String proxyusername, String proxypassword)
	throws KettleException {
		Connection conn = null;
		boolean isAuthenticated= false;
		File keyFile=null;
		try {
			   // perform some checks
			   if(useKey)  {
					if(Const.isEmpty(keyFilename)) {
						throw new KettleException(BaseMessages.getString(PKG, "SSH.Error.PrivateKeyFileMissing"));
					}
					keyFile = new File(keyFilename);
					if(!keyFile.exists()) {
						throw new KettleException(BaseMessages.getString(PKG, "SSH.Error.PrivateKeyNotExist", keyFilename));
					}
				}
				// Create a new connection
				conn = new Connection(serveur, port);
				
				/* We want to connect through a HTTP proxy */
				if(!Const.isEmpty(proxyhost)) {
					/* Now connect */
					// if the proxy requires basic authentication:
					if(!Const.isEmpty(proxyusername)) {
						conn.setProxyData(new HTTPProxyData(proxyhost, proxyport, proxyusername, proxypassword));
					} else {
						conn.setProxyData(new HTTPProxyData(proxyhost, proxyport));
					}
				}
				
				// and connect
				if(timeOut==0)
					conn.connect();
				else
					conn.connect(null, 0,timeOut*1000);
				// authenticate
				if(useKey)  {
					isAuthenticated = conn.authenticateWithPublicKey(username, keyFile,
							space.environmentSubstitute(passPhrase));
				} else {
					isAuthenticated = conn.authenticateWithPassword(username, password);
				}
				if (isAuthenticated == false) throw new KettleException(BaseMessages.getString(PKG, "SSH.Error.AuthenticationFailed", username));	
			}catch(Exception e) { 
				// Something wrong happened
				// do not forget to disconnect if connected
				if(conn!=null) conn.close();
				throw new KettleException(BaseMessages.getString(PKG, "SSH.Error.ErrorConnecting", serveur, username), e);
			}
			return conn;
	  }
	/**
     * Returns the Input/Output metadata for this step.
     * 
     */
    public StepIOMetaInterface getStepIOMeta() {
    	return new StepIOMeta(isDynamicCommand(), true, false, false, false, false);
    }
}
