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

package org.pentaho.di.job;

import org.pentaho.di.base.BaseHopMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.w3c.dom.Node;

/**
 * This class defines a hop from one job entry copy to another.
 *
 * @author Matt
 * @since 19-06-2003
 *
 */
public class JobHopMeta extends BaseHopMeta<JobEntryCopy> {
  private static Class<?> PKG = JobHopMeta.class; // for i18n purposes, needed by Translator2!!

  private boolean evaluation;
  private boolean unconditional;

  public JobHopMeta() {
    this( (JobEntryCopy) null, (JobEntryCopy) null );
  }

  public JobHopMeta( JobEntryCopy from, JobEntryCopy to ) {
    this.from = from;
    this.to = to;
    enabled = true;
    split = false;
    evaluation = true;
    unconditional = false;
    id = null;

    if ( from != null && from.isStart() ) {
      setUnconditional();
    }
  }

  public JobHopMeta( Node hopnode, JobMeta job ) throws KettleXMLException {
    try {
      String from_name = XMLHandler.getTagValue( hopnode, "from" );
      String to_name = XMLHandler.getTagValue( hopnode, "to" );
      String sfrom_nr = XMLHandler.getTagValue( hopnode, "from_nr" );
      String sto_nr = XMLHandler.getTagValue( hopnode, "to_nr" );
      String senabled = XMLHandler.getTagValue( hopnode, "enabled" );
      String sevaluation = XMLHandler.getTagValue( hopnode, "evaluation" );
      String sunconditional = XMLHandler.getTagValue( hopnode, "unconditional" );

      int from_nr, to_nr;
      from_nr = Const.toInt( sfrom_nr, 0 );
      to_nr = Const.toInt( sto_nr, 0 );

      this.from = job.findJobEntry( from_name, from_nr, true );
      this.to = job.findJobEntry( to_name, to_nr, true );

      if ( senabled == null ) {
        enabled = true;
      } else {
        enabled = "Y".equalsIgnoreCase( senabled );
      }
      if ( sevaluation == null ) {
        evaluation = true;
      } else {
        evaluation = "Y".equalsIgnoreCase( sevaluation );
      }
      unconditional = "Y".equalsIgnoreCase( sunconditional );
    } catch ( Exception e ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "JobHopMeta.Exception.UnableToLoadHopInfoXML" ), e );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );
    if ( ( null != this.from ) && ( null != this.to ) ) {
      retval.append( "    " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
      retval.append( "      " ).append( XMLHandler.addTagValue( "from", this.from.getName() ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "to", this.to.getName() ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "from_nr", this.from.getNr() ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "to_nr", this.to.getNr() ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "enabled", enabled ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "evaluation", evaluation ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "unconditional", unconditional ) );
      retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );
    }

    return retval.toString();
  }

  public boolean getEvaluation() {
    return evaluation;
  }

  public void setEvaluation() {
    if ( !evaluation ) {
      setChanged();
    }
    setEvaluation( true );
  }

  public void setEvaluation( boolean e ) {
    if ( evaluation != e ) {
      setChanged();
    }
    evaluation = e;
  }

  public void setUnconditional() {
    if ( !unconditional ) {
      setChanged();
    }
    unconditional = true;
  }

  public void setConditional() {
    if ( unconditional ) {
      setChanged();
    }
    unconditional = false;
  }

  public boolean isUnconditional() {
    return unconditional;
  }

  public void setSplit( boolean split ) {
    if ( this.split != split ) {
      setChanged();
    }
    this.split = split;
  }

  public boolean isSplit() {
    return split;
  }

  public String getDescription() {
    if ( isUnconditional() ) {
      return BaseMessages.getString( PKG, "JobHopMeta.Msg.ExecNextJobEntryUncondition" );
    } else {
      if ( getEvaluation() ) {
        return BaseMessages.getString( PKG, "JobHopMeta.Msg.ExecNextJobEntryFlawLess" );
      } else {
        return BaseMessages.getString( PKG, "JobHopMeta.Msg.ExecNextJobEntryFailed" );
      }
    }
  }

  public String toString() {
    return getDescription();
    // return from_entry.getName()+"."+from_entry.getNr()+" --> "+to_entry.getName()+"."+to_entry.getNr();
  }

  public JobEntryCopy getFromEntry() {
    return this.from;
  }

  public void setFromEntry( JobEntryCopy fromEntry ) {
    this.from = fromEntry;
    changed = true;
  }

  public JobEntryCopy getToEntry() {
    return this.to;
  }

  public void setToEntry( JobEntryCopy toEntry ) {
    this.to = toEntry;
    changed = true;
  }

  /**
   * @param unconditional
   *          the unconditional to set
   */
  public void setUnconditional( boolean unconditional ) {
    if ( this.unconditional != unconditional ) {
      setChanged();
    }
    this.unconditional = unconditional;
  }

}
