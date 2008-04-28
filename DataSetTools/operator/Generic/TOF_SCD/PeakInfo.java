/*
 * File:  PeakInfo.java 
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
 * Revision 1.11  2007/04/27 12:58:00  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.10  2007/02/22 16:24:55  rmikk
 * Replaced IDataGrid by IGrid3D so this can be used by other detector
 *    type information(i.e. arrays)
 *
 * Revision 1.9  2006/11/14 01:58:47  dennis
 * Fixed java doc error.
 *
 * Revision 1.8  2006/06/26 20:16:07  rmikk
 * Found the max intensity of a peak and reported it a peak.IOBS
 * Subtracted the background intensity from the weighted x,y,z, and
 *    intensity values instead of the cutoff intensity
 *
 * Revision 1.7  2006/06/16 18:22:50  rmikk
 * Introduced code that sill not allow a peak to have and extend greater than
 *    40% of the max for row and col and 20% of max for time
 *
 * Revision 1.6  2006/06/10 21:38:44  rmikk
 * Added a lot more documentation
 *
 * Revision 1.5  2006/06/09 04:15:21  rmikk
 * Fixed an error due to the position of the Initial Path length attribute in
 *   a data set
 *
 * Revision 1.4  2006/06/08 15:55:12  rmikk
 * Added GPL
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;


/**
 * This class contains the statistics for a peak and a method to add a cell to 
 * a peak and methods to retrieve statistics about the peak and background
 * 
 *  Ruth
 *
 */
public class PeakInfo {
	public DataSet DS;
	SampleOrientation sampOrient;
	XScale xscl;
	float initialPath;
	float T0;
	
	
    int ncells;
    public int maxX,
               maxY,
               maxZ;
    public int minX,
               minY,
               minZ;
    int sumX,
        sumY,
        sumZ;
    float WsumX,
          WsumY,
          WsumZ,
          MaxIntensity;
    float Wx,
          Wy,
          Wz;
    float TotIntensity;
    int detNum;
    IDataGrid grid;
    float TotExtentIntensity;
    float background;
    public float backgroundIntensity;
    
    public boolean debug = false;
    int maxChannels,
        maxRows,
        maxCols;
    IGrid3D C_Grid;
    
    int MaxExtent = -1;
    int startRow = -1,
        startCol = -1,
        startChan = -1;
    public PeakInfo( IGrid3D grid, float backgroundIntensity){

       ncells = 0;
       maxX = maxY = maxZ = - 1;
       sumX = sumY = sumZ = 0;
       minX = minY = minZ = Integer.MAX_VALUE;
       WsumX = WsumY = WsumZ = Wx = Wy = Wz = TotIntensity = 
         MaxIntensity = TotExtentIntensity = 0; 
       if( !Float.isNaN( backgroundIntensity ) && ( backgroundIntensity  > 0 ) )
            
          background = this.backgroundIntensity = backgroundIntensity;
      
      else{
         
          backgroundIntensity = 0f;
          background = 0f;
          
      }
      C_Grid = grid;
      DS =null;
      xscl = null;
      initialPath = Float.NaN;
      T0 = Float.NaN;
      
      maxRows = C_Grid.num_rows();
      maxCols = C_Grid.num_cols();
      maxChannels = C_Grid.num_channels(1,1);
    }
    /**
     * Constructor
     * @param detNum     The detector number
     * @param grid       The grid with the data
     * @param backgroundIntensity    The cutoff point for average intensity of nearest neighbors
     * @param DS                     The DataSet
     */
    public PeakInfo( int detNum , IDataGrid grid , float backgroundIntensity , DataSet DS ) {
    	
        this( new DSGrid(DS, detNum),backgroundIntensity);
        this.detNum  = detNum;
        this.grid = grid;
        
      
        
        this.DS = DS;
    	sampOrient = null;
    	xscl = null;
    	initialPath = Float.NaN;
    	T0 = Float.NaN;
    	
    
    }  
       
    /**
     * Sets the maximum change from the starting coordinate of a peak
     * or -1 if this is not to be used. 
     * @param maxDelta  The maximum extent of a peak from the starting point.
     */
    public void setMaxExtent( int maxDelta){
       MaxExtent = maxDelta;
    }
    
    /**
     * Adds the current cell to the peak if it is in bounds or large enough average intensity with
     * neightbors
     * 
     * @param row    The row for the current cell
     * @param col    The column for the current cell
     * @param timeChan  The time channel for the current cell
     * @param intensity  The intensity of the current cell
     * @return  true if it is added otherwise false
     */
    public boolean addPeak( int row , int col , int timeChan , float intensity ){
    	  if( this.startRow < 0){
          startRow = row;
          startCol = col;
          startChan = timeChan;
        }
    	 
    	           
        if( MaxExtent > 0){
           if( Math.abs( row - startRow) > MaxExtent) return false;
           if( Math.abs( col - startCol) > MaxExtent) return false;
           if( Math.abs( timeChan - startChan) >  MaxExtent) return false;
        }
        
        if( row < 1 ) 
        	return false;
        if( col < 1 ) 
        	return false;
        if( timeChan < 0 ) 
        	return false;
        if( row > C_Grid.num_rows() )
        	return false;
        if( col > C_Grid.num_cols() )
        	return false;
        
        float Intensity = 0;
        int Ncells = 0;
        for( int r = row - 1 ; r <= row + 1 ; r++ )
            for( int c = col - 1 ; c <= col + 1 ; c++ )
                if( (  r > 0 ) &&( c > 0 )&&( r <= C_Grid.num_rows() )&&( c <= C_Grid.num_cols() ) ){
                    Ncells++ ;
                    Intensity += C_Grid.intensity( r , c, timeChan );
                }
        if( Ncells <= 0 )return false;
        if( Intensity/Ncells < backgroundIntensity )
            return false;
        
        intensity = C_Grid.intensity( row , col, timeChan );
        
        if( debug )
        	 System.out.println( "new Cell ,row,col,timechan,intensity=" + row + "," + col + "," + timeChan + "," + intensity );
      
        if( ncells == 0 ){
            minY = maxY = row;
            minX = maxX = col;
            
            minZ = maxZ = timeChan;
            TotExtentIntensity  = intensity;
            MaxIntensity = intensity;
        }
        ncells++ ;
        if( intensity > MaxIntensity )
        	MaxIntensity = intensity;
//      -------------- eliminate rambling through all the data  ------------------------
        if( col -minX  > .4* maxCols)
        	return false;
        if( maxX -col > .4* maxCols)
        	return false;
        if( row - minY > .4* maxRows)
        	return false;
        if( maxY - row > .4*maxRows)
        	 return false;
        if( timeChan- minZ >.2*maxChannels)
        	return false;
        if( maxZ -timeChan > .2*maxChannels)
        	return false;
        
//      -------------- update Total Extent ------------------------
        if( col > maxX ){
            for( int i = maxX + 1 ; i <= col ;  i++ )
                for( int j = minY ; j <= maxY ; j++ )
                    for( int k = minZ ;  k <= maxZ ;  k++ )
                        TotExtentIntensity += C_Grid.intensity( j , i , k );
             maxX = col;
        }
        if( row > maxY ) {
            for( int i = minX ; i <= maxX ; i++ )
                for( int j = maxY + 1 ; j <= row ; j++ )
                    for( int k = minZ ; k <= maxZ ; k++ )
                        TotExtentIntensity += C_Grid.intensity( j , i , k );
            maxY = row;
        }
        if( timeChan > maxZ ){
            for( int i = minX ; i <= maxX ; i++ )
                for( int j = minY ; j <= maxY ; j++ )
                    for( int k = maxZ + 1 ; k <= timeChan ; k++ )
                        TotExtentIntensity += C_Grid.intensity( j , i , k );
            maxZ = timeChan;
        }
        if( col < minX ) {
            for( int i = col ; i < minX ; i++ )
                for( int j = minY ; j <= maxY ; j++ )
                    for( int k = minZ ; k <= maxZ ; k++ )
                        TotExtentIntensity += C_Grid.intensity( j , i , k );
            minX = col;
        }
        if( row < minY ) {
            for( int i = minX ; i <= maxX ; i++ )
                for( int j = row ; j < minY ; j++ )
                    for( int k = minZ ; k <= maxZ ; k++ )
                        TotExtentIntensity += C_Grid.intensity( j , i , k );
            minY = row;
        }
        if( timeChan < minZ ) {
            for( int i = minX ; i <= maxX ; i++ )
                for( int j = minY ; j <= maxY ; j++ )
                    for( int k = timeChan ; k < minZ ; k++ )
                        TotExtentIntensity += C_Grid.intensity( j , i , k );
            minZ = timeChan;
        }
        
        //------------------ update statistic variables --------------------
        sumX += col;
        sumY += row;
        sumZ += timeChan;
        WsumX += intensity*col;
        WsumY += intensity*row;
        WsumZ += intensity*timeChan;
        Wx += intensity;
        Wy += intensity;
        Wz += intensity;
        TotIntensity += intensity;
        
        if( ncells  > 0 )
           background = ( TotExtentIntensity - TotIntensity )/
                           ( (1 + maxX - minX )*(1 + maxY - minY )*(1 + maxZ - minZ ) - ncells );
        
        if( !Float.isNaN( background )&& background > backgroundIntensity)
              background = backgroundIntensity;
        
       return true; 
    }
    
    
    
    /**
     *  Returns whether a give cell is inside the extent of this peak
     * @param row      The row of the cell to be tested
     * @param col      The column of the cell to be tested
     * @param timeChan  The channel of the cell to be tested
     * @return   true if this cell is between minX and maxX, minY and maxY,
     *                     and minZ and maxZ 
     */
    public boolean insideExtent( int row , int col , int timeChan ){
    	
        if( row < minY ) 
        	return false;
        
        if( col < minX )
        	return false;
        
        if( timeChan < minZ )
        	return false;
        
        if( row > maxY ) 
        	return false;
        
        if( col > maxX ) 
        	return false;
        
        if( timeChan > maxZ )
        	return false;
        
        
        return true;
    }
    
    

    /**
     *  Returns whether a given Peak hits the extent of this peak
     *  @param   peak    The peak to be tested
     *  @return  true if any cell the extent if peak is in this
     *           extent 
     */
    public boolean hits( PeakInfo peak ){
        return peak.hits( minY , maxY , minX , maxX , minZ , maxZ );
    }
    
    
    
    /**
     * Returns whether the extent of this peak lies in the bounds given
     * by the parameters
     * 
     * @param minRow        The minimum row of this extent
     * @param maxRow        The maximum row of this extent
     * @param minCol        The minimum column of this extent
     * @param maxCol        The maximum column of this extent
     * @param minTimeChan   The minimum channel of this extent
     * @param maxTimeChan   The maximum channel of this extent
     * @return              true if this peaks extent is in the given extent 
     */
    public boolean hits( int minRow , int maxRow ,int minCol , int maxCol ,            
                   int minTimeChan , int maxTimeChan ){
    	
        if( maxRow < this.minY )
        	return false;
        if( minRow > this.maxY ) 
        	return false;
        if( maxCol < this.minX )
        	return false;
        if( minCol > this.maxX ) 
        	return false;
        if( maxTimeChan < this.minZ )
        	return false;
        if( minTimeChan > this.maxZ ) 
        	return false;
        
        return true;
        
    }
    
    
    /**
     * Returns the average column(x value) for the cells in the peak
     * @return  average column(x value) for the cells in the peak
     */
    public float getAverageCol(){
    	
        if( ncells <= 0 )
            return Float.NaN;
       
       return sumX/(float)ncells; 
    }

    
    
    /**
     * Returns the average row(y value) for the cells in the peak
     * @return  average row(y value) for the cells in the peak
     */    
    public float getAverageRow(){
    	
        if( ncells <= 0 )
            return Float.NaN;
       
       return sumY/(float)ncells; 
       
    }

    
    
    
    /**
     * Returns the average channel(z value) for the cells in the peak
     * @return  average channel(z value) for the cells in the peak
     */  
    public float getAverageChan(){
    	
        if( ncells <= 0 )
            return Float.NaN;
       
       return sumZ/(float)ncells; 
    }
   

    
    
    /**
     * Returns the intensity weighted average column(x value) for the cells in the peak
     * @return  the intensity weighted average column(x value) for the cells in the peak
     *           with the background intensity( cutoff intensity) subtracted
     */  
    public float getWeightedAverageCol(){
    	
        if( ncells <= 0 )
            return Float.NaN;
       float b =background;
       if( Float.isNaN(b))
       	b=backgroundIntensity;
       //float b = backgroundIntensity;
       return (WsumX - b*sumX )/(TotIntensity - ncells*b );
       
    }

    
    
    /**
     * Returns the intensity weighted average row(y value) for the cells in the peak
     * @return  the intensity weighted average row(y value) for the cells in the peak
     *           with the background intensity( cutoff intensity) subtracted
     */  
    public float getWeightedAverageRow(){
    	
        if( ncells <= 0 )
            return Float.NaN;

        float b =background;
        if( Float.isNaN(b))
        	b=backgroundIntensity;
        //float b = backgroundIntensity; 
       return (WsumY - b*sumY )/(TotIntensity - ncells*b );
       
    }

    
    
    
    /**
     * Returns the intensity weighted average channel(z value) for the cells in the peak
     * @return  the intensity weighted average channel(z value) for the cells in the peak
     *           with the background intensity( cutoff intensity) subtracted
     */
      public float getWeightedAverageChan(){
    	  
        if( ncells <= 0 )
            return Float.NaN;

        float b =background;
        if( Float.isNaN(b))
        	b=backgroundIntensity;
        //float b = backgroundIntensity;
       return ( WsumZ - b*sumZ )/( TotIntensity - ncells*b );
       
    }
    
      
      
    /**
     * 
     * @return  the average intensity for those cells in the extent that are not connected to
     *           the peak.
     */
    public float getCalcBackgroundLevel(){
    	
        return background;
        
    }
    
    
    /**
     * 
     * @return  the average of the Maximum x value and minimum x value for this peak
     */    
    public float getMiddleCol(){
    	
       return ( maxX + minX )/2f;
       
    }

    

    /**
     * 
     * @return  the average of the Maximum y value and minimum y value for this peak
     */    
    public float getMiddleRow(){
    	
       return  ( maxY + minY )/2f;
       
    }
    
    


    /**
     * 
     * @return  the average of the Maximum z value and minimum z value for this peak
     */    
    public float getMiddleChan(){
    	
       return  ( maxZ + minZ )/2f;
       
    }
    
    

    /**
     * 
     * @return  the total of the intensities of the peaks in this cell with 
     *          the background(cutoff) subtracted
     */
    
    public float getTotIntensity(){
        float b = background;
        if( Float.isNaN(b))
        	b=backgroundIntensity;
        return  TotIntensity - ncells*backgroundIntensity;
        
    }
    
    
    

    /**
     * 
     * @return  the number of cells in this peak
     */
    
    public int getNCells(){
    	
       
    return ncells;
    
    }
    
    
    

    /**
     * 
     * @return  the number of cells in the extent. Includes the peak cells
     */
    
    public int getNCellsExtent(){
    	
    	if( maxX < 0 ) 
    		return 0;
    	
    	return ( maxX - minX + 1 )*( maxY - minY + 1 )*( maxZ - minZ + 1 );
    	
    }
    
    
    
    // Gets attributes out of the data set and data blocks
    private void setUpBasics(){
    	
    	if( sampOrient !=  null )
    		return;
    	sampOrient = ( SampleOrientation )DS.getAttributeValue(  DataSetTools.dataset.Attribute.SAMPLE_ORIENTATION );
    	Float T = ((Float)DS.getAttributeValue( DataSetTools.dataset.Attribute.T0_SHIFT ) );
    	if( T == null )
    		T0 = 0f;
    	else 
    		T0 = T.floatValue();
    	xscl = DS.getData_entry( 0 ).getX_scale();
    	Float I = ((Float)DS.getData_entry( 0 ).getAttributeValue( DataSetTools.dataset.Attribute.INITIAL_PATH ) );
    	if( I == null )
    		initialPath = 0f;
    	else
    		initialPath = I.floatValue();
    	
    }
    
    
    
      
    /**
     * Creates a new type Peak from this information
     * @return  a PeakObject
     */
    public Peak_new makePeak(){
    	 if( DS == null)
          return null;
    	setUpBasics();
    	int reflag = 11;
    	if( ncells < 0 )
    	  reflag = 21;
        if( maxX < 0 )
           reflag = 21;
        if( ncells == ( maxX - minX + 1 )*( maxY -minY + 1 )*( maxZ - minZ + 1 ) )
           reflag = 21;
    	Peak_new PP = null;
    	if( reflag == 11)
    	  PP = new Peak_new( getWeightedAverageCol() , getWeightedAverageRow() , getWeightedAverageChan() ,
    			     grid , sampOrient , T0 , xscl , initialPath );
    	else{ 
    	  PP = new Peak_new( startCol, startRow, startChan,
                 grid , sampOrient , T0 , xscl , initialPath );
    	}
      PP.reflag( reflag);
  
    	float peakIntensity = Float.NaN;
    
        peakIntensity = MaxIntensity;
        
    	PP.ipkobs( (int)( peakIntensity + .5 ) );
    	PP.inti( getTotIntensity() );
    	int[] runs = (int[])DS.getAttributeValue( Attribute.RUN_NUM );
    	if( runs != null ) if( runs.length  > 0 )
    		PP.nrun( runs[ 0 ] );
    	
    	return PP;
    	
    }
    
    
    
}
