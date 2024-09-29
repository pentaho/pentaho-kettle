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


package org.pentaho.di.ui.trans.steps.fileinput.text;

import java.util.Arrays;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.widget.GetCaretPositionInterface;
import org.pentaho.di.ui.core.widget.InsertTextInterface;

public class VariableButtonListenerFactory {
  private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  // Listen to the Variable... button
  public static final SelectionAdapter getSelectionAdapter( final Composite composite, final Text destination,
      VariableSpace space ) {
    return getSelectionAdapter( composite, destination, null, null, space );
  }

  // Listen to the Variable... button
  public static final SelectionAdapter getSelectionAdapter( final Composite composite, final Text destination,
      final GetCaretPositionInterface getCaretPositionInterface, final InsertTextInterface insertTextInterface,
      final VariableSpace space ) {
    return new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        String[] keys = space.listVariables();
        Arrays.sort( keys );

        int size = keys.length;
        String[] key = new String[size];
        String[] val = new String[size];
        String[] str = new String[size];

        for ( int i = 0; i < keys.length; i++ ) {
          key[i] = keys[i];
          val[i] = space.getVariable( key[i] );
          str[i] = key[i] + "  [" + val[i] + "]";
        }

        // Before focus is lost, we get the position of where the selected variable needs to be inserted.
        int position = 0;
        if ( getCaretPositionInterface != null ) {
          position = getCaretPositionInterface.getCaretPosition();
        }

        EnterSelectionDialog esd =
            new EnterSelectionDialog( composite.getShell(), str, BaseMessages.getString( PKG,
                "System.Dialog.SelectEnvironmentVar.Title" ), BaseMessages.getString( PKG,
                    "System.Dialog.SelectEnvironmentVar.Message" ) );
        if ( esd.open() != null ) {
          int nr = esd.getSelectionNr();
          String var = "${" + key[nr] + "}";

          if ( insertTextInterface == null ) {
            destination.insert( var );
            // destination.setToolTipText(StringUtil.environmentSubstitute( destination.getText() ) );
            e.doit = false;
          } else {
            insertTextInterface.insertText( var, position );
          }
        }
      }
    };
  }
}
