/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.ldapoutput;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.ldapinput.LDAPConnection;

/**
 * @author Samatar Hassan
 * @since 21-09-2007
 */
public class LDAPOutputData extends BaseStepData implements StepDataInterface {
  public LDAPConnection connection;
  public int indexOfDNField;
  public int[] fieldStream;
  public String[] fieldsAttribute;
  public int nrfields;
  public int nrfieldsToUpdate;
  public String separator;
  public String[] attributes;
  public String[] attributesToUpdate;

  public int[] fieldStreamToUpdate;
  public String[] fieldsAttributeToUpdate;

  public int indexOfOldDNField;
  public int indexOfNewDNField;

  public LDAPOutputData() {
    super();
    this.indexOfDNField = -1;
    this.nrfields = 0;
    this.separator = null;
    this.fieldStreamToUpdate = null;
    this.fieldsAttributeToUpdate = null;
    this.attributesToUpdate = null;
    this.nrfieldsToUpdate = 0;
    this.indexOfOldDNField = -1;
    this.indexOfNewDNField = -1;
  }

}
