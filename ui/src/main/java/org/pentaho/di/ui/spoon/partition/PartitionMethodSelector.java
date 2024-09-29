/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
