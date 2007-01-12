/*
 * File:  NxBean.java 
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
 * Revision 1.8  2007/01/12 14:48:46  dennis
 * Removed unused imports.
 *
 * Revision 1.7  2006/11/14 16:51:44  rmikk
 * Added code to check the xml Fixit file for some of the fields
 *
 * Revision 1.6  2005/12/29 23:14:06  rmikk
 * Removed useless == comparisons with Float.NaN
 *
 * Revision 1.5  2005/03/16 17:47:58  rmikk
 * Used static references for static methods
 *
 * Revision 1.4  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/11/20 16:14:43  pfpeterson
 * reformating
 *
 */
package NexIO;

import DataSetTools.dataset.*;
import NexIO.State.*;
import NexIO.Util.*;
//import javax.xml.parsers.*;
import org.w3c.dom.*;
public class NxBeam{
  String errormessage;

  public NxBeam(){
    errormessage = "";
  }

  public String getErrorMessage(){
    return errormessage;
  }

  public boolean processDS(  NxNode node,  DataSet DS){
     return processDS(node,DS, null);
  }
  
  public boolean processDS(  NxNode node,  DataSet DS, NxfileStateInfo State ){
    errormessage = "Improper inputs to NxBeam";
    NxEntryStateInfo EntryInfo = NexUtils.getEntryStateInfo( State );
    Node xmlDoc =null;
    if( State != null)
       xmlDoc = State.xmlDoc;
    if( (node == null) && (xmlDoc == null))
      return true;
    if( DS == null)
      return true;
    errormessage ="";
    float energy_in= Float.NaN,
          energy_out= Float.NaN,
          distance = Float.NaN;
    if( node != null ) {
         if( ! node.getNodeClass().equals( "NXbeam" ) )
            return true;
         errormessage = "";
         NxData_Gen ng = new NxData_Gen();
         NXData_util nu = new NXData_util();
         NxNode n1 = node.getChildNode( "incident_energy" );
         if( n1 != null ) {
            Object O = n1.getNodeValue();
            float x[];
            float f;
            x = NXData_util.Arrayfloatconvert( O );
            if( x != null )
               if( x.length > 0 )
                  energy_in = x[ 0 ];
               else
                  energy_in = Float.NaN;
            else
               energy_in = Float.NaN;
        
           
         }

         // energy out
         n1 = node.getChildNode( "final_energy" );
         if( n1 != null ) {
            Object O = n1.getNodeValue();
            float x[];
            float f;
            x = NXData_util.Arrayfloatconvert( O );
            if( x != null )
               if( x.length > 0 )
                  energy_out = x[ 0 ];
               else
                  energy_out = Float.NaN;
            else
               energy_out = Float.NaN;
            if( Float.isNaN( energy_out ) ) {
               Float F = ng.cnvertoFloat( O );
               if( F != null )
                  energy_out = F.floatValue();
            }
         
         }
       
      }
    
    if( xmlDoc != null){
       Node[] NN =Util.getxmlNXentryNodes( xmlDoc, EntryInfo.Name, State.filename);
       for( int i=0; i< 3; i++){
          
       }
    }
    if( ! Float.isNaN( energy_in ) ) {
       FloatAttribute FA = new FloatAttribute(
                Attribute.NOMINAL_ENERGY_IN , ( energy_in) );

       DS.setAttribute( FA );
       FA = new FloatAttribute( Attribute.ENERGY_IN , ( energy_in ) );

       DS.setAttribute( FA );
    }
    if( ! Float.isNaN( energy_out ) ) {
       FloatAttribute FA = new FloatAttribute( Attribute.ENERGY_OUT ,
                ( energy_out ) );

       DS.setAttribute( FA );
    }
    return false;  
  }//processDS
}
