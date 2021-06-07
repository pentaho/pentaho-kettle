/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.custom;


/**
 * This class provides access to the public constants provided by <code>StyledText</code>.
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class ST {

  /*
   *  Navigation Key Actions. Key bindings for the actions are set
   *  by the StyledText widget.
   */
  public static final int LINE_UP = 16777217; 			// binding = SWT.ARROW_UP
  public static final int LINE_DOWN = 16777218; 		// binding = SWT.ARROW_DOWN
  public static final int LINE_START = 16777223; 		// binding = SWT.HOME
  public static final int LINE_END = 16777224; 		// binding = SWT.END
  public static final int COLUMN_PREVIOUS = 16777219; 	// binding = SWT.ARROW_LEFT
  public static final int COLUMN_NEXT = 16777220; 		// binding = SWT.ARROW_RIGHT
  public static final int PAGE_UP = 16777221; 			// binding = SWT.PAGE_UP
  public static final int PAGE_DOWN = 16777222; 		// binding = SWT.PAGE_DOWN
  public static final int WORD_PREVIOUS = 17039363;	// binding = SWT.MOD1 + SWT.ARROW_LEFT
  public static final int WORD_NEXT = 17039364; 		// binding = SWT.MOD1 + SWT.ARROW_RIGHT
  public static final int TEXT_START = 17039367; 		// binding = SWT.MOD1 + SWT.HOME
  public static final int TEXT_END = 17039368; 		// binding = SWT.MOD1 + SWT.END
  public static final int WINDOW_START = 17039365; 	// binding = SWT.MOD1 + SWT.PAGE_UP
  public static final int WINDOW_END = 17039366; 		// binding = SWT.MOD1 + SWT.PAGE_DOWN

  /*
   * Selection Key Actions
   */
  public static final int SELECT_ALL = 262209; 				// binding = SWT.MOD1 + 'A'
  public static final int SELECT_LINE_UP = 16908289; 			// binding = SWT.MOD2 + SWT.ARROW_UP
  public static final int SELECT_LINE_DOWN = 16908290; 		// binding = SWT.MOD2 + SWT.ARROW_DOWN
  public static final int SELECT_LINE_START = 16908295; 		// binding = SWT.MOD2 + SWT.HOME
  public static final int SELECT_LINE_END = 16908296; 			// binding = SWT.MOD2 + SWT.END
  public static final int SELECT_COLUMN_PREVIOUS = 16908291;	// binding = SWT.MOD2 + SWT.ARROW_LEFT
  public static final int SELECT_COLUMN_NEXT = 16908292; 		// binding = SWT.MOD2 + SWT.ARROW_RIGHT
  public static final int SELECT_PAGE_UP = 16908293; 			// binding = SWT.MOD2 + SWT.PAGE_UP
  public static final int SELECT_PAGE_DOWN = 16908294; 		// binding = SWT.MOD2 + SWT.PAGE_DOWN
  public static final int SELECT_WORD_PREVIOUS = 17170435;		// binding = SWT.MOD1 + SWT.MOD2 + SWT.ARROW_LEFT
  public static final int SELECT_WORD_NEXT = 17170436; 		// binding = SWT.MOD1 + SWT.MOD2 + SWT.ARROW_RIGHT
  public static final int SELECT_TEXT_START = 17170439; 		// binding = SWT.MOD1 + SWT.MOD2 + SWT.HOME
  public static final int SELECT_TEXT_END = 17170440; 			// binding = SWT.MOD1 + SWT.MOD2 + SWT.END
  public static final int SELECT_WINDOW_START = 17170437; 		// binding = SWT.MOD1 + SWT.MOD2 + SWT.PAGE_UP
  public static final int SELECT_WINDOW_END = 17170438; 		// binding = SWT.MOD1 + SWT.MOD2 + SWT.PAGE_DOWN

  /*
   *  Modification Key Actions
   */
  public static final int CUT = 131199; 			// binding = SWT.MOD2 + SWT.DEL
  public static final int COPY = 17039369; 		// binding = SWT.MOD1 + SWT.INSERT;
  public static final int PASTE = 16908297;		// binding = SWT.MOD2 + SWT.INSERT ;
  public static final int DELETE_PREVIOUS = '\b'; 	// binding = SWT.BS;
  public static final int DELETE_NEXT = 0x7F; 		// binding = SWT.DEL;
  public static final int DELETE_WORD_PREVIOUS = 262152;	// binding = SWT.BS | SWT.MOD1;
  public static final int DELETE_WORD_NEXT = 262271;	// binding = SWT.DEL | SWT.MOD1;

  /*
   * Miscellaneous Key Actions
   */
  public static final int TOGGLE_OVERWRITE = 16777225; // binding = SWT.INSERT;

  /**
   * TEMPORARY CODE - API SUBJECT TO CHANGE
   *
   * Toggle block selection mode
   *
   * @since 3.5
   */
  public static final int TOGGLE_BLOCKSELECTION = 16777226;

  /**
   *  Bullet style dot.
   *
   *  @see Bullet
   *
   *  @since 3.2
   */
  public static final int BULLET_DOT = 1 << 0;

  /**
   *  Bullet style number.
   *
   *  @see Bullet
   *
   *  @since 3.2
   */
  public static final int BULLET_NUMBER = 1 << 1;

  /**
   *  Bullet style lower case letter.
   *
   *  @see Bullet
   *
   *  @since 3.2
   */
  public static final int BULLET_LETTER_LOWER = 1 << 2;

  /**
   *  Bullet style upper case letter.
   *
   *  @see Bullet
   *
   *  @since 3.2
   */
  public static final int BULLET_LETTER_UPPER = 1 << 3;

  /**
   *  Bullet style text.
   *
   *  @see Bullet
   *
   *  @since 3.2
   */
  public static final int BULLET_TEXT = 1 << 4;

  /**
   *  Bullet style custom draw.
   *  @since 3.2
   */
  public static final int BULLET_CUSTOM = 1 << 5;

  /**
   *  The ExtendedModify event type (value is 3000).
   *
   *  @since 3.8
   */
  public static final int ExtendedModify = 3000;

  /**
   *  The LineGetBackground event type (value is 3001).
   *
   *  @since 3.8
   */
  public static final int LineGetBackground = 3001;

  /**
   *  The LineGetStyle event type (value is 3002).
   *
   *  @since 3.8
   */
  public static final int LineGetStyle = 3002;

  /**
   *  The TextChanging event type (value is 3003).
   *
   *  @since 3.8
   */
  public static final int TextChanging = 3003;

  /**
   *  The TextSet event type (value is 3004).
   *
   *  @since 3.8
   */
  public static final int TextSet = 3004;

  /**
   *  The VerifyKey event type (value is 3005).
   *
   *  @since 3.8
   */
  public static final int VerifyKey = 3005;

  /**
   *  The TextChanged event type (value is 3006).
   *
   *  @since 3.8
   */
  public static final int TextChanged = 3006;

  /**
   *  The LineGetSegments event type (value is 3007).
   *
   *  @since 3.8
   */
  public static final int LineGetSegments = 3007;

  /**
   *  The PaintObject event type (value is 3008).
   *
   *  @since 3.8
   */
  public static final int PaintObject = 3008;

  /**
   *  The WordNext event type (value is 3009).
   *
   *  @since 3.8
   */
  public static final int WordNext = 3009;

  /**
   *  The WordPrevious event type (value is 3010).
   *
   *  @since 3.8
   */
  public static final int WordPrevious = 3010;

  /**
   *  The CaretMoved event type (value is 3011).
   *
   *  @since 3.8
   */
  public static final int CaretMoved = 3011;

}
