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

package org.pentaho.di.core.gui;

import java.util.EnumSet;
import java.util.Set;

/**
 * When we draw something in Spoon (TransPainter) we keep a list of all the things we draw and the object that's behind
 * it. That should make it a lot easier to track what was drawn, setting tooltips, etc.
 *
 * @author Matt
 *
 */
public class AreaOwner {

  public enum AreaType {
    NOTE, REMOTE_INPUT_STEP, REMOTE_OUTPUT_STEP, STEP_PARTITIONING, STEP_ICON, STEP_ERROR_ICON, STEP_ERROR_RED_ICON,
      STEP_INPUT_HOP_ICON, STEP_OUTPUT_HOP_ICON, STEP_INFO_HOP_ICON, STEP_ERROR_HOP_ICON, STEP_TARGET_HOP_ICON,
      HOP_COPY_ICON, ROW_DISTRIBUTION_ICON, HOP_ERROR_ICON, HOP_INFO_ICON, HOP_INFO_STEP_COPIES_ERROR,

      MINI_ICONS_BALLOON,

      STEP_TARGET_HOP_ICON_OPTION, STEP_EDIT_ICON, STEP_MENU_ICON, STEP_COPIES_TEXT, STEP_DATA_SERVICE,

      JOB_ENTRY_ICON, JOB_HOP_ICON, JOB_HOP_PARALLEL_ICON, JOB_ENTRY_MINI_ICON_INPUT, JOB_ENTRY_MINI_ICON_OUTPUT,
      JOB_ENTRY_MINI_ICON_CONTEXT, JOB_ENTRY_MINI_ICON_EDIT,

      JOB_ENTRY_BUSY, JOB_ENTRY_RESULT_SUCCESS, JOB_ENTRY_RESULT_FAILURE, JOB_ENTRY_RESULT_CHECKPOINT,
      STEP_INJECT_ICON,

      CUSTOM;

    private static final Set<AreaType> jobContextMenuArea = EnumSet.of( MINI_ICONS_BALLOON, JOB_ENTRY_MINI_ICON_INPUT,
        JOB_ENTRY_MINI_ICON_EDIT, JOB_ENTRY_MINI_ICON_CONTEXT, JOB_ENTRY_MINI_ICON_OUTPUT );

    private static final Set<AreaType> stepContextMenuArea = EnumSet.of( MINI_ICONS_BALLOON, STEP_INPUT_HOP_ICON,
        STEP_EDIT_ICON, STEP_MENU_ICON, STEP_OUTPUT_HOP_ICON, STEP_INJECT_ICON );

    public boolean belongsToJobContextMenu() {
      return jobContextMenuArea.contains( this );
    }

    public boolean belongsToTransContextMenu() {
      return stepContextMenuArea.contains( this );
    }

  }

  private Rectangle area;
  private Object parent;
  private Object owner;
  private AreaType areaType;
  private Object extensionAreaType;

  /**
   * @param x
   * @param y
   * @param width
   * @param heigth
   * @param owner
   */
  public AreaOwner( AreaType areaType, int x, int y, int width, int heigth, Point offset, Object parent,
    Object owner ) {
    super();
    this.areaType = areaType;
    this.area = new Rectangle( x - offset.x, y - offset.y, width, heigth );
    this.parent = parent;
    this.owner = owner;
  }

  public AreaOwner( Object extensionAreaType, int x, int y, int width, int heigth, Point offset, Object parent,
      Object owner ) {
    super();
    this.extensionAreaType = extensionAreaType;
    this.area = new Rectangle( x - offset.x, y - offset.y, width, heigth );
    this.parent = parent;
    this.owner = owner;
  }

  /**
   * Validate if a certain coordinate is contained in the area
   *
   * @param x
   *          x-coordinate
   * @param y
   *          y-coordinate
   * @return true if the specified coordinate is contained in the area
   */
  public boolean contains( int x, int y ) {
    return area.contains( x, y );
  }

  /**
   * @return the area
   */
  public Rectangle getArea() {
    return area;
  }

  /**
   * @param area
   *          the area to set
   */
  public void setArea( Rectangle area ) {
    this.area = area;
  }

  /**
   * @return the owner
   */
  public Object getOwner() {
    return owner;
  }

  /**
   * @param owner
   *          the owner to set
   */
  public void setOwner( Object owner ) {
    this.owner = owner;
  }

  /**
   * @return the parent
   */
  public Object getParent() {
    return parent;
  }

  /**
   * @param parent
   *          the parent to set
   */
  public void setParent( Object parent ) {
    this.parent = parent;
  }

  /**
   * @return the areaType
   */
  public AreaType getAreaType() {
    return areaType;
  }

  /**
   * @param areaType
   *          the areaType to set
   */
  public void setAreaType( AreaType areaType ) {
    this.areaType = areaType;
  }

  public Object getExtensionAreaType() {
    return extensionAreaType;
  }

  public void setExtensionAreaType( Object extensionAreaType ) {
    this.extensionAreaType = extensionAreaType;
  }
}
