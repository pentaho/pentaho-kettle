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

package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.Random;
import java.util.UUID;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.exception.KettleException;

public class ConditionLoadSaveValidator implements FieldLoadSaveValidator<Condition> {
  final Random rand = new Random();
  @Override
  public Condition getTestObject() {
    Condition rtn = new Condition();
    rtn.setFunction( rand.nextInt( Condition.functions.length ) );
    rtn.setLeftValuename( UUID.randomUUID().toString() );
    rtn.setNegated( rand.nextBoolean() );
    rtn.setOperator( rand.nextInt( Condition.operators.length ) );
    rtn.setRightValuename( UUID.randomUUID().toString() );
    return rtn;
  }

  @Override
  public boolean validateTestObject( Condition testObject, Object actual ) {
    if ( !( actual instanceof Condition ) ) {
      return false;
    }
    Condition another = (Condition) actual;
    try {
      return ( testObject.getXML().equals( another.getXML() ) );
    } catch ( KettleException ex ) {
      throw new RuntimeException( ex );
    }
  }
}
