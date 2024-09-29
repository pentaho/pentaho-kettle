/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

import static org.pentaho.di.core.Const.MARGIN;
import static org.pentaho.di.ui.core.WidgetUtils.createFieldDropDown;

public class TopicSelection extends Composite {

  private final PropsUI props;
  private final TransMeta transMeta;
  private final BaseStepMeta stepMeta;
  private final ModifyListener lsMod;
  private final boolean topicInField;
  private final String topicGroupLabel;
  private final String fieldTopicLabel;
  private final String fieldTopicErrorToolTip;
  private final String textTopicLabel;
  private final String textTopicRadioLabel;
  private final String fieldTopicRadioLabel;
  private final boolean displayTopicErrorIcon;

  private Group wTopicGroup;
  private Button wTopicFromField;
  private Button wTopicFromText;
  private Label wlTopic;
  private Label wlConnectionError;

  //use TextVar or ComboVar based on the boolean value
  private boolean isTopicTextCombo;
  private TextVar wTopicText;
  private ComboVar wTopicTextCombo;

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
    this.fieldTopicErrorToolTip = builder.fieldTopicErrorToolTip;
    this.textTopicLabel = builder.textTopicLabel;
    this.textTopicRadioLabel = builder.textTopicRadioLabel;
    this.fieldTopicRadioLabel = builder.fieldTopicRadioLabel;
    this.isTopicTextCombo = builder.isTopicTextCombo;
    this.displayTopicErrorIcon = builder.displayTopicErrorIcon;

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

    wTopicGroup.setLayoutData( new FormDataBuilder().top( 0, ConstUI.MEDUIM_MARGIN ).fullWidth().bottom().result() );

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

    wTopicFromText.setLayoutData( new FormDataBuilder().left().top().result() );

    wTopicFromField.setLayoutData( new FormDataBuilder().left().top( wTopicFromText ).result() );
    wTopicFromField.addSelectionListener( selectionListener );
    wTopicFromText.addSelectionListener( selectionListener );

    Label separator = new Label( wTopicGroup, SWT.SEPARATOR | SWT.VERTICAL );
    separator.setLayoutData( new FormDataBuilder().top().left( wTopicFromField, 15 ).bottom().result() );

    wlTopic = new Label( wTopicGroup, SWT.LEFT );
    props.setLook( wlTopic );

    if ( displayTopicErrorIcon ) {
      //Connection Error Icon label
      wlTopic.setLayoutData( new FormDataBuilder().top().left( separator, 15 ).result() );

      wlConnectionError = new Label( wTopicGroup, SWT.LEFT );
      wlConnectionError.setToolTipText( fieldTopicErrorToolTip );
      wlConnectionError.setImage( SwtSvgImageUtil.getImage(
        this.getDisplay(), getClass().getClassLoader(), "error.svg", ConstUI.SMALL_ICON_SIZE,
        ConstUI.SMALL_ICON_SIZE ) );
      props.setLook( wlConnectionError );

      wlConnectionError.setLayoutData( new FormDataBuilder().top().left( wlTopic, MARGIN ).result() );
      wlConnectionError.setVisible( false );
    } else {
      wlTopic.setLayoutData( new FormDataBuilder().top().left( separator, 15 ).right().result() );
    }

    FormData fdTopic = new FormDataBuilder().top( wlTopic ).left( separator, 15 ).right().result();

    wTopicTextCombo = new ComboVar( transMeta, wTopicGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTopicTextCombo );
    wTopicTextCombo.setLayoutData( fdTopic );
    wTopicTextCombo.addModifyListener( lsMod );

    wTopicText = new TextVar( transMeta, wTopicGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTopicText );
    wTopicText.setLayoutData( fdTopic );
    wTopicText.addModifyListener( lsMod );

    wTopicField = createFieldDropDown( wTopicGroup, props, stepMeta, fdTopic );
    props.setLook( wTopicField );
    wTopicField.setLayoutData( fdTopic );
    setTopicWidgetVisibility( wTopicFromField );
    wTopicField.addModifyListener( lsMod );
  }

  private void setTopicWidgetVisibility( Button topicComesFromField ) {
    stepMeta.setChanged( stepMeta.hasChanged() || topicInField != topicComesFromField.getSelection() );
    wTopicField.setVisible( topicComesFromField.getSelection() );

    if ( topicComesFromField.getSelection() ) {
      wlTopic.setText( fieldTopicLabel );
      wTopicTextCombo.setVisible( false );
      wTopicText.setVisible( false );
      if ( displayTopicErrorIcon ) {
        wlConnectionError.setVisible( false );
      }
    } else {
      wlTopic.setText( textTopicLabel );
      toggleTopicTextComboVisible( isTopicTextCombo );
    }
  }

  public void setIsTopicTextCombo( boolean isCombo ) {
    isTopicTextCombo = isCombo;
  }

  public void toggleTopicTextComboVisible( boolean isComboVisible ) {
    //ignore toggle if the topic is set to come from a field
    if ( !wTopicFromField.getSelection() ) {
      if ( displayTopicErrorIcon ) {
        wlConnectionError.setVisible( !isComboVisible );
      }
      wTopicTextCombo.setVisible( isComboVisible );
      wTopicText.setVisible( !isComboVisible );
    }
  }

  public String getTopicText() {
    return isTopicTextCombo ? wTopicTextCombo.getText() : wTopicText.getText();
  }

  public String getTopicFieldText() {
    return wTopicField.getText();
  }

  public void setTopicFieldText( String selectedTopicFieldText ) {
    this.wTopicField.setText( selectedTopicFieldText );
  }

  public void setTopicText( String topicText ) {
    wTopicTextCombo.setText( topicText );
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

  @Override
  public void setEnabled( boolean enabled ) {
    wTopicGroup.setEnabled( enabled );

    wTopicFromField.setEnabled( enabled );
    wTopicFromText.setEnabled( enabled );

    wlTopic.setEnabled( enabled );

    if ( isTopicTextCombo ) {
      wTopicTextCombo.setEnabled( enabled );
      wTopicTextCombo.setEditable( enabled );
    } else {
      wTopicText.setEnabled( enabled );
      wTopicText.setEditable( enabled );
    }

    wTopicField.setEnabled( enabled );
    wTopicField.setEditable( enabled );
  }

  public ComboVar getTopicTextCombo() {
    return wTopicTextCombo;
  }

  public TextVar getTopicTextComponent() {
    return wTopicText;
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
    private String fieldTopicErrorToolTip;
    private String textTopicLabel;
    private String textTopicRadioLabel;
    private String fieldTopicRadioLabel;
    private boolean isTopicTextCombo;
    private boolean displayTopicErrorIcon = false;

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

    public Builder setFieldTopicErrorToolTip( String fieldTopicErrorToolTip ) {
      this.fieldTopicErrorToolTip = fieldTopicErrorToolTip;
      return this;
    }

    public Builder isDisplayTopicErrorIcon( boolean displayTopicErrorIcon ) {
      this.displayTopicErrorIcon = displayTopicErrorIcon;
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

    public Builder isFieldTextCombo( boolean isCombo ) {
      this.isTopicTextCombo = isCombo;
      return this;
    }

    public TopicSelection build() {
      return new TopicSelection( this );
    }
  }
}
