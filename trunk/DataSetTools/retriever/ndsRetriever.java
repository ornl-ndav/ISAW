/*
 * File:  ndsRetriever.java 
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
 * Revision 1.1  2001/08/01 21:04:43  rmikk
 * Initial Checkin
 *

*/

package DataSetTools.retriever ;
import DataSetTools.dataset.* ;
import NexIO.*;
import NexIO.NDS.*;

/**  Class used to Retrieve local nexus files
 */
public class ndsRetriever extends Retriever
{ExtGetDS ext ;
 String errormessage ;
 NxNode node ;

    /**
     *@param   dataSourceName  should be a local filename<P>
     * If the file exists this class will append to this file
     */
    public ndsRetriever(  String dataSourceName )
    {super(  dataSourceName ) ;
     errormessage = "" ;
     dataSource DT = new dataSource( dataSourceName);
     if( !DT.check())
	 return;
      NDSClient nds = new NDSClient( DT.getMachine() , 
                        new Integer(DT.getPort()).intValue() , 6081998 ) ;
      nds.connect() ;
      node = ( NxNode )( new NdsSvNode( DT.getFileName() , nds ) ) ;
     
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
  *@see DataSetTools.retriever.Retriever.data_source_name

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
      {int nsets = ext.numDataSets() ;
       errormessage = ext.getErrorMessage() ;
       return nsets ;
      }

  /**
  * Returns any errormessages or "" if there are no errors or warnings
  */
   public String getErrorMessage()
    {return errormessage ;
    }
  /** Test program for the ndsRetriever module. Enter a valid data Source
  * Name.  The files will be saved in Test0.isd, Test1.isd,... 
 */
  public static void main( String args[] )
   {String data_source_name ="dmikk.mscs.uwstout.edu;6008;;;lrcs3000.nxs";
   if( args!= null)
     if( args.length > 0)
         data_source_name= args[0].trim();
    ndsRetriever R = new ndsRetriever(data_source_name  ) ;
    if( R.getErrorMessage() != "" )
      {System.out.println( "Error = " + R.getErrorMessage() ) ;
       System.exit( 0 ) ;
      }

    for( int i = 0 ; i< R.numDataSets() ; i++ )
      {DataSet DS = R.getDataSet( i ) ;
       if( DS == null )
         System.out.println( "DataSet " + i + " is null" ) ;
       else
        DataSet_IO.SaveDataSet( DS, "Test" + i + ".isd" ) ;
       System.out.println( "Error = " + R.getErrorMessage() ) ;


      }


   }            
  
}

