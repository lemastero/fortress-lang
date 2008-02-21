/*******************************************************************************
    Copyright 2008 Sun Microsystems, Inc.,
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

package com.sun.fortress.compiler;

import com.sun.fortress.nodes.AbstractNode;
import com.sun.fortress.nodes_util.ErrorMsgMaker;
import com.sun.fortress.useful.HasAt;

/**
 * A static error generated by the type checker.
 */
public abstract class TypeError extends StaticError {

    /**
     * Make a simple static error with the given location.
     */
    @Override
    public static StaticError make(String description, HasAt location) {
        return make(description, location.at());
    }

    /**
     * Make a simple type error with type description "Error" and the given
     * location.
     */
    @Override
    public static StaticError make(final String description, final String location) {
        return new TypeError() {
            public String description() { return description; }
            public String at() { return location; }
        };
    }

}
