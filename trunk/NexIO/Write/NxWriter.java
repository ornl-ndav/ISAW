/*
 * File:  NxWriter.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2001/08/17 19:01:04  rmikk
 * Added error checking in case the file could not be created
 *
 * Revision 1.2  2001/07/30 20:11:42  rmikk
 * No longer implements Writer
 *
 * Revision 1.1  2001/07/25 21:23:20  rmikk
 * Initial checkin
 *
*/
package NexIO.Write;

import NexIO.Write.*;
import NexIO.Write.NexApi.*;
import NexIO.*;
import java.lang.*;
import DataSetTools.dataset.*;
import NexIO.NexApi.*;
import IsawGUI.*;
/** Writes Nexus formatted files from DataSets
 */
public class NxWriter 
{String errormessage; 
 NxWriteNode node;
 NxNodeUtils nn;
 NxData_Gen nd;

/** 
*@param node  The Root node of the file to write
*/ 
 public NxWriter( NxWriteNode node)
   {this.node = node;
    errormessage= "";
    if( node == null)
	errormessage = NxNodeUtils.ER_BADFILE;
    nn= new NxNodeUtils();
    nd = new NxData_Gen();    
    //get Global Attributes here and append to 
   } 
 
/** Returns an errormessage or "" if none
*/ 
public String getErrorMessage()
  { 
   return errormessage;
  }

/**Gets the number of hisograms in this file so far
* Needed when appending files
*/
public int getNumHistograms()
  {int res = 0;
   return node.getNumClasses( "NXentry" );
   }


/** Appends the histograms with their monitors to a nexus file
*@param   Monitors   The set of monitor datasets associated with All
*                    the Histograms
*@param  Histogram  The set of histograms to be added to a Nexus formatted file.
*NOTE: To Save an experiment, call the Append function twice
*/
 public void Append( DataSet[] Monitors , DataSet[] Histogram)
    {int n;
    String runTitle = null;
    Object run_title = null;
  /*  if( Histogram.length > 0)
      {run_title = Histogram[0].getAttributeValue(
                                 Attribute.RUN_TITLE );
      }
    else if( Monitors.length > 0 )
           run_title = Monitors[0].getAttributeValue(
                                 Attribute.RUN_TITLE );
      
    runTitle = nd.cnvertoString( run_title );
    
    if( runTitle != null )
     { byte b[];
       b = runTitle.getBytes();
      int rank[];
      rank = new int[1];
      rank[0] = b.length;
      node.addAttribute( "run_title" , b , Types.Char , rank );
     // System.out.println( "added run title =" + runTitle );
     }
    */
    NxWriteMonitor nm = new NxWriteMonitor();
    NxWriteNode n1 , 
                n2;
    if( Histogram == null ) 
         n = 1;
    else 
          n = Histogram.length;
    n1 = n2 = null;
    NxWriteInstrument nw = new NxWriteInstrument();
    int kNxentries = getNumHistograms();
    for( int i = 0; i<n ; i++ )
	{String S;       
	 if( Histogram != null )
           {n1 = node.newChildNode( "Histogram" + 
                                   new Integer( i+kNxentries ).toString() ,
                                 "NXentry" );
            NxWriteNode nwNode = n1.newChildNode( "instrument" , 
                                                   "NXinstrument" );
            if( nwNode == null )
	      {errormessage = n1.getErrorMessage();	      
               return;
              }
            if( nw.processDS( nwNode , Histogram[i]   ) )
              { errormessage += ";" + nw.getErrorMessage();	     
	       return;
              }
            int kk =1;
            if( Monitors != null )
             if( i == 0 )
              for( int j = 0; j<Monitors.length; j++ )
               for( int k = 0; k< Monitors[j].getNum_entries(); k++ )
		 {n2 = n1.newChildNode( "monitor" +kk// new Integer( j + 1 ).
                                                  //toString() + "_" + k ,
                                                   ,"NXmonitor" );
                
                 kk++;
                 if( !nm.processDS( n2 , Monitors[j] , k ) )
                   { n2.setLinkHandle( "MonLink" + ( j ) + "_" + k ); 
		
                   }
                 else errormessage += ";" + nm.getErrorMessage();
              
                }
             else//if not the first
               { for( int j = 0; j<Monitors.length; j++ )
                 for( int k = 0; k < Monitors[j].getNum_entries(); k++ )
                  { n1.addLink( "MonLink" + ( j ) + "_" + k ); 
	           // System.out.println( "in NxWriter-Wrote links" +  j  + "," );
                  }
               }

	 
          NxWriteData nxd = new NxWriteData();
          if( nxd.processDS( n1 ,nwNode , Histogram[i] , true ) )  
	      {errormessage += ";" +  nxd.getErrorMessage();
	      }
         
           
          }
       else if ( Monitors != null )
	 { 
            n1 = node.newChildNode( "Histogram0" ,
                                 "NXentry" );
            NxWriteNode nwNode = node.newChildNode( "instrument" , 
                                                   "NXinstrument" );
            if( nw.processDS( nwNode , Histogram[i]  ) )
                 errormessage += ";" + nw.getErrorMessage();
            int kk = 1 ;
            for( int j = 0; j<Monitors.length; j++ )
	      for( int k = 0; k< Monitors[j].getNum_entries(); k++ )
                 {n2 = n1.newChildNode( "monitor" + kk,//( j + 1 ) + "_" + k ,
                                    "NXmonitor" );
                  nm = new NxWriteMonitor();
              
                  if( nm.processDS( n2 , Monitors[j] , k ) )
                     errormessage += ";" + nm.getErrorMessage();

                 }
          }
      
      

       NxWriteEntry ne = new NxWriteEntry();  
       if( ne.processDS( n1 , Histogram[i] ) )
         errormessage +=  ";" + ne.getErrorMessage();

       NxWriteSample ns = new NxWriteSample();
       if( ns.processDS( n1 , Histogram[i] ) )
          errormessage +=  ";" + ns.getErrorMessage();

       NxWriteBeam nb = new NxWriteBeam();
       if( nb.processDS( n1 , Histogram[i] ) )
          errormessage += ";" + nb.getErrorMessage();
      
        }//For each histogram
    //     (( NexWriteNode )node ).show();
    }

/** Append the Monitor/DataSet pair to the Nexus formatted file
*NOTE: one or both arguments can be null
*/
 public void Append( DataSet Monitor , DataSet Histogram )
    {DataSet DS[], Mon[];
     if( Monitor == null)
         Mon = null;
     else
        {Mon = new DataSet[1];
         Mon[0] = Monitor;
        }

     if( Histogram == null)
         DS = null;
     else
        {DS = new DataSet[1];
         DS[0] = Histogram;
        }
      Append( Mon, DS );
 
    }
  
/** Closes the file: The file may not be written if this is not called
*/ 
 public void close()
    {node.write();
     errormessage = node.getErrorMessage();
     node.close();
    }
/** Test program for this NxWriter module
*@param  args[0]  The filename sans extension
*@result a new file with an extension .nxs will be created from filename.run
*/
public static void main( String args[] )
 {DataSet DSS[] , 
          DSH[] , 
          DSM[];
  Util UT = new Util();
  int i;
  String filename = "C:\\SampleRuns\\gppd9898"; 
  int n;
  if( args == null)
      n = 1;
  else if( args.length <= 1)
      n = 1;
  else
      n= args.length;
  if(args != null) if( args.length > 0)
     filename = args[0];
  
  NexWriteNode nwr = new NexWriteNode( filename  + ".nxs" );
  // XmlWriteNode nwr = new XmlWriteNode( filename + ".xml" );
   if( nwr.getErrorMessage() != "")
       System.out.println( "Error 1="+nwr.getErrorMessage());
 //  if( nwr instanceof NexIO.Write.NxWriteNode)
  //     System.out.print("is instance of");
   System.out.println(nwr.getClass() +":"+nwr.getClass().getSuperclass()+":");
   System.out.print("interfaces:");
   Class x[];
   x= nwr.getClass().getInterfaces();
   if( x!= null)
   for( i=0; i<  x.length; i++)
     System.out.print( x[i]+":: ");
   System.out.println("");
   NexIO.Write.NxWriteNode nwrx =  (NexIO.Write.NxWriteNode)nwr;
   NxWriter Writer = new NxWriter( nwrx  );
 //Has one monitor and one histogram
  for(  i = 0; i < n ; i++ )
    {DSS = UT.loadRunfile( filename + ".run" );
 
   
    DSM = new DataSet[1];
    DSH = new DataSet[ DSS.length - 1 ];
    DSM[0] = DSS[0];
    for( int k = 1; k < DSS.length; k++ )
      DSH[k-1] = DSS[k];
    Writer.Append( DSM , DSH );
  
   
    System.out.println( " Error =" + Writer.getErrorMessage() ) ; 
     if( i+1 < n)
       filename = args [ i+1 ];

  }
   nwrx.show();
   Writer.close();
  } 

}

