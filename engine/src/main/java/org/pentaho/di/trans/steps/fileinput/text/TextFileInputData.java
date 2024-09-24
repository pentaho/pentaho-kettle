/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.fileinput.text;

import java.util.LinkedList;
import java.util.List;

import org.pentaho.di.core.playlist.FilePlayList;
import org.pentaho.di.trans.steps.file.BaseFileInputStepData;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class TextFileInputData extends BaseFileInputStepData {

  public List<TextFileLine> lineBuffer;

  public Object[] previous_row;

  public int nrLinesOnPage;

  public boolean doneReading;

  public int headerLinesRead;

  public int footerLinesRead;

  public int pageLinesRead;

  public boolean doneWithHeader;

  public FilePlayList filePlayList;

  public TextFileFilterProcessor filterProcessor;

  public StringBuilder lineStringBuilder;

  public int fileFormatType;

  public int fileType;

  /**
   * The separator (delimiter)
   */
  public String separator;

  public String enclosure;

  public String escapeCharacter;

  public EncodingType encodingType;

  public TextFileInputData() {
    // linked list is better, as usually .remove(0) is applied to this list
    lineBuffer = new LinkedList<TextFileLine>();

    nr_repeats = 0;
    previous_row = null;

    nrLinesOnPage = 0;

    filterProcessor = null;
    lineStringBuilder = new StringBuilder( 256 );
  }
}
