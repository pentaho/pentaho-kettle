/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

/**
 * FieldDecorationRegistry is a common registry used to define shared field
 * decorations within an application. Unlike resource registries, the
 * FieldDecorationRegistry does not perform any lifecycle management of the
 * decorations.
 * </p>
 * <p>
 * Clients may specify images for the decorations in several different ways.
 * Images may be described by their image id in a specified
 * {@link ImageRegistry}. In this case, the life cycle of the image is managed
 * by the image registry, and the decoration registry will not attempt to obtain
 * an image from the image registry until the decoration is actually requested.
 * In cases where the client has access to an already-created image, the image
 * itself can be specified when registering the decoration. In this case, the
 * life cycle should be managed by the specifying client.
 * </p>
 * 
 * @see FieldDecoration
 * @see ImageRegistry
 * 
 * @since 1.0
 */
public class FieldDecorationRegistry implements Serializable {

	/**
	 * Decoration id for the decoration that should be used to cue the user that
	 * content proposals are available.
	 */
	public static final String DEC_CONTENT_PROPOSAL = "DEC_CONTENT_PROPOSAL"; //$NON-NLS-1$

	/**
	 * Decoration id for the decoration that should be used to cue the user that
	 * a field is required.
	 */
	public static final String DEC_REQUIRED = "DEC_REQUIRED"; //$NON-NLS-1$

	/**
	 * Decoration id for the decoration that should be used to cue the user that
	 * a field has an error.
	 */
	public static final String DEC_ERROR = "DEC_ERROR"; //$NON-NLS-1$

	/**
	 * Decoration id for the decoration that should be used to cue the user that
	 * a field has a warning.
	 */
	public static final String DEC_WARNING = "DEC_WARNING"; //$NON-NLS-1$

	/**
	 * Decoration id for the decoration that should be used to cue the user that
	 * a field has additional information.
	 * 
	 */
	public static final String DEC_INFORMATION = "DEC_INFORMATION"; //$NON-NLS-1$

	/**
	 * Decoration id for the decoration that should be used to cue the user that
	 * a field has an error with quick fix available.
	 * 
	 */
	public static final String DEC_ERROR_QUICKFIX = "DEC_ERRORQUICKFIX"; //$NON-NLS-1$

	/*
	 * Image id's
	 */
	private static final String IMG_DEC_FIELD_CONTENT_PROPOSAL = "org.eclipse.jface.fieldassist.IMG_DEC_FIELD_CONTENT_PROPOSAL"; //$NON-NLS-1$

	private static final String IMG_DEC_FIELD_REQUIRED = "org.eclipse.jface.fieldassist.IMG_DEC_FIELD_REQUIRED"; //$NON-NLS-1$

	private static final String IMG_DEC_FIELD_ERROR = "org.eclipse.jface.fieldassist.IMG_DEC_FIELD_ERROR"; //$NON-NLS-1$
	
	private static final String IMG_DEC_FIELD_ERROR_QUICKFIX = "org.eclipse.jface.fieldassist.IMG_DEC_FIELD_ERROR_QUICKFIX"; //$NON-NLS-1$

	private static final String IMG_DEC_FIELD_WARNING = "org.eclipse.jface.fieldassist.IMG_DEC_FIELD_WARNING"; //$NON-NLS-1$

	private static final String IMG_DEC_FIELD_INFO = "org.eclipse.jface.fieldassist.IMG_DEC_FIELD_INFO"; //$NON-NLS-1$

	/*
	 * Declare images and decorations immediately.
	 */
	static {
		ImageRegistry imageRegistry = JFaceResources.getImageRegistry();

		// Define the images used in the standard decorations.
		imageRegistry.put(IMG_DEC_FIELD_CONTENT_PROPOSAL, ImageDescriptor
				.createFromFile(FieldDecorationRegistry.class,
						"images/contassist_ovr.gif"));//$NON-NLS-1$
		imageRegistry.put(IMG_DEC_FIELD_ERROR, ImageDescriptor.createFromFile(
				FieldDecorationRegistry.class, "images/error_ovr.gif"));//$NON-NLS-1$

		imageRegistry.put(IMG_DEC_FIELD_WARNING, ImageDescriptor
				.createFromFile(FieldDecorationRegistry.class,
						"images/warn_ovr.gif"));//$NON-NLS-1$

		imageRegistry.put(IMG_DEC_FIELD_REQUIRED, ImageDescriptor
				.createFromFile(FieldDecorationRegistry.class,
						"images/required_field_cue.gif"));//$NON-NLS-1$	
		
		imageRegistry.put(IMG_DEC_FIELD_ERROR_QUICKFIX, ImageDescriptor
				.createFromFile(FieldDecorationRegistry.class,
						"images/errorqf_ovr.gif"));//$NON-NLS-1$
		
		imageRegistry.put(IMG_DEC_FIELD_INFO, ImageDescriptor
				.createFromFile(FieldDecorationRegistry.class,
						"images/info_ovr.gif"));//$NON-NLS-1$		

		// Define the standard decorations. Some do not have standard
		// descriptions. Use null in these cases.
		getDefault()
				.registerFieldDecoration(
						DEC_CONTENT_PROPOSAL,
						JFaceResources
								.getString("FieldDecorationRegistry.contentAssistMessage"), //$NON-NLS-1$
						IMG_DEC_FIELD_CONTENT_PROPOSAL, imageRegistry);

		getDefault().registerFieldDecoration(
				DEC_ERROR,
				JFaceResources
						.getString("FieldDecorationRegistry.errorMessage"), //$NON-NLS-1$
				IMG_DEC_FIELD_ERROR, imageRegistry);
		
		getDefault().registerFieldDecoration(
				DEC_ERROR_QUICKFIX,
				JFaceResources
						.getString("FieldDecorationRegistry.errorQuickFixMessage"), //$NON-NLS-1$
				IMG_DEC_FIELD_ERROR_QUICKFIX, imageRegistry);

		getDefault().registerFieldDecoration(DEC_WARNING, null,
				IMG_DEC_FIELD_WARNING, imageRegistry);
		
		getDefault().registerFieldDecoration(DEC_INFORMATION, null,
				IMG_DEC_FIELD_INFO, imageRegistry);

		getDefault()
				.registerFieldDecoration(
						DEC_REQUIRED,
						JFaceResources
								.getString("FieldDecorationRegistry.requiredFieldMessage"), //$NON-NLS-1$
						IMG_DEC_FIELD_REQUIRED, imageRegistry);

	}

	/*
	 * Data structure that holds onto the decoration image info and description,
	 * and can produce a decorator on request.
	 */
	class Entry {
		private String description;

		private String imageId;

		private ImageRegistry imageRegistry;

		private Image image;

		private FieldDecoration decoration;

		Entry(String description, String imageId, ImageRegistry registry) {
			this.description = description;
			this.imageId = imageId;
			this.imageRegistry = registry;
		}

		Entry(String description, Image image) {
			this.description = description;
			this.image = image;
		}

		FieldDecoration getDecoration() {
			if (decoration == null) {
				if (image == null) {
					if (imageRegistry == null) {
						imageRegistry = JFaceResources.getImageRegistry();
					}
					image = imageRegistry.get(imageId);
				}
				decoration = new FieldDecoration(image, description);
			}
			// Null out all other fields now that the decoration has an image
			description = null;
			imageId = null;
			imageRegistry = null;
			image = null;

			return decoration;
		}
	}

	/**
	 * Default instance of the registry. Applications may install their own
	 * registry.
	 */
	private static FieldDecorationRegistry defaultInstance;

	/**
	 * Maximum width and height used by decorations in this registry. Clients
	 * may use these values to reserve space in dialogs for decorations or to
	 * adjust layouts so that decorated and non-decorated fields line up.
	 */
	private int maxDecorationWidth = 0;
	private int maxDecorationHeight = 0;

	private HashMap /* <String id, FieldDecoration> */decorations = new HashMap();

	/**
	 * Get the default FieldDecorationRegistry.
	 * 
	 * @return the singleton FieldDecorationRegistry that is used to manage
	 *         shared field decorations.
	 */
	public static FieldDecorationRegistry getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new FieldDecorationRegistry();
		}
		return defaultInstance;
	}

	/**
	 * Set the default FieldDecorationRegistry.
	 * 
	 * @param defaultRegistry
	 *            the singleton FieldDecorationRegistry that is used to manage
	 *            shared field decorations.
	 */
	public static void setDefault(FieldDecorationRegistry defaultRegistry) {
		defaultInstance = defaultRegistry;
	}

	/**
	 * Construct a FieldDecorationRegistry.
	 */
	public FieldDecorationRegistry() {
		maxDecorationWidth = 0;
		maxDecorationHeight = 0;
	}

	/**
	 * Get the maximum width (in pixels) of any decoration retrieved so far in
	 * the registry. This value changes as decorations are added and retrieved.
	 * This value can be used by clients to reserve space or otherwise compute
	 * margins when aligning non-decorated fields with decorated fields.
	 * 
	 * @return the maximum width in pixels of any accessed decoration
	 */
	public int getMaximumDecorationWidth() {
		return maxDecorationWidth;
	}

	/**
	 * Get the maximum height (in pixels) of any decoration retrieved so far in
	 * the registry. This value changes as decorations are added and retrieved.
	 * This value can be used by clients to reserve space or otherwise compute
	 * margins when aligning non-decorated fields with decorated fields.
	 * 
	 * 
	 * @return the maximum height in pixels of any accessed decoration
	 */
	public int getMaximumDecorationHeight() {
		return maxDecorationHeight;
	}

	/**
	 * Registers a field decoration using the specified id. The lifecyle of the
	 * supplied image should be managed by the client. That is, it will never be
	 * disposed by this registry and the decoration should be removed from the
	 * registry if the image is ever disposed elsewhere.
	 * 
	 * @param id
	 *            the String id used to identify and access the decoration.
	 * @param description
	 *            the String description to be used in the decoration, or
	 *            <code>null</code> if the decoration has no description.
	 * @param image
	 *            the image to be used in the decoration
	 */
	public void registerFieldDecoration(String id, String description,
			Image image) {
		decorations.put(id, new Entry(description, image));
		// Recompute the maximums since this might be a replacement
		recomputeMaximums();
	}

	/**
	 * Registers a field decoration using the specified id. An image id of an
	 * image located in the default JFaceResources image registry is supplied.
	 * The image will not be created until the decoration is requested.
	 * 
	 * @param id
	 *            the String id used to identify and access the decoration.
	 * @param description
	 *            the String description to be used in the decoration, or
	 *            <code>null</code> if the decoration has no description. *
	 * @param imageId
	 *            the id of the image in the JFaceResources image registry that
	 *            is used for this decorator
	 */
	public void registerFieldDecoration(String id, String description,
			String imageId) {
		decorations.put(id, new Entry(description, imageId, JFaceResources
				.getImageRegistry()));
		// Recompute the maximums as this could be a replacement of a previous
		// image.
		recomputeMaximums();
	}

	/**
	 * Registers a field decoration using the specified id. An image id and an
	 * image registry are supplied. The image will not be created until the
	 * decoration is requested.
	 * 
	 * @param id
	 *            the String id used to identify and access the decoration.
	 * @param description
	 *            the String description to be used in the decoration, or
	 *            <code>null</code> if the decoration has no description. *
	 * @param imageId
	 *            the id of the image in the supplied image registry that is
	 *            used for this decorator
	 * @param imageRegistry
	 *            the registry used to obtain the image
	 */
	public void registerFieldDecoration(String id, String description,
			String imageId, ImageRegistry imageRegistry) {
		decorations.put(id, new Entry(description, imageId, imageRegistry));
		// Recompute the maximums since this could be a replacement
		recomputeMaximums();
	}

	/**
	 * Unregisters the field decoration with the specified id. No lifecycle
	 * management is performed on the decoration's image. This message has no
	 * effect if no field decoration with the specified id was previously
	 * registered.
	 * </p>
	 * <p>
	 * This method need not be called if the registered decoration's image is
	 * managed in an image registry. In that case, leaving the decoration in the
	 * registry will do no harm since the image will remain valid and will be
	 * properly disposed when the application is shut down. This method should
	 * be used in cases where the caller intends to dispose of the image
	 * referred to by the decoration, or otherwise determines that the
	 * decoration should no longer be used.
	 * 
	 * @param id
	 *            the String id of the decoration to be unregistered.
	 */
	public void unregisterFieldDecoration(String id) {
		decorations.remove(id);
		recomputeMaximums();
	}

	/**
	 * Returns the field decoration registered by the specified id .
	 * 
	 * @param id
	 *            the String id used to access the decoration.
	 * @return the FieldDecoration with the specified id, or <code>null</code>
	 *         if there is no decoration with the specified id.
	 */
	public FieldDecoration getFieldDecoration(String id) {
		Object entry = decorations.get(id);
		if (entry == null) {
			return null;
		}
		return ((Entry) entry).getDecoration();

	}

	/*
	 * The maximum decoration width and height must be recomputed. Typically
	 * called in response to adding, removing, or replacing a decoration.
	 */
	private void recomputeMaximums() {
		Iterator entries = decorations.values().iterator();
		
		maxDecorationHeight = 0;
		maxDecorationWidth = 0;
		while (entries.hasNext()) {
			Image image = ((Entry)entries.next()).getDecoration().getImage();
			if (image != null) {
				maxDecorationHeight = Math.max(maxDecorationHeight, image.getBounds().height);
				maxDecorationWidth = Math.max(maxDecorationWidth, image.getBounds().width);
			}
		}

	}
}
