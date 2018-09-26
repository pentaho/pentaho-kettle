/*
 * Copyright 2018 Hitachi Vantara. All rights reserved.
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
 */

package org.pentaho.di.ui.core;

import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * Created by ddiroma on 8/15/18.
 */
public class GetFieldsDialogOperation {
  private Shell shell;
  private int width;
  private int height;
  private String filename;
  private String title;
  private List<String> paths;

  public GetFieldsDialogOperation( Shell shell, int width, int height, String filename, String title, List<String>
          paths ) {
    this.shell = shell;
    this.width = width;
    this.height = height;
    this.filename = filename;
    this.title = title;
    this.paths = paths;
  }

  public Shell getShell() {
    return shell;
  }

  public void setShell( Shell shell ) {
    this.shell = shell;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth( int width ) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight( int height ) {
    this.height = height;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public List<String> getPaths() {
    return paths;
  }

  public void setPaths( List<String> paths ) {
    this.paths = paths;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }
}
