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


package org.pentaho.di.trans.steps.randomvalue;

import java.util.Random;

import javax.crypto.KeyGenerator;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.UUID4Util;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 24-jan-2005
 */
public class RandomValueData extends BaseStepData implements StepDataInterface {
  public boolean readsRows;

  public RowMetaInterface outputRowMeta;

  public UUID4Util u4;
  /* Generating a random Message Authentication Code (MAC MD5) */
  public KeyGenerator keyGenHmacMD5;
  /* Generating a random Message Authentication Code (MAC SHA1) */
  public KeyGenerator keyGenHmacSHA1;

  public final Random randomgen = new Random();

  public RandomValueData() {
    super();
  }
}
