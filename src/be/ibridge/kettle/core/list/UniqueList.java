package be.ibridge.kettle.core.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public interface UniqueList
{
    public boolean add(Object o) throws ObjectAlreadyExistsException;
    public void add(int index, Object element) throws ObjectAlreadyExistsException;
    public boolean addAll(Collection c) throws ObjectAlreadyExistsException;
    public boolean addAll(int index, Collection c) throws ObjectAlreadyExistsException;
    
    public void clear();
    public boolean contains(Object o);
    public boolean containsAll(Collection c);
    public Object get(int index);
    public int indexOf(Object o);
    public boolean isEmpty();
    public Iterator iterator();
    public int lastIndexOf(Object o);
    public ListIterator listIterator();
    public ListIterator listIterator(int index);
    public Object remove(int index);
    public boolean remove(Object o);
    public boolean removeAll(Collection c);
    public boolean retainAll(Collection c);
    public Object set(int index, Object element) throws ObjectAlreadyExistsException;
    public int size();
    public List subList(int fromIndex, int toIndex);
    public Object[] toArray();
    public Object[] toArray(Object[] a);
}
