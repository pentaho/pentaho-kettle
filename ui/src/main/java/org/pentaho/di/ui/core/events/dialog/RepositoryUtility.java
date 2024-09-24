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

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.function.Supplier;

/**
 * Utility class for repository related functions.
 */
public class RepositoryUtility {

  private Supplier<Spoon> spoonSupplier;

  /**
   * Default constructor.
   */
  public RepositoryUtility() {
    // empty constructor
  }

  /**
   * Determine if connected to repository.
   * @return true if connected, false otherwise.
   */
  public boolean isConnectedToRepository() {
    // NOTE: can dynamically switch between rep and non-rep
    return StringUtils.isNotBlank( getSpoon().getUsername() );
  }

  /**
   * Get spoon instance.
   * @return
   */
  protected Spoon getSpoon() {
    if ( spoonSupplier == null ) {
      spoonSupplier = Spoon::getInstance;
    }
    return spoonSupplier.get();
  }
}
