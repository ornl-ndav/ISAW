/*
 * File:  NexusRetriever.java 
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
 * Revision 1.5  2001/08/16 20:31:32  rmikk
 * Fixed the javadocs
 *
 * Revision 1.4  2001/08/10 13:41:11  rmikk
 * Added Code to return error information
 *
 * Revision 1.3  2001/07/26 13:15:40  rmikk
 * New Nexus retriever that uses routines from the general
 * NexIO package.
 *
*/

package DataSetTools.retriever ;
import DataSetTools.dataset.* ;
import NexIO.*;

/**  Class used to Retrieve local nexus files
 */
public class NexusRetriever extends Retriever
{ExtGetDS ext ;
 String errormessage ;
 NxNode node ;

    /**
     *@param   dataSourceName  should be a local filename<P>
     * If the file exists this class will append to this file
     */
    public NexusRetriever(  String dataSourceName )
    {super(  dataSourceName ) ;
     errormessage = "" ;
     node = ( NxNode )( new NexIO.NexApi.NexNode(  dataSourceName  ) ) ;
     if( node.getErrorMessage() != null)
       if( node.getErrorMessage().length() > 0)
         errormessage = node.getErrorMessage();
     ext = new ExtGetDS( node, dataSourceName ) ;
    }

    /** 
    *@param data_set_num  the data set that is to be retrieved
    *@return  the data set if the data_set_num is valid or null
    *
    *@see #getErrorMessage()
    */
  public DataSet getDataSet( int data_set_num ) 
    {DataSet DS = ext.getDataSet( data_set_num ) ;
     errormessage = ext.getErrorMessage() ;
     return DS ;
    }               
  
  /**
  * @param  the data set index
  * @return the type( Histogram/monitor/invalid/etc.) of the data 
  *
  *@see DataSetTools.retriever.dataSource

  */ 
   public   int getType( int data_set_num ) 
     {int type = ext.getType( data_set_num ) ;
      errormessage = ext.getErrorMessage() ;
      return type ;
     }           
  
  /**
  * returns the total number of datasets of All Types
  */
   public   int  numDataSets() 
      {if( errormessage !=null)
        if( errormessage.length() > 0)
          return  RemoteDataRetriever.BAD_FILE_NAME;
       int nsets = ext.numDataSets() ;
       
       errormessage = ext.getErrorMessage() ;
       if( errormessage != null )
         if( errormessage.length() > 0)
           return RemoteDataRetriever.BAD_FILE_NAME;
       return nsets ;
      }

  /**
  * Returns any errormessages or "" if there are no errors or warnings
  */
   public String getErrorMessage()
    {return errormessage ;
    }
  /** Test program for the NexusRetriever module 
 */
  public static void main( String args[] )
   {String filename = "C:\\SampleRuns\\Nex\\lrcs3000.nxs" ;
   if( args!= null)
     if( args.length > 0)
         filename = args[0].trim();
    NexusRetriever R = new NexusRetriever( filename ) ;
    if( R.getErrorMessage() != "" )
      {System.out.println( "Error = " + R.getErrorMessage() ) ;
       System.exit( 0 ) ;
      }

    for( int i = 0 ; i< R.numDataSets() ; i++ )
      {DataSet DS = R.getDataSet( i ) ;
       if( DS == null )
         System.out.println( "DataSet " + i + " is null" ) ;
       else
        DataSet_IO.SaveDataSet( DS, "C:\\Test" + i + ".isd" ) ;
       System.out.println( "Error = " + R.getErrorMessage() ) ;


      }


   }            
  
}

