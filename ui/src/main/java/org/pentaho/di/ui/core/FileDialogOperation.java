/*
 * Copyright 2017-2018 Hitachi Vantara. All rights reserved.
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

import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryObjectInterface;

/**
 * Created by bmorrise on 8/17/17.
 */
public class FileDialogOperation {

  public static String SELECT_FOLDER = "selectFolder";
  public static String OPEN = "open";
  public static String SAVE = "save";
  public static String ORIGIN_SPOON = "spoon";
  public static String ORIGIN_OTHER = "other";
  public static String TRANSFORMATION = "transformation";
  public static String JOB = "job";

  private Repository repository;
  private String command;
  private String filter;
  private String origin;
  private RepositoryObjectInterface repositoryObject;
  private String startDir;
  private String title;
  private String filename;
  private String fileType;

  public FileDialogOperation( String command ) {
    this.command = command;
  }

  public FileDialogOperation( String command, String origin ) {
    this.command = command;
    this.origin = origin;
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

  public String getOrigin() {
    return origin;
  }

  public void setOrigin( String origin ) {
    this.origin = origin;
  }

  public RepositoryObjectInterface getRepositoryObject() {
    return repositoryObject;
  }

  public void setRepositoryObject( RepositoryObjectInterface repositoryObject ) {
    this.repositoryObject = repositoryObject;
  }

  public String getStartDir() {
    return startDir;
  }

  public void setStartDir( String startDir ) {
    this.startDir = startDir;
  }

  public Repository getRepository() {
    return repository;
  }

  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType( String fileType ) {
    this.fileType = fileType;
  }
}
