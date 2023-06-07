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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class TellusionCSVReader
    {
    private static Logger oLogger = Logger.getLogger(TellusionCSVReader.class);

    private final int BUFFER_SIZE = 64;
    private InputStreamReader oInputStreamReader;
    private LinkedList<Character> listBufferedChars = new LinkedList<>();
    private boolean bEOF = false;

    public TellusionCSVReader(InputStreamReader oInputStreamReader)
            throws IOException
        {
        if ( oInputStreamReader == null )
            throw new IOException("InputStreamReader is required");
        this.oInputStreamReader = oInputStreamReader;
        }

    private Character getNextCharFromBuffer()
            throws IOException
        {
        this.fillBuffer();

        if ( this.listBufferedChars.size() == 0 )
            return null;

        return this.listBufferedChars.removeFirst();
        }

    private Character peekNextCharFromBuffer()
            throws IOException
        {
        this.fillBuffer();

        if ( this.listBufferedChars.size() == 0 )
            return null;

        return this.listBufferedChars.peekFirst();
        }

    private void fillBuffer()
            throws IOException
        {
        int iReadBytes = 0;
        char[] acBuffer = new char[BUFFER_SIZE];

        if ( this.listBufferedChars.size() < BUFFER_SIZE && !this.bEOF )
            if ( (iReadBytes = this.oInputStreamReader.read(acBuffer, 0, BUFFER_SIZE)) == -1 )
                this.bEOF = true;

        for ( int iByte = 0; iByte < iReadBytes; iByte++ )
            this.listBufferedChars.add(acBuffer[iByte]);
        }

    private boolean isEOF()
        {
        return this.bEOF && this.listBufferedChars.size() == 0;
        }

    public String[] readNext()
            throws IOException
        {
        LinkedList<String> listFields = new LinkedList<>();
        StringBuilder oFieldSB = null;
        char[] acBuffer = new char[10];
        boolean bInQuotes = false;

        // fill listFields to EOL
        while ( true )
            {
            Character ocNextChar = this.getNextCharFromBuffer();
            if ( ocNextChar == null )
                break;

//            oLogger.info(String.format("- readNext() ocNextChar = '%s' bInQuotes = %s", ocNextChar, bInQuotes));

            if ( ocNextChar == '"' && this.peekNextCharFromBuffer() == '"' && bInQuotes == true )
                {
                // handle double quotes
                if ( this.getNextCharFromBuffer() != '"' ) // eat the second quote
                    throw new IOException("quote failure");
                }
            else if (!bInQuotes)
                {
                if (ocNextChar == '\n')
                    break;
                else if (ocNextChar == '\r')
                    continue;
                else if (ocNextChar == '"' )
                    {
                    bInQuotes = true;
                    continue;
                    }
                else if (ocNextChar == ',')
                    {
                    //oLogger.info(String.format("- readNext() COMMA oFieldSB = '%s'", oFieldSB == null ? null : oFieldSB.toString()));
                    if ( oFieldSB != null )
                        listFields.add(oFieldSB.toString());
                    oFieldSB = null;
                    continue;
                    }
                }
            else
                {
                if (ocNextChar == '"' )
                    {
                    bInQuotes = false;
                    continue;
                    }
                }

            if ( oFieldSB == null )
                oFieldSB = new StringBuilder();
            oFieldSB.append(ocNextChar);
            }

        // add current field (if any) to list
        if ( oFieldSB != null )
            listFields.add(oFieldSB.toString());

        if ( this.isEOF() && listFields.size() == 0 )
            return null;

        return listFields.toArray(new String[listFields.size()]);
        }
    }
