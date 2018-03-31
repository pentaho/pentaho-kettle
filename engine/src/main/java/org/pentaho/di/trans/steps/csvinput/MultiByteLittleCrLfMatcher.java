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

package org.pentaho.di.trans.steps.csvinput;

public class MultiByteLittleCrLfMatcher implements CrLfMatcherInterface {

  @Override
  public boolean isReturn( byte[] source, int location ) {
    if ( location >= 1 ) {
      return source[location] == 0x0d && source[location + 1] == 0x00;
    } else {
      return false;
    }
  }

  @Override
  public boolean isLineFeed( byte[] source, int location ) {
    if ( location >= 1 ) {
      return source[location] == 0x0a && source[location + 1] == 0x00;
    } else {
      return false;
    }
  }

}
