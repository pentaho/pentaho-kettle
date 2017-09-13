/*
 * Copyright 2017 Pentaho Corporation. All rights reserved.
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

import org.pentaho.di.repository.RepositoryObject;

/**
 * Created by bmorrise on 8/17/17.
 */
public class FileDialogOperation {

  public static String OPEN = "open";
  public static String SAVE = "save";

  private String command;
  private String filter;
  private RepositoryObject repositoryObject;
  private String startDir;

  public FileDialogOperation( String command ) {
    this.command = command;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand( String command ) {
    this.command = command;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter( String filter ) {
    this.filter = filter;
  }

  public RepositoryObject getRepositoryObject() {
    return repositoryObject;
  }

  public void setRepositoryObject( RepositoryObject repositoryObject ) {
    this.repositoryObject = repositoryObject;
  }

  public String getStartDir() {
    return startDir;
  }

  public void setStartDir( String startDir ) {
    this.startDir = startDir;
  }

}
