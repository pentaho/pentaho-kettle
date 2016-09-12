/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

/**
 *
 */

package org.pentaho.libformula.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.libformula.editor.FormulaEvaluator;
import org.pentaho.libformula.editor.FormulaMessage;
import org.pentaho.libformula.editor.function.FunctionDescription;
import org.pentaho.libformula.editor.function.FunctionLib;
import org.pentaho.libformula.editor.util.CompletionProposal;
import org.pentaho.libformula.editor.util.PositionAndLength;
import org.pentaho.reporting.libraries.formula.lvalues.ParsePosition;

/**
 * @author matt
 *
 */
public class LibFormulaEditor extends Dialog implements KeyListener {
  public static final String FUNCTIONS_FILE = "functions.xml";

  private Shell shell;
  private Tree tree;
  // private TreeEditor treeEditor;
  private SashForm sashForm;
  private StyledText expressionEditor;
  private String formula;
  private Browser message;

  private Button ok, cancel;
  private String[] inputFields;

  private Color blue;
  private Color red;
  private Color green;
  private Color white;
  private Color gray;
  private Color black;

  Menu helperMenu;

  private FunctionLib functionLib;
  private String[] functions;
  private String[] categories;

  private SashForm rightSash;

  private FormulaEvaluator evaluator;

  public LibFormulaEditor( Shell parent, int style, String formula, String[] inputFields ) throws Exception {
    super( parent, style );
    this.formula = formula;
    this.inputFields = inputFields;

    // Run it in a new shell:
    //
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );

    // The layout...
    //
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 5;
    formLayout.marginHeight = 5;
    shell.setLayout( formLayout );

    // At the bottom we have a few buttons...
    //
    Composite buttonsComposite = new Composite( shell, SWT.NONE );
    FillLayout bcLayout = new FillLayout();
    bcLayout.spacing = 5;
    buttonsComposite.setLayout( bcLayout );
    ok = new Button( buttonsComposite, SWT.PUSH );
    ok.setText( "  OK  " ); // TODO i18n
    cancel = new Button( buttonsComposite, SWT.PUSH );
    cancel.setText( " Cancel " ); // TODO i18n

    ok.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        ok();
      }
    } );
    cancel.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        cancel();
      }
    } );

    // A tree on the left, an editor on the right: put them in a sash form...
    // Right below the editor we display the error messages...
    //
    sashForm = new SashForm( shell, SWT.HORIZONTAL );
    sashForm.setLayout( new FillLayout() );

    FormData fdSashForm = new FormData();
    fdSashForm.left = new FormAttachment( 0, 0 );
    fdSashForm.right = new FormAttachment( 100, 0 );
    fdSashForm.top = new FormAttachment( 0, 10 );
    fdSashForm.bottom = new FormAttachment( buttonsComposite, -10 );
    sashForm.setLayoutData( fdSashForm );

    FormData fdBC = new FormData();
    fdBC.left = new FormAttachment( sashForm, 0, SWT.CENTER );
    fdBC.bottom = new FormAttachment( 100, 0 );
    buttonsComposite.setLayoutData( fdBC );

    // Read the function descriptions...
    //
    readFunctions();

    evaluator = new FormulaEvaluator( functions, inputFields );

    // A tree on the left:
    //
    tree = new Tree( sashForm, SWT.SINGLE );
    for ( int i = 0; i < categories.length; i++ ) {
      String category = categories[i];
      String i18nCategory = category;
      // Look up the category in i18n if needed.
      if ( category.startsWith( "%" ) ) {
        i18nCategory = BaseMessages.getString( FunctionLib.class, category.substring( 1 ) ); // skip the %
      }

      TreeItem item = new TreeItem( tree, SWT.NONE );
      item.setText( i18nCategory );

      String[] fnames = functionLib.getFunctionsForACategory( category );
      for ( String fname : fnames ) {
        TreeItem fitem = new TreeItem( item, SWT.NONE );
        fitem.setText( fname );
      }
    }
    /**
     * If someone clicks on a function, we display the description of the function in the message box...
     */
    tree.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent event ) {
        if ( tree.getSelectionCount() == 1 ) {
          TreeItem item = tree.getSelection()[0];
          if ( item.getParentItem() != null ) { // has a category above it
            String functionName = item.getText();
            FunctionDescription functionDescription = functionLib.getFunctionDescription( functionName );
            if ( functionDescription != null ) {
              String report = functionDescription.getHtmlReport();
              message.setText( report );
            }
          }
        }
      }

    } );

    rightSash = new SashForm( sashForm, SWT.VERTICAL );

    // An expression editor on the right
    //
    expressionEditor = new StyledText( rightSash, SWT.NONE );
    expressionEditor.setText( this.formula );
    expressionEditor.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent event ) {
        setStyles();
      }
    } );
    expressionEditor.addKeyListener( this );

    // Some information concerning the validity of the formula expression
    //
    message = new Browser( rightSash, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL );
    FormData fdMessage = new FormData();
    fdMessage.left = new FormAttachment( 0, 0 );
    fdMessage.right = new FormAttachment( 100, 0 );
    fdMessage.top = new FormAttachment( 0, 0 );
    fdMessage.bottom = new FormAttachment( 0, 100 );
    message.setLayoutData( fdMessage );

    rightSash.setWeights( new int[] { 10, 80, } );

    sashForm.setWeights( new int[] { 15, 85, } );

    red = new Color( shell.getDisplay(), 255, 0, 0 );
    green = new Color( shell.getDisplay(), 0, 220, 0 );
    blue = new Color( shell.getDisplay(), 0, 0, 255 );
    white = new Color( shell.getDisplay(), 255, 255, 255 );
    gray = new Color( shell.getDisplay(), 150, 150, 150 );
    black = new Color( shell.getDisplay(), 0, 0, 0 );

    setStyles();

    shell.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent arg0 ) {
        red.dispose();
        green.dispose();
        blue.dispose();
        white.dispose();
        gray.dispose();
        black.dispose();
      }
    } );
  }

  public String open() {
    shell.layout();
    shell.open();

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    while ( !shell.isDisposed() ) {
      if ( !shell.getDisplay().readAndDispatch() ) {
        shell.getDisplay().sleep();
      }
    }
    return formula;
  }

  public void ok() {
    formula = expressionEditor.getText();
    shell.dispose();
  }

  public void cancel() {
    formula = null;
    shell.dispose();
  }

  public void readFunctions() throws Exception {
    // URL url =
    // this.getClass().getResource("/"+FunctionLib.class.getPackage().getName().replace(".","/")+"/"+FUNCTIONS_FILE);
    functionLib = new FunctionLib( FUNCTIONS_FILE );
    functions = functionLib.getFunctionNames();
    categories = functionLib.getFunctionCategories();
  }

  public void setStyles() {

    String expression = expressionEditor.getText();
    int expressionLength = expression.length();
    Map<String, FormulaMessage> messages = evaluator.evaluateFormula( expression );

    // We need to provide an array of styles for this event.
    //
    Vector<StyleRange> styles = new Vector<StyleRange>();
    StringBuilder report = new StringBuilder();

    for ( FormulaMessage message : messages.values() ) {
      ParsePosition position = message.getPosition();

      PositionAndLength positionAndLength = PositionAndLength.calculatePositionAndLength( expression, position );

      int pos = positionAndLength.getPosition();
      int length = positionAndLength.getLength();

      if ( pos < expressionLength ) {
        switch ( message.getType() ) {
          case FormulaMessage.TYPE_ERROR:
            report.append( message.toString() ).append( Const.CR );

            StyleRange styleRangeRed = new StyleRange( pos, length, red, null, SWT.BOLD );
            styleRangeRed.underline = true;
            styles.add( styleRangeRed );

            break;

          case FormulaMessage.TYPE_FUNCTION:
            styles.add( new StyleRange( pos, length, black, null, SWT.BOLD ) );
            break;

          case FormulaMessage.TYPE_FIELD:
            // styles.add(new StyleRange(pos, length, green, null, SWT.BOLD )); // TODO : Not working for some reason.
            break;

          case FormulaMessage.TYPE_STATIC_NUMBER:
          case FormulaMessage.TYPE_STATIC_STRING:
          case FormulaMessage.TYPE_STATIC_DATE:
          case FormulaMessage.TYPE_STATIC_LOGICAL:
            styles.add( new StyleRange( pos, length, blue, gray, SWT.BOLD | SWT.ITALIC ) );
            break;
          default:
            break;
        }
      }
    }

    message.setText( report.toString() );

    // Now set the styled ranges...
    //
    // Sort the styles first...
    //
    Collections.sort( styles, new Comparator<StyleRange>() {
      public int compare( StyleRange o1, StyleRange o2 ) {
        return o1.start - o2.start;
      }
    } );

    StyleRange[] styleRanges = new StyleRange[styles.size()];
    styles.copyInto( styleRanges );

    // expressionEditor.getStyledText().replaceStyleRanges(0, expression.length(), new StyleRange[] { styles.get(0), });
    expressionEditor.setStyleRanges( styleRanges );
  }

  public static void main( String[] args ) throws Exception {
    Display display = new Display();
    String[] inputFields = { "firstname", "name", };
    LibFormulaEditor lbe =
      new LibFormulaEditor(
        new Shell( display, SWT.NONE ), SWT.NONE, "MID(UPPER([name] & \" \" & [firstname]);5;10)", inputFields );
    lbe.open();
  }

  public void keyPressed( KeyEvent e ) {
    boolean ctrl = ( ( e.stateMask & SWT.CONTROL ) != 0 );
    // boolean alt = ((e.stateMask & SWT.ALT) != 0);

    List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

    // CTRL-SPACE?
    //
    if ( ctrl && e.character == ' ' ) {
      // Gab the content before the cursor position...
      //
      StringBuilder beforeBuffer = new StringBuilder();
      String editor = expressionEditor.getText();
      int pos = expressionEditor.getCaretOffset() - 1;
      while ( pos >= 0 && pos < editor.length() ) {
        char c = editor.charAt( pos );
        if ( Character.isWhitespace( c ) ) {
          break;
        }
        if ( Character.isLetterOrDigit( c ) || c == '[' ) {
          beforeBuffer.insert( 0, c );
          pos--;
        } else {
          break;
        }
      }

      String before = beforeBuffer.toString();
      System.out.println( "BEFORE = " + before );

      // if we just have [ we display only the field names...
      //
      if ( before.equals( "[" ) ) {
        for ( String fieldName : inputFields ) {
          proposals.add( new CompletionProposal( "[" + fieldName + "] (input field)", fieldName + "]", fieldName
            .length() + 1 ) );
        }
      } else if ( Utils.isEmpty( before ) ) {
        for ( String fieldName : inputFields ) {
          proposals.add( new CompletionProposal(
            "[" + fieldName + "] (input field)", "[" + fieldName + "]", fieldName.length() + 2 ) );
        }
      } else {
        // Only add those where "before" matches the start of the keyword or function
        //
        for ( String fieldName : inputFields ) {
          String key = "[" + fieldName;
          if ( key.startsWith( before ) && !key.equalsIgnoreCase( before ) ) {
            proposals.add( new CompletionProposal( "[" + fieldName + "] (keyword)", fieldName.substring( before
              .length() )
              + "]", fieldName.length() - before.length() + 1 ) );
          }
        }
        for ( String function : functions ) {
          if ( function.startsWith( before ) && !function.equalsIgnoreCase( before ) ) {
            proposals.add( new CompletionProposal( function + "() (Function)", function
              .substring( before.length() )
              + "()", function.length() - before.length() + 1 ) );
          }
        }
      }

      if ( helperMenu == null ) {
        helperMenu = new Menu( shell, SWT.POP_UP );
      } else {
        for ( MenuItem item : helperMenu.getItems() ) {
          item.dispose();
        }
      }
      // final int offset = expressionEditor.getCaretOffset();
      final int offset = expressionEditor.getCaretOffset();
      Point p = expressionEditor.getLocationAtOffset( offset );
      int h = expressionEditor.getLineHeight( offset );
      Point l = GUIResource.calculateControlPosition( expressionEditor );

      MenuItem first = null;
      if ( proposals.size() == 1 ) {
        MenuItem item = new MenuItem( helperMenu, SWT.NONE );
        if ( first == null ) {
          first = item;
        }
        final CompletionProposal proposal = proposals.get( 0 );
        item.setText( proposal.getMenuText() );
        item.addSelectionListener( new SelectionAdapter() {
          public void widgetSelected( SelectionEvent se ) {
            expressionEditor.insert( proposal.getCompletionString() );
            expressionEditor.setSelection( offset + proposal.getOffset() );
          }
        } );
        helperMenu.setLocation( l.x + p.x, l.y + p.y + h );
        helperMenu.setDefaultItem( first );
        helperMenu.setVisible( true );
      } else if ( proposals.size() > 0 ) {
        int nr = 0;
        for ( final CompletionProposal proposal : proposals ) {
          MenuItem item = new MenuItem( helperMenu, SWT.NONE );
          if ( first == null ) {
            first = item;
          }
          item.setText( proposal.getMenuText() );
          item.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent se ) {
              expressionEditor.insert( proposal.getCompletionString() );
              expressionEditor.setSelection( offset + proposal.getOffset() );
            }
          } );
          if ( nr++ > 5 ) {
            break;
          }
        }
        helperMenu.setLocation( l.x + p.x, l.y + p.y + h );
        helperMenu.setDefaultItem( first );
        helperMenu.setVisible( true );
      }
    }
  }

  public void keyReleased( KeyEvent arg0 ) {
    // TODO Auto-generated method stub

  }

}
