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

package com.sun.fortress.useful;

import java.util.Iterator;

public class StringArrayIterator implements Iterator<String> {
    String[] a;
    int n;
    int i;

    public StringArrayIterator(String[] a, int n) {
        this.a = a;
        this.n = n;
    }

    public boolean hasNext() {
        return i < n;
    }

    public String next() {
        if (hasNext()) {
            return a[i++];
        }
        return null;
    }

    public void remove() {
        throw new Error("remove not implemented");
    }

}