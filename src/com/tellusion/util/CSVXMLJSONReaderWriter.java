package com.tellusion.util;

import org.apache.log4j.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import com.google.gson.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;

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
 * Licensor: Andrew Prendergast
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

public class CSVXMLJSONReaderWriter
    {
    private static Logger oLogger = Logger.getLogger(CSVXMLJSONReaderWriter.class);

    public static final String REGEX_XML = "^\\s*<[^>]+>\\s*$";
    public static final String REGEX_JSON_OBJECT = "^\\s*\\{([^}]+)\\}\\s*$";
    public static final String REGEX_JSON_ARRAY = "^\\s*\\[.+\\]\\s*$";
    public static final String REGEX_WHITESPACE = "^[\\s\\r\\n]*$";

    /** 1000 line limit */
    private boolean IS_DEBUG = false;

    /** enable in memory buffer for debugging */
    private boolean IN_MEMORY = false;

    /**
     * some files contain comments, this is the string lines are prefixed with to denote a comment
     */
    private String sLineCommentPrefix = null; // usually set to #

    /**
     * when a column has no associated name in the header row, prefix it with this
     */
    private String sUnknownColumnNamePrefix = "Column";

    /**
     * the CSV/TDF file we're reading from
     */
    private FileInputStream oInputStream = null;

    /**
     * the list of column names, in the order that they will be output
     */
    private LinkedHashSet<String> setColumnNames = new LinkedHashSet<>();

    /**
     * a cached version of setColumnNames for performance and so they are accessible by numerical index
     */
    private String[] asCachedColumnNames = null;

    /**
     * sometimes we wan't to re-name columns in the output - this is the list of aliases (key=original column name, value=alias to output)
     */
    private TreeMap<String, String> mapColumnAliases = new TreeMap<>();

    /**
     * This is the output ordering of column names
     */
    private LinkedHashSet<String> setOutputColumnNames = new LinkedHashSet<>(), setExcludedColumnNames = new LinkedHashSet<>();

    /**
     * Where IN_MEMORY rows are kept for debugging
     */
    private LinkedList<LinkedHashMap<String, String>> listAllNameValuePairMaps = null;

    /* -- CONSTRUCTOR -- */

    public CSVXMLJSONReaderWriter()
        {
        }

    /**
     * Enables in-memory caching of file output
     * @param IN_MEMORY true = keep in memory copy
     * @return
     */
    public CSVXMLJSONReaderWriter _setIN_MEMORY(boolean IN_MEMORY)
        {
        this.IN_MEMORY = IN_MEMORY;
        return this;
        }

    /**
     * @return previously cached dataset
     * @throws IOException
     * @see this._setIN_MEMORY()
     */
    public LinkedList<LinkedHashMap<String, String>> _getIN_MEMORY()
            throws IOException
        {
        if ( !this.IN_MEMORY )
            throw new IOException("IN_MEMORY not enabled");
        if ( this.listAllNameValuePairMaps == null )
            throw new IOException("no IN_MEMORY data");
        return this.listAllNameValuePairMaps;
        }

    /* -- MUTATORS -- */

    public CSVXMLJSONReaderWriter setInputFile(String sInputFile)
            throws FileNotFoundException
        {
        if (sInputFile == null)
            throw new NullPointerException("sInputFile is required");
        return this.setInputStream(new FileInputStream((sInputFile)));
        }

    public CSVXMLJSONReaderWriter setInputStream(FileInputStream oInputStream)
        {
        if (oInputStream == null)
            throw new NullPointerException("oInputStream is required");
        this.oInputStream = oInputStream;
        return this;
        }

    public CSVXMLJSONReaderWriter writeOutputToFile(String sOutputFile)
            throws IOException, NoSuchFieldException
        {
        this.writeOutputToStream(new FileOutputStream(sOutputFile));
        return this;
        }

    public CSVXMLJSONReaderWriter writeOutputToStream(OutputStream oOutputStream)
            throws IOException, NoSuchFieldException
        {
        FileOutputStream oTempOutputStream = null;
        FileInputStream oTempInputStream = null;
        File oTempFile = null;

        if (this.oInputStream == null)
            throw new IOException("input file not open: use .setInputFile() or .setInputStream() first");

        oTempFile = File.createTempFile("CSVJSONReaderWriter", ".csv");

        try
            {
            // read input file into temp
            oTempOutputStream = new FileOutputStream(oTempFile);
            this.writeOutputToStream(this.oInputStream, oTempOutputStream, false);
            oTempOutputStream.close();
            oTempOutputStream = null;

//            oLogger.info(String.format("writeOutputToStream(): ============================ RESTARTING ============================"));
//            oLogger.info(String.format("writeOutputToStream(): this.setColumnNames = '%s'", this.setColumnNames));

            // add remaining column names to output column list in the order encountered
            for ( String sColumnName : this.setColumnNames )
                if ( !this.setOutputColumnNames.contains(sColumnName) )
                    this.setOutputColumnNames.add(sColumnName);

            // read temp into output file
            oTempInputStream = new FileInputStream(oTempFile);
            this.writeOutputToStream(oTempInputStream, oOutputStream, true);
            oTempInputStream.close();
            oTempInputStream = null;
            }
        finally
            {
            if (oTempInputStream != null)
                oTempInputStream.close();
            if (oTempOutputStream != null)
                oTempOutputStream.close();
            oTempFile.delete();
            }
        return this;
        }

    private CSVXMLJSONReaderWriter writeOutputToStream(InputStream oInputStream, OutputStream oOutputStream, boolean bColumnNamesResolved)
            throws IOException
        {
        TellusionCSVReader oCSVReader;
        TellusionCSVWriter oCSVWriter;
        String[] asCurrentLine = null;

        Pattern oXMLPattern = Pattern.compile(CSVXMLJSONReaderWriter.REGEX_XML, Pattern.MULTILINE);
        Pattern oJSONOBJECTPattern = Pattern.compile(CSVXMLJSONReaderWriter.REGEX_JSON_OBJECT, Pattern.MULTILINE);
        Pattern oJSONARRAYPattern = Pattern.compile(CSVXMLJSONReaderWriter.REGEX_JSON_ARRAY, Pattern.MULTILINE);

        oCSVReader = new TellusionCSVReader(new InputStreamReader(oInputStream));
        oCSVWriter = new TellusionCSVWriter(new OutputStreamWriter(oOutputStream));
        listAllNameValuePairMaps = new LinkedList<>();

//        oLogger.info(String.format("writeOutputToStream() bColumnNamesResolved = %s", bColumnNamesResolved));

        for (int iLineCount = 0; (asCurrentLine = oCSVReader.readNext()) != null && (!IS_DEBUG || iLineCount < 1000); iLineCount++)
            {
            LinkedHashMap<String, String> listCurrentLineNameValuePairs = new LinkedHashMap<>(), listFinalisedNameValuePairs;

//            oLogger.info(String.format("writeOutputToStream() iLineCount=%s asCurrentLine=%s",iLineCount, Implode.toString(asCurrentLine, "`", "`", ", ")));

            // pre-populate names
            for (String sColumnName : this.setColumnNames)
                listCurrentLineNameValuePairs.put(sColumnName, null);

            // skip comment lines
            if (this.sLineCommentPrefix != null &&
                    asCurrentLine.length > 0 &&
                    asCurrentLine[0].length() > 0 &&
                    asCurrentLine[0].startsWith(this.sLineCommentPrefix)
            )
                {
                iLineCount--;
                continue;
                }

            // read lines
            if (iLineCount == 0)
                {
                // read header row
                for (String sColumnName : asCurrentLine)
                    {
                    if (!bColumnNamesResolved)
                        {
                        this.setColumnNames.add(sColumnName);
                        listCurrentLineNameValuePairs.put(sColumnName, this.getColumnAlias(sColumnName));
                        }
                    else
                        {
                        listCurrentLineNameValuePairs.putAll(this.setColumnNames.stream().collect(Collectors.toMap(k -> k, v -> this.getColumnAlias(v))));
                        }
                    }
                }
            else
                {
                // parse each field in data row
                for (int iColumnIndex = 0; asCurrentLine.length > iColumnIndex; iColumnIndex++)
                    {
                    String sColumnValue = asCurrentLine[iColumnIndex];

                    if ( bColumnNamesResolved == false && oXMLPattern.matcher(sColumnValue).find() ) // XML
                        {
//                        oLogger.info(String.format("@@@ XML FOUND  writeOutputToStream(): %s='%s'", this.getColumnName(iColumnIndex), sColumnValue));
                        listCurrentLineNameValuePairs.putAll(this.extractNameValuePairsFromXML(this.getColumnName(iColumnIndex), new String(sColumnValue)));
                        listCurrentLineNameValuePairs.put(this.getColumnName(iColumnIndex), sColumnValue);
                        }
                    else if ( bColumnNamesResolved == false && oJSONOBJECTPattern.matcher(sColumnValue).find() ) // JSON Object
                        {
//                        oLogger.info(String.format("@@@ JSON FOUND  writeOutputToStream(): %s='%s'", this.getColumnName(iColumnIndex), new String(sColumnValue)));
                        listCurrentLineNameValuePairs.putAll(this.extractNameValuePairsFromJSON(this.getColumnName(iColumnIndex), sColumnValue));
                        listCurrentLineNameValuePairs.put(this.getColumnName(iColumnIndex), sColumnValue);
                        }
                    else if ( bColumnNamesResolved == false && oJSONARRAYPattern.matcher(sColumnValue).find() ) // JSON Array
                        {
//                        oLogger.info(String.format("@@@ JSON ARRAY FOUND  writeOutputToStream(): %s='%s'", this.getColumnName(iColumnIndex), new String(sColumnValue)));
                        listCurrentLineNameValuePairs.putAll(this.extractNameValuePairsFromJSON(this.getColumnName(iColumnIndex), sColumnValue));
                        listCurrentLineNameValuePairs.put(this.getColumnName(iColumnIndex), sColumnValue);
                        }
                    else
                        {
                        // plain data
                        listCurrentLineNameValuePairs.putAll(this.extractNameValuePairsFromTXT(this.getColumnName(iColumnIndex), sColumnValue));
                        }
                    }
                }

            // make sure new column names are added
            //oLogger.info(String.format("writeOutputToStream(): BEFORE this.setColumnNames = %s", this.setColumnNames.toString()));
            for (Map.Entry<String, String> oMapEntry : listCurrentLineNameValuePairs.entrySet())
                if (!setColumnNames.contains(oMapEntry.getKey()))
                    setColumnNames.add(oMapEntry.getKey());
            //oLogger.info(String.format("writeOutputToStream(): AFTER this.setColumnNames = %s", this.setColumnNames.toString()));


            // assemble output line
            if ( bColumnNamesResolved )
                {
                listFinalisedNameValuePairs = new LinkedHashMap<>();
                for (String sColumnName : setOutputColumnNames)
                    if ( !this.setExcludedColumnNames.contains(sColumnName) )
                        listFinalisedNameValuePairs.put(
                            sColumnName,
                            listCurrentLineNameValuePairs.containsKey(sColumnName) ? listCurrentLineNameValuePairs.get(sColumnName) : null
                    );
                }
            else
                listFinalisedNameValuePairs = listCurrentLineNameValuePairs;

//            oLogger.info(String.format("writeOutputToStream(): setColumnNames = %s", setColumnNames));
//            oLogger.info(String.format("writeOutputToStream(): listCurrentLineNameValuePairs = %s", listCurrentLineNameValuePairs));
//            oLogger.info(String.format("writeOutputToStream(): bColumnNamesResolved = %s setOutputColumnNames = %s", bColumnNamesResolved, setOutputColumnNames));
//            oLogger.info(String.format("writeOutputToStream(): listFinalisedNameValuePairs = %s", listFinalisedNameValuePairs));

            // write line to output buffer
//            oLogger.info((String.format("### WRITING %s", listFinalisedNameValuePairs.values())));
            if ( IN_MEMORY )
                {
                listAllNameValuePairMaps.add(listFinalisedNameValuePairs);
//                oLogger.info(String.format("--- %d", listAllNameValuePairMaps.size()));
                }
            else
                {
                oCSVWriter.writeNext(listFinalisedNameValuePairs.values().toArray(new String[listFinalisedNameValuePairs.size()]));
//                oLogger.info(String.format("---"));
                }

            }

        // write output to CSV
        if ( IN_MEMORY )
            {
//            oLogger.info((String.format("#### BUFFER CONTAINS %s LINES", listAllNameValuePairMaps.size())));
            for (Map<String, String> mapCurrentLine : listAllNameValuePairMaps)
                {
//                oLogger.info((String.format("## WRITING %s", mapCurrentLine.values())));
                oCSVWriter.writeNext(mapCurrentLine.values().toArray(new String[mapCurrentLine.size()]));
                }
            }

        oCSVWriter.close();

        return this;
        }

    private Map<String, String> extractNameValuePairsFromTXT(String sColumnName, String sColumnValue)
        {
        LinkedHashMap<String,String> mapCurrentLineNameValuePairs = new LinkedHashMap<>();
        mapCurrentLineNameValuePairs.put(sColumnName, sColumnValue);
        return mapCurrentLineNameValuePairs;
        }

    private Map<String, String> extractNameValuePairsFromJSON(String sColumnName, String sColumnValue)
        {
//        oLogger.info(String.format("JSON ENCOUNTERED: sColumnName=%s sColumnValue=%s", sColumnName, sColumnValue));

        class JSONTraverser
            {
            LinkedHashMap<String,String> mapCurrentLineNameValuePairs = new LinkedHashMap<>();
            Stack<String> oNameStack = new Stack<>();

            void traverse(String sElementName, JsonElement oJsonElement)
                {
                oNameStack.push(sElementName);
                if (oJsonElement.isJsonNull())
                    mapCurrentLineNameValuePairs.put(Implode.toString(oNameStack, "."), "");
                else if (oJsonElement.isJsonPrimitive())
                    mapCurrentLineNameValuePairs.put(Implode.toString(oNameStack, "."), oJsonElement.getAsString());
                else if (oJsonElement.isJsonArray())
                    {
                    int i = 0;
                    for ( JsonElement oJsonArrayElement : oJsonElement.getAsJsonArray() )
                        {
                        traverse(String.format("a%d", i), oJsonArrayElement);
                        i++;
                        }
                    }
                else if (oJsonElement.isJsonObject())
                    {
                    for ( Map.Entry<String, JsonElement> oJsonObjectElementEntry : oJsonElement.getAsJsonObject().asMap().entrySet() )
                        {
                        traverse(oJsonObjectElementEntry.getKey(), oJsonObjectElementEntry.getValue());
                        }
                    }
                oNameStack.pop();
                }
            }

        try {
            JSONTraverser oJsonTraverser = new JSONTraverser();
            oJsonTraverser.traverse(sColumnName, new JsonParser().parse(sColumnValue));
//            for ( Map.Entry<String,String> oMapEntry : oJsonTraverser.mapCurrentLineNameValuePairs.entrySet() )
//                oLogger.info(String.format("- %s = %s", oMapEntry.getKey(), oMapEntry.getValue()));
            return oJsonTraverser.mapCurrentLineNameValuePairs;
            }
        catch (JsonSyntaxException ex) {
//            oLogger.error(String.format("! JSON parsing failed: %s='%s'", sColumnName, sColumnValue), ex);
            LinkedHashMap<String,String> mapCurrentLineNameValuePairs = new LinkedHashMap<>();
            mapCurrentLineNameValuePairs.put(sColumnName, sColumnValue);
            return mapCurrentLineNameValuePairs;
            }
        }

    /**
     * @see <a href="https://docs.oracle.com/cd/B19306_01/appdev.102/b14252/adx_j_parser.htm#CCHGIIHA">XML Developer's Kit Programmer's Guide</a>
     */
    private Map<String, String> extractNameValuePairsFromXML(String sColumnName, String sColumnValue)
        {
//        oLogger.info(String.format("XML ENCOUNTERED: sColumnName=%s sColumnValue=%s", sColumnName, sColumnValue));

        class XMLDocumentHandler extends DefaultHandler
            {
            LinkedHashMap<String,String> mapCurrentLineNameValuePairs = new LinkedHashMap<>();
            Stack<String> oNameStack = new Stack<>();
            Stack<StringBuilder> oValueStack = new Stack<>();
            Pattern oWHITESPACEPattern = Pattern.compile(CSVXMLJSONReaderWriter.REGEX_WHITESPACE, Pattern.MULTILINE);
            LinkedHashMap<String,NamespaceEntry> mapNamespaceRoot = new LinkedHashMap<>();

            /**
             * Used to keep a tree of XML elements so we can track namespace collisions
             */
            class NamespaceEntry
                {
                String sElementName = null;
                Integer iOccurences = 0;
                Integer iDepth = null;
                LinkedHashMap<String,NamespaceEntry> mapNamespaceChildren = new LinkedHashMap<>();

                public NamespaceEntry(String sElementName, Integer iDepth)
                    {
                    if ( sElementName == null || sElementName.trim().equals("") )
                        throw new RuntimeException("ElementName is required");
                    this.sElementName = sElementName;
                    this.iDepth = iDepth;
                    }

                @Override
                public String toString()
                    {
                    return String.format("[%s:sElementName=%s:iOccurences=%s:iDepth=%s]",
                            this.getClass().getCanonicalName(),
                            sElementName,
                            iOccurences,
                            iDepth
                        );
                    }
                }

            @Override
            public void setDocumentLocator(Locator locator)
                {
//                oLogger.info(String.format("setDocumentLocator(): locator = %s", locator));
                }

            @Override
            public void startDocument()
                    throws SAXException
                {
//                oLogger.info("startDocument()");
                }

            @Override
            public void endDocument()
                    throws SAXException
                {
//                oLogger.info("endDocument()");
                }

            @Override
            public void startPrefixMapping(String s, String s1)
                    throws SAXException
                {
//                oLogger.info(String.format("startPrefixMapping(): s = %s s1 = %s", s, s1));
                }

            @Override
            public void endPrefixMapping(String s)
                    throws SAXException
                {
//                oLogger.info(String.format("endPrefixMapping(): s = %s", s));
                }

            @Override
            public void startElement(String s, String s1, String s2, Attributes attributes)
                    throws SAXException
                {
                String sElementName = kebabCase(s, s1, s2);
                NamespaceEntry oNamespaceEntry = addorgetNamespaceEntry(sElementName);
                if ( oNamespaceEntry.iOccurences++ > 0 )
                    sElementName = String.format("%s%d", sElementName, oNamespaceEntry.iOccurences);
                oNameStack.push(sElementName);
                oValueStack.push(null);

//                oLogger.info(String.format("startElement() s=%s s1=%s s2=%s == %s, oNameStack.size() = %d = %s oNamespaceEntry=%s",
//                        s, s1, s2, sElementName, oNameStack.size(), oNameStack, oNamespaceEntry));
                }

            @Override
            public void endElement(String s, String s1, String s2)
                    throws SAXException
                {
                String sElementName = kebabCase(s, s1, s2);
                String sElementValue = oValueStack.peek() == null ? null : oValueStack.peek().toString();
//                if ( !(oNameStack.peek() == null && sElementName == null) && !(sElementName.equals(oNameStack.peek())) )
//                    throw new SAXException(String.format("Malformed XML encountered: expected '%s' got %s'", sElementName, oNameStack.peek()));
                sElementName = Implode.toString(oNameStack, ".");
//                oLogger.info(String.format("endElement() s=%s s1=%s s2=%s == %s = %s, oNameStack.size() = %d = %s sElementValue = `%s`",
//                    s, s1, s2, sElementName, sElementValue, oNameStack.size(), oNameStack, sElementValue));
                this.mapCurrentLineNameValuePairs.put(sElementName, sElementValue);
                oValueStack.pop();
                oNameStack.pop();
                }

            @Override
            public void characters(char[] chars, int i, int i1)
                    throws SAXException
                {
                String sElementName = Implode.toString(oNameStack, ".");
                String sElementValue = new String(chars).substring(i, i + i1);

                if ( sElementValue.strip().length() == 0 )
                    sElementValue = "";

                //oLogger.info(String.format("characters() i = %d i1 = %d '%s' = '%s'", i, i1, sElementName, sElementValue));

                if ( oValueStack.peek() == null )
                    {
                    oValueStack.pop();
                    oValueStack.push(new StringBuilder());
                    }

                oValueStack.peek().append(sElementValue);
                }

            @Override
            public void ignorableWhitespace(char[] chars, int i, int i1)
                    throws SAXException
                {
//                oLogger.info(String.format("ignorableWhitespace(): i = %s i1 = %s", i, i1));
                }

            @Override
            public void processingInstruction(String s, String s1)
                    throws SAXException
                {
//                oLogger.info(String.format("processingInstruction(): s = %s s1 = %s", s, s1));
                }

            @Override
            public void skippedEntity(String s)
                    throws SAXException
                {
//                oLogger.info(String.format("skippedEntity(): s = %s", s));
                }

            private String kebabCase(String... asValues)
                {
                StringBuilder oSB = new StringBuilder();
                if ( asValues == null )
                    return null;
                for ( String sValue : asValues )
                    oSB.append(String.format("%s%s", oSB.length() > 0 ? "-" : "", sValue));
                return oSB.toString();
                }

            private NamespaceEntry addorgetNamespaceEntry(String sCurrentElementName)
                {
                LinkedHashMap<String,NamespaceEntry> mapNamespaceCurrent = this.mapNamespaceRoot;
                NamespaceEntry oNamespaceEntry = null;
                String[] asColumnNames = new String[this.oNameStack.size() + 1];

                int i = 0;
                for ( String sElementName : this.oNameStack )
                    asColumnNames[i++] = sElementName;
                asColumnNames[i] = sCurrentElementName;

//                oLogger.info(String.format("addorgetNamespaceEntry(): this.oNameStack = %s asColumnNames = [%s] i = %s",
//                        this.oNameStack, Implode.toString(asColumnNames, "`", "`", ", "), i));

                for ( String sElementName : asColumnNames )
                    {
                    if (mapNamespaceCurrent.containsKey(sElementName))
                        {
                        oNamespaceEntry = mapNamespaceCurrent.get(sElementName);
                        }
                    else
                        {
                        oNamespaceEntry = new NamespaceEntry(sElementName, i);
                        mapNamespaceCurrent.put(oNamespaceEntry.sElementName, oNamespaceEntry);
                        }
                    mapNamespaceCurrent = oNamespaceEntry.mapNamespaceChildren;
                    }

//                oLogger.info(String.format("addorgetNamespaceEntry(): this.mapNamespaceRoot = %s", this.mapNamespaceRoot));

                return oNamespaceEntry;
                }
            }

        try {
            if ( sColumnValue == null )
                return new LinkedHashMap<>();
            XMLDocumentHandler oXMLDocumentHandler = new XMLDocumentHandler();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setSchema(null);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            SAXParser saxParser = factory.newSAXParser(); // XXE attack, see https://rules.sonarsource.com/java/RSPEC-2755
            InputStream oByteArrayInputStream = new ByteArrayInputStream(sColumnValue.getBytes(StandardCharsets.UTF_8));
            saxParser.parse(oByteArrayInputStream, oXMLDocumentHandler);
//            oLogger.info(String.format("extractNameValuePairsFromXML() oXMLDocumentHandler.mapCurrentLineNameValuePairs = %s", oXMLDocumentHandler.mapCurrentLineNameValuePairs));

            return oXMLDocumentHandler.mapCurrentLineNameValuePairs;
            }
        catch (ParserConfigurationException | SAXException | IOException ex) {
//            oLogger.error(String.format("! XML parsing failed: %s='%s'", sColumnName, sColumnValue), ex);
            LinkedHashMap<String,String> mapCurrentLineNameValuePairs = new LinkedHashMap<>();
            mapCurrentLineNameValuePairs.put(sColumnName, sColumnValue);
            return mapCurrentLineNameValuePairs;
            }
        }

    /**
     * NOT USED: Keeping this around so we can build a command line template of field ordering.
     */
    private LinkedHashMap<String, String> sortCurrentLineByColumnNames(LinkedHashMap<String, String> listCurrentLineNameValuePairs)
            throws NoSuchFieldException
        {
        LinkedHashMap<String, String> listNewCurrentLineNameValuePairs = new LinkedHashMap<>();
        for ( String sColumnName : this.setColumnNames )
            listNewCurrentLineNameValuePairs.put(sColumnName, "");
//        oLogger.info(String.format("sortCurrentLineByColumnNames(): this.setColumnNames = '%s'", this.setColumnNames));
//        oLogger.info(String.format("sortCurrentLineByColumnNames(): listNewCurrentLineNameValuePairs = '%s'", listNewCurrentLineNameValuePairs.toString()));
        for ( Map.Entry<String,String> oEntry : listCurrentLineNameValuePairs.entrySet() )
            {
            if ( !listNewCurrentLineNameValuePairs.containsKey(oEntry.getKey()) )
                throw new NoSuchFieldException(String.format("unknown column name encountered: '%s' (value = '%s'). Valid columns are '%s'", oEntry.getKey(), oEntry.getValue(), this.setColumnNames.toString()));
            listNewCurrentLineNameValuePairs.replace(oEntry.getKey(), oEntry.getValue());
            }

//        oLogger.info(String.format("sortCurrentLineByColumnNames(): WAS '%s'", listCurrentLineNameValuePairs.toString()));
//        oLogger.info(String.format("sortCurrentLineByColumnNames(): NOW '%s'", listNewCurrentLineNameValuePairs.toString()));

        return listNewCurrentLineNameValuePairs;
        }

    /* -- GETTERS & SETTERS -- */

    public CSVXMLJSONReaderWriter setUnknownColumnNamePrefix(String sUnknownColumnNamePrefix)
        {
        if (sUnknownColumnNamePrefix == null || sUnknownColumnNamePrefix.isEmpty())
            throw new IllegalArgumentException("column name prefix cannot be null");
        this.sUnknownColumnNamePrefix = sUnknownColumnNamePrefix;
        return this;
        }

    public CSVXMLJSONReaderWriter setCommentLinePrefix(String sLineCommentPrefix)
        {
        if ( sLineCommentPrefix == null || sLineCommentPrefix.isEmpty() )
            throw new NullPointerException("comment line prefix cannot be null");
        this.sLineCommentPrefix = sLineCommentPrefix;
        return this;
        }

    public CSVXMLJSONReaderWriter setColumnAlias(String sOriginalColumnName, String sOutputColumnName)
        {
        if ( sOriginalColumnName == null || sOriginalColumnName.isEmpty() )
            throw new NullPointerException("original column name is required");
        if ( sOutputColumnName == null || sOutputColumnName.isEmpty() )
            throw new NullPointerException("aliased column name to output is required");
        this.mapColumnAliases.put(sOriginalColumnName, sOutputColumnName);
        return this;
        }

    private String getColumnName(int iColumnIndex)
        {
        if ( this.setColumnNames.size() < iColumnIndex + 1 )
            while ( this.setColumnNames.size() < iColumnIndex + 1 )
                {
                this.addColumnName(String.format("%s%d", this.sUnknownColumnNamePrefix, this.setColumnNames.size() + 1));
                this.asCachedColumnNames = null;
                }

        if ( this.asCachedColumnNames == null || this.asCachedColumnNames.length != this.setColumnNames.size() )
            this.asCachedColumnNames = this.setColumnNames.toArray(new String[ this.setColumnNames.size() ]);

        return this.asCachedColumnNames[iColumnIndex];
        }

    private String getColumnName(String sColumnName)
        {
        return this.getColumnAlias(sColumnName);
        }

    private String getColumnAlias(String sColumnName)
        {
        if ( this.mapColumnAliases.containsKey(sColumnName) )
            return this.mapColumnAliases.get(sColumnName);
        else
            return sColumnName;
        }

    private CSVXMLJSONReaderWriter addColumnName(String sColumnName)
        {
        this.setColumnNames.add(sColumnName);
        this.asCachedColumnNames = null;
        return this;
        }

    public CSVXMLJSONReaderWriter setColumnOrderingPreference(Iterable<String> setColumnNames)
        {
        for ( String sColumnName : setColumnNames )
            this.setOutputColumnNames.add(sColumnName);
        return this;
        }

    public CSVXMLJSONReaderWriter setExcludedColumn(String sColumnName)
        {
        this.setExcludedColumnNames.add(sColumnName);
        return this;
        }

    /* -- FUNCTORS -- */
    public Iterable<String> getColumnNames()
        {
        return new LinkedHashSet<>();
        }

    public boolean hasColumn(String sColumnName)
        {
        return false;
        }

    /* -- DESTRUCTORS -- */

     public CSVXMLJSONReaderWriter close()
             throws IOException
        {
             if ( this.oInputStream != null )
                 this.oInputStream.close();
         return this;
         }

    @Override
    protected void finalize()
        {
        try {
            this.close();
            }
        catch (IOException e) {
            throw new RuntimeException(e);
            }
        }
    }
