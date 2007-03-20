package be.ibridge.kettle.core.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * We want to enforce uniqueness in the list.
 * The objects stored added need to have the "equals" implemented
 * 
 * @author Matt
 * @since  2007-03-19
 *
 */
public class UniqueArrayList implements UniqueList
{
    private List list;
    
    private static final long serialVersionUID = -4032535311575763475L;
    
    public UniqueArrayList()
    {
        this.list = new ArrayList();
    }
    
    /**
     * @param c
     */
    public UniqueArrayList(Collection c)
    {
        this.list = new ArrayList(c);
    }

    /**
     * @param initialCapacity
     */
    public UniqueArrayList(int initialCapacity)
    {
        this.list = new ArrayList(initialCapacity);
    }

    public boolean add(Object o) throws ObjectAlreadyExistsException
    {
        if (list.contains(o)) throw new ObjectAlreadyExistsException();
        return list.add(o);
    }

    public void add(int index, Object element) throws ObjectAlreadyExistsException
    {
        if (list.contains(element)) throw new ObjectAlreadyExistsException();
        list.add(index, element);
        
    }

    public boolean addAll(Collection c) throws ObjectAlreadyExistsException
    {
        for (Iterator iter = c.iterator(); iter.hasNext();)
        {
            Object element = (Object) iter.next();
            if (list.contains(element))  throw new ObjectAlreadyExistsException();
        }
        return list.addAll(c);
    }

    public boolean addAll(int index, Collection c) throws ObjectAlreadyExistsException
    {
        for (Iterator iter = c.iterator(); iter.hasNext();)
        {
            Object element = (Object) iter.next();
            if (list.contains(element))  throw new ObjectAlreadyExistsException();
        }
        return list.addAll(index, c);
    }

    public void clear()
    {
        list.clear();
    }

    public boolean contains(Object o)
    {
        return list.contains(o);
    }

    public boolean containsAll(Collection c)
    {
        return list.containsAll(c);
    }

    public Object get(int index)
    {
        return list.get(index);
    }

    public int indexOf(Object o)
    {
        return list.indexOf(o);
    }

    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    public Iterator iterator()
    {
        return list.iterator();
    }

    public int lastIndexOf(Object o)
    {
        return list.lastIndexOf(o);
    }

    public ListIterator listIterator()
    {
        return list.listIterator();
    }

    public ListIterator listIterator(int index)
    {
        return list.listIterator(index);
    }

    public Object remove(int index)
    {
        return list.remove(index);
    }

    public boolean remove(Object o)
    {
        return list.remove(o);
    }

    public boolean removeAll(Collection c)
    {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection c)
    {
        return list.retainAll(c);
    }

    public Object set(int index, Object element) throws ObjectAlreadyExistsException
    {
        return list.set(index, element);
    }

    public int size()
    {
        return list.size();
    }

    public List subList(int fromIndex, int toIndex)
    {
        return list.subList(fromIndex, toIndex);
    }

    public Object[] toArray()
    {
        return list.toArray();
    }

    public Object[] toArray(Object[] a)
    {
        return list.toArray(a);
    }

    public List getList()
    {
        return list;
    }
}
