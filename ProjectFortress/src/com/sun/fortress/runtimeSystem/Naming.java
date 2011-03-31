/*******************************************************************************
    Copyright 2009,2011, Oracle and/or its affiliates.
    All rights reserved.


    Use is subject to license terms.

    This distribution may include materials developed by third parties.

******************************************************************************/
package com.sun.fortress.runtimeSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.fortress.compiler.NamingCzar;
import com.sun.fortress.nodes_util.NodeFactory;
import com.sun.fortress.useful.CheapSerializer;
import com.sun.fortress.useful.Pair;

public class Naming {
    
    public final static
    
    CheapSerializer.PAIR<
        String,
        List<Pair<String, String>>> xlationSerializer = 
            
            new CheapSerializer.PAIR<String,List<Pair<String, String>>>(
                    CheapSerializer.STRING,
                    new CheapSerializer.LIST<Pair<String, String>>(
                            new CheapSerializer.PAIR<String, String>(
                                    CheapSerializer.STRING,
                                    CheapSerializer.STRING)
                            )
                    )
        ;

    public final static int TUPLE_ORIGIN = 1;
    public final static int STATIC_PARAMETER_ORIGIN = 1;
    public final static String STATIC_PARAMETER_GETTER_SIG = "()Ljava/lang/Object;";
    public final static String STATIC_PARAMETER_FIELD_DESC = "Ljava/lang/Object;";
    
    // Used to indicate translation convention to apply to type parameter.
    public final static String FOREIGN_TAG = "\u2615"; // hot beverage == JAVA
    public final static String NORMAL_TAG = "\u263a"; // smiley face == normal case.
    public final static String INTERNAL_TAG = "\u26a0"; // warning sign -- internal use only (fortress.)

    public final static String ENVELOPE = "\u2709"; // Signals necessary closure
    public final static String SNOWMAN = "\u2603"; // for empty tuple, sigh.
    public final static String INDEX = "\u261e";  // "__"; // "\u261e"; // white right point index (for dotted of functional methods)
    public final static String BOX = "\u2610"; // ballot box, used to indicate prefix or postfix.
    
    public final static String HEAVY_X = "\u2716"; // heavy X -- stop rewriting in an instantiation, for schema tags

    public static final String UP_INDEX = "\u261d"; // Special static parameter for static type of self in generic method invocation
    
    public static final String BALLOT_BOX_WITH_CHECK = "\u2611"; // boolean static param
    public static final String SCALES = "\u2696"; // dimension static param
    public static final String MUSIC_SHARP = "\u266f"; // int static param
    public static final String MUSIC_NATURAL = "\u266e"; // nat(ural) static param
    public static final String HAMMER_AND_PICK = "\u2692"; // opr static param
    public static final String YINYANG = "\u262f"; // type static param
    public static final String ATOM = "\u269b"; // unit static param

    public static final String GENERIC_TAGS =
        BALLOT_BOX_WITH_CHECK + SCALES + MUSIC_SHARP +
        HAMMER_AND_PICK + YINYANG + ATOM;

    public static final String ENTER = "\u2386";

    public static final String INTERNAL_SNOWMAN = INTERNAL_TAG + SNOWMAN;

    public final static char FOREIGN_TAG_CHAR = FOREIGN_TAG.charAt(0);
    public final static char NORMAL_TAG_CHAR = NORMAL_TAG.charAt(0);
    public final static char INTERNAL_TAG_CHAR = INTERNAL_TAG.charAt(0);
    public static final char BALLOT_BOX_WITH_CHECK_CHAR = BALLOT_BOX_WITH_CHECK.charAt(0);
    public static final char SCALES_CHAR = SCALES.charAt(0);
    public static final char MUSIC_SHARP_CHAR = MUSIC_SHARP.charAt(0);
    public static final char MUSIC_NATURAL_CHAR = MUSIC_NATURAL.charAt(0);
    public static final char HAMMER_AND_PICK_CHAR = HAMMER_AND_PICK.charAt(0);
    public static final char YINYANG_CHAR = YINYANG.charAt(0);
    public static final char ATOM_CHAR = ATOM.charAt(0);
    public static final char HEAVY_X_CHAR = HEAVY_X.charAt(0);

    public final static String GEAR = "\u2699";

    public final static char LEFT_OXFORD_CHAR = '\u27e6';
    public final static char RIGHT_OXFORD_CHAR = '\u27e7';
    public final static String LEFT_OXFORD = "\u27e6";
    public final static String RIGHT_OXFORD = "\u27e7";

    /**
     * Name for Arrow-interface generic.
     */
    public final static String ARROW_TAG="Arrow";
    /**
     * Name for Tuple-interface generic.
     */
    public final static String TUPLE_TAG="Tuple";
    public final static String TUPLE_RTTI_TAG="Tuple,";

    public final static String COMPILER_BUILTIN = "CompilerBuiltin";
    public final static String NATIVE_PREFIX_DOT = "native.";

    public final static String APPLY_METHOD = "apply";
    public final static String APPLIED_METHOD = "the_function";
    
    // By default, the static and instance methods would have the same name, which does not work.
    public final static String GENERIC_METHOD_FINDER_SUFFIX_IN_TRAIT = "_static";

    public static final String runtimeValues = "com/sun/fortress/compiler/runtimeValues/";

    // java.lang.Object correctly formatted for asm generation
    public static final String javaObject = "java/lang/Object";
    
    /* Sometimes a generated constant or string depends on a type parameter.
     * In order to efficiently insert the substituted value, it is encoded
     * into a (bogus) method invocation returning the appropriate type.
     * For example, INTERP.hash"this is some string constant"() (returns long).
     * When the Instantiating classloader encounters such a method invocation,
     * it performs the appropriate calculation and replacement.
     * Plan, likely, is to encode strings with a count and characters.
     */
    public static final String magicInterpClass = "CONST";
    public static final String hashMethod = "hash";
    public static final String stringMethod = "String";

    /**
     * Java descriptors for (boxed) Fortress types, INCLUDING leading L and trailing ;
     */
    static Map<String, String> specialFortressDescriptors = new HashMap<String, String>();
    /**
     * Java descriptors for (boxed) Fortress types, WITHOUT leading L and trailing ;
     */
    static Map<String, String> specialFortressTypes = new HashMap<String, String>();


    static void bl(String lib, String ft, String cl) {
        cl = runtimeValues + cl;
        specialFortressDescriptors.put(lib+ft, "L" + cl + ";");
        specialFortressTypes.put(lib+ft, cl );
    }

    static {
        /*
         * This code is duplicated, mostly, in runtime Naming.java,
         * except that it deals only in strings.
         */
        bl(COMPILER_BUILTIN, "$Boolean", "FBoolean");
        bl(COMPILER_BUILTIN, "$Char", "FChar");
        bl(COMPILER_BUILTIN, "$RR32", "FRR32");
        bl(COMPILER_BUILTIN, "$RR64", "FRR64");
        bl(COMPILER_BUILTIN, "$ZZ32", "FZZ32");
        bl(COMPILER_BUILTIN, "$ZZ64", "FZZ64");
        bl(COMPILER_BUILTIN, "$String", "FString");
        bl("", SNOWMAN, "FVoid");
        bl("", INTERNAL_SNOWMAN, "FVoid");
    }

    public static String opForString(String op, String s) {
        return op + "." + s;
    }

    
    private final static String magicDot = magicInterpClass + "." ;
    public static boolean isEncodedConst(String s) {
        return s.startsWith(magicDot);
    }

    public static String encodedOp(String s) {
        int first = s.indexOf('.');
        return s.substring(0, first);
    }
    
    public static String encodedConst(String s) {
        int first = s.indexOf('.');
        return s.substring(first+1);      
    }
    
    /**
     * (Symbolic Freedom) Dangerous characters should not appear in JVM identifiers
     */
    private static final String SF_DANGEROUS = "/.;$<>][:";
    /**
     * (Symbolic Freedom) Escape characters have a translation if they appear following
     * a backslash.
     * Note omitted special case -- leading \= is empty string.
     * Note note \ by itself is not escaped unless it is followed by
     * one of the escape characters.
     */
    private static final String    SF_ESCAPES = "|,?%^_}{!-";
    /**
     * (Symbolic Freedom) Translations of escapes, in corresponding order.
     */
    private static final String SF_TRANSLATES = "/.;$<>][:\\";

    private static final String SF_FIRST_ESCAPES = SF_ESCAPES + "=";

    public static final String FUNCTION_GENERIC_TAG = "function";
    public static final String TRAIT_GENERIC_TAG = "trait";
    public static final String OBJECT_GENERIC_TAG = "object";

    public static String javaDescForTaggedFortressType(String ft) {

        //char ch = ft.charAt(0);
        String tx = specialFortressDescriptors.get(ft);
        if (tx != null) {
            return tx; // Should be correct by construction
        }
        return "L" + (ft) + ";";

//        else if (ch == NORMAL_TAG_CHAR) {
//            //return "L" + mangleFortressIdentifier(ft) + ";";
//            return "L" + (ft) + ";";
//        } else if (ch == INTERNAL_TAG_CHAR) {
//            //return "Lfortress/" + mangleFortressIdentifier(ft) + ";";
//            return "Lfortress/" + (ft) + ";";
//        } else if (ch == FOREIGN_TAG_CHAR) {
//            throw new Error("Haven't figured out JVM xlation of foreign type " + ft);
//        }
//        throw new Error("Bad fortress naming scheme tag (unicode " + Integer.toHexString(ch) +
//                    ") on fortress type " + ft);
    }

    public static String deDot(String s) {
        int lox = s.indexOf(LEFT_OXFORD_CHAR);
        if (lox == -1)
            return s.replace(".", "/");
        // don't de-dot inside of oxford brackets.
        //int rox = s.indexOf(RIGHT_OXFORD_CHAR);
        return s.substring(0, lox).replace(".", "/") +
               s.substring(lox);
    }

    public static String deDollar(String s) {
        return s.replace(".", "$");
    }

    public static String deMangle(String s) {
        if (s.length() < 2 || s.charAt(0) != '\\')
            return s;
        if (s.charAt(1) == '=')
            s = s.substring(2);
        int l = s.length();
        if (l == 0)
            return s;
        StringBuilder sb = new StringBuilder();
        boolean sawback = false;
        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
            if (sawback) {
                int j = SF_ESCAPES.indexOf(ch);
                if (j != -1) {
                    sb.append(SF_TRANSLATES.charAt(j));
                    sawback = false;
                    continue;
                }
                sb.append('\\');
            }
            if (ch == '\\') {
                sawback = true;
            } else {
                sawback = false;
                sb.append(ch);
            }
        }
        if (sawback)
            sb.append('\\');

        return sb.toString();
    }



    public static boolean likelyMangled(String s) {
        if (s.length() < 2) return false;
        if (s.charAt(0) != '\\') return false;
        // if (-1 == NF_FIRST_ESCAPES.indexOf(s.charAt(1))) return false;
        return true;
    }



    public static boolean isMangled(String s) {
        int l = s.length();
        if (l < 2) return false;
        if (s.charAt(0) != '\\') return false;
        if (s.charAt(1) == '=') return true;
        return isSubstringMangled(s, l);
    }



    /**
     * @param s
     * @param l
     * @return
     */
    private static boolean isSubstringMangled(String s, int l) {
        for (int i = 0; i < l-1; i++) {
            char ch = s.charAt(i);
            if (ch == '\\' && -1 != SF_ESCAPES.indexOf(s.charAt(i+1)))
                return true;
        }
        return false;
    }



    /**
     * Concatenates s1 and s2, preserving valid-mangling property.
     *
     * @param s1 Validly mangled ("naming freedom") JVM identifier
     * @param s2 Validly mangled ("naming freedom") JVM identifier
     * @return concatenation of s1 and s2, validly mangled if s1 and s2 were validly mangled.
     */
    public static String catMangled(String s1, String s2) {
        int l1 = s1.length();
        int l2 = s2.length();

        // Strictly speaking, empty strings are illegal inputs.
        if (l1 == 0) return s2;
        if (l2 == 0) return s1;

        boolean ms1 = likelyMangled(s1);
        boolean ms2 = likelyMangled(s2);

        if (ms1) {
            // Fancy way to encode the empty string.
            if (l1 == 2 && s1.charAt(1) == '=')
                return s2;

            if (ms2) {
                // ms2 begins with \, hence no accidental escapes
                char ch1 = s2.charAt(1);
                if (ch1 == '=') {
                    // remove embedded \=
                    return Naming.catMangledCheckingJoint(s1, s2.substring(2));
                } else if (ch1 == '-' && l2 > 2 && s2.charAt(2) == '=') {
                    // replace non-first \-= with \=
                    return s1 + "\\" + s2.substring(2);
                } else{
                  return s1 + s2;
                }
            } else {
                return Naming.catMangledCheckingJoint(s1, s2);
            }
        } else if (ms2) {
            char ch1 = s2.charAt(1);

            // If s2 is truly mangled, then prepend \= to concatenation.
                if (ch1 == '=') {
                    if (l2 == 2) {
                        // Fancy way to encode the empty string.
                        return s1;
                    }
                    // definitely mangled, but embedded \=
                    return "\\=" + Naming.catMangledCheckingJoint(s1, s2.substring(2));
                } else if (ch1 == '-' && l2 > 2 && s2.charAt(2) == '=') {
                    // Embedded \-= goes away.
                    String s2sub = s2.substring(3);
                    if (isSubstringMangled(s2sub, l2-3)) {
                        // Joints ok
                        return "\\=" + s1 + "\\=" + s2sub;
                    } else {
                        // Joints ok
                        return s1 + "\\=" + s2sub;
                    }
                } else if (isMangled(s2)) {
                    // mangled for some other reason.
                    return "\\=" + s1 + s2;
                } else {
                    // Joints ok (s2 begins with \\)
                    return s1 + s2;
                }
        } else {
            return Naming.catMangledCheckingJoint(s1, s2);
        }
    }



    /**
     * @param s1
     * @param s2
     * @return
     */
    public static String catMangledCheckingJoint(String s1, String s2) {
        int l1 = s1.length();
        int l2 = s2.length();

        if (l2 == 0)
            return s1;

        if (s1.endsWith("\\") &&
                -1 != (l1 == 1 ? SF_FIRST_ESCAPES : SF_ESCAPES).indexOf(s2.charAt(0))) {
            // must escape trailing \
            return s1.charAt(0) != '\\'
                ? "\\=" + s1 + "-" + s2
                : s1 + "-" + s2;

        } else {
            return s1 + s2;
        }
    }



    public static String catMangled(String s1, String s2, String s3) {
        return catMangled(catMangled(s1,s2), s3);
    }
    public static String catMangled(String s1, String s2, String s3, String s4) {
        return catMangled(catMangled(s1,s2), catMangled(s3,s4));
    }

    public static String mangleMethodSignature(String s) {
        StringBuilder sb = new StringBuilder();
        int l = s.length();
        int i = 0;
        while (i < l) {
            char ch = s.charAt(i);
            switch (ch) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 'V': // should only appear in return if well-formed

            case '[': // eat array indicator
            case '(': // eat intro and outro, assume well-formed
            case ')':
                sb.append(ch);
                i++;
                break;
            case 'L':
                sb.append(ch);
                i = mangleFortressIdentifier(s, i+1, sb);
                break;
            default:
                throw new Error("Was not expecting to see character " + ch + " in " + s);
            }
        }
        return sb.toString();
    }

    public static String demangleMethodSignature(String s) {
        StringBuilder sb = new StringBuilder();
        int l = s.length();
        int i = 0;
        while (i < l) {
            char ch = s.charAt(i);
            switch (ch) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 'V': // should only appear in return if well-formed

            case '[': // eat array indicator
            case '(': // eat intro and outro, assume well-formed
            case ')':
                sb.append(ch);
                i++;
                break;
            case 'L':
                sb.append(ch);
                i = demangleFortressIdentifier(s, i+1, sb);
                break;
            default:
                throw new Error("Was not expecting to see character " + ch);
            }
        }
        return sb.toString();
    }

    /**
     * Mangles the chunks of a fortress identifier, where the chunks are
     * delimited by $, /, and ; appearing outside of Oxford brackets.
     *
     * Returns either when the string is exhausted,
     * or after a semicolon is processed.
     *
     * @param s  the string to mangle
     * @param i  the index to begin at
     * @param sb the stringbuffer to which the transformed string is appended.
     * @return the index of the next character to process (if any).
     */
    private static int mangleFortressIdentifier(String s, int start, StringBuilder sb) {
        return mangleOrNotFortressIdentifier(s,start, sb,true);
    }

    public static String mangleFortressIdentifier(String s) {
        if (s == null)
            return null;
        //int l = s.length();
        // Special case of <init> and <clinit>
        if (pointyDelimitedInitMethod(s))
            return s;
        StringBuilder sb = new StringBuilder();
         mangleOrNotFortressIdentifier(s,0, sb,true);
         String t = sb.toString();
         if (t.startsWith("\\-=Arrow"))
             throw new Error("AHA!");
         return t;
    }

    public static String mangleFortressFileName(String s) {
        if (s == null)
            return null;
       
         StringBuilder sb = new StringBuilder();
         mangleOrNotFortressFileName(s,0, sb,true);
         String t = sb.toString();
         
         return t;
    }

    /**
     * Expects a type, surrounded by L;, or one of the descriptor type characters.
     * @param s
     * @return
     */
    public static String mangleFortressDescriptor(String s) {
        // This is a degenerate case of "signature"; if that is made pickier, this will not work.
        String t =  mangleMethodSignature(s);
        if (t.startsWith("\\-=Arrow"))
            throw new Error("AHA!");
        return t;
    }
    public static String demangleFortressDescriptor(String s) {
        // This is a degenerate case of "signature"; if that is made pickier, this will not work.
        return demangleMethodSignature(s);
    }

    /**
     * Mangles names of methods (and fields?).
     * Mangling includes / and $.
     * Names beginning and end with less-than and greater-than are left along
     * (init, clinit)
     *
     * @param s
     * @return
     */
    public static String mangleMemberName(String s) {
        if (s == null)
            return null;
        //int l = s.length();
        // Special case of <init> and <clinit>
        if (pointyDelimitedInitMethod(s))
            return s;
        return mangleIdentifier(s);
    }

    /**
     * DE-mangles the chunks of a fortress identifier, where the chunks are
     * delimited by $, /, and ; appearing outside of Oxford brackets.
     *
     * Returns either when the string is exhausted,
     * or after a semicolon is processed.
     *
     * @param s  the string to mangle
     * @param i  the index to begin at
     * @param sb the stringbuffer to which the transformed string is appended.
     * @return the index of the next character to process (if any).
     */
    private static int demangleFortressIdentifier(String s, int start, StringBuilder sb) {
        return mangleOrNotFortressIdentifier(s,start, sb,false);
    }

    public static String demangleFortressIdentifier(String s) {
        if (s == null)
            return null;
        //int l = s.length();

        // Special case of <init> and <clinit>
        if (pointyDelimitedInitMethod(s))
            return s;

        StringBuilder sb = new StringBuilder();
         mangleOrNotFortressIdentifier(s,0, sb, false);
         return sb.toString();
    }

    /**
     * @param s
     * @param l
     * @return
     */
    private static boolean pointyDelimitedInitMethod(String s) {
        //int l = s.length();
        return s.length() > 0 && s.charAt(0) == '<' && s.endsWith("init>");
    }


    private static int mangleOrNotFortressIdentifier(String s, int start, StringBuilder sb, boolean mangleOrNot) {
        // specials = $/;
        return mangleOrNotFortressIdentifier(s, start, sb, mangleOrNot,"$/;");
    }

    private static int mangleOrNotFortressFileName(String s, int start, StringBuilder sb, boolean mangleOrNot) {
        // specials = $/;
        return mangleOrNotFortressIdentifier(s, start, sb, mangleOrNot,"$/;.");
    }

    private static int mangleOrNotFortressIdentifier(String s, int start, StringBuilder sb, boolean mangleOrNot, String specials) {
        int l = s.length();
        int nesting = 0;

        for (int i = start; i < l; i++) {
            char ch = s.charAt(i);
            if (ch == LEFT_OXFORD_CHAR) {
                nesting++;
            } else if (ch == RIGHT_OXFORD_CHAR) {
                nesting--;
            } else if (nesting == 0 && (-1 != specials.indexOf(ch))) {
                appendNonEmptyMangledSubstring(sb, s, start, i, mangleOrNot);
                sb.append(ch);
                if (ch == ';')
                    return i+1;
                start = i+1;
            }
        }
        appendNonEmptyMangledSubstring(sb, s, start, l, mangleOrNot);
        return l;
    }


    /**
     * @param sb
     * @param s
     * @param start
     * @param i
     */
    private static void appendNonEmptyMangledSubstring(StringBuilder sb,
            String s, int start, int i, boolean mangleOrNot) {
        if (i - start > 0) {
            s = s.substring(start, i);
            sb.append(mangleOrNot ? mangleIdentifier(s) : deMangle(s));
        }
    }


    /**
         * Convert a string identifier into something that will be legal in a
         * JVM.
         *
         * http://blogs.sun.com/jrose/entry/symbolic_freedom_in_the_vm
         * Dangerous characters are the union of all characters forbidden
         * or otherwise restricted by the JVM specification, plus their mates,
         * if they are brackets.

         * @param identifier
         * @return
         */
        public static String mangleIdentifier(String identifier) {

            /* This is not quite right; accidental escapes are those
             * where the backslash is followed by one of |,?%^_{}!
             */

            // 1. In each accidental escape, replace the backslash with an escape sequence (\-)
            StringBuilder mangledStringBuilder = null;
            String mangledString = identifier;

            int l = identifier.length();
            if (l == 0)
                return "\\=";

            for (int j = 0; j < l-1; j++) {
                char ch = identifier.charAt(j);
                if (ch == '\\') {
                    ch = identifier.charAt(j+1);
                    if (-1 != SF_ESCAPES.indexOf(ch) || j == 0 && ch == '=') {
                        // found one, do the translation.
                        mangledStringBuilder = new StringBuilder(mangledString.substring(0, j+1));
                        mangledStringBuilder.append('-');
                        mangledStringBuilder.append(ch);
                         for (int i = j+2; i < l-1; i++) {
                            ch = identifier.charAt(i);
                            mangledStringBuilder.append(ch);
                            if (ch == '\\') {
                                ch = identifier.charAt(i+1);
                                if (-1 !=  SF_ESCAPES.indexOf(ch)) {
                                    // found one, do the translation.
                                    mangledStringBuilder.append('-');
                                }
                            }

                        }
                         if (j + 2 < l)
                             mangledStringBuilder.append(identifier.charAt(l-1));
                        mangledString = mangledStringBuilder.toString();
                        break;
                    }
                }
            }

    //        if (mangledString.startsWith("\\=")) {
    //            mangledString = "\\-=" + mangledString.substring(2);
    //        }

            // 2. Replace each dangerous character with an escape sequence (\| for /, etc.)

            mangledString = mangledString.replaceAll("/", "\\\\|");
            mangledString = mangledString.replaceAll("\\.", "\\\\,");
            mangledString = mangledString.replaceAll(";", "\\\\?");
            mangledString = mangledString.replaceAll("\\$", "\\\\%");
            mangledString = mangledString.replaceAll("<", "\\\\^");
            mangledString = mangledString.replaceAll(">", "\\\\_");
            mangledString = mangledString.replaceAll("\\[", "\\\\{");
            mangledString = mangledString.replaceAll("\\]", "\\\\}");
            mangledString = mangledString.replaceAll(":", "\\\\!");

            // Actually, this is NOT ALLOWED.
    //        // Non-standard name-mangling convention.  Michael Spiegel 6/16/2008
    //        mangledString = mangledString.replaceAll("\\ ", "\\\\~");

            // 3. If the first two steps introduced any change, <em>and</em> if the
            // string does not already begin with a backslash, prepend a null prefix (\=)
            if (!mangledString.equals(identifier))
                if (!(mangledString.charAt(0) == '\\'))
                mangledString = "\\=" + mangledString;

            // debugging check for double-mangling
            if (mangledString.startsWith("\\-")) {}

            return mangledString;
        }

        /**
         * Need to generalize to include BCDFIJS, too.
         * @param sig
         * @param selfIndex
         * @return
         */
    public static String removeNthSigParameter(String sig, int selfIndex) {
        // start, end, are inclusive bounds of nth parameter in sig.
        int start = 1;
        int end = sig.indexOf(';');
        for (int i = 0; i < selfIndex; i++) {
            start = end+1;
            end = sig.indexOf(';', start);
        }

        return sig.substring(0,start) + sig.substring(end+1);
    }

    /**
     * Need to generalize to include BCDFIJS, too.
     * @param sig
     * @param selfIndex
     * @return
     */
public static String replaceNthSigParameter(String sig, int selfIndex, String newParamDesc) {
    // start, end, are inclusive bounds of nth parameter in sig.
    int start = 1;
    int end = sig.indexOf(';');
    for (int i = 0; i < selfIndex; i++) {
        start = end+1;
        end = sig.indexOf(';', start);
    }

    return sig.substring(0,start) + newParamDesc + sig.substring(end+1);
}

    public static String nthSigParameter(String sig, int selfIndex) {
        // start, end, are inclusive bounds of nth parameter in sig.
        int start = 1;
        int end = sig.indexOf(';');
        for (int i = 0; i < selfIndex; i++) {
            start = end+1;
            end = sig.indexOf(';', start);
        }

        return sig.substring(start,end+1);
    }

    public static String dotToSep(String name) {
        name = name.replace('.', '/');
        return name;
    }

    /**
     * Returns the package+class name for the class generated for the closure
     * implementing a generic method.  Includes GEAR  (generic function),
     * ENVELOPE (closure), static parameters, and schema.
     * 
     * @param simple_name
     * @param static_parameters
     * @param generic_arrow_schema
     * @return
     */
    public static String genericFunctionPkgClass(String component_pkg_class, String simple_name,
            String static_parameters, String generic_arrow_schema) {
        return component_pkg_class + GEAR +"$" +
        simple_name + static_parameters + ENVELOPE + "$" + HEAVY_X + generic_arrow_schema;
    }


    //Asm requires you to call visitMaxs for every method
    // but ignores the arguments.
    public static final int ignoredMaxsParameter = 1;

    public static final String RTTI_CLASS_SUFFIX = "$RTTIc";

    public static final String RTTI_INTERFACE_SUFFIX = "$RTTIi";


    /**
     * Returns the name of the RTTI class for a Fortress type,
     * given the (Java) name of the stem of the type.
     * The "stem" is the type name, minus static parameters (in Oxford
     * brackets) if there are any.
     * 
     * This is used to allocate the type names, and in certain instances of
     * type queries (e.g., for Object types).
     * 
     * @param stemClassName
     * @return
     */
    public static String stemClassJavaName(String stemClassName) {
        return stemClassName + RTTI_CLASS_SUFFIX;
    }

    /**
     * Returns the name of the RTTI interface for a Fortress type,
     * given the (Java) name of the stem of the type.
     * The "stem" is the type name, minus static parameters (in Oxford
     * brackets) if there are any.
     * 
     * In general, this is what is tested against and cast to in type queries.
     * 
     * @param stemClassName
     * @return
     */
    public static String stemInterfaceJavaName(String stemClassName) {
        return stemClassName +
                                    RTTI_INTERFACE_SUFFIX;
    }


    public final static String RTTI_CONTAINER_TYPE = "java/lang/Object";


    /**
     * @param owner_and_result_class
     * @param n_static_params
     * @return
     */
    public static String rttiFactorySig(String owner_and_result_class,
            final int n_static_params) {
        return NamingCzar.jvmSignatureForNTypes(
                n_static_params, RTTI_CONTAINER_TYPE, "L" + RTTI_CONTAINER_TYPE +";");
    }
    
    public static String combineStemAndSparams(String stem, String sparams_in_oxfords) {
        return stem + sparams_in_oxfords;
    }

    /**
     * Convert an ASM internal form to a Java descriptor form.
     * That is, surround a class type with L and ;
     */
    // Widely used
    public static String internalToDesc(String type) {
        return "L" + type + ";";
    }
    
}
