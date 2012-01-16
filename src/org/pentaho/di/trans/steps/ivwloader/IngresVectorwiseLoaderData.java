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

package org.pentaho.di.trans.steps.ivwloader;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.ivwloader.IngresVectorwiseLoader.FifoOpener;

/**
 * Stores data for the MySQL bulk load step.
 *
 * @author Matt
 * @since  14-apr-2009
 */
public class IngresVectorwiseLoaderData extends BaseStepData implements StepDataInterface
{
  public int    keynrs[];         // nr of keylookup -value in row...
    
  public StreamLogger errorLogger;

  public StreamLogger outputLogger;

  public byte[] separator;
  public byte[] newline;

  public String schemaTable;

  public String fifoFilename;

  public OutputStream fifoStream;
  
  public IngresVectorwiseLoader.SqlRunner sqlRunner;

    public byte[] quote;

    public Process sqlProcess;

    public OutputStream sqlOutputStream;

    public FifoOpener fifoOpener;

    public boolean isEncoding;

    public String encoding;

  /**
   *  Default constructor.
   */
  public IngresVectorwiseLoaderData()
  {
    super();
  }
  
    public byte[] getBytes(String str) {
      if (isEncoding) {
        try {
          return str.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      } else {
        return str.getBytes();
      }
    }
}
