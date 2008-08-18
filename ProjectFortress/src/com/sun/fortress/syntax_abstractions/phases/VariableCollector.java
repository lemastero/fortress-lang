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

package com.sun.fortress.syntax_abstractions.phases;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;

import com.sun.fortress.nodes.Id;
import com.sun.fortress.nodes.NodeDepthFirstVisitor;
import com.sun.fortress.nodes.NodeDepthFirstVisitor_void;
import com.sun.fortress.nodes.PrefixedSymbol;
import com.sun.fortress.nodes.GroupSymbol;
import com.sun.fortress.nodes.SyntaxSymbol;
import com.sun.fortress.nodes.SyntaxDef;
import com.sun.fortress.nodes.AndPredicateSymbol;
import com.sun.fortress.nodes.NotPredicateSymbol;

import com.sun.fortress.nodes.RepeatSymbol;
import com.sun.fortress.nodes.RepeatOneOrMoreSymbol;
import com.sun.fortress.nodes.OptionalSymbol;

import com.sun.fortress.syntax_abstractions.environments.Depth;
import com.sun.fortress.syntax_abstractions.environments.Depth.BaseDepth;
import com.sun.fortress.syntax_abstractions.environments.Depth.ListDepth;
import com.sun.fortress.syntax_abstractions.environments.Depth.OptionDepth;

import com.sun.fortress.useful.Debug;

public class VariableCollector extends NodeDepthFirstVisitor_void {

    private Map<Id,Depth> depthMap;
    private Depth depth;

    public VariableCollector(Map<Id,Depth> depthMap) {
        this(depthMap, new BaseDepth());
    }

    private VariableCollector(Map<Id,Depth> depthMap, Depth depth) {
        this.depthMap = depthMap;
        this.depth = depth;
    }

    @Override public void defaultCase(com.sun.fortress.nodes.Node that) {
        return;
    }	

    @Override public void forPrefixedSymbol(PrefixedSymbol that) {
        if (that.getId().isSome()) {
            this.depthMap.put(that.getId().unwrap(), this.depth);
        }
        super.forPrefixedSymbol(that);
    }

    @Override public void forNotPredicateSymbol(NotPredicateSymbol that) {
        return; // FIXME: ???
    }

    @Override public void forAndPredicateSymbol(AndPredicateSymbol that) {
        return; // FIXME: ???
    }

    @Override public void forRepeatSymbol(RepeatSymbol that) {
        Debug.debug(Debug.Type.SYNTAX, 3, "Repeat symbol ", that.getSymbol());
        that.getSymbol().accept(new VariableCollector(this.depthMap, depth.addStar()));
    }

    @Override public void forRepeatOneOrMoreSymbol(RepeatOneOrMoreSymbol that) {
        Debug.debug(Debug.Type.SYNTAX, 3, "Repeat One or more symbol ", that.getSymbol());
        that.getSymbol().accept(new VariableCollector(this.depthMap, depth.addPlus()));
    }

    @Override public void forOptionalSymbol(OptionalSymbol that) {
        Debug.debug( Debug.Type.SYNTAX, 3, "Optional ", that.getSymbol() );
        that.getSymbol().accept(new VariableCollector(this.depthMap, depth.addOptional()));
    }
}
