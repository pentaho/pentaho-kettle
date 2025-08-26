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


package org.pentaho.di.trans.steps.selectvalues;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class SelectValuesHelper extends BaseStepHelper {

  private static final String LOCALES = "locales";
  private static final String TIMEZONES = "timezones";
  private static final String ENCODINGS = "encodings";

  public SelectValuesHelper() {
    super();
  }

  /**
   * Handles step-specific actions for SelectValues.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                         Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    switch ( method ) {
      case LOCALES:
        response = getLocales();
        break;
      case TIMEZONES:
        response = getTimezones();
        break;
      case ENCODINGS:
        response = getEncodings();
        break;
      default:
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        break;
    }
    return response;
  }

  /**
   * Retrieves the list of available locales and returns them as a JSON object.
   *
   * @return A JSON object containing the list of locales.
   */
  public JSONObject getLocales() {
    JSONObject response = new JSONObject();
    JSONArray locales = new JSONArray();
    locales.addAll( Arrays.asList( EnvUtil.getLocaleList() ) );
    response.put( LOCALES, locales );
    return response;
  }

  /**
   * Retrieves the list of available time zones and returns them as a JSON object.
   *
   * @return A JSON object containing the list of time zones.
   */
  public JSONObject getTimezones() {
    JSONObject response = new JSONObject();
    JSONArray timezones = new JSONArray();
    timezones.addAll( Arrays.asList( EnvUtil.getTimeZones() ) );
    response.put( TIMEZONES, timezones );
    return response;
  }

  /**
   * Retrieves the list of available character encodings and returns them as a JSON object.
   *
   * @return A JSON object containing the list of character encodings.
   */
  public JSONObject getEncodings() {
    JSONObject response = new JSONObject();
    JSONArray encodings = new JSONArray();
    encodings.addAll( Arrays.asList( getCharsets() ) );
    response.put( ENCODINGS, encodings );
    return response;
  }

  /**
   * Retrieves the list of available character sets.
   *
   * @return An array of character set display names.
   */
  public String[] getCharsets() {
    Collection<Charset> charsetCol = Charset.availableCharsets().values();
    String[] charsets = new String[ charsetCol.size() ];
    int i = 0;
    for ( Charset charset : charsetCol ) {
      charsets[ i++ ] = charset.displayName();
    }
    return charsets;
  }

}
