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
 * Revision 1.7  2003/11/24 13:54:19  rmikk
 * Writes out separate detectors to separate NXdata.
 * Writes out the detector orientation as Euler Transformations
 *
 * Revision 1.6  2003/06/18 20:34:12  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.5  2002/11/27 23:29:19  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/11/20 16:15:37  pfpeterson
 * reformating
 *
 * Revision 1.3  2002/04/01 20:50:02  rmikk
 * Each NXdata now has a name related to its DataSet Title
 * A common label attribute is added to each NXdata from one data set. This, when read will by Isaw will merge these NXdata into one data set
 *
 * Revision 1.2  2002/03/18 20:58:27  dennis
 * Added initial support for TOF Diffractometers.
 * Added support for more units.
 *
 */

package NexIO.Write;

import NexIO.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.util.StringUtil;
import java.io.*;
import NexIO.Util.*;
import DataSetTools.util.*;
/**
 * Class used to write the NXdata information of a Data Set
 */
public class NxWriteData{
  String errormessage;
  String axis1Link = "axis1";
  String axis2Link = "axis2";
  String axis3Link = "axis3";
  
  int Inst_Type;

  public NxWriteData( int Inst_Type){
    errormessage = "";
    this.Inst_Type = Inst_Type;
  }

  /**
   * Returns an error message or "" if none
   */ 
  public String getErrorMessage(){
    return errormessage;
  }

  /**
   * Writes the Data from a data set to a NXdata section of a Nexus
   * file
   *
   * @param nodeEntr an NxEntry node
   * @param nxInstr an NXinstrument node
   * @param DS The data set with the information
   * @param makelinks should always be true
   *
   * NOTE: This routine also writes some NXdetector information due to
   * linking requirements
   */
  public boolean processDS( NxWriteNode nodeEntr ,  NxWriteNode nxInstr ,  
                            DataSet DS ,    boolean makelinks ){
    int rank1[], intval[], i, j, kk;
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
  
    int[] gridIDs = Grid_util.getAreaGridIDs( DS);
    
    if( Can2Dify( gridIDs, DS))
       return processDSgrid( nodeEntr, nxInstr, DS, makelinks);
    float xvals[] = null;
    float xvalsPrev[] = null;
    float data[] = null;
    int startIndex, endIndex;
    startIndex = endIndex = -1;
    kk = 0;
    //Adjacent spectra will now have the same Xscale? and efficiencies
    DS.Sort( Attribute.EFFICIENCY_FACTOR, true,DataSet.Q_SORT);
    DS.Sort( Attribute.TIME_FIELD_TYPE,true,DataSet.Q_SORT);
    for(  i = 0 ; i < DS.getNum_entries() ; i++ ){
      Data DB = DS.getData_entry( i );
      XScale XX = DB.getX_scale();
      int HistData = 0;
      xvals = XX.getXs();
      int ny_s = xvals.length;
      if( DB.getY_values().length < ny_s ) 
        HistData = 1;
      if( xvalsPrev == null ){
        startIndex = 0;
        xvalsPrev = xvals;
      }else if( xvalsPrev.length != xvals.length )
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
      // Check to see if the efficiency Xscale has changed

      if(( endIndex > 0 )||( i == DS.getNum_entries( )-1 ) ){
        if( endIndex <= 0 ) 
          endIndex = DS.getNum_entries();
        node = nodeEntr.newChildNode( DS.getTitle()+"_"+kk , "NXdata" );
          
        NxWriteNode nxdet = nxInstr.newChildNode("Detector"+kk,"NXdetector");
        String nodeName=(new Inst_Type()).AxisWriteHandler(1,kk,Inst_Type,node,
                                               nxdet,DS,startIndex , endIndex);
        if( nodeName != null)
          node.addLink( nodeName);
        else
          System.out.println("UUUUUUUU axis 1 no link");
        nodeName=(new Inst_Type()).AxisWriteHandler(2,kk,Inst_Type,node,nxdet,
                                                    DS,startIndex,endIndex);
         
        if( nodeName != null)
          node.addLink( nodeName);
        else
          System.out.println("UUUUUUUU axis 2 no link");
        new NxWriteDetector(Inst_Type).processDS(nxdet,DS,startIndex,endIndex);
        nxdet.setLinkHandle( "NXdetector"+kk);
        node.addLink( "NXdetector"+kk );

   
        if( DS.getNum_entries() <=  0 ){
          errormessage = " No Data Entries";
          return true;
        }

        // Now enter the Data Elements    
        Data DB1 = DS.getData_entry( startIndex );
        
        ny_s = DB1.getY_values().length;
        data = new float [ ny_s* ( endIndex - startIndex )];
        float[] errors = new float [ ny_s* ( endIndex - startIndex )];
        float y[];
   
        for(  j = startIndex ; j < endIndex ; j++ ){
          DB1 = DS.getData_entry( j  );
      
          y = DB1.getY_values();
          int uu = ny_s;
          if( y.length != uu ){
            if( y.length < uu ) uu = y.length;             
          }
          System.arraycopy( y, 0 , data , ny_s*( j-startIndex ) , uu ); 

          y = DB1.getErrors();
          uu = ny_s;
          if( y.length != uu ){
            if( y.length < uu ) uu = y.length;             
          }
          System.arraycopy( y, 0 , errors , ny_s*( j-startIndex ) , uu ); 
        }
        //------------------ Write data ---------------
        NxWriteNode n3 = node.newChildNode( "data", "SDS" );
        n3.addAttribute("label", (DS.getTitle()+(char)0).getBytes(),
          NexIO.Types.Char,
          NexIO.Inst_Type.makeRankArray(DS.getTitle().length()+1,-1,-1,-1,-1));
          
        rank1 = new int[1];
        rank1[0] = 1;
        intval = new int[1];
        intval[0] = 1;
        n3.addAttribute("signal",intval , NexIO.Types.Int , rank1 );
        n3.addAttribute("units",(DS.getY_units()+(char)0).getBytes(), 
                NexIO.Types.Char, NexIO.Inst_Type.makeRankArray(DS.getY_units().length()+1,
                                                    -1,-1,-1,-1));
    
        rank1 = new int[2];
        rank1[1] = ny_s;
        rank1[0] = endIndex - startIndex ;
        n3.setNodeValue( data , NexIO.Types.Float , rank1 ); 
        errormessage = n3.getErrorMessage();   
        if( errormessage == "" ){
          // do nothing
        }else
          return true;
        //------------------ Write Errors ----------------
        NxWriteNode n4 = node.newChildNode( "errors", "SDS" );
       
          
        rank1 = new int[1];
        rank1[0] = 1;
        intval = new int[1];
        intval[0] = 1;
        n4.addAttribute("units",(DS.getY_units()+(char)0).getBytes(), 
                NexIO.Types.Char, NexIO.Inst_Type.makeRankArray(DS.getY_units().length()+1,
                                                    -1,-1,-1,-1));
    
        rank1 = new int[2];
        rank1[1] = ny_s;
        rank1[0] = endIndex - startIndex ;
        n4.setNodeValue( errors , NexIO.Types.Float , rank1 ); 
        errormessage = n4.getErrorMessage();   
        if( errormessage == "" ){
          // do nothing
        }else
          return true;
       
        xvalsPrev = xvals;
        startIndex = i;
        endIndex = -1;
        kk++;
      }//if endIndex > 0
  

    }//for i = 0 i < num entries
    return false;
  }

  //Obsolete
  private float[] MergeXvals ( int db , DataSet DS , float xvals[] ){
    if( db >= DS.getNum_entries() )
      return xvals; 
    if( db == 0 ){
      Data DB = DS.getData_entry(0 );
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
  
    while( ( i < xvals.length ) ||( j < xlocvals.length ) ){
      if( i >= xvals.length ){
        j++;
        n++;
      }else if( j >= xlocvals.length ){
        i++;
        n++;
      }else if( xvals[i] < xlocvals[j]-Delta ){
        i++;
        n++;
      }else if(xvals[i] > xlocvals[j]+Delta ){
        j++;
        n++;
      }else{
        i++;
        j++;
        n++;
      }
    }  
    
    float Res[];
    Res = new float[ n  ];
    j = 0;
    i = 0;
    n = 0;
    while( ( i < xvals.length ) ||( j < xlocvals.length ) ){
      if( i >= xvals.length ){
        Res[n] = xlocvals[j];j++;n++;
      }else if( j >= xlocvals.length ){
        Res[n] = xvals[i]; i++;n++;
      }else if( xvals[i] < xlocvals[j]-Delta ){
        Res[n] = xvals[i];i++; n++;
      }else if(xvals[i] > xlocvals[j]+Delta ){
        Res[n] = xlocvals[j];j++;n++;
      }else{
        Res[n] = ( xvals[i]+xlocvals[j] )/2.0f;i++;j++;n++;
      }
    }
   
    return MergeXvals ( db+1 ,  DS , Res );

  }

  /**
   * Obsolete :Use this routine to determine interval lengths for both
   * histgram and function data
   */
  private float intlength( float xvals[] , int intnum , boolean histogram ){
    int nfix = 1;
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
   * Obsolete Returns a rebinned value for this data blocks y values
   * wrt the xvals rebinning.  Assumes histogram so far
   */
  private float[] Rebinn( Data DB , float xvals[] ){
    errormessage = "null inputs to Rebinn";
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
    for( int i = 0 ; i < yvals.length ; i++ ){
      boolean done = false;
      while( !done ){
        Res[j] = yvals[i]*(xvals[j+1]-xvals[j])/(xlocvals[i+1]-xlocvals[i]);
        j++;
        if( j >= xvals.length - nfix )
          done = true;
        else if( xvals[j] >= xlocvals[ i + 1 ]- Delta )
          done = true;
      }
    } 
    return Res;  
  }

  /**
   *    Can this DataSet be made into a 2D or 3D Nexus NXdata.
   *    Extensions may allow for several 2D,3D etc, NXdata by returning
   *    int[] with int's of 0( no can do), or 1 ,2,3 which are tags for the
   *    Data blocks that can be grouped( TO DO)
   */
  public boolean Can2Dify(int[] grids , DataSet DS){
     
     if( grids == null)
       return false;
     if( grids.length < 1)
       return false;
    
     Data D = Grid_util.getAreaGrid(DS,grids[0]).getData_entry(1,1);
     if( D == null)
        return false;
     XScale xsc = D.getX_scale(); // should be the common Xscale for 
                                  // All data blocks in grid
     for( int grid = 0; grid < grids.length; grid++){
        IDataGrid Grid = Grid_util.getAreaGrid(DS,grids[grid]);
        if( Grid.num_rows() <=1) if(Grid.num_cols() <=1) 
           return false;
        for( int row =1; row <= Grid.num_rows(); row++)
           for( int col = 1; col <=Grid.num_cols(); col++){
              D = Grid.getData_entry(row,col);
              if( D == null)
                return false;
              if( D.getX_scale() != xsc)
                 return false;
           } 
      }
     return true;
  }
  //make links is false
  //  will create a 2D arrays for each ID then
  //  later will produce 1 3D array
  public boolean processDSgrid( NxWriteNode nodeEntr, NxWriteNode nxInstr, 
            DataSet DS, boolean makelinks){
     int[] grids =Grid_util.getAreaGridIDs( DS); 
     for( int i=0; i< grids.length; i++)
        if( processDSgrid1(nodeEntr, nxInstr, DS, makelinks, grids[i]))
           return true;
     return false;    

  }

  public boolean processDSgrid1( NxWriteNode nodeEntr, NxWriteNode nxInstr, 
            DataSet DS, boolean makelinks, int GridNum){
     IDataGrid grid = Grid_util.getAreaGrid( DS, GridNum);
     if( !grid.isData_entered())
       ((UniformGrid)grid).setData_entries( DS);
     NxWriteNode Nxdata = nodeEntr.newChildNode( DS.getTitle()+"_"+GridNum,
                                                   "NXdata");
     NxWriteNode Nxdetector = nxInstr.newChildNode( DS.getTitle()+"_"+GridNum,
                                         "NXdetector");
     //------------------ time_of_flight field -----------------------
     NxWriteNode tofnode = Nxdetector.newChildNode( "time_of_flight", "SDS");
     tofnode.addAttribute( "axis", NexIO.Inst_Type.makeRankArray(1,-1,-1,-1,-1),
           NexIO.Types.Int, NexIO.Inst_Type.makeRankArray(1,-1,-1,-1,-1));

     tofnode.addAttribute( "units", (DS.getX_units()+(char)0).getBytes(), NexIO.Types.Char,
          NexIO.Inst_Type.makeRankArray( DS.getX_units().length()+1,-1,-1,-1,-1));
     float[] offset = new float[1];
     offset[0] = 0.0f;
     tofnode.addAttribute( "histogram_offset", offset,
          NexIO.Types.Float,NexIO.Inst_Type.makeRankArray(1,-1,-1,-1,-1));
     float[] xvals = DS.getData_entry(0).getX_scale().getXs();
     tofnode.setNodeValue( xvals, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray(
           xvals.length,-1,-1,-1,-1));
     tofnode.setLinkHandle( "NXdata_"+GridNum);
     Nxdata.addLink( "NXdata_"+GridNum);

     //----------------------- axis 1 ------------------------------
     NxWriteNode xoffset = Nxdata.newChildNode( "x_offset", "SDS");
     xoffset.addAttribute( "axis", NexIO.Inst_Type.makeRankArray(2,-1,-1,-1,-1),
           NexIO.Types.Int, NexIO.Inst_Type.makeRankArray(1,-1,-1,-1,-1));
     xoffset.addAttribute( "units", (grid.units()+(char)0).getBytes(), NexIO.Types.Char,
          NexIO.Inst_Type.makeRankArray( grid.units().length()+1,-1,-1,-1,-1));
     xoffset.addAttribute("link",(DS.getTitle()+"_"+GridNum+(char)0).getBytes(),
            NexIO.Types.Char, NexIO.Inst_Type.makeRankArray( (DS.getTitle()+"_"+GridNum).
               length()+1,-1,-1,-1,-1));
     float[]  col_cm;
     
     col_cm = new float[ grid.num_cols()];
     for( int j = 0; j < grid.num_cols(); j++)
        col_cm[j] = grid.x( 1,j);
     xoffset.setNodeValue( col_cm, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray(
               col_cm.length,-1,-1,-1,-1));
     //--------------------------axis 2 --------------------------------
     NxWriteNode yoffset = Nxdata.newChildNode( "y_offset", "SDS");
     yoffset.addAttribute( "axis", NexIO.Inst_Type.makeRankArray(3,-1,-1,-1,-1),
           NexIO.Types.Int, NexIO.Inst_Type.makeRankArray(1,-1,-1,-1,-1));
     yoffset.addAttribute( "units", (grid.units()+(char)0).getBytes(), NexIO.Types.Char,
          NexIO.Inst_Type.makeRankArray( grid.units().length()+1,-1,-1,-1,-1));
     float[]  row_cm;
     
     row_cm = new float[ grid.num_rows()];
     for( int j = 0; j < grid.num_rows(); j++)
        row_cm[j] = grid.x( j,1);
     yoffset.setNodeValue( row_cm, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray(
               row_cm.length,-1,-1,-1,-1));

    //----------------------  data ---------------------------
     int ny_s= grid.getData_entry( 1, 1).getY_values().length;
     int numRows = grid.num_rows();
     int numCols = grid.num_cols();
     float[][][] data = new float[ grid.num_rows()][grid.num_cols()][(ny_s)];
     
     float[][][] errs = new float[ grid.num_rows()][grid.num_cols()][(ny_s)];
     for( int row =  1; row <= grid.num_rows(); row++)
        for( int col = 1; col <= grid.num_cols(); col++){
          int start = (row-1)*numCols*ny_s+col-1;
          Data db = grid.getData_entry( row, col);
          float[] yvalues =grid.getData_entry( row, col).getY_values();
          System.arraycopy( yvalues, 0, data[ row-1][col-1],0,ny_s);
          yvalues = grid.getData_entry(row,col).getErrors();
          System.arraycopy( yvalues, 0, errs[ row-1][col-1],0,ny_s);
     }
     NxWriteNode dataNode = Nxdata.newChildNode( "data", "SDS");
     dataNode.addAttribute( "signal", NexIO.Inst_Type.makeRankArray(1,-1,-1,-1,-1),
           NexIO.Types.Int, NexIO.Inst_Type.makeRankArray(1,-1,-1,-1,-1));
     dataNode.addAttribute( "units", (DS.getY_units()+(char)0).getBytes(), NexIO.Types.Char,
          NexIO.Inst_Type.makeRankArray( DS.getY_units().length()+1,-1,-1,-1,-1));
     
     dataNode.addAttribute( "label", (DS.getTitle()+(char)0).getBytes(), NexIO.Types.Char,
          NexIO.Inst_Type.makeRankArray( DS.getTitle().length()+1,-1,-1,-1,-1));
     dataNode.setNodeValue( data, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray(
               numRows,numCols,ny_s,-1,-1));

     dataNode = Nxdata.newChildNode( "errors", "SDS");
     dataNode.addAttribute( "units", (DS.getY_units()+(char)0).getBytes(), NexIO.Types.Char,
          NexIO.Inst_Type.makeRankArray( DS.getY_units().length()+1,-1,-1,-1,-1));
     dataNode.setNodeValue( errs, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray(
               numRows,numCols,ny_s,-1,-1));

    //-------------------NxDetector fields -----------------------------
    
     NxWriteNode distanceNode = Nxdetector.newChildNode( "distance", "SDS");
     Vector3D position = grid.position();
     float[] rtp  = (new Position3D( position)).getSphericalCoords();
     rtp = Types.convertToNexus(rtp[0],rtp[2],rtp[1]);              
     float[] res = new float[1]; res[0] = rtp[0];
     distanceNode.setNodeValue( res, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray(
           1,-1,-1,-1,-1));
     
     distanceNode.addAttribute( "units", (DS.getX_units()+(char)0).getBytes(),
         NexIO.Types.Char, NexIO.Inst_Type.makeRankArray( DS.getX_units().length()+1,
            -1,-1,-1,-1));

     NxWriteNode azimuthNode = Nxdetector.newChildNode( "azimuthal_angle", "SDS");
     res = new float[1]; res[0] = rtp[2];
     azimuthNode.setNodeValue( res, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray(
           1,-1,-1,-1,-1));
     azimuthNode.addAttribute( "units", ("radians"+(char)0).getBytes(),
         NexIO.Types.Char, NexIO.Inst_Type.makeRankArray( 7,
            -1,-1,-1,-1));

     NxWriteNode polarNode = Nxdetector.newChildNode( "polar_angle", "SDS");
     res = new float[1]; res[0] = rtp[1];
     polarNode.setNodeValue( res, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray(
           1,-1,-1,-1,-1));
     polarNode.addAttribute( "units", ("radians"+(char)0).getBytes(),
         NexIO.Types.Char, NexIO.Inst_Type.makeRankArray( 7,
            -1,-1,-1,-1));

     int[][] IDs = new int[ numRows][numCols];
     for( int i = 1; i <= numRows; i++)
       for( int j=1; j <= numCols; j++)
          IDs[ i-1][j-1]= grid.getData_entry(i,j).getGroup_ID();
     NxWriteNode idNode = Nxdetector.newChildNode( "id","SDS");
     idNode.setNodeValue( IDs, NexIO.Types.Int, NexIO.Inst_Type.makeRankArray( numRows,
         numCols,-1,-1,-1));

     res = new float[1];
     res[0] = grid.width();
     idNode = Nxdetector.newChildNode( "width","SDS");
     idNode.setNodeValue( res, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray( 1,
         -1,-1,-1,-1));

     res = new float[1];
     res[0] = grid.height();
     idNode = Nxdetector.newChildNode( "height","SDS");
     idNode.setNodeValue( res, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray( 1,
         -1,-1,-1,-1));

     res = new float[1];
     res[0] = grid.depth();
     idNode = Nxdetector.newChildNode( "depth","SDS");
     idNode.setNodeValue( res, NexIO.Types.Float, NexIO.Inst_Type.makeRankArray( 1,
         -1,-1,-1,-1));
     
     
    

    
    float solidAngle[][] = new float[numRows][numCols];
    float rawAngle[][]   = new float[numRows][numCols];
    float Tot_Count[][]  = new float[numRows][numCols];

    int[][] slot         = new int[numRows][numCols];
    int[][] crate        = new int[numRows][numCols];
    int[][] input        = new int[numRows][numCols];
    Object XX;
    for( int row = 1; row <= numRows; row++)
      for( int col = 1;col <= numCols; col++){
      Data DB = grid.getData_entry( row,col);
      XX = DB.getAttributeValue( Attribute.SOLID_ANGLE );
      if( XX == null) 
           solidAngle = null;
      if( (XX != null) &&(solidAngle != null) ){
        
        float F = ConvertDataTypes.floatValue( XX );
        if( Float.isNaN(F) )
          solidAngle[ row-1][col-1] = F;
        else 
          solidAngle[  row-1][col-1 ] = -1.0f;
      }else if( solidAngle != null )
        solidAngle[  row-1][col-1 ] = -1.0f;

     
      XX = DB.getAttributeValue( Attribute.RAW_ANGLE );
      if( XX == null)
          rawAngle = null;
      if(( XX != null)&&(rawAngle != null) ){
       
        float F = ConvertDataTypes.floatValue( XX );
        if(Float.isNaN(F) )
          rawAngle[ row-1][col-1] = F;
        else 
          rawAngle[  row-1][col-1 ] = -1.0f;
      }
      else if( rawAngle != null )
        rawAngle[ row-1][col-1 ] = -1.0f;

      XX = DB.getAttributeValue( Attribute.TOTAL_COUNT );
      if( XX == null)
           Tot_Count = null;
      if(( XX != null)&&(Tot_Count != null) ){
        
        float  F = ConvertDataTypes.floatValue(  XX );
        if( Float.isNaN(F) )
          Tot_Count[row-1][col-1  ] = -1.0f;
        else if( F >= 0 )
          Tot_Count[row-1][col-1  ] = F;
        else 
          Tot_Count[row-1][col-1  ] = -1.0f;
      }else if( Tot_Count != null )
        Tot_Count[ row-1][col-1  ] = -1.0f;

      //slot----
      XX = DB.getAttributeValue( Attribute.SLOT );
      if( XX == null)slot = null;
      if( (XX != null)&&(slot != null) ){
        
        float[] F = ConvertDataTypes.floatArrayValue( XX );
        slot[ row-1][col-1 ] = -1;
        if( F != null )
          if(F.length >0)
            slot[row-1][col-1 ] = (int)(F[0]);
        
      }else if( slot != null )
        slot[ row-1][col-1 ] = -1;

      //crate
      XX = DB.getAttributeValue( Attribute.CRATE );
      if( XX == null) crate = null;
      if( (XX != null)&&(crate != null) ){
        
        float[] F =  ConvertDataTypes.floatArrayValue( XX );
        crate[row-1][col-1 ] = -1;
        if( F != null )
          if(F.length >0)
            crate[row-1][col-1 ] =(int)( F[0]);
      }else if( crate != null )
        crate[row-1][col-1 ] = -1;

      //input
      XX = DB.getAttributeValue( Attribute.INPUT );
      if( XX == null) input = null;
      if( (XX != null)&&(input != null) ){
       
        float[] F = ConvertDataTypes.floatArrayValue( XX );
        input[row-1][col-1 ] = -1;
        if( F != null )
          if(F.length >0)
            input[ row-1][col-1 ] = (int)(F[0]);
      }else if( input != null )
        input[row-1][col-1 ] = -1;
     }//For all groups

     
    if( slot != null){
      NxWriteNode nn = Nxdetector.newChildNode("slot","SDS");
      nn.setNodeValue( slot,NexIO.Types.Int,
                       NexIO.Inst_Type.makeRankArray( slot.length,-1,-1,-1,-1));
      
    }

    if( crate != null){
      NxWriteNode nn = Nxdetector.newChildNode("crate","SDS");
      nn.setNodeValue( crate,NexIO.Types.Int,
                       NexIO.Inst_Type.makeRankArray( crate.length,-1,-1,-1,-1));
     
    }
    if( input != null){
      NxWriteNode nn = Nxdetector.newChildNode("input","SDS");
      nn.setNodeValue( input,NexIO.Types.Int,
                       NexIO.Inst_Type.makeRankArray( input.length,-1,-1,-1,-1));
      //nn.addAttribute("units", ("radians"+(char)0).getBytes(),NexIO.Types.Char,
      //NexIO.Inst_Type.makeRankArray(8,-1,-1,-1,-1));
    }
    if( solidAngle != null){
      NxWriteNode nn = Nxdetector.newChildNode("input","SDS");
      nn.setNodeValue( solidAngle,NexIO.Types.Float,
                       NexIO.Inst_Type.makeRankArray( solidAngle.length,-1,-1,-1,-1));
      nn.addAttribute("units", ("radians"+(char)0).getBytes(),NexIO.Types.Char,
      NexIO.Inst_Type.makeRankArray(8,-1,-1,-1,-1));
    }
    if( rawAngle != null){
      NxWriteNode nn = Nxdetector.newChildNode("input","SDS");
      nn.setNodeValue( rawAngle,NexIO.Types.Float,
                       NexIO.Inst_Type.makeRankArray(rawAngle.length,-1,-1,-1,-1));
     nn.addAttribute("units", ("degrees"+(char)0).getBytes(),NexIO.Types.Char,
      NexIO.Inst_Type.makeRankArray(8,-1,-1,-1,-1));
    }

    if( Tot_Count != null){
      NxWriteNode nn = Nxdetector.newChildNode("input","SDS");
      nn.setNodeValue( Tot_Count,NexIO.Types.Int,
                       NexIO.Inst_Type.makeRankArray( Tot_Count.length,-1,-1,-1,-1));
    
    }

   //--------------------- Orientation ------------------------------

   float[] orientation = new float[3];
   Vector3D xdir = grid.x_vec();
   Vector3D ydir = grid.y_vec();
   Vector3D planeNormal = new Vector3D();
   planeNormal.cross( ydir, xdir);
   float[] coords = planeNormal.get();
   float DxyPlaneNormal = (float)java.lang.Math.sqrt(coords[1]*coords[1]+ 
                                coords[2]*coords[2]);
  // ----------------------- phi1 ----------------------
   float phi1 = 0;
   if( DxyPlaneNormal >0)
       phi1 = (float)java.lang.Math.acos( coords[2]/DxyPlaneNormal);
   phi1 = (float)( java.lang.Math.atan2(coords[1],coords[2]));
   Tran3D Matrix = new Tran3D();
   Matrix.setRotation( phi1*180/(float)java.lang.Math.PI, new Vector3D( 1,0,0));
   Vector3D resx= new Vector3D();
   Matrix.apply_to( planeNormal, resx);
   Vector3D xvecT = new Vector3D(), yvecT = new Vector3D();
   Matrix.apply_to( xdir, xvecT);
   Matrix.apply_to( ydir, yvecT);

   //---------------------------- theta ---------------------
   coords = resx.get();
   
   float DyzPlaneNormalT= (float)java.lang.Math.sqrt( coords[0]*coords[0]+ 
                              coords[2]*coords[2]);
   float theta = 0;
   if( DyzPlaneNormalT > 0)
       theta = (float)java.lang.Math.acos( coords[0]/DyzPlaneNormalT);
   theta = (float)( java.lang.Math.atan2(coords[2],coords[0]));
  
   
   Tran3D next = new Tran3D();
   next.setRotation( theta*180/(float)java.lang.Math.PI, new Vector3D( 0,1,0));
   
   next.multiply_by( Matrix);
   Matrix = next;
   Vector3D res1 = new Vector3D();
   next.apply_to( resx,res1);

   res1 = new Vector3D();
   Matrix.apply_to( planeNormal, res1);
   
   Matrix.apply_to( xdir, xvecT);
   Matrix.apply_to( ydir, yvecT);
   
   
  //--------------------------- phi2 ---------------
   
   xvecT = new Vector3D();
   Matrix.apply_to( xdir, xvecT);
   coords =xvecT.get();

   float phi2 = (float)java.lang.Math.acos( xvecT.dot( new Vector3D(0,-1,0))/
                          xvecT.length());

   phi2 = (float) java.lang.Math.atan2(coords[2], -coords[1]);
   next = new Tran3D();
   next.setRotation( phi2*180/(float)java.lang.Math.PI, new Vector3D( 1,0,0));
   next.multiply_by( Matrix);
   Matrix = next;
 
   Matrix.apply_to( xdir, xvecT);
   Matrix.apply_to( ydir, yvecT);
   NxWriteNode orient = Nxdetector.newChildNode( "orientation","SDS");
   orientation =  new float[3];
   orientation[2] = -phi1;
   orientation[1] = -theta;
   orientation[0] = -phi2;
   orient.setNodeValue( orientation,NexIO.Types.Float,
                       NexIO.Inst_Type.makeRankArray( 3,-1,-1,-1,-1));
   
   
  return false;
 
 }
  public static void main( String args[] ){
    NxWriteData nw = new NxWriteData(1);
    IsawGUI.Util ut = new IsawGUI.Util();
    DataSet dss[];
    dss = ut.loadRunfile( "C:\\SampleRuns\\gppd9898.run" );
   
    float xvals[];
    xvals = nw.MergeXvals( 0, dss[1] , null );
    System.out.println(  "*****final xval list= *****" );
    System.out.println( StringUtil.toString( xvals ));
    Data DB = dss[1].getData_entry( 0 );
    float ynew[] , xold[] , yold[];
    ynew = nw.Rebinn( DB , xvals );
    XScale XX = DB.getX_scale();
    xold = XX.getXs();
    yold = DB.getY_values();
    System.out.println( "xold= "+StringUtil.toString( xold ) );
    System.out.println( "" );
    System.out.println( "xbew= "+StringUtil.toString( xvals ) );
    System.out.println( "" );
    System.out.println( "yold= "+StringUtil.toString( yold ) );
    System.out.println( "" );
    System.out.println( "ynew= "+StringUtil.toString( ynew ) );
    System.out.println( "" );
  }
}
