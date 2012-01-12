/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.ui.trans.steps.hadoopenter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.hadoopenter.HadoopEnterMeta;
import org.pentaho.di.ui.trans.step.BaseStepXulDialog;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;

public class HadoopEnterDialog extends BaseStepXulDialog implements StepDialogInterface {
  private static final Class<?> PKG = HadoopEnterMeta.class;

  private String workingStepname;
  
  private HadoopEnterMetaMapper metaMapper;
  
  private List<String> typeList;
  
  public HadoopEnterDialog(Shell parent, Object in, TransMeta tr, String sname) throws Throwable {
    super("org/pentaho/di/ui/trans/steps/hadoopenter/dialog.xul", parent, (BaseStepMeta) in, tr, sname);
    
    typeList = new ArrayList<String>();
    for(String type : ValueMeta.getTypes()) {
      typeList.add(type);
    }
    
    init();
  }
  
  public void init() throws Throwable {
    workingStepname = stepname;
    
    metaMapper = new HadoopEnterMetaMapper();
    metaMapper.loadMeta((HadoopEnterMeta)baseStepMeta);
    
    bf.setBindingType(Binding.Type.ONE_WAY);

    setTextBoxValue("input-key-length", metaMapper.getInKeyLength());
    setTextBoxValue("input-key-precision", metaMapper.getInKeyPrecision());
    setTextBoxValue("input-value-length", metaMapper.getInValueLength());
    setTextBoxValue("input-value-precision", metaMapper.getInValuePrecision());
    
    
    bf.createBinding("step-name", "value", this, "stepName");
    bf.createBinding(this, "stepName", "step-name", "value").fireSourceChanged();
    bf.createBinding(this, "types", "input-key-type", "elements").fireSourceChanged();
    bf.createBinding(this, "types", "input-value-type", "elements").fireSourceChanged();
    
    if(metaMapper.getInKeyType() >= 0) {
      ((XulMenuList<String>)getXulDomContainer().getDocumentRoot().getElementById("input-key-type")).setSelectedItem(ValueMeta.getTypeDesc(metaMapper.getInKeyType()));
    }
    if(metaMapper.getInValueType() >= 0) {
      ((XulMenuList<String>)getXulDomContainer().getDocumentRoot().getElementById("input-value-type")).setSelectedItem(ValueMeta.getTypeDesc(metaMapper.getInValueType()));
    }
  }

  @Override
  protected Class<?> getClassForMessages() {
    return HadoopEnterMeta.class;
  }

  @Override
  public void onAccept() {
    metaMapper.setInKeyType(fetchValue((XulMenuList<?>)getXulDomContainer().getDocumentRoot().getElementById("input-key-type")));
    metaMapper.setInKeyLength(fetchValue((XulTextbox)getXulDomContainer().getDocumentRoot().getElementById("input-key-length")));
    metaMapper.setInKeyPrecision(fetchValue((XulTextbox)getXulDomContainer().getDocumentRoot().getElementById("input-key-precision")));
    
    metaMapper.setInValueType(fetchValue((XulMenuList<?>)getXulDomContainer().getDocumentRoot().getElementById("input-value-type")));
    metaMapper.setInValueLength(fetchValue((XulTextbox)getXulDomContainer().getDocumentRoot().getElementById("input-value-length")));
    metaMapper.setInValuePrecision(fetchValue((XulTextbox)getXulDomContainer().getDocumentRoot().getElementById("input-value-precision")));
    
    if(!workingStepname.equals(stepname)) {
      stepname = workingStepname;
      baseStepMeta.setChanged();
    }
    
    metaMapper.saveMeta((HadoopEnterMeta)baseStepMeta);
    dispose();
  }
  
  private int fetchValue(XulTextbox textbox) {
    int result = -1;
    
    if(textbox != null && !StringUtil.isEmpty(textbox.getValue())) {
      try {
        result = Integer.parseInt(textbox.getValue());
      } catch(NumberFormatException e) {
        log.logError(BaseMessages.getString("HadoopEnter.Error.ParseInteger",textbox.getValue()));
      }
    }
    
    return result;
  }
  
  private int fetchValue(XulMenuList<?> menulist) {
    int result = -1;
    
    if(menulist != null && menulist.getValue() != null) {
      result = ValueMeta.getType(menulist.getValue());
    }
    
    return result;
  }
  
  private void setTextBoxValue(String textbox, int value) {
    String v = "";
    
    if(value >= 0) {
      v = Integer.toString(value);
    }
    
    ((XulTextbox)getXulDomContainer().getDocumentRoot().getElementById(textbox)).setValue(v);
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
  
  public List<String> getTypes() {
    return typeList;
  }
}
