/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.textfileinput;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.playlist.FilePlayList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;


/**
 * @author Matt
 * @since 22-jan-2005
 */
public class TextFileInputData extends BaseStepData implements
		StepDataInterface {
	
	public List<TextFileLine> lineBuffer;

	public Object[] previous_row;

	public int nr_repeats;

	public int nrLinesOnPage;

	public FileInputList files;
	
	public HashMap<FileObject, Object[]> passThruFields;
	
	public Object[] currentPassThruFieldsRow;
	
	public int nrPassThruFields;

	public boolean isLastFile;

	public String filename;
	
	public int lineInFile;
	
	public FileObject file;

	public int filenr;

	public InputStream fr;

	public ZipInputStream zi;

	public GZIPInputStream gzi;

	public InputStreamReader isr;

	public boolean doneReading;

	public int headerLinesRead;

	public int footerLinesRead;

	public int pageLinesRead;

	public boolean doneWithHeader;

	public FileErrorHandler dataErrorLineHandler;

	public FilePlayList filePlayList;
	
	public TextFileFilterProcessor filterProcessor;

    public RowMetaInterface outputRowMeta;

    public StringBuilder lineStringBuilder;

    public int fileFormatType;

    public int fileType;

    public RowMetaInterface convertRowMeta;

	public RowSet rowSet;
	
	/**
	 * The separator (delimiter)
	 */
	public String separator;
	
	
	public boolean addShortFilename;
	public boolean addExtension;
	public boolean addPath;
	public boolean addSize;
	public boolean addIsHidden;
	public boolean addLastModificationDate;
	public boolean addUri;
	public boolean addRootUri;
	
	public String shortFilename;
	public String path;	
	public String extension;	
	public boolean hidden;	
	public Date lastModificationDateTime;	
	public String uriName;	
	public String rootUriName;	
	public long size;

    public EncodingType encodingType;
    
    public Map<String, Boolean> rejectedFiles;
    
	/**
	 * 
	 */
	public TextFileInputData() {
		super();

		lineBuffer = new ArrayList<TextFileLine>();

		nr_repeats = 0;
		previous_row = null;
		filenr = 0;

		nrLinesOnPage = 0;

		fr = null;
		zi = null;
		
		filterProcessor = null;
        lineStringBuilder = new StringBuilder(256);
        
        rejectedFiles = new HashMap<String, Boolean>();
	}
}