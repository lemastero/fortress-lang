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
package com.sun.fortress.parser.preparser;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import xtc.util.State;
import com.sun.fortress.nodes.Id;
import com.sun.fortress.nodes.IdOrOp;
import com.sun.fortress.nodes.Op;
import com.sun.fortress.nodes_util.NodeFactory;
import com.sun.fortress.nodes_util.NodeUtil;
import com.sun.fortress.nodes_util.Span;
import com.sun.fortress.parser_util.precedence_resolver.PrecedenceMap;
import com.sun.fortress.useful.Debug;

/**
 * PreParser state for checking delimiter matching.  Note that
 * this class supports only a single state-modifying transaction.
 * In other words, calls to {@link #start()}, {@link #commit()},
 * and {@link #abort()} cannot be nested within each other.
 */
public class PreParserState implements State {

    /** The list of left delimiters. */
    private List<IdOrOp> lefts;

    /** The beginning keywords matching with "end". */
    private List<String> keywords;

    /** The file to report mismatched delimiters. */
    private BufferedWriter writer;

    /** Nested component/API definitions are not allowed. */
    private boolean sawCompilation = false;

    /** Create a new preparser state object. */
    public PreParserState() {
        reset("No file yet!");
    }

    /** Initialize the map of matching keywords. */
    public void init(BufferedWriter wr) {
        writer = wr;
        String[] ends = new String[]{"api", "component", "trait", "object", "grammar",
                                     "case", "if", "label", "try", "typecase"};
        keywords = new ArrayList<String>(java.util.Arrays.asList(ends));
    }

    public void reset(String file) {
        lefts = new ArrayList<IdOrOp>();
    }

    public void start() { }
    public void commit() { }
    public void abort() { }

    /** Record a left delimiter. */
    public void left(IdOrOp open) {
        Debug.debug( Debug.Type.PARSER, 1, "Left delimiter " + open );
        String token = open.getText();
        if ( token.equals("component") || token.equals("api") ) {
            if ( sawCompilation )
                log(NodeUtil.getSpan(open),
                    "Nested " + token + " definitions are not allowed.");
            sawCompilation = true;
        }
        if ( lefts.isEmpty() || !isQuote(lefts.get(0).getText()) )
            lefts.add(0, open);
    }

    public void left(Span span, String open) {
        left( NodeFactory.makeId(span, open) );
    }

    /** Check a right delimiter. */
    public void right(IdOrOp close) {
        Debug.debug( Debug.Type.PARSER, 1, "Right delimiter " + close );
        if ( lefts.isEmpty() ) {
            Debug.debug( Debug.Type.PARSER, 1,
                         "right: Unmatched delimiter \"" + close + "\"." );
            log(NodeUtil.getSpan(close),
                "Unmatched delimiter \"" + close + "\".");
        } else {
            IdOrOp open = lefts.remove(0);
            /* (if ... )        -- covered by matches
             * (if ... end)     -- covered by matches
             * (if ... end ...) -- here
             */
            if ( open.getText().equals("(if") && close.getText().equals("end") ) {
                lefts.add(0, NodeFactory.makeId(NodeUtil.getSpan(open), "("));
            } else if ( ! matches(open, close) ) {
                Debug.debug( Debug.Type.PARSER, 1,
                             "right: Unmatched delimiter \"" + close + "\"." );
                log(NodeUtil.spanTwo(NodeUtil.getSpan(open),
                                     NodeUtil.getSpan(close)),
                    "Unmatched delimiters \"" + open + "\" and \"" + close + "\".");
            }
        }
    }

    public void right(Span span, String close) {
        right( NodeFactory.makeId(span, close) );
    }

    public void quote(Span span, String token) {
        Debug.debug( Debug.Type.PARSER, 1, "Quote " + token);
        if ( lefts.isEmpty() )
            left( NodeFactory.makeId(span, token) );
        else {
            if ( matches(lefts.get(0).getText(), token) )
                lefts.remove(0);
            else
                left( NodeFactory.makeId(span, token) );
        }
    }

    public void report() {
        if ( ! lefts.isEmpty() ) {
            for ( IdOrOp left : lefts ) {
                Debug.debug( Debug.Type.PARSER, 1,
                             "report: Unmatched delimiter \"" + left + "\"." );
                log(NodeUtil.getSpan(left),
                    "Unmatched delimiter \"" + left + "\".");
            }
        }
    }

    private void log(Span span, String message) {
        NodeUtil.log(writer, span, message);
    }

    private boolean isQuote(String token) {
        String[] all = new String[]{"\\\"", "\u201c"};
        List<String> quotes = new ArrayList<String>(java.util.Arrays.asList(all));
        return quotes.contains( token );
    }

    /** Check whether the given delimiters are matching delimiters. */
    private boolean matches(IdOrOp open, IdOrOp close) {
        String left = open.getText();
        String right = close.getText();
        if ( open  instanceof Id && close instanceof Id )
            return matches(left, right);
        else if ( open  instanceof Op && close instanceof Op )
            return PrecedenceMap.ONLY.matchedBrackets(left, right);
        else return false;
    }

    private boolean matches(String left, String right) {
        if ( keywords.contains(left) ) return right.equals("end");
        else if ( left.equals("do") )
            return right.equals("end") || right.equals("also");
        else if ( left.equals("(if") ) return ( right.equals(")") || right.equals("end)") );
        else if ( left.equals("(") ) return right.equals(")");
        else if ( left.equals("[") ) return right.equals("]");
        else if ( left.equals("{") ) return right.equals("}");
        else if ( left.equals("[\\") || left.equals("\u27e6") )
            return ( right.equals("\\]") || right.equals("\u27e7") );
        else if ( left.equals("\\\"") || left.equals("\u201c") )
            return ( right.equals("\\\"") || right.equals("\u201d") );
        else if ( left.equals("`") || left.equals("'") || left.equals("\u2018") )
            return ( right.equals("'") || right.equals("\u2019") );
        else return false;
    }
}
