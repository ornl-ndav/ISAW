/*
 * File:  XmlDWriter.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2003/03/03 17:02:34  pfpeterson
 *  Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 *  Revision 1.2  2002/11/27 23:26:45  pfpeterson
 *  standardized header
 *
 *  Revision 1.1  2002/06/18 19:47:12  rmikk
 *  Initial Checkin
 *
 */
package DataSetTools.writer;
import java.io.*;
import DataSetTools.dataset.*;
import java.util.zip.*;

/**
* Contains Methods to write out arrays of Isaw Data Sets in either
* an xml format or its zip( extension must be zip) analog
*/
public class XmlDWriter extends Writer
 {String extension;

  /**
  * Constructor for this writer
  * @param  data_destination_name  The file name to which the data sets will
  *                                be saved. If the extension is ".zip" it 
  *                                will be saved in a zip format otherwise it
  *                                will be saved in an xml format
  */
  public XmlDWriter( String data_destination_name)
   { super(data_destination_name);
     int i= data_destination_name.lastIndexOf('.');
     if( i < 0)
       extension = "";
     else
       extension = data_destination_name.substring( i+1 );
   }

  /**
  * Writes out the datasets in either an xml or in a zipped form of the
  * xml file( if extension is ".zip")
  * 
  * @param DS[]  The array of data sets that will be written
  */ 
  public void writeDataSets( DataSet DS[])
   { try{
     File ff = new File( data_destination_name );
     FileOutputStream fi = new FileOutputStream(ff);
     OutputStream fo = fi;
     boolean zip = false;
     if( extension.toUpperCase().equals("ZIP"))
       {fo= new ZipOutputStream( fi );
        zip = true;
       }
    
     if( DS == null)
      {
        DataSetTools.util.SharedData.addmsg(" No Data sets to write");
        return;
      }
     if( DS.length < 1)
     {
        DataSetTools.util.SharedData.addmsg(" No Data sets to write");
        return;
      }
     if( zip)
       ((ZipOutputStream)fo).putNextEntry( new ZipEntry("Entry"));
     fo.write( 
  "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<DataSetList size=\"".getBytes());
     fo.write(( ""+DS.length+"\">\n").getBytes());
    // if( zip)
     //  ((ZipOutputStream)fo).closeEntry();
     for (int i= 0; i<DS.length; i++)
       { 
         DS[i].setStandAlone( false);
        
          if( zip)
              ((ZipOutputStream)fo).putNextEntry( new ZipEntry("Entry"+i));
     
         DS[i].XMLwrite( fo, 0);
        // if( zip)
         //  ((ZipOutputStream)fo).closeEntry();
        }
     fo.write("</DataSetList>".getBytes());
     fo.close();
     }
     catch( Exception s)
      { DataSetTools.util.SharedData.addmsg( "Exception="+
             s.getClass()+":"+s.getMessage());
        return;
      }
    }//writeDataSets
  
 /**
 * Test program for this module
 * @param  args[0] the filename where the data sets are saved<P>
 *
 * NOTE: The files loaded are the hrcs2447 and hrcs2995 run files
 */
 public static void main( String args[])
   { DataSet[] Ds1 = (new IsawGUI.Util()).loadRunfile( "C:/Isaw/SampleRuns/hrcs2447.run");
     DataSet[] Ds2 = (new IsawGUI.Util()).loadRunfile( "C:/Isaw/SampleRuns/hrcs2955.run");
     DataSet[] DS = new DataSet[ Ds1.length+Ds2.length];
     int i;
     for (i = 0; i< Ds1.length ; i++)
       DS[i]=Ds1[i];
     int k = Ds1.length;
     for (i = 0; i< Ds2.length ; i++)
       DS[k+i]=Ds2[i];  
     XmlDWriter xmw = new XmlDWriter( args[0]);
     xmw.writeDataSets( DS );
    
    }
  }
