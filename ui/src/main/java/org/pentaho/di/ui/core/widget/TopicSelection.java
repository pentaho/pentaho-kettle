/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.ui.core.PropsUI;

import static org.pentaho.di.ui.core.WidgetUtils.createFieldDropDown;

public class TopicSelection extends Composite {

  private final PropsUI props;
  private final TransMeta transMeta;
  private final BaseStepMeta stepMeta;
  private final ModifyListener lsMod;
  private final boolean topicInField;
  private final String topicGroupLabel;
  private final String fieldTopicLabel;
  private final String textTopicLabel;
  private final String textTopicRadioLabel;
  private final String fieldTopicRadioLabel;

  private Group wTopicGroup;
  private Button wTopicFromField;
  private Button wTopicFromText;
  private Label wlTopic;
  private TextVar wTopicText;
  private ComboVar wTopicField;

  private TopicSelection( final Builder builder ) {
    super( builder.composite, builder.style );
    this.props = builder.props;
    this.transMeta = builder.transMeta;
    this.stepMeta = builder.stepMeta;
    this.lsMod = builder.lsMod;
    this.topicInField = builder.topicInField;
    this.topicGroupLabel = builder.topicGroupLabel;
    this.fieldTopicLabel = builder.fieldTopicLabel;
    this.textTopicLabel = builder.textTopicLabel;
    this.textTopicRadioLabel = builder.textTopicRadioLabel;
    this.fieldTopicRadioLabel = builder.fieldTopicRadioLabel;

    layoutUI();
  }

  private void layoutUI() {
    FormLayout topicSelectionLayout = new FormLayout();
    this.setLayout( topicSelectionLayout );

    wTopicGroup = new Group( this, SWT.SHADOW_ETCHED_IN );
    props.setLook( wTopicGroup );
    wTopicGroup.setText( topicGroupLabel );

    FormLayout topicGroupLayout = new FormLayout();
    topicGroupLayout.marginHeight = 15;
    topicGroupLayout.marginWidth = 15;
    wTopicGroup.setLayout( topicGroupLayout );

    FormData fdTopicGroup = new FormData();
    fdTopicGroup.left = new FormAttachment( 0, 0 );
    fdTopicGroup.top = new FormAttachment( 0, 10 );
    fdTopicGroup.right = new FormAttachment( 100, 0 );
    fdTopicGroup.bottom = new FormAttachment( 100, 0 );
    wTopicGroup.setLayoutData( fdTopicGroup );

    wTopicFromText = new Button( wTopicGroup, SWT.RADIO );
    wTopicFromField = new Button( wTopicGroup, SWT.RADIO );
    props.setLook( wTopicFromText );
    props.setLook( wTopicFromField );

    SelectionAdapter selectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        super.widgetSelected( selectionEvent );
        setTopicWidgetVisibility( wTopicFromField );
      }
    };

    wTopicFromField.addSelectionListener( selectionListener );
    wTopicFromText.addSelectionListener( selectionListener );

    wTopicFromField.setSelection( topicInField );
    wTopicFromText.setSelection( !topicInField );

    wTopicFromText.setText( textTopicRadioLabel );
    wTopicFromField.setText( fieldTopicRadioLabel );

    FormData specifyTopicLayout = new FormData();
    specifyTopicLayout.left = new FormAttachment( 0, 0 );
    specifyTopicLayout.top = new FormAttachment( 0, 0 );
    wTopicFromText.setLayoutData( specifyTopicLayout );

    FormData fdTopicComesFromField = new FormData();
    fdTopicComesFromField.left = new FormAttachment( 0, 0 );
    fdTopicComesFromField.top = new FormAttachment( wTopicFromText, 5 );

    wTopicFromField.setLayoutData( fdTopicComesFromField );
    wTopicFromField.addSelectionListener( selectionListener );
    wTopicFromText.addSelectionListener( selectionListener );

    Label separator = new Label( wTopicGroup, SWT.SEPARATOR | SWT.VERTICAL );
    FormData fdSeparator = new FormData();
    fdSeparator.top = new FormAttachment( 0, 0 );
    fdSeparator.left = new FormAttachment( wTopicFromField, 15 );
    fdSeparator.bottom = new FormAttachment( 100, 0 );
    separator.setLayoutData( fdSeparator );

    FormData fdTopicEntry = new FormData();
    fdTopicEntry.top = new FormAttachment( 0, 0 );
    fdTopicEntry.left = new FormAttachment( separator, 15 );
    fdTopicEntry.right = new FormAttachment( 100, 0 );

    wlTopic = new Label( wTopicGroup, SWT.LEFT );
    wlTopic.setLayoutData( fdTopicEntry );
    props.setLook( wlTopic );

    FormData formData = new FormData();
    formData.top = new FormAttachment( wlTopic, 5 );
    formData.left = new FormAttachment( separator, 15 );
    formData.right = new FormAttachment( 100, 0 );

    wTopicText = new TextVar( transMeta, wTopicGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wTopicField = createFieldDropDown( wTopicGroup, props, stepMeta, formData );
    wTopicText.setLayoutData( formData );
    wTopicField.setLayoutData( formData );

    setTopicWidgetVisibility( wTopicFromField );

    wTopicText.addModifyListener( lsMod );
    wTopicField.addModifyListener( lsMod );
  }

  private void setTopicWidgetVisibility( Button topicComesFromField ) {
    stepMeta.setChanged( stepMeta.hasChanged() || topicInField != topicComesFromField.getSelection() );
    wTopicField.setVisible( topicComesFromField.getSelection() );
    wTopicText.setVisible( !topicComesFromField.getSelection() );
    if ( topicComesFromField.getSelection() ) {
      wlTopic.setText( fieldTopicLabel );
    } else {
      wlTopic.setText( textTopicLabel );
    }
  }

  public String getTopicText() {
    return wTopicText.getText();
  }

  public String getTopicFieldText() {
    return wTopicField.getText();
  }

  public void setTopicFieldText( String selectedTopicFieldText ) {
    this.wTopicField.setText( selectedTopicFieldText );
  }

  public void setTopicText( String topicText ) {
    wTopicText.setText( topicText );
  }

  public void setTopicInField( boolean topicInField ) {
    wTopicFromField.setSelection( topicInField );
    wTopicFromText.setSelection( !topicInField );

    setTopicWidgetVisibility( wTopicFromField );
  }

  public boolean isTopicInField() {
    return wTopicFromField.getSelection();
  }

  public void setEnabled( boolean enabled ) {
    wTopicGroup.setEnabled( enabled );

    wTopicFromField.setEnabled( enabled );
    wTopicFromText.setEnabled( enabled );

    wlTopic.setEnabled( enabled );

    wTopicText.setEnabled( enabled );
    wTopicText.setEditable( enabled );

    wTopicField.setEnabled( enabled );
    wTopicField.setEditable( enabled );
  }

  /**
   * Builder class for TopicSelection
   */
  public static final class Builder {
    private Composite composite;
    private int style;
    private PropsUI props;
    private TransMeta transMeta;
    private BaseStepMeta stepMeta;
    private ModifyListener lsMod;
    private boolean topicInField;
    private String topicGroupLabel;
    private String fieldTopicLabel;
    private String textTopicLabel;
    private String textTopicRadioLabel;
    private String fieldTopicRadioLabel;

    public Builder setComposite( Composite composite ) {
      this.composite = composite;
      return this;
    }

    public Builder setStyle( int style ) {
      this.style = style;
      return this;
    }

    public Builder setProps( PropsUI props ) {
      this.props = props;
      return this;
    }

    public Builder setTransMeta( TransMeta transMeta ) {
      this.transMeta = transMeta;
      return this;
    }

    public Builder setStepMeta( BaseStepMeta stepMeta ) {
      this.stepMeta = stepMeta;
      return this;
    }

    public Builder setLsMod( ModifyListener lsMod ) {
      this.lsMod = lsMod;
      return this;
    }

    public Builder setTopicInField( boolean topicInField ) {
      this.topicInField = topicInField;
      return this;
    }

    public Builder setTopicGroupLabel( String topicGroupLabel ) {
      this.topicGroupLabel = topicGroupLabel;
      return this;
    }

    public Builder setFieldTopicLabel( String fieldTopicLabel ) {
      this.fieldTopicLabel = fieldTopicLabel;
      return this;
    }

    public Builder setTextTopicLabel( String textTopicLabel ) {
      this.textTopicLabel = textTopicLabel;
      return this;
    }

    public Builder setTextTopicRadioLabel( String textTopicRadioLabel ) {
      this.textTopicRadioLabel = textTopicRadioLabel;
      return this;
    }

    public Builder setFieldTopicRadioLabel( String fieldTopicRadioLabel ) {
      this.fieldTopicRadioLabel = fieldTopicRadioLabel;
      return this;
    }

    public TopicSelection build() {
      return new TopicSelection( this );
    }
  }
}
