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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.10  2003/10/20 16:41:53  rmikk
 * Fixed javadoc error
 *
 * Revision 1.9  2003/09/11 18:33:45  rmikk
 * Caught some errors in the underlying jnexus system
 *
 * Revision 1.8  2002/11/27 23:23:16  pfpeterson
 * standardized header
 *
 * Revision 1.7  2002/07/29 18:47:24  rmikk
 * Added code to report errors to status pane and, if file
 *   cannot be opened to System.out.
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
         {errormessage = node.getErrorMessage();
	  DataSetTools.util.SharedData.addmsg("Cannot Read File: "+errormessage);
          ext = null;
          System.out.println( errormessage );
          return;
         }
     ext = new ExtGetDS( node, dataSourceName ) ;
     errormessage = ext.getErrorMessage();
     if( errormessage != null)
       if( errormessage.length() > 0)
          ext = null;
    }

    /** 
    *@param data_set_num  the data set that is to be retrieved
    *@return  the data set if the data_set_num is valid or null
    *
    *@see #getErrorMessage()
    */
  public DataSet getDataSet( int data_set_num ) 
    {if( ext == null )
        return null;
     DataSet DS = ext.getDataSet( data_set_num ) ;
     errormessage = ext.getErrorMessage() ;
     return DS ;
    }               
  
  /**
  * @param data_set_num the data set index
  * @return the type( Histogram/monitor/invalid/etc.) of the data 
  *
  *@see DataSetTools.retriever.dataSource

  */ 
   public   int getType( int data_set_num ) 
     {if( ext == null)
         return -1;
      int type = ext.getType( data_set_num ) ;
      errormessage = ext.getErrorMessage() ;
      return type ;
     }           
  
  /**
  * returns the total number of datasets of All Types
  */
   public   int  numDataSets() 
      {if( ext == null)
         return 0;
      if( errormessage !=null)
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

