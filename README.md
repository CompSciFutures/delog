# delog: JSON/XML logfile deserializer

This tool will de-serialse/expand/explode any embedded XML/JSON data structures found in comma-separated (CSV) logfiles so that each element or field found in JSON or XML arrays or data structures found in the CSV file will be converted into separate, individual CSV columns for each.

This is particularly useful for reading logfiles that contain unstructured data into Excel, PowerBI, Tableau, Python, MATLAB and many other tools that expect tabular data.

Alot of instrumentation for security operations, IoT devices, drones and data lakes is in the form of logfiles which contains a combination of tabular data & unstructured data. This tool addresses that complexity and allows one to more readily make use of these files, either to inspect them or for applications such as data science.

A mix of JSON objects, JSON arrays and XML can all be combined into a single CSV file.

Tab-delimited is not currently implemented as it's support for multi-line isn't as well defined - let me know if there's a need for it.

## Installing

Download the contents of the **dist** folder - the delog.jar is an uber-jar with everything you need.

Or just grab the <a href="https://github.com/CompSciFutures/delog/releases/download/HEAD/delog-1.0.0.zip">latest release ZIP</a> file from GitHub.

## Example usage

```text
java -jar delog --include ColC --include ColB --include ColA --exclude ColD testdata.csv
```

Or on Windows:

```text
delog.exe --include ColC --include ColB --include ColA --exclude ColD testdata.csv
```

Will convert the following CSV:

```text
ColA,ColB,ColC,ColD
d,e,f,"      <Ingredient>
         <Qty unit="ml">500</Qty>
         <Qty unit="g">9</Qty>
         <Item>Cottage cheese</Item>
      </Ingredient>"
```

Into:

```text
"ColC","ColB","ColA","Ingredient.Qty","Ingredient.Qty2","Ingredient.Item","Ingredient"
"f","e","d","500","9","Cottage cheese",
```

Note that JSON & XML are supported, so a mix of JSON objects, JSON arrays and XML can all be combined into a single CSV file.

## Example: Office 365 Audit logs with embedded JSON

Here is a typical exmaple of unstructured data in security logs: if you have Office 365 E3 or higher, you probably have access to audit logs in O365 admin. They are CSV logs, but they contain within them unstructured information about each event that's in the form of a JSON object inside a single CSV cell.

Delog allows you to read them, easily.  To get the logs, follow these instructions.

**1. Go to Office 365 Admin:**

![O365-1.png](images%2FO365-1.png)

**2. Click 'Show All'**

![O365-2.png](images%2FO365-2.png)

**3. Click 'Compliance'**

![O365-3.png](images%2FO365-3.png)

**4. Click 'Audit'**

![O365-4.png](images%2FO365-4.png)

**5. Select your search scope and click 'Search' then wait for your search to finish. Click 'Completed' once it's ready**

![O365-5.png](images%2FO365-5.png)

There, you should get a CSV with embedded JSON representing unstructured data which can be run through **delog** before being able to inspect it in Excel. 

