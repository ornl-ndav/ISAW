
/*
 * File:  NxDataStateInfo.java
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
 * Revision 1.8  2006/11/14 16:38:24  rmikk
 * Fixed Translate axis names by adding 1
 * Search the xml fixit doc to link name
 * Search for filename through xml fixit doc
 *
 * Revision 1.7  2006/07/27 18:32:12  rmikk
 * Added code to translate incorrect axes numbers to correct axes numbers.
 * Added more spacing around in the code
 *
 * Revision 1.6  2006/07/25 00:05:57  rmikk
 * Added code to update fields in a FixIt xml file in the same directory as
 * the NeXus file
 *
 * Revision 1.5  2005/12/29 23:04:11  rmikk
 * Now checks for the link attribute in every SDS child of an NXdata node
 *
 * Revision 1.4  2005/06/04 20:09:51  rmikk
 * Fixes the problem with the dimensions of NXdata.data that Fortran
 * programmers get.  This works only with the newer NeXus files( the
 * ones with links)
 *
 * Revision 1.3  2004/12/23 12:53:03  rmikk
 * updated to NeXus standard v 1.0.  Uses detector_number instead of id 
 *   and looks for the axes attribute to determine axes.
 *
 * Revision 1.2  2003/12/08 17:28:23  rmikk
 * Eliminated a debu print
 *
 * Revision 1.1  2003/11/16 21:42:25  rmikk
 * Initial Checkin
 *
 */

package NexIO.State;
import NexIO.*;
import NexIO.Util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 *   This class contains state information needed to process an NXdata entry
 *   of a NeXus file
 */ 
public class NxDataStateInfo extends StateInfo{
  /**
   *   Contains the names of the axes with axis attributes 1 to 4.
   *   This field is initialized to an array of 4 null strings
  */
  public String[] axisName; 
  public int[] XlateAxes; //if used incorrect conventions this translates
                         //from bad name to correct name

  /**
   *   The dimensions of the data entry as given by the NeXus file.
   *   The first element of the array is the number of values associated 
   *   with the slowest changing attribute.  The last item is the number
   *   of time_of_flight data( +1 if it is a histogram)
   */
  public int[] dimensions;

  /**
   *   The name of the NXdata node
   */
  public String Name;

  /**
   *   The value of the label attribute on the data field of this NXdata. If 
   *   this is not null, this NXdata node will be merged with all other NXdata
   *   nodes in an NXentry with the same label attribute on their data field.
   */
  public String labelName;

  /**
   *   Determines whether the Datablock ID's are determined by the NXdetector.id
   *   or NXdetector.detector_number if present or by the default GroupID. 
   */
  public boolean hasIntIDField;

  /**
   *   The starting Default GroupID if there is no id or detector_number field.
   */
  public int startGroupID;

  /**
   *   The name of the corresponding NXdetector for this NXdata node. This
   *   comes from a link attribute on one of the axes.
   */
  public String linkName;

  /** 
   *   Constructor for the NxDataStatInfo.
   *   NOTE: Fields can be added to this structure, Other methods and/or
   *         constructors can be introduced, or subclassing can be used to
   *         account for variabilities
   *   @param  NxDataNode  An NxNode containing information on a NeXus NXdata 
   *                       class
   *   @param  NxInstrumentNode An NxNode containing information on a NeXus  
   *                       NXinstrument class
   *   @param  Params   a linked list of State information
   *   @param startGroupID the default starting ID for the data blocks. This is
   *                       used if there is NO int id field in the corresponding
   *                       NXdetector 
   */
  public NxDataStateInfo( NxNode NxDataNode , NxNode NxInstrumentNode , 
                NxfileStateInfo Params , int startGroupID ){
                  
     Name = NxDataNode.getNodeName();
     axisName = new String[ 4 ];
     axisName[ 0 ] = axisName[ 1 ]= axisName[ 2 ] = axisName[ 3 ] = null;
     labelName = null;
     dimensions = null;
     hasIntIDField = false;
     this.startGroupID = startGroupID;
     int xvals_length = -1;
     
     //Find axes
     for( int i = 0 ; i < NxDataNode.getNChildNodes() ; i++ ){
       
        NxNode child = NxDataNode.getChildNode( i );
        
        if( child.getNodeClass().equals( "SDS" )){
           int[] l = child.getDimension();
           int axNum = ConvertDataTypes.intValue( child.getAttrValue( "axis" ) );
           if( ( axNum >= 1 ) &&( axNum < 4 ) )
              if( axisName[ axNum - 1 ] == null )
                   axisName[ axNum - 1 ] = child.getNodeName();
           if(( axNum == 1 )&&( l != null )&&( l.length == 1 ))
               xvals_length = l[ 0 ];
        
          if( child.getNodeName().equals( "data" ) ){
             
            dimensions = child.getDimension();
           
            labelName = ConvertDataTypes.StringValue( child.getAttrValue( "label" ) );
            Object O = child.getAttrValue( "axes" );
            if( O instanceof String[] ){
               String S = ( String )O;
               S = S.trim();
               if( S.startsWith( "[" ) ) S = S.substring( 0 );
               if( S.endsWith( "]" ) ) S = S.substring( 0 , S.length() - 1 );
               String[] SS = S.trim().split( ":," );    
               for( int j = 0 ; j < SS.length ; j++ )
                   axisName[ j ] = SS[ SS.length - j ];
            }
              
          }else if( child.getNodeName().equals( "id" ) || child.getNodeName().
            equals( "detector_number" ) ){
                 
            Object O = child.getNodeValue();
            if( O != null )
            if( O instanceof int[] ){                 
               hasIntIDField = true;
            }
                    
         }//Node Class is SDS   
          
         //Find links for NXdata name
         String L = ConvertDataTypes.StringValue( child.getAttrValue( "link" ) ); 
         if( L == null )
            L = ConvertDataTypes.StringValue( child.getAttrValue( "target" ) );
          
         if( L != null ){
             linkName = FixUp( L , child );
         }
        }//child is SDS  
        
     } //for loop
     
     XlateAxes = FindAxes( Params.xmlDoc );  
     if( XlateAxes == null){
        XlateAxes = new int[dimensions.length];
        for( int i = 0 ; i< XlateAxes.length ; i++)
           XlateAxes[i]= i+1;
     }
     if( axisName.length > dimensions.length){
        String[] A = new String[ dimensions.length];
        System.arraycopy( axisName,0,A,0,A.length);
        axisName = A;
     }
     //Check xml for link name
     if( Params.xmlDoc != null){
        NxEntryStateInfo EntryState = NexUtils.getEntryStateInfo( Params );
        String name = null;
        if( EntryState != null)
           name = EntryState.Name;
        Node N = Util.getNXInfo( Params.xmlDoc,"Common.NXentry",null,null,null);
        Node NN1[] = NexIO.Util.NexUtils_mixDims.getNxEntryNodes( N,name );
        N = Util.getNXInfo( Params.xmlDoc,"Runs",null,null,null);
        String filename = Params.filename;
        Node NN2[]= new Node[2]; NN2[0]=NN2[1] = null;
        if( filename != null){
           filename = filename.replace('\\','/');
           int kk= filename.lastIndexOf('/');
           if( kk >=0)
              filename = filename.substring(kk+1);
        
           N = Util.getNXInfo( N,"Run",null,null,filename);
           NN2 = NexIO.Util.NexUtils_mixDims.getNxEntryNodes( N,name );
        }
        Node NNN[] = new Node[4];
        NNN[0]=NN1[0]; NNN[1]=NN1[1];NNN[2]=NN2[0]; NNN[3]=NN2[1];
        for( int i=0; i< 4; i++){
           if( NNN[i] != null){
              N=Util.getNXInfo( NNN[i],"NXdata.link",Name,null,null);
              if( N != null){
                 String S = ConvertDataTypes.StringValue( Util.getLeafNodeValues( N));
                 if( S != null)
                    linkName = S;
              }
           }
        }
     }
     //NexIO.Util.NexUtils.disFortranDimension( dimensions , xvals_length );
      
  }
  
  
  
   //Eliminates all but the trailing path part, unless it matches this nodes
  // name, the return the previous path section.  Paths can be separated by
  // ,./ or \
  // NOTE: Version 2 requires pointing to an SDS field under a class
   private String FixUp( String linkName , NxNode node ){
      
      if( node == null )
         return null;
      
      String Name = node.getNodeName().trim();
      
      if( linkName != null )if( linkName.length() > 0 ){
         
         if( linkName.endsWith( "/" ) || linkName.endsWith( "\\" ) || linkName.endsWith( "." ) )
            linkName = linkName.substring( 0 , linkName.length() - 1 );
         
         if( linkName.endsWith( "\\" + Name ) )
            linkName = linkName.substring( 0 , linkName.length() - Name.length() - 1);

         else if( linkName.endsWith( "/" + Name ) )
            linkName = linkName.substring( 0 , linkName.length() - Name.length() - 1 );
         
         else if( linkName.endsWith( "." + Name ) )
            linkName = linkName.substring( 0 , linkName.length() - Name.length() - 1 );
         
         //Now find last part of the path
         int k = linkName.lastIndexOf( "/" );
         int k1 = linkName.lastIndexOf( "\\" );
         int k2 = linkName.lastIndexOf( "." );
         if( k < 0 )
            k = k1;
         if( k1 >= 0 )
            if( k < 0 ) 
               k = k1;
            if( k < k1 ) 
               k = k1;
         if( k2 > 0 )
            if( k < 0 ) 
               k = k2;
            else if( k < k2 ) 
               k = k2;
          if( k >= 0 ) 
             if( k + 1 < linkName.length() )
                 linkName = linkName.substring( k + 1 );
         
       }
          
      return linkName;  

   }
   
   
   /**
    *   Finds the axes from an XML file.  All it does is search for
    *   <axes  , gets the string value and converts it to an array of
    *   integers.
    * @param xmlDoc  the xml parsed document in which to search for <axes
    * @return   The value of the tag( if tail) returned as an int array
    * 
    */
   public int[] FindAxes( Node xmlDoc ){
      
      Node res = Util.getNXInfo( xmlDoc , "NXdata.axes" , Name , null ,null );
      
      if( res == null )
         return null;
      
      String S = ConvertDataTypes.StringValue( Util.getLeafNodeValues( res ));
      if( S == null )
         return null;
      
      return ConvertDataTypes.intArrayValue( S.split( "[ ]+" ) );
   }
  
}


