/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * Contains static methods for performing simple geometric operations
 * on the SWT geometry classes.
 *
 * @since 1.0
 */
public class Geometry {

    /**
     * Prevent this class from being instantiated.
     * 
     * @since 1.0
     */
    private Geometry() {
    	//This is not instantiated
    }

    /**
     * Returns the square of the distance between two points. 
     * <p>This is preferred over the real distance when searching
     * for the closest point, since it avoids square roots.</p>
     * 
     * @param p1 first endpoint
     * @param p2 second endpoint
     * @return the square of the distance between the two points
     * 
     * @since 1.0
     */
    public static int distanceSquared(Point p1, Point p2) {
        int term1 = p1.x - p2.x;
        int term2 = p1.y - p2.y;
        return term1 * term1 + term2 * term2;
    }

    /**
     * Returns the magnitude of the given 2d vector (represented as a Point)
     *  
     * @param p point representing the 2d vector whose magnitude is being computed
     * @return the magnitude of the given 2d vector
     * @since 1.0
     */
    public static double magnitude(Point p) {
        return Math.sqrt(magnitudeSquared(p));
    }

    /**
     * Returns the square of the magnitude of the given 2-space vector (represented
     * using a point)
     * 
     * @param p the point whose magnitude is being computed
     * @return the square of the magnitude of the given vector
     * @since 1.0
     */
    public static int magnitudeSquared(Point p) {
        return p.x * p.x + p.y * p.y;
    }

    /**
     * Returns the dot product of the given vectors (expressed as Points)
     * 
     * @param p1 the first vector
     * @param p2 the second vector
     * @return the dot product of the two vectors
     * @since 1.0
     */
    public static int dotProduct(Point p1, Point p2) {
        return p1.x * p2.x + p1.y * p2.y;
    }

    /**
     * Returns a new point whose coordinates are the minimum of the coordinates of the
     * given points
     * 
     * @param p1 a Point
     * @param p2 a Point
     * @return a new point whose coordinates are the minimum of the coordinates of the
     * given points
     * @since 1.0
     */
    public static Point min(Point p1, Point p2) {
        return new Point(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
    }

    /**
     * Returns a new point whose coordinates are the maximum of the coordinates
     * of the given points
     * @param p1 a Point
     * @param p2 a Point
     * @return point a new point whose coordinates are the maximum of the coordinates
     * @since 1.0
     */
    public static Point max(Point p1, Point p2) {
        return new Point(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
    }

    /**
     * Returns a vector in the given direction with the given
     * magnitude. Directions are given using SWT direction constants, and
     * the resulting vector is in the screen's coordinate system. That is,
     * the vector (0, 1) is down and the vector (1, 0) is right. 
     * 
     * @param distance magnitude of the vector
     * @param direction one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, or SWT.RIGHT
     * @return a point representing a vector in the given direction with the given magnitude
     * @since 1.0
     */
    public static Point getDirectionVector(int distance, int direction) {
        switch (direction) {
        case SWT.TOP:
            return new Point(0, -distance);
        case SWT.BOTTOM:
            return new Point(0, distance);
        case SWT.LEFT:
            return new Point(-distance, 0);
        case SWT.RIGHT:
            return new Point(distance, 0);
        }

        return new Point(0, 0);
    }

    /**
     * Returns the point in the center of the given rectangle.
     * 
     * @param rect rectangle being computed
     * @return a Point at the center of the given rectangle.
     * @since 1.0
     */
    public static Point centerPoint(Rectangle rect) {
        return new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
    }

    /**
     * Returns a copy of the given point
     * 
     * @param toCopy point to copy
     * @return a copy of the given point
     */
    public static Point copy(Point toCopy) {
        return new Point(toCopy.x, toCopy.y);
    }
    
    /**
     * Sets result equal to toCopy
     * 
     * @param result object that will be modified
     * @param toCopy object that will be copied
     * @since 1.0
     */
    public static void set(Point result, Point toCopy) {
    	result.x = toCopy.x;
    	result.y = toCopy.y;
    }
    
    /**
     * Sets result equal to toCopy
     * 
     * @param result object that will be modified
     * @param toCopy object that will be copied
     * @since 1.0
     */
    public static void set(Rectangle result, Rectangle toCopy) {
    	result.x = toCopy.x;
    	result.y = toCopy.y;
    	result.width = toCopy.width;
    	result.height = toCopy.height;
    }

    /**
     * <p>Returns a new difference Rectangle whose x, y, width, and height are equal to the difference of the corresponding
     * attributes from the given rectangles</p>
     * 
     * <p></p>
     * <b>Example: Compute the margins for a given Composite, and apply those same margins to a new GridLayout</b>
     * 
     * <code><pre>      
     *      // Compute the client area, in the coordinate system of the input composite's parent  
     *      Rectangle clientArea = Display.getCurrent().map(inputComposite, 
     *      	inputComposite.getParent(), inputComposite.getClientArea());
     *      
     *      // Compute the margins for a given Composite by subtracting the client area from the composite's bounds
     *      Rectangle margins = Geometry.subtract(inputComposite.getBounds(), clientArea);
     *      
     *      // Now apply these margins to a new GridLayout
     *      GridLayout layout = GridLayoutFactory.fillDefaults().margins(margins).create();
     * </pre></code>
     * 
     * @param rect1 first rectangle
     * @param rect2 rectangle to subtract
     * @return the difference between the two rectangles (computed as rect1 - rect2)
     * @since 1.0
     */
    public static Rectangle subtract(Rectangle rect1, Rectangle rect2) {
    	return new Rectangle(rect1.x - rect2.x, rect1.y - rect2.y, rect1.width - rect2.width, rect1.height - rect2.height);
    }
    
    /**
     * <p>Returns a new Rectangle whose x, y, width, and height is the sum of the x, y, width, and height values of 
     * both rectangles respectively.</p>
     * 
     * @param rect1 first rectangle to add
     * @param rect2 second rectangle to add
     * @return a new rectangle whose x, y, height, and width attributes are the sum of the corresponding attributes from
     *         the arguments.
     * @since 1.0
     */
    public static Rectangle add(Rectangle rect1, Rectangle rect2) {
    	return new Rectangle(rect1.x + rect2.x, rect1.y + rect2.y, 
    			rect1.width + rect2.width, rect1.height + rect2.height);
    }
    
    /**
     * Adds two points as 2d vectors. Returns a new point whose coordinates are
     * the sum of the original two points.
     * 
     * @param point1 the first point (not null)
     * @param point2 the second point (not null)
     * @return a new point whose coordinates are the sum of the given points
     * @since 1.0
     */
    public static Point add(Point point1, Point point2) {
        return new Point(point1.x + point2.x, point1.y + point2.y);
    }
    
    /**
     * Divides both coordinates of the given point by the given scalar. 
     * 
     * @since 1.0 
     *
     * @param toDivide point to divide
     * @param scalar denominator
     * @return a new Point whose coordinates are equal to the original point divided by the scalar
     */
    public static Point divide(Point toDivide, int scalar) {
        return new Point(toDivide.x / scalar, toDivide.y / scalar);
    }
    

    /**
     * Performs vector subtraction on two points. Returns a new point equal to
     * (point1 - point2).
     * 
     * @param point1 initial point
     * @param point2 vector to subtract
     * @return the difference (point1 - point2)
     * @since 1.0
     */
    public static Point subtract(Point point1, Point point2) {
        return new Point(point1.x - point2.x, point1.y - point2.y);
    }

    /**
     * Swaps the X and Y coordinates of the given point.
     * 
     * @param toFlip modifies this point
     * @since 1.0
     */
    public static void flipXY(Point toFlip) {
    	int temp = toFlip.x;
    	toFlip.x = toFlip.y;
    	toFlip.y = temp;
    }

    /**
     * Swaps the X and Y coordinates of the given rectangle, along with the height and width.
     * 
     * @param toFlip modifies this rectangle
     * @since 1.0
     */
    public static void flipXY(Rectangle toFlip) {
    	int temp = toFlip.x;
    	toFlip.x = toFlip.y;
    	toFlip.y = temp;
    	
    	temp = toFlip.width;
    	toFlip.width = toFlip.height;
    	toFlip.height = temp;
    }
    
    /**
     * Returns the height or width of the given rectangle.
     * 
     * @param toMeasure rectangle to measure
     * @param width returns the width if true, and the height if false
     * @return the width or height of the given rectangle
     * @since 1.0
     */
    public static int getDimension(Rectangle toMeasure, boolean width) {
        if (width) {
            return toMeasure.width;
        }
		return toMeasure.height;
    }

    /**
     * Returns the x or y coordinates of the given point.
     * 
     * @param toMeasure point being measured
     * @param width if true, returns x. Otherwise, returns y.
     * @return the x or y coordinate
     * @since 1.0
     */
    public static int getCoordinate(Point toMeasure, boolean width) {
    	return width ? toMeasure.x : toMeasure.y;
    }
    
    /**
     * Returns the x or y coordinates of the given rectangle.
     * 
     * @param toMeasure rectangle being measured
     * @param width if true, returns x. Otherwise, returns y.
     * @return the x or y coordinate
     * @since 1.0
     */
    public static int getCoordinate(Rectangle toMeasure, boolean width) {
    	return width ? toMeasure.x : toMeasure.y;
    }
    
    /**
     * Sets one dimension of the given rectangle. Modifies the given rectangle.
     * 
     * @param toSet rectangle to modify
     * @param width if true, the width is modified. If false, the height is modified.
     * @param newCoordinate new value of the width or height
     * @since 1.0
     */
    public static void setDimension(Rectangle toSet, boolean width, int newCoordinate) {
    	if (width) {
    		toSet.width = newCoordinate;
    	} else {
    		toSet.height = newCoordinate;
    	}
    }

    /**
     * Sets one coordinate of the given rectangle. Modifies the given rectangle.
     * 
     * @param toSet rectangle to modify
     * @param width if true, the x coordinate is modified. If false, the y coordinate is modified.
     * @param newCoordinate new value of the x or y coordinates
     * @since 1.0
     */
    public static void setCoordinate(Rectangle toSet, boolean width, int newCoordinate) {
    	if (width) {
    		toSet.x = newCoordinate;
    	} else {
    		toSet.y = newCoordinate;
    	}
    }
    
    /**
     * Sets one coordinate of the given point. Modifies the given point.
     * 
     * @param toSet point to modify
     * @param width if true, the x coordinate is modified. If false, the y coordinate is modified.
     * @param newCoordinate new value of the x or y coordinates
     * @since 1.0
     */
    public static void setCoordinate(Point toSet, boolean width, int newCoordinate) {
    	if (width) {
    		toSet.x = newCoordinate;
    	} else {
    		toSet.y = newCoordinate;
    	}
    }
    
    /**
     * Returns the distance of the given point from a particular side of the given rectangle.
     * Returns negative values for points outside the rectangle.
     * 
     * @param rectangle a bounding rectangle
     * @param testPoint a point to test
     * @param edgeOfInterest side of the rectangle to test against
     * @return the distance of the given point from the given edge of the rectangle
     * @since 1.0
     */
    public static int getDistanceFromEdge(Rectangle rectangle, Point testPoint,
            int edgeOfInterest) {
        switch (edgeOfInterest) {
        case SWT.TOP:
            return testPoint.y - rectangle.y;
        case SWT.BOTTOM:
            return rectangle.y + rectangle.height - testPoint.y;
        case SWT.LEFT:
            return testPoint.x - rectangle.x;
        case SWT.RIGHT:
            return rectangle.x + rectangle.width - testPoint.x;
        }

        return 0;
    }

    /**
     * Extrudes the given edge inward by the given distance. That is, if one side of the rectangle
     * was sliced off with a given thickness, this returns the rectangle that forms the slice. Note
     * that the returned rectangle will be inside the given rectangle if size > 0.
     * 
     * @param toExtrude the rectangle to extrude. The resulting rectangle will share three sides
     * with this rectangle.
     * @param size distance to extrude. A negative size will extrude outwards (that is, the resulting
     * rectangle will overlap the original iff this is positive). 
     * @param orientation the side to extrude.  One of SWT.LEFT, SWT.RIGHT, SWT.TOP, or SWT.BOTTOM. The 
     * resulting rectangle will always share this side with the original rectangle.
     * @return a rectangle formed by extruding the given side of the rectangle by the given distance.
     * @since 1.0
     */
    public static Rectangle getExtrudedEdge(Rectangle toExtrude, int size,
            int orientation) {
        Rectangle bounds = new Rectangle(toExtrude.x, toExtrude.y,
                toExtrude.width, toExtrude.height);

        if (!isHorizontal(orientation)) {
            bounds.width = size;
        } else {
            bounds.height = size;
        }

        switch (orientation) {
        case SWT.RIGHT:
            bounds.x = toExtrude.x + toExtrude.width - bounds.width;
            break;
        case SWT.BOTTOM:
            bounds.y = toExtrude.y + toExtrude.height - bounds.height;
            break;
        }

        normalize(bounds);

        return bounds;
    }

    /**
     * Returns the opposite of the given direction. That is, returns SWT.LEFT if
     * given SWT.RIGHT and visa-versa.
     * 
     * @param swtDirectionConstant one of SWT.LEFT, SWT.RIGHT, SWT.TOP, or SWT.BOTTOM
     * @return one of SWT.LEFT, SWT.RIGHT, SWT.TOP, or SWT.BOTTOM
     * @since 1.0
     */
    public static int getOppositeSide(int swtDirectionConstant) {
        switch (swtDirectionConstant) {
        case SWT.TOP:
            return SWT.BOTTOM;
        case SWT.BOTTOM:
            return SWT.TOP;
        case SWT.LEFT:
            return SWT.RIGHT;
        case SWT.RIGHT:
            return SWT.LEFT;
        }

        return swtDirectionConstant;
    }

    /**
     * Converts the given boolean into an SWT orientation constant.
     * 
     * @param horizontal if true, returns SWT.HORIZONTAL. If false, returns SWT.VERTICAL 
     * @return SWT.HORIZONTAL or SWT.VERTICAL.
     * @since 1.0
     */
    public static int getSwtHorizontalOrVerticalConstant(boolean horizontal) {
        if (horizontal) {
            return SWT.HORIZONTAL;
        }
		return SWT.VERTICAL;
    }

    /**
     * Returns true iff the given SWT side constant corresponds to a horizontal side
     * of a rectangle. That is, returns true for the top and bottom but false for the
     * left and right.
     * 
     * @param swtSideConstant one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, or SWT.RIGHT
     * @return true iff the given side is horizontal.
     * @since 1.0
     */
    public static boolean isHorizontal(int swtSideConstant) {
        return !(swtSideConstant == SWT.LEFT || swtSideConstant == SWT.RIGHT);
    }

    /**
     * Moves the given rectangle by the given delta.
     * 
     * @param rect rectangle to move (will be modified)
     * @param delta direction vector to move the rectangle by
     * @since 1.0
     */
    public static void moveRectangle(Rectangle rect, Point delta) {
        rect.x += delta.x;
        rect.y += delta.y;
    }

    /**
     * Moves each edge of the given rectangle outward by the given amount. Negative values
     * cause the rectangle to contract. Does not allow the rectangle's width or height to be
     * reduced below zero.
     *  
     * @param rect normalized rectangle to modify
     * @param differenceRect difference rectangle to be added to rect
     * @since 1.0
     */
    public static void expand(Rectangle rect, Rectangle differenceRect) {
    	rect.x += differenceRect.x;
    	rect.y += differenceRect.y;
    	rect.height = Math.max(0, rect.height + differenceRect.height);
    	rect.width = Math.max(0, rect.width + differenceRect.width);
    }
    
    /**
     * <p>Returns a rectangle which, when added to another rectangle, will expand each side
     * by the given number of units.</p>
     * 
     * <p>This is commonly used to store margin sizes. For example:</p>
     * 
     * <code><pre>
     *     // Expands the left, right, top, and bottom 
     *     // of the given control by 10, 5, 1, and 15 units respectively
     *      
     *     Rectangle margins = Geometry.createDifferenceRect(10,5,1,15);
     *     Rectangle bounds = someControl.getBounds();
     *     someControl.setBounds(Geometry.add(bounds, margins));
     * </pre></code>
     * 
     * @param left distance to expand the left side (negative values move the edge inward)
     * @param right distance to expand the right side (negative values move the edge inward)
     * @param top distance to expand the top (negative values move the edge inward)
     * @param bottom distance to expand the bottom (negative values move the edge inward)
     * 
     * @return a difference rectangle that, when added to another rectangle, will cause each
     * side to expand by the given number of units
     * @since 1.0
     */
    public static Rectangle createDiffRectangle(int left, int right, int top, int bottom) {
    	return new Rectangle(-left, -top, left + right, top + bottom);
    }
    
    /**
     * Moves each edge of the given rectangle outward by the given amount. Negative values
     * cause the rectangle to contract. Does not allow the rectangle's width or height to be
     * reduced below zero.
     *  
     * @param rect normalized rectangle to modify
     * @param left distance to move the left edge outward (negative values move the edge inward)
     * @param right distance to move the right edge outward (negative values move the edge inward) 
     * @param top distance to move the top edge outward (negative values move the edge inward)
     * @param bottom distance to move the bottom edge outward (negative values move the edge inward)
     * @since 1.0
     */
    public static void expand(Rectangle rect, int left, int right, int top, int bottom) {
        rect.x -= left;
        rect.width = Math.max(0, rect.width + left + right);
        rect.y -= top;
        rect.height = Math.max(0, rect.height + top + bottom);
    }
    
    /**
     * Normalizes the given rectangle. That is, any rectangle with
     * negative width or height becomes a rectangle with positive
     * width or height that extends to the upper-left of the original
     * rectangle. 
     * 
     * @param rect rectangle to modify
     * @since 1.0
     */
    public static void normalize(Rectangle rect) {
        if (rect.width < 0) {
            rect.width = -rect.width;
            rect.x -= rect.width;
        }

        if (rect.height < 0) {
            rect.height = -rect.height;
            rect.y -= rect.height;
        }
    }

    /**
     * Converts the given rectangle from display coordinates to the local coordinate system 
     * of the given object into display coordinates.
     * 
     * @param coordinateSystem local coordinate system being converted to
     * @param toConvert rectangle to convert
     * @return a rectangle in control coordinates
     * @since 1.0
     */
    public static Rectangle toControl(Control coordinateSystem,
            Rectangle toConvert) {
    	return(coordinateSystem.getDisplay().map
    			(null,coordinateSystem,toConvert));
    }

    /**
     * Converts the given rectangle from the local coordinate system of the given object
     * into display coordinates.
     * 
     * @param coordinateSystem local coordinate system being converted from
     * @param toConvert rectangle to convert
     * @return a rectangle in display coordinates
     * @since 1.0
     */
    public static Rectangle toDisplay(Control coordinateSystem,
            Rectangle toConvert) {
    	return(coordinateSystem.getDisplay().map
    			(coordinateSystem,null,toConvert));   

    }

    /**
     * Determines where the given point lies with respect to the given rectangle.
     * Returns a combination of SWT.LEFT, SWT.RIGHT, SWT.TOP, and SWT.BOTTOM, combined
     * with bitwise or (for example, returns SWT.TOP | SWT.LEFT if the point is to the
     * upper-left of the rectangle). Returns 0 if the point lies within the rectangle.
     * Positions are in screen coordinates (ie: a point is to the upper-left of the
     * rectangle if its x and y coordinates are smaller than any point in the rectangle)
     *  
     * @param boundary normalized boundary rectangle 
     * @param toTest point whose relative position to the rectangle is being computed
     * @return one of SWT.LEFT | SWT.TOP, SWT.TOP, SWT.RIGHT | SWT.TOP, SWT.LEFT, 0,
     * SWT.RIGHT, SWT.LEFT | SWT.BOTTOM, SWT.BOTTOM, SWT.RIGHT | SWT.BOTTOM
     * @since 1.0
     */
    public static int getRelativePosition(Rectangle boundary, Point toTest) {
        int result = 0;

        if (toTest.x < boundary.x) {
            result |= SWT.LEFT;
        } else if (toTest.x >= boundary.x + boundary.width) {
            result |= SWT.RIGHT;
        }

        if (toTest.y < boundary.y) {
            result |= SWT.TOP;
        } else if (toTest.y >= boundary.y + boundary.height) {
            result |= SWT.BOTTOM;
        }

        return result;
    }

    /**
     * Returns the distance from the point to the nearest edge of the given
     * rectangle. Returns negative values if the point lies outside the rectangle.
     * 
     * @param boundary rectangle to test
     * @param toTest point to test
     * @return the distance between the given point and the nearest edge of the rectangle.
     * Returns positive values for points inside the rectangle and negative values for points
     * outside the rectangle.
     * @since 1.0
     */
    public static int getDistanceFrom(Rectangle boundary, Point toTest) {
        int side = getClosestSide(boundary, toTest);
        return getDistanceFromEdge(boundary, toTest, side);
    }
    
    /**
     * Returns the edge of the given rectangle is closest to the given
     * point.
     * 
     * @param boundary rectangle to test
     * @param toTest point to compare
     * @return one of SWT.LEFT, SWT.RIGHT, SWT.TOP, or SWT.BOTTOM
     * 
     * @since 1.0
     */
    public static int getClosestSide(Rectangle boundary, Point toTest) {
        int[] sides = new int[] { SWT.LEFT, SWT.RIGHT, SWT.TOP, SWT.BOTTOM };

        int closestSide = SWT.LEFT;
        int closestDistance = Integer.MAX_VALUE;

        for (int idx = 0; idx < sides.length; idx++) {
            int side = sides[idx];

            int distance = getDistanceFromEdge(boundary, toTest, side);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestSide = side;
            }
        }

        return closestSide;
    }

    /**
     * Returns a copy of the given rectangle
     * 
     * @param toCopy rectangle to copy
     * @return a copy of the given rectangle
     * @since 1.0
     */
    public static Rectangle copy(Rectangle toCopy) {
        return new Rectangle(toCopy.x, toCopy.y, toCopy.width, toCopy.height);
    }

    /**
     * Returns the size of the rectangle, as a Point
     * 
     * @param rectangle rectangle whose size is being computed
     * @return the size of the given rectangle
     * @since 1.0
     */
    public static Point getSize(Rectangle rectangle) {
        return new Point(rectangle.width, rectangle.height);
    }

    /**
     * Sets the size of the given rectangle to the given size
     * 
     * @param rectangle rectangle to modify
     * @param newSize new size of the rectangle
     * @since 1.0
     */
    public static void setSize(Rectangle rectangle, Point newSize) {
        rectangle.width = newSize.x;
        rectangle.height = newSize.y;
    }

    /**
     * Sets the x,y position of the given rectangle. For a normalized
     * rectangle (a rectangle with positive width and height), this will
     * be the upper-left corner of the rectangle. 
     * 
     * @param rectangle rectangle to modify
     * @param newLocation new location of the rectangle
     * 
     * @since 1.0
     */
    public static void setLocation(Rectangle rectangle, Point newLocation) {
        rectangle.x = newLocation.x;
        rectangle.y = newLocation.y;
    }

    /**
     * Returns the x,y position of the given rectangle. For normalized rectangles
     * (rectangles with positive width and height), this is the upper-left
     * corner of the rectangle.
     * 
     * @param toQuery rectangle to query
     * @return a Point containing the x,y position of the rectangle
     * 
     * @since 1.0
     */
    public static Point getLocation(Rectangle toQuery) {
        return new Point(toQuery.x, toQuery.y);
    }

    /**
     * Returns a new rectangle with the given position and dimensions, expressed
     * as points.
     * 
     * @param position the (x,y) position of the rectangle
     * @param size the size of the new rectangle, where (x,y) -> (width, height)
     * @return a new Rectangle with the given position and size
     * 
     * @since 1.0
     */
    public static Rectangle createRectangle(Point position, Point size) {
        return new Rectangle(position.x, position.y, size.x, size.y);
    }
    
    /**
	 * Repositions the 'inner' rectangle to lie completely within the bounds of the 'outer'
	 * rectangle if possible. One use for this is to ensure that, when setting a control's bounds,
	 * that they will always lie within its parent's client area (to avoid clipping).
	 * 
	 * @param inner The 'inner' rectangle to be repositioned (should be smaller than the 'outer' rectangle)
	 * @param outer The 'outer' rectangle
	 */
	public static void moveInside(Rectangle inner, Rectangle outer) {
		// adjust X
		if (inner.x < outer.x) {
			inner.x = outer.x;
		}
		if ((inner.x + inner.width) > (outer.x + outer.width)) {
			inner.x -= (inner.x + inner.width) - (outer.x + outer.width);
		}

		// Adjust Y
		if (inner.y < outer.y) {
			inner.y = outer.y;
		}
		if ((inner.y + inner.height) > (outer.y + outer.height)) {
			inner.y -= (inner.y + inner.height) - (outer.y + outer.height);
		}
	}
    
}
