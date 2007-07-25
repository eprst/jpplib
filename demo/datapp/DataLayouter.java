//This file is part of the Java™ Pretty Printer Library (JPPlib)
//Copyright (C) 2007 Martin Giese

//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package datapp;

import de.uka.ilkd.pp.Layouter;
import de.uka.ilkd.pp.Backend;
import de.uka.ilkd.pp.WriterBackend;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/** An extension of {@link de.uka.ilkd.pp.Layouter} to print
 * Java data structures.  There is a {@link #print(Object)} method
 * that prints objects according to their type.  In particular,
 * there are special layouts for (nested) collections, maps, and
 * arrays.  Classes implementing the interface {@link PrettyPrintable}
 * provide their own method for printing themselves to a DataLayouter.
 * 
 * @author mgiese
 *
 * @param <Exc>
 */
public class DataLayouter<Exc extends Exception> extends Layouter<Exc> {

	public DataLayouter(Backend<Exc> back,int indentation) {
		super(back, indentation);
	}
	
	// STATIC FACTORY METHODS ----------------------------------------

	/** Factory method for a DataLayouter with a {@link WriterBackend}.
	 * The line width is taken to be {@link #DEFAULT_LINE_WIDTH}, and the
	 * default indentation {@link #DEFAULT_INDENTATION}. 
	 *
	 * @param writer the {@link java.io.Writer} the Backend is going to use
	 */
	public static DataLayouter<IOException> 
	getWriterDataLayouter(java.io.Writer writer) {
		return getWriterDataLayouter(writer,DEFAULT_LINE_WIDTH);
	}

	/** Factory method for a DataLayouter with a {@link WriterBackend}.
	 * The default indentation is taken from {@link #DEFAULT_INDENTATION}. 
	 *
	 * @param writer the {@link java.io.Writer} the Backend is going to use
	 * @param lineWidth the maximum lineWidth the Backend is going to use
	 */
	public static DataLayouter<IOException> 
	getWriterDataLayouter(java.io.Writer writer,int lineWidth) {
		return getWriterDataLayouter(writer,lineWidth,DEFAULT_INDENTATION);
	}

	/** Factory method for a DataLayouter with a {@link WriterBackend}.
	 *
	 * @param writer the {@link java.io.Writer} the Backend is going to use
	 * @param lineWidth the maximum lineWidth the Backend is going to use
	 * @param indentation the default indentation
	 */
	public static DataLayouter<IOException> 
	getWriterDataLayouter(java.io.Writer writer,int lineWidth,int indentation) {
		return new DataLayouter<IOException>(new WriterBackend(writer,lineWidth)
		,indentation);
	}

	// DATA PRINTING METHODS ----------------------------------------

	/** Print <code>o</code> to this DataLayouter.
	 * Figures out the type of <code>o</code> and delgates
	 * to one of the specialized printing methods.
	 * 
	 * @param o
	 *            the object to be pretty printed
	 */
	public DataLayouter<Exc> print(Object o) throws Exc {
		if (o instanceof Collection<?>) {
			return print((Collection<?>) o);
		} else if (o instanceof Map<?, ?>) {
			return print((Map<?, ?>) o);
		} else if (o.getClass().isArray()) {
			return printArray(o);		
		} else if (o instanceof PrettyPrintable) {
			((PrettyPrintable) o).prettyPrint(this);
			return this;
		} else {
			return print(String.valueOf(o));
		}
	}

	/** Print a collection.
	 * This is printed as
	 * <pre>
	 * [xxx, yyy, zzz]
	 * </pre>
	 * if it fits on one line, and as
	 * <pre>
	 * [xxx,
	 *  yyy,
	 *  zzz]
	 * </pre>
	 * otherwise.
	 * 
	 * @param c A collection
	 */
	public DataLayouter<Exc> print(Collection<?> c) throws Exc {
		print("[").beginC(0);
		boolean first = true;
		for (Object o : c) {
			if (!first) {
				print(",").brk(1, 0);
			}
			print(o);
			first = false;
		}
		print("]").end();
		return this;
	}

	/** Pretty prints an array of reference or primitive elements.
	 * The format is the same as for collections.
	 * 
	 * @param o an object, has to be an array!
	 */
	public DataLayouter<Exc> printArray(Object o) throws Exc {
		Object[] boxed = BoxArrays.boxArray(o);
		print(Arrays.asList(boxed));
		return this;
	}
	
	/** Print a map.
	 * This is printed as
	 * <pre>
	 * {k1=v1, k2=v2, k3=v3]
	 * </pre>
	 * if it fits on one line, and as
	 * <pre>
	 * {key1=val1,
	 *  key2=val2,
	 *  key3=val3]
	 * </pre>
	 * otherwise.  If values don't fit on one line, the
	 * key-value pairs will also be spread over two lines, e.g.
	 * <pre>
	 * {key1=val1,
	 *  key2=
	 *    [long,
	 *     long,
	 *     value],
	 *  key3=val3]
	 * </pre>
	 */
	public DataLayouter<Exc> print(Map<?, ?> m) throws Exc {
		print("{").beginC(0);
		boolean first = true;
		for (Map.Entry<?, ?> e : m.entrySet()) {
			if (!first) {
				print(",").brk(1, 0);
			}
			printEntry(e);
			first = false;
		}
		print("}").end();
		return this;
	}

	/** Print a map entry.
	 * This is printed as
	 * <pre>
	 * key=val
	 * </pre>
	 * if it fits on one line, and as
	 * <pre>
	 * key=
	 *   val
	 * </pre>
	 * otherwise.  This is mainly to prevent key from adding too
	 * much indentation.
	 */
	public DataLayouter<Exc> printEntry(Map.Entry<?, ?> e) throws Exc {
		beginC();
		print(e.getKey());
		print("=").brk(0, 0);
		print(e.getValue());
		end();
		return this;
	}

	@Override
	public DataLayouter<Exc> begin(boolean consistent, int indent) {
		super.begin(consistent, indent);
		return this;
	}

	@Override
	public DataLayouter<Exc> begin(boolean consistent) {
		super.begin(consistent);
		return this;
	}

	@Override
	public DataLayouter<Exc> beginC() {
		super.beginC();
		return this;
	}

	@Override
	public DataLayouter<Exc> beginC(int indent) {
		super.beginC(indent);
		return this;
	}

	@Override
	public DataLayouter<Exc> beginI() {
		super.beginI();
		return this;
	}

	@Override
	public DataLayouter<Exc> beginI(int indent) {
		super.beginI(indent);
		return this;
	}

	@Override
	public DataLayouter<Exc> brk() throws Exc {
		super.brk();
		return this;
	}

	@Override
	public DataLayouter<Exc> brk(int width, int offset) throws Exc {
		super.brk(width, offset);
		return this;
	}

	@Override
	public DataLayouter<Exc> brk(int width) throws Exc {
		super.brk(width);
		return this;
	}

	@Override
	public DataLayouter<Exc> end() throws Exc {
		super.end();
		return this;
	}

	@Override
	public DataLayouter<Exc> flush() throws Exc {
		super.flush();
		return this;
	}

	@Override
	public DataLayouter<Exc> ind() throws Exc {
		super.ind();
		return this;
	}

	@Override
	public DataLayouter<Exc> ind(int width, int offset) throws Exc {
		super.ind(width, offset);
		return this;
	}

	@Override
	public DataLayouter<Exc> mark(Object o) throws Exc {
		super.mark(o);
		return this;
	}

	@Override
	public DataLayouter<Exc> nl() throws Exc {
		super.nl();
		return this;
	}

	@Override
	public DataLayouter<Exc> pre(String s) throws Exc {
		super.pre(s);
		return this;
	}

	@Override
	public DataLayouter<Exc> print(String s) throws Exc {
		super.print(s);
		return this;
	}
	
}
