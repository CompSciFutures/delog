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

import java.util.*;

/**
* convenience class to make comparing stuff neater
* 
* NULLs are considered, exceptions are never throwsn and overloading
* is used to make the same one-liner compare different types.
*  
* @author ap
*/
public class IS
	{
	/**
	 * Simple null check
	 * @param o to test for null state
	 * @return true if o is null or attempting to access o would generate a NullPointerException
	 */
	public static boolean NULL(Object o)
		{
		try {
			return ( o == null );
			}
		catch(NullPointerException e)
			{
			return false;
			}
		}
	
	public static boolean empty(Object o)
		{
		if ( NULL(o) )
			return true;
		if ( o instanceof String )
			return ( ((String)o).trim().length() == 0 );
		if ( o instanceof Number )
			return !Numbers.toBoolean(o);
		if ( o instanceof Iterable )
			return !((Iterable)o).iterator().hasNext();
		if ( o instanceof org.w3c.dom.NodeList )
			return ((org.w3c.dom.NodeList)o).getLength() > 0;
		return false;
		}
	
	public static boolean equal(Collection a, Collection b)
		{
		Iterator iterA, iterB;
		
		if ( a == null || b == null )
			return ( a == null && b == null ? true : false );
		if ( a.size() != b.size() )
			return false;

		// deep compare of collection
		iterA = a.iterator();
		iterB = b.iterator();

		while ( iterA.hasNext() && iterB.hasNext() )
			if ( !IS.equal(iterA.next(), iterB.next()) )
				return false;
	
		return true;
		}
	
	public static boolean equal(String a, String b)
		{
		if ( a == null || b == null )
			return ( a == null && b == null ? true : false );
		else
			return ( a.equals(b) ? true : false );
		}
	
	public static boolean equal(Integer a, Integer b)
		{
		if ( a == null || b == null )
			return ( a == null && b == null ? true : false );
		else
			return ( a.equals(b) ? true : false );
		}
	
	public static boolean equal(Double a, Double b)
		{
		if ( a == null || b == null )
			return ( a == null && b == null ? true : false );
		else
			return ( a.equals(b) ? true : false );
		}

	public static boolean equal(Float a, Float b)
		{
		if ( a == null || b == null )
			return ( a == null && b == null ? true : false );
		else
			return ( a.equals(b) ? true : false );
		}
	
	public static boolean equal(Number a, Number b)
		{
		if ( a == null || b == null )
			return ( a == null && b == null ? true : false );
		else
			return ( a.equals(b) ? true : false );
		}
	
	public static boolean equal(byte[] a, byte[] b)
		{
		if ( a == null || b == null )
			return ( a == null && b == null ? true : false );
		else
			return Arrays.equals(a, b);
		}
	
	public static boolean equal(Date a, Date b)
		{
		if ( a == null || b == null )
			return ( a == null && b == null ? true : false );
		else
			return ( Math.abs(a.getTime() - b.getTime()) < 1000 ); // precision < 1000 MS not required
		}
	
	public static boolean equal(Object a, Object b)
		{
		// null test
		if ( a == null || b == null )
			return ( a == null && b == null ? true : false );
		// pass to object-specific equality test
		else if ( a instanceof Date && b instanceof Date )
			return IS.equal((Date)a, (Date)b);
		else if ( a instanceof byte[] && b instanceof byte[] )
			return IS.equal((byte[])a, (byte[])b);
		// this must come 2nd last
		else if ( a instanceof Comparable && b instanceof Comparable && a.getClass() == b.getClass() )
			return ( ((Comparable)a).compareTo((Comparable)b) == 0 );
		// this must come last
		else
			return ( a.equals(b) ? true : false );
		}

	public static boolean equalIgnoreCase(String a, String b)
		{
		if ( a == null || b == null )
			return ( a == null && b == null ? true : false );
		else
			return IS.equal(a.toLowerCase(), b.toLowerCase());
		}
	
	public static boolean on(Number o)
		{
		if ( o == null )
			return false;
		return( Numbers.toBoolean(o) );
		}
	
	public static boolean on(String o)
		{
		if ( o == null || o.length() == 0 )
			return false;
		if ( o.equalsIgnoreCase("0") || o.equalsIgnoreCase("off") || o.equalsIgnoreCase("n") || o.equalsIgnoreCase("no") || o.equalsIgnoreCase("nay") || o.equalsIgnoreCase("false") )
			return false;
		return true;
		}
	
	public static boolean on(Object o)
		{
		if ( o == null )
			return false;
		if ( o instanceof Boolean )
			return (Boolean)o;
		if ( o instanceof Number )
			return IS.on((Number)o);
		if ( o instanceof String )
			return IS.on((String)o);
		return true;
		}
	}
