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
 

package org.pentaho.di.trans.steps.xmljoin;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Ingo Klose
 * @since 30-apr-2008
 */
public class XMLJoinData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;
	
	public RowSet TargetRowSet;
	public RowSet SourceRowSet;
	
	public Object[] outputRowData;
	
	public Document targetDOM;
	
	public Node targetNode;
	
	public NodeList targetNodes;
	
	public String XPathStatement;
	
	public int iSourceXMLField = -1;
	public int iCompareFieldID = -1;
	

    /**
     * 
     */
    public XMLJoinData()
    {
        super();
        
    }

}
