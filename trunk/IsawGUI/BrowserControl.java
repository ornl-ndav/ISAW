/*
 * File: BrowserControl.java
 *
 * Copyright (C) 1999, Alok Chatterjee
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.8  2004/04/21 22:34:57  bouzekc
 * Now retrieves the PREFERRED_BROWSER from IsawProps.dat under *NIX systems.
 * Defaults to Netscape if this tag is not found.
 *
 * Revision 1.7  2003/05/28 18:58:20  pfpeterson
 * Changed System.getProperty to SharedData.getProperty
 *
 * Revision 1.6  2003/01/27 14:58:38  rmikk
 * Changed so that only the http: protocol changes the "m"
 *    in the extension to %06D
 *
 * Revision 1.5  2002/12/11 16:00:12  pfpeterson
 * Workaround in windows where 'url.dll' does not work with '.htm' or '.html'
 * files. This is done by replacing the 'm' with its hexidecimal equivalent.
 * Added DEBUG flag and statements that are printed when it is true.
 *
 * Revision 1.4  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 */

package IsawGUI;

import DataSetTools.util.SharedData;
import java.io.*;

public class BrowserControl
{
  public static final boolean DEBUG=false;
    /**
     * Display a file in the system browser. If you want to display a
     * file, you must include the absolute path name.
     *
     * @param url the file's url (the url must start with either "http://"
or
     * "file://").
     */
    public static void displayURL(String url)
    {
        boolean windows = isWindowsPlatform();
        String cmd = null;

        try
        {
            if (windows )
            {
                // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
               if( url.startsWith("http:"))
                  {
                   int index=url.indexOf(".htm");
                   if(index==url.length()-4){
                      if(DEBUG) System.out.println("URL:"+url);
                      url=url.substring(0,index)+".htm%06D";
                      if(DEBUG) System.out.println("  ->"+url);
                   }else if(index==url.length()-5){
                      if(DEBUG) System.out.println("URL:"+url);
                      url=url.substring(0,index)+".ht%06Dl";
                      if(DEBUG) System.out.println("  ->"+url);
                   }
                }
                cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
                if(DEBUG) System.out.println("CMD="+cmd);
                Process p = Runtime.getRuntime().exec(cmd);
            }
            else
            {
              String UNIX_PATH;
              
              UNIX_PATH = SharedData.getProperty( "PREFERRED_BROWSER" );
              
              if( UNIX_PATH == null ) {
                UNIX_PATH = "netscape";
              }
              System.out.println( url );
              cmd = UNIX_PATH + " " + url;
              Runtime.getRuntime().exec(cmd);
            }
        }
        catch(IOException x)
        {
            // couldn't exec browser
            System.err.println("Could not invoke browser, command=" + cmd);
            System.err.println("Caught: " + x);
        }
    }

    /**
     * Try to determine whether this application is running under Windows
     * or some other platform by examing the "os.name" property.
     *
     * @return true if this application is running under a Windows OS
     */
    public static boolean isWindowsPlatform()
    {
        String os = SharedData.getProperty("os.name");

        if ( os != null && os.startsWith(WIN_ID))
            return true;
        else
            return false;
    }

    /**
     * Simple example.
     */
    public static void main(String[] args)
    {
        displayURL("http://www.javaworld.com");
    }


    // Used to identify the windows platform.
    private static final String WIN_ID = "Windows";

    // The default system browser under windows.
    private static final String WIN_PATH = "rundll32";

    // The flag to display a url.
    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
}
