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
 * Revision 1.1  2001/07/25 21:23:20  rmikk
 * Initial checkin
 *
 */
package NexIO.Write;
import NexIO.*;
import DataSetTools.dataset.*;
import java.util.*;
import java.text.*;
public class NxWriteEntry
{String errormessage;

    public NxWriteEntry()
      {errormessage = "";
      }

   public String getErrorMessage()
    {
     return errormessage;
    }


    /** Assumes that the NxMonitor and NxData node have already been set
   *
   */
   public boolean processDS( NxWriteNode node, DataSet DS)
    {NxNodeUtils nu = new NxNodeUtils();
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
    
     if( X instanceof int[])
      {int u[]; u = (int[])X;
       if( u.length > 0)
         {intval = new int[1];
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
        try
          {
           SDate = new SimpleDateFormat("yyyy-MM-dd").format(X);
          }
        catch(Exception s)
          {
           SDate = X.toString();
           } 
    else
       SDate = ne.cnvertoString( X);

    if( X1 != null)
      if( X1 instanceof Date)
       try
         {
           STime = new SimpleDateFormat("hh:mm:ss").format(X1);
          }
       catch(Exception s){STime = X1.toString();} 
    else
       STime = ne.cnvertoString( X1);
   
    SDate = SDate+" "+STime;
   
    if( SDate.length() > 1)
      {n1 = node.newChildNode( "end_time", "SDS");
       ranks = new int[1];
       ranks[0] = SDate.length()+1;
       n1.setNodeValue((SDate+cc).getBytes(), Types.Char, ranks);
       if( n1.getErrorMessage()  != "")
          errormessage += ";"+errormessage;     
      }
    
     X = DS.getAttributeValue( Attribute.RUN_TITLE);     
     if( X != null)
      {String instr_name = ne.cnvertoString( X);       
       if( instr_name != null)
        { n1 = node.newChildNode( "title","SDS");         
          ranks = new int[1];
          ranks[0] = instr_name.length() +1;
          n1.setNodeValue( (instr_name+cc).getBytes(), Types.Char, ranks); 
          if( n1.getErrorMessage() != "")
            errormessage += ";"+errormessage;         
        }
      }

/*  //Moved to NxWriteInstrument
     X = DS.getAttributeValue( Attribute.INST_NAME);
     NxWriteNode NxInstr = node.newChildNode( "Instrument","NXinstrument");
     if( X != null)
      {String instr_name = ne.cnvertoString( X);
      if( instr_name != null)
        {
         n1 = NxInstr.newChildNode( "name", "SDS");
          ranks = new int[1];
          ranks[0] = instr_name.length() +1;
          n1.setNodeValue( (instr_name+cc).getBytes(), Types.Char, ranks); 
          if( n1.getErrorMessage() != "")
            errormessage += ";"+errormessage;         
       }
      }
       X = DS.getAttributeValue( Attribute.INST_TYPE);
    
    if( X != null)
      {int instr_type = ne.cnvertoint( X);
      NexIO.Inst_Type it = new NexIO.Inst_Type();
     
       String analysis = it.getNexAnalysisName( instr_type);
      if( analysis == null)
        {ranks = new int[1];
         ranks[0] = 1;
         intval = new int[1];
         intval[0] = instr_type;
         node.addAttribute("isaw_instr_type", intval, Types.Int, ranks);
        }
      else
        if( analysis.length() > 0)
        {
         n1 = node.newChildNode( "analysis", "SDS");
          ranks = new int[1];
          ranks[0] = analysis.length()+1;
          
          
          n1.setNodeValue( (analysis+cc).getBytes(), Types.Char, ranks); 
          if( n1.getErrorMessage() != "")
            errormessage += ";"+errormessage;         
       }
      }
*/


    X = DS.getAttributeValue( Attribute.SAMPLE_NAME);
    if( X !=  null)
     {String Samp_name = ne.cnvertoString( X);
      if( Samp_name != null)
       {NxWriteNode Instrnode = node.newChildNode( "sample", "NXsample");
        n1 = Instrnode.newChildNode( "name", "SDS");
        ranks = new int[1];
        ranks[0] = Samp_name.length()+1;
        n1.setNodeValue( (Samp_name+cc).getBytes(),Types.Char,ranks);
       }
     }
/*
    n1 = NxInstr.newChildNode("detector", "NXdetector");
    NxWriteDetector ndet = new NxWriteDetector();
    if( ndet.processDS( n1 , DS ))
      errormessage += ";"+ndet.getErrorMessage();
    return false;
    //Monitors and NxData already taken care of
    //add attributes/fields run_number, and end_time and end_date
*/
    return false;
    }

}





