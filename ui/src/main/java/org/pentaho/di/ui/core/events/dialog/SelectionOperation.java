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


package org.pentaho.di.ui.core.events.dialog;

/**
 * enum for dialog selection operations. Meant to be an abstraction layer between the select values
 * of @see org.pentaho.di.ui.core.FileDialogOperation
 * <br> For example: @link org.pentaho.di.ui.core.FileDialogOperation#SELECT_FILE
 */
public enum SelectionOperation {
  FILE,
  FOLDER,
  FILE_OR_FOLDER,
  SAVE,
  SAVE_TO,
  SAVE_TO_FILE_FOLDER,
  OPEN;
}
