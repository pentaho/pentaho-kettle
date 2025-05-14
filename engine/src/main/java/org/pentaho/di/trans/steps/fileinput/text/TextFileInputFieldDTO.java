/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.fileinput.text;

public class TextFileInputFieldDTO {

  private String name;
  private String type;
  private String format;
  private String position;
  private String length;
  private String precision;
  private String currency;
  private String decimal;
  private String group;
  private String trimType;
  private String nullif;
  private String ifnull;
  private String repeat;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getLength() {
    return length;
  }

  public void setLength(String length) {
    this.length = length;
  }

  public String getPrecision() {
    return precision;
  }

  public void setPrecision(String precision) {
    this.precision = precision;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getDecimal() {
    return decimal;
  }

  public void setDecimal(String decimal) {
    this.decimal = decimal;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getTrimType() {
    return trimType;
  }

  public void setTrimType(String trimType) {
    this.trimType = trimType;
  }

  public String getNullif() {
    return nullif;
  }

  public void setNullif(String nullif) {
    this.nullif = nullif;
  }

  public String getIfnull() {
    return ifnull;
  }

  public void setIfnull(String ifnull) {
    this.ifnull = ifnull;
  }

  public String getRepeat() {
    return repeat;
  }

  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }

}
