package be.ibridge.kettle.core;

public final class Rectangle
{
    /**
     * the x coordinate of the rectangle
     */
    public int x;

    /**
     * the y coordinate of the rectangle
     */
    public int y;

    /**
     * the width of the rectangle
     */
    public int width;

    /**
     * the height of the rectangle
     */
    public int height;

    /**
     * Construct a new instance of this class given the x, y, width and height values.
     * 
     * @param x the x coordinate of the origin of the rectangle
     * @param y the y coordinate of the origin of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    public Rectangle(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns <code>true</code> if the point specified by the arguments is inside the area specified by the receiver,
     * and <code>false</code> otherwise.
     * 
     * @param x the x coordinate of the point to test for containment
     * @param y the y coordinate of the point to test for containment
     * @return <code>true</code> if the rectangle contains the point and <code>false</code> otherwise
     */
    public boolean contains(int x, int y)
    {
        return (x >= this.x) && (y >= this.y) && ((x - this.x) < width) && ((y - this.y) < height);
    }

    /**
     * Returns <code>true</code> if the given point is inside the area specified by the receiver, and
     * <code>false</code> otherwise.
     * 
     * @param pt the point to test for containment
     * @return <code>true</code> if the rectangle contains the point and <code>false</code> otherwise
     * 
     * @exception IllegalArgumentException
     * <ul>
     * <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     */
    public boolean contains(Point pt)
    {
        return contains(pt.x, pt.y);
    }

    /**
     * Compares the argument to the receiver, and returns true if they represent the <em>same</em> object using a
     * class specific comparison.
     * 
     * @param object the object to compare with this object
     * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
     * 
     * @see #hashCode()
     */
    public boolean equals(Object object)
    {
        if (object == this) return true;
        if (!(object instanceof Rectangle)) return false;
        Rectangle r = (Rectangle) object;
        return (r.x == this.x) && (r.y == this.y) && (r.width == this.width) && (r.height == this.height);
    }

    /**
     * Returns an integer hash code for the receiver. Any two objects which return <code>true</code> when passed to
     * <code>equals</code> must return the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals(Object)
     */
    public int hashCode()
    {
        return x ^ y ^ width ^ height;
    }

    /**
     * Destructively replaces the x, y, width and height values in the receiver with ones which represent the
     * intersection of the rectangles specified by the receiver and the given rectangle.
     * 
     * @param rect the rectangle to intersect with the receiver
     * 
     * @exception IllegalArgumentException
     * <ul>
     * <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     * 
     * since 3.0
     */
    public void intersect(Rectangle rect)
    {
        if (this == rect) return;
        int left = x > rect.x ? x : rect.x;
        int top = y > rect.y ? y : rect.y;
        int lhs = x + width;
        int rhs = rect.x + rect.width;
        int right = lhs < rhs ? lhs : rhs;
        lhs = y + height;
        rhs = rect.y + rect.height;
        int bottom = lhs < rhs ? lhs : rhs;
        x = right < left ? 0 : left;
        y = bottom < top ? 0 : top;
        width = right < left ? 0 : right - left;
        height = bottom < top ? 0 : bottom - top;
    }

    /**
     * Returns a new rectangle which represents the intersection of the receiver and the given rectangle.
     * <p>
     * The intersection of two rectangles is the rectangle that covers the area which is contained within both
     * rectangles.
     * </p>
     * 
     * @param rect the rectangle to intersect with the receiver
     * @return the intersection of the receiver and the argument
     * 
     * @exception IllegalArgumentException
     * <ul>
     * <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     */
    public Rectangle intersection(Rectangle rect)
    {
        if (this == rect) return new Rectangle(x, y, width, height);
        int left = x > rect.x ? x : rect.x;
        int top = y > rect.y ? y : rect.y;
        int lhs = x + width;
        int rhs = rect.x + rect.width;
        int right = lhs < rhs ? lhs : rhs;
        lhs = y + height;
        rhs = rect.y + rect.height;
        int bottom = lhs < rhs ? lhs : rhs;
        return new Rectangle(right < left ? 0 : left, bottom < top ? 0 : top, right < left ? 0 : right - left, bottom < top ? 0 : bottom - top);
    }

    /**
     * Returns <code>true</code> if the rectangle described by the arguments intersects with the receiver and
     * <code>false</code> otherwise.
     * <p>
     * Two rectangles intersect if the area of the rectangle representing their intersection is not empty.
     * </p>
     * 
     * @param x the x coordinate of the origin of the rectangle
     * @param y the y coordinate of the origin of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @return <code>true</code> if the rectangle intersects with the receiver, and <code>false</code> otherwise
     * 
     * @exception IllegalArgumentException
     * <ul>
     * <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     * 
     * @see #intersection(Rectangle)
     * @see #isEmpty()
     * 
     * @since 3.0
     */
    public boolean intersects(int x, int y, int width, int height)
    {
        return (x < this.x + this.width) && (y < this.y + this.height) && (x + width > this.x) && (y + height > this.y);
    }

    /**
     * Returns <code>true</code> if the given rectangle intersects with the receiver and <code>false</code>
     * otherwise.
     * <p>
     * Two rectangles intersect if the area of the rectangle representing their intersection is not empty.
     * </p>
     * 
     * @param rect the rectangle to test for intersection
     * @return <code>true</code> if the rectangle intersects with the receiver, and <code>false</code> otherwise
     * 
     * @exception IllegalArgumentException
     * <ul>
     * <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     * 
     * @see #intersection(Rectangle)
     * @see #isEmpty()
     */
    public boolean intersects(Rectangle rect)
    {
        return rect == this || intersects(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Returns <code>true</code> if the receiver does not cover any area in the (x, y) coordinate plane, and
     * <code>false</code> if the receiver does cover some area in the plane.
     * <p>
     * A rectangle is considered to <em>cover area</em> in the (x, y) coordinate plane if both its width and height
     * are non-zero.
     * </p>
     * 
     * @return <code>true</code> if the receiver is empty, and <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return (width <= 0) || (height <= 0);
    }

    /**
     * Returns a string containing a concise, human-readable description of the receiver.
     * 
     * @return a string representation of the rectangle
     */
    public String toString()
    {
        return "Rectangle {" + x + ", " + y + ", " + width + ", " + height + "}"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    /**
     * Returns a new rectangle which represents the union of the receiver and the given rectangle.
     * <p>
     * The union of two rectangles is the smallest single rectangle that completely covers both of the areas covered by
     * the two given rectangles.
     * </p>
     * 
     * @param rect the rectangle to perform union with
     * @return the union of the receiver and the argument
     * 
     * @exception IllegalArgumentException
     * <ul>
     * <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     * 
     */
    public Rectangle union(Rectangle rect)
    {
        int left = x < rect.x ? x : rect.x;
        int top = y < rect.y ? y : rect.y;
        int lhs = x + width;
        int rhs = rect.x + rect.width;
        int right = lhs > rhs ? lhs : rhs;
        lhs = y + height;
        rhs = rect.y + rect.height;
        int bottom = lhs > rhs ? lhs : rhs;
        return new Rectangle(left, top, right - left, bottom - top);
    }

}
