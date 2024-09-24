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

package org.pentaho.di.trans;

import java.util.List;
import java.util.Objects;

import org.pentaho.di.base.BaseHopMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

/*
 * Created on 19-jun-2003
 *
 */

/**
 * Defines a link between 2 steps in a transformation
 */
public class TransHopMeta extends BaseHopMeta<StepMeta> implements Comparable<TransHopMeta> {
  private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_HOP_TAG = "hop";
  public static final String XML_FROM_TAG = "from";
  public static final String XML_TO_TAG = "to";

  public TransHopMeta( StepMeta from, StepMeta to, boolean en ) {
    this.from = from;
    this.to = to;
    enabled = en;
  }

  public TransHopMeta( StepMeta from, StepMeta to ) {
    this.from = from;
    this.to = to;
    enabled = true;
  }

  public TransHopMeta() {
    this( null, null, false );
  }

  public TransHopMeta( Node hopnode, List<StepMeta> steps ) throws KettleXMLException {
    try {
      this.from = searchStep( steps, XMLHandler.getTagValue( hopnode, TransHopMeta.XML_FROM_TAG ) );
      this.to = searchStep( steps, XMLHandler.getTagValue( hopnode, TransHopMeta.XML_TO_TAG ) );
      String en = XMLHandler.getTagValue( hopnode, "enabled" );

      if ( en == null ) {
        enabled = true;
      } else {
        enabled = en.equalsIgnoreCase( "Y" );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "TransHopMeta.Exception.UnableToLoadHopInfo" ), e );
    }
  }

  public void setFromStep( StepMeta from ) {
    this.from = from;
  }

  public void setToStep( StepMeta to ) {
    this.to = to;
  }

  public StepMeta getFromStep() {
    return this.from;
  }

  public StepMeta getToStep() {
    return this.to;
  }

  private StepMeta searchStep( List<StepMeta> steps, String name ) {
    for ( StepMeta stepMeta : steps ) {
      if ( stepMeta.getName().equalsIgnoreCase( name ) ) {
        return stepMeta;
      }
    }

    return null;
  }

  public boolean equals( Object obj ) {
    TransHopMeta other = (TransHopMeta) obj;
    if ( this.from == null || this.to == null ) {
      return false;
    }
    return this.from.equals( other.getFromStep() ) && this.to.equals( other.getToStep() );
  }

  public int hashCode() {
    return Objects.hash( to, from );
  }

  /**
   * Compare 2 hops.
   */
  public int compareTo( TransHopMeta obj ) {
    return toString().compareTo( obj.toString() );
  }

  public void flip() {
    StepMeta dummy = this.from;
    this.from = this.to;
    this.to = dummy;
  }

  public String toString() {
    String str_fr = ( this.from == null ) ? "(empty)" : this.from.getName();
    String str_to = ( this.to == null ) ? "(empty)" : this.to.getName();
    return str_fr + " --> " + str_to + " (" + ( enabled ? "enabled" : "disabled" ) + ")";
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    if ( this.from != null && this.to != null ) {
      retval.append( "    " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
      retval.append( "      " ).append( XMLHandler.addTagValue( TransHopMeta.XML_FROM_TAG, this.from.getName() ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( TransHopMeta.XML_TO_TAG, this.to.getName() ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "enabled", enabled ) );
      retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );
    }

    return retval.toString();
  }
}
