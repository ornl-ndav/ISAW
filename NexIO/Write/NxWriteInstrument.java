/*
 * File:  NxWriteInstrument.java 
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
 * Revision 1.4  2002/11/20 16:15:40  pfpeterson
 * reformating
 *
 * Revision 1.3  2002/03/18 20:58:44  dennis
 * Added initial support for TOF Diffractometers.
 * Added support for more units.
 *
 * Revision 1.2  2001/08/01 14:38:53  rmikk
 * Moved the savind of  a DataSet's INSTR_TYPE to
 * NXentry .analysis
 *
 * Revision 1.1  2001/07/25 21:23:20  rmikk
 * Initial checkin
 *
 */

package NexIO.Write;

import NexIO.*;
import DataSetTools.dataset.*;

public class NxWriteInstrument{
  String errormessage;
  NxWriteNode node;
  int instrType;

  public NxWriteInstrument(int instrType ){
    errormessage = "";
    this.instrType = instrType;
  }

  /**
   * Returns an errormessage or "" if no error
   */
  public String getErrorMessage(){
    return errormessage;
  }

  /**
   * Adds a new detector to an NxInstrument Node.
   *
   * @param Instr The instrument node to which the detector is to be
   * added
   * @param axis1_link<br> axis2_link The axis that are linked between
   * NXdata and NXdetectors
   *@param startIndex, endIndex The indecis of the Data Blocks to be
   *<br> included in this NXdetector(startIndex-> endIndex-1
   *@param DS The data set that has the information
   */
  public boolean addDetector( NxWriteNode Instr , String axis1_link , 
                              String axis2_link , int  startIndex , 
                              int endIndex , DataSet DS ){
    NxWriteNode ndNode = Instr.newChildNode( "detector" + startIndex , 
                                             "NXdetector" );
    
    NxWriteDetector nd = new NxWriteDetector( instrType);
    nd.setLinkNames( axis1_link , axis2_link , null );
    if( nd.processDS( ndNode , DS , startIndex , endIndex ) ){
      errormessage  += ";" + nd.getErrorMessage();
      return true;
    }
    return false;
  }

   /**
    * Writes data from a DataSet to an NxInstrument Node
    *
    * @param NxInstr A NXinstrument node
    * @param DS the data set with the information
    */
  public boolean processDS( NxWriteNode NxInstr , DataSet DS ){
    errormessage = "Undefined inputs to NXInstrument";
   
    if( NxInstr == null ) 
      return true;
     
    if( DS == null ) 
      return true;
     
    NxData_Gen ne = new NxData_Gen ();
    errormessage = "";
    NxWriteNode n1 = null;
    Object X = DS.getAttributeValue( Attribute.INST_NAME );
    int ranks[] , intval[];
    char cc = 0;
    
    if( X != null ){
      String instr_name = ne.cnvertoString( X );
      if( instr_name != null ){
        n1 = NxInstr.newChildNode( "name" , "SDS" );
        ranks = new int[1];
        ranks[0] = instr_name.length() + 1;
        n1.setNodeValue( (instr_name+cc).getBytes(), Types.Char, ranks ); 
        if( n1.getErrorMessage() != "" )
          errormessage  += ";" + n1.getErrorMessage();  
      }
    }
    
/* //goes to NXEntry
   X = DS.getAttributeValue( Attribute.INST_TYPE );
   
   if( X != null )
   {int instr_type = ne.cnvertoint( X );
   NexIO.Inst_Type it = new NexIO.Inst_Type();
   
   String analysis = it.getNexAnalysisName( instr_type );
   n1 =  NxInstr.newChildNode( "analysis" , "SDS" );
   ranks = new int[1];
   ranks[0] = 1;
   intval = new int[1];
   intval[0] = instr_type;
   if( analysis == null )
   {
   n1.addAttribute( "isaw_instr_type" , intval , 
   Types.Int , ranks );
   }
   else if(analysis.length() <= 0)
   n1.addAttribute( "isaw_instr_type" , intval , 
   Types.Int , ranks );
   else
   {
   
   ranks = new int[1];
   ranks[0] = analysis.length() + 1;          
   
   n1.setNodeValue( ( analysis + cc ).getBytes( ), 
   Types.Char , ranks ); 
   if( n1.getErrorMessage() != "" )
   errormessage += ";" + errormessage;         
   }
   
*/
    if( DS.getNum_entries() > 0 ){
      Object XX =DS.getData_entry(0).getAttributeValue(Attribute.INITIAL_PATH);
      if( XX !=  null )if( XX instanceof Number ){
        NxWriteNode nxSrce = NxInstr.newChildNode( "Source" , "NXsource" );
        NxWriteNode n3 = nxSrce.newChildNode( "distance" , "SDS" );
        int rank[];
        rank = new int[1];
        rank[0] = 1;
        float xx[];
        xx = new float[1];
        xx[0] = ( ( Number )XX ).floatValue();
        n3.setNodeValue( xx , Types.Float  , rank );
      }
    }
    return false;
  }
}
