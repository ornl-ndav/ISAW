/*
 * File:  Process1NxData.java
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.8  2007/07/11 18:06:40  rmikk
 * Introduced Methods to more fully search the xml fixit file for fields of interest
 *
 * Revision 1.7  2007/01/12 14:48:46  dennis
 * Removed unused imports.
 *
 * Revision 1.6  2006/11/14 16:48:02  rmikk
 * Takes care of the out of order tuples by directiing processing to the class
 *    that takes care of these
 *
 * Revision 1.5  2006/07/25 00:10:34  rmikk
 * Added code to include a missing or ne NXdetector.layout field
 * Created new Info variables if they were not in the list.
 *
 * Revision 1.4  2004/12/23 15:50:13  rmikk
 * Added extra spacing between lines
 *
 * Revision 1.3  2004/12/23 13:15:22  rmikk
 * Added the NxEntryInfo to the States list so that the version number
 *   is available.
 * Popped the added StateInfo from the State list
 *
 * Revision 1.2  2003/12/09 14:40:18  rmikk
 * Fixed javadoc warnings
 *
 * Revision 1.1  2003/11/16 21:39:38  rmikk
 * Initial Checkin
 *
 */

package NexIO.Process;

import NexIO.*;
import NexIO.State.*;
import DataSetTools.dataset.*;
import NexIO.Util.*;
// import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * This class processes ONE NXdata class in a NeXus file, updating a DataSet.
 * This is the generic Process1NxData. It assumes that the NXdetector is
 * associated with the NXdata is via a link attribute on one of its axes. The
 * names of the axes are not used( they are determined by their axis
 * attributes). The associated NXdetector is processed with this classes
 * processDS method. Currently only a generic processing for NXdetector is used.
 * This processes multidimensional arrays and puts them into uniform grids( may
 * change).
 */

public class Process1NxData implements IProcessNxData {

   String           errormessage = "";
   public INexUtils nxut         = new NexUtils();


   /**
    * @return an errormessage or an empty string if there is no error
    */
   public String getErrorMessage() {

      return errormessage;
   }


   private boolean setErrorMessage( String message ) {

      errormessage = message;
      return true;
   }


   /**
    * Method that fills out the DataSet DS from information in the NXdata node.
    * 
    * @param NxEntryNode
    *           An NxNode with information on the NeXus NXentry class.
    * @param NxDataNode
    *           An NxNode with information on the NeXus NXdata class.
    * @param NxInstrument
    *           An NxNode with information on the NeXus NXinstrument class.
    * @param DS
    *           The DataSet(not null) that is to have info added to it
    * @param States
    *           The linked list of state information
    * @param startGroupID
    *           The starting Group ID for the NEW data blocks that are added
    */
   public boolean processDS( NxNode NxEntryNode , NxNode NxDataNode ,
            NxNode NxInstrument , DataSet DS , NxfileStateInfo States ,
            int startGroupID ) {

      int firstIndex = DS.getNum_entries();
      int npush = 0;
      
      NxDataStateInfo DataState = NexIO.Util.NexUtils.getDataStateInfo( States );
     
    
      NxEntryStateInfo EntryState = NexIO.Util.NexUtils
               .getEntryStateInfo( States );
      if( EntryState == null ) {
         EntryState = new NxEntryStateInfo( NxEntryNode , States, null, 
                  null, null, null );
         States.Push( EntryState );
         npush++ ;
      }

      Node xmldoc = States.xmlDoc;
      int timeDim = 0 , coldim = 1 , rowdim = 2;
      boolean dimChange = false;
      if( DataState != null )
         if( DataState.dimensions.length <= 1 ) {
            coldim = rowdim = - 1;
            if( xmldoc != null ) dimChange = true;
         }
         else if( xmldoc != null ) {

            String fname = null;
            fname = States.filename;

            int timeDim1 = getXMLIntVal( xmldoc , NxEntryNode.getNodeName() ,
                     new String[]{ "time_dimension" } , null , fname ,
                     true , null );
            
            int rowDim1 = getXMLIntVal( xmldoc , NxEntryNode.getNodeName() ,
                     new String[]{ "row_dimension"} , null , fname , 
                     true , null );
            
            int colDim1 = getXMLIntVal( xmldoc , NxEntryNode.getNodeName() ,
                     new String[]{ "col_dimension" } , null , fname , 
                     true , null );
            
            dimChange = false;
            
            if( timeDim1 >= 0 ) {
               if( timeDim1 != timeDim ) 
                   dimChange = true;
               timeDim = timeDim1;
            }
            
            if( rowDim1 >= 0 ) {
               if( rowDim1 != rowdim ) 
                  dimChange = true;
               rowdim = rowDim1;
            }
            
            if( colDim1 >= 0 ) {
               if( colDim1 != coldim ) 
                  dimChange = true;
               coldim = colDim1;
            }

         }
      int x = Math.max( Math.max( rowdim , coldim ) , timeDim ) + 1;
      if( DataState != null ) if( x >= DataState.dimensions.length ) x = - 1;
      
      if( dimChange )
         nxut = new NexUtils_mixDims( timeDim , rowdim , coldim , x , 1 , 2 ,
                  3 , 4 , States );

      if( DataState == null ) {
         DataState = new NxDataStateInfo( NxDataNode , NxInstrument , States ,
                  startGroupID );
         npush++ ;
         States.Push( DataState );
      }

      NxInstrumentStateInfo InstrumentState = new NxInstrumentStateInfo(
               NxInstrument , States );

      States.Push( InstrumentState );


      NxNode NxDetectorNode = NexUtils.getCorrespondingNxDetector(
               DataState.linkName , NxInstrument );

      NxDetectorStateInfo DetState = null;
      DetState = NexUtils.getDetectorStateInfo( States );
      if( NxDetectorNode == null ) {
         DataSetTools.util.SharedData.addmsg( "no NxDetector Node for "
                  + NxDataNode.getNodeName() );

      }
      else if( DetState == null ) {
         DetState = new NxDetectorStateInfo( NxDetectorNode , States );
         States.Push( DetState );
         npush++ ;
      }
      
      if( States.xmlDoc != null ) {
         Node xx = Util.getNXInfo( States.xmlDoc , "layout" , null , null ,
                  null );
         if( xx != null ) DetState.hasLayout = xx.getNodeValue().toString();
      }

      boolean res = nxut.setUpNxData( DS , NxDataNode , startGroupID ,
               States );
      if( res ) {

         for( int i = 0 ; i < npush ; i++ )
            States.Pop();
         return setErrorMessage( nxut.getErrorMessage() );
      }

      if( ( NxDetectorNode != null ) || ( States.xmlDoc != null ) )
         res = nxut.setUpNXdetectorAttributes( DS , NxDataNode ,
                  NxDetectorNode , firstIndex , States );

      for( int i = 0 ; i < npush ; i++ )
         States.Pop();

      if( res ) return setErrorMessage( nxut.getErrorMessage() );

      return false;
   }


   /**
    * Hook to add new fields to the class that are not in the processDS methods
    * parameters
    */
   public void setNewInfo( String Name , Object value ) {

   }


   /**
    * @return the current value of the new info associated with Name
    */
   public Object getNewInfo( String Name ) {

      return null;
   }


   /**
    * Will Search the xml document( or part) for the Node
    * 
    * @param xmlDoc
    *           The node that is being searched
    * @param NXentryName
    *           the Name of the NXentry xml node, could be null for any NXentry
    * @param FieldClass
    *           The classes(<___) of the field in the NXentry Node later
    *           entries are subclasses of previous FieldClass
    * 
    * @param FieldClassName
    *           the corresponding name attributes( could be "" or null)
    * 
    * @param filename
    *           The filename attribute or null. Additional searches will be done
    *           with the filename == null.
    * 
    * @param SearchNoNameNXentry
    *           Does an additional search on NXentries without a name.
    * @param SearchNoNamesubFields
    *           Does additional searches on NXdata's without names
    * 
    * @return the integer value of the given field or Integer.MIN_VALUE if not
    *         found
    * 
    * ALGORITHM: The Run fields will be searched first with the filename
    * attribute. then the Common fields In each The full path with names will be
    * searched, then entries with nonames NXentries if searchNoNameNXentry, then
    * NXentries with names will be searched for fields without names if
    * specified in SearchNoNamesubFields. Finally all the nonamed fields will be
    * searched
    */
   public static int getXMLIntVal( Node xmlDoc , String NXentryName ,
            String[] FieldClass , String[] FieldClassName , String filename ,
            boolean SearchNoNameNXentry , boolean[] SearchNoNamesubFields ) {

      Node Res = Util.getXMLNodeVal( xmlDoc , NXentryName , FieldClass ,
               FieldClassName , filename , SearchNoNameNXentry ,
               SearchNoNamesubFields );

      if( Res == null ) return Integer.MIN_VALUE;

      return ConvertDataTypes.intValue( Util.getLeafNodeValues( Res ) );


   }

}
