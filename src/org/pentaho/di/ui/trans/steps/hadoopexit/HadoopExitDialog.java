/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.trans.steps.hadoopexit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.hadoopexit.HadoopExit;
import org.pentaho.di.trans.steps.hadoopexit.HadoopExitMeta;
import org.pentaho.di.ui.trans.step.BaseStepXulDialog;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulMenuList;

public class HadoopExitDialog extends BaseStepXulDialog implements StepDialogInterface {
  private static final Class<?> PKG = HadoopExit.class;

  private XulMenuList<?> outKeyFieldnames;
  private XulMenuList<?> outValueFieldnames;
  
  private HadoopExitMetaMapper metaMapper;
  private String workingStepname;
  
  private List<ValueMetaInterface> outKeyFields = new ArrayList<ValueMetaInterface>();
  private List<ValueMetaInterface> outValueFields = new ArrayList<ValueMetaInterface>();
  
  public HadoopExitDialog(Shell parent, Object in, TransMeta tr, String sname) throws Throwable {
    super("org/pentaho/di/ui/trans/steps/hadoopexit/dialog.xul", parent, (BaseStepMeta) in, tr, sname);
    init();
  }
  
  public void init() throws Throwable {
    workingStepname = stepname;
    
    metaMapper = new HadoopExitMetaMapper();
    metaMapper.loadMeta((HadoopExitMeta)baseStepMeta);
    
    // Get input fields to generate drop down lists
    RowMetaInterface inputRow = null;
    try {
      inputRow = transMeta.getPrevStepFields(stepMeta);
    } catch (KettleStepException e) {
      // No previous step found, leave list empty
    }
    
    // Seed the lists with the previously selected fields: This is done first so the last selection is at the top
    if(!StringUtil.isEmpty(metaMapper.getOutKeyFieldname())) {
      outKeyFields.add(new ValueMeta(metaMapper.getOutKeyFieldname()));
    }
    if(!StringUtil.isEmpty(metaMapper.getOutValueFieldname())) {
      outValueFields.add(new ValueMeta(metaMapper.getOutValueFieldname()));
    }
    
    if(inputRow != null) {
      for(ValueMetaInterface field : inputRow.getValueMetaList()) {
        // Avoid adding duplicates
        if(StringUtil.isEmpty(metaMapper.getOutKeyFieldname()) || !metaMapper.getOutKeyFieldname().equals(field.getName())) {
          outKeyFields.add(new ValueMeta(field.getName()));
        }
        
        // Avoid adding duplicates
        if(StringUtil.isEmpty(metaMapper.getOutValueFieldname()) || !metaMapper.getOutValueFieldname().equals(field.getName())) {
          outValueFields.add(new ValueMeta(field.getName()));
        }
      }
    }
    
    // Populate outKey menulist
    bf.setBindingType(Binding.Type.ONE_WAY);

    bf.createBinding("step-name", "value", this, "stepName");
    bf.createBinding(this, "stepName", "step-name", "value").fireSourceChanged();
    bf.createBinding(this, "outKeyFields", "output-key-fieldname", "elements").fireSourceChanged();
    bf.createBinding(this, "outValueFields", "output-value-fieldname", "elements").fireSourceChanged();
    
    outKeyFieldnames = (XulMenuList<?>) getXulDomContainer().getDocumentRoot().getElementById("output-key-fieldname");
    outValueFieldnames = (XulMenuList<?>) getXulDomContainer().getDocumentRoot().getElementById("output-value-fieldname");

    if((outKeyFieldnames != null) && (outKeyFieldnames.getElements().size() > 0)) {
      outKeyFieldnames.setSelectedIndex(0);
    }
    
    if((outValueFieldnames != null) && (outValueFieldnames.getElements().size() > 0)) {
      outValueFieldnames.setSelectedIndex(0);
    }
  }

  @Override
  protected Class<?> getClassForMessages() {
    return HadoopExit.class;
  }

  @Override
  public void onAccept() {
    metaMapper.setOutKeyFieldname(outKeyFieldnames.getValue());
    metaMapper.setOutValueFieldname(outValueFieldnames.getValue());
    
    if(!workingStepname.equals(stepname)) {
      stepname = workingStepname;
      baseStepMeta.setChanged();
    }
    
    metaMapper.saveMeta((HadoopExitMeta)baseStepMeta);
    dispose();
  }

  @Override
  public void onCancel() {
    setStepName(null);
    dispose();
  }
  
  public void setStepName(String stepname) {
    workingStepname = stepname;
  }
  
  public String getStepName() {
    return workingStepname;
  }
  
  public List<ValueMetaInterface> getOutKeyFields() {
    return outKeyFields;
  }
  
  public List<ValueMetaInterface> getOutValueFields() {
    return outValueFields;
  }

}
