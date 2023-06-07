/*
 * “Commons Clause” License Condition v1.0
 * =======================================
 *
 * The Software is provided to you by the Licensor under the License,
 * as defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights
 * under the License will not include, and the License does not grant to
 * you, the right to Sell the Software.
 *
 * For purposes of the foregoing, “Sell” means practicing any or all of
 * the rights granted to you under the License to provide to third
 * parties, for a fee or other consideration (including without
 * limitation fees for hosting or consulting/ support services related
 * to the Software), a product or service whose value derives, entirely
 * or substantially, from the functionality of the Software. Any license
 * notice or attribution required by the License must also include this
 * Commons Clause License Condition notice.
 *
 * Software: tellusion-utils
 * License: GNU General Public License version 3
 * Licensor: Andrew Prendergast ap@tellusion.com
 *
 * GNU General Public License version 3
 * ------------------------------------
 *
 * (C) COPYRIGHT 2000-2023 Andrew Prendergast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tellusion.util;

import java.math.*;
import java.util.*;
import java.sql.Types;
import java.text.*;
import org.junit.Assert;

public class Numbers
	{
	private static final Integer N_BYTE = 1;
	private static final Integer N_SHORT = 2;
	private static final Integer N_INT = 3;
	private static final Integer N_LONG = 4;
	private static final Integer N_FLOAT = 5;
	private static final Integer N_DOUBLE = 6;

	public static Number max(Number... aoNumbers)
		{
		return Numbers.max(Arrays.asList(aoNumbers));
		}
	
	public static Number max(Collection<Number> c)
		{
		int i = 0;
		Number nMax = null;
		
		if ( c == null || c.size() == 0 )
			return null;
		
		for ( Number n : c )
			if ( n != null )
				if ( i++ == 0 || n.doubleValue() > nMax.doubleValue() )
					nMax = n;
		
		return nMax;
		}
	
	public static boolean equals(Float a, Float b)
		{
		return ( Math.abs(a - b) < 0.00001 );
		}

	public static boolean equals(Float a, Float b, double delta)
		{
		return ( Math.abs(a - b) < delta );
		}

	public static boolean equals(Double a, Double b, double delta)
		{
		return ( Math.abs(a - b) < delta );
		}
	
	public static Number min(Number... aoNumbers)
		{
		return Numbers.min(Arrays.asList(aoNumbers));
		}
	
	public static Number min(Collection<Number> c)
		{
		int i = 0;
		Number nMin = null;
		
		if ( c == null || c.size() == 0 )
			return null;
		
		for ( Number n : c )
			if ( n != null )
				if ( i++ == 0 || n.doubleValue() < nMin.doubleValue() )
					nMin = n;
		
		return nMin;
		}
	
	/**
	 * Robust coercion of an Object into a Boolean
	 * @return boolean representation of o or false if conversion failed or o is null 
	 */
	public static boolean toBoolean(Object o)
		{
		if ( o == null )
			return false;
		else if ( o instanceof Boolean )
			return (Boolean)o;
		else if ( o instanceof String && (o.toString().equalsIgnoreCase("true") || o.toString().equalsIgnoreCase("yes") | o.toString().equalsIgnoreCase("on")) )
			return true;
		else
			return ( Numbers.toDouble(o) != 0 );
		}
	
	/**
	 * Robust coercion of an Object into a Byte
	 * @return byte representation of o or 0 if conversion failed 
	 */
	public static byte toByte(Object o)
		{
		try {
			if ( o instanceof Number )
				{
				if ( isDecimal(o) )
					return ((Double)Math.rint(toDouble(o))).byteValue();
				else
					return ((Number)o).byteValue();
				}
			else if ( o instanceof String )
				return Byte.parseByte((String)o);
			else if ( o instanceof Boolean )
				return ((byte)( ((Boolean)o) ? 1 : 0 ));
			else
				return (byte)0;
			}
		catch(NumberFormatException e)
			{
			return ((Double)Math.rint(toDouble(o))).byteValue();
			}
		}
	
	/**
	 * Robust coercion of an Object into a Short
	 * @return short representation of o or 0 if conversion failed 
	 */
	public static short toShort(Object o)
		{
		try {
			if ( o instanceof Number )
				{
				if ( isDecimal(o) )
					return ((Double)Math.rint(toDouble(o))).shortValue();
				else
					return ((Number)o).shortValue();
				}
			else if ( o instanceof String )
				return Short.parseShort((String)o);
			else if ( o instanceof Boolean )
				return ((short)( ((Boolean)o) ? 1 : 0 ));
			else
				return (short)0;
			}
		catch(NumberFormatException e)
			{
			return ((Double)Math.rint(toDouble(o))).shortValue();
			}
		}
	
	/**
	 * Robust coercion of an Object into an Integer
	 * @return integer representation of o or 0 if conversion failed 
	 */
	public static int toInteger(Object o)
		{
		try {
			if ( o instanceof Number )
				{
				if ( isDecimal(o) )
					return ((Double)Math.rint(toDouble(o))).intValue();
				else
					return ((Number)o).intValue();
				}
			else if ( o instanceof String )
				return Integer.parseInt((String)o);
			else if ( o instanceof Boolean )
				return ((int)( ((Boolean)o) ? 1 : 0 ));
			else
				return (int)0;
			}
		catch(NumberFormatException e)
			{
			return ((Double)Math.rint(toDouble(o))).intValue();
			}
		}
	
	/**
	 * Robust coercion of an Object into a Long
	 * @return long representation of o or 0 if conversion failed 
	 */
	public static long toLong(Object o)
		{
		try {
			if ( o instanceof Number )
				{
				if ( isDecimal(o) )
					return ((Double)Math.rint(toDouble(o))).longValue();
				else
					return ((Number)o).longValue();
				}
			else if ( o instanceof String )
				return Long.parseLong((String)o);
			else if ( o instanceof Boolean )
				return ((long)( ((Boolean)o) ? 1 : 0 ));
			else
				return (long)0;
			}
		catch(NumberFormatException e)
			{
			return ((Double)Math.rint(toDouble(o))).longValue();
			}
		}
	
	/**
	 * Robust coercion of an Object into a Float
	 * @return float representation of o or 0.0 if conversion failed 
	 */
	public static float toFloat(Object o)
		{
		try {
			if ( o instanceof Number )
				return ((Number)o).floatValue();
			else if ( o instanceof String )
				return NumberFormat.getNumberInstance().parse((String)o).floatValue();
			else if ( o instanceof Boolean )
				return ((float)( ((Boolean)o) ? 1.0 : 0.0 ));
			else
				return 0;
			}
		catch(ParseException e)
			{
			return (float)0.0;
			}
		}
	
	/**
	 * Robust coercion of an Object into a Double
	 * @return double representation of o or 0.0 if conversion failed 
	 */
	public static double toDouble(Object o)
		{
		try {
			if ( o instanceof Number )
				return ((Number)o).doubleValue();
			else if ( o instanceof String )
				return NumberFormat.getNumberInstance().parse((String)o).doubleValue();
			else if ( o instanceof Boolean )
				return ((double)( ((Boolean)o) ? 1.0 : 0.0 ));
			else
				return 0;
			}
		catch(ParseException e)
			{
			return (double)0.0;
			}
		}

	/**
	 * Determine if the supplied object is some type of integer number
	 */
	public static boolean isInteger(Object o)
		{
		if ( o instanceof Byte ) return true;
		if ( o instanceof Short ) return true;
		if ( o instanceof Integer ) return true;
		if ( o instanceof Long ) return true;
		if ( o instanceof BigInteger ) return true;
		return false;
		}
	
	/**
	 * Determine if the supplied object is some type of decimal number
	 */
	public static boolean isDecimal(Object o)
		{
		if ( o instanceof Float ) return true;
		if ( o instanceof Double ) return true;
		if ( o instanceof BigDecimal ) return true;
		return false;
		}
	
	/**
	 * Adds two numbers, returning the answer in the most complex of the two types 
	 * @return sum or null if a non-Number was encountered
	 */
	public static Number add(Number a, Number b)
		{
		if ( a instanceof Number && b == null )
			return a;
		else if ( b instanceof Number && a == null )
			return b;
		else if ( a instanceof BigInteger && b instanceof BigInteger )
			return ((BigInteger)a).add((BigInteger)b);
		else if ( a instanceof BigDecimal && b instanceof BigDecimal )
			return ((BigDecimal)a).add((BigDecimal)b);
		else if ( a instanceof Number && b instanceof Number )
			{
			int iClass = 0;
			
			if ( a instanceof Byte || b instanceof Byte ) iClass = N_BYTE;
			if ( a instanceof Short || b instanceof Short ) iClass = N_SHORT;
			if ( a instanceof Integer || b instanceof Integer ) iClass = N_INT;
			if ( a instanceof Long || b instanceof Long ) iClass = N_LONG;
			if ( a instanceof Float || b instanceof Float ) iClass = N_FLOAT;
			if ( a instanceof Double || b instanceof Double ) iClass = N_DOUBLE;
			
			if ( iClass == N_BYTE )
				return a.byteValue() + b.byteValue();
			else if ( iClass == N_SHORT )
				return a.shortValue() + b.shortValue();
			else if ( iClass == N_INT )
				return a.intValue() + b.intValue();
			else if ( iClass == N_LONG )
				return a.longValue() + b.longValue();
			else if ( iClass == N_FLOAT )
				return a.floatValue() + b.floatValue();
			else if ( iClass == N_DOUBLE )
				return a.doubleValue() + b.doubleValue();
			else
				throw new RuntimeException(String.format("can not add %s (%s) and %s (%s)",
					a, a.getClass().getSimpleName(), b, b.getClass().getSimpleName()));
			}
		else
			return null;
		}

	/**
	 * Subtracts two numbers, returning the answer in the most complex of the two types 
	 * @return sum or null if a non-Number was encountered
	 */
	public static Number subtract(Number a, Number b)
		{
		if ( a instanceof Number && b == null )
			return a;
		else if ( b instanceof Number && a == null )
			a = new Long(0);
		
		if ( a instanceof BigInteger && b instanceof BigInteger )
			return ((BigInteger)a).subtract((BigInteger)b);
		else if ( a instanceof BigDecimal && b instanceof BigDecimal )
			return ((BigDecimal)a).subtract((BigDecimal)b);
		else if ( a instanceof Number && b instanceof Number )
			{
			int iClass = 0;
			
			if ( a instanceof Byte || b instanceof Byte ) iClass = N_BYTE;
			if ( a instanceof Short || b instanceof Short ) iClass = N_SHORT;
			if ( a instanceof Integer || b instanceof Integer ) iClass = N_INT;
			if ( a instanceof Long || b instanceof Long ) iClass = N_LONG;
			if ( a instanceof Float || b instanceof Float ) iClass = N_FLOAT;
			if ( a instanceof Double || b instanceof Double ) iClass = N_DOUBLE;
			
			if ( iClass == N_BYTE )
				return a.byteValue() - b.byteValue();
			else if ( iClass == N_SHORT )
				return a.shortValue() - b.shortValue();
			else if ( iClass == N_INT )
				return a.intValue() - b.intValue();
			else if ( iClass == N_LONG )
				return a.longValue() - b.longValue();
			else if ( iClass == N_FLOAT )
				return a.floatValue() - b.floatValue();
			else if ( iClass == N_DOUBLE )
				return a.doubleValue() - b.doubleValue();
			else
				throw new RuntimeException(String.format("can not add %s (%s) and %s (%s)",
					a, a.getClass().getSimpleName(), b, b.getClass().getSimpleName()));
			}
		else
			return null;
		}
	
	/**
	 * @see Types
	 * @param iType type constant
	 * @return TRUE if the Type constant refers to a Number
	 */
	public static boolean isNumberType(int iType)
		{
		if (
			iType == Types.BIGINT ||
			iType == Types.BOOLEAN ||
			iType == Types.DECIMAL ||
			iType == Types.DOUBLE ||
			iType == Types.FLOAT ||
			iType == Types.INTEGER ||
			iType == Types.NUMERIC ||
			iType == Types.REAL ||
			iType == Types.SMALLINT ||
			iType == Types.TINYINT
			)
			return true;
		else
			return false;
		}

	/**
	 * @see Types
	 * @param iType type constant
	 * @return TRUE if the Type constant refers to a Number
	 */
	public static boolean isIntegerType(int iType)
		{
		if (
			iType == Types.BIGINT ||
			iType == Types.BOOLEAN ||
			iType == Types.INTEGER ||
			iType == Types.SMALLINT ||
			iType == Types.TINYINT
			)
			return true;
		else
			return false;
		}

	public static void assertEquals(Object a, Object b)
	{
		assertEquals(String.format("%s != %s", a, b), a, b, 0.00001);
	}

	public static void assertEquals(Object a, Object b, double delta)
	{
		assertEquals(String.format("%s != %s", a, b), a, b, delta);
	}
	
	public static void assertEqualsS(String s, Object a, Object b)
	{
		assertEquals(s, a, b, 0.00001);
	}

	public static void assertEquals(String s, Object a, Object b, double delta)
		{
		if ( a == null && b != null )
			Assert.assertNull(s,b);
		else if ( b == null && a != null )
			Assert.assertNull(s,a);
		else if ( Numbers.isDecimal(a) || Numbers.isDecimal(b) )
			{
			Double da, db;
			da = ( a == null ? null : Numbers.toDouble(a) );
			db = ( b == null ? null : Numbers.toDouble(b) );
			Assert.assertEquals(s,da, db, delta);
			}
		else if ( Numbers.isInteger(a) || Numbers.isInteger(b) )
			{
			Long la, lb;
			la = ( a == null ? null : Numbers.toLong(a) );
			lb = ( b == null ? null : Numbers.toLong(b) );
			Assert.assertEquals(s,la, lb);
			}
		else
			Assert.assertEquals(s,a, b);
		}
	
	
	}
