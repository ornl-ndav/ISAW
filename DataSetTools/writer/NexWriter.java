/*
 * File:  NexWriter.java
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
 *  Revision 1.5  2007/07/04 17:17:55  rmikk
 *  Now writes out NeXus files that only contain monitors
 *
 *  Revision 1.4  2004/01/24 22:49:30  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.3  2002/11/27 23:26:45  pfpeterson
 *  standardized header
 *
 */

package DataSetTools.writer;

import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.io.File;
import java.util.Vector;

import  DataSetTools.dataset.*;
import NexIO.Write.*;
import NexIO.Write.NexApi.*;
/**
 * Root class for objects that write DataSet objects to files. 
 * Derived classes for particular types of files must actually implement 
 * the methods write the data sets.
 */

public class NexWriter extends Writer
{
    String filename;
    String errorMessage;

    /**
     * Construct the Writer for the specified destination name.
     *
     * @param data_destination_name  This identifies the data destination.  
     *                               For file data writers, this should be 
     *                               the fully qualified file name.
     */

    public NexWriter( String data_destination_name )
    {super( data_destination_name);
     errorMessage = null;
      
    }

   
    
    /**
     * Send the specified array of data sets to the current data destination.
     * If an array of DataSets includes both monitor and histogram DataSets
     * the recommended convention is to list the monitor DataSet in the array
     * before the list of histogram DataSets to which it applies.  That is
     * M1, H1, H2, H3, M2, H3, H4 would be interpreted to mean that M1 is
     * the monitor DataSet for histograms H1, H2, H3 and the M2 is the monitor
     * DataSet for histograms H3 and H4.
     * 
     * NOTE: Several NXentries will be made if there are several groups of 
     * monitor data sets in the array.  Use blank monitor data sets to create
     * separate NXentries for sets of data sets.
     */
    public void writeDataSets1( DataSet ds[] )// redo
    { 
       errorMessage ="";
       if( ds == null)
       {  errorMessage =" No DataSets to Save";
          return ;
       }
      NexWriteNode nwr = new NexWriteNode( data_destination_name );
      if( nwr.getErrorMessage() != null)
	  if(! nwr.getErrorMessage().equals(""))
            { System.out.println("Could not Create File");
              errorMessage +="Could not Create NeXus File";
              return;
            } 
      NxWriter Writer = new NxWriter( (NxWriteNode)nwr  );
      if( Writer.getErrorMessage() != null)
        if( !Writer.getErrorMessage().equals(""))
           {  System.out.println("Could not Create File");
           errorMessage +="Could not Create NeXus File";
             
              return;
           }
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
                   Monit[0] = null;
                }
           }
       }
      if( Monit[0] != null )//This means that there were no histograms
         Writer.Append( Monit[0], null);
      Writer.close();

      String message = nwr.getErrorMessage();
      if( message != null )
          if(! message.equals(""))
            {

             errorMessage +="Could not Create NeXus File:"+message;
             System.out.println("Could not Create File " + message);
            }
     
      
    }
    
    public String getErrorMessage()
    {
       return errorMessage;
    }
    /**
     * Send the specified array of data sets to the current data destination.
     * If an array of DataSets includes both monitor and histogram DataSets
     * the recommended convention is to list the monitor DataSet in the array
     * before the list of histogram DataSets to which it applies.  That is
     * M1, H1, H2, H3, M2, H3, H4 would be interpreted to mean that M1 is
     * the monitor DataSet for histograms H1, H2, H3 and the M2 is the monitor
     * DataSet for histograms H3 and H4.
     * 
     * NOTE: Several NXentries will be made if there are several groups of 
     * monitor data sets in the array.  Use blank monitor data sets to create
     * separate NXentries for sets of data sets.
     */
    public void writeDataSets( DataSet ds[] )// redo
    { 
       errorMessage ="";
       if( ds == null)
       {  errorMessage =" No DataSets to Save";
          return ;
       }
      NexWriteNode nwr = new NexWriteNode( data_destination_name );
      if( nwr.getErrorMessage() != null)
     if(! nwr.getErrorMessage().equals(""))
            { System.out.println("Could not Create File");
              errorMessage +="Could not Create NeXus File";
              return;
            } 
      NxWriter Writer = new NxWriter( (NxWriteNode)nwr  );
      if( Writer.getErrorMessage() != null)
        if( !Writer.getErrorMessage().equals(""))
           {  System.out.println("Could not Create File");
           errorMessage +="Could not Create NeXus File";
             
              return;
           }
      DataSet Hist[], Monit[];
      int kMon=0;
      int kHist =0;
      while( kMon <ds.length && kHist < ds.length)
      {
         boolean found= false;//found a non monitor dataset
         int k1Mon;
        
         int nDataSets = 0;
         int nMonitors = 0;
         for( k1Mon=kMon; k1Mon < ds.length && !found; )
            if( ds[k1Mon]!= null)
            {
               String S =(String) ds[k1Mon].getAttributeValue( Attribute.DS_TYPE );
               if(! S.equals( Attribute.MONITOR_DATA))
                  found = true;
               else
                  {
                  nMonitors ++;
                  k1Mon++;
                  }
            }
         Monit = new DataSet[nMonitors];
         int k=0;
         found = false;
         for( k1Mon=kMon; k1Mon < ds.length && !found; )
            if( ds[k1Mon]!= null)
            {
               String S =(String) ds[k1Mon].getAttributeValue( Attribute.DS_TYPE );
               if(! S.equals( Attribute.MONITOR_DATA))
                  found = true;
               else 
                  {
                  Monit[k++]= ds[k1Mon];
                  k1Mon++;
                  }
            }
         
         found = false;
         int k1Hist;
         for(k1Hist=k1Mon; k1Hist < ds.length && !found; )
            if( ds[k1Hist]!= null)
            {
               String S =(String) ds[k1Mon].getAttributeValue( Attribute.DS_TYPE );
               if( S.equals( Attribute.MONITOR_DATA))
                  found = true;
               else
               {
                  nDataSets++;
                  k1Hist ++;
               }
            }
         Hist= new DataSet[nDataSets];
         found = false;
         k=0;
         for(k1Hist=k1Mon; k1Hist < ds.length && !found; )
            if( ds[k1Hist]!= null)
            {
               String S =(String) ds[k1Mon].getAttributeValue( Attribute.DS_TYPE );
               if( S.equals( Attribute.MONITOR_DATA))
                  found = true;
               else 
                  {
                  Hist[k++]= ds[k1Hist];
                  k1Hist ++;
                  }
            }
         Writer.Append( Monit , Hist );
         if( Writer.getErrorMessage()!= null && Writer.getErrorMessage().length()>0)
         {
            errorMessage = Writer.getErrorMessage();
            return;
         }
         kMon = k1Hist+1;
         kHist = kMon;
      }
      Writer.close();

      String message = nwr.getErrorMessage();
      if( message != null )
          if(! message.equals(""))
            {

             errorMessage +="Could not Create NeXus File:"+message;
             System.out.println("Could not Create File " + message);
            }
     
      
    }
 
    
 
    /**
       * Saves a set of datasets to a new NXentry in
       * a Nexus file
       * @param filename  The name of a new file to save. It should have 
       *                  an extension of .nxs(for NeXus), .xml.or .zip for
       *                  zipped xml. Currently .xml does NOT use the new Nexus
       *                  xml writer.
       * 
       * @param DatSets   the Vector of monitor and sample data sets. The
       *                 order should follow the convention for writeDataSets.
       *                 Several NXentries will be written if there is more 
       *                 than one group of monitors after the first data set
       *                 in the list.
       * @param append    if true, the information will be appended to the
       *                  existing file if there is one, otherwise the 
       *                  existing file will be deleted.
       * @return        "Success" or an ErrorString with all the errors.
       *                
       */
   public static Object SaveDataSets( String filename , 
            Vector DatSets, boolean append )
   {
      
      int n=0;
      if( !append)
      {
         File f = new File(filename);
         if(f.exists())
            try
         {
               if(!f.delete())
                  return new ErrorString("could not delete file");
         }catch( Exception ss)
         {
            return new ErrorString("could not delete file");
         }
         
      }
      
      if( DatSets != null)
         n += DatSets.size();
      if( n <=0)
         return new ErrorString("No DataSets to Save");
      DataSet[] DSS = new DataSet[n ];
      int k=0;
      try{
     
      if( DatSets != null)
         for( int i=0; i< DatSets.size(); i++)
            DSS[k++]= (DataSet)DatSets.elementAt( i );
      Writer nxWr = null;
      if( filename.toUpperCase().endsWith( ".NXS" ))
               nxWr = new NexWriter( filename);
      else if( filename.toUpperCase().endsWith( ".XML" ))
               nxWr = new XmlDWriter( filename);
      else if( filename.toUpperCase().endsWith( ".ZIP" ))
               nxWr = new XmlDWriter( filename);
      else
         return new ErrorString("Improper extension for filename");
      
      nxWr.writeDataSets( DSS );
      if( nxWr.getErrorMessage() != null && nxWr.getErrorMessage().length()>0)
         return new ErrorString( nxWr.getErrorMessage());
      
      }catch( Exception s)
      {
         return new ErrorString( s.getMessage());
      }
      return "Success";
   }


   public static void main( String args[] )
   {

      String filename = "C:\\ISAW\\SampleRuns\\scd06496";
      DataSet DS[];
      DS = ( new IsawGUI.Util() ).loadRunfile( filename + ".run" );
      
      Vector DSS = new Vector();
      DSS.addElement(  DS[0] );
      for( int i=1;i< DS.length; i++)
         DSS.addElement(  DS[i] );
      DataSet D = (DataSet)DS[1].clone();
      D.setTitle( "x1");
      DSS.addElement( D )
      ;D = (DataSet)DS[1].clone();
      D.setTitle( "x2");
      DSS.addElement( D );
      D = (DataSet)DS[1].clone();
      D.setTitle( "x3");
      DSS.addElement( D );
     System.out.println("Result="+ NexWriter.SaveDataSets( filename+"B.nxs", DSS,false));
      //NexWriter W = new NexWriter( filename + ".nxs" );
     // W.writeDataSets( DS );


   }
}
