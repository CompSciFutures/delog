import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import com.tellusion.util.CSVXMLJSONReaderWriter;

import com.tellusion.util.Implode;
import org.apache.log4j.*;
import org.junit.*;

public class TestCSVJSONReaderWriter
    {
    private static Logger oLogger = Logger.getLogger(TestCSVJSONReaderWriter.class);

    @Before
    public void log4jBefore()
        {
        Properties props = new Properties();
        org.apache.log4j.BasicConfigurator.configure();
        props.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%6r [%t]%x %m%n");
//    		props.setProperty("log4j.rootLogger", "TRACE,stdout");
        props.setProperty("log4j.rootLogger", "DEBUG,stdout");
        PropertyConfigurator.configure(props);
        NDC.clear(); NDC.push("");
        this.log43jAfter();
        }

//        @After
    public void log43jAfter()
        {
        String[] asDeadFiles =
            {
//                "temp/10rows-converted.csv",
            "blah"
            };
        for ( String sDeadFile : asDeadFiles )
            new File(sDeadFile).delete();
        }

    @Test
    public void testMain() throws Throwable
        {
        new CSVXMLJSONMain().ParseCommandLineArgs(null);
        }

    @Test
    public void testSet1() throws Throwable
        {
        CSVXMLJSONReaderWriter oCSVJSONReaderWriter = new CSVXMLJSONReaderWriter();
        oCSVJSONReaderWriter
                .setColumnAlias("AuditData", "AuditDataJSON")
                ._setIN_MEMORY(true)
                .setInputFile("testdata/set1.csv")
                .writeOutputToFile("temp/set1-converted.csv")
                .close();

        LinkedList<LinkedHashMap<String, String>> listAllData = oCSVJSONReaderWriter._getIN_MEMORY();

        Assert.assertEquals(111, listAllData.size());
        Assert.assertEquals(211, listAllData.get(10).values().size());
        }

    @Test
    public void testSet2() throws Throwable
        {
        CSVXMLJSONReaderWriter oCSVJSONReaderWriter = new CSVXMLJSONReaderWriter();
        oCSVJSONReaderWriter
                ._setIN_MEMORY(true)
                .setInputFile("testdata/set2.csv")
                .writeOutputToFile("temp/set2-converted.csv")
                .close();

        LinkedList<LinkedHashMap<String, String>> listAllData = oCSVJSONReaderWriter._getIN_MEMORY();

        Assert.assertEquals(1292955, listAllData.size());
        }

    /**
     * @TODO option to omit column header row
     * @TODO specifying column prefix
     * @TODO include XML attributes
     */
    @Test
    public void testSet3() throws Throwable
        {
        CSVXMLJSONReaderWriter oCSVJSONReaderWriter;

        // NB the JSON in row 1 doesn't parse because the google parser doesnt support \xXX or u'.....'

        oCSVJSONReaderWriter = new CSVXMLJSONReaderWriter();
        oCSVJSONReaderWriter
                .setCommentLinePrefix("@")
                .setColumnAlias("ColA", "AAA")
                ._setIN_MEMORY(true)
                .setColumnOrderingPreference(List.of(new String[]{"ColD", "ColC"}))
                .setExcludedColumn("ColD.pics.a0.url.a0")
                .setInputFile("testdata/set3.csv")
                .writeOutputToFile("temp/set3-converted.csv")
                .close();

        LinkedList<LinkedHashMap<String, String>> listAllData = oCSVJSONReaderWriter._getIN_MEMORY();

        Assert.assertEquals(6, listAllData.size());
        Assert.assertEquals(21, listAllData.get(0).size());

        String[] asRow;

        // check row 0
        asRow = new String[listAllData.get(0).values().size()];
        asRow = listAllData.get(0).values().toArray(asRow);
        oLogger.info(String.format("listAllData.get(0).values().toArray() = %s", Implode.toString(asRow)));
        Assert.assertEquals("ColD", asRow[0]);
        Assert.assertEquals("ColC", asRow[1]);
        Assert.assertEquals("AAA", asRow[2]);
        Assert.assertEquals("ColB", asRow[3]);
        Assert.assertFalse(new HashSet<String>(List.of(asRow)).contains("ColD.pics.a0.url.a0"));

        // check row 1
        asRow = new String[listAllData.get(1).values().size()];
        asRow = listAllData.get(1).values().toArray(asRow);
        oLogger.info(String.format("listAllData.get(1).values().toArray() = %s", Implode.toString(asRow)));
        //Assert.assertEquals("ColD", asRow[0]);
        Assert.assertEquals("c", asRow[1]);
        Assert.assertEquals("a", asRow[2]);
        Assert.assertEquals("b", asRow[3]);

        // check row 2
        asRow = new String[listAllData.get(2).values().size()];
        asRow = listAllData.get(2).values().toArray(asRow);
        oLogger.info(String.format("listAllData.get(1).values().toArray() = %s", Implode.toString(asRow)));
        //Assert.assertEquals("ColD", asRow[0]);
        Assert.assertEquals("f", asRow[1]);
        Assert.assertEquals("d", asRow[2]);
        Assert.assertEquals("e", asRow[3]);

        // check row 3
        asRow = new String[listAllData.get(3).values().size()];
        asRow = listAllData.get(3).values().toArray(asRow);
        oLogger.info(String.format("listAllData.get(1).values().toArray() = %s", Implode.toString(asRow)));
        //Assert.assertEquals("ColD", asRow[0]);
        Assert.assertEquals("I", asRow[1]);
        Assert.assertEquals("g", asRow[2]);
        Assert.assertEquals("h", asRow[3]);
        }

    @Test
    public void testMatches() throws Throwable
        {
        String sXMLColumnValue = " <menu id=\"file\" value=\"File\">\n" +
                "  <popup>\n" +
                "    <menuitem value=\"New\" onclick=\"CreateNewDoc()\" />\n" +
                "    <menuitem value=\"Open\" onclick=\"OpenDoc()\" />\n" +
                "    <menuitem value=\"Close\" onclick=\"CloseDoc()\" />\n" +
                "  </popup>\n" +
                "</menu>   ";

        String sJSONO1ColumnValue = "   {\"\"menu\"\": {\n" +
                "  \"\"id\"\": \"\"file\"\",\n" +
                "  \"\"value\"\": \"\"File\"\",\n" +
                "  \"\"popup\"\": {\n" +
                "    \"\"menuitem\"\": [\n" +
                "      {\"\"value\"\": \"\"New\"\", \"\"onclick\"\": \"\"CreateNewDoc()\"\"},\n" +
                "      {\"\"value\"\": \"\"Open\"\", \"\"onclick\"\": \"\"OpenDoc()\"\"},\n" +
                "      {\"\"value\"\": \"\"Close\"\", \"\"onclick\"\": \"\"CloseDoc()\"\"}\n" +
                "    ]\n" +
                "  }\n" +
                "}}   ";

        String sJSONO2ColumnValue = "{\n" +
                "  \"\"rating\"\": 3.0,\n" +
                "  'reviewerName': u'an lam',\n" +
                "  'reviewText': u'Ch\\u1ea5t l\\u01b0\\u1ee3ng t\\u1ea1m \\u1ed5n',\n" +
                "  'categories': [u'Gi\\u1ea3i Tr\\xed - Caf\\xe9'],\n" +
                "  'gPlusPlaceId': u'108103314380004200232',\n" +
                "  'unixReviewTime': 1372686659,\n" +
                "  'reviewTime': u'Jul 1, 2013',\n" +
                "  'gPlusUserId': u'100000010817154263736'\n" +
                "}";

        String sJSONAColumnValue = "  [\"\"1\"\",\"\"2\"\",\"\"3\"\",4,[8,7,6,5]]  ";

        String sWHITESPACEColumnValue = "\n" +
                "  ";

        Pattern oPattern;

        oPattern = Pattern.compile(CSVXMLJSONReaderWriter.REGEX_XML, Pattern.MULTILINE);
        Assert.assertTrue(oPattern.matcher(sXMLColumnValue).find());
        Assert.assertTrue(oPattern.matcher(sXMLColumnValue.trim()).find());
        Assert.assertFalse(oPattern.matcher(sJSONO1ColumnValue).find());
        Assert.assertFalse(oPattern.matcher(sJSONO2ColumnValue).find());
        Assert.assertFalse(oPattern.matcher(sJSONAColumnValue).find());
        Assert.assertFalse(oPattern.matcher(sWHITESPACEColumnValue).find());

        oPattern = Pattern.compile(CSVXMLJSONReaderWriter.REGEX_JSON_OBJECT, Pattern.MULTILINE);
        Assert.assertFalse(oPattern.matcher(sXMLColumnValue).find());
        Assert.assertTrue(oPattern.matcher(sJSONO1ColumnValue).find());
        Assert.assertTrue(oPattern.matcher(sJSONO1ColumnValue.trim()).find());
        Assert.assertTrue(oPattern.matcher(sJSONO2ColumnValue).find());
        Assert.assertTrue(oPattern.matcher(sJSONO2ColumnValue.trim()).find());
        Assert.assertFalse(oPattern.matcher(sJSONAColumnValue).find());
        Assert.assertFalse(oPattern.matcher(sJSONAColumnValue.trim()).find());
        Assert.assertFalse(oPattern.matcher(sWHITESPACEColumnValue).find());

        oPattern = Pattern.compile(CSVXMLJSONReaderWriter.REGEX_JSON_ARRAY, Pattern.MULTILINE);
        Assert.assertFalse(oPattern.matcher(sXMLColumnValue).find());
        Assert.assertFalse(oPattern.matcher(sJSONO1ColumnValue).find());
        Assert.assertFalse(oPattern.matcher(sJSONO2ColumnValue).find());
        Assert.assertTrue(oPattern.matcher(sJSONAColumnValue).find());
        Assert.assertTrue(oPattern.matcher(sJSONAColumnValue.trim()).find());
        Assert.assertFalse(oPattern.matcher(sWHITESPACEColumnValue).find());

        oPattern = Pattern.compile(CSVXMLJSONReaderWriter.REGEX_WHITESPACE, Pattern.MULTILINE);
        Assert.assertFalse(oPattern.matcher(sXMLColumnValue).find());
        Assert.assertFalse(oPattern.matcher(sJSONO1ColumnValue).find());
        Assert.assertFalse(oPattern.matcher(sJSONO2ColumnValue).find());
        Assert.assertFalse(oPattern.matcher(sJSONAColumnValue).find());
        Assert.assertTrue(oPattern.matcher(sWHITESPACEColumnValue).find());
        }
    }
