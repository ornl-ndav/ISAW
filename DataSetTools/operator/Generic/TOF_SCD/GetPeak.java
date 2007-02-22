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
 * Revision 1.8  2007/02/22 16:28:54  rmikk
 * Now uses the  IGrid3D structure instead of the IDataGrid structure
 *
 * Revision 1.7  2006/06/16 18:21:44  rmikk
 * Did some more bounds checking
 *
 * Revision 1.6  2006/06/10 23:22:46  rmikk
 * Removed a statement designed to give an error
 *
 * Revision 1.5  2006/06/10 21:39:09  rmikk
 * Added documentation
 *
 * Revision 1.4  2006/06/08 15:55:12  rmikk
 * Added GPL
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;



import Command.ScriptUtil;
import Command.Script_Class_List_Handler;
import DataSetTools.dataset.*;

/**
 *  This class find a peak by growing it from a point first in the x direction
 *  then in the y directions and finally in the z directions. 
 * @author Ruth
 *
 */
public class GetPeak {
	
	public static boolean debug = false; //level 1-Macro results, level 2-adds info about each cell 
	
	/**
	 * Constructor
	 * Not used because all the methods are static
	 *
	 */
    public GetPeak() { 
        super();
        
    }
    
    /**
     * Finds the peakInfo that contains information about one peak.  This 
     * information can be used to find the centroid, background, intensities
     * @param row    A row inside the peak
     * @param col    a col inside the peak
     * @param timeChan  a time channel inside the peak
     * @param DetID   The detector number associated with the row/col
     * @param DS         the data set with the peak in it
     * @param backIntensity  The cutoff value to determine when a cell is added
     *                  to the peak. The average cell intensity of the cell and
     *                  its nearest neighbbor must exceed this cutoff to be 
     *                  included
     * @param  PeakSpan  The max extent of peak in row,col, and time channel
     *                   or -1 if there is none.
     *                                   
     * @return          The block of information to calculate the information 
     *                   needed.
     */
    public static PeakInfo getPeakInfo( int row , int col , int timeChan ,  
                                                     IGrid3D grid , float backIntensity ,int PeakSpan){
       
       if( Float.isNaN( backIntensity) ){
          ScriptUtil.display( "The background Intensity is NaN in getPeakInfo");
          return null;
      }
      
     
      PeakInfo Pinf = new PeakInfo( grid, backIntensity  );
      Pinf.setMaxExtent( PeakSpan );
      if( debug )
         Pinf.debug = true;
      int[] posMax = new int[ 6 ];
   
   
      posMax[ 0 ] = -1;//y pos plus 1 line of max 
      posMax[ 1 ]  = -1;//y pos minus 1 line
      posMax[ 2 ] = -1;//x pos plus 
      posMax[ 3 ] = -1;//y pos plus 
      posMax[ 4 ] = -1;//x pos minus 
      posMax[ 5 ] = -1;//x pos minus
      float[] MaxVal = new float[ 1 ];
      MaxVal[ 0 ] = Float.MIN_VALUE;
      return RecGetPeakInfo( row , col , timeChan , grid ,  3 , 3 , 3 , backIntensity , posMax , Pinf );
  
       
    }
    
    
    /**
     * Finds the peakInfo that contains information about one peak.  This 
     * information can be used to find the centroid, background, intensities
     * @param row    A row inside the peak
     * @param col    a col inside the peak
     * @param timeChan  a time channel inside the peak
     * @param DetID   The detector number associated with the row/col
     * @param DS         the data set with the peak in it
     * @param backIntensity  The cutoff value to determine when a cell is added
     *                  to the peak. The average cell intensity of the cell and
     *                  its nearest neighbbor must exceed this cutoff to be 
     *                  included
     *                  
     * @return          The block of information to calculate the information 
     *                   needed.
     */
    public static PeakInfo getPeakInfo( int row , int col , int timeChan , int DetID , 
                                                     DataSet DS , float backIntensity ,int PeakSpan){
        DSGrid ds_grid =new DSGrid( DS, DetID);
    	  PeakInfo pk = getPeakInfo( row,col,timeChan,ds_grid , backIntensity, PeakSpan);
        if( pk == null) return null;
        pk.DS =DS;
        pk.grid =ds_grid.grid;
        return pk;
            
    }
    
    
    
    
    // Recursively grow the peak first in the x direction, then y direction then finally z direction. The growth
    //   in any direction continues until the next cell 's average intensity with its nearest neighbors does
    //   does not excede backIntensity
    //@param row , col, timeChan   the starting cell for growing the peak
    //@param grid   The grid for getting values
    //@param xdir  0 means do not grow, 2 mins to grow in positive x direction, 1 means grow in negative x direction
    //              3 means grow in both directions
    //@param ydir, zdir  See xdir
    //@param backIntensity  the cutoff point for the average intensity of a cell and nearest neighbors on this time
    //                    slice  to be included in a peak
    //@param  posMax  An array containing the positions of the max's in the other directions
    //               posMax[0]- x position of strongest cell in positive y direction from this cell
    //               posMax[2]- x position of strongest cell in negative y direction from this cell
    //               posMax[3]- x position of strongest cell in positive z direction from this cell
    //               posMax[4]- x position of strongest cell in negative z direction from this cell
    //               posMax[5]- y position of strongest cell in positive z direction from this cell
    //               posMax[6]- y position of strongest cell in negative z direction from this cell
    private static PeakInfo RecGetPeakInfo( int row , int col , int timeChan , IGrid3D grid , int xdir , int ydir , int zdir ,
                            float backIntensity , int[] posMax , PeakInfo Pinf ){
        if( row < 1 ) 
        	return Pinf;
        if( col < 1 ) 
        	return Pinf;
        if( row > grid.num_rows() )
        	return Pinf;
        if( col > grid.num_cols() ) 
        	return Pinf;
        if( timeChan < 0 ) 
        	return Pinf;
        
        int nchans = grid.num_channels( row , col );
        if(nchans <=0 )
        	return Pinf;
       
        if( timeChan >= grid.num_channels( row, col) ) 
        	return Pinf;

        float intensity = grid.intensity( row , col, timeChan );
        
        if( !Pinf.addPeak( row , col , timeChan , intensity ) )
            return Pinf;
    
        
        // Input values are in range so now proceed
        
        posMax = update( grid , posMax , row , col , timeChan , 0 , 1 );
        posMax = update( grid , posMax , row , col , timeChan , 0 , -1 );
        posMax = update( grid , posMax , row , col , timeChan , 1 , 0 );
        posMax = update( grid , posMax , row , col , timeChan ,  -1 , 0 );
        
        if( xdir >= 2 ){//go right only
            
            RecGetPeakInfo( row , col + 1 , timeChan , grid , 2 , 0 , 0 , backIntensity , posMax , Pinf );
            xdir -= 2;
        }
        
        
        if( xdir >= 1 ){//go left only

            
            RecGetPeakInfo( row , col - 1 , timeChan , grid , 1 , 0 , 0 , backIntensity , posMax , Pinf );
            xdir -= 1;
        }
        
        
        int colplus = posMax[ 0 ] , 
            colminus = posMax[ 1 ];

        if( ydir >= 2 ){ //go in pos y dir only and both directions in x dir
            posMax[ 0 ] = posMax[ 1 ] = -1;
            RecGetPeakInfo( row + 1 , colplus , timeChan , grid , 3 , 2 , 0 , backIntensity , posMax , Pinf );
            
            ydir -= 2;            
        }

        if( ydir >= 1 ){//go down in the y direction
            posMax[ 0 ] = posMax[ 1 ] = -1;
            RecGetPeakInfo( row - 1 , colminus , timeChan , grid , 3 , 1 , 0 , backIntensity , posMax , Pinf );
                       
        }   
        
        int zplusy = posMax[ 2 ];
        int zplusx = posMax[ 3 ];
        int zminusy = posMax[ 4 ];
        int zminusx = posMax[ 5 ];
        
        
        if( zdir >= 2 ){ //go in the positive z direction only
            
            java.util.Arrays.fill( posMax ,  -1 );
            RecGetPeakInfo( zplusy , zplusx , timeChan + 1 , grid , 3 , 3 , 2 , backIntensity , posMax , Pinf );
            zdir -= 2;
        }

        if( zdir >= 1 ){  //go in the negative z direction
            java.util.Arrays.fill( posMax , -1 );
            RecGetPeakInfo( zminusy , zminusx , timeChan - 1 , grid , 3 , 3 , 1 , backIntensity , posMax , Pinf );
            zdir -= 1;
        }
        
        return Pinf;
    }
    
    
  
    
    
    
    // diry and dirz are 0,1,or -1.  Only one can be nonzero
    // updates the position of the max in the next direction
    private static int[] update( IGrid3D grid , int[] posMax , int thisrow , int thiscol , int thischan ,
              int diry , int dirz ){
    	if( thiscol < 1)
    		return posMax;
    	if( thiscol > grid.num_cols())
    		return posMax;
    	if( thisrow < 1)
    		return posMax;
    	
    	if( thisrow > grid.num_rows())
    		return posMax;
    	if( thisrow + diry <1)
   		   return posMax;
   	    if( thisrow+diry > grid.num_rows())
   		   return posMax;
    	if( thischan < 0)
    		return posMax;
    	 if( thischan > grid.num_channels( thisrow + diry ,  thiscol ))
             return posMax;
        if( thischan + dirz < 0 )
        	return posMax;
        
        if( thischan + dirz > grid.num_channels( thisrow + diry ,  thiscol ) )
             return posMax;
        
        if( posMax[ 0 ] < 0 )if( diry != 0 ){
            posMax[ 0 ] = thiscol;
            return posMax;
        }
        if( posMax[ 1 ] < 0 )if( diry != 0 ){
            posMax[ 1 ] = thiscol;
            return posMax;
        }

        if( posMax[ 2 ] < 0 )if( dirz != 0 ){
            posMax[ 2 ] = thisrow;
            posMax[ 3 ] = thiscol;
            return posMax;
        }

        if( posMax[ 4 ] < 0 )if( dirz != 0 ){
            posMax[ 4 ] = thisrow;
            posMax[ 5 ] = thiscol;
            return posMax;
        }
        
        if( diry != 0 ){
            int k = 0; if( diry < 0 ) k = 1;
            if( grid.intensity( thisrow + diry , thiscol,  thischan ) >
                grid.intensity( thisrow + diry , posMax[ k ],  thischan ) )
                posMax[ k ] = thiscol;
            return posMax;
            
        }
        
        if( dirz == 0 ) 
        	return posMax;
        
        int k = 2;
        if( dirz < 0 )
        	k = 4;
        if( grid.intensity( thisrow , thiscol,  thischan + dirz ) >
                       grid.intensity( posMax[ k ] , posMax[ k + 1 ] , thischan + dirz ) ){
           posMax[ k ] = thisrow;
           posMax[ k + 1 ] = thiscol;
        }
        
        
        return posMax;
        
    }
  
    
    
    
    /*
     * Show the arguments needed on the command line to run this program 
     */
    public static void ShowUsage(){
        System.out.println("Enter the name of the file with the data set and the data set number" );
        System.exit(0 );
    }
    
    
   
    
    /**
     * Test program for this class
     * @param args  Enter none to see what the arguments should be
     */
    public static void main( String[] args ) {
    	
       if( (args == null ) ||(args.length < 2 ) ){
    	   
            GetPeak.ShowUsage();
            System.exit(0 );
       }
       
       
       DataSet DS = null;
       try{
    	   
          DataSet[] DSS = ScriptUtil.load( args[ 0 ] );
          DS = DSS[  ( new Integer( args[ 1 ] ) ).intValue() ];
          
       }catch( Exception s ){
    	   
           s.printStackTrace();
           System.exit( 0 );
           
       }
       
       
       int row ,
           col ,
           timeChan ,
           DetID;
       
       float backIntensity;

       row = 95; 
       col = 95; 
       timeChan = 11;
       DetID = 19;
       backIntensity = 2f;
       
       while( true )
       {
           try{
        	   
               System.out.print( "Enter Row:" );
               row = ( new Integer( Script_Class_List_Handler.getString() ) ).intValue();
               
               System.out.print( "Enter Col:" );
               col = ( new Integer( Script_Class_List_Handler.getString() ) ).intValue();
               
               System.out.print( "Enter chan:" );
               timeChan = ( new Integer( Script_Class_List_Handler.getString() ) ).intValue();
               
               System.out.print( "Enter detID:" );
               DetID = ( new Integer( Script_Class_List_Handler.getString() ) ).intValue();
               
               System.out.print( "Enter backInt:" );
               backIntensity = ( new Float( Script_Class_List_Handler.getString() ) ).floatValue();
               
           }catch( Exception s ){
        	   
               row = col = timeChan = DetID = -1;
               backIntensity = Float.NaN;
           }
           
           GetPeak.debug = true;
           PeakInfo pk = GetPeak.getPeakInfo( row , col , timeChan , DetID , DS , backIntensity, -1 );
           
           if( pk == null )
               System.out.println( "Result is null" );
           else{
        	   
               System.out.println( "Total Intensitywo/w background=" + pk.TotIntensity + "," + pk.getTotIntensity() );
               System.out.println( "Max extent= x:" + pk.minX + "-" + pk.maxX + ";y:" + pk.minY + "-" + pk.maxY + ";z:" + 
            		   pk.minZ + "-" + pk.maxZ + "; TotExtentIntensity=" + 
                        pk.TotExtentIntensity );

               
               System.out.println( "Middle pos =" + pk.getWeightedAverageCol() + "," + pk.getWeightedAverageRow() + "," + 
                       pk.getWeightedAverageChan() );
               System.out.println( "number of cells/background intensity/cell=" + pk.getNCells() + "," +  pk.background );
           }
       
       }
    }
  
   
}





