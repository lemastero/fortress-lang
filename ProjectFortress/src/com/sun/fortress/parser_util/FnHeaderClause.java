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

/*
 * Fortress functional header clauses.
 * Fortress AST node local to the Rats! com.sun.fortress.interpreter.parser.
 */
package com.sun.fortress.parser_util;

import com.sun.fortress.nodes.BaseType;
import com.sun.fortress.nodes.Contract;
import com.sun.fortress.nodes.Type;
import com.sun.fortress.nodes.WhereClause;
import com.sun.fortress.useful.MagicNumbers;
import edu.rice.cs.plt.tuple.Option;

import java.util.List;

public class FnHeaderClause {

    private Option<List<BaseType>> throwsClause;
    private Option<WhereClause> whereClause;
    private Option<Contract> contractClause;
    private Option<Type> returnType;

    public FnHeaderClause(Option<List<BaseType>> throwsClause,
                          Option<WhereClause> whereClause,
                          Option<Contract> contractClause,
                          Option<Type> returnType) {
        this.throwsClause = throwsClause;
        this.whereClause = whereClause;
        this.contractClause = contractClause;
        this.returnType = returnType;
    }

    public Option<List<BaseType>> getThrowsClause() {
        return throwsClause;
    }

    public Option<WhereClause> getWhereClause() {
        return whereClause;
    }

    public Option<Contract> getContractClause() {
        return contractClause;
    }

    public Option<Type> getReturnType() {
        return returnType;
    }

    public int hashCode() {
        return throwsClause.hashCode() + whereClause.hashCode() * MagicNumbers.n +
               contractClause.hashCode() * MagicNumbers.t * returnType.hashCode() * MagicNumbers.d;
    }

    public boolean equals(Object o) {
        if (o.getClass().equals(this.getClass())) {
            FnHeaderClause fhc = (FnHeaderClause) o;
            return throwsClause.equals(fhc.getThrowsClause()) && whereClause.equals(fhc.getWhereClause()) &&
                   contractClause.equals(fhc.getContractClause()) && returnType.equals(fhc.getReturnType());
        }
        return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (throwsClause.isSome()) {
            sb.append("throws { ");
            sb.append(throwsClause.unwrap());
            sb.append(" } ");
        }
        sb.append("where...");
        if (contractClause.isSome()) {
            sb.append(contractClause.unwrap());
        }
        if (returnType.isSome()) {
            sb.append(":");
            sb.append(returnType.unwrap());
        }
        return sb.toString();
    }
}
