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


package org.pentaho.amazon.s3;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutput;


public class S3FileOutput extends TextFileOutput {

  public S3FileOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    init( smi );
    return super.init( smi, sdi );
  }

  @Override public void markStop() {
    super.markStop();
    String accessKeySystemProperty = System.getProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY );
    String secretKeySystemProperty = System.getProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY );

    if ( !StringUtil.isEmpty( accessKeySystemProperty ) ) {
      System.setProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, "" );
    }
    if ( !StringUtil.isEmpty( secretKeySystemProperty ) ) {
      System.setProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY, "" );
    }
  }

  @VisibleForTesting
  void init( StepMetaInterface smi ) {
    /* For legacy transformations containing AWS S3 access credentials, {@link Const#KETTLE_USE_AWS_DEFAULT_CREDENTIALS} can force Spoon to use
     * the Amazon Default Credentials Provider Chain instead of using the credentials embedded in the transformation metadata. */
    if ( !ValueMetaBase.convertStringToBoolean( Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_USE_AWS_DEFAULT_CREDENTIALS ), "N" ) ) ) {
      S3FileOutputMeta s3Meta = (S3FileOutputMeta) smi;

      String accessKeySystemProperty = System.getProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY );
      String secretKeySystemProperty = System.getProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY );

      if ( !StringUtil.isEmpty( s3Meta.getAccessKey() ) && StringUtil.isEmpty( accessKeySystemProperty ) ) {
        System.setProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( s3Meta.getAccessKey() ) ) );
      }
      if ( !StringUtil.isEmpty( s3Meta.getSecretKey() ) && StringUtil.isEmpty( secretKeySystemProperty ) ) {
        System.setProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY, Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( s3Meta.getSecretKey() ) ) );
      }
    }
  }
}
