// RAP [bm]: ToolTip

///*******************************************************************************
// * Copyright (c) 2006, 2007 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *******************************************************************************/
//
//package org.eclipse.jface.window;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.custom.CLabel;
//import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.graphics.Font;
//import org.eclipse.swt.graphics.Image;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Event;
//
///**
// * Default implementation of ToolTip that provides an iconofied label with font
// * and color controls by subclass.
// * 
// * @since 1.0
// */
//public class DefaultToolTip extends ToolTip {
//	private String text;
//
//	private Color backgroundColor;
//
//	private Font font;
//
//	private Image backgroundImage;
//
//	private Color foregroundColor;
//
//	private Image image;
//
//	private int style = SWT.SHADOW_NONE;
//
//	/**
//	 * Create new instance which add TooltipSupport to the widget
//	 * 
//	 * @param control the control on whose action the tooltip is shown
//	 */
//	public DefaultToolTip(Control control) {
//		super(control);
//	}
//
//	/**
//	 * Create new instance which add TooltipSupport to the widget
//	 * 
//	 * @param control the control to which the tooltip is bound
//	 * @param style style passed to control tooltip behaviour
//	 * @param manualActivation <code>true</code> if the activation is done manually using
//	 *            {@link #show(Point)}
//	 * @see #RECREATE
//	 * @see #NO_RECREATE
//	 */
//	public DefaultToolTip(Control control, int style, boolean manualActivation) {
//		super(control, style, manualActivation);
//	}
//	
//	/**
//	 * Creates the content are of the the tooltip. By default this creates a
//	 * CLabel to display text. To customize the text Subclasses may override the
//	 * following methods
//	 * <ul>
//	 * <li>{@link #getStyle(Event)}</li>
//	 * <li>{@link #getBackgroundColor(Event)}</li>
//	 * <li>{@link #getForegroundColor(Event)}</li>
//	 * <li>{@link #getFont(Event)}</li>
//	 * <li>{@link #getImage(Event)}</li>
//	 * <li>{@link #getText(Event)}</li>
//	 * <li>{@link #getBackgroundImage(Event)}</li>
//	 * </ul>
//	 * 
//	 * @param event
//	 *            the event that triggered the activation of the tooltip
//	 * @param parent
//	 *            the parent of the content area
//	 * @return the content area created
//	 */
//	protected Composite createToolTipContentArea(Event event, Composite parent) {
//		Image image = getImage(event);
//		Image bgImage = getBackgroundImage(event);
//		String text = getText(event);
//		Color fgColor = getForegroundColor(event);
//		Color bgColor = getBackgroundColor(event);
//		Font font = getFont(event);
//
//		CLabel label = new CLabel(parent, getStyle(event));
//		if (text != null) {
//			label.setText(text);
//		}
//
//		if (image != null) {
//			label.setImage(image);
//		}
//
//		if (fgColor != null) {
//			label.setForeground(fgColor);
//		}
//
//		if (bgColor != null) {
//			label.setBackground(bgColor);
//		}
//
//		if (bgImage != null) {
//			label.setBackgroundImage(image);
//		}
//
//		if (font != null) {
//			label.setFont(font);
//		}
//
//		return label;
//	}
//
//	/**
//	 * The style used to create the {@link CLabel} in the default implementation
//	 * 
//	 * @param event
//	 *            the event triggered the popup of the tooltip
//	 * @return the style
//	 */
//	protected int getStyle(Event event) {
//		return style;
//	}
//
//	/**
//	 * The {@link Image} displayed in the {@link CLabel} in the default
//	 * implementation implementation
//	 * 
//	 * @param event
//	 *            the event triggered the popup of the tooltip
//	 * @return the {@link Image} or <code>null</code> if no image should be
//	 *         displayed
//	 */
//	protected Image getImage(Event event) {
//		return image;
//	}
//
//	/**
//	 * The foreground {@link Color} used by {@link CLabel} in the default
//	 * implementation
//	 * 
//	 * @param event
//	 *            the event triggered the popup of the tooltip
//	 * @return the {@link Color} or <code>null</code> if default foreground
//	 *         color should be used
//	 */
//	protected Color getForegroundColor(Event event) {
//		return (foregroundColor == null) ? event.widget.getDisplay()
//				.getSystemColor(SWT.COLOR_INFO_FOREGROUND) : foregroundColor;
//	}
//
//	/**
//	 * The background {@link Color} used by {@link CLabel} in the default
//	 * implementation
//	 * 
//	 * @param event
//	 *            the event triggered the popup of the tooltip
//	 * @return the {@link Color} or <code>null</code> if default background
//	 *         color should be used
//	 */
//	protected Color getBackgroundColor(Event event) {
//		return (backgroundColor == null) ? event.widget.getDisplay()
//				.getSystemColor(SWT.COLOR_INFO_BACKGROUND) : backgroundColor;
//	}
//
//	/**
//	 * The background {@link Image} used by {@link CLabel} in the default
//	 * implementation
//	 * 
//	 * @param event
//	 *            the event triggered the popup of the tooltip
//	 * @return the {@link Image} or <code>null</code> if no image should be
//	 *         displayed in the background
//	 */
//	protected Image getBackgroundImage(Event event) {
//		return backgroundImage;
//	}
//
//	/**
//	 * The {@link Font} used by {@link CLabel} in the default implementation
//	 * 
//	 * @param event
//	 *            the event triggered the popup of the tooltip
//	 * @return the {@link Font} or <code>null</code> if the default font
//	 *         should be used
//	 */
//	protected Font getFont(Event event) {
//		return font;
//	}
//
//	/**
//	 * The text displayed in the {@link CLabel} in the default implementation
//	 * 
//	 * @param event
//	 *            the event triggered the popup of the tooltip
//	 * @return the text or <code>null</code> if no text has to be displayed
//	 */
//	protected String getText(Event event) {
//		return text;
//	}
//
//	/**
//	 * The background {@link Image} used by {@link CLabel} in the default
//	 * implementation
//	 * 
//	 * @param backgroundColor
//	 *            the {@link Color} or <code>null</code> if default background
//	 *            color ({@link SWT#COLOR_INFO_BACKGROUND}) should be used
//	 */
//	public void setBackgroundColor(Color backgroundColor) {
//		this.backgroundColor = backgroundColor;
//	}
//
//	/**
//	 * The background {@link Image} used by {@link CLabel} in the default
//	 * implementation
//	 * 
//	 * @param backgroundImage
//	 *            the {@link Image} or <code>null</code> if no image should be
//	 *            displayed in the background
//	 */
//	public void setBackgroundImage(Image backgroundImage) {
//		this.backgroundImage = backgroundImage;
//	}
//
//	/**
//	 * The {@link Font} used by {@link CLabel} in the default implementation
//	 * 
//	 * @param font
//	 *            the {@link Font} or <code>null</code> if the default font
//	 *            should be used
//	 */
//	public void setFont(Font font) {
//		this.font = font;
//	}
//
//	/**
//	 * The foreground {@link Color} used by {@link CLabel} in the default
//	 * implementation
//	 * 
//	 * @param foregroundColor
//	 *            the {@link Color} or <code>null</code> if default foreground
//	 *            color should be used
//	 */
//	public void setForegroundColor(Color foregroundColor) {
//		this.foregroundColor = foregroundColor;
//	}
//
//	/**
//	 * The {@link Image} displayed in the {@link CLabel} in the default
//	 * implementation implementation
//	 * 
//	 * @param image
//	 *            the {@link Image} or <code>null</code> if no image should be
//	 *            displayed
//	 */
//	public void setImage(Image image) {
//		this.image = image;
//	}
//
//	/**
//	 * The style used to create the {@link CLabel} in the default implementation
//	 * 
//	 * @param style
//	 *            the event triggered the popup of the tooltip
//	 */
//	public void setStyle(int style) {
//		this.style = style;
//	}
//
//	/**
//	 * The text displayed in the {@link CLabel} in the default implementation
//	 * 
//	 * @param text
//	 *            the text or <code>null</code> if no text has to be displayed
//	 */
//	public void setText(String text) {
//		this.text = text;
//	}
//
//}
