/*******************************************************************************
    4150 Network Circle, Santa Clara, California 95054, U.S.A.
    All rights reserved.
    Copyright 2008 Sun Microsystems, Inc.,

    U.S. Government Rights - Commercial software.
    Government users are subject to the Sun Microsystems, Inc. standard
    license agreement and applicable provisions of the FAR and its supplements.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.

    Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered
    trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
 ******************************************************************************/

package com.sun.fortress.interpreter.glue.prim;

import static com.sun.fortress.exceptions.ProgramError.error;
import static com.sun.fortress.exceptions.ProgramError.errorMsg;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import com.sun.fortress.interpreter.evaluator.Environment;
import com.sun.fortress.interpreter.evaluator.types.FTypeObject;
import com.sun.fortress.interpreter.evaluator.values.FBool;
import com.sun.fortress.interpreter.evaluator.values.FInt;
import com.sun.fortress.interpreter.evaluator.values.FObject;
import com.sun.fortress.interpreter.evaluator.values.FString;
import com.sun.fortress.interpreter.evaluator.values.FValue;
import com.sun.fortress.interpreter.evaluator.values.FVoid;
import com.sun.fortress.interpreter.evaluator.values.NativeConstructor;
import com.sun.fortress.interpreter.glue.NativeMeth0;
import com.sun.fortress.interpreter.glue.NativeMeth1;
import com.sun.fortress.nodes.GenericWithParams;

public class Reader extends NativeConstructor {
    private static NativeConstructor con = null;

    public Reader(Environment env, FTypeObject selfType, GenericWithParams def) {
        super(env, selfType, def);
        con = this;
    }

    @Override
	protected FNativeObject makeNativeObject(List<FValue> args,
                                             NativeConstructor con) {
        String name = args.get(0).getString();
        try {
        	InputStreamReader ir = new InputStreamReader(new FileInputStream(
                    name), Charset.forName("UTF-8"));
        	BufferedReader r = new BufferedReader(ir);
            return new PrimReader(name, r);
        } catch (FileNotFoundException ex) {
            return error("FileNotFound: "+name);
        }
    }

    static final class PrimReader extends FNativeObject {
		protected final BufferedReader reader;
        protected final String name;
        protected boolean eof = false;
		protected boolean consumed = false;

        public PrimReader(String name, BufferedReader reader) {
			super(Reader.con);
            this.reader = reader;
            this.name = name;
        }

        @Override
        public NativeConstructor getConstructor() {
            return Reader.con;
        }

        @Override
        public String getString() {
            return "<Reader on \"" + name + "\">";
        }

        public BufferedReader getReader() {
			return reader;
        }
        
        public void whenUnconsumed() {
			if (consumed) {
				error(errorMsg(
						"Performed operation on consumed FileReadStream ", name));
			}
		}
        
        @Override
        public boolean seqv(FValue v) {
            return (v==this);
        }
    }

    private static abstract class r2S extends NativeMeth0 {
		abstract String f(PrimReader r) throws IOException;
        @Override
        public final FValue applyMethod(FObject self) {
            try {
                return FString.make(f((PrimReader) self));
            } catch (IOException e) {
                return error("Read IOException on " + self.getString());
            }
        }
    }

    private static abstract class r2I extends NativeMeth0 {
		abstract int f(PrimReader r) throws IOException;

		@Override
		public final FValue applyMethod(FObject self) {
			try {
				return FInt.make(f((PrimReader) self));
			} catch (IOException e) {
				return error("Read IOException on " + self.getString());
			}
		}
	}

	private static abstract class r2B extends NativeMeth0 {
		protected abstract boolean f(PrimReader r) throws IOException;

		@Override
		public final FValue applyMethod(FObject self) {
			try {
				return FBool.make(f((PrimReader) self));
			} catch (IOException e) {
				return error("Read IOException on " + self.getString());
			}
		}
	}


    private static abstract class rS2V extends NativeMeth1 {
        protected abstract void f(java.io.Reader r, String s)
                throws IOException;
        @Override
        public final FValue applyMethod(FObject self, FValue s) {
            try {
                f(((PrimReader) self).reader, s.getString());
                return FVoid.V;
            } catch (IOException e) {
                return error("Read IOException on " + self.getString());
            }
        }
    }

    private static abstract class r2V extends NativeMeth0 {
		protected abstract void f(java.io.Reader r) throws IOException;
        @Override
        public final FValue applyMethod(FObject self) {
            try {
                f(((PrimReader) self).reader);
                return FVoid.V;
            } catch (IOException e) {
                return error("IOException on "+self.getString());
            }
        }
    }

    public static final class fileName extends r2S {
		@Override
		protected final String f(PrimReader r) {
            return r.name;
        }
    }
    
    public static final class toString extends r2S {
		@Override
		protected final String f(PrimReader r) {
			return r.getString();
		}
	}

	public static final class readLine extends r2S {
		@Override
		protected final String f(PrimReader r) throws IOException {
			String line = r.reader.readLine();
			if (line == null) {
				r.eof = true;
				line = "";
			}
			return line;
		}
	}

	public static final class readChar extends r2I {
		@Override
		protected final int f(PrimReader r) throws IOException {
			int c = r.reader.read();
			if (c == -1) {
				r.eof = true;
			}
			return c;
		}
	}

	public static final class readk extends NativeMeth1 {
		@Override
		public final FValue applyMethod(FObject self, FValue size) {
			PrimReader r = (PrimReader) self;
			int k = size.getInt();
			k = (k <= 0) ? 65536 : k;
			char c[] = new char[k];
			try {
				k = r.reader.read(c, 0, k);
				if (k == -1) {
					r.eof = true;
					return FString.make("");
				}
				return FString.make(new String(c, 0, k));
			} catch (IOException e) {
				return error(r.getString() + ".read(" + k + ") IO error.");
			}
		}
	}

    public static final class eof extends r2B {
		@Override
		protected final boolean f(PrimReader r) {
			return r.eof;
		}
	}

	public static final class ready extends r2B {
		@Override
		public final boolean f(PrimReader r) throws IOException {
			return r.reader.ready();
		}
	}

	public static final class close extends NativeMeth0 {
		@Override
		public final FValue applyMethod(FObject x) {
			PrimReader r = (PrimReader) x;
			try {
				r.reader.close();
				r.eof = true;
				return FVoid.V;
			} catch (IOException e) {
				return error("Close IOException on " + x.getString());
			}
        }
    }

    public static final class whenUnconsumed extends NativeMeth0 {
		@Override
		public synchronized final FValue applyMethod(FObject self) {
			PrimReader r = (PrimReader) self;
			r.whenUnconsumed();
			return FVoid.V;
        }
    }

    public static final class consume extends NativeMeth0 {
		@Override
		public final FValue applyMethod(FObject self) {
			PrimReader r = (PrimReader) self;
			synchronized (r) {
				r.whenUnconsumed();
				r.consumed = true;
			}
			return FVoid.V;
        }
    }
    
    @Override
    protected void unregister() {
        con = null;
    }

}

