/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2017 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.step;

/**
 * @deprecated as of 7.1 release , no longer needed. Due to the possibility of interface
 * default methods, the interface method is moved to StepMetaInterface
 */
@Deprecated
public interface StepMetaInterfaceExtended extends StepMetaInterface {

  /**
   * True if the step passes it's result data straight to the servlet output. See exposing Kettle data over a web service
   * <a href="http://wiki.pentaho.com/display/EAI/PDI+data+over+web+services">http://wiki.pentaho.com/display/EAI/PDI+data+over+web+services</a>
   *
   * @return True if the step passes it's result data straight to the servlet output, false otherwise
   */
  boolean passDataToServletOutput();

}
