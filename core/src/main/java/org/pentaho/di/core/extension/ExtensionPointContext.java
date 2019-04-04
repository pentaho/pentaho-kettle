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

package org.pentaho.di.core.extension;

/**
 * Convenience class you can use to pass more than one object in an extension point
 *
 * @author matt
 *
 */
public class ExtensionPointContext {
  private Object subject;
  private Object parent;
  private Object relation;

  public ExtensionPointContext( Object subject, Object parent, Object relation ) {
    super();
    this.subject = subject;
    this.parent = parent;
    this.relation = relation;
  }

  public Object getSubject() {
    return subject;
  }

  public void setSubject( Object subject ) {
    this.subject = subject;
  }

  public Object getParent() {
    return parent;
  }

  public void setParent( Object parent ) {
    this.parent = parent;
  }

  public Object getRelation() {
    return relation;
  }

  public void setRelation( Object relation ) {
    this.relation = relation;
  }
}
