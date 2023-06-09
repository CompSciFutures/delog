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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class TellusionCSVWriter
    {
    private static Logger oLogger = Logger.getLogger(TellusionCSVWriter.class);

    private OutputStreamWriter oOutputStreamWriter;

    private int iRows = 0;

    public TellusionCSVWriter(OutputStreamWriter oOutputStreamWriter)
            throws IOException
        {
        if ( oOutputStreamWriter == null )
            throw new IOException("OutputStreamWriter is required");
        this.oOutputStreamWriter = oOutputStreamWriter;
        }

    public void writeNext(String[] asFields)
            throws IOException
        {
//        oLogger.info(String.format("writeNext(): asFields=%s", asFields));
        int iFields = 0;
        for ( String sField : asFields )
            {
            if ( iFields > 0 )
                this.oOutputStreamWriter.write(",");
            if (sField != null)
                {
                String s = "\"" + sField.replaceAll("\"", "\"\"") + "\"";
                this.oOutputStreamWriter.write(s);
                }
            iFields++;
            }
        this.oOutputStreamWriter.write("\r\n");

//        if ( iRows % 128 == 0 )
//            this.oOutputStreamWriter.flush();
        }

    public void close()
            throws IOException
        {
        this.oOutputStreamWriter.close();
        }
    }
