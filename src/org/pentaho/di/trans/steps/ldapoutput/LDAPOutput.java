/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/


package org.pentaho.di.trans.steps.ldapoutput;


import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import org.pentaho.di.trans.steps.ldapinput.LDAPConnection;
import org.pentaho.di.core.encryption.Encr;

/**
 * Write to LDAP.
 * 
 * @author Samatar
 * @since 21-09-2007
 */
public class LDAPOutput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = LDAPOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private LDAPOutputMeta meta;
	private LDAPOutputData data;

	
	public LDAPOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
       
		
		 // Grab a row and also wait for a row to be finished.
        Object[] outputRowData= getRow();
        
        if(outputRowData==null) {
			setOutputDone();
			return false;
		}
		if (first) {
            first = false;
        	
            if(meta.getOperationType()!=LDAPOutputMeta.OPERATION_TYPE_DELETE && meta.getOperationType()!=LDAPOutputMeta.OPERATION_TYPE_RENAME) {
            	
	    		// get total fields in the grid
				data.nrfields = meta.getUpdateLookup().length;
					
				// Check if field list is filled 
				if (data.nrfields==0) {
					throw new KettleException(BaseMessages.getString(PKG, "LDAPOutputUpdateDialog.FieldsMissing.DialogMessage"));
				}
	            
				// Take care of variable
				data.fieldsAttribute = new String[data.nrfields];
				// Build the mapping of input position to field name
				data.fieldStream = new int[data.nrfields];
				
				// Fields to update
				List<Integer> fieldsToUpdateInStreaml= new ArrayList<Integer>();
				List<String> fieldsToUpdateAttributel= new ArrayList<String>();
				
				for (int i = 0; i < data.nrfields; i++) {
					
					data.fieldStream[i] = getInputRowMeta().indexOfValue(environmentSubstitute(meta.getUpdateStream()[i]));
					if (data.fieldStream[i] < 0) {
						throw new KettleException("Field [" + meta.getUpdateStream()[i]+ "] couldn't be found in the input stream!");
					}
					data.fieldsAttribute[i]=environmentSubstitute(meta.getUpdateLookup()[i]);
					
					if(meta.getOperationType()==LDAPOutputMeta.OPERATION_TYPE_UPSERT) {
						if (meta.getUpdate()[i].booleanValue() ) {
							// We need also to keep care of the fields to update
							fieldsToUpdateInStreaml.add(data.fieldStream[i]);
							fieldsToUpdateAttributel.add(data.fieldsAttribute[i]);
						}
					}
				}
				
				data.nrfieldsToUpdate=fieldsToUpdateInStreaml.size();
				if(data.nrfieldsToUpdate>0) {
					data.fieldStreamToUpdate = new int[data.nrfieldsToUpdate];
					data.fieldsAttributeToUpdate = new String[data.nrfieldsToUpdate];
					for(int i=0; i<fieldsToUpdateInStreaml.size(); i++) {
						data.fieldStreamToUpdate[i] = (Integer)fieldsToUpdateInStreaml.get(i);
						data.fieldsAttributeToUpdate[i] = (String)fieldsToUpdateAttributel.get(i);
					}
				}
				fieldsToUpdateInStreaml=null;
				fieldsToUpdateAttributel=null;
				
				data.attributes = new String[data.nrfields];
				if(meta.getOperationType()==LDAPOutputMeta.OPERATION_TYPE_UPSERT && data.nrfieldsToUpdate>0) {
					data.attributesToUpdate = new String[data.nrfieldsToUpdate];
				}
            }
			
            if(meta.getOperationType()==LDAPOutputMeta.OPERATION_TYPE_RENAME) {
            	 String oldDnField=environmentSubstitute(meta.getOldDnFieldName());
            	 if(Const.isEmpty(oldDnField)) {
 					throw new KettleException(BaseMessages.getString(PKG, "LDAPOutput.Error.OldDNFieldMissing"));
 				 }
            	 
            	 String newDnField=environmentSubstitute(meta.getNewDnFieldName());
            	 if(Const.isEmpty(newDnField)) {
 					throw new KettleException(BaseMessages.getString(PKG, "LDAPOutput.Error.NewDNFieldMissing"));
 				 }
            	 
            	// return the index of the field in the input stream
 	        	data.indexOfOldDNField = getInputRowMeta().indexOfValue(oldDnField);
 	        
 				if(data.indexOfOldDNField<0) {
 					// the field is unreachable!
 					throw new KettleException(BaseMessages.getString(PKG, "LDAPOutput.Error.CanNotFindField", oldDnField));
 				}
 				// return the index of the field in the input stream
 	        	data.indexOfNewDNField = getInputRowMeta().indexOfValue(newDnField);
 	        
 				if(data.indexOfNewDNField<0) {
 					// the field is unreachable!
 					throw new KettleException(BaseMessages.getString(PKG, "LDAPOutput.Error.CanNotFindField", newDnField));
 				}
            	 
            }else {
	            String dnField=environmentSubstitute(meta.getDnField());
	        	// Check Dn field
				if(Const.isEmpty(dnField)) {
					throw new KettleException(BaseMessages.getString(PKG, "LDAPOutput.Error.DNFieldMissing"));
				}
				
	        	// return the index of the field in the input stream
	        	data.indexOfDNField = getInputRowMeta().indexOfValue(dnField);
	        
				if(data.indexOfDNField<0) {
					// the field is unreachable!
					throw new KettleException(BaseMessages.getString(PKG, "LDAPOutput.Error.CanNotFindField", dnField));
				}
            }
				
        }
        
		incrementLinesInput();
		String dn=null;
		
		try {	
			if(meta.getOperationType()!= LDAPOutputMeta.OPERATION_TYPE_RENAME) {
				// Get DN
				dn = getInputRowMeta().getString(outputRowData, data.indexOfDNField);
				
				if(isDebug()) logDebug(BaseMessages.getString(PKG, "LDAPOutput.ProcessDn", dn));
				
				if(meta.getOperationType()!=LDAPOutputMeta.OPERATION_TYPE_DELETE) {
					// Build new value attributes
					for(int i=0; i<data.nrfields; i++) {
						data.attributes[i]=getInputRowMeta().getString(outputRowData, data.fieldStream[i]);
					}
				}
			}
			switch (meta.getOperationType()) {
				case LDAPOutputMeta.OPERATION_TYPE_UPSERT:
					// handle fields to update
					for(int i=0; i<data.nrfieldsToUpdate; i++) {
						data.attributesToUpdate[i]=getInputRowMeta().getString(outputRowData, data.fieldStreamToUpdate[i]);
					}
					int status = data.connection.upsert(dn, data.fieldsAttribute, data.attributes, 
							data.fieldsAttributeToUpdate, data.attributesToUpdate, data.separator);
					switch (status) {
						case LDAPConnection.STATUS_INSERTED:
							 incrementLinesOutput();
							break;
						case LDAPConnection.STATUS_UPDATED:
							 incrementLinesUpdated();
							break;
						default:
							 incrementLinesSkipped();
							break;
					}
					break;
				case LDAPOutputMeta.OPERATION_TYPE_UPDATE:
					status = data.connection.update(dn, data.fieldsAttribute, data.attributes, meta.isFailIfNotExist());
					switch (status) {
					case LDAPConnection.STATUS_UPDATED:
						 incrementLinesUpdated();
						break;
					default:
						 incrementLinesSkipped();
						break;
					}
					break;
				case LDAPOutputMeta.OPERATION_TYPE_ADD:
					status = data.connection.add(dn, data.fieldsAttribute, data.attributes, data.separator, meta.isFailIfNotExist());
					switch (status) {
					case LDAPConnection.STATUS_ADDED:
						 incrementLinesUpdated();
						break;
					default:
						 incrementLinesSkipped();
						break;
					}
					break;
				case LDAPOutputMeta.OPERATION_TYPE_DELETE:
					status = data.connection.delete(dn, meta.isFailIfNotExist());
					switch (status) {
					case LDAPConnection.STATUS_DELETED:
						incrementLinesUpdated();
						break;
					default:
						 incrementLinesSkipped();
						break;
					}
					break;
				case LDAPOutputMeta.OPERATION_TYPE_RENAME:
					String oldDn= getInputRowMeta().getString(outputRowData, data.indexOfOldDNField);
					String newDn= getInputRowMeta().getString(outputRowData, data.indexOfNewDNField);
					
					data.connection.rename(oldDn, newDn, meta.isDeleteRDN());
					incrementLinesOutput();
					break;
				default:
					// Insert
					data.connection.insert(dn, data.fieldsAttribute, data.attributes, data.separator);
					 incrementLinesOutput();
					break;
			}
			
			putRow(getInputRowMeta(), outputRowData);  // copy row to output rowset(s);

			if(log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "LDAPOutput.log.ReadRow"), getInputRowMeta().getString(outputRowData));
			
		    if (checkFeedback(getLinesInput())) {
		    	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "LDAPOutput.log.LineRow") + getLinesInput());
		    }
		    
		    return true; 
		    
		 }  catch(Exception e){
			 
	        boolean sendToErrorRow=false;
			String errorMessage = null;
			
			if (getStepMeta().isDoingErrorHandling()){
		         sendToErrorRow = true;
		         errorMessage = e.toString();
			} else {
				logError(BaseMessages.getString(PKG, "LDAPOutput.log.Exception", e.getMessage()));
				setErrors(1);
				logError(Const.getStackTracker(e));
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;				
			}
			if (sendToErrorRow) {
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), outputRowData, 1, errorMessage, null, "LDAPOutput001");
			}
		} 
		return true;
	}		
	

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LDAPOutputMeta)smi;
		data=(LDAPOutputData)sdi;
		
		if (super.init(smi, sdi)) {
			try {
					// Define new LDAP connection
					data.connection = new LDAPConnection(log, environmentSubstitute(meta.getHost()), 
							Const.toInt(environmentSubstitute(meta.getPort()), LDAPConnection.DEFAULT_PORT));
					 
					data.connection.setReferral(LDAPOutputMeta.getReferralTypeCode(meta.getReferralType()));
					data.connection.setDerefAliases(LDAPOutputMeta.getDerefAliasesCode(meta.getDerefAliasesType()));
					
					data.connection.setProtocol(LDAPConnection.getProtocolFromCode(meta.getProtocol()));
				    if(meta.isUseCertificate()) {
				    	data.connection.setTrustStorePath(meta.getTrustStorePath());
				    	data.connection.setTrustStorePassword(meta.getTrustStorePassword());
				    	data.connection.trustAllCertificates(meta.isTrustAllCertificates());
				    }
					
					// connect
			       if (meta.UseAuthentication()) {
			   			String username=environmentSubstitute(meta.getUserName());
			   			String password=Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(meta.getPassword()));
						data.connection.connect(username, password);
			       }else {
						data.connection.connect();
			       }
			       data.separator= environmentSubstitute(meta.getMultiValuedSeparator());
			}catch(Exception e) {
    			logError(BaseMessages.getString(PKG, "LDAPOutput.Error.Init", e));
				stopAll();
				setErrors(1);
    			return false;
			}
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LDAPOutputMeta)smi;
		data=(LDAPOutputData)sdi;
		if(data.connection!=null) {
			try {
				// Close connection
				data.connection.close();
			} catch (KettleException e) {
	             logError(BaseMessages.getString(PKG, "LDAPOutput.Exception.ErrorDisconecting",e.toString()));
			}
		}
		
		super.dispose(smi, sdi);
	}
		
}
