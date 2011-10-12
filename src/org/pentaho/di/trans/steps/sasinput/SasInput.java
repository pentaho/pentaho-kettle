/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.sasinput;

import java.io.File;
import java.util.ArrayList;

import org.eobjects.sassy.SasColumnType;
import org.eobjects.sassy.SasReader;
import org.eobjects.sassy.SasReaderCallback;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Reads data from a SAS file in SAS7BAT format.
 * 
 * @author Matt
 * @since 9-OCT-2011
 * @version 4.3
 */
public class SasInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = SasInputMeta.class; // for i18n purposes, needed
                                                    // by Translator2!!
                                                    // $NON-NLS-1$

  private SasInputMeta    meta;
  private SasInputData    data;

  public SasInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    meta = (SasInputMeta) smi;
    data = (SasInputData) sdi;

    final Object[] fileRowData = getRow();
    if (fileRowData==null) {
      // No more work to do...
      //
      setOutputDone();
      return false;
    }
    
    // First we see if we need to get a list of files from input...
    //
    if (first) {
      
      // The output row meta data, what does it look like?
      //
      data.outputRowMeta = new RowMeta();

      // See if the input row contains the filename field...
      //
      int idx = getInputRowMeta().indexOfValue(meta.getAcceptingField());
      if (idx < 0) {
            throw new KettleException(BaseMessages.getString(PKG, "SASInput.Log.Error.UnableToFindFilenameField", meta.getAcceptingField()));
        }
      
      // Determine the output row layout
      //
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
    }

    String rawFilename = getInputRowMeta().getString(fileRowData, meta.getAcceptingField(), null);
    final String filename = KettleVFS.getFilename(KettleVFS.getFileObject(rawFilename)); 
    
    data.helper = new SasInputHelper(filename);
    logBasic(BaseMessages.getString(PKG, "SASInput.Log.OpenedSASFile") + " : [" + data.helper + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    // verify the row layout...
    //
    if (data.fileLayout == null) {
      data.fileLayout = data.helper.getRowMeta();
    } else {
      // Verify that all files are of the same file format, this is a requirement...
      //
      if (data.fileLayout.size()!=data.helper.getRowMeta().size()) {
        throw new KettleException("All input files need to have the same number of fields. File '"+
            filename+"' has "+data.helper.getRowMeta().size()+
            " fields while the first file only had "+data.fileLayout.size());
      }
      for (int i=0;i<data.fileLayout.size();i++) {
        ValueMetaInterface first = data.fileLayout.getValueMeta(i);
        ValueMetaInterface second = data.helper.getRowMeta().getValueMeta(i);
        if (!first.getName().equalsIgnoreCase(second.getName())) {
          throw new KettleException("Field nr "+i+" in file '"+filename+"' is called '"+second.getName()+"' while it was called '"+first.getName()+"' in the first file");
        }
        if (first.getType()!=second.getType()) {
          throw new KettleException("Field nr "+i+" in file '"+filename+"' is of data type '"+second.getTypeDesc()+"' while it was '"+first.getTypeDesc()+"' in the first file");
        }
      }
    }

    // Also make sure that we only read the specified fields, not any other...
    //
    if (first) {
      first = false;
      
      data.fieldIndexes = new ArrayList<Integer>();
      for (SasInputField field : meta.getOutputFields()) {
        int fieldIndex = data.fileLayout.indexOfValue(field.getName());
        if (fieldIndex<0) {
          throw new KettleException("Selected field '"+field.getName()+"' couldn't be found in file '"+filename+"'");
        }
        data.fieldIndexes.add(fieldIndex);
      }
    }
    
    // Add this to the result file names...
    //
    ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(filename), getTransMeta().getName(), getStepname());
    resultFile.setComment(BaseMessages.getString(PKG, "SASInput.ResultFile.Comment"));
    addResultFile(resultFile);
    
    SasReader sasReader = new SasReader(new File(filename));
    sasReader.read(new SasReaderCallback() {
      private boolean firstRead=true;
      
      public void column(int index, String name, String label, SasColumnType type, int length) {
      }

      public boolean readData() {
        return true;
      }

      public boolean row(int rowNumber, Object[] rowData) {
        try {
          // Let's copy the data for safety
          //
          if (firstRead) {
            firstRead=false;
          } else {
            if (rowNumber==1) return false;
          }
          Object[] row = RowDataUtil.createResizedCopy(fileRowData, data.outputRowMeta.size());
          
          // Only pick those fields that we're interested in.
          //
          int outputIndex=getInputRowMeta().size();
          for (int i=0;i<data.fieldIndexes.size();i++) {
            int fieldIndex = data.fieldIndexes.get(i);
            int type = data.fileLayout.getValueMeta(fieldIndex).getType();
            switch(type) {
            case ValueMetaInterface.TYPE_STRING: 
              row[outputIndex++] = rowData[fieldIndex]; break;
            case ValueMetaInterface.TYPE_NUMBER: 
              Double value = (Double)rowData[fieldIndex];
              if (value.equals(Double.NaN)) {
                value=null;
              }
              row[outputIndex++] = value;
              break;
            default:
              throw new RuntimeException("Unhandled data type '"+ValueMeta.getTypeDesc(type));
            }
          }

          // Convert the data type of the new data to the requested data types
          //
          convertData(data.fileLayout, row, data.outputRowMeta);

          // Pass along the row to further steps...
          //
          putRow(data.outputRowMeta, row);
          
          // System.out.println(rowNumber+" ---- passed row : "+Arrays.toString(rowData));
          
          return !isStopped();
        } catch(Exception e) {
          throw new RuntimeException("There was an error reading from SAS7BAT file '"+filename+"'", e);
        }
      }
    });

    return true;
  }
  
  protected void convertData(RowMetaInterface source, Object[] sourceData, RowMetaInterface target) throws KettleException { 
    int targetIndex=getInputRowMeta().size();
    for (int i=0;i<data.fieldIndexes.size();i++) {
      int fieldIndex = data.fieldIndexes.get(i);
      ValueMetaInterface sourceValueMeta = source.getValueMeta(fieldIndex);
      ValueMetaInterface targetValueMeta = target.getValueMeta(targetIndex);
      sourceData[targetIndex] = targetValueMeta.convertData(sourceValueMeta, sourceData[targetIndex]);
      
      targetIndex++;
    }
  }

  @Override
  public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException {
  }

}