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

package com.sun.fortress.interpreter.evaluator.values;

import com.sun.fortress.interpreter.evaluator.Environment;
import com.sun.fortress.interpreter.evaluator.types.FType;
import com.sun.fortress.interpreter.evaluator.types.FTypeObject;
import com.sun.fortress.nodes.Id;

public class FOrdinaryObject extends FObject {

    final Environment lexicalEnv;
    final Environment selfEnv;
    final FTypeObject ftype;

    public FOrdinaryObject(FTypeObject selfType, Environment lexical_env, Environment self_dot_env) {
        this.lexicalEnv = lexical_env;
        this.selfEnv = self_dot_env;
        this.ftype = selfType;
    }

    /**
     * The environment that you get from "self."
     */
    public Environment getSelfEnv() {
        return selfEnv;
    }

    public Environment getLexicalEnv() {
        return lexicalEnv;
    }

    public FType type() {
        return ftype;
    }

    public boolean seqv(FValue v) {
        if (!(ftype.isValueType())) return false;
        if (!(v instanceof FOrdinaryObject)) return false;
        FOrdinaryObject o = (FOrdinaryObject) v;
        if (ftype != o.type()) return false;
        if (getLexicalEnv() != o.getLexicalEnv()) return false;
        for (Id fld : ftype.getFieldNames()) {
            String name = fld.getText();
            if (!(select(name).seqv(o.select(name)))) return false;
        }
        return true;
    }

}
