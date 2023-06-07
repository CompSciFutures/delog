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
 * Licensor: Andrew Prendergast ap@andrewprendergast.com
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
 * Implodes a collection, PHP style
 */
public class Implode
	{
	private LinkedList listStrings = new LinkedList();
	private String strGlue = ",";

	public static String toString(Object oCollection)
		{
		return Implode.toString(oCollection, ",");
		}
	
	public static String toString(Object oCollection, String strGlue)
		{
		return Implode.toString(oCollection, null, null, strGlue);
		}
	
	public static String toString(Object oCollection, String strPrefix, String strSuffix, String strGlue)
		{
		StringBuilder oStringBuilder = new StringBuilder();
		int i = 0;
		if ( oCollection instanceof Collection )
			for ( Object o : (Collection)oCollection )
				{
				if ( oStringBuilder.length() > 0 )
					oStringBuilder.append(strGlue);
				if ( strPrefix != null )
					oStringBuilder.append(strPrefix);
				if ( o != null )
					oStringBuilder.append(o);
				if ( strSuffix != null )
					oStringBuilder.append(strSuffix);
				}
		else
			oStringBuilder.append(oCollection);
		return oStringBuilder.toString();
		}

	public static String toString(Object[] astrSubject)
		{
		return astrSubject == null ? null : Implode.toString(Arrays.asList(astrSubject), null, null, ",");
		}
	
	public static String toString(Object[] astrSubject, String strGlue)
		{
		return astrSubject == null ? null : Implode.toString(Arrays.asList(astrSubject), null, null, strGlue);
		}

	public static String toString(Object[] astrSubject, String strPrefix, String strSuffix, String strGlue)
		{
		return astrSubject == null ? null : Implode.toString(Arrays.asList(astrSubject), strPrefix, strSuffix, strGlue);
		}
	
	public Implode()
		{
		}
	
	public Implode(String strGlue)
		{
		this.strGlue = strGlue;
		}
	
	public void add(String s)
		{
		this.listStrings.add(s);
		}
	
	public String toString()
		{
		return Implode.toString(this.listStrings, this.strGlue);
		}
	}
