/*
 * File:  NxWriteEntry.java 
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
 * Revision 1.11  2004/12/23 19:15:19  rmikk
 * Changed the "sec" unit to second
 * Eliminated unused code
 * Added blank lines between some code
 *
 * Revision 1.10  2004/05/14 15:03:52  rmikk
 * Removed unused variables
 *
 * Revision 1.9  2004/02/14 17:49:57  rmikk
 * duration is now in NXentry and has units of seconds
 *
 * Revision 1.8  2003/12/08 20:57:49  rmikk
 * Moved the writing of duration from NXbeam to NXentry
 *
 * Revision 1.7  2003/11/24 14:05:31  rmikk
 * Changed the analysis field to the description field.
 * Added a NXsample subclass to this NXentry class
 * Add several NXlog subclasses for testing purposes.
 *
 * Revision 1.6  2002/11/27 23:29:19  pfpeterson
 * standardized header
 *
 * Revision 1.5  2002/11/20 16:15:39  pfpeterson
 * reformating
 *
 * Revision 1.4  2002/04/01 20:53:38  rmikk
 * Fixed a logical error
 *
 * Revision 1.3  2002/03/18 20:58:38  dennis
 * Added initial support for TOF Diffractometers.
 * Added support for more units.
 *
 */

package NexIO.Write;

import NexIO.*;
import DataSetTools.dataset.*;
import java.util.*;
import java.text.*;

public class NxWriteEntry{
  String errormessage;
  int instrType;
  public static String DESCRIPTION = "description";
     //Changed from analysis
  public NxWriteEntry(int instrType){
    errormessage = "";
    this.instrType=instrType;
  }

  public String getErrorMessage(){
    return errormessage;
  }


  /**
   * Assumes that the NxMonitor and NxData node have already been set
   */
  public boolean processDS( NxWriteNode node, DataSet DS){
    NxData_Gen ne = new NxData_Gen();
    
    int ranks[],intval[];
    char cc = 0;
    errormessage = "Null or improper inputs to NxEntry processor";
    
    if( node == null)
      return true;
     
    if( DS == null) 
      return true;
     
    errormessage =  "";
    NxWriteNode n1 ;
    Object X = DS.getAttributeValue( Attribute.RUN_NUM);
    
    if( X instanceof int[]){
      int u[]; u = (int[])X;
      if( u.length > 0){
        intval = new int[1];
        ranks = new int[1];
        ranks[0] = 1;
        intval[0] = u[0];
         
        n1 = node.newChildNode("run_number","SDS");
        n1.setNodeValue( intval ,Types.Int, ranks);
        if( n1.getErrorMessage() != "")
          errormessage += ";"+n1.getErrorMessage();
      }
    }
    
    X = DS.getAttributeValue( Attribute.END_DATE);
    Object X1 = DS.getAttributeValue( Attribute.END_TIME);
   
    String SDate = "";
    String STime = "";
    if( X != null)
      if( X instanceof Date)
        try{
          SDate = new SimpleDateFormat("yyyy-MM-dd").format(X);
        }catch(Exception s){
          SDate = X.toString();
        } 
      else
        SDate = ne.cnvertoString( X);

    if( X1 != null)
      if( X1 instanceof Date)
        try{
          STime = new SimpleDateFormat("hh:mm:ss").format(X1);
        }catch(Exception s){
          STime = X1.toString();
        }
      else
        STime = ne.cnvertoString( X1);
   
    SDate = SDate+" "+STime;
   
    if( SDate.length() > 1){
      n1 = node.newChildNode( "end_time", "SDS");
      ranks = new int[1];
      ranks[0] = SDate.length()+1;
      n1.setNodeValue((SDate+cc).getBytes(), Types.Char, ranks);
      if( n1.getErrorMessage()  != "")
        errormessage += ";"+errormessage;     
    }
    
    X = DS.getAttributeValue( Attribute.RUN_TITLE);     
    if( X != null){
      String instr_name = ne.cnvertoString( X);       
      if( instr_name != null){
        n1 = node.newChildNode( "title","SDS");         
        ranks = new int[1];
        ranks[0] = instr_name.length() +1;
        n1.setNodeValue( (instr_name+cc).getBytes(), Types.Char, ranks); 
        if( n1.getErrorMessage() != "")
          errormessage += ";"+errormessage;         
      }
    }
    

    int instr_type = instrType;
    NexIO.Inst_Type it = new NexIO.Inst_Type();
    
    String analysis = it.getNexAnalysisName( instr_type);
    n1 = node.newChildNode( DESCRIPTION, "SDS");
    ranks = new int[1];
    ranks[0] = 1;
    intval = new int[1];
    intval[0] = instr_type;
      
    if( (analysis == null)){
      n1.addAttribute("isaw_instr_type", intval, Types.Int, ranks);
      
      analysis ="";
    }else if( analysis.length() <= 0){
      n1.addAttribute("isaw_instr_type", intval, Types.Int, ranks);
      analysis = "";
    }else{
      ranks = new int[1];
      ranks[0] = analysis.length()+1;
          
      n1.setNodeValue( (analysis+cc).getBytes(), Types.Char, ranks); 
      if( n1.getErrorMessage() != "")
        errormessage += ";"+errormessage;         
    }
    ranks = new int[1];
    ranks[0] = 4;
    n1.addAttribute("version",("1.0"+cc).getBytes(),Types.Char, ranks);
    
    //--------------------- duration ---------------------------
    Object O = DS.getAttributeValue( Attribute.NUMBER_OF_PULSES );
    if( O != null ){
      if( O instanceof Number ){
        float f = ( ( Number )O ).floatValue();
        float[] ff = new float[1];
        ff[0] = f/30.0f;
        int[] rank = new int[1];
        rank[0] = 1;
        n1 = node.newChildNode( "duration" ,"SDS" );
        n1.setNodeValue( ff , Types.Float , rank ); 
        if( n1.getErrorMessage() != "" );
        errormessage += ":" + n1.getErrorMessage();
        n1.addAttribute("units",("second"+(char)0).getBytes(),Types.Char,
                        Inst_Type.makeRankArray(4,-1,-1,-1,-1));
      }  
    }
   //---------------------user--------------------
   String user = (String)DS.getAttributeValue( Attribute.USER); 
   if( user != null){
      n1 = node.newChildNode( "user" ,"SDS" );
      n1.setNodeValue( (user+(char)0).getBytes() , Types.Char , 
         Inst_Type.makeRankArray(user.length()+1,-1,-1,-1,-1)); 
   }
      
    NxWriteLog writelog1 = new NxWriteLog( 6);
    NxWriteNode logNode = node.newChildNode( "log_6","NXlog");
    if( writelog1.processDS( logNode, null, 6))
        errormessage += writelog1.getErrorMessage();


    if( errormessage.length() <1)
       return false;
    else 
       return true;
  }
}
