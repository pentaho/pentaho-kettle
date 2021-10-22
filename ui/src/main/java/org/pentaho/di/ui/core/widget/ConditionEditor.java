/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.widget;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterValueDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Widget that allows you to edit a Condition in a graphical way.
 *
 * @author Matt
 * @since 29-07-2004
 *
 */

public class ConditionEditor extends Canvas {
  private static Class<?> PKG = ConditionEditor.class; // for i18n purposes, needed by Translator2!!

  private static final int X_PADDING = 18;
  private static final String STRING_NOT = BaseMessages.getString( PKG, "ConditionEditor.StringNot" );
  private static final String STRING_UP = BaseMessages.getString( PKG, "ConditionEditor.StringUp" );

  private static final int AREA_NONE = 0;
  private static final int AREA_BACKGROUND = 1;
  private static final int AREA_NOT = 2;
  private static final int AREA_CONDITION = 3;
  private static final int AREA_SUBCONDITION = 4;
  private static final int AREA_OPERATOR = 5;
  private static final int AREA_UP = 6;
  private static final int AREA_LEFT = 7;
  private static final int AREA_FUNCTION = 8;
  private static final int AREA_RIGHT_VALUE = 9;
  private static final int AREA_RIGHT_EXACT = 10;
  private static final int AREA_ICON_ADD = 11;

  protected Canvas widget;
  private Shell shell;
  private Display display;
  private Condition active_condition;
  // private Props props;
  private Color bg, white, black, red, green, blue, gray;
  private Font fixed;

  private Image imageAdd;

  private Rectangle size_not, size_widget, size_and_not;
  private Rectangle size_up;
  private Rectangle size_left, size_fn, size_rightval, size_rightex;
  private Rectangle[] size_cond;
  private Rectangle[] size_oper;
  private Rectangle size_add;
  private Rectangle maxdrawn;

  private int hover_condition;
  private int hover_operator;
  private boolean hover_not, hover_up;
  private boolean hover_left, hover_fn, hover_rightval, hover_rightex;

  private int previous_area;
  private int previous_area_nr;

  private ArrayList<Condition> parents;
  private RowMetaInterface fields;

  private int max_field_length;

  private ScrollBar sbVertical, sbHorizontal;

  private int offsetx, offsety;

  private ArrayList<ModifyListener> modListeners;

  private String messageString;
  private Menu mPop;

  public ConditionEditor( Composite composite, int arg1, Condition co, RowMetaInterface inputFields ) {
    super( composite, arg1 | SWT.NO_BACKGROUND | SWT.V_SCROLL | SWT.H_SCROLL );

    widget = this;

    this.active_condition = co;
    this.fields = inputFields;

    imageAdd = GUIResource.getInstance().getImage( "ui/images/Add.svg" );

    modListeners = new ArrayList<ModifyListener>();

    sbVertical = getVerticalBar();
    sbHorizontal = getHorizontalBar();
    offsetx = 0;
    offsety = 0;
    maxdrawn = null;

    size_not = null;
    size_widget = null;
    size_cond = null;

    previous_area = -1;
    previous_area_nr = -1;

    parents = new ArrayList<Condition>(); // Remember parent in drill-down...

    hover_condition = -1;
    hover_operator = -1;
    hover_not = false;
    hover_up = false;
    hover_left = false;
    hover_fn = false;
    hover_rightval = false;
    hover_rightex = false;

    /*
     * Determine the maximum field length...
     */
    getMaxFieldLength();

    shell = composite.getShell();
    display = shell.getDisplay();

    bg = GUIResource.getInstance().getColorBackground();
    fixed = GUIResource.getInstance().getFontFixed();

    white = GUIResource.getInstance().getColorWhite();
    black = GUIResource.getInstance().getColorBlack();
    red = GUIResource.getInstance().getColorRed();
    green = GUIResource.getInstance().getColorGreen();
    blue = GUIResource.getInstance().getColorBlue();
    gray = GUIResource.getInstance().getColorDarkGray();

    widget.addPaintListener( new PaintListener() {
      @Override
      public void paintControl( PaintEvent pe ) {
        Rectangle r = widget.getBounds();
        if ( r.width > 0 && r.height > 0 ) {
          repaint( pe.gc, r.width, r.height );
        }
      }
    } );

    widget.addMouseMoveListener( new MouseMoveListener() {
      @Override
      public void mouseMove( MouseEvent e ) {
        Point screen = new Point( e.x, e.y );
        int area = getAreaCode( screen );

        int nr = 0;
        boolean need_redraw = false;

        hover_condition = -1;
        hover_operator = -1;
        hover_not = false;
        hover_up = false;
        hover_left = false;
        hover_fn = false;
        hover_rightval = false;
        hover_rightex = false;

        if ( area != AREA_ICON_ADD ) {
          setToolTipText( null );
        } else {
          setToolTipText( BaseMessages.getString( PKG, "ConditionEditor.AddCondition.Label" ) );
        }

        switch ( area ) {
          case AREA_NOT:
            hover_not = true;
            nr = 1;
            break;
          case AREA_UP:
            hover_up = getLevel() > 0;
            nr = 1;
            break;
          case AREA_BACKGROUND:
            break;
          case AREA_SUBCONDITION:
            hover_condition = getNrSubcondition( screen );
            nr = hover_condition;
            break;
          case AREA_OPERATOR:
            hover_operator = getNrOperator( screen );
            nr = hover_operator;
            break;
          case AREA_LEFT:
            hover_left = true;
            nr = 1;
            break;
          case AREA_FUNCTION:
            hover_fn = true;
            nr = 1;
            break;
          case AREA_RIGHT_VALUE:
            hover_rightval = true;
            nr = 1;
            break;
          case AREA_RIGHT_EXACT:
            hover_rightex = true;
            nr = 1;
            break;
          case AREA_CONDITION:
            break;
          case AREA_NONE:
            break;
          default:
            break;
        }

        if ( area != previous_area || nr != previous_area_nr ) {
          need_redraw = true;
        }

        if ( need_redraw ) {
          offsetx = -sbHorizontal.getSelection();
          offsety = -sbVertical.getSelection();
          widget.redraw();
        }

        previous_area = area;
        previous_area_nr = nr;
      }
    } );

    widget.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseDown( MouseEvent e ) {
        Point screen = new Point( e.x, e.y );
        // Point real = Screen2Real(screen);
        int area = getAreaCode( screen );

        if ( e.button == 1 ) { // Left click on widget...

          switch ( area ) {
            case AREA_NOT:
              active_condition.negate();
              setModified();
              widget.redraw();
              break;
            case AREA_OPERATOR:
              int operator = getNrOperator( screen );
              EnterSelectionDialog esd =
                new EnterSelectionDialog( shell, Condition.getRealOperators(),
                  BaseMessages.getString( PKG, "ConditionEditor.Operator.Label" ),
                  BaseMessages.getString( PKG, "ConditionEditor.SelectOperator.Label" ) );
              esd.setAvoidQuickSearch();
              Condition selcond = active_condition.getCondition( operator );
              String def = selcond.getOperatorDesc();
              int defnr = esd.getSelectionNr( Const.trim( def ) );
              String selection = esd.open( defnr );
              if ( selection != null ) {
                int opnr = Condition.getOperator( selection );
                active_condition.getCondition( operator ).setOperator( opnr );
                setModified();
              }
              widget.redraw();
              break;
            case AREA_SUBCONDITION:
              int nr = getNrSubcondition( screen );
              editCondition( nr );
              setMessageString( BaseMessages
                .getString( PKG, "ConditionEditor.GoUpOneLevel.Label", "" + getLevel() ) );
              redraw();
              break;
            case AREA_UP:
              // Go to the parent condition...
              goUp();
              redraw();
              break;
            case AREA_FUNCTION:
              if ( active_condition.isAtomic() ) {
                esd =
                  new EnterSelectionDialog( shell, Condition.functions,
                    BaseMessages.getString( PKG, "ConditionEditor.Functions.Label" ),
                    BaseMessages.getString( PKG, "ConditionEditor.SelectFunction.Label" ) );
                esd.setAvoidQuickSearch();
                def = active_condition.getFunctionDesc();
                defnr = esd.getSelectionNr( def );
                selection = esd.open( defnr );
                if ( selection != null ) {
                  int fnnr = Condition.getFunction( selection );
                  active_condition.setFunction( fnnr );

                  if ( active_condition.getFunction() == Condition.FUNC_NOT_NULL || active_condition.getFunction() == Condition.FUNC_NULL ) {
                    active_condition.setRightValuename( null );
                    active_condition.setRightExact( null );
                  }

                  setModified();
                }
                widget.redraw();
              }
              break;
            case AREA_LEFT:
              if ( active_condition.isAtomic() && fields != null ) {
                esd =
                  new EnterSelectionDialog(
                    shell, fields.getFieldNamesAndTypes( max_field_length ),
                    BaseMessages.getString( PKG, "ConditionEditor.Fields" ),
                    BaseMessages.getString( PKG, "ConditionEditor.SelectAField" ) );
                esd.setAvoidQuickSearch();
                def = active_condition.getLeftValuename();
                defnr = esd.getSelectionNr( def );
                selection = esd.open( defnr );
                if ( selection != null ) {
                  ValueMetaInterface v = fields.getValueMeta( esd.getSelectionNr() );
                  active_condition.setLeftValuename( v.getName() );
                  setModified();
                }
                widget.redraw();
              }
              break;
            case AREA_RIGHT_VALUE:
              if ( active_condition.isAtomic() && fields != null ) {
                esd =
                  new EnterSelectionDialog(
                    shell, fields.getFieldNamesAndTypes( max_field_length ),
                    BaseMessages.getString( PKG, "ConditionEditor.Fields" ),
                    BaseMessages.getString( PKG, "ConditionEditor.SelectAField" ) );
                esd.setAvoidQuickSearch();
                def = active_condition.getLeftValuename();
                defnr = esd.getSelectionNr( def );
                selection = esd.open( defnr );
                if ( selection != null ) {
                  ValueMetaInterface v = fields.getValueMeta( esd.getSelectionNr() );
                  active_condition.setRightValuename( v.getName() );
                  active_condition.setRightExact( null );
                  setModified();
                }
                widget.redraw();
              }
              break;
            case AREA_RIGHT_EXACT:
              if ( active_condition.isAtomic() ) {
                ValueMetaAndData v = active_condition.getRightExact();
                if ( v == null ) {
                  ValueMetaInterface leftval =
                    fields != null ? fields.searchValueMeta( active_condition.getLeftValuename() ) : null;
                  if ( leftval != null ) {
                    try {
                      v =
                        new ValueMetaAndData(
                          ValueMetaFactory.createValueMeta( "constant", leftval.getType() ), null );
                    } catch ( Exception exception ) {
                      new ErrorDialog( shell, "Error", "Error creating value meta object", exception );
                    }
                  } else {
                    v = new ValueMetaAndData( new ValueMetaString( "constant" ), null );
                  }
                }
                EnterValueDialog evd = new EnterValueDialog( shell, SWT.NONE, v.getValueMeta(), v.getValueData() );
                evd.setModalDialog( true ); // To keep the condition editor from being closed with a value dialog still
                                            // open. (PDI-140)
                ValueMetaAndData newval = evd.open();
                if ( newval != null ) {
                  active_condition.setRightValuename( null );
                  active_condition.setRightExact( newval );
                  setModified();
                }
                widget.redraw();
              }
              break;
            case AREA_ICON_ADD:
              addCondition();
              break;

            default:
              break;
          }
        }

      }

      @Override
      public void mouseUp( MouseEvent e ) {
      }
    } );

    widget.addMenuDetectListener( new MenuDetectListener() {

      //
      // set the pop-up menu
      //
      @Override
      public void menuDetected( MenuDetectEvent e ) {

        Point screen = new Point( e.x, e.y );
        Point widgetScreen = widget.toDisplay( 1, 1 );
        Point wRel = new Point( screen.x - widgetScreen.x, screen.y - widgetScreen.y );
        int area = getAreaCode( wRel );
        setMenu( area, wRel );
      }

    } );

    sbVertical.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        offsety = -sbVertical.getSelection();
        widget.redraw();
      }
    } );

    sbHorizontal.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        offsetx = -sbHorizontal.getSelection();
        widget.redraw();
      }
    } );

    widget.addControlListener( new ControlAdapter() {
      @Override
      public void controlResized( ControlEvent arg0 ) {
        size_widget = widget.getBounds();
        setBars();
      }
    } );
  }

  private void getMaxFieldLength() {
    max_field_length = 5;
    if ( fields != null ) {
      for ( int i = 0; i < fields.size(); i++ ) {
        ValueMetaInterface value = fields.getValueMeta( i );
        if ( value != null && value.getName() != null ) {
          int len = fields.getValueMeta( i ).getName().length();
          if ( len > max_field_length ) {
            max_field_length = len;
          }
        }
      }
    }
  }

  public int getLevel() {
    return parents.size();
  }

  public void goUp() {
    if ( parents.size() > 0 ) {
      int last = parents.size() - 1;
      active_condition = parents.get( last );
      parents.remove( last );
    }
    if ( getLevel() > 0 ) {
      setMessageString( BaseMessages.getString( PKG, "ConditionEditor.GoUpOneLevel.Label", "" + getLevel() ) );
    } else {
      setMessageString( BaseMessages.getString( PKG, "ConditionEditor.EditSubCondition" ) );
    }

  }

  @Override
  public void setMenu( Menu menu ) {
    if ( menu != null ) {
      menu.setLocation( this.getDisplay().getCursorLocation() );
      menu.setVisible( true );
    }
  }

  private void setMenu( int area, Point screen ) {
    final int cond_nr = getNrSubcondition( screen );
    if ( mPop != null && !mPop.isDisposed() ) {
      mPop.dispose();
    }

    switch ( area ) {
      case AREA_NOT:
        mPop = new Menu( widget );
        MenuItem miNegate = new MenuItem( mPop, SWT.PUSH );
        miNegate.setText( BaseMessages.getString( PKG, "ConditionEditor.NegateCondition" ) );
        miNegate.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            active_condition.negate();
            widget.redraw();
            setModified();
          }
        } );
        setMenu( mPop );
        break;
      case AREA_BACKGROUND:
      case AREA_ICON_ADD:
        mPop = new Menu( widget );
        MenuItem miAdd = new MenuItem( mPop, SWT.PUSH );
        miAdd.setText( BaseMessages.getString( PKG, "ConditionEditor.AddCondition.Label" ) );
        miAdd.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            addCondition();
          }
        } );
        setMenu( mPop );
        break;
      case AREA_SUBCONDITION:
        mPop = new Menu( widget );
        MenuItem miEdit = new MenuItem( mPop, SWT.PUSH );
        miEdit.setText( BaseMessages.getString( PKG, "ConditionEditor.EditCondition.Label" ) );
        miEdit.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            editCondition( cond_nr );
            setModified();
            widget.redraw();
          }
        } );
        MenuItem miDel = new MenuItem( mPop, SWT.PUSH );
        miDel.setText( BaseMessages.getString( PKG, "ConditionEditor.DeleteCondition.Label" ) );
        miDel.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            removeCondition( cond_nr );
            setModified();
            widget.redraw();
          }
        } );
        // Add a sub-condition in the subcondition... (move down)
        final Condition sub = active_condition.getCondition( cond_nr );
        if ( sub.getLeftValuename() != null ) {
          miAdd = new MenuItem( mPop, SWT.PUSH );
          miAdd.setText( BaseMessages.getString( PKG, "ConditionEditor.AddSubCondition.Label" ) );
          miAdd.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
              Condition c = new Condition();
              c.setOperator( Condition.OPERATOR_AND );
              sub.addCondition( c );
              setModified();
              widget.redraw();
            }
          } );
        }
        // --------------------------------------------------
        new MenuItem( mPop, SWT.SEPARATOR );

        MenuItem miCopy = new MenuItem( mPop, SWT.PUSH );
        miCopy.setText( BaseMessages.getString( PKG, "ConditionEditor.CopyToClipboard" ) );
        miCopy.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            Condition c = active_condition.getCondition( cond_nr );
            try {
              String xml = c.getXML();
              GUIResource.getInstance().toClipboard( xml );
              widget.redraw();
            } catch ( Exception ex ) {
              new ErrorDialog( shell, "Error", "Error encoding to XML", ex );
            }

          }
        } );
        MenuItem miPasteBef = new MenuItem( mPop, SWT.PUSH );
        miPasteBef.setText( BaseMessages.getString( PKG, "ConditionEditor.PasteFromClipboardBeforeCondition" ) );
        miPasteBef.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            String xml = GUIResource.getInstance().fromClipboard();
            try {
              Document d = XMLHandler.loadXMLString( xml );
              Node condNode = XMLHandler.getSubNode( d, "condition" );
              if ( condNode != null ) {
                Condition c = new Condition( condNode );
                active_condition.addCondition( cond_nr, c );
                widget.redraw();
              } else {
                new ErrorDialog( shell, BaseMessages.getString( PKG, "ConditionEditor.Error" ), BaseMessages
                  .getString( PKG, "ConditionEditor.NoConditionFoundXML" ), new KettleXMLException( BaseMessages
                  .getString( PKG, "ConditionEditor.NoConditionFoundXML.Exception", Const.CR + Const.CR + xml ) ) );
              }
            } catch ( KettleXMLException ex ) {
              new ErrorDialog( shell, BaseMessages.getString( PKG, "ConditionEditor.Error" ), BaseMessages
                .getString( PKG, "ConditionEditor.ErrorParsingCondition" ), ex );
            }
          }
        } );
        // --------------------------------------------------
        new MenuItem( mPop, SWT.SEPARATOR );

        MenuItem miPasteAft = new MenuItem( mPop, SWT.PUSH );
        miPasteAft.setText( BaseMessages.getString( PKG, "ConditionEditor.PasteFromClipboardAfterCondition" ) );
        miPasteAft.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            String xml = GUIResource.getInstance().fromClipboard();
            try {
              Document d = XMLHandler.loadXMLString( xml );
              Node condNode = XMLHandler.getSubNode( d, "condition" );
              if ( condNode != null ) {
                Condition c = new Condition( condNode );
                active_condition.addCondition( cond_nr + 1, c );
                widget.redraw();
              } else {
                new ErrorDialog( shell, BaseMessages.getString( PKG, "ConditionEditor.Error" ), BaseMessages
                  .getString( PKG, "ConditionEditor.NoConditionFoundXML" ), new KettleXMLException( BaseMessages
                  .getString( PKG, "ConditionEditor.NoConditionFoundXML.Exception", Const.CR + Const.CR + xml ) ) );
              }
            } catch ( KettleXMLException ex ) {
              new ErrorDialog( shell, BaseMessages.getString( PKG, "ConditionEditor.Error" ), BaseMessages
                .getString( PKG, "ConditionEditor.ErrorParsingCondition" ), ex );
            }
          }
        } );
        // --------------------------------------------------
        new MenuItem( mPop, SWT.SEPARATOR );
        MenuItem miMoveSub = new MenuItem( mPop, SWT.PUSH );
        miMoveSub.setText( BaseMessages.getString( PKG, "ConditionEditor.MoveConditionToSubCondition" ) );
        miMoveSub.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            // Move the condition lower: this means create a subcondition and put the condition there in the list.
            //
            Condition down = active_condition.getCondition( cond_nr );
            Condition c = new Condition();
            c.setOperator( down.getOperator() );
            down.setOperator( Condition.OPERATOR_NONE );
            active_condition.setCondition( cond_nr, c );
            c.addCondition( down );

            widget.redraw();
          }
        } );
        MenuItem miMoveParent = new MenuItem( mPop, SWT.PUSH );
        miMoveParent.setText( BaseMessages.getString( PKG, "ConditionEditor.MoveConditionToParentCondition" ) );
        if ( getLevel() == 0 ) {
          miMoveParent.setEnabled( false );
        }
        miMoveParent.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            // Move the condition lower: this means delete the condition from the active_condition.
            // After that, move it to the parent.
            Condition up = active_condition.getCondition( cond_nr );
            active_condition.removeCondition( cond_nr );
            Condition parent = parents.get( getLevel() - 1 );

            parent.addCondition( up );

            // Take a look upward...
            goUp();

            widget.redraw();
          }
        } );
        // --------------------------------------------------
        new MenuItem( mPop, SWT.SEPARATOR );
        MenuItem miMoveDown = new MenuItem( mPop, SWT.PUSH );
        miMoveDown.setText( BaseMessages.getString( PKG, "ConditionEditor.MoveConditionDown" ) );
        if ( cond_nr >= active_condition.nrConditions() - 1 ) {
          miMoveDown.setEnabled( false );
        }
        miMoveDown.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            Condition down = active_condition.getCondition( cond_nr );
            active_condition.removeCondition( cond_nr );
            active_condition.addCondition( cond_nr + 1, down );

            widget.redraw();
          }
        } );
        MenuItem miMoveUp = new MenuItem( mPop, SWT.PUSH );
        miMoveUp.setText( BaseMessages.getString( PKG, "ConditionEditor.MoveConditionUp" ) );
        if ( cond_nr == 0 ) {
          miMoveUp.setEnabled( false );
        }
        miMoveUp.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            Condition up = active_condition.getCondition( cond_nr );
            active_condition.removeCondition( cond_nr );
            active_condition.addCondition( cond_nr - 1, up );

            widget.redraw();
          }
        } );

        setMenu( mPop );

        break;
      case AREA_OPERATOR:
        Menu mPop = new Menu( widget );
        MenuItem miDown = new MenuItem( mPop, SWT.PUSH );
        miDown.setText( BaseMessages.getString( PKG, "ConditionEditor.MoveDown" ) );
        miDown.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            // Move a condition down!
            // oper_nr = 1 : means move down
            setModified();
            widget.redraw();
          }
        } );
        setMenu( mPop );
        break;

      default:
        setMenu( null );
        break;
    }
  }

  public void repaint( GC gc, int width, int height ) {
    // Initialize some information
    size_not = getNotSize( gc );
    size_widget = getWidgetSize( gc );
    size_and_not = getAndNotSize( gc );
    size_up = getUpSize( gc );
    size_add = getAddSize( gc );
    size_left = null;
    size_fn = null;
    size_rightval = null;
    size_rightex = null;

    // Clear the background...
    gc.setBackground( white );
    gc.setForeground( black );
    gc.fillRectangle( 0, 0, width, height );

    // Set the fixed font:
    gc.setFont( fixed );

    // Atomic condition?
    if ( active_condition.isAtomic() ) {
      size_cond = null;
      drawNegated( gc, 0, 0, active_condition );

      drawAtomic( gc, 0, 0, active_condition );

      // gc.drawText("ATOMIC", 10, size_widget.height-20);
    } else {
      drawNegated( gc, 0, 0, active_condition );

      size_cond = new Rectangle[active_condition.nrConditions()];
      size_oper = new Rectangle[active_condition.nrConditions()];

      int basex = 10;
      int basey = size_not.y + 5;

      for ( int i = 0; i < active_condition.nrConditions(); i++ ) {
        Point to = drawCondition( gc, basex, basey, i, active_condition.getCondition( i ) );
        basey += size_and_not.height + to.y + 15;
      }
    }

    gc.drawImage( imageAdd, size_add.x, size_add.y );

    /*
     * Draw the up-symbol if needed...
     */
    if ( parents.size() > 0 ) {
      drawUp( gc );
    }

    if ( messageString != null ) {
      drawMessage( gc );
    }

    /*
     * Determine the maximum size of the displayed items... Normally, they are all size up already.
     */
    getMaxSize();

    /*
     * Set the scroll bars: show/don't show and set the size
     */
    setBars();
  }

  private Rectangle getNotSize( GC gc ) {
    Point p = gc.textExtent( STRING_NOT );
    return new Rectangle( 0, 0, p.x + 10, p.y + 4 );
  }

  private Rectangle getWidgetSize( GC gc ) {
    Rectangle cs = widget.getBounds(); // Canvas size
    return new Rectangle( 0, 0, cs.width, cs.height );
  }

  private Rectangle getAndNotSize( GC gc ) {
    Point p = gc.textExtent( Condition.operators[Condition.OPERATOR_AND_NOT] );
    return new Rectangle( 0, 0, p.x, p.y );
  }

  private Rectangle getUpSize( GC gc ) {
    Point p = gc.textExtent( STRING_UP );
    return new Rectangle( size_not.x + size_not.width + 40, size_not.y, p.x + 20, size_not.height );
  }

  private Rectangle getAddSize( GC gc ) {
    Rectangle is = imageAdd.getBounds(); // image size
    Rectangle cs = getBounds(); // Canvas size

    return new Rectangle( cs.width - is.width - 5 - X_PADDING, 5, is.width, is.height );
  }

  private void drawNegated( GC gc, int x, int y, Condition condition ) {
    Color color = gc.getForeground();

    if ( hover_not ) {
      gc.setBackground( gray );
    }
    gc.fillRectangle( Real2Screen( size_not ) );
    gc.drawRectangle( Real2Screen( size_not ) );

    if ( condition.isNegated() ) {
      if ( hover_not ) {
        gc.setForeground( green );
      }
      gc.drawText( STRING_NOT, size_not.x + 5 + offsetx, size_not.y + 2 + offsety, SWT.DRAW_TRANSPARENT );
      gc.drawText( STRING_NOT, size_not.x + 6 + offsetx, size_not.y + 2 + offsety, SWT.DRAW_TRANSPARENT );
      if ( hover_not ) {
        gc.setForeground( color );
      }
    } else {
      if ( hover_not ) {
        gc.setForeground( red );
        gc.drawText( STRING_NOT, size_not.x + 5 + offsetx, size_not.y + 2 + offsety, SWT.DRAW_TRANSPARENT );
        gc.drawText( STRING_NOT, size_not.x + 6 + offsetx, size_not.y + 2 + offsety, SWT.DRAW_TRANSPARENT );
        gc.setForeground( color );
      }
    }

    if ( hover_not ) {
      gc.setBackground( bg );
    }
  }

  private void drawAtomic( GC gc, int x, int y, Condition condition ) {

    // First the text sizes...
    String left = Const.rightPad( condition.getLeftValuename(), max_field_length );
    Point ext_left = gc.textExtent( left );
    if ( condition.getLeftValuename() == null ) {
      ext_left = gc.textExtent( "<field>" );
    }

    String fn_max = Condition.functions[Condition.FUNC_NOT_NULL];
    String fn = condition.getFunctionDesc();
    Point ext_fn = gc.textExtent( fn_max );

    String rightval = Const.rightPad( condition.getRightValuename(), max_field_length );
    Point ext_rval = gc.textExtent( rightval );
    if ( condition.getLeftValuename() == null ) {
      ext_rval = gc.textExtent( "<field>" );
    }

    String rightex = condition.getRightExactString();

    String rightex_max = rightex;
    if ( rightex == null ) {
      rightex_max = Const.rightPad( " ", 10 );
    } else {
      if ( rightex.length() < 10 ) {
        rightex_max = Const.rightPad( rightex, 10 );
      }
    }

    Point ext_rex = gc.textExtent( rightex_max );

    size_left = new Rectangle( x + 5, y + size_not.height + 5, ext_left.x + 5, ext_left.y + 5 );

    size_fn =
      new Rectangle( size_left.x + size_left.width + 15, y + size_not.height + 5, ext_fn.x + 5, ext_fn.y + 5 );

    size_rightval =
      new Rectangle( size_fn.x + size_fn.width + 15, y + size_not.height + 5, ext_rval.x + 5, ext_rval.y + 5 );

    size_rightex =
      new Rectangle(
        size_fn.x + size_fn.width + 15, y + size_not.height + 5 + size_rightval.height + 5, ext_rex.x + 5,
        ext_rex.y + 5 );

    if ( hover_left ) {
      gc.setBackground( gray );
    }
    gc.fillRectangle( Real2Screen( size_left ) );
    gc.drawRectangle( Real2Screen( size_left ) );
    gc.setBackground( bg );

    if ( hover_fn ) {
      gc.setBackground( gray );
    }
    gc.fillRectangle( Real2Screen( size_fn ) );
    gc.drawRectangle( Real2Screen( size_fn ) );
    gc.setBackground( bg );

    if ( hover_rightval ) {
      gc.setBackground( gray );
    }
    gc.fillRectangle( Real2Screen( size_rightval ) );
    gc.drawRectangle( Real2Screen( size_rightval ) );
    gc.setBackground( bg );

    if ( hover_rightex ) {
      gc.setBackground( gray );
    }
    gc.fillRectangle( Real2Screen( size_rightex ) );
    gc.drawRectangle( Real2Screen( size_rightex ) );
    gc.setBackground( bg );

    if ( condition.getLeftValuename() != null ) {
      gc.drawText( left, size_left.x + 1 + offsetx, size_left.y + 1 + offsety, SWT.DRAW_TRANSPARENT );
    } else {
      gc.setForeground( gray );
      gc.drawText( "<field>", size_left.x + 1 + offsetx, size_left.y + 1 + offsety, SWT.DRAW_TRANSPARENT );
      gc.setForeground( black );
    }

    gc.drawText( fn, size_fn.x + 1 + offsetx, size_fn.y + 1 + offsety, SWT.DRAW_TRANSPARENT );

    if ( condition.getFunction() != Condition.FUNC_NOT_NULL && condition.getFunction() != Condition.FUNC_NULL ) {
      String re = rightex == null ? "" : rightex;
      String stype = "";
      ValueMetaAndData v = condition.getRightExact();
      if ( v != null ) {
        stype = " (" + v.getValueMeta().getTypeDesc() + ")";
      }

      if ( condition.getRightValuename() != null ) {
        gc.drawText( rightval, size_rightval.x + 1 + offsetx, size_rightval.y + 1 + offsety, SWT.DRAW_TRANSPARENT );
      } else {
        String nothing = rightex == null ? "<field>" : "";
        gc.setForeground( gray );
        gc.drawText( nothing, size_rightval.x + 1 + offsetx, size_rightval.y + 1 + offsety, SWT.DRAW_TRANSPARENT );
        if ( condition.getRightValuename() == null ) {
          gc.setForeground( black );
        }
      }

      if ( rightex != null ) {
        gc.drawText( re, size_rightex.x + 1 + offsetx, size_rightex.y + 1 + offsety, SWT.DRAW_TRANSPARENT );
      } else {
        String nothing = condition.getRightValuename() == null ? "<value>" : "";
        gc.setForeground( gray );
        gc.drawText( nothing, size_rightex.x + 1 + offsetx, size_rightex.y + 1 + offsety, SWT.DRAW_TRANSPARENT );
        gc.setForeground( black );
      }

      gc.drawText(
        stype, size_rightex.x + 1 + size_rightex.width + 10 + offsetx, size_rightex.y + 1 + offsety,
        SWT.DRAW_TRANSPARENT );
    } else {
      gc.drawText( "-", size_rightval.x + 1 + offsetx, size_rightval.y + 1 + offsety, SWT.DRAW_TRANSPARENT );
      gc.drawText( "-", size_rightex.x + 1 + offsetx, size_rightex.y + 1 + offsety, SWT.DRAW_TRANSPARENT );
    }
  }

  private Point drawCondition( GC gc, int x, int y, int nr, Condition condition ) {
    int opx, opy, opw, oph;
    int cx, cy, cw, ch;

    opx = x;
    opy = y;
    opw = size_and_not.width + 6;
    oph = size_and_not.height + 2;

    /*
     * First draw the operator ...
     */
    if ( nr > 0 ) {
      String operator = condition.getOperatorDesc();
      // Remember the size of the rectangle!
      size_oper[nr] = new Rectangle( opx, opy, opw, oph );
      if ( nr == hover_operator ) {
        gc.setBackground( gray );
        gc.fillRectangle( Real2Screen( size_oper[nr] ) );
        gc.drawRectangle( Real2Screen( size_oper[nr] ) );
        gc.setBackground( bg );
      }
      gc.drawText( operator, size_oper[nr].x + 2 + offsetx, size_oper[nr].y + 2 + offsety, SWT.DRAW_TRANSPARENT );
    }

    /*
     * Then draw the condition below, possibly negated!
     */
    String str = condition.toString( 0, true, false ); // don't show the operator!
    Point p = gc.textExtent( str );

    cx = opx + 23;
    cy = opy + oph + 10;
    cw = p.x + 5;
    ch = p.y + 5;

    // Remember the size of the rectangle!
    size_cond[nr] = new Rectangle( cx, cy, cw, ch );

    if ( nr == hover_condition ) {
      gc.setBackground( gray );
      gc.fillRectangle( Real2Screen( size_cond[nr] ) );
      gc.drawRectangle( Real2Screen( size_cond[nr] ) );
      gc.setBackground( bg );
    }
    gc.drawText( str, size_cond[nr].x + 2 + offsetx, size_cond[nr].y + 5 + offsety, SWT.DRAW_DELIMITER
      | SWT.DRAW_TRANSPARENT | SWT.DRAW_TAB | SWT.DRAW_MNEMONIC );

    p.x += 0;
    p.y += 5;

    return p;
  }

  public void drawUp( GC gc ) {
    if ( hover_up ) {
      gc.setBackground( gray );
      gc.fillRectangle( size_up );
    }
    gc.drawRectangle( size_up );
    gc.drawText( STRING_UP, size_up.x + 1 + offsetx, size_up.y + 1 + offsety, SWT.DRAW_TRANSPARENT );
  }

  public void drawMessage( GC gc ) {
    gc.setForeground( blue );
    gc.drawText(
      getMessageString(), size_up.x + size_up.width + offsetx + 40, size_up.y + 1 + offsety,
      SWT.DRAW_TRANSPARENT );
    // widget.setToolTipText(getMessageString());
  }

  private boolean isInNot( Point screen ) {
    if ( size_not == null ) {
      return false;
    }
    return Real2Screen( size_not ).contains( screen );
  }

  private boolean isInUp( Point screen ) {
    if ( size_up == null || parents.isEmpty() ) {
      return false; // not displayed!
    }

    return Real2Screen( size_up ).contains( screen );
  }

  private boolean isInAdd( Point screen ) {
    if ( size_add == null || screen == null ) {
      return false;
    }
    return size_add.contains( screen );
  }

  private boolean isInWidget( Point screen ) {
    if ( size_widget == null ) {
      return false;
    }

    return Real2Screen( size_widget ).contains( screen );
  }

  private int getNrSubcondition( Point screen ) {
    if ( size_cond == null ) {
      return -1;
    }

    for ( int i = 0; i < size_cond.length; i++ ) {
      if ( size_cond[i] != null && Screen2Real( size_cond[i] ).contains( screen ) ) {
        return i;
      }
    }
    return -1;
  }

  private boolean isInSubcondition( Point screen ) {
    return getNrSubcondition( screen ) >= 0;
  }

  private int getNrOperator( Point screen ) {
    if ( size_oper == null ) {
      return -1;
    }

    for ( int i = 0; i < size_oper.length; i++ ) {
      if ( size_oper[i] != null && Screen2Real( size_oper[i] ).contains( screen ) ) {
        return i;
      }
    }
    return -1;
  }

  private boolean isInOperator( Point screen ) {
    return getNrOperator( screen ) >= 0;
  }

  private boolean isInLeft( Point screen ) {
    if ( size_left == null ) {
      return false;
    }
    return Real2Screen( size_left ).contains( screen );
  }

  private boolean isInFunction( Point screen ) {
    if ( size_fn == null ) {
      return false;
    }
    return Real2Screen( size_fn ).contains( screen );
  }

  private boolean isInRightValue( Point screen ) {
    if ( size_rightval == null ) {
      return false;
    }
    return Real2Screen( size_rightval ).contains( screen );
  }

  private boolean isInRightExact( Point screen ) {
    if ( size_rightex == null ) {
      return false;
    }
    return Real2Screen( size_rightex ).contains( screen );
  }

  private int getAreaCode( Point screen ) {
    if ( isInNot( screen ) ) {
      return AREA_NOT;
    }
    if ( isInUp( screen ) ) {
      return AREA_UP;
    }
    if ( isInAdd( screen ) ) {
      return AREA_ICON_ADD;
    }

    if ( active_condition.isAtomic() ) {
      if ( isInLeft( screen ) ) {
        return AREA_LEFT;
      }
      if ( isInFunction( screen ) ) {
        return AREA_FUNCTION;
      }
      if ( isInRightExact( screen ) ) {
        return AREA_RIGHT_EXACT;
      }
      if ( isInRightValue( screen ) ) {
        return AREA_RIGHT_VALUE;
      }
    } else {
      if ( isInSubcondition( screen ) ) {
        return AREA_SUBCONDITION;
      }
      if ( isInOperator( screen ) ) {
        return AREA_OPERATOR;
      }
    }

    if ( isInWidget( screen ) ) {
      return AREA_BACKGROUND;
    }

    return AREA_NONE;
  }

  /**
   * Edit the condition in a separate dialog box...
   *
   * @param condition
   *          The condition to be edited
   */
  private void editCondition( int nr ) {
    if ( active_condition.isComposite() ) {
      parents.add( active_condition );
      active_condition = active_condition.getCondition( nr );
    }
  }

  private void addCondition() {
    Condition c = new Condition();
    c.setOperator( Condition.OPERATOR_AND );

    addCondition( c );
    setModified();

    widget.redraw();
  }

  /**
   * Add a sub-condition to the active condition...
   *
   * @param condition
   *          The condition to which we want to add one more.
   */
  private void addCondition( Condition condition ) {
    active_condition.addCondition( condition );
  }

  /**
   * Remove a sub-condition from the active condition...
   *
   * @param condition
   *          The condition to which we want to add one more.
   */
  private void removeCondition( int nr ) {
    active_condition.removeCondition( nr );
  }

  /**
   * @param messageString
   *          The messageString to set.
   */
  public void setMessageString( String messageString ) {
    this.messageString = messageString;
  }

  /**
   * @return Returns the messageString.
   */
  public String getMessageString() {
    return messageString;
  }

  private Rectangle Real2Screen( Rectangle r ) {
    return new Rectangle( r.x + offsetx, r.y + offsety, r.width, r.height );
  }

  private Rectangle Screen2Real( Rectangle r ) {
    return new Rectangle( r.x - offsetx, r.y - offsety, r.width, r.height );
  }

  /**
   * Determine the maximum rectangle of used canvas space...
   */
  private void getMaxSize() {
    // Top line...
    maxdrawn = size_not.union( size_up );

    // Atomic
    if ( active_condition.isAtomic() ) {
      maxdrawn = maxdrawn.union( size_left );
      maxdrawn = maxdrawn.union( size_fn );
      maxdrawn = maxdrawn.union( size_rightval );
      maxdrawn = maxdrawn.union( size_rightex );
      maxdrawn.width += 100;
    } else {
      if ( size_cond != null ) {
        for ( int i = 0; i < size_cond.length; i++ ) {
          if ( size_cond[i] != null ) {
            maxdrawn = maxdrawn.union( size_cond[i] );
          }
        }
      }
      if ( size_oper != null ) {
        for ( int i = 0; i < size_oper.length; i++ ) {
          if ( size_oper[i] != null ) {
            maxdrawn = maxdrawn.union( size_oper[i] );
          }
        }
      }
    }

    maxdrawn.width += 10;
    maxdrawn.height += 10;
  }

  private void setBars() {
    if ( size_widget == null || maxdrawn == null ) {
      return;
    }

    // Horizontal scrollbar behavior
    //
    if ( size_widget.width > maxdrawn.width ) {
      offsetx = 0;
      sbHorizontal.setSelection( 0 );
      sbHorizontal.setVisible( false );
    } else {
      offsetx = -sbHorizontal.getSelection();
      sbHorizontal.setVisible( true );
      // Set the bar's parameters...
      sbHorizontal.setMaximum( maxdrawn.width );
      sbHorizontal.setMinimum( 0 );
      sbHorizontal.setPageIncrement( size_widget.width );
      sbHorizontal.setIncrement( 10 );
    }

    // Vertical scrollbar behavior
    //
    if ( size_widget.height > maxdrawn.height ) {
      offsety = 0;
      sbVertical.setSelection( 0 );
      sbVertical.setVisible( false );
    } else {
      offsety = sbVertical.getSelection();
      sbVertical.setVisible( true );
      // Set the bar's parameters...
      sbVertical.setMaximum( maxdrawn.height );
      sbVertical.setMinimum( 0 );
      sbVertical.setPageIncrement( size_widget.height );
      sbVertical.setIncrement( 10 );
    }
  }

  public void addModifyListener( ModifyListener lsMod ) {
    modListeners.add( lsMod );
  }

  public void setModified() {
    for ( int i = 0; i < modListeners.size(); i++ ) {
      ModifyListener lsMod = modListeners.get( i );
      if ( lsMod != null ) {
        Event e = new Event();
        e.widget = this;
        lsMod.modifyText( new ModifyEvent( e ) );
      }
    }
  }
}
