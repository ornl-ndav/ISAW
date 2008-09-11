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
 * Revision 1.12  2004/03/09 17:55:56  rmikk
 * Fixed javadoc error(I hope)
 *
 * Revision 1.11  2003/12/15 00:52:50  rmikk
 * Implements the hasInformation interface so more information on the datasets
 * in a file can be returned before retrieving the whole data set
 *
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
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Numeric.*;
import java.util.*;

/**  Class used to Retrieve local nexus files
 */
public class NexusRetriever extends Retriever implements hasInformation
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
     node = ( new NexIO.NexApi.NexNode(  dataSourceName  ) ) ;
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
     * Saves the information from this NeXus file about each data set. This
     * includes information to access relevant information where the information
     * about this data set is stored in the NeXus file
     * 
     * @param filename  the name of the file to get the information or null
     *                  if it goes to the standard place for the instrument
     *                  
     * @return  true if successful otherwise false
     */
    public boolean SaveSetUpInfo( String filename ){
       return ext.SaveStartUpInfo( filename );
    }
    
    
    /**
     * Uses the information stored in SaveSetUpInf() to initialize the information
     * on the data sets stored in this file.
     * 
     * @param filename  the name of the file to with the information or null
     *                  if it uses the standard file for the instrument
     * 
     */
    public void RetrieveSetUpInfo( String filename ){
       ext.RestoreStartUpInfo( filename);
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
   * Tries to close the file in the underlying NeXus system
   */
  public void close(){
     if( node != null)
        node.close();
     node = null;
     ext = null;
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
     *  Returns info in the form for selecting this data set
     * @param  data_set_num  the number of the data set
     * @return an array of Strings where the first is the data set name,
     *         the second is the data set type( monitor,sample) and the
     *         third is the string description of a range of default GroupID'x
     *         for this data set.
     * @see #getType(int )
     */
   public String[] getDataSetInfo( int data_set_num){
      if( ext == null)
         return new String[0];
      return ext.getDataSetInfo( data_set_num);
   }


   
  /**
  * Returns any errormessages or "" if there are no errors or warnings
  */
   public String getErrorMessage()
    {return errormessage ;
    }
 
   
   /**
    * Retrieves datasets from one NeXus file with options to use caching for 
    * faster loading of the data
    * 
    * @param filename          The name of the nexus file with the data sets
    * @param DsNums            The data set numbers to be loaded or empty to 
    *                              load all
    * @param GroupIDs          The group IDs to be loaded or Empty for all.
    * @param useDefaultCache   Use the default cache for this instrument. This
    *                          file is located in user.home/ISAW. Its name
    *                          starts with the first 3 letters of the instrument
    *                          name( first 3 characters of the filename). It ends
    *                          with \".startup\".
    *               
    * @param CacheFilename    The name of the filename with the cache information
    * 
    * @return   The array of data sets from this NeXus file.
    */
   public static DataSet[]  LoadNeXusDataSetsFast( String filename, 
                                                IntListString DsNums, 
                                                IntListString GroupIDs, 
                                                boolean useDefaultCache, 
                                                String CacheFilename){
      
      
         NexusRetriever retriever = new NexusRetriever( filename);
         
         if( useDefaultCache)
            retriever.RetrieveSetUpInfo( null );
         else if(CacheFilename != null && CacheFilename.trim().length()>1)
            retriever.RetrieveSetUpInfo(  CacheFilename );
      
         if( DsNums == null || DsNums.toString().trim().length() <1 )
            DsNums = new IntListString("0:"+ (retriever.numDataSets()-1));
         
         int[] dsList = IntList.ToArray(  DsNums.toString() );
         
         int[] GroupList = null;
         if( GroupIDs != null)
            GroupList = IntList.ToArray( GroupIDs.toString() );
         
         DataSet[] DSS = new DataSet[ dsList.length];
         for( int i=0; i< DSS.length; i++)
            if( GroupList != null)
               DSS[i] = retriever.getDataSet(  dsList[i], GroupList );
            else
               DSS[i] = retriever.getDataSet(  dsList[i]);
         
        retriever.close();
        return DSS; 
        
      
   }
   
   /**
    *  Get the dataset with number dsNum and only keeps those with the given ids.
    *  
    *  @param dsNum    the number associated with the data set to be retrieved
    *  @param ids      the groupIDs to keep.
    *  
    *  @return  the given data set or null if there is no data set or ids specified.
    */
   public DataSet getDataSet( int dsNum, int[] ids){
      DataSet ds = getDataSet( dsNum );
      if( ds == null || ds.getNum_entries() < 1 || ids ==null || 
                                                 ids.length < 1)
         return ds;
     
      for( int i= ds.getNum_entries() - 1 ; i >= 0 ; i-- )
         if( Arrays.binarySearch( ids, ds.getData_entry(i).getGroup_ID()) < 0 )
            ds.removeData_entry( i );
      
      
      return ds;
         
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

