/*
 * File:  Inst_Type.java 
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
 * Revision 1.3  2001/08/09 16:43:27  rmikk
 * Added Documentation.
 *
 */
package NexIO;
import java.util.*;
import DataSetTools.instruments.*;
import java.io.*;
import java.util.*;

/** This class currently maps correspondences between Nexus and Isaw
 *instrument types.  This class will later map instrument types to
 * Handlers for various parts of the retrievers and writers
 */
public class Inst_Type
{private static Hashtable HT =null;
 private
    static String  NxNames[] = {"MonoNXPD" , "TOFNDGS" , "TOFNIGS"};
                                                //MonoNXTAS
 private 
  static int  Isaw_inst_types[] ={InstrumentType.MONO_CHROM_DIFFRACTOMETER,   
                                // InstrumentType.UNKNOWN,
                                 InstrumentType.TOF_DG_SPECTROMETER ,
                                 InstrumentType.TOF_IDG_SPECTROMETER };


    /** Initializes correspondence tables
   */
 public Inst_Type()
   { if( HT == null)
      {HT = new Hashtable();
       HT.put( "MonoNXPD", new Integer(0)); 
      
        HT.put( "TOFNDGS", new Integer(1));  
         HT.put( "TOFNIGS", new Integer(2));  
      }
   }

 /** Returns Isaw's instrument number that corresponds to Nexus' analysis
*    name
*/
 public int getIsawInstrNum( String NexusAnalysisName )
   {
   Integer I = (Integer )( HT.get( NexusAnalysisName ) );
   if( I == null)
     return InstrumentType.UNKNOWN;
   int i = I.intValue();
   if( i<0) 
       return InstrumentType.UNKNOWN;
   if( i >= Isaw_inst_types.length )
       return InstrumentType.UNKNOWN;
   return Isaw_inst_types[i];
   }

/** Returns the Nexus analysis field name corresponding to Isaw's
* instrument number IsawInstrNum
*/
public String getNexAnalysisName( int IsawInstrNum)
  {for( int i=0; i<Isaw_inst_types.length; i++)
     if( Isaw_inst_types[i] == IsawInstrNum)
        return NxNames[i];
   return null;
  }

/// more to get NxEntry Handlers, NxData Handlers, etc...


/** Test program for procedures in this module
*/
public static void main( String args[])
  { Inst_Type  X = new Inst_Type();
    char option=0;
    while( option !='x')
    { System.out.println( "Enter option");
      System.out.println(" a) Enter number");
      System.out.println(" b) Enter String");
      option = 0;
      try{
        while( option <32)
          option = (char)System.in.read();
         }
      catch(IOException s){option =0;}
     String S = "";
     char c=0;
     try{
        while( c<33)
          c =(char)System.in.read();
        while( c >=32)
          {S = S+c;
           c =(char)System.in.read();
           }
        }
     catch(IOException s){}
    if( option =='a')
     {int num=-1;
      try{
         num = (new Integer(S)).intValue();
         System.out.println("Result="+X.getNexAnalysisName( num));
         }
      catch( Exception s)
         {System.out.println("Error="+s);}
        
     }
    else if( option == 'b')
     {System.out.println("Res="+X.getIsawInstrNum( S));
     }

    }//while

  }

}
