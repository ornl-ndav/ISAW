/*
 * File:  XmlWriter.java
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
 *  Revision 1.3  2002/11/27 23:26:45  pfpeterson
 *  standardized header
 *
 */

package DataSetTools.writer;

import  DataSetTools.dataset.*;
import  java.io.*;
import DataSetTools.retriever.*;
import NexIO.Write.*;
import NexIO.Write.NexApi.*;
/**
 * Root class for objects that write DataSet objects to files. 
 * Derived classes for particular types of files must actually implement 
 * the methods write the data sets.
 */

public class XmlWriter extends Writer
{
    String filename;


    /**
     * Construct the Writer for the specified destination name.
     *
     * @param data_destination_name  This identifies the data destination.  
     *                               For file data writers, this should be 
     *                               the fully qualified file name.
     */

    public XmlWriter( String data_destination_name )
    {super( data_destination_name);
     
      
    }

    
    /**
     * Send the specified array of data sets to the current data destination.
     * If an array of DataSets includes both monitor and histogram DataSets
     * the recommended convention is to list the monitor DataSet in the array
     * before the list of histogram DataSets to which it applies.  That is
     * M1, H1, H2, H3, M2, H3, H4 would be interpreted to mean that M1 is
     * the monitor DataSet for histograms H1, H2, H3 and the M2 is the monitor
     * DataSet for histograms H3 and H4.
     */
    public void writeDataSets( DataSet ds[] )
    { if( ds == null)
	return ;
      XmlWriteNode nwr = new XmlWriteNode( data_destination_name );
      if( nwr.getErrorMessage() != null)
	  if(! nwr.getErrorMessage().equals(""))
            { System.out.println("Could not Create File");
            
              return;
            } 
      NxWriter Writer = new NxWriter( (NxWriteNode)nwr  );
      DataSet Hist[], Monit[];
      Hist = new DataSet[1];
      Monit = new DataSet[1];
      Monit[0] = null;
      for( int i = 0; i < ds.length; i++)
       { DataSet D = ds[i];
         if( D == null)
            Monit[0] = null;
        else
           {String S =(String) D.getAttributeValue( Attribute.DS_TYPE );
             if( S.equals( Attribute.MONITOR_DATA))
                Monit[0] = D;
             else
                { Hist[0] = D;
                   Writer.Append( Monit, Hist);
                }
           }
       }

      Writer.close();
     
      
    }
public static void main( String args[])
 {String filename = "C:\\SampleRuns\\HRCS2441";
  DataSet DS[];
  DS = (new IsawGUI.Util()).loadRunfile( filename +".run");
  XmlWriter W = new XmlWriter( filename+".xml");
  W.writeDataSets( DS );


  }
}
