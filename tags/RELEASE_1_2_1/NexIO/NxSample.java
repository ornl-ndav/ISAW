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
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
 *
 */
package NexIO;
import DataSetTools.dataset.*;
import NexIO.*;

/** This class is used to process the NxSample part of a Nexus datasource
 */
public class NxSample
{String errormessage;
    public NxSample()
     {
    errormessage = "";
     }


    /**Returns error and warning messages or "" if there is none
   */
   public String getErrorMessage()
     {
       return errormessage;
      }


    /** Fills out an existing DataSet with information from the NXsample
   * section of a Nexus datasource
  *@param node  the current node positioned to an NXsample part of a datasource
  *@param  DS  the existing DataSet that is to be filled out
  *@return  error status: true if there is an error otherwise false
  */
    public boolean processDS( NxNode node , DataSet DS )
      {errormessage = "Improper NxSampel inputs";
       if( node == null ) 
          return true;
       if( DS == null ) 
            return true;
       if( !node.getNodeClass().equals( "NXsample" ) ) 
           return true;
       errormessage = "";
       NxNode X = node.getChildNode( "name" );
       if( X!= null )
        {Object val = X.getNodeValue();
         String S = new NxData_Gen().cnvertoString( val );
         if(  S!=  null )
           DS.setAttribute(  new StringAttribute( Attribute.SAMPLE_NAME , S ) ); 
        }
       X = node.getChildNode( "temperature" );
       if( X!= null )
        {Object val = X.getNodeValue();
         if( val != null )
          {  Float S = new NxData_Gen().cnvertoFloat( val );
             if( S!= null )
             if( S.floatValue()!= ( java.lang.Float.NaN ) ) 
               DS.setAttribute( new FloatAttribute( Attribute.TEMPERATURE , S.floatValue() ) );
           } 
        }
      X = node.getChildNode( "pressure" );
       if( X!= null )
        {Object val = X.getNodeValue();
         if( val != null )
          {  Float S = new NxData_Gen().cnvertoFloat( val );
             if( S!= null )
             if( S.floatValue()!= ( java.lang.Float.NaN ) ) 
               DS.setAttribute( new FloatAttribute( Attribute.PRESSURE , S.floatValue() ) );
           } 
        }
      return false;  
      }





}
