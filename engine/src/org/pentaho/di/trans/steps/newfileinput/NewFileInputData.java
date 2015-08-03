package org.pentaho.di.trans.steps.newfileinput;

import java.util.LinkedList;
import java.util.List;

import org.pentaho.di.core.playlist.FilePlayList;
import org.pentaho.di.trans.steps.baseinput.BaseInputStepData;

public class NewFileInputData extends BaseInputStepData {

  public List<NewFileLine> lineBuffer;

  public Object[] previous_row;

  public int nrLinesOnPage;

  public boolean doneReading;

  public int headerLinesRead;

  public int footerLinesRead;

  public int pageLinesRead;

  public boolean doneWithHeader;

  public FilePlayList filePlayList;

  public NewFileFilterProcessor filterProcessor;

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
  

  public NewFileInputData() {
    // linked list is better, as usually .remove(0) is applied to this list
    lineBuffer = new LinkedList<NewFileLine>();

    nr_repeats = 0;
    previous_row = null;

    nrLinesOnPage = 0;

    filterProcessor = null;
    lineStringBuilder = new StringBuilder( 256 );
  }
}
