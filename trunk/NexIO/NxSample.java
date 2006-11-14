/*
 * File:  NxSample.java 
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
 * Revision 1.7  2006/11/14 16:51:44  rmikk
 * Added code to check the xml Fixit file for some of the fields
 *
 * Revision 1.6  2005/12/29 23:13:50  rmikk
 * Removed useless == comparisons with Float.NaN
 *
 * Revision 1.5  2004/12/23 18:47:50  rmikk
 * Now reads the sample_orientation Nexus field
 *
 * Revision 1.4  2004/02/16 02:15:56  bouzekc
 * Removed unused import statements.
 *
 * Revision 1.3  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/11/20 16:14:51  pfpeterson
 * reformating
 *
 */

package NexIO;

import DataSetTools.dataset.*;
import DataSetTools.instruments.*;
import NexIO.Util.*;
import NexIO.State.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
/**
 * This class is used to process the NxSample part of a Nexus datasource
 */
public class NxSample{
  String errormessage;

  public NxSample(){
    errormessage = "";
  }


  /**
   * Returns error and warning messages or "" if there is none
   */
  public String getErrorMessage(){
    return errormessage;
  }

  public boolean processDS( NxNode node , DataSet DS ){
     return processDS( node, DS, null);
  }
  /**
   * Fills out an existing DataSet with information from the NXsample
   * section of a Nexus datasource
   *
   * @param node the current node positioned to an NXsample part of a
   * datasource
   * @param  DS  the existing DataSet that is to be filled out
   *
   * @return error status: true if there is an error otherwise false
   */
  public boolean processDS( NxNode node , DataSet DS, NexIO.State.NxfileStateInfo fileStateInfo ){
    errormessage = "Improper NxSampel inputs";

    NxEntryStateInfo EntryState = NexUtils.getEntryStateInfo( fileStateInfo);
    if( (node == null) && ( EntryState == null ) ) 
      return true;
    if( DS == null ) 
      return true;
    if( node != null)
    if( !node.getNodeClass().equals( "NXsample" ) ) 
      return true;
    errormessage = "";
    NxNode X = null;
    String sample_name=null;
    float  temperature =Float.NaN,
           pressure = Float.NaN;
    float[] orientation=null;
    if( node != null ) {
         X = node.getChildNode( "name" );

         if( X != null ) {
            Object val = X.getNodeValue();
            sample_name = new NxData_Gen().cnvertoString( val );
           
         }
         X = node.getChildNode( "temperature" );
         if( X != null ) {
            Object val = X.getNodeValue();
            if( val != null ) {
               Float S = new NxData_Gen().cnvertoFloat( val );
               if( S != null ){
                  temperature = S.floatValue();
                 
                     
                  }
            }
         }
         X = node.getChildNode( "pressure" );
         if( X != null ) {
            Object val = X.getNodeValue();
            if( val != null ) {
               Float S = new NxData_Gen().cnvertoFloat( val );
               if( S != null ){
                  pressure = S.floatValue();
               }
            }
         }

         orientation = NexIO.Util.NexUtils.getFloatArrayFieldValue(
                  node , "sample_orientation" );
         String units = NexIO.Util.NexUtils.getStringAttributeValue( node
                  .getChildNode( "sample_orientation" ) , "units" );

         NexIO.Util.ConvertDataTypes.UnitsAdjust( orientation , units ,
                  "radians" , (float) ( 180.0 / java.lang.Math.PI ) , 0f );
        
      }
    
    if( fileStateInfo != null)if( fileStateInfo.xmlDoc != null){
      Node[] NN= NexIO.Util.Util.getxmlNXentryNodes( fileStateInfo.xmlDoc,EntryState.Name, fileStateInfo.filename);
      for( int i=0; i<4; i++){
         String S =ConvertDataTypes.StringValue(Util.getLeafNodeValues(
                     Util.getNXInfo(NN[i],"NXsample.name",null,null,null)));
         if( S != null)
            sample_name = S;
         Node N3=Util.getNXInfo(NN[i],"NXsample.temperature",null,null,null);
         float T = ConvertDataTypes.floatValue(Util.getLeafNodeValues(
                      N3));
         if( !Float.isNaN( T ))
            temperature = T;
         N3=Util.getNXInfo(NN[i],"NXsample.pressure",null,null,null);
         float P =ConvertDataTypes.floatValue(Util.getLeafNodeValues( N3
                        ));
         if( !Float.isNaN( P))
            pressure = P;
         N3 =Util.getNXInfo(NN[i],"NXsample.sample_orientation",null,null,null);
         float[] Or =ConvertDataTypes.floatArrayValue(Util.getLeafNodeValues(
                 N3 ));
         if( Or != null)
            orientation = Or;
      }
    }
    if( sample_name!= null )
       DS.setAttribute( new StringAttribute(
                         Attribute.SAMPLE_NAME , sample_name ) );
    if( !Float.isNaN( temperature ) )
       DS.setAttribute( new FloatAttribute(
                Attribute.TEMPERATURE ,temperature ) );
    if( ! Float.isNaN( pressure ) )
       DS.setAttribute( new FloatAttribute( Attribute.PRESSURE ,
                pressure ) );
    if( orientation != null ){
       if( fileStateInfo != null)
          if((fileStateInfo.facility != null) && ( fileStateInfo.facility.equals("LANL")))
          if(( fileStateInfo.InstrumentName != null ) && (fileStateInfo.InstrumentName.equals("SCD"))){

             DS.setAttribute( new SampleOrientationAttribute(
                      Attribute.SAMPLE_ORIENTATION ,
                      new LANSCE_SCD_SampleOrientation( orientation[ 0 ] ,
                               orientation[ 1 ] , orientation[ 2 ] ) ) );
             return false;
          }
             
         DS.setAttribute( new SampleOrientationAttribute(
                  Attribute.SAMPLE_ORIENTATION ,
                  new IPNS_SCD_SampleOrientation( orientation[ 0 ] ,
                           orientation[ 1 ] , orientation[ 2 ] ) ) );
    }
    return false;  
 
}
}