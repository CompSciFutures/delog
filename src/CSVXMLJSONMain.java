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
import java.io.FileInputStream;
import java.util.*;

import gnu.getopt.*;
import org.apache.log4j.*;
import com.tellusion.util.*;

public class CSVXMLJSONMain
    {
    protected static Logger oLogger = Logger.getLogger(CSVXMLJSONMain.class);

    LinkedList<String> listFilenames = null;
    LinkedList<String> listIncludedFields = null;
    LinkedList<String> listExcludedFields = null;
    HashMap<String,String> mapAliases = null;

    public static void main(String[] args)
        {
        CSVXMLJSONMain oMain;

        Properties props = new Properties();
        org.apache.log4j.BasicConfigurator.configure();

        props.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.stdout.Threshold", "DEBUG");
        props.setProperty("log4j.appender.stdout.Target", "System.out");
        props.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%6r [%t]%x %m%n");

        props.setProperty("log4j.appender.stdout.filter.filter1","org.apache.log4j.varia.LevelRangeFilter");
        props.setProperty("log4j.appender.stdout.filter.filter1.levelMin","TRACE");
        props.setProperty("log4j.appender.stdout.filter.filter1.levelMax","INFO");

        props.setProperty("log4j.appender.stderr", "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.stderr.Threshold", "WARN");
        props.setProperty("log4j.appender.stderr.Target", "System.out");
        props.setProperty("log4j.appender.stderr.layout", "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.stderr.layout.ConversionPattern", "%6r [%t]%x %m%n");

        PropertyConfigurator.configure(props);

        try {
            oMain = new CSVXMLJSONMain();
            oMain.Start(args);
            }
        catch (Exception e)
            {
//            CSVXMLJSONMain.oLogger.error("Fatal Exception", e);
            e.printStackTrace(System.err);
            System.exit(1);
            }
        catch (Error e)
            {
//            CSVXMLJSONMain.oLogger.error("Fatal Error", e);
            e.printStackTrace(System.err);
            System.exit(1);
            }

        System.exit(0);
        }

    public void Start(String[] args)
            throws Exception
        {
        Properties oProperties;
        ArrayList<String> listCommandLine;

//        oLogger.info(String.format("Start() args = %s", args));

        // parse command line args
        oProperties = PropertyLoader.searchForOptionalPropertyFile("delog.properties");
        this.ParseCommandLineArgs(args);
        if ( listFilenames.size() == 0 )
            this.DisplayUsageThenDie();

        if ( !IS.empty(oProperties.getProperty("include")) )
            this.listIncludedFields.addAll(List.of(oProperties.getProperty("include").split(",")));
        if ( !IS.empty(oProperties.getProperty("exclude")) )
            this.listExcludedFields.addAll(List.of(oProperties.getProperty("exclude").split(",")));

        for ( String sInputFilename : listFilenames )
            {
//            oLogger.info(String.format("sInputFilename = %s", sInputFilename));
            CSVXMLJSONReaderWriter oCSVJSONReaderWriter = new CSVXMLJSONReaderWriter();
            oCSVJSONReaderWriter.setCommentLinePrefix("#");
            oCSVJSONReaderWriter.setColumnOrderingPreference(listIncludedFields);
            for ( Map.Entry<String,String> oMapEntry : mapAliases.entrySet() )
                oCSVJSONReaderWriter.setColumnAlias(oMapEntry.getKey(), oMapEntry.getValue());
            for ( String sExcludedColumn : this.listExcludedFields )
                oCSVJSONReaderWriter.setExcludedColumn(sExcludedColumn);
            oCSVJSONReaderWriter.setInputStream(new FileInputStream(sInputFilename));
            oCSVJSONReaderWriter.writeOutputToStream(System.out);
            }
        }

    private static void DisplayUsageThenDie()
        {
        System.err.printf("usage: delog [--include <field>] [--exclude <field>] [--rename <from>=<to>] <filename>\n\n");
        System.exit(1);
        }

    public void ParseCommandLineArgs(String[] argv)
        {
        listFilenames = new LinkedList<>();
        listIncludedFields = new LinkedList<>();
        listExcludedFields = new LinkedList<>();
        mapAliases = new HashMap<String,String>();
        int c;

        if ( argv == null || argv.length == 0 )
            DisplayUsageThenDie();

        LongOpt[] longopts = new LongOpt[4];
        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("include", LongOpt.REQUIRED_ARGUMENT, null, 'i');
        longopts[2] = new LongOpt("exclude", LongOpt.REQUIRED_ARGUMENT, null, 'e');
        longopts[3] = new LongOpt("rename", LongOpt.REQUIRED_ARGUMENT, null, 'r');

        Getopt g = new Getopt("delog", argv, "h?i:e:r:", longopts);
        g.setOpterr(false); // We'll do our own error handling

        while ((c = g.getopt()) != -1)
            {
            String sArgumentText;
            oLogger.info(String.format("c = %d", c));
            switch (c)
                {
                case 'i':
                case 1: // include
                    sArgumentText = g.getOptarg();
                    if (IS.empty(sArgumentText))
                        throw new RuntimeException("field name is required");
                    listIncludedFields.add(sArgumentText);
                    break;

                case 'e':
                case 2: // exclude
                    sArgumentText = g.getOptarg();
                    if (IS.empty(sArgumentText))
                        throw new RuntimeException("field name is required");
                    listExcludedFields.add(sArgumentText);
                    break;

                case 'r':
                case 3: // rename
                    sArgumentText = g.getOptarg();
                    if (IS.empty(sArgumentText))
                        throw new RuntimeException("field name is required");
                    String[] asAlias = sArgumentText.split("=", 2);
                    if (IS.empty(asAlias[0]) || IS.empty(asAlias[1]))
                        throw new RuntimeException("invalid alias");
                    mapAliases.put(asAlias[0], asAlias[1]);
                    break;

                case 'h':
                case '?':
                case 0: // help
                    ///DisplayUsageThenDie();
                    break;

                default:
                    oLogger.error(String.format("invalid argument: %s", c));
                    DisplayUsageThenDie();
                }
            }

//        oLogger.info(String.format("ParseCommandLineArgs() listIncludedFields = %s", listIncludedFields));
//        oLogger.info(String.format("ParseCommandLineArgs() listExcludedFields = %s", listExcludedFields));
//        oLogger.info(String.format("ParseCommandLineArgs() mapAliases = %s", mapAliases));
//        oLogger.info(String.format("ParseCommandLineArgs() argv = %s g.getOptind() = %s", Implode.toString(argv), g.getOptind()));

        for (int i = g.getOptind(); i < argv.length ; i++)
            listFilenames.add(argv[i]);
        }
    }
