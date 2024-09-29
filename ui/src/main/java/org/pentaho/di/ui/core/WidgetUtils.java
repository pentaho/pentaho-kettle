/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.ui.core.widget.ComboVar;

import java.util.List;

public abstract class WidgetUtils {
  private WidgetUtils() {

  }

  public static void setFormLayout( Composite composite, int margin ) {
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = margin;
    formLayout.marginHeight = margin;
    composite.setLayout( formLayout );
  }

  /**
   * creates a ComboVar populated with fields from the previous step.
   * @param parentComposite - the composite in which the widget will be placed
   * @param props - PropsUI props for L&F
   * @param stepMeta - stepMeta of the current step
   * @param formData - FormData to use for placement
   */
  public static ComboVar createFieldDropDown(
    Composite parentComposite, PropsUI props, BaseStepMeta stepMeta, FormData formData ) {
    TransMeta transMeta = stepMeta.getParentStepMeta().getParentTransMeta();
    ComboVar fieldDropDownCombo = new ComboVar( transMeta, parentComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( fieldDropDownCombo );
    fieldDropDownCombo.addModifyListener( e -> stepMeta.setChanged() );

    fieldDropDownCombo.setLayoutData( formData );
    Listener focusListener = e -> {
      String current = fieldDropDownCombo.getText();
      fieldDropDownCombo.getCComboWidget().removeAll();
      fieldDropDownCombo.setText( current );

      try {
        RowMetaInterface rmi = transMeta.getPrevStepFields( stepMeta.getParentStepMeta().getName() );
        List ls = rmi.getValueMetaList();
        for ( Object l : ls ) {
          ValueMetaBase vmb = (ValueMetaBase) l;
          fieldDropDownCombo.add( vmb.getName() );
        }
      } catch ( KettleStepException ex ) {
        // can be ignored, since previous step may not be set yet.
        stepMeta.logDebug( ex.getMessage(), ex );
      }
    };
    fieldDropDownCombo.getCComboWidget().addListener( SWT.FocusIn, focusListener );
    return fieldDropDownCombo;
  }

  /**
   * Creates a FormData object specifying placement below anchorControl, with pixelsBetweeenAnchor space between
   * anchor and the control.
   */
  public static FormData formDataBelow( Control anchorControl, int width, int pixelsBetweenAnchor ) {
    FormData fdMessageField = new FormData();
    fdMessageField.left = new FormAttachment( 0, 0 );
    fdMessageField.top = new FormAttachment( anchorControl, pixelsBetweenAnchor );
    fdMessageField.right = new FormAttachment( 0, width );
    return fdMessageField;
  }


  public static CTabFolder createTabFolder( Composite composite, FormData fd, String... titles ) {
    Composite container = new Composite( composite, SWT.NONE );
    WidgetUtils.setFormLayout( container, 0 );
    container.setLayoutData( fd );

    CTabFolder tabFolder = new CTabFolder( container, SWT.NONE );
    tabFolder.setLayoutData( new FormDataBuilder().fullSize().result() );

    for ( String title : titles ) {
      if ( title.length() < 8 ) {
        title = StringUtils.rightPad( title, 8 );
      }
      Composite tab = new Composite( tabFolder, SWT.NONE );
      WidgetUtils.setFormLayout( tab, ConstUI.MEDUIM_MARGIN );

      CTabItem tabItem = new CTabItem( tabFolder, SWT.NONE );
      tabItem.setText( title );
      tabItem.setControl( tab );
    }

    tabFolder.setSelection( 0 );
    return tabFolder;
  }

  public static FormData firstColumn( Control top ) {
    return new FormDataBuilder().top( top, ConstUI.MEDUIM_MARGIN ).percentWidth( 47 ).result();
  }

  public static FormData secondColumn( Control top ) {
    return new FormDataBuilder().top( top, ConstUI.MEDUIM_MARGIN ).right().left( 53, 0 ).result();
  }
}
