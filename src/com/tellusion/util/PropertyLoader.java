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

import java.io.*;
import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;

/**
 * Search the current directory and all parent directories for a named property file,
 * 	which is then loaded into the System Properties object.
 * NB: system properties specified on the JVM command line with -D<name>=<value> will
 * 	override anything that is loaded here. 
 */
public class PropertyLoader
	{
	/** specify a different directory to start the search from */
	private static String strBaseDirectory = null;
	
	/** list of previously loaded files */
	private static HashSet<String> setLoadedPropertyFiles = new HashSet<String>();
	
	/**
	 * Specify the directory to search for property files
	 * 
	 * @param strBaseDirectory to start search from
	 * @throws FileNotFoundException if directory not found
	 */
	public static void setBaseDirectory(String strBaseDirectory)
			throws FileNotFoundException
		{
		File oFile = new File(strBaseDirectory);
		
		if ( !oFile.exists() )
			throw new FileNotFoundException("file does not exist: " + strBaseDirectory);
		if ( !oFile.isDirectory() )
			throw new FileNotFoundException("not a directory: " + strBaseDirectory);
		
		PropertyLoader.strBaseDirectory = strBaseDirectory;
		}
		
	/**
	 * Search for a named property file.
	 * 
	 * If a property file by that name has already been loaded, it will not be loaded again (unless reset() has been called).
	 * 
	 * @param strFilename name of the file to search for (parent dirs will also be searched)
	 * @param bRequired throw exception if the named file could not be found
	 * @return true if file was successfully loaded 
	 * @throws FileNotFoundException if file not found
	 */
	public static Properties searchForPropertyFile(String strFilename, boolean bRequired)
			throws IOException, FileNotFoundException
		{
		File oCurrentDirectory, oCandidateFile;
		Properties oNewProperties;
		
		// sanity check
		if ( strFilename == null || strFilename.trim().length() == 0 )
			throw new FileNotFoundException("invalid filename: " + strFilename);
		
		// prevent double-loading
		if ( setLoadedPropertyFiles.contains(strFilename) )
			return System.getProperties();
		else if ( !bRequired )
			setLoadedPropertyFiles.add(strFilename);
		
		// search directory chain for property file 
		for 	(
				oCurrentDirectory = new File(PropertyLoader.strBaseDirectory, ".").getAbsoluteFile();
				oCurrentDirectory != null;
				oCurrentDirectory = oCurrentDirectory.getParentFile()
				)
			{
			// look for file
			oCandidateFile = new File(oCurrentDirectory, strFilename);
			if ( !oCandidateFile.exists() )
				continue;
			
			// load it
			oNewProperties = new Properties();
			oNewProperties.load(new BufferedInputStream(new FileInputStream(oCandidateFile)));
			oNewProperties.putAll(System.getProperties());
			System.setProperties(oNewProperties);
			
			setLoadedPropertyFiles.add(strFilename);
			
			return System.getProperties(); // file found
			}
		
		// if we got here, file not found
		if ( bRequired )
			throw new FileNotFoundException("property file not found: " + strFilename);
		
		return System.getProperties();
		}

	/**
	 * Helper method, searches for an optional property file
	 * @see searchForRequiredPropertyFile()
	 */
	public static Properties searchForOptionalPropertyFile(String strFilename)
		{
		try {
			PropertyLoader.searchForPropertyFile(strFilename, false);
			}
		catch(IOException e)
			{
			// ignored
			}
		
		return System.getProperties();
		}
	
	/**
	 * Helper method, searches for a required property file
	 * IOException & FileNotFoundException are changed into RuntimeException so this can be used in member variable initialisers
	 * @see searchForRequiredPropertyFile()
	 */
	public static Properties searchForRequiredPropertyFile(String strFilename)
		{
		try {
			PropertyLoader.searchForPropertyFile(strFilename, true);
			}
		catch(IOException e)
			{
			throw new RuntimeException(e);
			}
		
		return System.getProperties();
		}
	
	/**
	 * Clears the list of previously loaded property files
	 */
	public static void reset()
		{
		PropertyLoader.setLoadedPropertyFiles.clear();
		}
	
	public static void SetupLogging(Properties oProperties, int iVerbose)
		{
		String strRootLogger;
		Properties props = new Properties();
		int iAppenderCount = 0;
		
		strRootLogger = "TRACE";
		
		// stdout appender, with ISO 8601 date format
	    props.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
	    props.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
	    props.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%6r [%t]%x %m%n");
	    if ( iVerbose <= 0 )
			props.setProperty("log4j.appender.stdout.Threshold", "ERROR");
	    else if ( iVerbose == 1 )
			props.setProperty("log4j.appender.stdout.Threshold", "WARN");
	    else if ( iVerbose == 2 )
			props.setProperty("log4j.appender.stdout.Threshold", "INFO");
	    else if ( iVerbose == 3 )
			props.setProperty("log4j.appender.stdout.Threshold", "DEBUG");
	    else if ( iVerbose >= 4 )
			props.setProperty("log4j.appender.stdout.Threshold", "TRACE");
	    strRootLogger += ", stdout";
	    iAppenderCount++;
	    
	    // rolling logfile appender, roll based on size
	    if ( !IS.empty(oProperties.getProperty("run.logfile")) )
	    	{
	    	props.setProperty("log4j.appender.rollinglogfile", "org.apache.log4j.RollingFileAppender");
			props.setProperty("log4j.appender.rollinglogfile.layout", "org.apache.log4j.PatternLayout");
			props.setProperty("log4j.appender.rollinglogfile.MaxFileSize", "250MB");
			props.setProperty("log4j.appender.rollinglogfile.MaxBackupIndex", "10");
		    props.setProperty("log4j.appender.rollinglogfile.layout.ConversionPattern", "%d %6r %-5p %40.40c [%t]%x %m%n");
	    	props.setProperty("log4j.appender.rollinglogfile.File", oProperties.getProperty("run.logfile"));
	    	props.setProperty("log4j.appender.rollinglogfile.Threshold", "TRACE");
		    strRootLogger += ", rollinglogfile";
		    iAppenderCount++;
	    	}
	    
	    // shush some things
		props.setProperty("log4j.logger.org.apache.activemq", "OFF");
		props.setProperty("log4j.logger.org.apache","FATAL, stdout");
		props.setProperty("log4j.logger.com.google","WARN, stdout");
		props.setProperty("log4j.logger.com.amazonaws","WARN, stdout");
		props.setProperty("log4j.logger.httpclient.wire","INFO, stdout");
	    
	    // either completely disable logging or enable the configured appenders
	    if ( iAppenderCount <= 0 )
	    	Logger.getRootLogger().setLevel(Level.OFF);
	    else
		    props.setProperty("log4j.rootLogger", strRootLogger);
	
	    PropertyConfigurator.configure(props);
	    NDC.clear(); NDC.push("");
		}
	
	}
