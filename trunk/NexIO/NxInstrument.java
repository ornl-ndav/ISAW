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

/** This class is used to process the NXinstrument information in a Nexus
*data source
*/
public class NxInstrument
{String errormessage;
 public NxInstrument()
   {errormessage = "";}

/**Returns error and/or warning messages or "" if none
*/
 public String getErrorMessage()
   {
     return errormessage;
   }

 /** Fills out an existing DataSet with information from the NXinstrument
   * section of a Nexus datasource
  *@param node  the current node positioned to an NXinstrument part of a datasource
  *@param  DS  the existing DataSet that is to be filled out
  *@return  error status: true if there is an error otherwise false
  */
 public boolean processDS( NxNode node ,  DataSet DS )
  {
    errormessage = "Improper inputs NxInstrument";
  
   if( node ==  null ) 
        return true;
   if( DS == null ) 
        return true;
   if( !node.getNodeClass().equals( "NXinstrument" ) )
          return true;
   errormessage = "";
   NxNode X = node.getChildNode( "name" );
   if( X!= null )
     {Object r = X.getNodeValue();
      String S = new NxData_Gen().cnvertoString( r );
     if( S!= null ) 
        DS.setAttribute( new StringAttribute( Attribute.INST_NAME , S ) );
     }
    for( int i = 0 ; i < node.getNChildNodes() ; i++ )
     {NxNode tnode = node.getChildNode( i );
      if( tnode.getNodeClass().equals( "NXdetector" ) )
        {
          NxDetector nd = new NxDetector();	
         if( nd.processDS( tnode ,  DS ) )
          {
            errormessage = errormessage+":"+nd.getErrorMessage();
           }
         }
     }
  if( errormessage.length() > 0 )
    return true;
  return false;
  }  
}
