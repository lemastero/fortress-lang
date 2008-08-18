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
package com.sun.fortress.syntax_abstractions.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;

import xtc.parser.ParseError;
import xtc.parser.ParserBase;
import xtc.parser.SemanticValue;

import com.sun.fortress.compiler.GlobalEnvironment;
import com.sun.fortress.compiler.index.GrammarIndex;
import com.sun.fortress.compiler.index.ApiIndex;
import com.sun.fortress.compiler.Parser;
import com.sun.fortress.compiler.Parser.Result;
import com.sun.fortress.exceptions.ParserError;
import com.sun.fortress.exceptions.StaticError;
import com.sun.fortress.repository.ProjectProperties;
import com.sun.fortress.nodes.AliasedAPIName;
import com.sun.fortress.nodes.Api;
import com.sun.fortress.nodes.CompilationUnit;
import com.sun.fortress.nodes.Component;
import com.sun.fortress.nodes.APIName;
import com.sun.fortress.nodes.Id;
import com.sun.fortress.nodes.Import;
import com.sun.fortress.nodes.ImportApi;
import com.sun.fortress.nodes.ImportedNames;
import com.sun.fortress.nodes.GrammarDef;
import com.sun.fortress.nodes.Node;
import com.sun.fortress.nodes_util.NodeUtil;
import com.sun.fortress.nodes_util.ASTIO;
import com.sun.fortress.syntax_abstractions.ComposingMacroCompiler;
import com.sun.fortress.syntax_abstractions.phases.Transform;
import com.sun.fortress.syntax_abstractions.rats.util.ParserMediator;
import com.sun.fortress.useful.Path;
import com.sun.fortress.useful.Useful;
import com.sun.fortress.useful.Debug;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.tuple.Option;

public class FortressParser {


    /** Parses a single file. */
    public static Result parse(APIName api_name, 
                               File f, 
                               GlobalEnvironment env, 
                               boolean verbose) {
        try {
            if (!ProjectProperties.noPreparse) {
                return parseInner(api_name, f, env, verbose);
            } else {
                return new Result(Parser.parseFileConvertExn(api_name, f),
                                  f.lastModified());
            }
        } catch (StaticError se) {
            return new Result(se);
        }
    }

    private static Result parseInner(APIName api_name,
                                     File f,
                                     final GlobalEnvironment env,
                                     boolean verbose) {
        // throws StaticError, ParserError

        PreParser.Result ppr = PreParser.parse(api_name, f, env);
        if (!ppr.isSuccessful()) {
            return new Result(ppr.errors());
        }
        if (verbose) {
            System.err.println("Parsing files: "+f.getName());
        }
        if (ppr.getGrammars().isEmpty()) {
            return new Result(Parser.parseFileConvertExn(api_name, f), 
                              f.lastModified());
        }

        Collection<GrammarIndex> grammars = ppr.getGrammars();
        initializeGrammarIndexExtensions( env.apis().values(), grammars );

        // Compile the syntax abstractions and create a temporary parser
        Class<?> temporaryParserClass = 
            ComposingMacroCompiler.parserForComponent(grammars);

        Debug.debug( Debug.Type.SYNTAX, 2, "Created temporary parser" );

        BufferedReader in = null; 
        try {
            in = Useful.utf8BufferedFileReader(f);
            ParserBase p =
                ParserMediator.getParser(api_name, temporaryParserClass, in, f.toString());
            CompilationUnit original = Parser.checkResultCU(ParserMediator.parse(p), p, f.getName());
            // dump(original, "original-" + f.getName());
            CompilationUnit cu = (CompilationUnit) Transform.transform(env, original);
            // dump(cu, "dump-" + f.getName());
            return new Result(cu, f.lastModified());
        } catch (Exception e) {
            String desc =
                "Error occurred while instantiating and executing a temporary parser: "
                + temporaryParserClass.getCanonicalName();
            e.printStackTrace();
            if (e.getMessage() != null) { desc += " (" + e.getMessage() + ")"; }
            return new Result(StaticError.make(desc, f.toString()));
        } finally {
            if (in != null) try { in.close(); } catch (IOException ioe) {}
        }
    }

    public static Collection<? extends StaticError> initializeGrammarIndexExtensions( Collection<ApiIndex> apis, Collection<GrammarIndex> grammars) {
        List<StaticError> errors = new LinkedList<StaticError>();

        Map<String, GrammarIndex> grammarMap = new HashMap<String, GrammarIndex>();
        for (ApiIndex a2: apis) {
            for (Map.Entry<String, GrammarIndex> e: a2.grammars().entrySet()) {
                grammarMap.put(e.getKey(), e.getValue());
            }
        }

        for ( GrammarIndex grammar : grammars ){
            Option<GrammarDef> og = grammar.ast();
            if (og.isSome()) {
                List<GrammarIndex> ls = new LinkedList<GrammarIndex>();
                for (Id n: og.unwrap().getExtends()) {
                    Debug.debug( Debug.Type.SYNTAX, 3, "Add grammar " + n.getText() + "[" + grammarMap.get(n.getText()) + "] to the extends list of " + grammar );
                    ls.add(grammarMap.get(n.getText()));
                }
                Debug.debug( Debug.Type.SYNTAX, 3, "Grammar " + grammar.getName() + " extends " + ls );
                grammar.setExtended(ls);
            } else {
                Debug.debug( Debug.Type.SYNTAX, 3, "Grammar " + grammar.getName() + " has no ast" );
            }
        }
        return errors;
    }


    /**
     * Get all files potentially containing APIs imported by cu that aren't
     * currently in fortress.
     */
    private static Set<File> extractNewDependencies(CompilationUnit cu,
                                                    GlobalEnvironment env, Path p) {
        Set<APIName> importedApis = new LinkedHashSet<APIName>();
        for (Import i : cu.getImports()) {
            if (i instanceof ImportApi) {
                for (AliasedAPIName apiAlias : ((ImportApi) i).getApis()) {
                    importedApis.add(apiAlias.getApi());
                }
            }
            else { // i instanceof ImportedNames
                importedApis.add(((ImportedNames) i).getApi());
            }
        }

        Set<File> result = new HashSet<File>();
        for (APIName n : importedApis) {
            if (!env.definesApi(n)) {
                try {
                    File f = canonicalRepresentation(fileForApiName(n, p));
                    // Believe this test is redundant with thrown exception,
                    // but leave it for now.
                    if (IOUtil.attemptExists(f)) {
                        result.add(f);
                    }
                } catch (FileNotFoundException ex) {
                    // do nothing?
                }
            }
        }
        return result;
    }

    /** Get the filename in which the given API should be defined. */
    private static File fileForApiName(APIName api, Path p) throws FileNotFoundException {
        return p.findFile(NodeUtil.dirString(api) + ".fsi");
    }

    /**
     * Convert to a filename that is canonical for each (logical) file, preventing
     * reparsing the same file.
     */
    private static File canonicalRepresentation(File f) {
        // treat the same absolute path as the same file; different absolute path but
        // the same *canonical* path (a symlink, for example) is treated as two
        // different files; if absolute file can't be determined, assume they are
        // distinct.
        return IOUtil.canonicalCase(IOUtil.attemptAbsoluteFile(f));
    }

    private static void dump( Node node, String name ){
        try{
            ASTIO.writeJavaAst( (CompilationUnit) node, name );
            Debug.debug( Debug.Type.SYNTAX, 1, "Dumped node to " + name );
        } catch ( IOException e ){
            e.printStackTrace();
        }
    }

}
