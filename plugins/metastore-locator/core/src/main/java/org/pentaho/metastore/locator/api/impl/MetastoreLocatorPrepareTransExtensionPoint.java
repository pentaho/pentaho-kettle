/*!
 * Copyright 2010 - 2022 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.metastore.locator.api.impl;

import org.pentaho.di.core.extension.ExtensionPoint;

/**
 * Created by tkafalas on 7/31/2017.
 * <p>
 * This class exists because two ExtensionPoint annotations are not allowed on the same class.
 */
@ExtensionPoint( id = "MetstoreLocatorPrepareTransExtensionPoint", extensionPointId = "TransformationPrepareExecution",
  description = "" )
public class MetastoreLocatorPrepareTransExtensionPoint extends MetastoreLocatorExtensionPoint {
}
