/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.partition;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class PartitionMethodSelector {

  private static final Class<?> PKG = PartitionMethodSelector.class;

  public static final String STRING_PARTITION_METHOD_SELECTOR_DIALOG_TITLE =
          BaseMessages.getString( PKG, "PartitionMethodSelector.DIALOG_TITLE" );

  public static final String STRING_PARTITION_METHOD_SELECTOR_DIALOG_TEXT =
          BaseMessages.getString( PKG, "PartitionMethodSelector.DIALOG_TEXT" );

  public String askForPartitionMethod( Shell shell, PartitionSettings settings ) {
    EnterSelectionDialog dialog =
      new EnterSelectionDialog( shell, settings.getOptions(), STRING_PARTITION_METHOD_SELECTOR_DIALOG_TITLE,
              STRING_PARTITION_METHOD_SELECTOR_DIALOG_TEXT );
    return dialog.open( settings.getDefaultSelectedMethodIndex() );
  }

}
