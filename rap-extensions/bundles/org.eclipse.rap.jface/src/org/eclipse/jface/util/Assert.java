/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.util;

/**
 * <code>Assert</code> is useful for for embedding runtime sanity checks
 * in code. The static predicate methods all test a condition and throw some
 * type of unchecked exception if the condition does not hold.
 * <p>
 * Assertion failure exceptions, like most runtime exceptions, are
 * thrown when something is misbehaving. Assertion failures are invariably
 * unspecified behavior; consequently, clients should never rely on
 * these being thrown (or not thrown). <b>If you find yourself in the
 * position where you need to catch an assertion failure, you have most 
 * certainly written your program incorrectly.</b>
 * </p>
 * <p>
 * Note that an <code>assert</code> statement is slated to be added to the
 * Java language in JDK 1.4, rending this class obsolete.
 * 
 * @deprecated As of 3.3, replaced by {@link org.eclipse.core.runtime.Assert}
 * </p>
 */
public final class Assert {

    /**
     * <code>AssertionFailedException</code> is a runtime exception thrown
     * by some of the methods in <code>Assert</code>.
     * <p>
     * This class is not declared public to prevent some misuses; programs that catch 
     * or otherwise depend on assertion failures are susceptible to unexpected
     * breakage when assertions in the code are added or removed.
     * </p>
     */
    private static class AssertionFailedException extends RuntimeException {

        /**
         * Generated serial version UID for this class.
         */
        private static final long serialVersionUID = 3257852073508024376L;

        /**
         * Constructs a new exception with the given message.
         * @param detail the detail message
         */
        public AssertionFailedException(String detail) {
            super(detail);
        }
    }

    /* This class is not intended to be instantiated. */
    private Assert() {
    }

    /**
     * Asserts that an argument is legal. If the given boolean is
     * not <code>true</code>, an <code>IllegalArgumentException</code>
     * is thrown.
     *
     * @param expression the outcome of the check
     * @return <code>true</code> if the check passes (does not return
     *    if the check fails)
     * @exception IllegalArgumentException if the legality test failed
     */
    public static boolean isLegal(boolean expression) {
        // succeed as quickly as possible
        if (expression) {
            return true;
        }
        return isLegal(expression, "");//$NON-NLS-1$
    }

    /**
     * Asserts that an argument is legal. If the given boolean is
     * not <code>true</code>, an <code>IllegalArgumentException</code>
     * is thrown.
     * The given message is included in that exception, to aid debugging.
     *
     * @param expression the outcome of the check
     * @param message the message to include in the exception
     * @return <code>true</code> if the check passes (does not return
     *    if the check fails)
     * @exception IllegalArgumentException if the legality test failed
     */
    public static boolean isLegal(boolean expression, String message) {
        if (!expression) {
			throw new IllegalArgumentException("assertion failed; " + message); //$NON-NLS-1$
		}
        return expression;
    }

    /**
     * Asserts that the given object is not <code>null</code>. If this
     * is not the case, some kind of unchecked exception is thrown.
     * <p>
     * As a general rule, parameters passed to API methods must not be
     * <code>null</code> unless <b>explicitly</b> allowed in the method's
     * specification. Similarly, results returned from API methods are never
     * <code>null</code> unless <b>explicitly</b> allowed in the method's
     * specification. Implementations are encouraged to make regular use of 
     * <code>Assert.isNotNull</code> to ensure that <code>null</code> 
     * parameters are detected as early as possible.
     * </p>
     * 
     * @param object the value to test
     * @exception AssertionFailedException an unspecified unchecked exception if the object
     *   is <code>null</code>
     */
    public static void isNotNull(Object object) {
        // succeed as quickly as possible
        if (object != null) {
            return;
        }
        isNotNull(object, "");//$NON-NLS-1$
    }

    /**
     * Asserts that the given object is not <code>null</code>. If this
     * is not the case, some kind of unchecked exception is thrown.
     * The given message is included in that exception, to aid debugging.
     * <p>
     * As a general rule, parameters passed to API methods must not be
     * <code>null</code> unless <b>explicitly</b> allowed in the method's
     * specification. Similarly, results returned from API methods are never
     * <code>null</code> unless <b>explicitly</b> allowed in the method's
     * specification. Implementations are encouraged to make regular use of 
     * <code>Assert.isNotNull</code> to ensure that <code>null</code> 
     * parameters are detected as early as possible.
     * </p>
     * 
     * @param object the value to test
     * @param message the message to include in the exception
     * @exception AssertionFailedException an unspecified unchecked exception if the object
     *   is <code>null</code>
     */
    public static void isNotNull(Object object, String message) {
        if (object == null) {
			throw new AssertionFailedException("null argument;" + message);//$NON-NLS-1$
		}
    }

    /**
     * Asserts that the given boolean is <code>true</code>. If this
     * is not the case, some kind of unchecked exception is thrown.
     *
     * @param expression the outcome of the check
     * @return <code>true</code> if the check passes (does not return
     *    if the check fails)
     */
    public static boolean isTrue(boolean expression) {
        // succeed as quickly as possible
        if (expression) {
            return true;
        }
        return isTrue(expression, "");//$NON-NLS-1$
    }

    /**
     * Asserts that the given boolean is <code>true</code>. If this
     * is not the case, some kind of unchecked exception is thrown.
     * The given message is included in that exception, to aid debugging.
     *
     * @param expression the outcome of the check
     * @param message the message to include in the exception
     * @return <code>true</code> if the check passes (does not return
     *    if the check fails)
     */
    public static boolean isTrue(boolean expression, String message) {
        if (!expression) {
			throw new AssertionFailedException("Assertion failed: " + message);//$NON-NLS-1$
		}
        return expression;
    }
}
