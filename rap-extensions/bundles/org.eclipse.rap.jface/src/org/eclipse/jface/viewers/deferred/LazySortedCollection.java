/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers.deferred;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;

/**
 * This object maintains a collection of elements, sorted by a comparator
 * given in the constructor. The collection is lazily sorted, allowing 
 * more efficient runtimes for most methods. There are several methods on this
 * object that allow objects to be queried by their position in the sorted
 * collection.
 * 
 * <p>
 * This is a modified binary search tree. Each subtree has a value, a left and right subtree, 
 * a count of the number of children, and a set of unsorted children. 
 * Insertion happens lazily. When a new node N is inserted into a subtree T, it is initially 
 * added to the set of unsorted children for T without actually comparing it with the value for T. 
 * </p>
 * <p>
 * The unsorted children will remain in the unsorted set until some subsequent operation requires
 * us to know the exact set of elements in one of the subtrees. At that time, we partition
 * T by comparing all of its unsorted children with T's value and moving them into the left 
 * or right subtrees.
 * </p>
 * 
 * @since 1.0
 */
public class LazySortedCollection implements Serializable {
    private final int MIN_CAPACITY = 8;
    private Object[] contents = new Object[MIN_CAPACITY];
    private int[] leftSubTree = new int[MIN_CAPACITY];
    private int[] rightSubTree = new int[MIN_CAPACITY];
    private int[] nextUnsorted = new int[MIN_CAPACITY];
    private int[] treeSize = new int[MIN_CAPACITY];
    private int[] parentTree = new int[MIN_CAPACITY];
    private int root = -1;
    private int lastNode = 0;
    private int firstUnusedNode = -1;
    
    private static final float loadFactor = 0.75f;
    
    private IntHashMap objectIndices;
    private Comparator comparator;
    private static int counter = 0;
    
    /**
     * Disables randomization and enables additional runtime error checking.
     * Severely degrades performance if set to true. Intended for use in test 
     * suites only.
     */
    public boolean enableDebug = false;
    
    // This object is inserted as the value into any node scheduled for lazy removal
    private Object lazyRemovalFlag = new Object() {
        public String toString() {
            return "Lazy removal flag";  //$NON-NLS-1$
        }
    };
    
    private final static int DIR_LEFT = 0;
    private final static int DIR_RIGHT = 1;
    private final static int DIR_UNSORTED = 2;
    
    // Direction constants indicating root nodes
    private final static int DIR_ROOT = 3;
    private final static int DIR_UNUSED = 4;
       
    private final class Edge {
        private int startNode;
        private int direction;
        
        private Edge() {
            startNode = -1;
            direction = -1;
        }
        
        private Edge(int node, int dir) {
            startNode = node;
            direction = dir;
        }
        
        private int getStart() {
            return startNode;
        }
        
        private int getTarget() {
            if (startNode == -1) {
                if (direction == DIR_UNSORTED) {
                    return firstUnusedNode;
                } else if (direction == DIR_ROOT) {
                    return root;
                }
                return -1;
            }
            
            if (direction == DIR_LEFT) {
                return leftSubTree[startNode];
            }
            if (direction == DIR_RIGHT) {
                return rightSubTree[startNode];
            }
            return nextUnsorted[startNode];
        }
        
        private boolean isNull() {
            return getTarget() == -1;
        }
     
        /**
         * Redirects this edge to a new node
         * @param newNode
         * @since 1.0
         */
        private void setTarget(int newNode) {            
            if (direction == DIR_LEFT) {
    	        leftSubTree[startNode] = newNode;
            } else if (direction == DIR_RIGHT) {
                rightSubTree[startNode] = newNode;
            } else if (direction == DIR_UNSORTED) {
                nextUnsorted[startNode] = newNode;
            } else if (direction == DIR_ROOT) {
                root = newNode;
            } else if (direction == DIR_UNUSED) {
                firstUnusedNode = newNode;
            }
            
	        if (newNode != -1) {
	            parentTree[newNode] = startNode;
	        }
        }
        
        private void advance(int direction) {
            startNode = getTarget();
            this.direction = direction;
        }
    }

    private void setRootNode(int node) {
        root = node;
        if (node != -1) {
            parentTree[node] = -1;
        }
    }
    
    /**
     * Creates a new sorted collection using the given comparator to determine
     * sort order.
     * 
     * @param c comparator that determines the sort order
     */
    public LazySortedCollection(Comparator c) {
        this.comparator = c;
    }
    
    /**
     * Tests if this object's internal state is valid. Throws a runtime
     * exception if the state is invalid, indicating a programming error
     * in this class. This method is intended for use in test
     * suites and should not be called by clients.
     */
    public void testInvariants() {
        if (!enableDebug) {
            return;
        }
        
        testInvariants(root);
    }
    
    private void testInvariants(int node) {
        if (node == -1) {
            return;
        }
        
        // Get the current tree size (we will later force the tree size
        // to be recomputed from scratch -- if everything works properly, then
        // there should be no change.
        int treeSize = getSubtreeSize(node);

        int left = leftSubTree[node];
        int right = rightSubTree[node];
        int unsorted = nextUnsorted[node];
        
        if (isUnsorted(node)) {
            Assert.isTrue(left == -1, "unsorted nodes shouldn't have a left subtree"); //$NON-NLS-1$
            Assert.isTrue(right == -1, "unsorted nodes shouldn't have a right subtree"); //$NON-NLS-1$
        }
        
        if (left != -1) {
            testInvariants(left);
            Assert.isTrue(parentTree[left] == node, "left node has invalid parent pointer"); //$NON-NLS-1$
        }
        if (right != -1) {
            testInvariants(right);
            Assert.isTrue(parentTree[right] == node, "right node has invalid parent pointer");             //$NON-NLS-1$
        }

        int previous = node;
        while (unsorted != -1) {
            int oldTreeSize = this.treeSize[unsorted];
            recomputeTreeSize(unsorted);
            
            Assert.isTrue(this.treeSize[unsorted] == oldTreeSize, 
                    "Invalid node size for unsorted node"); //$NON-NLS-1$
            Assert.isTrue(leftSubTree[unsorted] == -1, "unsorted nodes shouldn't have left subtrees"); //$NON-NLS-1$
            Assert.isTrue(rightSubTree[unsorted] == -1, "unsorted nodes shouldn't have right subtrees"); //$NON-NLS-1$
            Assert.isTrue(parentTree[unsorted] == previous, "unsorted node has invalid parent pointer"); //$NON-NLS-1$
            Assert.isTrue(contents[unsorted] != lazyRemovalFlag, "unsorted nodes should not be lazily removed"); //$NON-NLS-1$
            previous = unsorted;
            unsorted = nextUnsorted[unsorted];
        }
        
        // Note that we've already tested that the child sizes are correct... if our size is
        // correct, then recomputing it now should not cause any change.
        recomputeTreeSize(node);
                
        Assert.isTrue(treeSize == getSubtreeSize(node), "invalid tree size"); //$NON-NLS-1$
    }
    
    private boolean isUnsorted(int node) {
        int parent = parentTree[node];
        
        if (parent != -1) {
            return nextUnsorted[parent] == node;
        }
        
        return false;
    }
    
    private final boolean isLess(int element1, int element2) {
        return comparator.compare(contents[element1], contents[element2]) < 0;
    }
    
    /**
     * Adds the given element to the given subtree. Returns the new
     * root of the subtree.
     * 
     * @param subTree index of the subtree to insert elementToAdd into. If -1, 
     *                then a new subtree will be created for elementToAdd
     * @param elementToAdd index of the element to add to the subtree. If -1, this method
     *                 is a NOP.
     * @since 1.0
     */
    private final int addUnsorted(int subTree, int elementToAdd) {
        if (elementToAdd == -1) {
            return subTree;
        }
        
        if (subTree == -1) {
            nextUnsorted[elementToAdd] = -1;
            treeSize[elementToAdd] = 1;
            return elementToAdd;
        }
        
        // If the subTree is empty (ie: it only contains nodes flagged for lazy removal),
        // chop it off.
        if (treeSize[subTree] == 0) {
            removeSubTree(subTree);
            nextUnsorted[elementToAdd] = -1;
            treeSize[elementToAdd] = 1;
            return elementToAdd;
        }
        
        // If neither subtree has any children, add a pseudorandom chance of the
        // newly added element becoming the new pivot for this node. Note: instead
        // of a real pseudorandom generator, we simply use a counter here.
        if (!enableDebug && leftSubTree[subTree] == -1 && rightSubTree[subTree] == -1 
                && leftSubTree[elementToAdd] == -1 && rightSubTree[elementToAdd] == -1) {
	        counter--;
	        
	        if (counter % treeSize[subTree] == 0) {
	            // Make the new node into the new pivot 
	            nextUnsorted[elementToAdd] = subTree;
	            parentTree[elementToAdd] = parentTree[subTree];
	            parentTree[subTree] = elementToAdd;
	            treeSize[elementToAdd] = treeSize[subTree] + 1;
	            return elementToAdd;
	        }
        }
        
        int oldNextUnsorted = nextUnsorted[subTree];
        nextUnsorted[elementToAdd] = oldNextUnsorted;
        
        if (oldNextUnsorted == -1) {
            treeSize[elementToAdd] = 1;
        } else {
            treeSize[elementToAdd] = treeSize[oldNextUnsorted] + 1;
            parentTree[oldNextUnsorted] = elementToAdd;
        }
        
        parentTree[elementToAdd] = subTree;
        
        nextUnsorted[subTree] = elementToAdd;
        treeSize[subTree]++;        
        return subTree;
    }
    
    /**
     * Returns the number of elements in the collection
     * 
     * @return the number of elements in the collection
     */
    public int size() {
        int result = getSubtreeSize(root);
        
        testInvariants();
        
        return result;
    }
    
    /**
     * Given a tree and one of its unsorted children, this sorts the child by moving
     * it into the left or right subtrees. Returns the next unsorted child or -1 if none
     * 
     * @param subTree parent tree
     * @param toMove child (unsorted) subtree
     * @since 1.0
     */
    private final int partition(int subTree, int toMove) {
        int result = nextUnsorted[toMove];
        
        if (isLess(toMove, subTree)) {
            int nextLeft = addUnsorted(leftSubTree[subTree], toMove);
            leftSubTree[subTree] = nextLeft;
            parentTree[nextLeft] = subTree;
        } else {
            int nextRight = addUnsorted(rightSubTree[subTree], toMove);
            rightSubTree[subTree] = nextRight;
            parentTree[nextRight] = subTree;
        }
        
        return result;
    }
    
    /**
     * Partitions the given subtree. Moves all unsorted elements at the given node
     * to either the left or right subtrees. If the node itself was scheduled for
     * lazy removal, this will force the node to be removed immediately. Returns
     * the new subTree.
     * 
     * @param subTree
     * @return the replacement node (this may be different from subTree if the subtree
     * was replaced during the removal)
     * @since 1.0
     */
    private final int partition(int subTree, FastProgressReporter mon) throws InterruptedException {
        if (subTree == -1) {
            return -1;
        }
        
        if (contents[subTree] == lazyRemovalFlag) {
            subTree = removeNode(subTree);
            if (subTree == -1) {
                return -1;
            }
        }
        
        for (int idx = nextUnsorted[subTree]; idx != -1;) { 
            idx = partition(subTree, idx);
            nextUnsorted[subTree] = idx;
            if (idx != -1) {
                parentTree[idx] = subTree;
            }
            
            if (mon.isCanceled()) {
                throw new InterruptedException();
            }
        }
        
        // At this point, there are no remaining unsorted nodes in this subtree
        nextUnsorted[subTree] = -1;
        
        return subTree;
    }
    
    private final int getSubtreeSize(int subTree) {
        if (subTree == -1) {
            return 0;
        }
        return treeSize[subTree];
    }
    
    /**
     * Increases the capacity of this collection, if necessary, so that it can hold the 
     * given number of elements. This can be used prior to a sequence of additions to
     * avoid memory reallocation. This cannot be used to reduce the amount 
     * of memory used by the collection.
     *
     * @param newSize capacity for this collection
     */
    public final void setCapacity(int newSize) {
        if (newSize > contents.length) {
            setArraySize(newSize);
        }
    }
    
    /**
     * Adjusts the capacity of the array.
     * 
     * @param newCapacity
     */
    private final void setArraySize(int newCapacity) {
        Object[] newContents = new Object[newCapacity];
        System.arraycopy(contents, 0, newContents, 0, lastNode);
        contents = newContents;
        
        int[] newLeftSubTree = new int[newCapacity];
        System.arraycopy(leftSubTree, 0, newLeftSubTree, 0, lastNode);
        leftSubTree = newLeftSubTree;
        
        int[] newRightSubTree = new int[newCapacity];
        System.arraycopy(rightSubTree, 0, newRightSubTree, 0, lastNode);
        rightSubTree = newRightSubTree;
        
        int[] newNextUnsorted = new int[newCapacity];
        System.arraycopy(nextUnsorted, 0, newNextUnsorted, 0, lastNode);
        nextUnsorted = newNextUnsorted;
        
        int[] newTreeSize = new int[newCapacity];
        System.arraycopy(treeSize, 0, newTreeSize, 0, lastNode);
        treeSize = newTreeSize;
        
        int[] newParentTree = new int[newCapacity];
        System.arraycopy(parentTree, 0, newParentTree, 0, lastNode);
        parentTree = newParentTree;
    }
    
    /**
     * Creates a new node with the given value. Returns the index of the newly
     * created node.
     * 
     * @param value
     * @return the index of the newly created node
     * @since 1.0
     */
    private final int createNode(Object value) {
        int result = -1;

        if (firstUnusedNode == -1) {
            // If there are no unused nodes from prior removals, then 
            // we add a node at the end
            result = lastNode;
            
            // If this would cause the array to overflow, reallocate the array 
            if (contents.length <= lastNode) {
                setCapacity(lastNode * 2);
            }
            
            lastNode++;
        } else {
            // Reuse a node from a prior removal
            result = firstUnusedNode;
            firstUnusedNode = nextUnsorted[result];
        }
        
        contents[result] = value;
        treeSize[result] = 1;
        
        // Clear pointers
        leftSubTree[result] = -1;
        rightSubTree[result] = -1;
        nextUnsorted[result] = -1;
        
        // As long as we have a hash table of values onto tree indices, incrementally
        // update the hash table. Note: the table is only constructed as needed, and it
        // is destroyed whenever the arrays are reallocated instead of reallocating it.
        if (objectIndices != null) {
            objectIndices.put(value, result);
        }
        
        return result;
    }
    
    /**
     * Returns the current tree index for the given object.
     * 
     * @param value
     * @return the current tree index
     * @since 1.0
     */
    private int getObjectIndex(Object value) {
        // If we don't have a map of values onto tree indices, build the map now.
        if (objectIndices == null) {
            int result = -1;
            
            objectIndices = new IntHashMap((int)(contents.length / loadFactor) + 1, loadFactor);
            
            for (int i = 0; i < lastNode; i++) {
                Object element = contents[i];
                
                if (element != null && element != lazyRemovalFlag) {
                    objectIndices.put(element, i);
                    
                    if (value == element) {
                        result = i;
                    }
                }
            }
            
            return result;
        }
        
        // If we have a map of values onto tree indices, return the result by looking it up in
        // the map
        return objectIndices.get(value, -1);
    }
    
    /**
     * Redirects any pointers from the original to the replacement. If the replacement
     * causes a change in the number of elements in the parent tree, the changes are
     * propogated toward the root.
     * 
     * @param nodeToReplace
     * @param replacementNode
     * @since 1.0
     */
    private void replaceNode(int nodeToReplace, int replacementNode) {
        int parent = parentTree[nodeToReplace];
        
        if (parent == -1) {
            if (root == nodeToReplace) {
                setRootNode(replacementNode);
            }
        } else {
            if (leftSubTree[parent] == nodeToReplace) {
                leftSubTree[parent] = replacementNode;
            } else if (rightSubTree[parent] == nodeToReplace) {
                rightSubTree[parent] = replacementNode;
            } else if (nextUnsorted[parent] == nodeToReplace) {
                nextUnsorted[parent] = replacementNode;
            }
            if (replacementNode != -1) {
                parentTree[replacementNode] = parent;
            }
        }
    }
    
    private void recomputeAncestorTreeSizes(int node) {
        while (node != -1) {
            int oldSize = treeSize[node];
            
            recomputeTreeSize(node);
            
            if (treeSize[node] == oldSize) {
                break;
            }
            
            node = parentTree[node];
        }        
    }
    
    /**
     * Recomputes the tree size for the given node.
     * 
     * @param node
     * @since 1.0
     */
    private void recomputeTreeSize(int node) {
        if (node == -1) {
            return;
        }
        treeSize[node] = getSubtreeSize(leftSubTree[node])
    		+ getSubtreeSize(rightSubTree[node])
    		+ getSubtreeSize(nextUnsorted[node])
    		+ (contents[node] == lazyRemovalFlag ? 0 : 1); 
    }
    
    /**
     * 
     * @param toRecompute
     * @param whereToStop
     * @since 1.0
     */
    private void forceRecomputeTreeSize(int toRecompute, int whereToStop) {
        while (toRecompute != -1 && toRecompute != whereToStop) {
	        recomputeTreeSize(toRecompute);
	        
	        toRecompute = parentTree[toRecompute];
        }
    }
    
    /**
     * Destroy the node at the given index in the tree
     * @param nodeToDestroy
     * @since 1.0
     */
    private void destroyNode(int nodeToDestroy) {
        // If we're maintaining a map of values onto tree indices, remove this entry from
        // the map
        if (objectIndices != null) {
            Object oldContents = contents[nodeToDestroy];
            if (oldContents != lazyRemovalFlag) {
                objectIndices.remove(oldContents);
            }
        }
        
        contents[nodeToDestroy] = null;
        leftSubTree[nodeToDestroy] = -1;
        rightSubTree[nodeToDestroy] = -1;
        
        if (firstUnusedNode == -1) {
            treeSize[nodeToDestroy] = 1;
        } else {
            treeSize[nodeToDestroy] = treeSize[firstUnusedNode] + 1;
            parentTree[firstUnusedNode] = nodeToDestroy;
        }
        
        nextUnsorted[nodeToDestroy] = firstUnusedNode;
        
        firstUnusedNode = nodeToDestroy; 
    }
    
    /**
     * Frees up memory by clearing the list of nodes that have been freed up through removals.
     * 
     * @since 1.0
     */
    private final void pack() {
        
        // If there are no unused nodes, then there is nothing to do
        if (firstUnusedNode == -1) {
            return;
        }
        
        int reusableNodes = getSubtreeSize(firstUnusedNode);
        int nonPackableNodes = lastNode - reusableNodes;
        
        // Only pack the array if we're utilizing less than 1/4 of the array (note:
        // this check is important, or it will change the time bounds for removals)
        if (contents.length < MIN_CAPACITY || nonPackableNodes > contents.length / 4) {
            return;
        }
        
        // Rather than update the entire map, just null it out. If it is needed,
        // it will be recreated lazily later. This will save some memory if the
        // map isn't needed, and it takes a similar amount of time to recreate the
        // map as to update all the indices.
        objectIndices = null;
        
        // Maps old index -> new index
        int[] mapNewIdxOntoOld = new int[contents.length];
        int[] mapOldIdxOntoNew = new int[contents.length];
        
        int nextNewIdx = 0;
        // Compute the mapping. Determine the new index for each element 
        for (int oldIdx = 0; oldIdx < lastNode; oldIdx++) {
            if (contents[oldIdx] != null) {
                mapOldIdxOntoNew[oldIdx] = nextNewIdx;
                mapNewIdxOntoOld[nextNewIdx] = oldIdx;
                nextNewIdx++;
            } else {
                mapOldIdxOntoNew[oldIdx] = -1;
            }
        }
        
        // Make the actual array size double the number of nodes to allow
        // for expansion.
        int newNodes = nextNewIdx;
        int newCapacity = Math.max(newNodes * 2, MIN_CAPACITY);
        
        // Allocate new arrays
        Object[] newContents = new Object[newCapacity];
        int[] newTreeSize = new int[newCapacity];
        int[] newNextUnsorted = new int[newCapacity];
        int[] newLeftSubTree = new int[newCapacity];
        int[] newRightSubTree = new int[newCapacity];
        int[] newParentTree = new int[newCapacity];
        
        for (int newIdx = 0; newIdx < newNodes; newIdx++) {
            int oldIdx = mapNewIdxOntoOld[newIdx];
            newContents[newIdx] = contents[oldIdx];
            newTreeSize[newIdx] = treeSize[oldIdx];
            
            int left = leftSubTree[oldIdx];
            if (left == -1) {
                newLeftSubTree[newIdx] = -1;
            } else {
                newLeftSubTree[newIdx] = mapOldIdxOntoNew[left];
            }
            
            int right = rightSubTree[oldIdx];
            if (right == -1) {
                newRightSubTree[newIdx] = -1;                
            } else {
                newRightSubTree[newIdx] = mapOldIdxOntoNew[right];
            }

            int unsorted = nextUnsorted[oldIdx];
            if (unsorted == -1) {
                newNextUnsorted[newIdx] = -1;
            } else {
                newNextUnsorted[newIdx] = mapOldIdxOntoNew[unsorted];
            }
            
            int parent = parentTree[oldIdx];
            if (parent == -1) {
                newParentTree[newIdx] = -1;
            } else {
                newParentTree[newIdx] = mapOldIdxOntoNew[parent];
            }
        }
        
        contents = newContents;
        nextUnsorted = newNextUnsorted;
        treeSize = newTreeSize;
        leftSubTree = newLeftSubTree;
        rightSubTree = newRightSubTree;
        parentTree = newParentTree;
        
        if (root != -1) {
            root = mapOldIdxOntoNew[root];
        }
        
        // All unused nodes have been removed
        firstUnusedNode = -1;
        lastNode = newNodes;
    }
    
    /**
     * Adds the given object to the collection. Runs in O(1) amortized time.
     * 
     * @param toAdd object to add
     */
    public final void add(Object toAdd) {
    	Assert.isNotNull(toAdd);
        // Create the new node
        int newIdx = createNode(toAdd);
        
        // Insert the new node into the root tree
        setRootNode(addUnsorted(root, newIdx));
        
        testInvariants();
    }
    
    /**
     * Adds all items from the given collection to this collection 
     * 
     * @param toAdd objects to add
     */
    public final void addAll(Collection toAdd) {
    	Assert.isNotNull(toAdd);
        Iterator iter = toAdd.iterator();
        while (iter.hasNext()) {
            add(iter.next());
        }
        
        testInvariants();
    }
    
    /**
     * Adds all items from the given array to the collection
     * 
     * @param toAdd objects to add
     */
    public final void addAll(Object[] toAdd) {
    	Assert.isNotNull(toAdd);
        for (int i = 0; i < toAdd.length; i++) {
            Object object = toAdd[i];
            
            add(object);
        }
        
        testInvariants();
    }
    
    /**
     * Returns true iff the collection is empty
     * 
     * @return true iff the collection contains no elements
     */
    public final boolean isEmpty() {
        boolean result = (root == -1);
        
        testInvariants();
        
        return result;
    }
    
    /**
     * Removes the given object from the collection. Has no effect if
     * the element does not exist in this collection.
     * 
     * @param toRemove element to remove
     */
    public final void remove(Object toRemove) {
        internalRemove(toRemove);
        
        pack();
        
        testInvariants();
    }
    
    /**
     * Internal implementation of remove. Removes the given element but does not
     * pack the container after the removal.
     * 
     * @param toRemove element to remove
     */
    private void internalRemove(Object toRemove) {
        int objectIndex = getObjectIndex(toRemove);
        
        if (objectIndex != -1) {
            int parent = parentTree[objectIndex];
            lazyRemoveNode(objectIndex);
            //Edge parentEdge = getEdgeTo(objectIndex);
            //parentEdge.setTarget(lazyRemoveNode(objectIndex));
            recomputeAncestorTreeSizes(parent);
        }
        
        //testInvariants();
    }
    
    /**
     * Removes all elements in the given array from this collection.
     * 
     * @param toRemove elements to remove 
     */
    public final void removeAll(Object[] toRemove) {
    	Assert.isNotNull(toRemove);
    	
        for (int i = 0; i < toRemove.length; i++) {
            Object object = toRemove[i];
            
            internalRemove(object);
        }
    	pack();
    }
    
    /**
     * Retains the n smallest items in the collection, removing the rest. When
     * this method returns, the size of the collection will be n. Note that
     * this is a no-op if n > the current size of the collection.
     * 
     * Temporarily package visibility until the implementation of FastProgressReporter
     * is finished.
     * 
     * @param n number of items to retain
     * @param mon progress monitor
     * @throws InterruptedException if the progress monitor is cancelled in another thread
     */
    /* package */ final void retainFirst(int n, FastProgressReporter mon) throws InterruptedException {
        int sz = size();
        
        if (n >= sz) {
            return;
        }
        
        removeRange(n, sz - n, mon);
        
        testInvariants();
    }
    
    /**
     * Retains the n smallest items in the collection, removing the rest. When
     * this method returns, the size of the collection will be n. Note that
     * this is a no-op if n > the current size of the collection.
     * 
     * @param n number of items to retain
     */
    public final void retainFirst(int n) {
        try {
            retainFirst(n, new FastProgressReporter());
        } catch (InterruptedException e) {
        }
        
        testInvariants();
    }
    
    /**
     * Removes all elements in the given range from this collection.
     * For example, removeRange(10, 3) would remove the 11th through 13th
     * smallest items from the collection.
     * 
     * @param first 0-based index of the smallest item to remove
     * @param length number of items to remove
     */
    public final void removeRange(int first, int length) {
        try {
            removeRange(first, length, new FastProgressReporter());
        } catch (InterruptedException e) {
        }
        
        testInvariants();
    }
    
    /**
     * Removes all elements in the given range from this collection.
     * For example, removeRange(10, 3) would remove the 11th through 13th
     * smallest items from the collection.
     * 
     * Temporarily package visiblity until the implementation of FastProgressReporter is
     * finished.
     * 
     * @param first 0-based index of the smallest item to remove
     * @param length number of items to remove
     * @param mon progress monitor
     * @throws InterruptedException if the progress monitor is cancelled in another thread
     */
    /* package */ final void removeRange(int first, int length, FastProgressReporter mon) throws InterruptedException {
    	removeRange(root, first, length, mon);
    	
    	pack();
    	
    	testInvariants();
    }
    
    private final void removeRange(int node, int rangeStart, int rangeLength, FastProgressReporter mon) throws InterruptedException {
    	if (rangeLength == 0) {
    		return;
    	}
    	
    	int size = getSubtreeSize(node);
    	
    	if (size <= rangeStart) {
    		return;
    	}
    	
    	// If we can chop off this entire subtree without any sorting, do so.
    	if (rangeStart == 0 && rangeLength >= size) {
    		removeSubTree(node);
    		return;
    	}
    	try {
	    	// Partition any unsorted nodes
    	    node = partition(node, mon);
	    	
	    	int left = leftSubTree[node];
	    	int leftSize = getSubtreeSize(left);
	    	
	    	int toRemoveFromLeft = Math.min(leftSize - rangeStart, rangeLength);
	    	
	    	// If we're removing anything from the left node
	    	if (toRemoveFromLeft >= 0) {
	    		removeRange(leftSubTree[node], rangeStart, toRemoveFromLeft, mon);
	    		
	    		// Check if we're removing from both sides
	    		int toRemoveFromRight = rangeStart + rangeLength - leftSize - 1;
	    		
	    		if (toRemoveFromRight >= 0) {
	    			// Remove from right subtree
	    			removeRange(rightSubTree[node], 0, toRemoveFromRight, mon);
	    			
	    			// ... removing from both sides means we need to remove the node itself too
	    			removeNode(node);
	    			return;
	    		}
	    	} else {
	    		// If removing from the right side only
	    		removeRange(rightSubTree[node], rangeStart - leftSize - 1, rangeLength, mon);
	    	}
    	} finally {
    	    recomputeTreeSize(node);
    	}
    }
    
    /**
     * Prunes the given subtree (and all child nodes, sorted or unsorted).
     * 
     * @param subTree
     * @since 1.0
     */
    private final void removeSubTree(int subTree) {
        if (subTree == -1) {
            return;
        }
        
        // Destroy all unsorted nodes
        for (int next = nextUnsorted[subTree]; next != -1;) {
            int current = next;
            next = nextUnsorted[next];
            
            // Destroy this unsorted node
            destroyNode(current);
        }
        
        // Destroy left subtree
        removeSubTree(leftSubTree[subTree]);
        
        // Destroy right subtree
        removeSubTree(rightSubTree[subTree]);
        
        replaceNode(subTree, -1);
        // Destroy pivot node
        destroyNode(subTree);
    }
    
    /**
     * Schedules the node for removal. If the node can be removed in constant time,
     * it is removed immediately.
     * 
     * @param subTree
     * @return the replacement node
     * @since 1.0
     */
    private final int lazyRemoveNode(int subTree) {
        int left = leftSubTree[subTree];
        int right = rightSubTree[subTree];

        // If this is a leaf node, remove it immediately
        if (left == -1 && right == -1) {
            int result = nextUnsorted[subTree];
            replaceNode(subTree, result);
            destroyNode(subTree);
            return result;
        }
        
        // Otherwise, flag it for future removal
        Object value = contents[subTree];
        contents[subTree] = lazyRemovalFlag;
        treeSize[subTree]--;
        if (objectIndices != null) {
            objectIndices.remove(value);
        }
        
        return subTree;
    }
    
    /**
     * Removes the given subtree, replacing it with one of its children.
     * Returns the new root of the subtree
     * 
     * @param subTree
     * @return the index of the new root
     * @since 1.0
     */
    private final int removeNode(int subTree) {
        int left = leftSubTree[subTree];
        int right = rightSubTree[subTree];
        
        if (left == -1 || right == -1) {
            int result = -1;
            
            if (left == -1 && right == -1) {
                // If this is a leaf node, replace it with its first unsorted child
                result = nextUnsorted[subTree];
            } else {
                // Either the left or right child is missing -- replace with the remaining child  
                if (left == -1) {
                    result = right;
                } else {
                    result = left;
                }

                try {
                    result = partition(result, new FastProgressReporter());
                } catch (InterruptedException e) {
                    
                }
                if (result == -1) {
                    result = nextUnsorted[subTree];
                } else {
	                int unsorted = nextUnsorted[subTree];
	                nextUnsorted[result] = unsorted;
	                int additionalNodes = 0;
	                if (unsorted != -1) {
	                    parentTree[unsorted] = result;
	                    additionalNodes = treeSize[unsorted];
	                }
	                treeSize[result] += additionalNodes;
                }
            }
            
            replaceNode(subTree, result);
            destroyNode(subTree);
            return result;
        }
                
        // Find the edges that lead to the next-smallest and
        // next-largest nodes
        Edge nextSmallest = new Edge(subTree, DIR_LEFT);
        while (!nextSmallest.isNull()) {
            nextSmallest.advance(DIR_RIGHT);
        }
        
        Edge nextLargest = new Edge(subTree, DIR_RIGHT);
        while (!nextLargest.isNull()) {
            nextLargest.advance(DIR_LEFT);
        }
        
        // Index of the replacement node
        int replacementNode = -1;
        
        // Count of number of nodes moved to the right
        
        int leftSize = getSubtreeSize(left);
        int rightSize = getSubtreeSize(right);
        
        // Swap with a child from the larger subtree
        if (leftSize > rightSize) {
            replacementNode = nextSmallest.getStart();

            // Move any unsorted nodes that are larger than the replacement node into
            // the left subtree of the next-largest node
            Edge unsorted = new Edge(replacementNode, DIR_UNSORTED);
            while (!unsorted.isNull()) {
                int target = unsorted.getTarget();
                
                if (!isLess(target, replacementNode)) {
                    unsorted.setTarget(nextUnsorted[target]);
                    nextLargest.setTarget(addUnsorted(nextLargest.getTarget(), target));
                } else {
                    unsorted.advance(DIR_UNSORTED);
                }
            }
            
            forceRecomputeTreeSize(unsorted.getStart(), replacementNode);
            forceRecomputeTreeSize(nextLargest.getStart(), subTree);
        } else {
            replacementNode = nextLargest.getStart();

            // Move any unsorted nodes that are smaller than the replacement node into
            // the right subtree of the next-smallest node
            Edge unsorted = new Edge(replacementNode, DIR_UNSORTED);
            while (!unsorted.isNull()) {
                int target = unsorted.getTarget();
                
                if (isLess(target, replacementNode)) {
                    unsorted.setTarget(nextUnsorted[target]);
                    nextSmallest.setTarget(addUnsorted(nextSmallest.getTarget(), target));
                } else {
                    unsorted.advance(DIR_UNSORTED);
                }
            }
            
            forceRecomputeTreeSize(unsorted.getStart(), replacementNode);
            forceRecomputeTreeSize(nextSmallest.getStart(), subTree);
        }
        
        // Now all the affected treeSize[...] elements should be updated to reflect the
        // unsorted nodes that moved. Swap nodes. 
        Object replacementContent = contents[replacementNode];
        contents[replacementNode] = contents[subTree];
        contents[subTree] = replacementContent;
        
        if (objectIndices != null) {
            objectIndices.put(replacementContent, subTree);
            // Note: currently we don't bother updating the index of the replacement
            // node since we're going to remove it immediately afterwards and there's
            // no good reason to search for the index in a method that was given the
            // index as a parameter...
            
            // objectIndices.put(contents[replacementNode], replacementNode)
        }
        
        int replacementParent = parentTree[replacementNode]; 
        
        replaceNode(replacementNode, removeNode(replacementNode));
        //Edge parentEdge = getEdgeTo(replacementNode);
        //parentEdge.setTarget(removeNode(replacementNode));

        forceRecomputeTreeSize(replacementParent, subTree);
        recomputeTreeSize(subTree);
        
        //testInvariants();
        
        return subTree;
    }
   
    /**
     * Removes all elements from the collection
     */
    public final void clear() {
        lastNode = 0;
        setArraySize(MIN_CAPACITY);
        root = -1;
        firstUnusedNode = -1;
        objectIndices = null;
        
        testInvariants();
    }
    
    /**
     * Returns the comparator that is determining the sort order for this collection
     * 
     * @return comparator for this collection
     */
    public Comparator getComparator() {
        return comparator;
    }
    
    /**
     * Fills in an array of size n with the n smallest elements from the collection.
     * Can compute the result in sorted or unsorted order. 
     * 
     * Currently package visible until the implementation of FastProgressReporter is finished.
     * 
     * @param result array to be filled
     * @param sorted if true, the result array will be sorted. If false, the result array
     * may be unsorted. This does not affect which elements appear in the result, only their 
     * order.
     * @param mon monitor used to report progress and check for cancellation
     * @return the number of items inserted into the result array. This will be equal to the minimum
     * of result.length and container.size()
     * @throws InterruptedException if the progress monitor is cancelled
     */
    /* package */ final int getFirst(Object[] result, boolean sorted, FastProgressReporter mon) throws InterruptedException {
        int returnValue = getRange(result, 0, sorted, mon);
        
        testInvariants();
        
        return returnValue;
    }
    
    /**
     * Fills in an array of size n with the n smallest elements from the collection.
     * Can compute the result in sorted or unsorted order. 
     * 
     * @param result array to be filled
     * @param sorted if true, the result array will be sorted. If false, the result array
     * may be unsorted. This does not affect which elements appear in the result. It only
     * affects their order. Computing an unsorted result is asymptotically faster.
     * @return the number of items inserted into the result array. This will be equal to the minimum
     * of result.length and container.size()
     */
    public final int getFirst(Object[] result, boolean sorted) {
        int returnValue = 0;
        
        try {
            returnValue = getFirst(result, sorted, new FastProgressReporter());
        } catch (InterruptedException e) {
        }
        
        testInvariants();
        
        return returnValue;
    }
    
    /**
     * Given a position defined by k and an array of size n, this fills in the array with
     * the kth smallest element through to the (k+n)th smallest element. For example, 
     * getRange(myArray, 10, false) would fill in myArray starting with the 10th smallest item
     * in the collection. The result can be computed in sorted or unsorted order. Computing the
     * result in unsorted order is more efficient.
     * <p>
     * Temporarily set to package visibility until the implementation of FastProgressReporter
     * is finished.
     * </p>
     * 
     * @param result array to be filled in
     * @param rangeStart index of the smallest element to appear in the result
     * @param sorted true iff the result array should be sorted
     * @param mon progress monitor used to cancel the operation
     * @throws InterruptedException if the progress monitor was cancelled in another thread
     */
    /* package */ final int getRange(Object[] result, int rangeStart, boolean sorted, FastProgressReporter mon) throws InterruptedException {
        return getRange(result, 0, rangeStart, root, sorted, mon);
    }
    
    /**
     * Computes the n through n+k items in this collection.
     * Computing the result in unsorted order is more efficient. Sorting the result will
     * not change which elements actually show up in the result. That is, even if the result is
     * unsorted, it will still contain the same elements as would have been at that range in
     * a fully sorted collection. 
     * 
     * @param result array containing the result
     * @param rangeStart index of the first element to be inserted into the result array
     * @param sorted true iff the result will be computed in sorted order
     * @return the number of items actually inserted into the result array (will be the minimum
     * of result.length and this.size())
     */
    public final int getRange(Object[] result, int rangeStart, boolean sorted) {
        int returnValue = 0;
        
        try {
            returnValue = getRange(result, rangeStart, sorted, new FastProgressReporter());
        } catch (InterruptedException e) {
        }
        
        testInvariants();
        
        return returnValue;
    }
    
    /**
     * Returns the item at the given index. Indexes are based on sorted order.
     * 
     * @param index index to test
     * @return the item at the given index
     */
    public final Object getItem(int index) {
        Object[] result = new Object[1];
        try {
            getRange(result, index, false, new FastProgressReporter());
        } catch (InterruptedException e) {
            // shouldn't happen
        }
        Object returnValue = result[0];
        
        testInvariants();
        
        return returnValue;
    }
    
    /**
     * Returns the contents of this collection as a sorted or unsorted
     * array. Computing an unsorted array is more efficient.
     * 
     * @param sorted if true, the result will be in sorted order. If false,
     * the result may be in unsorted order.
     * @return the contents of this collection as an array.
     */
    public final Object[] getItems(boolean sorted) {
        Object[] result = new Object[size()];
        
        getRange(result, 0, sorted);
        
        return result;
    }
    
    private final int getRange(Object[] result, int resultIdx, int rangeStart, int node, boolean sorted, FastProgressReporter mon) throws InterruptedException {
        if (node == -1) {
            return 0;
        }

        int availableSpace = result.length - resultIdx;
        
        // If we're asking for all children of the current node, simply call getChildren
        if (rangeStart == 0) {
            if (treeSize[node] <= availableSpace) {
                return getChildren(result, resultIdx, node, sorted, mon);
            }
        }
        
        node = partition(node, mon);
        if (node == -1) {
            return 0;
        }
        
        int inserted = 0;
        
        int numberLessThanNode = getSubtreeSize(leftSubTree[node]);
                
        if (rangeStart < numberLessThanNode) {
            if (inserted < availableSpace) {
                inserted += getRange(result, resultIdx, rangeStart, leftSubTree[node], sorted, mon);
            }
        }
        
        if (rangeStart <= numberLessThanNode) {
	        if (inserted < availableSpace) {
	            result[resultIdx + inserted] = contents[node];
	            inserted++;
	        }	        
        } 
        
        if (inserted < availableSpace) {
            inserted += getRange(result, resultIdx + inserted,
                Math.max(rangeStart - numberLessThanNode - 1, 0), rightSubTree[node], sorted, mon);
        }
        
        return inserted;
    }
    
    /**
     * Fills in the available space in the given array with all children of the given node.
     * 
     * @param result 
     * @param resultIdx index in the result array where we will begin filling in children
     * @param node
     * @return the number of children added to the array
     * @since 1.0
     */
    private final int getChildren(Object[] result, int resultIdx, int node, boolean sorted, FastProgressReporter mon) throws InterruptedException {
        if (node == -1) {
            return 0;
        }
        
        int tempIdx = resultIdx;
        
        if (sorted) {
            node = partition(node, mon);
            if (node == -1) {
                return 0;
            }
        }
        
        // Add child nodes smaller than this one
        if (tempIdx < result.length) {
            tempIdx += getChildren(result, tempIdx, leftSubTree[node], sorted, mon);
        }
        
        // Add the pivot
        if (tempIdx < result.length) {
            Object value = contents[node];
            if (value != lazyRemovalFlag) {
                result[tempIdx++] = value;
            }
        }
        
        // Add child nodes larger than this one
        if (tempIdx < result.length) {
            tempIdx += getChildren(result, tempIdx, rightSubTree[node], sorted, mon);
        }
        
        // Add unsorted children (should be empty if the sorted flag was true)
        for (int unsortedNode = nextUnsorted[node]; unsortedNode != -1 && tempIdx < result.length; 
        	unsortedNode = nextUnsorted[unsortedNode]) {
            
            result[tempIdx++] = contents[unsortedNode];
        }
        
        return tempIdx - resultIdx;
    }

    /**
     * Returns true iff this collection contains the given item
     * 
     * @param item item to test
     * @return true iff this collection contains the given item
     */
    public boolean contains(Object item) {
    	Assert.isNotNull(item);
        boolean returnValue = (getObjectIndex(item) != -1);
        
        testInvariants();
        
        return returnValue;
    }
}
