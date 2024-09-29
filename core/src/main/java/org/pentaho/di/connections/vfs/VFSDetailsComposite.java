/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.connections.vfs;

public interface VFSDetailsComposite {
  Object open();

  void close();

  /**
   * Validates the fields that the DetailsComposite if responsible for.
   * @return null if successfully validates or an error message if it does not.
   */
  default String validate() {
    return null;
  }

  /**
   * Gets the Object of SelectionAdapterFileDialogTextVar for the browse file dialog.
   * <p>
   * @param TextVar The TextVar object.
   * @return Object of SelectionAdapterFileDialogTextVar or null otherwise.
   * Defaults to {@code null}.
   */
  default Object getRootPathSelectionAdapter( Object textUiWidget ) {
    return null;
  }
}
