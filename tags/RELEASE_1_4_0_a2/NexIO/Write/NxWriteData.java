/*
 * File:  NxWriteData.java 
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
 * Revision 1.3  2002/04/01 20:50:02  rmikk
 * Each NXdata now has a name related to its DataSet Title
 * A common label attribute is added to each NXdata from one data set. This, when read will by Isaw will merge these NXdata into one data set
 *
 * Revision 1.2  2002/03/18 20:58:27  dennis
 * Added initial support for TOF Diffractometers.
 * Added support for more units.
 *
 * Revision 1.1  2001/07/25 21:23:20  rmikk
 * Initial checkin
 *
 */

package NexIO.Write;
import NexIO.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import java.io.*;

/** Class used to write the NXdata information of a Data Set
 */
public class NxWriteData
{String errormessage;
  String axis1Link = "axis1";
  String axis2Link = "axis2";
  String axis3Link = "axis3";
  int instrType;
    public NxWriteData( int instrType)
      {errormessage = "";
       this.instrType = instrType;
       
      }

   /** Returns an error message or "" if none
   */ 
   public String getErrorMessage()
    {return errormessage;
    }


    /** Writes the Data from a data set to a NXdata section of a Nexus file
    *@param   nodeEntr an NxEntry node
    *@param   nxInstr  an NXinstrument node
    *@param   DS       The data set with the information
    *@param   makelinks should always be true
    *NOTE: This routine also writes some NXdetector information due to
    * linking requirements
    */
   public boolean processDS( NxWriteNode nodeEntr ,  NxWriteNode nxInstr ,  
       DataSet DS ,    boolean makelinks )
    {int rank1[] , 
         intval[] , 
         i , 
         j , 
         kk;
     
    char cc = 0;
    NxWriteNode node;
    errormessage = " Null inputs to Write Data";
    if( nodeEntr == null )
      return true;
    
    if( nxInstr == null )
      return true;
  
    if( DS == null )
       return true;
    errormessage = "";
  
    float xvals[] = null;
    float xvalsPrev[] = null;
    float data[] = null;
    int startIndex ,
        endIndex;
    startIndex = endIndex = -1;
    kk = 0;
    DS.Sort( Attribute.EFFICIENCY_FACTOR, true,DataSet.Q_SORT);
    DS.Sort( Attribute.TIME_FIELD_TYPE,true,DataSet.Q_SORT);
    for(  i = 0 ; i < DS.getNum_entries() ; i++ )
     {
      Data DB = DS.getData_entry( i );
      XScale XX = DB.getX_scale();
      int HistData = 0;
      xvals = XX.getXs();
       int ny_s = xvals.length;
      if( DB.getY_values().length < ny_s ) 
         HistData = 1;
      if( xvalsPrev == null )
         {startIndex = 0;
          xvalsPrev = xvals;
          }
      else if( xvalsPrev.length != xvals.length )
         endIndex = i;
      else if( xvalsPrev[0] != xvals[0] )
         endIndex = i;
      else if( xvalsPrev[xvalsPrev.length -1] != xvals[ xvals.length -1] )
         endIndex = i;
      else if( xvalsPrev[xvals.length/2] !=xvals[xvals.length/2] )
         endIndex = i;
       else if( xvalsPrev[xvals.length/4] != xvals[xvals.length/4] )
         endIndex = i;
      else if( xvalsPrev[3*xvals.length/4] != xvals[3*xvals.length/4] )
         endIndex = i;  
     // Check to see if the efficiency vector has changed

      if(( endIndex > 0 )||( i == DS.getNum_entries( )-1 ) )
        {
          if( endIndex <= 0 ) 
             endIndex = DS.getNum_entries();
          node = nodeEntr.newChildNode( DS.getTitle()+"_"+kk , "NXdata" );
          
          NxWriteNode nxdet = nxInstr.newChildNode("Detector"+kk,"NXdetector");
         String nodeName=(new Inst_Type()).AxisWriteHandler(1,kk,instrType,node,nxdet,DS,
              startIndex , endIndex);
         if( nodeName != null)
            node.addLink( nodeName);
         else System.out.println("UUUUUUUU axis 1 no link");
         nodeName=(new Inst_Type()).AxisWriteHandler(2,kk,instrType,node,nxdet,DS,
              startIndex,endIndex);
         
         if( nodeName != null)
            node.addLink( nodeName);
         else System.out.println("UUUUUUUU axis 2 no link");
         new NxWriteDetector(instrType).processDS( nxdet,DS,startIndex,endIndex);
         nxdet.setLinkHandle( "NXdetector"+kk);
         node.addLink( "NXdetector"+kk );
         //node.addLink( "axis1"+kk );
          //node.addLink( "axis2"+kk );

   
    if( DS.getNum_entries() <=  0 )
       {errormessage = " No Data Entries";
        return true;
       }

   // Now enter the Data Elements    
    Data DB1 = DS.getData_entry( startIndex );
        
    ny_s = DB1.getY_values().length;
    data = new float [ ny_s* ( endIndex - startIndex )];
     float y[];
   
    for(  j = startIndex ; j < endIndex ; j++ )
      { DB1 = DS.getData_entry( j  );
      
       y = DB1.getY_values();
       int uu = ny_s;
       if( y.length != uu )
            {  
              if( y.length < uu ) uu = y.length;             
            }
       System.arraycopy( y, 0 , data , ny_s*( j-startIndex ) , uu ); 
      }
   
    NxWriteNode n3 = node.newChildNode( "data", "SDS" );
    n3.addAttribute("label", (DS.getTitle()+(char)0).getBytes(),NexIO.Types.Char,
              NexIO.Inst_Type.makeRankArray(DS.getTitle().length()+1,-1,-1,-1,-1));
          
    
    rank1 = new int[1];
    rank1[0] = 1;
    intval = new int[1];
    intval[0] = 1;
    n3.addAttribute( "signal" , intval , Types.Int , rank1 );
    n3.addAttribute( "units", (DS.getY_units()+(char)0).getBytes(), Types.Char, 
        Inst_Type.makeRankArray(DS.getY_units().length()+1,-1,-1,-1,-1));
    

    rank1 = new int[2];
    rank1[1] = ny_s;
    rank1[0] = endIndex - startIndex ;
    n3.setNodeValue( data , Types.Float , rank1 ); 
    errormessage = n3.getErrorMessage();   
    if( errormessage == "" )
        {}
    else
         return true;

    // Add get the NxDetector to the NxInstrument
   //new NxWriteInstrument().addDetector( nxInstr ,"axis1"+kk , "axis2"+kk, 
   //                                    startIndex , endIndex , DS ,HistData);
   
         
   xvalsPrev = xvals;
   startIndex = i;
   endIndex = -1;
       kk++;
   }//if endIndex > 0
  

 }//for i = 0 i < num entries
   return false;
    }



    //Obsolete
private float[] MergeXvals ( int db , DataSet DS , float xvals[] )
  {if( db >= DS.getNum_entries() )
      return xvals; 
   if( db == 0 )
     { Data DB = DS.getData_entry(0 );
       XScale XX = DB.getX_scale();
       return MergeXvals( 1 , DS , XX.getXs() );
      }
   Data DB = DS.getData_entry( db );
   XScale XX = DB.getX_scale();
   float xlocvals[];
   xlocvals = XX.getXs();
   float Delta = ( xvals[ xvals.length -1] -xvals[0] )/xvals.length /20.0f;
   //System.out.println("Delta = "+Delta );
   if( Delta < 0 ) Delta = 0.0f;
   int j = 0; 
   int i = 0;
   int n = 0;
  
   while( ( i < xvals.length ) ||( j < xlocvals.length ) )
     { 
       if( i >= xvals.length )
         {j++;n++;}
       else if( j >= xlocvals.length )
         {i++;n++;}

       else if( xvals[i] < xlocvals[j]-Delta )
        { i++; n++;}
      else if(xvals[i] > xlocvals[j]+Delta )
        {j++;n++;}
      else
        {i++;j++;n++;}
     }  
  
   float Res[];
   Res = new float[ n  ];
    j = 0; i = 0;
   n = 0;
   while( ( i < xvals.length ) ||( j < xlocvals.length ) )
     { if( i >= xvals.length )
         {Res[n] = xlocvals[j];j++;n++;}
       else if( j >= xlocvals.length )
         {Res[n] = xvals[i]; i++;n++;}

       else if( xvals[i] < xlocvals[j]-Delta )
        { Res[n] = xvals[i];i++; n++;}
      else if(xvals[i] > xlocvals[j]+Delta )
        {Res[n] = xlocvals[j];j++;n++;}
      else
        {Res[n] = ( xvals[i]+xlocvals[j] )/2.0f;i++;j++;n++;}
     }
/*    System.out.println( "C" ); 
   NxNodeUtils nd = new NxNodeUtils();
   System.out.println( "local xvals = "+ nd.Showw( xlocvals ) );
  
   System.out.println( "incoming xvals = "+ nd.Showw(  xvals ) );
   
   System.out.println( "Result = "+ nd.Showw( Res ) );
   char c = 0;
   try{
    while( c< 32 )
    c = ( char ) System.in.read();
     }
   catch( IOException s ){}
*/ 
    return MergeXvals ( db+1 ,  DS , Res );

  }
/** Obsolete :Use this routine to determine interval lengths for both
* histgram and function data
*/
private float intlength( float xvals[] , int intnum , boolean histogram )
  {int nfix = 1;
   if( !histogram ) nfix = 0;
   if( xvals == null ) return 0.0f;
   if( intnum < 0 ) return 0.0f;
   if( intnum >=  xvals.length - nfix ) return 0.0f;
   if( histogram )
       return xvals[intnum+1]-xvals[intnum];
   float rendpt , lendpt;
   if( intnum == 0 )
      lendpt = xvals[0]-( xvals[1]-xvals[0] )/2.0f;
   else
      lendpt = ( xvals[intnum - 1]+xvals[intnum] )/2.0f;
    if( intnum >=  xvals.length-1 )
      rendpt = xvals[ xvals.length - 1] +
                ( xvals[xvals.length-1]+xvals[xvals.length -2 ] )/2.0f;
    else 
      rendpt = ( xvals[ intnum]+xvals[intnum+1] )/2.0f;
    return rendpt - lendpt;

  }
/**
* Obsolete Returns a rebinned value for this data blocks y values wrt the
* xvals rebinning.  Assumes histogram so far
*/
  private float[] Rebinn( Data DB , float xvals[] )
   {errormessage = "null inputs to Rebinn";
      if( DB == null )
         return null;
      if( xvals == null )
        return null;
     float yvals[];
     yvals = DB.getY_values( );
     if( yvals == null )
       return null;
     float xlocvals[];
     XScale XX = DB.getX_scale();
     xlocvals = XX.getXs();
     if( xlocvals == null )
       return null;
     errormessage = "";
     int ny_s =  xvals.length;
     int nfix = 0;
     if( xlocvals.length != yvals.length )
          nfix = 1;
      float Res[];
     Res = new float[ ny_s - nfix ];
     float Delta = ( xvals[ xvals.length-1]-xvals[0] )/xvals.length/15.0f;
     //System.out.println( "Rebinn Delta = "+Delta );
     int j = 0;
     for( int i = 0 ; i < yvals.length ; i++ )
      {boolean done = false;
       while( !done )
        {Res[j] = yvals[i] *( xvals[j+1]-xvals[j] ) /
                                            ( xlocvals[i+1]-xlocvals[i] );
         j++;
        if( j >= xvals.length - nfix ) done = true;
        else if( xvals[j] >= xlocvals[ i + 1 ]- Delta ) done = true;
        
        }
       
      } 
    
     return Res;  

    }
public static void main( String args[] )
  {NxWriteData nw = new NxWriteData(1);
   NxNodeUtils nd = new NxNodeUtils();
   IsawGUI.Util ut = new IsawGUI.Util();
   DataSet dss[];
   dss = ut.loadRunfile( "C:\\SampleRuns\\gppd9898.run" );
   
   float xvals[];
   xvals = nw.MergeXvals( 0, dss[1] , null );
   System.out.println(  "*****final xval list= *****" );
   System.out.println( nd.Showw( xvals ));
   Data DB = dss[1].getData_entry( 0 );
   float ynew[] , xold[] , yold[];
   ynew = nw.Rebinn( DB , xvals );
   XScale XX = DB.getX_scale();
   xold = XX.getXs();
   yold = DB.getY_values();
   System.out.println( "xold= "+nd.Showw( xold ) );
   System.out.println( "" );
   System.out.println( "xbew= "+nd.Showw( xvals ) );
   System.out.println( "" );
   System.out.println( "yold= "+nd.Showw( yold ) );
   System.out.println( "" );
   System.out.println( "ynew= "+nd.Showw( ynew ) );
   System.out.println( "" );
   }
}










