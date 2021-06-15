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
//package org.eclipse.jface.fieldassist;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import org.eclipse.jface.resource.JFaceColors;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.graphics.RGB;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Display;
//
///**
// * FieldAssistColors defines protocol for retrieving colors that can be used to
// * provide visual cues with fields. For consistency with JFace dialogs and
// * wizards, it is recommended that FieldAssistColors is used when colors are
// * used to annotate fields.
// * <p>
// * Color resources that are returned using methods in this class are maintained
// * in the JFace color registries, or by SWT. Users of any color resources
// * provided by this class are not responsible for the lifecycle of the color.
// * Colors provided by this class should never be disposed by clients. In some
// * cases, clients are provided information, such as RGB values, in order to
// * create their own color resources. In these cases, the client should manage
// * the lifecycle of any created resource.
// * 
// * @since 1.0
// * @deprecated As of 3.3, this class is no longer necessary.
// */
//public class FieldAssistColors {
//
//	private static boolean DEBUG = false;
//
//	/*
//	 * Keys are background colors, values are the color with the alpha value
//	 * applied
//	 */
//	private static Map requiredFieldColorMap = new HashMap();
//
//	/*
//	 * Keys are colors we have created, values are the displays on which they
//	 * were created.
//	 */
//	private static Map displays = new HashMap();
//
//	/**
//	 * Compute the RGB of the color that should be used for the background of a
//	 * control to indicate that the control has an error. Because the color
//	 * suitable for indicating an error depends on the colors set into the
//	 * control, this color is always computed dynamically and provided as an RGB
//	 * value. Clients who use this RGB to create a Color resource are
//	 * responsible for managing the life cycle of the color.
//	 * <p>
//	 * This color is computed dynamically each time that it is queried. Clients
//	 * should typically call this method once, create a color from the RGB
//	 * provided, and dispose of the color when finished using it.
//	 * 
//	 * @param control
//	 *            the control for which the background color should be computed.
//	 * @return the RGB value indicating a background color appropriate for
//	 *         indicating an error in the control.
//	 */
//	public static RGB computeErrorFieldBackgroundRGB(Control control) {
//		/*
//		 * Use a 10% alpha of the error color applied on top of the widget
//		 * background color.
//		 */
//		Color dest = control.getBackground();
//		Color src = JFaceColors.getErrorText(control.getDisplay());
//		int destRed = dest.getRed();
//		int destGreen = dest.getGreen();
//		int destBlue = dest.getBlue();
//
//		// 10% alpha
//		int alpha = (int) (0xFF * 0.10f);
//		// Alpha blending math
//		destRed += (src.getRed() - destRed) * alpha / 0xFF;
//		destGreen += (src.getGreen() - destGreen) * alpha / 0xFF;
//		destBlue += (src.getBlue() - destBlue) * alpha / 0xFF;
//
//		return new RGB(destRed, destGreen, destBlue);
//	}
//
//	/**
//	 * Return the color that should be used for the background of a control to
//	 * indicate that the control is a required field and does not have content.
//	 * <p>
//	 * This color is managed by FieldAssistResources and should never be
//	 * disposed by clients.
//	 * 
//	 * @param control
//	 *            the control on which the background color will be used.
//	 * @return the color used to indicate that a field is required.
//	 */
//	public static Color getRequiredFieldBackgroundColor(Control control) {
//		final Display display = control.getDisplay();
//
//		// If we are in high contrast mode, then don't apply an alpha
//		if (display.getHighContrast()) {
//			return control.getBackground();
//		}
//
//		// See if a color has already been computed
//		Object storedColor = requiredFieldColorMap.get(control.getBackground());
//		if (storedColor != null) {
//			return (Color) storedColor;
//		}
//
//		// There is no color already created, so we must create one.
//		// Use a 15% alpha of yellow on top of the widget background.
//		Color dest = control.getBackground();
//		Color src = display.getSystemColor(SWT.COLOR_YELLOW);
//		int destRed = dest.getRed();
//		int destGreen = dest.getGreen();
//		int destBlue = dest.getBlue();
//
//		// 15% alpha
//		int alpha = (int) (0xFF * 0.15f);
//		// Alpha blending math
//		destRed += (src.getRed() - destRed) * alpha / 0xFF;
//		destGreen += (src.getGreen() - destGreen) * alpha / 0xFF;
//		destBlue += (src.getBlue() - destBlue) * alpha / 0xFF;
//
//		// create the color
//		Color color = new Color(display, destRed, destGreen, destBlue);
//		// record the color in a map using the original color as the key
//		requiredFieldColorMap.put(dest, color);
//		// If we have never created a color on this display before, install
//		// a dispose exec on the display.
//		if (!displays.containsValue(display)) {
//			display.disposeExec(new Runnable() {
//				public void run() {
//					disposeColors(display);
//				}
//			});
//		}
//		// Record the color and its display in a map for later disposal.
//		displays.put(color, display);
//		return color;
//	}
//
//	/*
//	 * Dispose any colors that were allocated for the given display.
//	 */
//	private static void disposeColors(Display display) {
//		List toBeRemoved = new ArrayList(1);
//
//		if (DEBUG) {
//			System.out.println("Display map is " + displays.toString()); //$NON-NLS-1$
//			System.out
//					.println("Color map is " + requiredFieldColorMap.toString()); //$NON-NLS-1$
//		}
//
//		// Look for any stored colors that were created on this display
//		for (Iterator i = displays.keySet().iterator(); i.hasNext();) {
//			Color color = (Color) i.next();
//			if (((Display) displays.get(color)).equals(display)) {
//				// The color is on this display. Mark it for removal.
//				toBeRemoved.add(color);
//
//				// Now look for any references to it in the required field color
//				// map
//				List toBeRemovedFromRequiredMap = new ArrayList(1);
//				for (Iterator iter = requiredFieldColorMap.keySet().iterator(); iter
//						.hasNext();) {
//					Color bgColor = (Color) iter.next();
//					if (((Color) requiredFieldColorMap.get(bgColor))
//							.equals(color)) {
//						// mark it for removal from the required field color map
//						toBeRemovedFromRequiredMap.add(bgColor);
//					}
//				}
//				// Remove references in the required field map now that
//				// we are done iterating.
//				for (int j = 0; j < toBeRemovedFromRequiredMap.size(); j++) {
//					requiredFieldColorMap.remove(toBeRemovedFromRequiredMap
//							.get(j));
//				}
//			}
//		}
//		// Remove references in the display map now that we are
//		// done iterating
//		for (int i = 0; i < toBeRemoved.size(); i++) {
//			Color color = (Color) toBeRemoved.get(i);
//			// Removing from the display map must be done before disposing the
//			// color or else the comparison between this color and the one
//			// in the map will fail.
//			displays.remove(color);
//			// Dispose it
//			if (DEBUG) {
//				System.out.println("Disposing color " + color.toString()); //$NON-NLS-1$
//			}
//			color.dispose();
//		}
//		if (DEBUG) {
//			System.out.println("Display map is " + displays.toString()); //$NON-NLS-1$
//			System.out
//					.println("Color map is " + requiredFieldColorMap.toString()); //$NON-NLS-1$
//		}
//	}
//
//}
