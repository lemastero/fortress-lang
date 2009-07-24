/*******************************************************************************
 Copyright 2009 Sun Microsystems, Inc.,
 4150 Network Circle, Santa Clara, California 95054, U.S.A.
 All rights reserved.

 U.S. Government Rights - Commercial software.
 Government users are subject to the Sun Microsystems, Inc. standard
 license agreement and applicable provisions of the FAR and its supplements.

 Use is subject to license terms.

 This distribution may include materials developed by third parties.

 Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered
 trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
 ******************************************************************************/

package com.sun.fortress.parser_util.precedence_opexpr;

/**
 * An abstract implementation of a visitor over Precedence that does not return a value.
 * * This visitor implements the visitor interface with methods that
 * * first visit children, and then call visitCASEOnly().
 * * (CASE is replaced by the case name.)
 * * The default implementation of the forCASEOnly methods call
 * * protected method defaultCase(). This method defaults to no-op.
 */
public class PrecedenceDepthFirstVisitor_void implements PrecedenceVisitor_void {
    /* Methods to visit an item. */
    public void forNoneDoFirst(None that) {
        defaultDoFirst(that);
    }

    public void forNoneOnly(None that) {
        defaultCase(that);
    }

    public void forHigherDoFirst(Higher that) {
        defaultDoFirst(that);
    }

    public void forHigherOnly(Higher that) {
        defaultCase(that);
    }

    public void forLowerDoFirst(Lower that) {
        defaultDoFirst(that);
    }

    public void forLowerOnly(Lower that) {
        defaultCase(that);
    }

    public void forEqualDoFirst(Equal that) {
        defaultDoFirst(that);
    }

    public void forEqualOnly(Equal that) {
        defaultCase(that);
    }

    /* Implementation of PrecedenceVisitor_void methods to implement depth-first traversal. */
    public void forNone(None that) {
        forNoneDoFirst(that);
        forNoneOnly(that);
    }

    public void forHigher(Higher that) {
        forHigherDoFirst(that);
        forHigherOnly(that);
    }

    public void forLower(Lower that) {
        forLowerDoFirst(that);
        forLowerOnly(that);
    }

    public void forEqual(Equal that) {
        forEqualDoFirst(that);
        forEqualOnly(that);
    }

    /**
     * This method is called by default from cases that do not
     * * override forCASEDoFirst.
     */
    protected void defaultDoFirst(Precedence that) {
    }

    /**
     * This method is called by default from cases that do not
     * * override forCASEOnly.
     */
    protected void defaultCase(Precedence that) {
    }
}
