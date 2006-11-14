/*
 * File:  NxInstrument.java 
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
 * Revision 1.13  2006/11/14 16:51:44  rmikk
 * Added code to check the xml Fixit file for some of the fields
 *
 * Revision 1.12  2004/05/14 15:03:27  rmikk
 * Removed unused variables
 *
 * Revision 1.11  2004/02/16 02:15:55  bouzekc
 * Removed unused import statements.
 *
 * Revision 1.10  2003/11/24 00:02:58  rmikk
 * Eliminated some debugging prints
 * If there is only one NXdetector, it is matched with all NXdata
 *
 * Revision 1.9  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.8  2002/11/20 16:14:47  pfpeterson
 * reformating
 *
 * Revision 1.7  2002/11/20 15:56:28  rmikk
 * -Improved the robustness of the routine to find Detector ID.s
 *
 * Revision 1.6  2002/08/02 13:28:04  rmikk
 * Added a routine to get all the id's from each NxDetector
 *   and save info(NxDetector and position in id list) in a
 *   Hashtable
 *
 * Revision 1.5  2002/06/19 15:06:55  rmikk
 * Trimmed the Instrument name string to eliminate the extra
 * null character at the end.
 * Fixed code alignment and spacing
 *
 * Revision 1.4  2002/04/01 20:27:12  rmikk
 * Got rid of Debug prints.
 * Several error messages now go to the status pane
 *
 * Revision 1.3  2002/02/26 15:44:46  rmikk
 * This module can now be used to load in Monitor data that has been inadvertently labeled NXdata. It does not have two axes
 *
 */

package NexIO;
import DataSetTools.dataset.*;
import NexIO.Util.NexUtils;
import NexIO.State.*;
import NexIO.Util.*;
import java.lang.reflect.*;
import java.util.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;

/**
 * This class is used to process the NXinstrument information in a
 * Nexus data source
 */
public class NxInstrument{
  Hashtable DetectorInf =null;
  int nNxDet;
  NxNode LastNxDetNode=null;
  int[] ids= null;
  String errormessage;

  public NxInstrument(){
    DetectorInf = null;
    errormessage = "";
    nNxDet=0;
    LastNxDetNode=null;
    ids=null;
  }

  /**
   * Sets the ids of the data blocks that are to be loaded
   *
   * NOTE: ids should be increasing and a linear array. 
   * NOTE: if the array is null, ALL will be retrieved.
   */
  public void setIds( int ids[]){
    this.ids=ids;
  }

  /**
   * returns the ids to of the data blocks that are to be retrieved
   */
  public int[] getIds(){
    return ids;
  }

  /**
   * Returns error and/or warning messages or "" if none
   */
  public String getErrorMessage(){
    return errormessage;
  }
   
  /**
   * Sets up a Hashtable of detector id's versus corresponding Vector<BR>
   *     <NXdetector node,<BR>
   *      position of id child in the NXdetector node,
   *      index of the id with the corresponding detector id
   */
  public void SetUpDetectorInfo( NxNode instrNode){
    if(DetectorInf != null)
      return;
    nNxDet=0;
    DetectorInf=new Hashtable();
    LastNxDetNode=null;
    NexIO.NxData_Gen  util= new NexIO.NxData_Gen();
    if( instrNode == null)
      return;

    if( !instrNode.getNodeClass().equals( "NXinstrument"))
      return;

    DetectorInf = new Hashtable();
    for( int i = 0; i<instrNode.getNChildNodes(); i++){
      NxNode nd = instrNode.getChildNode( i );
      if( nd.getNodeClass().equals("NXdetector")){
        for( int j=0; j< nd.getNChildNodes(); j++){
          NxNode ndet_child = nd.getChildNode(j);
              
          boolean use = false;
          if( ndet_child.getNodeName().equals("id"))
            use = true;
          else{
            Object O = ndet_child.getAttrValue( "axis");
            if( O != null){
              int axis = -1;
              if( O instanceof Integer)
                axis = ((Integer)O).intValue();
              else{
                axis = util.cnvertoint(O);
                if( !util.getErrorMessage().equals(""))
                  axis = -1;
              }
              if( axis == 2)
                use = true;
              
            }//if axis attribute != null
          }//else for if child's name is "id"
          if( use){
            float[] ff =
              NXData_util.Arrayfloatconvert(ndet_child.getNodeValue());
            if( ff!= null)
              for( int k=0; k<ff.length; k++){
                try{
                  //det id's not unique
                  if( DetectorInf.containsKey( new Integer((int)ff[k]))){
                    nNxDet++;
                    LastNxDetNode = nd;
                    DetectorInf= new Hashtable();
                    return;
                  }
                  Vector V = new Vector();
                  V.addElement( nd);
                  V.addElement( new Integer( j ));
                  V.addElement(new Integer(k));
                  DetectorInf.put( new Integer( (int)ff[k]), V);
                }catch( Exception u){
                  // let it drop on the floor
                }
              }
          }//if use
        }//for  @ index of a detector node's id
        nNxDet++;
        LastNxDetNode = nd;
        
      }//if child is an Nxdetector node
    }//for @ child of the NxInstrument node
    //System.out.println("Detector information is");
    // showwDetectorInf();
  }

  public void showwDetectorInf(){
    if( DetectorInf == null){
      System.out.println("null");
      return;
    }
    if( DetectorInf.size() < 1){
      System.out.println( "No Elements");
      return;
    }
    Object[] k = DetectorInf.keySet().toArray();
    for( int i = 0; i< k.length; i++){
      Vector V =(Vector) DetectorInf.get( k[i]);
    }
  }

  /**
   * Returns the NXdetector node with the corresponding axes that are
   * linked
   */
  public NxNode matchNode( NxNode instrNode, String ax1Link, String ax2Link ){
    NxData_Gen ng = new NxData_Gen();

    int ax1;
    int ax2;

    ax1 = ax2 = 0;  //undefined-0; true-1;false-(-1)
    errormessage = " Improper inputs to matchNode";

    if( instrNode == null )
      return null;

    if( ( ax1Link == null ) )
      return null;

    if( !instrNode.getNodeClass().equals( "NXinstrument" ) )
      return null;

    errormessage = "";
    NxNode lastNxDetNode = null;
    int NumNxDetectorNodes =0;
    for( int i = 0; i < instrNode.getNChildNodes(); i++ ){
      NxNode nx = instrNode.getChildNode( i );
      
      if( nx == null )
        errormessage += ";improper Instr Child" + i;
      else if( nx.getNodeClass().equals( "NXdetector" ) ){
        NumNxDetectorNodes++;
        lastNxDetNode = nx;
        ax1 = ax2 = 0;
        for( int j=0; (j<nx.getNChildNodes()) && (ax1>=0) && (ax2 >= 0); j++ ){
          NxNode n1 = nx.getChildNode( j );

          if( n1 == null )
            errormessage += "improper Det Child" + j;
          else{
            Object X = n1.getAttrValue( "axis" );

            if( X != null ){
              int axnum = ng.cnvertoint( X );
              
              if( ng.getErrorMessage() != "" )
                DataSetTools.util.SharedData.addmsg( 
                                               "ERROR ="+ng.getErrorMessage());
              if( ng.getErrorMessage() == "" ){
                if( axnum == 1 )
                  if( n1.equals( ax1Link ) )
                    ax1 = 1;
                  else
                    ax1 = -1;
                else if( axnum == 2 )
                  if( ax2Link == null )
                    ax2 = -1;
                  else if( n1.equals( ax2Link ) )
                    ax2 = 1;
                  else
                    ax2 = -1;
              }//if ng.error ==""
            }//if X!=null
          }//else n1 ==null
        }//for j

        if( ( ax1 > 0 ) && ( ax2 ) > 0 )
          return nx;
        if( ( ax2Link == null ) && ( ax1 > 0 ) )
          return nx;
        // if( nDef == null)nDef= nx; if no match use first
      }//else if child a detector node
    }//for i
    if( NumNxDetectorNodes == 1)
       return lastNxDetNode;
    else
       return null;//nDef;
  }//matchNode

  /**
   * Fills out an existing DataSet with information from the
   * NXinstrument section of a Nexus datasource
   *
   * @param node the current node positioned to an NXinstrument part
   * of a datasource
   * @param DS the existing DataSet that is to be filled out
   *
   * @return error status: true if there is an error otherwise false
   */
  public boolean processDS( NxNode node, DataSet DS ){
     return processDS( node, DS, null);
  } 
  
  
  
  public boolean processDS( NxNode node, DataSet DS, NexIO.State.NxfileStateInfo fileStateInfo){
    errormessage = "Improper inputs NxInstrument";
    Node xmlDoc = null;
    if( fileStateInfo != null)
       xmlDoc = fileStateInfo.xmlDoc;
    if( (node == null) && (xmlDoc == null) )
      return true;
    
    if( DS == null )
       return true;
    String name = null;
    errormessage = "";
    float distance = Float.NaN;
    if( node != null ) {
         if( ! node.getNodeClass().equals( "NXinstrument" ) )
            return true;

         errormessage = "";

         NxNode X = node.getChildNode( "name" );
         if( X != null ) {
            Object r = X.getNodeValue();
            name = new NxData_Gen().cnvertoString( r );

  
         }

         // NXdetector stuff done in NXdata
         for( int i = 0 ; i < node.getNChildNodes() ; i++ ) {
            NxNode tnode = node.getChildNode( i );

            if( tnode.getNodeClass().equals( "NXsource" ) ) {
               NxNode tnode1 = tnode.getChildNode( "distance" );
               if( tnode1 == null )
                  return false;

               Object O = tnode1.getNodeValue();
               if( O != null )
                  if( O instanceof float[] )
                     if( Array.getLength( O ) == 1 ) {
                        distance = ( (float[]) O )[ 0 ];

                      
                     }
            }
         }
      }
    if( xmlDoc != null){
       NxEntryStateInfo EntryState = NexUtils.getEntryStateInfo( fileStateInfo);
       Node[] NN= NexIO.Util.Util.getxmlNXentryNodes( xmlDoc,EntryState.Name, fileStateInfo.filename);
       for( int i=0; i<4;i++){
         
            String S = ConvertDataTypes.StringValue(Util.getLeafNodeValues( 
                                   Util.getNXInfo( NN[i],"NXinstrument.name", null,null,null)));
            if( S != null)
               name = S;
            float f =ConvertDataTypes.floatValue(Util.getLeafNodeValues( 
                     Util.getNXInfo( NN[i],"NXsource.distance", null,null,null)));
            if( !Float.isNaN( f ))
               distance = f;
       }
    }
    if( name != null )
       DS.setAttribute( new StringAttribute( Attribute.INST_NAME , name
                .trim() ) );
    if( ! Float.isNaN( distance ))
       DS.setAttribute( new FloatAttribute(
                Attribute.INITIAL_PATH , distance ) );
    if( errormessage.length() > 0 )
      return true;
    return false;
  }
}
