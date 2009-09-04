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

package com.sun.fortress.compiler.runtimeValues;

public final class FBoolean extends fortress.CompilerBuiltin.Boolean.DefaultTraitMethods
        implements fortress.CompilerBuiltin.Boolean  {
    public static final FBoolean TRUE = new FBoolean(true);
    public static final FBoolean FALSE = new FBoolean(false);

    final boolean val;

    FBoolean(boolean x) { val = x; }
    public String toString() { return "" + val;}
    public boolean getValue() {return val;}
    public static FBoolean make(boolean x) {return x ? TRUE : FALSE;}
}
