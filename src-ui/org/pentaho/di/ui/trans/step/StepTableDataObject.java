/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.step;

import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jun 8, 2010
 * Time: 10:30:57 AM
 * To change this template use File | Settings | File Templates.
 */
public interface StepTableDataObject {
  String getName();
  String getDataType();
  int getLength();
  int getPrecision();
  StepTableDataObject createNew( ValueMetaInterface val );
}
