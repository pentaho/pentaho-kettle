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
