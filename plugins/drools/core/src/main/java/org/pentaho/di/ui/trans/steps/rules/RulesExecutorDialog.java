/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.trans.steps.rules;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.rules.RulesExecutor;
import org.pentaho.di.trans.steps.rules.RulesExecutorMeta;
import org.pentaho.di.ui.trans.step.BaseStepXulDialog;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulTree;

public class RulesExecutorDialog extends BaseStepXulDialog implements StepDialogInterface {
  private RulesExecutorMetaMapper metaMapper;
  private String workingStepname;

  public RulesExecutorDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( "org/pentaho/di/ui/trans/steps/rules/executorDialog.xul", parent, (BaseStepMeta) in, tr, sname );
    init();
  }

  @Override
  protected final Class<?> getClassForMessages() {
    return RulesExecutor.class; // for i18n purposes
  }

  public void init() {
    workingStepname = stepname;

    metaMapper = new RulesExecutorMetaMapper();
    metaMapper.loadMeta( (RulesExecutorMeta) baseStepMeta );

    // Set dialog values
    ( (XulTextbox) document.getElementById( "step-name" ) ).setValue( getStepName() );
    ( (XulTextbox) document.getElementById( "rule-file" ) ).setValue( metaMapper.getRuleFile() );
    ( (XulTextbox) document.getElementById( "rule-definition" ) ).setValue( metaMapper.getRuleDefinition() );
    ( (XulTree) document.getElementById( "fields-table" ) ).setElements( metaMapper.getColumnList() );

    // Set the initial dialog state
    if ( metaMapper.getRuleDefinition() != null && !metaMapper.getRuleDefinition().equals( "" ) ) {
      setRuleSource( "definition" );
      ( (XulRadio) document.getElementById( "rule-definition-radio-button" ) ).setSelected( true );
    } else {
      setRuleSource( "file" );
      ( (XulRadio) document.getElementById( "rule-file-radio-button" ) ).setSelected( true );
    }

    // Bind data objects to UI
    bf.setBindingType( Binding.Type.ONE_WAY );
    try {
      bf.createBinding( "step-name", "value", this, "stepName" );
      bf.createBinding( "rule-file", "value", metaMapper, "ruleFile" );
      bf.createBinding( "rule-definition", "value", metaMapper, "ruleDefinition" );
      bf.createBinding( metaMapper.getColumnList(), "children", "fields-table", "elements" ).fireSourceChanged();
      // TODO: Add converter to clear out opposing text box
      bf.createBinding( "rule-file-radio-button", "selected", "rule-file", "!disabled" ).fireSourceChanged();
      bf.createBinding( "rule-file-radio-button", "selected", "rule-definition", "disabled" ).fireSourceChanged();

      bf
        .createBinding( "rule-definition-radio-button", "selected", "rule-definition", "!disabled" )
        .fireSourceChanged();
      bf.createBinding( "rule-definition-radio-button", "selected", "rule-file", "disabled" ).fireSourceChanged();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  public void setStepName( String stepname ) {
    workingStepname = stepname;
  }

  public String getStepName() {
    return workingStepname;
  }

  public void setRuleSource( String ruleSource ) {
    metaMapper.setRuleSource( ruleSource );
  }

  public void addNewRow() {
    metaMapper.addNewRow();
  }

  @Override
  public void onAccept() {
    if ( !workingStepname.equals( stepname ) ) {
      stepname = workingStepname;
      baseStepMeta.setChanged();
    }

    metaMapper.saveMeta( (RulesExecutorMeta) baseStepMeta );
    dispose();
  }

  @Override
  public void onCancel() {
    setStepName( null );
    dispose();
  }
}
