/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;

public class FormDataBuilder implements Cloneable {

  private final FormAttachment MIN = new FormAttachment( 0, 0 );
  private final FormAttachment MAX = new FormAttachment( 100, 0 );

  protected FormData fd = new FormData();

  public FormDataBuilder width( int width ) {
    fd.width = width;
    return this;
  }

  public FormDataBuilder height( int height ) {
    fd.height = height;
    return this;
  }

  public FormDataBuilder left( int p1, int p2 ) {
    fd.left = new FormAttachment( p1, p2 );
    return this;
  }

  public FormDataBuilder right( int p1, int p2 ) {
    fd.right = new FormAttachment( p1, p2 );
    return this;
  }

  public FormDataBuilder top( int p1, int p2 ) {
    fd.top = new FormAttachment( p1, p2 );
    return this;
  }

  public FormDataBuilder bottom( int p1, int p2 ) {
    fd.bottom = new FormAttachment( p1, p2 );
    return this;
  }

  public FormDataBuilder left( FormAttachment fa ) {
    fd.left = fa;
    return this;
  }

  public FormDataBuilder right( FormAttachment fa ) {
    fd.right = fa;
    return this;
  }

  public FormDataBuilder top( FormAttachment fa ) {
    fd.top = fa;
    return this;
  }

  public FormDataBuilder bottom( FormAttachment fa ) {
    fd.bottom = fa;
    return this;
  }

  public FormDataBuilder left( Control control, int margin ) {
    return left( new FormAttachment( control, margin ) );
  }

  public FormDataBuilder top( Control control, int margin ) {
    return top( new FormAttachment( control, margin ) );
  }

  public FormDataBuilder bottom( Control control, int margin ) {
    return bottom( new FormAttachment( control, margin ) );
  }

  public FormDataBuilder right( Control control, int margin ) {
    return right( new FormAttachment( control, margin ) );
  }

  public FormDataBuilder left() {
    return left( MIN );
  }

  public FormDataBuilder right() {
    return right( MAX );
  }

  public FormDataBuilder top() {
    return top( MIN );
  }

  public FormDataBuilder bottom() {
    return bottom( MAX );
  }

  public FormDataBuilder top( Control control ) {
    return top( control, ConstUI.SMALL_MARGIN );
  }

  public FormDataBuilder fullWidth() {
    return percentWidth( 100 );
  }

  public FormDataBuilder percentWidth( int width ) {
    return left().right( width, 0 );
  }

  public FormDataBuilder fullSize() {
    return fullWidth().top().bottom();
  }

  public FormData result() {
    return fd;
  }

  @Override
  protected FormDataBuilder clone() {
    FormDataBuilder res = new FormDataBuilder();
    return res.left( fd.left ).right( fd.right ).top( fd.top ).bottom( fd.bottom );
  }

}
