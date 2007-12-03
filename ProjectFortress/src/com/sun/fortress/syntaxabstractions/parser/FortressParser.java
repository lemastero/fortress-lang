package com.sun.fortress.syntaxabstractions.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import xtc.parser.ParseError;
import xtc.parser.ParserBase;
import xtc.parser.SemanticValue;

import com.sun.fortress.compiler.GlobalEnvironment;
import com.sun.fortress.compiler.Parser;
import com.sun.fortress.compiler.StaticError;
import com.sun.fortress.compiler.StaticPhaseResult;
import com.sun.fortress.compiler.Parser.Error;
import com.sun.fortress.nodes.AliasedDottedName;
import com.sun.fortress.nodes.Api;
import com.sun.fortress.nodes.CompilationUnit;
import com.sun.fortress.nodes.Component;
import com.sun.fortress.nodes.DottedName;
import com.sun.fortress.nodes.Import;
import com.sun.fortress.nodes.ImportApi;
import com.sun.fortress.nodes.ImportFrom;
import com.sun.fortress.nodes_util.NodeUtil;
import com.sun.fortress.syntaxabstractions.FileBasedMacroCompiler;
import com.sun.fortress.syntaxabstractions.MacroCompiler;
import com.sun.fortress.syntaxabstractions.MacroCompiler.Result;
import com.sun.fortress.syntaxabstractions.rats.util.ParserMediator;
import com.sun.fortress.useful.Useful;

import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Box;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.SimpleBox;

public class FortressParser {

    public static class Result extends StaticPhaseResult {
        private final Iterable<Api> _apis;
        private final Iterable<Component> _components;

        public Result() {
            _apis = IterUtil.empty();
            _components = IterUtil.empty();
        }

        public Result(Api api) {
            _apis = IterUtil.singleton(api);
            _components = IterUtil.empty();
        }

        public Result(Iterable<? extends StaticError> errors) {
			super(errors);
			_apis = IterUtil.empty();
            _components = IterUtil.empty();
		}

		public Result(Component component) {
            _components = IterUtil.singleton(component);
            _apis = IterUtil.empty();
        }

        public Result(StaticError error) {
            super(IterUtil.singleton(error));
            _apis = IterUtil.empty();
            _components = IterUtil.empty();
        }

        public Result(Result r1, Result r2) {
            super(r1, r2);
            _apis = IterUtil.compose(r1._apis, r2._apis);
            _components = IterUtil.compose(r1._components, r2._components);
        }

        public Iterable<Api> apis() { return _apis; }
        public Iterable<Component> components() { return _components; }
    }

    public static class Error extends StaticError {
        private final ParseError _parseError;
        private final ParserBase _parser;

        public Error(ParseError parseError, ParserBase parser) {
            _parseError = parseError;
            _parser = parser;
        }

        public String typeDescription() { return "Parse Error"; }

        public String description() {
            String result = _parseError.msg;
            // TODO: I don't know for sure whether this is allowed to be null
            if (result == null || result.equals("")) { result = "Unspecified cause"; }
            return result;
        }

        public String at() {
            if (_parseError.index == -1) { return "Unspecified location"; }
            else { return _parser.location(_parseError.index).toString(); }
        }
    }

    /**
     * Parse the given files and any additional files that are expected to contain
     * referenced APIs.
     */
    public static Result parse(Iterable<? extends File> files,
                               final GlobalEnvironment env) {
        // box allows mutation of a final var
        final Box<Result> result = new SimpleBox<Result>(new Result());

        Set<File> fileSet = new HashSet<File>();
        for (File f : files) { fileSet.add(canonicalRepresentation(f)); }

        Lambda<File, Set<File>> parseAndGetDepends = new Lambda<File, Set<File>>() {
            public Set<File> value(File f) {
                Result r = parse(f, env);
                result.set(new Result(result.value(), r));
                if (r.isSuccessful()) {
                    Set<File> newFiles = new HashSet<File>();
                    for (CompilationUnit cu :
                             IterUtil.compose(r.apis(), r.components())) {
                        newFiles.addAll(extractNewDependencies(cu, env));
                    }
                    return newFiles;
                }
                else { return Collections.emptySet(); }
            }
        };

        // parses all dependency files
        CollectUtil.graphClosure(fileSet, parseAndGetDepends);
        return result.value();
    }

    /** Parses a single file. */
    public static Result parse(File f, final GlobalEnvironment env) {
        try {
            BufferedReader in = Useful.utf8BufferedFileReader(f);
            try {
            	
            	PreParser.Result ppr = PreParser.parse(f, env);
    			if (!ppr.isSuccessful()) { return new Result(ppr.errors()); }

    			xtc.parser.Result parseResult = null; 
    			ParserBase p = null;
    			
    			System.out.print("Parsing file: "+f.getName());
    			if (!ppr.getGrammars().isEmpty()) {
    				
    				// Compile the macro declarations and create a temporary parser
    				MacroCompiler macroCompiler = new FileBasedMacroCompiler();
    				MacroCompiler.Result tr = macroCompiler.compile(ppr.getGrammars());
    				if (!tr.isSuccessful()) { return new Result(tr.errors()); }
    				Class<?> temporaryParserClass = tr.getParserClass();
    				
    				try {
						p = ParserMediator.getParser(temporaryParserClass, in, f.toString());
						parseResult = ParserMediator.parse();
    				} catch (Exception e) {
    					String desc = "Error occured while instantiating and executing a temporary parser: "+temporaryParserClass.getCanonicalName();
    		            if (e.getMessage() != null) { desc += " (" + e.getMessage() + ")"; }
    		            return new Result(StaticError.make(desc, f.toString()));
					} 
    				
    			}
    			else {
                    p = new com.sun.fortress.parser.Fortress(in, f.toString());
                    parseResult = ((com.sun.fortress.parser.Fortress) p).pFile(0);
    			}
            	

                if (parseResult.hasValue()) {
                    Object cu = ((SemanticValue) parseResult).value;
                    if (cu instanceof Api) {
                        return new Result((Api) cu);
                    }
                    else if (cu instanceof Component) {
                        return new Result((Component) cu);
                    }
                    else {
                        throw new RuntimeException("Unexpected parse result: " + cu);
                    }
                }
                else {
                    return new Result(new Parser.Error((ParseError) parseResult, p));
                }
            }
            finally { in.close(); }
        }
        catch (FileNotFoundException e) {
            return new Result(StaticError.make("Cannot find file " + f.getName(),
                                               f.toString()));
        }
        catch (IOException e) {
            String desc = "Unable to read file";
            if (e.getMessage() != null) { desc += " (" + e.getMessage() + ")"; }
            return new Result(StaticError.make(desc, f.toString()));
        }
    }

    /**
     * Get all files potentially containing APIs imported by cu that aren't
     * currently in fortress.
     */
    private static Set<File> extractNewDependencies(CompilationUnit cu,
                                                    GlobalEnvironment env) {
        Set<DottedName> importedApis = new LinkedHashSet<DottedName>();
        for (Import i : cu.getImports()) {
            if (i instanceof ImportApi) {
                for (AliasedDottedName apiAlias : ((ImportApi) i).getApis()) {
                    importedApis.add(apiAlias.getApi());
                }
            }
            else { // i instanceof ImportFrom
                importedApis.add(((ImportFrom) i).getApi());
            }
        }

        Set<File> result = new HashSet<File>();
        for (DottedName n : importedApis) {
            if (!env.definesApi(n)) {
                File f = canonicalRepresentation(fileForApiName(n));
                if (IOUtil.attemptExists(f)) { result.add(f); }
            }
        }
        return result;
    }

    /** Get the filename in which the given API should be defined. */
    private static File fileForApiName(DottedName api) {
        return new File(NodeUtil.nameString(api) + ".fsi");
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
    
}