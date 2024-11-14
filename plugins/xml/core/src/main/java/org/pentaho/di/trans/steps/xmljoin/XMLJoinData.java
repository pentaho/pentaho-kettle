/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.xmljoin;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Ingo Klose
 * @since 30-apr-2008
 */
public class XMLJoinData extends BaseStepData implements StepDataInterface {
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
  public XMLJoinData() {
    super();

  }

}
