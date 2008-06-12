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

package com.sun.fortress.syntax_abstractions.util;

import java.util.LinkedList;
import java.util.List;

import com.sun.fortress.nodes.AnyCharacterSymbol;
import com.sun.fortress.nodes.BackspaceSymbol;
import com.sun.fortress.nodes.BreaklineSymbol;
import com.sun.fortress.nodes.CarriageReturnSymbol;
import com.sun.fortress.nodes.CharSymbol;
import com.sun.fortress.nodes.CharacterClassSymbol;
import com.sun.fortress.nodes.FormfeedSymbol;
import com.sun.fortress.nodes.Id;
import com.sun.fortress.nodes.NewlineSymbol;
import com.sun.fortress.nodes.NoWhitespaceSymbol;
import com.sun.fortress.nodes.TabSymbol;
import com.sun.fortress.nodes.VarType;
import com.sun.fortress.nodes.TraitType;
import com.sun.fortress.nodes.KeywordSymbol;
import com.sun.fortress.nodes.Node;
import com.sun.fortress.nodes.NodeDepthFirstVisitor;
import com.sun.fortress.nodes.NonterminalSymbol;
import com.sun.fortress.nodes.OptionalSymbol;
import com.sun.fortress.nodes.PrefixedSymbol;
import com.sun.fortress.nodes.RepeatOneOrMoreSymbol;
import com.sun.fortress.nodes.RepeatSymbol;
import com.sun.fortress.nodes.StaticArg;
import com.sun.fortress.nodes.SyntaxSymbol;
import com.sun.fortress.nodes.TokenSymbol;
import com.sun.fortress.nodes.Type;
import com.sun.fortress.nodes.TypeArg;
import com.sun.fortress.nodes_util.NodeFactory;
import com.sun.fortress.syntax_abstractions.environments.GrammarEnv;
import com.sun.fortress.syntax_abstractions.environments.MemberEnv;

public class TypeCollector extends NodeDepthFirstVisitor<Type> {

    private TypeCollector() {}

    public static Type getType(PrefixedSymbol ps) {
        return ps.getSymbol().accept(new TypeCollector());
    }

    @Override
    public Type defaultCase(Node that) {
        throw new RuntimeException("Unexpected case: "+that.getClass());
    }

    @Override
    public Type forOptionalSymbol(OptionalSymbol that) {
        return handle(that.getSymbol(), SyntaxAbstractionUtil.FORTRESSLIBRARY, SyntaxAbstractionUtil.MAYBE);
    }

    @Override
    public Type forRepeatOneOrMoreSymbol(RepeatOneOrMoreSymbol that) {
        return handle(that.getSymbol(), SyntaxAbstractionUtil.LIST, SyntaxAbstractionUtil.LIST);
    }

    @Override
    public Type forRepeatSymbol(RepeatSymbol that) {
        return handle(that.getSymbol(), SyntaxAbstractionUtil.LIST, SyntaxAbstractionUtil.LIST);
    }

    @Override
    public Type forNonterminalSymbol(NonterminalSymbol that) {
        if (!GrammarEnv.contains(that.getNonterminal())) {
            throw new RuntimeException("Grammar environment does not contain identifier: "+that.getNonterminal());
        }
        MemberEnv memberEnv = GrammarEnv.getMemberEnv(that.getNonterminal());
        return memberEnv.getAstType();
    }

    @Override
    public Type forKeywordSymbol(KeywordSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forTokenSymbol(TokenSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forCharacterClassSymbol(CharacterClassSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forAnyCharacterSymbol(AnyCharacterSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forBackspaceSymbol(BackspaceSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forBreaklineSymbol(BreaklineSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forCarriageReturnSymbol(CarriageReturnSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forCharSymbol(CharSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forFormfeedSymbol(FormfeedSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forNewlineSymbol(NewlineSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forNoWhitespaceSymbol(NoWhitespaceSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    @Override
    public Type forTabSymbol(TabSymbol that) {
        Id string = NodeFactory.makeId(SyntaxAbstractionUtil.FORTRESSBUILTIN, SyntaxAbstractionUtil.STRING);
        return new VarType(string);
    }

    private Type handle(SyntaxSymbol symbol, String api, String id) {
        Type type = symbol.accept(this);
        Id list = NodeFactory.makeId(api, id);
        List<StaticArg> args = new LinkedList<StaticArg>();
        args.add(new TypeArg(type));
        return new TraitType(list, args);
    }


}
