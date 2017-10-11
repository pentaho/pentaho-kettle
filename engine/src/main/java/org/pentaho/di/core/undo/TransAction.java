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

package org.pentaho.di.core.undo;

import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * This class store undo and redo information...
 *
 * Possible changes to a transformation:
 * <p>
 * step
 * <p>
 * hop
 * <p>
 * note
 * <p>
 * connection
 * <p>
 *
 * Build an Undo/Redo class containing:
 * <p>
 * Type of change
 * <p>
 * Description of action
 * <p>
 * Link to previous infomation
 * <p>
 *
 * @author Matt
 * @since 19-12-2003
 *
 */
public class TransAction {
  private static Class<?> PKG = TransAction.class; // for i18n purposes, needed by Translator2!!

  public static final int TYPE_ACTION_NONE = 0;
  public static final int TYPE_ACTION_CHANGE_STEP = 1;
  public static final int TYPE_ACTION_CHANGE_CONNECTION = 2;
  public static final int TYPE_ACTION_CHANGE_HOP = 3;
  public static final int TYPE_ACTION_CHANGE_NOTE = 4;
  public static final int TYPE_ACTION_NEW_STEP = 5;
  public static final int TYPE_ACTION_NEW_CONNECTION = 6;
  public static final int TYPE_ACTION_NEW_HOP = 7;
  public static final int TYPE_ACTION_NEW_NOTE = 8;
  public static final int TYPE_ACTION_DELETE_STEP = 9;
  public static final int TYPE_ACTION_DELETE_CONNECTION = 10;
  public static final int TYPE_ACTION_DELETE_HOP = 11;
  public static final int TYPE_ACTION_DELETE_NOTE = 12;
  public static final int TYPE_ACTION_POSITION_STEP = 13;
  public static final int TYPE_ACTION_POSITION_NOTE = 14;

  public static final int TYPE_ACTION_CHANGE_JOB_ENTRY = 15;
  public static final int TYPE_ACTION_CHANGE_JOB_HOP = 16;
  public static final int TYPE_ACTION_NEW_JOB_ENTRY = 17;
  public static final int TYPE_ACTION_NEW_JOB_HOP = 18;
  public static final int TYPE_ACTION_DELETE_JOB_ENTRY = 19;
  public static final int TYPE_ACTION_DELETE_JOB_HOP = 20;
  public static final int TYPE_ACTION_POSITION_JOB_ENTRY = 21;

  public static final int TYPE_ACTION_CHANGE_TABLEITEM = 22;
  public static final int TYPE_ACTION_NEW_TABLEITEM = 23;
  public static final int TYPE_ACTION_DELETE_TABLEITEM = 24;
  public static final int TYPE_ACTION_POSITION_TABLEITEM = 25;

  public static final int TYPE_ACTION_CHANGE_TABLE = 26;
  public static final int TYPE_ACTION_CHANGE_RELATIONSHIP = 27;
  public static final int TYPE_ACTION_NEW_TABLE = 28;
  public static final int TYPE_ACTION_NEW_RELATIONSHIP = 29;
  public static final int TYPE_ACTION_DELETE_TABLE = 30;
  public static final int TYPE_ACTION_DELETE_RELATIONSHIP = 31;
  public static final int TYPE_ACTION_POSITION_TABLE = 32;

  public static final int TYPE_ACTION_NEW_SLAVE = 33;
  public static final int TYPE_ACTION_CHANGE_SLAVE = 34;
  public static final int TYPE_ACTION_DELETE_SLAVE = 35;

  public static final int TYPE_ACTION_NEW_CLUSTER = 36;
  public static final int TYPE_ACTION_CHANGE_CLUSTER = 37;
  public static final int TYPE_ACTION_DELETE_CLUSTER = 38;

  public static final int TYPE_ACTION_NEW_PARTITION = 39;
  public static final int TYPE_ACTION_CHANGE_PARTITION = 40;
  public static final int TYPE_ACTION_DELETE_PARTITION = 41;

  public static final String[] desc_action = new String[] {
    "", BaseMessages.getString( PKG, "TransAction.label.ChangeStep" ),
    BaseMessages.getString( PKG, "TransAction.label.ChangeConnection" ),
    BaseMessages.getString( PKG, "TransAction.label.ChangeHop" ),
    BaseMessages.getString( PKG, "TransAction.label.ChangeNote" ),
    BaseMessages.getString( PKG, "TransAction.label.NewStep" ),
    BaseMessages.getString( PKG, "TransAction.label.NewConnection" ),
    BaseMessages.getString( PKG, "TransAction.label.NewHop" ),
    BaseMessages.getString( PKG, "TransAction.label.NewNote" ),
    BaseMessages.getString( PKG, "TransAction.label.DeleteStep" ),
    BaseMessages.getString( PKG, "TransAction.label.DeleteConnection" ),
    BaseMessages.getString( PKG, "TransAction.label.DeleteHop" ),
    BaseMessages.getString( PKG, "TransAction.label.DeleteNote" ),
    BaseMessages.getString( PKG, "TransAction.label.PositionStep" ),
    BaseMessages.getString( PKG, "TransAction.label.PositionNote" ),
    BaseMessages.getString( PKG, "TransAction.label.ChangeJobEntry" ),
    BaseMessages.getString( PKG, "TransAction.label.ChangeJobHop" ),
    BaseMessages.getString( PKG, "TransAction.label.NewJobEntry" ),
    BaseMessages.getString( PKG, "TransAction.label.NewJobHop" ),
    BaseMessages.getString( PKG, "TransAction.label.DeleteJobEntry" ),
    BaseMessages.getString( PKG, "TransAction.label.DeleteJobHop" ),
    BaseMessages.getString( PKG, "TransAction.label.PositionJobEntry" ),
    BaseMessages.getString( PKG, "TransAction.label.ChangeTableRow" ),
    BaseMessages.getString( PKG, "TransAction.label.NewTableRow" ),
    BaseMessages.getString( PKG, "TransAction.label.DeleteTableRow" ),
    BaseMessages.getString( PKG, "TransAction.label.PositionTableRow" ),
    BaseMessages.getString( PKG, "TransAction.label.ChangeTable" ),
    BaseMessages.getString( PKG, "TransAction.label.ChangeRelationship" ),
    BaseMessages.getString( PKG, "TransAction.label.NewTable" ),
    BaseMessages.getString( PKG, "TransAction.label.NewRelationship" ),
    BaseMessages.getString( PKG, "TransAction.label.DeleteTable" ),
    BaseMessages.getString( PKG, "TransAction.label.DeleteRelationship" ),
    BaseMessages.getString( PKG, "TransAction.label.PositionTable" ) };

  private int type;
  private Object[] previous;
  private Point[] previous_location;
  private int[] previous_index;

  private Object[] current;
  private Point[] current_location;
  private int[] current_index;

  private boolean nextAlso;

  public TransAction() {
    type = TYPE_ACTION_NONE;
  }

  public void setDelete( Object[] prev, int[] idx ) {
    current = prev;
    current_index = idx;

    if ( prev[0] instanceof StepMeta ) {
      type = TYPE_ACTION_DELETE_STEP;
    }
    if ( prev[0] instanceof DatabaseMeta ) {
      type = TYPE_ACTION_DELETE_CONNECTION;
    }
    if ( prev[0] instanceof TransHopMeta ) {
      type = TYPE_ACTION_DELETE_HOP;
    }
    if ( prev[0] instanceof NotePadMeta ) {
      type = TYPE_ACTION_DELETE_NOTE;
    }
    if ( prev[0] instanceof JobEntryCopy ) {
      type = TYPE_ACTION_DELETE_JOB_ENTRY;
    }
    if ( prev[0] instanceof JobHopMeta ) {
      type = TYPE_ACTION_DELETE_JOB_HOP;
    }
    if ( prev[0] instanceof String[] ) {
      type = TYPE_ACTION_DELETE_TABLEITEM;
    }
  }

  public void setChanged( Object[] prev, Object[] curr, int[] idx ) {
    previous = prev;
    current = curr;
    current_index = idx;
    previous_index = idx;

    if ( prev[0] instanceof StepMeta ) {
      type = TYPE_ACTION_CHANGE_STEP;
    }
    if ( prev[0] instanceof DatabaseMeta ) {
      type = TYPE_ACTION_CHANGE_CONNECTION;
    }
    if ( prev[0] instanceof TransHopMeta ) {
      type = TYPE_ACTION_CHANGE_HOP;
    }
    if ( prev[0] instanceof NotePadMeta ) {
      type = TYPE_ACTION_CHANGE_NOTE;
    }
    if ( prev[0] instanceof JobEntryCopy ) {
      type = TYPE_ACTION_CHANGE_JOB_ENTRY;
    }
    if ( prev[0] instanceof JobHopMeta ) {
      type = TYPE_ACTION_CHANGE_JOB_HOP;
    }
    if ( prev[0] instanceof String[] ) {
      type = TYPE_ACTION_CHANGE_TABLEITEM;
    }
  }

  public void setNew( Object[] prev, int[] position ) {
    if ( prev.length == 0 ) {
      return;
    }

    current = prev;
    current_index = position;
    previous = null;

    if ( prev[0] instanceof StepMeta ) {
      type = TYPE_ACTION_NEW_STEP;
    }
    if ( prev[0] instanceof DatabaseMeta ) {
      type = TYPE_ACTION_NEW_CONNECTION;
    }
    if ( prev[0] instanceof TransHopMeta ) {
      type = TYPE_ACTION_NEW_HOP;
    }
    if ( prev[0] instanceof NotePadMeta ) {
      type = TYPE_ACTION_NEW_NOTE;
    }
    if ( prev[0] instanceof JobEntryCopy ) {
      type = TYPE_ACTION_NEW_JOB_ENTRY;
    }
    if ( prev[0] instanceof JobHopMeta ) {
      type = TYPE_ACTION_NEW_JOB_HOP;
    }
    if ( prev[0] instanceof String[] ) {
      type = TYPE_ACTION_NEW_TABLEITEM;
    }
  }

  public void setPosition( Object[] obj, int[] idx, Point[] prev, Point[] curr ) {
    if ( prev.length != curr.length ) {
      return;
    }

    previous_location = new Point[prev.length];
    current_location = new Point[curr.length];
    current = obj;
    current_index = idx;

    for ( int i = 0; i < prev.length; i++ ) {
      previous_location[i] = new Point( prev[i].x, prev[i].y );
      current_location[i] = new Point( curr[i].x, curr[i].y );
    }

    Object fobj = obj[0];
    if ( fobj instanceof StepMeta ) {
      type = TYPE_ACTION_POSITION_STEP;
    }
    if ( fobj instanceof NotePadMeta ) {
      type = TYPE_ACTION_POSITION_NOTE;
    }
    if ( fobj instanceof JobEntryCopy ) {
      type = TYPE_ACTION_POSITION_JOB_ENTRY;
    }
  }

  public void setItemMove( int[] prev, int[] curr ) {
    previous_location = null;
    current_location = null;
    current = null;
    current_index = curr;
    previous = null;
    previous_index = prev;

    type = TYPE_ACTION_POSITION_TABLEITEM;
  }

  public int getType() {
    return type;
  }

  public Object[] getPrevious() {
    return previous;
  }

  public Object[] getCurrent() {
    return current;
  }

  public Point[] getPreviousLocation() {
    return previous_location;
  }

  public Point[] getCurrentLocation() {
    return current_location;
  }

  public int[] getPreviousIndex() {
    return previous_index;
  }

  public int[] getCurrentIndex() {
    return current_index;
  }

  /**
   * Indicate that the next operations needs to be undone too.
   *
   * @param nextAlso
   *          The nextAlso to set.
   */
  public void setNextAlso( boolean nextAlso ) {
    this.nextAlso = nextAlso;
  }

  /**
   * Get the status of the nextAlso flag.
   *
   * @return true if the next operation needs to be done too.
   */
  public boolean getNextAlso() {
    return nextAlso;
  }

  public String toString() {
    String retval = "";
    if ( type < 0 || type >= desc_action.length ) {
      return TransAction.class.getName();
    }

    retval = desc_action[type];

    if ( current != null && current.length > 1 ) {
      retval += " (x" + current.length + ")";
    }

    return retval;
  }
}
