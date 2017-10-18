/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class WidgetUtils {
  private WidgetUtils() {

  }

  public static void setFormLayout( Composite composite, int margin ) {
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = margin;
    formLayout.marginHeight = margin;
    composite.setLayout( formLayout );
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

  public static  FormData firstColumn( Control top ) {
    return new FormDataBuilder().top( top, ConstUI.MEDUIM_MARGIN ).percentWidth( 47 ).result();
  }

  public static  FormData secondColumn( Control top ) {
    return new FormDataBuilder().top( top, ConstUI.MEDUIM_MARGIN ).right().left( 53, 0 ).result();
  }
}
