/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.symmetriccrypto.symmetriccryptotrans;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

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
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.SymmetricCryptoMeta;



/**
 * Symmetric algorithm 
 * Executes a SymmetricCryptoTrans on the values in the input stream. 
 * Selected calculated values can then be put on the output stream.
 * 
 * @author Samatar
 * @since 5-apr-2003
 *
 */
public class SymmetricCryptoTransMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = SymmetricCryptoTransMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/** Operations type */
	private int operationType;
	
	/**
	 * The operations description
	 */
	public final static String operationTypeDesc[] = {
			BaseMessages.getString(PKG, "SymmetricCryptoTransMeta.operationType.Encrypt"),
			BaseMessages.getString(PKG, "SymmetricCryptoTransMeta.operationType.Decrypt")
			};
	
	/**
	 * The operations type codes
	 */
	public final static String operationTypeCode[] = { "encrypt", "decrypt" };

	public final static int OPERATION_TYPE_ENCRYPT = 0;

	public final static int OPERATION_TYPE_DECRYPT = 1;

	private String algorithm;
	private String schema;
	private String  messageField;

	private String  secretKey;
	private boolean secretKeyInField;
	private String  secretKeyField;

	
	private String  resultfieldname;
	
	private boolean readKeyAsBinary;
	private boolean outputResultAsBinary;

	
	public SymmetricCryptoTransMeta()
	{
		super(); // allocate BaseStepMeta
	}
	   private static int getOperationTypeByCode(String tt) {
			if (tt == null)
				return 0;

			for (int i = 0; i < operationTypeCode.length; i++) {
				if (operationTypeCode[i].equalsIgnoreCase(tt))
					return i;
			}
			return 0;
		}
	 public int getOperationType() {
			return operationType;
		}
		public static int getOperationTypeByDesc(String tt) {
			if (tt == null)
				return 0;

			for (int i = 0; i < operationTypeDesc.length; i++) {
				if (operationTypeDesc[i].equalsIgnoreCase(tt))
					return i;
			}
			// If this fails, try to match using the code.
			return getOperationTypeByCode(tt);
		}
		public void setOperationType(int operationType) {
			this.operationType = operationType;
		}
		public static String getOperationTypeDesc(int i) {
			if (i < 0 || i >= operationTypeDesc.length)
				return operationTypeDesc[0];
			return operationTypeDesc[i];
		}

    
    /**
     * @return Returns the XSL filename.
     */
    public String getSecretKeyField()
    {
        return secretKeyField;
    }
  
    public void setSecretKey(String secretKeyin)
    {
    	this.secretKey=secretKeyin;
    }
    
    
    public String getSecretKey()
    {
        return secretKey;
    }
    
    
    public String getResultfieldname()
    {
    	return resultfieldname;
    }

    
    public String getMessageFied()
    {
    	return messageField;
    }
    
    public String getAlgorithm()
    {
    	return algorithm;
    }
    /**
     * @param algorithm The algorithm to set.
     */
    public void setAlgorithm(String algorithm)
    {
        this.algorithm = algorithm;
    }
    
    
    public boolean isReadKeyAsBinary()
    {
    	return readKeyAsBinary;
    }
    /**
     * @param readKeyAsBinary The readKeyAsBinary to set.
     */
    public void setReadKeyAsBinary(boolean readKeyAsBinary)
    {
        this.readKeyAsBinary = readKeyAsBinary;
    }
    
    public boolean isOutputResultAsBinary()
    {
    	return outputResultAsBinary;
    }
    /**
     * @param outputResultAsBinary The outputResultAsBinary to set.
     */
    public void setOutputResultAsBinary(boolean outputResultAsBinary)
    {
        this.outputResultAsBinary = outputResultAsBinary;
    }
    public String getSchema()
    {
    	return schema;
    }
    /**
     * @param schema The schema to set.
     */
    public void setSchema(String schema)
    {
        this.schema = schema;
    }
    
    
    /**
     * @param secretKeyField The secretKeyField to set.
     */
    public void setsecretKeyField(String secretKeyField)
    {
        this.secretKeyField = secretKeyField;
    }
    
    
    public void setResultfieldname(String resultfield)
    {
        this.resultfieldname = resultfield;
    }
    

    
    public void setMessageField(String fieldnamein)
    {
        this.messageField =  fieldnamein;
    }
    
	 public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	  throws KettleXMLException
	{
		readData(stepnode);
	}

	

	public Object clone()
	{
		SymmetricCryptoTransMeta retval = (SymmetricCryptoTransMeta)super.clone();
		

		return retval;
	}
	
    
    
    public boolean isSecretKeyInField()
    {
        return secretKeyInField;
    }
    
      
    public void setSecretKeyInField(boolean secretKeyInField)
    {
        this.secretKeyInField = secretKeyInField;
    }
    
  
    
   
    
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{	
			operationType = getOperationTypeByCode(Const.NVL(XMLHandler.getTagValue(stepnode,	"operation_type"), ""));
			algorithm     = XMLHandler.getTagValue(stepnode, "algorithm");
			schema   = XMLHandler.getTagValue(stepnode, "schema");
			secretKeyField     = XMLHandler.getTagValue(stepnode, "secretKeyField"); //$NON-NLS-1$
			messageField     = XMLHandler.getTagValue(stepnode, "messageField"); //$NON-NLS-1$
			resultfieldname     = XMLHandler.getTagValue(stepnode, "resultfieldname"); //$NON-NLS-1$
			
			setSecretKey(Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "secretKey")));
			secretKeyInField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "secretKeyInField"));	
			readKeyAsBinary = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "readKeyAsBinary"));	
			outputResultAsBinary = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "outputResultAsBinary"));	
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "SymmetricCryptoTransMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		secretKeyField = null; //$NON-NLS-1$
		messageField = null;
		resultfieldname="result";
		secretKey=null;
		secretKeyInField=false;	
	    operationType=OPERATION_TYPE_ENCRYPT;
	    algorithm=SymmetricCryptoMeta.TYPE_ALGORYTHM_CODE[0];
	    schema=algorithm;
	    readKeyAsBinary=false;
	    outputResultAsBinary=false;
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{

        if (!Const.isEmpty(getResultfieldname())) {
        	int type= ValueMeta.TYPE_STRING;
        	if(isOutputResultAsBinary()) type= ValueMeta.TYPE_BINARY;
        	ValueMetaInterface v = new ValueMeta(getResultfieldname(), type);
            v.setOrigin(origin);
            rowMeta.addValueMeta(v); 
        }
    }

	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
        retval.append("    "+XMLHandler.addTagValue("operation_type",getOperationTypeCode(operationType)));
        retval.append("    "+XMLHandler.addTagValue("algorithm", algorithm));
        retval.append("    "+XMLHandler.addTagValue("schema", schema));
		retval.append("    "+XMLHandler.addTagValue("secretKeyField", secretKeyField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("messageField", messageField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    ").append(XMLHandler.addTagValue("secretKey", Encr.encryptPasswordIfNotUsingVariables(secretKey)));
		
		retval.append("    "+XMLHandler.addTagValue("secretKeyInField",  secretKeyInField));
		retval.append("    "+XMLHandler.addTagValue("readKeyAsBinary",  readKeyAsBinary));
		retval.append("    "+XMLHandler.addTagValue("outputResultAsBinary",  outputResultAsBinary));
		
		

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
        	operationType = getOperationTypeByCode(Const.NVL(rep.getStepAttributeString(id_step, "operation_type"), ""));
        	algorithm     = rep.getStepAttributeString(id_step, "algorithm");
        	schema    = rep.getStepAttributeString(id_step, "schema");
        	secretKeyField     = rep.getStepAttributeString(id_step, "secretKeyField"); //$NON-NLS-1$
			messageField     = rep.getStepAttributeString(id_step, "messageField"); //$NON-NLS-1$
			resultfieldname     = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
			
			secretKey              = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString (id_step, "secretKey") );	
			secretKeyInField    =      rep.getStepAttributeBoolean(id_step, "secretKeyInField"); 
			readKeyAsBinary    =      rep.getStepAttributeBoolean(id_step, "readKeyAsBinary"); 
			outputResultAsBinary  =      rep.getStepAttributeBoolean(id_step, "outputResultAsBinary"); 
			

		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "SymmetricCryptoTransMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	private static String getOperationTypeCode(int i) {
		if (i < 0 || i >= operationTypeCode.length)
			return operationTypeCode[0];
		return operationTypeCode[i];
	}
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
            rep.saveStepAttribute(id_transformation, id_step, "operation_type", getOperationTypeCode(operationType));
            rep.saveStepAttribute(id_transformation, id_step, "algorithm", algorithm);
            rep.saveStepAttribute(id_transformation, id_step, "schema", schema);
            
            rep.saveStepAttribute(id_transformation, id_step, "secretKeyField", secretKeyField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "messageField", messageField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultfieldname); //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "secretKey", Encr.encryptPasswordIfNotUsingVariables(secretKey));
			rep.saveStepAttribute(id_transformation, id_step, "secretKeyInField",  secretKeyInField);
			rep.saveStepAttribute(id_transformation, id_step, "readKeyAsBinary",  readKeyAsBinary);
			rep.saveStepAttribute(id_transformation, id_step, "outputResultAsBinary",  outputResultAsBinary);
			
			

		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "SymmetricCryptoTransMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	 public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo,
	            RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	  {

		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SymmetricCryptoTransMeta.CheckResult.ConnectedStepOK",String.valueOf(prev.size())), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
		}
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SymmetricCryptoTransMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$

        }
        remarks.add(cr);
		
		//	Check if The result field is given
		if (getResultfieldname()==null)
		{
			 // Result Field is missing !
			  cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SymmetricCryptoTransMeta.CheckResult.ErrorResultFieldNameMissing"), stepinfo); //$NON-NLS-1$
	          remarks.add(cr);
		
		}		
	}


	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new SymmetricCryptoTrans(stepMeta, stepDataInterface, cnr, transMeta, trans);
        
	}

	public StepDataInterface getStepData()
	{
		return new SymmetricCryptoTransData();
	}
    
 

    public boolean supportsErrorHandling()
    {
        return true;
    }
}
