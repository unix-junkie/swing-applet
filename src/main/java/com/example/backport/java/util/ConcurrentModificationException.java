/*
 * @(#)ConcurrentModificationException.java	1.7 01/11/29
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.example.backport.java.util;

import java.util.Hashtable;
import java.util.Vector;

/**
 * This exception may be thrown by methods that have detected concurrent
 * modification of a backing object when such modification is not permissible.
 * <p>
 * For example, it is not permssible for one thread to modify a Collection
 * while another thread is iterating over it.  In general, the results of the
 * iteration are undefined under these circumstances.  Some Iterator
 * implementations (including those of all the collection implementations
 * provided by the JDK) may choose to throw this exception if this behavior is
 * detected.  Iterators that do this are known as <i>fail-fast</i> iterators,
 * as they fail quickly and cleanly, rather that risking arbitrary,
 * non-deterministic behavior at an undetermined time in the future.
 *
 * @author  Josh Bloch
 * @version 1.7, 11/29/01
 * @see	    Collection
 * @see     Iterator
 * @see     ListIterator
 * @see	    Vector
 * @see	    LinkedList
 * @see	    HashSet
 * @see	    Hashtable
 * @see	    TreeMap
 * @see	    AbstractList
 * @since   JDK1.2
 */
public class ConcurrentModificationException extends RuntimeException {
	private static final long serialVersionUID = 2786947999980965178L;

    /**
     * Constructs a ConcurrentModificationException with no
     * detail message.
     */
    public ConcurrentModificationException() {
    }

    /**
     * Constructs a <tt>ConcurrentModificationException</tt> with the
     * specified detail message.
     */
    public ConcurrentModificationException(final String message) {
	super(message);
    }
}
