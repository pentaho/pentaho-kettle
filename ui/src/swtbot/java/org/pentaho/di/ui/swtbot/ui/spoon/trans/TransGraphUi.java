package org.pentaho.di.ui.swtbot.ui.spoon.trans;

import org.pentaho.di.core.gui.Point;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.di.ui.swtbot.ui.spoon.SpoonUi;

import java.lang.reflect.Constructor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * The transformation canvas ({@link TransGraph}) of the active editor tab.
 * <p>
 * Mouse gestures are sent to the canvas as SWT events, the way SWTBot clicks widgets. Drag and drop calls Spoon's own
 * drag listeners on the source tree and drop listeners on the canvas, in SWT's order, and skips only the
 * operating-system drag session: that needs OS-level mouse input, which on macOS requires the Accessibility
 * permission. Actions that can open a modal dialog run asynchronously, so the caller waits for their result.
 */
public final class TransGraphUi {
  private static final String DND_EVENT_CLASS = "org.eclipse.swt.dnd.DNDEvent";

  private final Spoon spoon;
  private final TransGraph graph;
  private final Canvas canvas;

  private TransGraphUi( Spoon spoon, TransGraph graph, Canvas canvas ) {
    this.spoon = spoon;
    this.graph = graph;
    this.canvas = canvas;
  }

  /**
   * The canvas of the active transformation tab.
   */
  public static TransGraphUi active( Spoon spoon ) {
    Result<TransGraphUi> active = () -> {
      TransGraph graph = spoon.getActiveTransGraph();
      if ( graph == null ) {
        throw new IllegalStateException( "no active transformation tab" );
      }
      Canvas canvas = findDropTargetCanvas( graph );
      if ( canvas == null ) {
        throw new IllegalStateException( "no drop-target canvas in the active transformation tab" );
      }
      return new TransGraphUi( spoon, graph, canvas );
    };
    return SpoonUi.onUiThread( spoon, active );
  }

  /**
   * The running or finished transformation, or null before the first run.
   */
  public Trans trans() {
    Result<Trans> trans = graph::getTrans;
    return SpoonUi.onUiThread( spoon, trans );
  }

  /**
   * Drags the selected {@code item} of a Design tab tree onto the canvas at {@code (x, y)} (canvas coordinates). Spoon
   * creates the step asynchronously; wait for it.
   */
  public void dropFrom( SWTBotTreeItem item, int x, int y ) {
    Result<Object> dragData = () -> {
      Widget tree = item.widget.getParent();
      DragSource source = (DragSource) tree.getData( DND.DRAG_SOURCE_KEY );
      if ( source == null ) {
        throw new IllegalStateException( "the tree of '" + item.widget.getText() + "' is not a drag source" );
      }
      DragSourceEvent start = dragSourceEvent( source );
      for ( DragSourceListener listener : source.getDragListeners() ) {
        listener.dragStart( start );
      }
      if ( !start.doit ) {
        throw new IllegalStateException( "Spoon refused to drag '" + item.widget.getText() + "'" );
      }
      DragSourceEvent setData = dragSourceEvent( source );
      for ( DragSourceListener listener : source.getDragListeners() ) {
        listener.dragSetData( setData );
      }
      return setData.data;
    };
    Object data = SpoonUi.onUiThread( spoon, dragData );
    if ( data == null ) {
      throw new IllegalStateException( "Spoon gave no drag data for '" + item.getText() + "'" );
    }
    UIThreadRunnable.asyncExec( spoon.getDisplay(), () -> {
      DropTarget target = (DropTarget) canvas.getData( DND.DROP_TARGET_KEY );
      org.eclipse.swt.graphics.Point display = canvas.toDisplay( x, y );
      DropTargetEvent over = dropTargetEvent( target, display, null );
      for ( DropTargetListener listener : target.getDropListeners() ) {
        listener.dragEnter( over );
        listener.dragOver( over );
      }
      DropTargetEvent drop = dropTargetEvent( target, display, data );
      for ( DropTargetListener listener : target.getDropListeners() ) {
        listener.dropAccept( drop );
        listener.drop( drop );
      }
    } );
  }

  /**
   * Holds Shift and drags from the {@code from} step icon to the {@code to} step icon, which creates a hop.
   */
  public void shiftDrag( String from, String to ) {
    // One UI-thread action, so a real mouse move over the canvas cannot clear the hop candidate mid-gesture.
    UIThreadRunnable.asyncExec( spoon.getDisplay(), () -> {
      redrawNow();
      org.eclipse.swt.graphics.Point start = stepCentre( from );
      org.eclipse.swt.graphics.Point end = stepCentre( to );
      mouse( SWT.MouseDown, start, 1, SWT.SHIFT );
      mouse( SWT.MouseMove, end, 0, SWT.SHIFT | SWT.BUTTON1 );
      redrawNow();
      mouse( SWT.MouseUp, end, 1, SWT.SHIFT | SWT.BUTTON1 );
    } );
  }

  /**
   * Double-clicks the {@code step} icon, which opens the step's dialog. Wait for the dialog shell.
   */
  public void doubleClickStep( String step ) {
    Result<org.eclipse.swt.graphics.Point> centre = () -> {
      redrawNow();
      return stepCentre( step );
    };
    org.eclipse.swt.graphics.Point point = SpoonUi.onUiThread( spoon, centre );
    UIThreadRunnable.asyncExec( spoon.getDisplay(), () -> {
      Event event = mouseEvent( point, 1, 0 );
      event.count = 2;
      canvas.notifyListeners( SWT.MouseDoubleClick, event );
    } );
  }

  /**
   * Text of the "Logging" tab in the Execution Results pane; empty before the first run.
   */
  public String loggingText() {
    Result<String> text = () -> {
      String log = graph.transLogDelegate.getLoggingText();
      return log == null ? "" : log;
    };
    return SpoonUi.onUiThread( spoon, text );
  }

  /** Canvas coordinates of the centre of the {@code step} icon. Call on the UI thread. */
  private org.eclipse.swt.graphics.Point stepCentre( String step ) {
    StepMeta stepMeta = graph.getTransMeta().findStep( step );
    if ( stepMeta == null ) {
      throw new IllegalStateException( "no step named '" + step + "' on the canvas" );
    }
    int half = PropsUI.getInstance().getIconSize() / 2;
    Point real = new Point( stepMeta.getLocation().x + half, stepMeta.getLocation().y + half );
    Point screen = graph.real2screen( real.x, real.y );
    Point check = graph.screen2real( screen.x, screen.y );
    if ( check.x != real.x || check.y != real.y ) {
      throw new IllegalStateException( "the canvas is zoomed; reset it to 100% before using TransGraphUi" );
    }
    return new org.eclipse.swt.graphics.Point( screen.x, screen.y );
  }

  /** Paints the canvas now, so the click areas of the step icons are up to date. Call on the UI thread. */
  private void redrawNow() {
    canvas.redraw();
    canvas.update();
  }

  private void mouse( int type, org.eclipse.swt.graphics.Point point, int button, int stateMask ) {
    canvas.notifyListeners( type, mouseEvent( point, button, stateMask ) );
  }

  private Event mouseEvent( org.eclipse.swt.graphics.Point point, int button, int stateMask ) {
    Event event = new Event();
    event.widget = canvas;
    event.display = canvas.getDisplay();
    event.x = point.x;
    event.y = point.y;
    event.button = button;
    event.stateMask = stateMask;
    event.count = 1;
    return event;
  }

  private static Canvas findDropTargetCanvas( Control control ) {
    if ( control instanceof Canvas && control.getData( DND.DROP_TARGET_KEY ) instanceof DropTarget ) {
      return (Canvas) control;
    }
    if ( control instanceof Composite ) {
      for ( Control child : ( (Composite) control ).getChildren() ) {
        Canvas found = findDropTargetCanvas( child );
        if ( found != null ) {
          return found;
        }
      }
    }
    return null;
  }

  private static DragSourceEvent dragSourceEvent( DragSource source ) {
    Event event = dndEvent( source );
    return newTypedEvent( DragSourceEvent.class, event );
  }

  private static DropTargetEvent dropTargetEvent( DropTarget target, org.eclipse.swt.graphics.Point display,
                                                  Object data ) {
    Event event = dndEvent( target );
    event.x = display.x;
    event.y = display.y;
    event.data = data;
    event.detail = DND.DROP_MOVE;
    return newTypedEvent( DropTargetEvent.class, event );
  }

  /**
   * SWT builds drag and drop events from the package-private {@code DNDEvent}; create one the same way.
   */
  private static Event dndEvent( Widget widget ) {
    try {
      Constructor<?> constructor = Class.forName( DND_EVENT_CLASS ).getDeclaredConstructor();
      constructor.setAccessible( true );
      Event event = (Event) constructor.newInstance();
      event.widget = widget;
      event.display = widget.getDisplay();
      event.doit = true;
      return event;
    } catch ( ReflectiveOperationException e ) {
      throw new IllegalStateException( "cannot create " + DND_EVENT_CLASS, e );
    }
  }

  private static <T> T newTypedEvent( Class<T> type, Event dndEvent ) {
    try {
      return type.getConstructor( Class.forName( DND_EVENT_CLASS ) ).newInstance( dndEvent );
    } catch ( ReflectiveOperationException e ) {
      throw new IllegalStateException( "cannot create " + type.getSimpleName(), e );
    }
  }
}
