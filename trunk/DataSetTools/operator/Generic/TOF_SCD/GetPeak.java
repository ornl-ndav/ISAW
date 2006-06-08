/*
 * File:  GetPeak.java 
 *             
 * Copyright (C) 2006, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.4  2006/06/08 15:55:12  rmikk
 * Added GPL
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import java.lang.reflect.Array;

import Command.ScriptUtil;
import Command.Script_Class_List_Handler;
import DataSetTools.dataset.*;


public class GetPeak {
	public static boolean debug = false; //level 1-Macro results, level 2-adds info about each cell 
    public GetPeak() { 
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Calculates the information to find the extent, sum, center(weighted and non weighted of
     * a peak around the point in question
     * @param row    A row inside the peak
     * @param col    a col inside the peak
     * @param timeChan  a time channel inside the peak
     * @param Detnum    The detector number associated with the row/col
     * @param DS         the data set with the peak in it
     * @return          The block of information to calculate the information needed.
     */
    public static PeakInfo getPeakInfo( int row, int col, int timeChan,int DetID, 
                                                     DataSet DS, float backIntensity){
        if(Float.isNaN(backIntensity)){
            ScriptUtil.display("The background Intensity is NaN in getPeakInfo");
            return null;
        }
        IDataGrid grid = Grid_util.getAreaGrid(DS, DetID);
        if( grid == null){
            ScriptUtil.display("There is no "+DetID+" for the DataSet "+DS+" in getPeakInfo");
            return null;
        }
        PeakInfo Pinf = new PeakInfo( DetID, grid, backIntensity, DS);
        if( debug)
        	Pinf.debug = true;
        int[] posMax= new int[6];
        posMax[0]=-1;//y pos plus 1 line of max 
        posMax[1] =-1;//y pos minus 1 line
        posMax[2] =-1;//x pos plus 
        posMax[3] =-1;//y pos plus 
        posMax[4] =-1;//x pos minus 
        posMax[5] =-1;//x pos minus
        float[] MaxVal = new float[1];
        MaxVal[0] =Float.MIN_VALUE;
        return RecGetPeakInfo(row,col,timeChan, grid, 3,3,3,backIntensity,posMax,Pinf);
        
    }
    
    private static PeakInfo RecGetPeakInfo( int row, int col, int timeChan,IDataGrid grid, int xdir, int ydir, int zdir,
                            float backIntensity,int[] posMax,PeakInfo Pinf){
        if( row <1) return Pinf;
        if( col < 1) return Pinf;
        if( row > grid.num_rows())return Pinf;
        if( col > grid.num_cols()) return Pinf;
        if( timeChan <0) return Pinf;
        Data D = grid.getData_entry(row,col);
        if( D==null)return Pinf;
        if(D.getX_scale()==null)return Pinf;
        if( timeChan >= D.getX_scale().getNum_x()) return Pinf;
        float intensity= grid.getData_entry(row,col).getY_values()[timeChan];
        
        if( !Pinf.addPeak(row,col,timeChan,intensity))
            return Pinf;
        
        posMax=update(grid,posMax, row,col,timeChan,0,1);
        posMax=update(grid,posMax,row,col,timeChan,0,-1);
        posMax = update( grid, posMax, row, col,timeChan, 1,0);
        posMax = update( grid, posMax, row, col,timeChan, -1,0);
        if( xdir >=2){//go right
            
            RecGetPeakInfo( row,col+1,timeChan,grid,2,0,0,backIntensity,posMax,Pinf);
            xdir -=2;
        }
        if(xdir >=1){

            
            RecGetPeakInfo( row,col-1,timeChan,grid,1,0,0,backIntensity,posMax,Pinf);
            xdir -=1;
        }
        int colplus = posMax[0], colminus=posMax[1];

        if( ydir >=2){
            posMax[0]=posMax[1]=-1;
            RecGetPeakInfo( row+1,colplus,timeChan,grid,3,2,0,backIntensity,posMax,Pinf);
            
            ydir -=2;            
        }

        if( ydir >=1){
            posMax[0]=posMax[1]=-1;
            RecGetPeakInfo( row-1,colminus,timeChan,grid,3,1,0,backIntensity,posMax,Pinf);
            
                        
        }   
        
        int zplusy=posMax[2];
        int zplusx=posMax[3];
        int zminusy=posMax[4];
        int zminusx=posMax[5];
        
        
        if( zdir >=2){
            
            java.util.Arrays.fill(posMax,-1);
            RecGetPeakInfo( zplusy,zplusx,timeChan+1,grid,3,3,2,backIntensity,posMax,Pinf);
            zdir -=2;
        }

        if( zdir >=1){
            java.util.Arrays.fill(posMax,-1);
            RecGetPeakInfo( zminusy,zminusx,timeChan-1,grid,3,3,1,backIntensity,posMax,Pinf);
            zdir -=1;
        }
        return Pinf;
    }
    //diry and dirz are 0,1,or -1.  Only one can be nonzero
    private static int[] update( IDataGrid grid, int[] posMax, int thisrow, int thiscol,int thischan,
              int diry, int dirz){
        if( thisrow+diry <1)return posMax;
        if( thischan+dirz <0)return posMax;
        if( thisrow+diry > grid.num_rows())return posMax;
        
        if( thischan+dirz > grid.getData_entry( thisrow+diry, thiscol).getX_scale().getNum_x())
             return posMax;
        
        if( posMax[0] <0)if( diry !=0){
            posMax[0] =thiscol;
            return posMax;
        }
        if( posMax[1] <0)if( diry !=0){
            posMax[1] =thiscol;
            return posMax;
        }

        if( posMax[2] <0)if( dirz !=0){
            posMax[2] =thisrow;
            posMax[3] =thiscol;
            return posMax;
        }

        if( posMax[4] <0)if( dirz !=0){
            posMax[4] =thisrow;
            posMax[5] =thiscol;
            return posMax;
        }
        if( diry !=0){
            int k=0; if(diry < 0) k=1;
            if( grid.getData_entry( thisrow+diry,thiscol).getY_values()[thischan] >
                grid.getData_entry( thisrow+diry,posMax[k]).getY_values()[thischan])
                posMax[k]= thiscol;
            return posMax;
            
        }
        if( dirz ==0) return posMax;
        int k=2;
        if( dirz <0)k=4;
        if( grid.getData_entry( thisrow,thiscol).getY_values()[thischan+dirz] >
        grid.getData_entry( posMax[k],posMax[k+1]).getY_values()[thischan+dirz]){
        posMax[k]= thisrow;
        posMax[k+1] = thiscol;
        }
        return posMax;
    }
    public static void ShowUsage(){
        System.out.println("Enter the name of the file with the data set and the data set number");
        System.exit(0);
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
       if( (args==null) ||(args.length<2))
               GetPeak.ShowUsage();
       DataSet DS=null;
       try{
          DataSet[] DSS =ScriptUtil.load( args[0]);
          DS = DSS[ (new Integer(args[1])).intValue()];
       }catch(Exception s){
           s.printStackTrace();
           System.exit(0);
       }
       int row,col,timeChan,DetID;
       float backIntensity;

       row=95; col=95; timeChan=11;DetID=19;backIntensity=2f;
       while(true)
       {
           try{
               System.out.print("Enter Row:");
               row = (new Integer(Script_Class_List_Handler.getString())).intValue();
               System.out.print("Enter Col:");
               col = (new Integer(Script_Class_List_Handler.getString())).intValue();
               System.out.print("Enter chan:");
               timeChan = (new Integer(Script_Class_List_Handler.getString())).intValue();
               System.out.print("Enter detID:");
               DetID = (new Integer(Script_Class_List_Handler.getString())).intValue();
               System.out.print("Enter backInt:");
               backIntensity = (new Float(Script_Class_List_Handler.getString())).floatValue();
               
           }catch(Exception s){
               row=col=timeChan=DetID=-1;
               backIntensity = Float.NaN;
           }
           GetPeak.debug = true;
           PeakInfo pk = GetPeak.getPeakInfo(row,col,timeChan,DetID,DS,backIntensity);
           if( pk == null)
               System.out.println("Result is null");
           else{
               System.out.println("Total Intensitywo/w background="+pk.TotIntensity+","+pk.getTotIntensity());
               System.out.println("Max extent= x:"+pk.minX+"-"+pk.maxX+";y:"+pk.minY+"-"+pk.maxY+";z:"+
            		   pk.minZ+"-"+pk.maxZ+"; TotExtentIntensity="+
                        pk.TotExtentIntensity);

               
               System.out.println("Middle pos ="+pk.getWeightedAverageCol()+","+pk.getWeightedAverageRow()+","+
                       pk.getWeightedAverageChan());
               System.out.println("number of cells/background intensity/cell="+pk.getNCells()+","+pk.background);
           }
       
       }
    }

}
