/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fieldassist;

import java.io.Serializable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * DecoratedField manages image decorations around a control. It allows clients
 * to specify an image decoration and a position for the decoration relative to
 * the field. Decorations may be assigned descriptions, which are shown when the
 * user hovers over the decoration. Clients can decorate any kind of control by
 * supplying a {@link IControlCreator} to create the control that is decorated.
 * <p>
 * Decorations always appear on either horizontal side of the field, never above
 * or below it. Decorations can be positioned at the top or bottom of either
 * side. Future implementations may provide additional positioning options for
 * decorations.
 * <p>
 * By default, DecoratedField will consult the {@link FieldDecorationRegistry}
 * to determine how much space should be reserved for each decoration. This
 * allows fields with decorations from different sources to align properly on
 * the same dialog, since the registry tracks the size of all decorations
 * registered. Therefore, it is recommended, but not required, that clients of
 * DecoratedField register the decorations used. In cases where alignment
 * between different fields is not a concern, clients can use
 * <code>setUseMaximumDecorationWidth(false)</code> and need not register
 * their decorations.
 * <p>
 * This class is not intended to be subclassed.
 * 
 * @since 1.0
 * @deprecated As of 3.3, clients should use {@link ControlDecoration} instead.
 */
public class DecoratedField implements Serializable {

	/**
	 * Cached platform flags for dealing with platform-specific issues.
	 */
	private static boolean MAC = Util.isMac();

	/**
	 * Constants describing the array indices used to hold the decorations in
	 * array slots.
	 */

	private static final int LEFT_TOP = 0;

	private static final int LEFT_BOTTOM = 1;

	private static final int RIGHT_TOP = 2;

	private static final int RIGHT_BOTTOM = 3;

	private static final int DECORATION_SLOTS = 4;

	/**
	 * Simple data structure class for specifying the internals for a field
	 * decoration. This class contains data specific to the implementation of
	 * field decorations as labels attached to the field. Clients should use
	 * <code>FieldDecoration</code> for specifying a decoration.
	 */
	private class FieldDecorationData {

		/* Package */FieldDecoration decoration;

		/* Package */Label label;

		/* Package */FormData data;

		/* Package */boolean showOnFocus;

		/* Package */boolean visible = true;

		/**
		 * Create a decoration data representing the specified decoration, using
		 * the specified label and form data for its representation.
		 * 
		 * @param decoration
		 *            the decoration whose data is kept.
		 * @param label
		 *            the label used to represent the decoration.
		 * @param formData
		 *            the form data used to attach the decoration to its field.
		 * @param showOnFocus
		 *            a boolean specifying whether the decoration should only be
		 *            shown when the field has focus.
		 */
		FieldDecorationData(FieldDecoration decoration, Label label,
				FormData formData, boolean showOnFocus) {
			this.decoration = decoration;
			this.label = label;
			this.data = formData;
			this.showOnFocus = showOnFocus;
		}
	}

	/**
	 * Decorations keyed by position.
	 */
	private FieldDecorationData[] decDatas = new FieldDecorationData[DECORATION_SLOTS];

	/**
	 * The associated control
	 */
	private Control control;

	/**
	 * The composite with form layout used to manage decorations.
	 */
	private Composite form;

	/**
	 * The boolean that indicates whether the maximum decoration width is used
	 * when allocating space for decorations.
	 */
	private boolean useMaxDecorationWidth = true;

	/**
	 * The hover used for showing description text
	 */
	private Hover hover;

	/**
	 * The hover used to show a decoration image's description.
	 */
	class Hover {
		private static final String EMPTY = ""; //$NON-NLS-1$

		/**
		 * Offset of info hover arrow from the left or right side.
		 */
		private int hao = 10;

		/**
		 * Width of info hover arrow.
		 */
		private int haw = 8;

		/**
		 * Height of info hover arrow.
		 */
		private int hah = 10;

		/**
		 * Margin around info hover text.
		 */
		private int hm = 2;

		/**
		 * This info hover's shell.
		 */
		Shell hoverShell;

		/**
		 * The info hover text.
		 */
		String text = EMPTY;

		/**
		 * The region used to manage the shell shape
		 */
		// RAP [bm]: Region
//		Region region;

		/**
		 * Boolean indicating whether the last computed polygon location had an
		 * arrow on left. (true if left, false if right).
		 */
		boolean arrowOnLeft = true;

		/*
		 * Create a hover parented by the specified shell.
		 */
		Hover(Shell parent) {
			final Display display = parent.getDisplay();
			hoverShell = new Shell(parent, SWT.NO_TRIM | SWT.ON_TOP
					| SWT.NO_FOCUS | SWT.TOOL);
			hoverShell.setBackground(display
					.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			hoverShell.setForeground(display
					.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			// RAP [bm]: 
//			hoverShell.addPaintListener(new PaintListener() {
//				public void paintControl(PaintEvent pe) {
//					pe.gc.drawString(text, hm, hm);
//					if (!MAC) {
//						pe.gc.drawPolygon(getPolygon(true));
//					}
//				}
//			});
			// RAPEND: [bm] 

			hoverShell.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					hideHover();
				}
			});
		}

		/*
		 * Compute a polygon that represents a hover with an arrow pointer. If
		 * border is true, compute the polygon inset by 1-pixel border. Consult
		 * the arrowOnLeft flag to determine which side the arrow is on.
		 */
		int[] getPolygon(boolean border) {
			Point e = getExtent();
			int b = border ? 1 : 0;
			if (arrowOnLeft) {
				return new int[] { 0, 0, e.x - b, 0, e.x - b, e.y - b,
						hao + haw, e.y - b, hao + haw / 2, e.y + hah - b, hao,
						e.y - b, 0, e.y - b, 0, 0 };
			}
			return new int[] { 0, 0, e.x - b, 0, e.x - b, e.y - b,
					e.x - hao - b, e.y - b, e.x - hao - haw / 2, e.y + hah - b,
					e.x - hao - haw, e.y - b, 0, e.y - b, 0, 0 };
		}

		/*
		 * Dispose the hover, it is no longer needed. Dispose any resources
		 * allocated by the hover.
		 */
		void dispose() {
			if (!hoverShell.isDisposed()) {
				hoverShell.dispose();
			}
			// RAP [bm]: Region
//			if (region != null) {
//				region.dispose();
//			}
			// RAPEND: [bm] 

		}

		/*
		 * Set the visibility of the hover.
		 */
		void setVisible(boolean visible) {
			if (visible) {
				if (!hoverShell.isVisible()) {
					hoverShell.setVisible(true);
				}
			} else {
				if (hoverShell.isVisible()) {
					hoverShell.setVisible(false);
				}
			}
		}

		/*
		 * Set the text of the hover to the specified text. Recompute the size
		 * and location of the hover to hover near the specified control,
		 * pointing the arrow toward the target control.
		 */
		void setText(String t, Control hoverNear, Control targetControl) {
			if (t == null) {
				t = EMPTY;
			}
			if (!t.equals(text)) {
				Point oldSize = getExtent();
				text = t;
				hoverShell.redraw();
				Point newSize = getExtent();
				if (!oldSize.equals(newSize)) {
					// set a flag that indicates the direction of arrow
					arrowOnLeft = hoverNear.getLocation().x <= targetControl
							.getLocation().x;
					setNewShape();
				}
			}

			if (hoverNear != null) {
				Point extent = getExtent();
				int y = -extent.y - hah + 1;
				int x = arrowOnLeft ? -hao + haw / 2 : -extent.x + hao + haw
						/ 2;

				hoverShell.setLocation(hoverNear.toDisplay(x, y));
			}

		}

		/*
		 * Return whether or not the hover (shell) is visible.
		 */
		boolean isVisible() {
			return hoverShell.isVisible();
		}

		/*
		 * Compute the extent of the hover for the current text.
		 */
		Point getExtent() {
			GC gc = new GC(hoverShell);
			Point e = gc.textExtent(text);
			gc.dispose();
			e.x += hm * 2;
			e.y += hm * 2;
			return e;
		}

		/*
		 * Compute a new shape for the hover shell.
		 */
		void setNewShape() {
			// RAP [bm]: Region
//			Region oldRegion = region;
//			region = new Region();
//			region.add(getPolygon(false));
//			hoverShell.setRegion(region);
//			if (oldRegion != null) {
//				oldRegion.dispose();
//			}
			// RAPEND: [bm] 

		}
	}

	/**
	 * Construct a decorated field which is parented by the specified composite
	 * and has the given style bits. Use the controlCreator to create the
	 * specific kind of control that is decorated inside the field.
	 * 
	 * @param parent
	 *            the parent of the decorated field.
	 * @param style
	 *            the desired style bits for the field.
	 * @param controlCreator
	 *            the IControlCreator used to specify the specific kind of
	 *            control that is to be decorated.
	 * 
	 * @see IControlCreator
	 */
	public DecoratedField(Composite parent, int style,
			IControlCreator controlCreator) {
		this.form = createForm(parent);
		this.control = controlCreator.createControl(form, style);

		addControlListeners();
		form.setTabList(new Control[] { control });

		// Set up the initial layout data.
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		control.setLayoutData(data);

	}

	/**
	 * Adds an image decoration to the field.
	 * 
	 * @param decoration
	 *            A FieldDecoration describing the image and description for the
	 *            decoration
	 * 
	 * @param position
	 *            The SWT constant indicating the position of the decoration
	 *            relative to the field's control. The position should include
	 *            style bits describing both the vertical and horizontal
	 *            orientation. <code>SWT.LEFT</code> and
	 *            <code>SWT.RIGHT</code> describe the horizontal placement of
	 *            the decoration relative to the field, and the constants
	 *            <code>SWT.TOP</code> and <code>SWT.BOTTOM</code> describe
	 *            the vertical alignment of the decoration relative to the
	 *            field. Decorations always appear on either horizontal side of
	 *            the field, never above or below it. For example, a decoration
	 *            appearing on the left side of the field, at the top, is
	 *            specified as SWT.LEFT | SWT.TOP. If an image decoration
	 *            already exists in the specified position, it will be replaced
	 *            by the one specified.
	 * @param showOnFocus
	 *            <code>true</code> if the decoration should only be shown
	 *            when the associated control has focus, <code>false</code> if
	 *            it should always be shown.
	 * 
	 */
	public void addFieldDecoration(FieldDecoration decoration, int position,
			boolean showOnFocus) {
		final Label label;
		FormData formData;
		int i = indexForPosition(position);
		if (decDatas[i] == null) {
			formData = createFormDataForIndex(i, decoration.getImage());
			label = new Label(form, SWT.HORIZONTAL | SWT.VERTICAL | SWT.CENTER);
			// RAP [bm]: 
//			label.addMouseTrackListener(new MouseTrackListener() {
//				public void mouseHover(MouseEvent event) {
//					FieldDecorationData decData = (FieldDecorationData) event.widget
//							.getData();
//					String desc = decData.decoration.getDescription();
//					if (desc != null) {
//						showHoverText(desc, label);
//					}
//				}
//
//				public void mouseEnter(MouseEvent event) {
//				}
//
//				public void mouseExit(MouseEvent event) {
//					hideHover();
//				}
//			});
			// RAPEND: [bm] 

			decDatas[i] = new FieldDecorationData(decoration, label, formData,
					showOnFocus);
		} else {
			label = decDatas[i].label;
			formData = decDatas[i].data;
			decDatas[i].decoration = decoration;
			decDatas[i].showOnFocus = showOnFocus;
		}
		label.setImage(decDatas[i].decoration.getImage());
		// RAP [fappel]: (mis-)use images toolTipText for the decorations' description
		showHoverText( decDatas[i].decoration.getDescription(), label );
		// RAPEND: [bm] 

		label.setData(decDatas[i]);
		label.setLayoutData(formData);
		label.setVisible(!showOnFocus);

		// Since sizes may have changed or there could be a new position
		// defined, we need to update layout data on the control.
		updateControlAttachments(i, decDatas[i]);
	}

	/*
	 * A decoration at the specified index has been added. Update the control's
	 * attachments if it has not previously been attached on that side or if it
	 * was attached to a decoration with a lesser width.
	 */
	private void updateControlAttachments(int index, FieldDecorationData decData) {
		FormData formData = (FormData) control.getLayoutData();
		int newWidth = widthOf(decData.decoration.getImage());
		// opposing represents the location of the decoration above or below
		// the one in question.
		int opposing;

		switch (index) {
		case LEFT_TOP:
		case LEFT_BOTTOM:
			if (index == LEFT_TOP) {
				opposing = LEFT_BOTTOM;
			} else {
				opposing = LEFT_TOP;
			}
			if (decDatas[opposing] == null) {
				// No decorator on the opposing side.
				// Attach the control to this decorator
				formData.left = new FormAttachment(decData.label);
			} else if (decDatas[opposing].data.width < newWidth) {
				// Decorator on opposing side is the smaller one. Attach
				// control to the new one.
				formData.left = new FormAttachment(decData.label);
				// Center align the smaller one relative to the larger one.
				decDatas[opposing].data.left.alignment = SWT.CENTER;
				decDatas[opposing].data.left.control = decData.label;
			} else {
				// The new decorator is the smaller one. Keep the
				// control attached to the opposing one.
				formData = null;
				// Horizontally center the smaller one relative to the larger
				// one.
				decData.data.left.alignment = SWT.CENTER;
				decData.data.left.control = decDatas[opposing].label;
			}
			break;
		/*
		 * The only real difference in right side cases is that we are attaching
		 * the right side of the control to the wider decoration rather than the
		 * left side of the control. Other concerns (horizontally aligning the
		 * smaller decoration relative to the larger one) are the same.
		 */
		case RIGHT_TOP:
		case RIGHT_BOTTOM:
			if (index == RIGHT_TOP) {
				opposing = RIGHT_BOTTOM;
			} else {
				opposing = RIGHT_TOP;
			}
			if (decDatas[opposing] == null) {
				// No decorator on the opposing side.
				// Attach the control to this decorator.
				formData.right = new FormAttachment(decData.label);
			} else if (decDatas[opposing].data.width < newWidth) {
				// Decorator on opposing side is the smaller one. Attach
				// control to the new one.
				formData.right = new FormAttachment(decData.label);
				// Center align the smaller one to the larger one.
				// Note that this could be done using the left or right
				// attachment, we use the right since it is already
				// created for all right-side decorations.
				decDatas[opposing].data.right.alignment = SWT.CENTER;
				decDatas[opposing].data.right.control = decData.label;
			} else {
				// The new decorator is the smaller one. Keep the
				// control attached to the opposing one.
				formData = null;
				// Horizontally center align the smaller one to the
				// larger one.
				decData.data.right.alignment = SWT.CENTER;
				decData.data.right.control = decDatas[opposing].label;
			}
			break;
		default:
			return;
		}
		if (formData != null) {
			// Form data was updated.
			control.setLayoutData(formData);
			form.layout();
		}
	}

	/**
	 * Get the control that is decorated by the receiver.
	 * 
	 * @return the Control decorated by the receiver, or <code>null</code> if
	 *         none has been created yet.
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Get the control that represents the decorated field. This composite
	 * should be used to lay out the field within its parent.
	 * 
	 * @return the Control that should be layed out in the field's parent's
	 *         layout. This is typically not the control itself, since
	 *         additional controls are used to represent the decorations.
	 */
	public Control getLayoutControl() {
		return form;
	}

	/**
	 * Create the parent composite and a form layout that will be used to manage
	 * decorations.
	 */
	private Composite createForm(Composite parent) {
		Composite composite = new Composite(parent, SWT.NO_FOCUS);
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=126553
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		composite.setLayout(new FormLayout());
		return composite;
	}

	/**
	 * Add any listeners needed on the target control.
	 */
	private void addControlListeners() {
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				if (hover != null) {
					hover.dispose();
				}
			}
		});
		control.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {
				controlFocusGained();
			}

			public void focusLost(FocusEvent event) {
				controlFocusLost();
			}

		});
	}

	/*
	 * Return the index in the array of decoration datas that represents the
	 * specified SWT position.
	 * 
	 * @param position The SWT constant indicating the position of the
	 * decoration relative to the field's control. The position should include
	 * style bits describing both the vertical and horizontal orientation.
	 * <code>SWT.LEFT</code> and <code>SWT.RIGHT</code> describe the
	 * horizontal placement of the decoration relative to the field, and the
	 * constants <code>SWT.TOP</code> and <code>SWT.BOTTOM</code> describe
	 * the vertical alignment of the decoration relative to the field.
	 * Decorations always appear on either horizontal side of the field, never
	 * above or below it. For example, a decoration appearing on the left side
	 * of the field, at the top, is specified as SWT.LEFT | SWT.TOP.
	 * 
	 * @return index the index in the array of decorations that represents the
	 * specified SWT position. If the position is not an expected position, the
	 * index representing the top left position will be returned.
	 * 
	 */
	private int indexForPosition(int position) {
		switch (position) {
		case SWT.LEFT | SWT.BOTTOM:
			return LEFT_BOTTOM;
		case SWT.RIGHT | SWT.TOP:
			return RIGHT_TOP;
		case SWT.RIGHT | SWT.BOTTOM:
			return RIGHT_BOTTOM;
		default:
			return LEFT_TOP;
		}
	}

	/*
	 * Create a form data that will place the decoration at the specified
	 * position.
	 * 
	 * @param index the index in the decDatas describing the position of the
	 * decoration.
	 * 
	 * @param image the image shown in the decoration.
	 * 
	 */
	private FormData createFormDataForIndex(int index, Image image) {
		Assert.isTrue(index >= 0 && index < DECORATION_SLOTS,
				"Index out of range"); //$NON-NLS-1$

		FormData data = new FormData();
		switch (index) {
		case LEFT_TOP:
			data.left = new FormAttachment(0, 0);
			data.top = new FormAttachment(0, 0);
			break;
		case LEFT_BOTTOM:
			data.left = new FormAttachment(0, 0);
			data.bottom = new FormAttachment(100, 0);
			break;
		case RIGHT_TOP:
			data.right = new FormAttachment(100, 0);
			data.top = new FormAttachment(0, 0);
			break;
		case RIGHT_BOTTOM:
			data.right = new FormAttachment(100, 0);
			data.bottom = new FormAttachment(100, 0);
			break;
		}
		data.width = widthOf(image);
		data.height = SWT.DEFAULT;

		return data;
	}

	/**
	 * Show the specified text using the same hover dialog as is used to show
	 * decorator descriptions. Normally, a decoration's description text will be
	 * shown in an info hover over the field's control whenever the mouse hovers
	 * over the decoration. This method can be used to show a decoration's
	 * description text at other times (such as when the control receives
	 * focus), or to show other text associated with the field.
	 * 
	 * <p>
	 * If there is currently a hover visible, the hover's text will be replaced
	 * with the specified text.
	 * 
	 * @param text
	 *            the text to be shown in the info hover, or <code>null</code>
	 *            if no text should be shown.
	 */
	public void showHoverText(String text) {
		showHoverText(text, control);
	}

	/**
	 * Hide any hover popups that are currently showing on the control.
	 * Normally, a decoration's description text will be shown in an info hover
	 * over the field's control as long as the mouse hovers over the decoration,
	 * and will be hidden when the mouse exits the control. This method can be
	 * used to hide a hover that was shown using <code>showHoverText</code>,
	 * or to programatically hide the current decoration hover.
	 * 
	 * <p>
	 * This message has no effect if there is no current hover.
	 * 
	 */
	public void hideHover() {
		if (hover != null) {
			hover.setVisible(false);
		}
	}

	/*
	 * The target control gained focus. Any decorations that should show only
	 * when they have the focus should be shown here.
	 */
	private void controlFocusGained() {
		for (int i = 0; i < DECORATION_SLOTS; i++) {
			if (decDatas[i] != null && decDatas[i].showOnFocus) {
				setVisible(decDatas[i], true);
			}
		}
	}

	/*
	 * The target control lost focus. Any decorations that should show only when
	 * they have the focus should be hidden here.
	 */
	private void controlFocusLost() {
		for (int i = 0; i < DECORATION_SLOTS; i++) {
			if (decDatas[i] != null && decDatas[i].showOnFocus) {
				setVisible(decDatas[i], false);
			}
		}
	}

	/**
	 * Show the specified decoration. This message has no effect if the
	 * decoration is already showing, or was not already added to the field
	 * using <code>addFieldDecoration</code>.
	 * 
	 * @param decoration
	 *            the decoration to be shown.
	 */
	public void showDecoration(FieldDecoration decoration) {
		FieldDecorationData data = getDecorationData(decoration);
		if (data == null) {
			return;
		}
		// record the fact that client would like it to be visible
		data.visible = true;
		// even if it is supposed to be shown, if the field does not have focus,
		// do not show it (yet)
		if (!data.showOnFocus || control.isFocusControl()) {
			setVisible(data, true);
		}
	}

	/**
	 * Hide the specified decoration. This message has no effect if the
	 * decoration is already hidden, or was not already added to the field using
	 * <code>addFieldDecoration</code>.
	 * 
	 * @param decoration
	 *            the decoration to be hidden.
	 */
	public void hideDecoration(FieldDecoration decoration) {
		FieldDecorationData data = getDecorationData(decoration);
		if (data == null) {
			return;
		}
		// Store the desired visibility in the decData. We remember the
		// client's instructions so that changes in visibility caused by
		// field focus changes won't violate the client's visibility setting.
		data.visible = false;
		setVisible(data, false);
	}

	/**
	 * Update the specified decoration. This message should be used if the image
	 * or description in the decoration have changed. This message has no
	 * immediate effect if the decoration is not visible, and no effect at all
	 * if the decoration was not previously added to the field.
	 * 
	 * @param decoration
	 *            the decoration to be hidden.
	 */
	public void updateDecoration(FieldDecoration decoration) {
		FieldDecorationData data = getDecorationData(decoration);
		if (data == null) {
			return;
		}
		if (data.label != null) {
			data.label.setImage(decoration.getImage());
			// If the decoration is being shown, and a hover is active,
			// update the hover text to display the new description.
			if (data.label.getVisible() == true && hover != null) {
				showHoverText(decoration.getDescription(), data.label);
			}
		}
	}

	/*
	 * Set the visibility of the specified decoration data. This method does not
	 * change the visibility value stored in the decData, but instead consults
	 * it to determine how the visibility should be changed. This method is
	 * called any time visibility of a decoration might change, whether by
	 * client API or focus changes.
	 */
	private void setVisible(FieldDecorationData decData, boolean visible) {
		// Check the decData visibility flag, since it contains the client's
		// instructions for visibility.
		if (visible && decData.visible) {
			decData.label.setVisible(true);
		} else {
			decData.label.setVisible(false);
		}
	}

	/*
	 * Get the FieldDecorationData that corresponds to the given decoration.
	 */
	private FieldDecorationData getDecorationData(FieldDecoration dec) {
		for (int i = 0; i < DECORATION_SLOTS; i++) {
			if (decDatas[i] != null && dec == decDatas[i].decoration
					&& decDatas[i].label != null
					&& !decDatas[i].label.isDisposed()) {
				return decDatas[i];
			}
		}
		return null;
	}

	/*
	 * Show the specified text in the hover, positioning the hover near the
	 * specified control.
	 */
	private void showHoverText(String text, Control hoverNear) {
		if (text == null) {
			hideHover();
			return;
		}
		// RAP [fappel]: (mis-)use images toolTipText for the decorations' description
		hoverNear.setToolTipText( text );
		// RAPEND: [bm] 

		if (hover == null) {
			hover = new Hover(hoverNear.getShell());
		}
		hover.setText(text, hoverNear, control);
		hover.setVisible(true);
	}

	/**
	 * Set a boolean that indicates whether the receiver should use the
	 * decoration registry's maximum decoration width when allocating space for
	 * decorations. The default value is <code>true</code>. Using the maximum
	 * decoration width is useful so that decorated fields on the same dialog
	 * that have different decoration widths will all align. This also allows
	 * client dialogs to align non-decorated fields with decorated fields by
	 * consulting the maximum decoration width.
	 * </p>
	 * <p>
	 * Clients may wish to set this value to <code>false</code> in cases where
	 * space usage is more important than alignment of fields. This value must
	 * be set before the decorations are added in order to ensure proper
	 * alignment.
	 * </p>
	 * 
	 * @param useMaximumWidth
	 *            <code>true</code> if the maximum decoration width should be
	 *            used as the size for all decorations, <code>false</code> if
	 *            only the decoration size should be used.
	 * 
	 * @see FieldDecorationRegistry#getMaximumDecorationWidth()
	 */
	public void setUseMaximumDecorationWidth(boolean useMaximumWidth) {
		useMaxDecorationWidth = useMaximumWidth;
	}

	/*
	 * Return the width appropriate for the specified decoration image.
	 */
	private int widthOf(Image image) {
		if (image == null) {
			return 0;
		}
		return useMaxDecorationWidth ? FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth() : image.getBounds().width;
	}
}
