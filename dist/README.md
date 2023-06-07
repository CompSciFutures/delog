# delog: JSON/XML logfile deserializer

This tool will de-serialse/expand/explode any embedded XML/JSON data structures found in comma-separated (CSV) logfiles so that each element or field found in JSON or XML arrays or data structures found in the CSV file will be converted into separate, individual CSV columns for each.

This is particularly useful for reading logfiles that contain unstructured data into Excel, PowerBI, Tableau, Python, MATLAB and many other tools that expect tabular data.

Alot of instrumentation for security operations, IoT devices, drones and data lakes is in the form of logfiles which contains a combination of tabular data & unstructured data. This tool addresses that complexity and allows one to more readily make use of these files, either to inspect them or for applications such as data science. 

A mix of JSON objects, JSON arrays and XML can all be combined into a single CSV file.

## Installing

Download the contents of the **dist** folder - the delog.jar is an uber-jar with everything you need.

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

## License

```text
“Commons Clause” License Condition v1.0
=======================================

The Software is provided to you by the Licensor under the License,
as defined below, subject to the following condition.

Without limiting other conditions in the License, the grant of rights
under the License will not include, and the License does not grant to
you, the right to Sell the Software.

For purposes of the foregoing, “Sell” means practicing any or all of
the rights granted to you under the License to provide to third
parties, for a fee or other consideration (including without
limitation fees for hosting or consulting/ support services related
to the Software), a product or service whose value derives, entirely
or substantially, from the functionality of the Software. Any license
notice or attribution required by the License must also include this
Commons Clause License Condition notice.

Software: tellusion-utils
License: GNU General Public License version 3 w/ Commons Clause
Licensor: Andrew Prendergast ap@tellusion.com

GNU General Public License version 3
------------------------------------

(C) COPYRIGHT 2000-2023 Andrew Prendergast

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License version 3 as
published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
