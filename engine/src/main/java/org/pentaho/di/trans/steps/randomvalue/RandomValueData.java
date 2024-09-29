/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
