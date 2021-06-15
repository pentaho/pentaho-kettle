/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.accessibility;


/**
 * Class ACC contains all the constants used in defining an
 * Accessible object.
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * 
 * @since 1.4
 */
public class ACC {
	public static final int STATE_NORMAL = 			0x00000000;
	public static final int STATE_SELECTED = 		0x00000002;
	public static final int STATE_SELECTABLE = 		0x00200000;
	public static final int STATE_MULTISELECTABLE = 0x01000000;
	public static final int STATE_FOCUSED = 		0x00000004;
	public static final int STATE_FOCUSABLE = 		0x00100000;
	public static final int STATE_PRESSED = 		0x00000008;
	public static final int STATE_CHECKED = 		0x00000010;
	public static final int STATE_EXPANDED = 		0x00000200;
	public static final int STATE_COLLAPSED = 		0x00000400;
	public static final int STATE_HOTTRACKED = 		0x00000080;
	public static final int STATE_BUSY = 			0x00000800;
	public static final int STATE_READONLY = 		0x00000040;
	public static final int STATE_INVISIBLE = 		0x00008000;
	public static final int STATE_OFFSCREEN = 		0x00010000;
	public static final int STATE_SIZEABLE = 		0x00020000;
	public static final int STATE_LINKED = 			0x00400000;
	public static final int STATE_DISABLED = 		0x00000001;
	public static final int STATE_ACTIVE = 			0x04000000;
	public static final int STATE_SINGLELINE = 		0x08000000;
	public static final int STATE_MULTILINE = 		0x10000000;
	public static final int STATE_REQUIRED = 		0x02000000;
	public static final int STATE_INVALID_ENTRY = 	0x20000000;
	public static final int STATE_SUPPORTS_AUTOCOMPLETION = 0x40000000;

	public static final int ROLE_CLIENT_AREA = 0xa;
	public static final int ROLE_WINDOW = 0x9;
	public static final int ROLE_MENUBAR = 0x2;
	public static final int ROLE_MENU = 0xb;
	public static final int ROLE_MENUITEM = 0xc;
	public static final int ROLE_SEPARATOR = 0x15;
	public static final int ROLE_TOOLTIP = 0xd;
	public static final int ROLE_SCROLLBAR = 0x3;
	public static final int ROLE_DIALOG = 0x12;
	public static final int ROLE_LABEL = 0x29;
	public static final int ROLE_PUSHBUTTON = 0x2b;
	public static final int ROLE_CHECKBUTTON = 0x2c;
	public static final int ROLE_RADIOBUTTON = 0x2d;
	public static final int ROLE_SPLITBUTTON = 0x3e;
	public static final int ROLE_COMBOBOX = 0x2e;
	public static final int ROLE_TEXT = 0x2a;
	public static final int ROLE_TOOLBAR = 0x16;
	public static final int ROLE_LIST = 0x21;
	public static final int ROLE_LISTITEM = 0x22;
	public static final int ROLE_TABLE = 0x18;
	public static final int ROLE_TABLECELL = 0x1d;
	public static final int ROLE_TABLECOLUMNHEADER = 0x19;
	public static final int ROLE_TABLEROWHEADER = 0x1a;
	public static final int ROLE_TREE = 0x23;
	public static final int ROLE_TREEITEM = 0x24;
	public static final int ROLE_TABFOLDER = 0x3c;
	public static final int ROLE_TABITEM = 0x25;
	public static final int ROLE_PROGRESSBAR = 0x30;
	public static final int ROLE_SLIDER = 0x33;
	public static final int ROLE_LINK = 0x1e;
	public static final int ROLE_ALERT = 0x08;
	public static final int ROLE_ANIMATION = 0x36;
	public static final int ROLE_CANVAS = 0x401;
	public static final int ROLE_COLUMN = 0x1b;
	public static final int ROLE_DOCUMENT = 0x0f;
	public static final int ROLE_GRAPHIC = 0x28;
	public static final int ROLE_GROUP = 0x14;
	public static final int ROLE_ROW = 0x1c;
	public static final int ROLE_SPINBUTTON = 0x34;
	public static final int ROLE_STATUSBAR = 0x17;
	public static final int ROLE_CHECKMENUITEM = 0x403;
	public static final int ROLE_RADIOMENUITEM = 0x431;
	public static final int ROLE_CLOCK = 0x3d;
	public static final int ROLE_CALENDAR = 0x2f;
	public static final int ROLE_DATETIME = 0x405;
	public static final int ROLE_FOOTER = 0x40E;
	public static final int ROLE_FORM = 0x410;
	public static final int ROLE_HEADER = 0x413;
	public static final int ROLE_HEADING = 0x414;
	public static final int ROLE_PAGE = 0x41D;
	public static final int ROLE_PARAGRAPH = 0x41E;
	public static final int ROLE_SECTION = 0x424;

	public static final int CHILDID_SELF = -1;
	public static final int CHILDID_NONE = -2;
	public static final int CHILDID_MULTIPLE = -3;

	/**
	 * An AT is requesting the accessible child object at the specified index.
	 * 
	 * @see AccessibleControlListener#getChild
	 * 
	 */
	public static final int CHILDID_CHILD_AT_INDEX = -4;
	
	/**
	 * An AT is requesting the index of this accessible in its parent.
	 * 
	 * @see AccessibleControlListener#getChild
	 * 
	 */
	public static final int CHILDID_CHILD_INDEX = -5;
	
	/**
	 * A detail constant indicating visible accessible objects.
	 * 
	 */
	public static final int VISIBLE = 0x01;
	
	/**
	 * A type constant specifying that insertion occurred.
	 * 
	 */
	public static final int INSERT = 0;

	/**
	 * A type constant specifying that deletion occurred.
	 * 
	 */
	public static final int DELETE = 1;
	
	public static final int TEXT_INSERT = INSERT;
	public static final int TEXT_DELETE = DELETE;

	/**
	 * A constant specifying that an operation succeeded.
	 * 
	 */
	public static final String OK = "OK"; //$NON-NLS-1$

	/**
	 * Typically, a single character is returned. In some cases more than one
	 * character is returned, for example, when a document contains field data
	 * such as a field containing a date, time, or footnote reference. In this
	 * case the caret can move over several characters in one movement of the
	 * caret. Note that after the caret moves, the caret offset changes by the
	 * number of characters in the field, e.g. by 8 characters in the following
	 * date: 03/26/07.
	 * 
	 */
	public static final int TEXT_BOUNDARY_CHAR = 0;
	
	/**
	 * The range provided matches the range observed when the application
	 * processes the Ctrl + left arrow and Ctrl + right arrow key sequences.
	 * Typically this is from the start of one word to the start of the next,
	 * but various applications are inconsistent in the handling of the end of a
	 * line.
	 * 
	 */
	public static final int TEXT_BOUNDARY_WORD = 1;
	
	/**
	 * Range is from start of one sentence to the start of another sentence.
	 * 
	 */
	public static final int TEXT_BOUNDARY_SENTENCE = 2;

	/**
	 * Range is from start of one paragraph to the start of another paragraph.
	 * 
	 */
	public static final int TEXT_BOUNDARY_PARAGRAPH = 3;

	/**
	 * Range is from start of one line to the start of another line. This often
	 * means that an end-of-line character will appear at the end of the range.
	 * However in the case of some applications an end-of-line character
	 * indicates the end of a paragraph and the lines composing the paragraph,
	 * other than the last line, do not contain an end of line character.
	 * 
	 */
	public static final int TEXT_BOUNDARY_LINE = 4;

	/**
	 * Using this value will cause all text to be returned.
	 * 
	 */
	public static final int TEXT_BOUNDARY_ALL = 5;

	/**
	 * Scroll the top left corner of the object or substring such that the top
	 * left corner (and as much as possible of the rest of the object or
	 * substring) is within the top level window. In cases where the entire
	 * object or substring fits within the top level window, the placement of
	 * the object or substring is dependent on the application. For example, the
	 * object or substring may be scrolled to the closest edge, the furthest
	 * edge, or midway between those two edges. In cases where there is a
	 * hierarchy of nested scrollable controls, more than one control may have
	 * to be scrolled.
	 * 
	 */
	public static final int SCROLL_TYPE_TOP_LEFT = 0;

	/**
	 * Scroll the bottom right corner of the object or substring such that the
	 * bottom right corner (and as much as possible of the rest of the object or
	 * substring) is within the top level window. In cases where the entire
	 * object or substring fits within the top level window, the placement of
	 * the object or substring is dependent on the application. For example, the
	 * object or substring may be scrolled to the closest edge, the furthest
	 * edge, or midway between those two edges. In cases where there is a
	 * hierarchy of nested scrollable controls, more than one control may have
	 * to be scrolled.
	 * 
	 */
	public static final int SCROLL_TYPE_BOTTOM_RIGHT = 1;

	/**
	 * Scroll the top edge of the object or substring such that the top edge
	 * (and as much as possible of the rest of the object or substring) is
	 * within the top level window. In cases where the entire object or substring
	 * fits within the top level window, the placement of the object or
	 * substring is dependent on the application. For example, the object or
	 * substring may be scrolled to the closest edge, the furthest edge, or
	 * midway between those two edges. In cases where there is a hierarchy of
	 * nested scrollable controls, more than one control may have to be
	 * scrolled.
	 * 
	 */
	public static final int SCROLL_TYPE_TOP_EDGE = 2;

	/**
	 * Scroll the bottom edge of the object or substring such that the bottom
	 * edge (and as much as possible of the rest of the object or substring) is
	 * within the top level window. In cases where the entire object or
	 * substring fits within the top level window, the placement of the object
	 * or substring is dependent on the application. For example, the object or
	 * substring may be scrolled to the closest edge, the furthest edge, or
	 * midway between those two edges. In cases where there is a hierarchy of
	 * nested scrollable controls, more than one control may have to be
	 * scrolled.
	 * 
	 */
	public static final int SCROLL_TYPE_BOTTOM_EDGE = 3;

	/**
	 * Scroll the left edge of the object or substring such that the left edge
	 * (and as much as possible of the rest of the object or substring) is
	 * within the top level window. In cases where the entire object or substring
	 * fits within the top level window, the placement of the object or
	 * substring is dependent on the application. For example, the object or
	 * substring may be scrolled to the closest edge, the furthest edge, or
	 * midway between those two edges. In cases where there is a hierarchy of
	 * nested scrollable controls, more than one control may have to be
	 * scrolled.
	 * 
	 */
	public static final int SCROLL_TYPE_LEFT_EDGE = 4;

	/**
	 * Scroll the right edge of the object or substring such that the right edge
	 * (and as much as possible of the rest of the object or substring) is
	 * within the top level window. In cases where the entire object or
	 * substring fits within the top level window, the placement of the object
	 * or substring is dependent on the application. For example, the object or
	 * substring may be scrolled to the closest edge, the furthest edge, or
	 * midway between those two edges. In cases where there is a hierarchy of
	 * nested scrollable controls, more than one control may have to be
	 * scrolled.
	 * 
	 */
	public static final int SCROLL_TYPE_RIGHT_EDGE =  5;

	/**
	 * Scroll the object or substring such that as much as possible of the
	 * object or substring is within the top level window. The placement of the
	 * object is dependent on the application. For example, the object or
	 * substring may be scrolled to to closest edge, the furthest edge, or
	 * midway between those two edges.
	 * 
	 */
	public static final int SCROLL_TYPE_ANYWHERE = 6;

	/**
	 * Scroll the top left corner of the object or substring to the specified point.
	 * 
	 */
	public static final int SCROLL_TYPE_POINT = 7;

	/**
	 * Send when the selection within a container has changed.
	 * 
	 */
	public static final int EVENT_SELECTION_CHANGED = 0x8009;
	
	/**
	 * Send when an object's text selection has changed.
	 * 
	 */
	public static final int EVENT_TEXT_SELECTION_CHANGED = 0x8014;
	
	/**
	 * Send when an object's state has changed, for example enabled/disabled, pressed/released, or checked/unchecked.
	 * <p>
	 * The eventData object is an array of 2 ints specifying the following:<ul>
	 * <li>state - the STATE_* constant identifying the state that changed</li>
	 * <li>newValue - either 1 or 0, indicating whether the state has changed to true or false</li>
	 * </ul></p>
	 * 
	 */
	public static final int EVENT_STATE_CHANGED = 0x800A;
	
	/**
	 * Send when an object has moved.
	 * <p>
	 * Note: only send one notification for the topmost object that has changed.
	 * </p>
	 * 
	 */
	public static final int EVENT_LOCATION_CHANGED = 0x800B;
	
	/**
	 * Send when an object's name has changed.
	 * 
	 */
	public static final int EVENT_NAME_CHANGED = 0x800C;
	
	/**
	 * Send when an object's description has changed.
	 * 
	 */
	public static final int EVENT_DESCRIPTION_CHANGED = 0x800D;
	
	/**
	 * Send when an object's value has changed.
	 * <p>
	 * The eventData object is an array of 2 Numbers specifying the following:<ul>
	 * <li>oldValue - the object's old value</li>
	 * <li>newValue - the object's new value</li>
	 * </ul></p>
	 * 
	 */
	public static final int EVENT_VALUE_CHANGED = 0x800E;

	/**
	 * Send when the loading of a document has completed.
	 * 
	 */
	public static final int EVENT_DOCUMENT_LOAD_COMPLETE = 0x105;

	/**
	 * Send when the loading of a document was interrupted.
	 * 
	 */
	public static final int EVENT_DOCUMENT_LOAD_STOPPED = 0x106;

	/**
	 * Send when the document contents are being reloaded.
	 * 
	 */
	public static final int EVENT_DOCUMENT_RELOAD = 0x107;

	/**
	 * Send when a slide changed in a presentation document
	 * or a page boundary was crossed in a word processing document. 
	 * 
	 */
	public static final int EVENT_PAGE_CHANGED = 0x111;

	/**
	 * Send when the caret moved from one section to the next.
	 * 
	 */
	public static final int EVENT_SECTION_CHANGED = 0x112;

	/**
	 * Send when the count or attributes of an accessible object's actions have changed.
	 * 
	 */
	public static final int EVENT_ACTION_CHANGED = 0x100;

	/**
	 * Send when the starting index of this link within the containing string has changed.
	 * 
	 */
	public static final int EVENT_HYPERLINK_START_INDEX_CHANGED = 0x10d;

	/**
	 * Send when the ending index of this link within the containing string has changed.
	 * 
	 */
	public static final int EVENT_HYPERLINK_END_INDEX_CHANGED = 0x108;

	/**
	 * Send when the number of anchors associated with this hyperlink object has changed.
	 * 
	 */
	public static final int EVENT_HYPERLINK_ANCHOR_COUNT_CHANGED = 0x109;

	/**
	 * Send when the hyperlink selected state changed from selected to unselected
	 * or from unselected to selected.
	 * 
	 */
	public static final int EVENT_HYPERLINK_SELECTED_LINK_CHANGED = 0x10a;

	/**
	 * Send when the hyperlink has been activated.
	 * 
	 */
	public static final int EVENT_HYPERLINK_ACTIVATED = 0x10b;

	/**
	 * Send when one of the links associated with the hypertext object has been selected.
	 * <p>
	 * The eventData object is an Integer that represents the index of the selected link
	 * in the hypertext object.
	 * </p>
	 * 
	 */
	public static final int EVENT_HYPERTEXT_LINK_SELECTED = 0x10c;

	/**
	 * Send when the number of hyperlinks associated with a hypertext object has changed.
	 * 
	 */
	public static final int EVENT_HYPERTEXT_LINK_COUNT_CHANGED = 0x10f;

	/**
	 * Send when an object's attributes have changed.
	 * 
	 * @see #EVENT_TEXT_ATTRIBUTE_CHANGED
	 * 
	 */
	public static final int EVENT_ATTRIBUTE_CHANGED = 0x200;

	/**
	 * Send when a table caption has changed.
	 * 
	 */
	public static final int EVENT_TABLE_CAPTION_CHANGED = 0x203;

	/**
	 * Send when a table's column description has changed.
	 * 
	 */
	public static final int EVENT_TABLE_COLUMN_DESCRIPTION_CHANGED = 0x204;

	/**
	 * Send when a table's column header has changed.
	 * 
	 */
	public static final int EVENT_TABLE_COLUMN_HEADER_CHANGED = 0x205;

	/**
	 * Send when a table's data has changed.
	 * <p>
	 * The eventData object is an array of 5 ints specifying the following:<ul>
	 * <li>type - {@link ACC#INSERT} or {@link ACC#DELETE} - the type of change</li>
	 * <li>rowStart - the index of the first row that changed</li>
	 * <li>rowCount - the number of contiguous rows that changed, or 0 if no rows changed</li>
	 * <li>columnStart - the index of the first column that changed</li>
	 * <li>columnCount - the number of contiguous columns that changed, or 0 if no columns changed</li>
	 * </ul></p>
	 * 
	 */
	public static final int EVENT_TABLE_CHANGED = 0x206;

	/**
	 * Send when a table's row description has changed.
	 * 
	 */
	public static final int EVENT_TABLE_ROW_DESCRIPTION_CHANGED = 0x207;

	/**
	 * Send when a table's row header has changed.
	 * 
	 */
	public static final int EVENT_TABLE_ROW_HEADER_CHANGED = 0x208;

	/**
	 * Send when a table's summary has changed.
	 * 
	 */
	public static final int EVENT_TABLE_SUMMARY_CHANGED = 0x209;

	/**
	 * Send when a text object's attributes have changed.
	 * 
	 * @see #EVENT_ATTRIBUTE_CHANGED
	 * 
	 */
	public static final int EVENT_TEXT_ATTRIBUTE_CHANGED = 0x20a;
	 
	/**
	 * Send when the caret has moved to a new position.
	 * 
	 */
	public static final int EVENT_TEXT_CARET_MOVED = 0x11b;

	/**
	 * Send when the caret has moved from one column to the next.
	 * 
	 */
	public static final int EVENT_TEXT_COLUMN_CHANGED = 0x11d;

	/**
	 * Send when text was inserted or deleted.
	 * <p>
	 * The eventData object is an array of 4 objects specifying the following:<ul>
	 * <li>type - {@link ACC#INSERT} or {@link ACC#DELETE} - the type of change</li>
	 * <li>start - the index of the first character that changed</li>
	 * <li>end - the index of the last character that changed</li>
	 * <li>text - the text string that was inserted or deleted</li>
	 * </ul></p>
	 * 
	 */
	public static final int EVENT_TEXT_CHANGED = 0x20c;

	/**
	 * Some attribute of this object is affected by a target object.
	 * 
	 */
	public static final int RELATION_CONTROLLED_BY = 0;

	/**
	 * This object is interactive and controls some attribute of a target object.
	 * 
	 */
	public static final int RELATION_CONTROLLER_FOR = 1;

	/**
	 * This object is described by the target object.
	 * 
	 */
	public static final int RELATION_DESCRIBED_BY = 2;

	/**
	 * This object is describes the target object.
	 * 
	 */
	public static final int RELATION_DESCRIPTION_FOR = 3;

	/**
	 * This object is embedded by a target object.
	 * 
	 */
	public static final int RELATION_EMBEDDED_BY = 4;

	/**
	 * This object embeds a target object. This relation can be used on a
	 * control's accessible to show where the content areas are.
	 * 
	 */
	public static final int RELATION_EMBEDS = 5;

	/**
	 * Content flows to this object from a target object. 
	 * This relation and RELATION_FLOWS_TO are useful to tie text and non-text
	 * objects together in order to allow assistive technology to follow the
	 * intended reading order.
	 * 
	 */
	public static final int RELATION_FLOWS_FROM = 6;

	/**
	 * Content flows from this object to a target object.
	 * 
	 */
	public static final int RELATION_FLOWS_TO = 7;

	/**
	 * This object is label for a target object.
	 * 
	 */
	public static final int RELATION_LABEL_FOR = 8;

	/**
	 * This object is labelled by a target object.
	 * 
	 */
	public static final int RELATION_LABELLED_BY = 9;

	/**
	 * This object is a member of a group of one or more objects. When 
	 * there is more than one object in the group each member may have one and the 
	 * same target, e.g. a grouping object.  It is also possible that each member has 
	 * multiple additional targets, e.g. one for every other member in the group.
	 * 
	 */
	public static final int RELATION_MEMBER_OF = 10;

	/**
	 * This object is a child of a target object.
	 * 
	 */
	public static final int RELATION_NODE_CHILD_OF = 11;

	/**
	 * This object is a parent window of the target object.
	 * 
	 */
	public static final int RELATION_PARENT_WINDOW_OF = 12;

	/**
	 * This object is a transient component related to the target object. 
	 * When this object is activated the target object doesn't lose focus.
	 * 
	 */
	public static final int RELATION_POPUP_FOR = 13;

	/**
	 * This object is a sub window of a target object.
	 * 
	 */
	public static final int RELATION_SUBWINDOW_OF = 14;	
}
