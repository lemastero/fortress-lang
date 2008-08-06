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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sun.fortress.compiler.GlobalEnvironment;
import com.sun.fortress.compiler.disambiguator.NonterminalEnv;
import com.sun.fortress.compiler.disambiguator.NonterminalNameDisambiguator;
import com.sun.fortress.compiler.index.ApiIndex;
import com.sun.fortress.compiler.index.GrammarIndex;
import com.sun.fortress.compiler.index.NonterminalIndex;
import com.sun.fortress.exceptions.StaticError;
import com.sun.fortress.exceptions.MacroError;
import com.sun.fortress.nodes.APIName;
import com.sun.fortress.nodes.Api;
import com.sun.fortress.nodes.GrammarDef;
import com.sun.fortress.nodes.GrammarMemberDecl;
import com.sun.fortress.nodes.Id;
import com.sun.fortress.nodes.ItemSymbol;
import com.sun.fortress.nodes.KeywordSymbol;
import com.sun.fortress.nodes.Node;
import com.sun.fortress.nodes.NodeDepthFirstVisitor;
import com.sun.fortress.nodes.NodeUpdateVisitor;
import com.sun.fortress.nodes.NonterminalSymbol;
import com.sun.fortress.nodes.OptionalSymbol;
import com.sun.fortress.nodes.PrefixedSymbol;
import com.sun.fortress.nodes.RepeatOneOrMoreSymbol;
import com.sun.fortress.nodes.RepeatSymbol;
import com.sun.fortress.nodes.SyntaxSymbol;
import com.sun.fortress.nodes.UnparsedTransformer;
import com.sun.fortress.nodes.TokenSymbol;
import com.sun.fortress.nodes.Type;
import com.sun.fortress.nodes_util.NodeFactory;
import com.sun.fortress.nodes_util.NodeUtil;
import com.sun.fortress.nodes_util.Span;
import com.sun.fortress.parser_util.IdentifierUtil;
import com.sun.fortress.syntax_abstractions.environments.GrammarEnv;
import com.sun.fortress.syntax_abstractions.environments.MemberEnv;
import com.sun.fortress.useful.HasAt;
import com.sun.fortress.useful.Debug;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.tuple.Option;

/* ItemDisambiguator
 * - infers names for syntax symbols without a prefix
 *    eg, A -> A:A
 * - Disambiguates "items" to nonterminals or keywords/tokens
 * - Disambiguates occurrences of nonterminal names within patterns
 *    eg, Expr -> FortressSyntax.Expression.Expr
 */
public class ItemDisambiguator extends NodeUpdateVisitor {

    private Collection<StaticError> _errors;
    private GlobalEnvironment _globalEnv;
    private GrammarIndex _currentGrammarIndex;
    private ApiIndex _currentApi;
    private String _currentItem;

    public ItemDisambiguator(GlobalEnvironment env) {
        this._errors = new LinkedList<StaticError>();
        this._globalEnv = env;
    }

    private void error(String msg, HasAt loc) {
        this._errors.add(StaticError.make(msg, loc));
    }

    public Option<GrammarIndex> grammarIndex(Id name) {
        if (name.getApi().isSome()) {
            APIName api = name.getApi().unwrap();
            if (this._globalEnv.definesApi(api)) {
                return Option.some(_globalEnv.api(api).grammars().get(name.getText()));
            }
            else {
                return Option.none();
            }
        }
        return Option.some(((ApiIndex) _currentApi).grammars().get(name));
    }

    public Collection<StaticError> errors() {
        return this._errors;
    }

    @Override
    public Node forApi(Api that) {
        if (this._globalEnv.definesApi(that.getName())) {
            this._currentApi = this._globalEnv.api(that.getName());
        }
        else {
            error("Undefined api ", that);
        }
        return super.forApi(that);
    }

    @Override
    public Node forGrammarDef(GrammarDef that) {
        Option<GrammarIndex> index = this.grammarIndex(that.getName());
        if (index.isSome()) {
            this._currentGrammarIndex = index.unwrap();
        }
        else {
            error("Grammar "+that.getName()+" not found", that);
        }
        return super.forGrammarDef(that);
    }
    
    @Override public Node forUnparsedTransformer(UnparsedTransformer that) {
        NonterminalNameDisambiguator nnd = new NonterminalNameDisambiguator(this._globalEnv);
        Option<Id> oname = nnd.handleNonterminalName(new NonterminalEnv(this._currentGrammarIndex), that.getNonterminal());

        if (oname.isSome()) {
            return new UnparsedTransformer( that.getTransformer(), oname.unwrap() ); 
        } else {
            throw new MacroError( "Cannot find non-terminal " + that.getNonterminal() );
        }
    }

    @Override
    public SyntaxSymbol forItemSymbol(ItemSymbol that) {
        SyntaxSymbol n = nameResolution(that);
        if (n == null) {
            error("Unknown item symbol: "+that, that);
        }
        if (n instanceof NonterminalSymbol ||
            n instanceof KeywordSymbol) {
            this._currentItem = that.getItem();
        }
        Debug.debug( Debug.Type.SYNTAX, 4, "Resolve item symbol " + that.getItem() + " to " + n );
        return n;
    }

    private SyntaxSymbol nameResolution(ItemSymbol item) {
        if (IdentifierUtil.validId(item.getItem())) {
            // GrammarAnalyzer<GrammarIndex> ga = new GrammarAnalyzer<GrammarIndex>();
            // NonterminalEnv env = new NonterminalEnv(this._currentGrammarIndex);
            Id name = makeId(item.getSpan(), item.getItem());
            NonterminalNameDisambiguator nnd = new NonterminalNameDisambiguator(this._globalEnv);
            Option<Id> oname = nnd.handleNonterminalName(new NonterminalEnv(this._currentGrammarIndex), name);

            if (oname.isSome()) {
                name = oname.unwrap();
                // Set<Id> setOfNonterminals = ga.getContained(name.getText(), this._currentGrammarIndex);
                // Set<Id> setOfNonterminals = env.declaredNonterminalNames(name.getText());

                return makeNonterminal(item, name);

                /*
                  if (setOfNonterminals.size() == 1) {
                  this._errors.addAll(nnd.errors());
                  Debug.debug( Debug.Type.SYNTAX, 4, "Disambiguate " + name + " to " + IterUtil.first(setOfNonterminals));
                  return makeNonterminal(item, IterUtil.first(setOfNonterminals));
                  }

                  if (setOfNonterminals.size() > 1) {
                  this._errors.addAll(nnd.errors());
                  error("Production name may refer to: " + NodeUtil.namesString(setOfNonterminals), name);
                  return makeNonterminal(item, name);
                  }
                */
            }
            return makeKeywordSymbol(item);
        }
        return makeTokenSymbol(item);
    }

    private NonterminalSymbol makeNonterminal(ItemSymbol that, Id name) {
        return new NonterminalSymbol(that.getSpan(), name);
    }

    private KeywordSymbol makeKeywordSymbol(ItemSymbol that) {
        return new KeywordSymbol(that.getSpan(), that.getItem());
    }

    private TokenSymbol makeTokenSymbol(ItemSymbol that) {
        return new TokenSymbol(that.getSpan(), that.getItem());
    }

    private static Id makeId(Span span, String item) {
        int lastIndexOf = item.lastIndexOf('.');
        if (lastIndexOf != -1) {
            APIName apiName = NodeFactory.makeAPIName(item.substring(0, lastIndexOf));
            return NodeFactory.makeId(span, apiName, NodeFactory.makeId(item.substring(lastIndexOf+1)));
        }
        else {
            return NodeFactory.makeId(span, item);
        }
    }

    @Override
    public Node forPrefixedSymbolOnly(final PrefixedSymbol prefix,
                                      Option<Id> id_result, Option<Type> type_result, SyntaxSymbol symbol_result) {
        String varName = symbol_result.accept(new PrefixHandler());
        if (id_result.isNone()) {
            if (!IdentifierUtil.validId(varName)) {
                return symbol_result;
            }
            Debug.debug( Debug.Type.SYNTAX, 3, "Create new prefixed symbol with id '" + varName + "'" );
            return handle(prefix, symbol_result, varName);
        }
        else {
            return new PrefixedSymbol(prefix.getSpan(), id_result, type_result, symbol_result);
        }
    }

    private Node handle(PrefixedSymbol prefix, SyntaxSymbol that, String varName) {
        Id var = NodeFactory.makeId(that.getSpan(), varName);
        return new PrefixedSymbol(prefix.getSpan(), Option.wrap(var), prefix.getType(), that);
    }

    private class PrefixHandler extends NodeDepthFirstVisitor<String> {
        @Override
        public String defaultCase(Node that) {
            return "";
        }

        @Override
        public String forItemSymbol(ItemSymbol that) {
            return that.getItem(); // handle(that, that.getItem());
        }

        @Override
        public String forTokenSymbol(TokenSymbol that) {
            return that.getToken(); // handle(that, that.getItem());
        }

        @Override
        public String forNonterminalSymbol(NonterminalSymbol that) {
            return _currentItem; // handle(that, _currentItem);
            // return that.getName();
        }

        @Override
        public String forKeywordSymbol(KeywordSymbol that) {
            return that.getToken(); // handle(that, that.getToken());
        }

        @Override
        public String forRepeatOneOrMoreSymbolOnly(RepeatOneOrMoreSymbol that, String symbol_result) {
            return symbol_result;
        }

        @Override
        public String forRepeatSymbolOnly(RepeatSymbol that, String symbol_result) {
            return symbol_result;
        }

        @Override
        public String forOptionalSymbolOnly(OptionalSymbol that,
                                            String symbol_result) {
            return symbol_result;
        }
    }

}
