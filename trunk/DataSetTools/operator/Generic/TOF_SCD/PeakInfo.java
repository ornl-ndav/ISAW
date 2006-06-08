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
 * Revision 1.4  2006/06/08 15:55:12  rmikk
 * Added GPL
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;
public class PeakInfo {
	DataSet DS;
	SampleOrientation sampOrient;
	XScale xscl;
	float initialPath;
	float T0;
	
	
    int ncells;
    public int maxX,maxY,maxZ;
    public int minX,minY,minZ;
    int sumX,sumY,sumZ;
    float WsumX,WsumY,WsumZ;
    float Wx,Wy,Wz;
    float TotIntensity;
    int detNum;
    IDataGrid grid;
    float TotExtentIntensity;
    float background;
    public float backgroundIntensity;
    
    public boolean debug = false;
    
    
    public PeakInfo( int detNum, IDataGrid grid , float backgroundIntensity, DataSet DS) {
        super();
        ncells=0;maxX=maxY=maxZ=-1;sumX=sumY=sumZ=0;
        minX=minY=minZ=Integer.MAX_VALUE;
        WsumX=WsumY=WsumZ=Wx=Wy=Wz=TotIntensity=TotExtentIntensity=0;
        this.detNum = detNum;
        this.grid = grid;
        if( !Float.isNaN(backgroundIntensity) && (backgroundIntensity >0))
            background=this.backgroundIntensity=backgroundIntensity;
        else{
            backgroundIntensity = 0f;
            background = 0f;
            
        }
        this.DS = DS;
    	sampOrient = null;
    	xscl = null;
    	initialPath = Float.NaN;
    	T0 = Float.NaN;
    }  
       

    public boolean addPeak( int row, int col, int timeChan, float intensity){
       // if( !Float.isNaN(backgroundIntensity))
       //     if( intensity < backgroundIntensity)
      //          return false;
        if( row <1) return false;
        if( col <1) return false;
        if(timeChan <0) return false;
        if(row > grid.num_rows())return false;
        if( col > grid.num_cols())return false;
        float Intensity = 0;
        int Ncells =0;
        for( int r=row -1; r<=row+1;r++)
            for( int c=col-1;c<=col+1;c++)
                if(( r>0) &&(c>0)&&(r<=grid.num_rows())&&(c<=grid.num_cols())){
                    Ncells++;
                    Intensity += grid.getData_entry(r,c).getY_values()[timeChan];
                }
        if( Ncells <=0)return false;
        if( Intensity/Ncells < backgroundIntensity)
            return false;
        
        if( debug)
        	 System.out.println("new Cell,row,col,timechan,intensity="+row+","+col+","+timeChan+","+intensity);
        
        if( ncells ==0){
            minY=maxY=row;
            minX=maxX=col;
            
            minZ=maxZ= timeChan;
            TotExtentIntensity  = intensity;
        }
        ncells++;
        if( col > maxX){
            for( int i=maxX+1; i<=col; i++)
                for( int j = minY;j<=maxY; j++)
                    for( int k = minZ; k <= maxZ; k++)
                        TotExtentIntensity += grid.getData_entry(j,i).getY_values()[k];
             maxX=col;
        }
        if( row > maxY) {
            for( int i=minX; i<=maxX; i++)
                for( int j = maxY+1;j<=row; j++)
                    for( int k = minZ; k <= maxZ; k++)
                        TotExtentIntensity += grid.getData_entry(j,i).getY_values()[k];
            maxY=row;
        }
        if( timeChan > maxZ){
            for( int i=minX; i<=maxX; i++)
                for( int j = minY;j<=maxY; j++)
                    for( int k = maxZ+1; k <= timeChan; k++)
                        TotExtentIntensity += grid.getData_entry(j,i).getY_values()[k];
            maxZ=timeChan;
        }
        if( col < minX) {
            for( int i=col; i< minX; i++)
                for( int j = minY;j<=maxY; j++)
                    for( int k = minZ; k <= maxZ; k++)
                        TotExtentIntensity += grid.getData_entry(j,i).getY_values()[k];
            minX=col;
        }
        if( row < minY) {
            for( int i=minX; i<=maxX; i++)
                for( int j = row;j<minY; j++)
                    for( int k = minZ; k <= maxZ; k++)
                        TotExtentIntensity += grid.getData_entry(j,i).getY_values()[k];
            minY=row;
        }
        if( timeChan < minZ) {
            for( int i=minX; i<=maxX; i++)
                for( int j = minY;j<=maxY; j++)
                    for( int k = timeChan; k < minZ; k++)
                        TotExtentIntensity += grid.getData_entry(j,i).getY_values()[k];
            minZ=timeChan;
        }
        sumX +=col;
        sumY+=row;
        sumZ +=timeChan;
        WsumX +=intensity*col;
        WsumY+=intensity*row;
        WsumZ +=intensity*timeChan;
        Wx += intensity;
        Wy += intensity;
        Wz += intensity;
        TotIntensity += intensity;
        if(ncells >0)
           background = (TotExtentIntensity-TotIntensity)/((1+maxX-minX)*(1+maxY-minY)*(1+maxZ-minZ)-ncells);
       return true; 
    }
    
    public boolean insideExtent( int row, int col, int timeChan){
        if( row < minY) return false;
        if( col < minX)return false;
        if( timeChan< minZ)return false;
        if(row > maxY) return false;
        if( col > maxX) return false;
        if( timeChan > maxZ)return false;
        return true;
    }
    
    public boolean hits( PeakInfo peak){
        return peak.hits( minY, maxY, minX, maxX, minZ, maxZ);
    }
    public boolean hits( int minRow,int maxRow,int minCol, int maxCol,            
                   int minTimeChan, int maxTimeChan){
        if( maxRow < this.minY)return false;
        if( minRow > this.maxY) return false;
        if( maxCol < this.minX)return false;
        if( minCol > this.maxX) return false;
        if( maxTimeChan < this.minZ)return false;
        if( minTimeChan > this.maxZ) return false;
        return true;
        
    }
    public float getAverageCol(){
        if( ncells <=0)
            return Float.NaN;
       
       return sumX/(float)ncells; 
    }

    public float getAverageRow(){
        if( ncells <=0)
            return Float.NaN;
       
       return sumY/(float)ncells; 
    }

    public float getAverageChan(){
        if( ncells <=0)
            return Float.NaN;
       
       return sumZ/(float)ncells; 
    }
   

    public float getWeightedAverageCol(){
        if( ncells <=0)
            return Float.NaN;
       
       return (WsumX-backgroundIntensity*sumX)/(TotIntensity-ncells*backgroundIntensity); 
    }

    public float getWeightedAverageRow(){
        if( ncells <=0)
            return Float.NaN;
        
       return (WsumY-backgroundIntensity*sumY)/(TotIntensity-ncells*backgroundIntensity);  
    }

    public float getWeightedAverageChan(){
        if( ncells <=0)
            return Float.NaN;
       
       return (WsumZ-backgroundIntensity*sumZ)/(TotIntensity-ncells*backgroundIntensity); 
    }
    
    /**
     * 
     * @return  the average intensity for those cells in the extent that are not connected to
     *           the peak.
     */
    public float getCalcBackgroundLevel(){
        return background;
    }
    public float getMiddleCol(){
       return (maxX+minX)/2f; 
    }

    public float getMiddleRow(){
       return  (maxY+minY)/2f; 
    }

    public float getMiddleChan(){
       return  (maxZ+minZ)/2f; 
    }
    
    public float getTotIntensity(){
        return  TotIntensity-ncells*backgroundIntensity; 
    }
    
    public int getNCells(){
        return ncells;
    }
    
    public int getNCellsExtent(){
    	if(maxX <0) return 0;
    	return (maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1);
    	
    }
    private void setUpBasics(){
    	if(sampOrient != null)
    		return;
    	sampOrient= (SampleOrientation)DS.getAttributeValue( DataSetTools.dataset.Attribute.SAMPLE_ORIENTATION);
    	Float T = ((Float)DS.getAttributeValue( DataSetTools.dataset.Attribute.T0_SHIFT));
    	if( T == null)
    		T0 =0f;
    	else 
    		T0 =T.floatValue();
    	xscl = DS.getData_entry(0).getX_scale();
    	Float I = ((Float)DS.getAttributeValue( DataSetTools.dataset.Attribute.INITIAL_PATH));
    	if( I == null)
    		initialPath = 0f;
    	else
    		initialPath = I.floatValue();
    }
      
    /**
     * Creates a new type Peak from this information
     * @return  a PeakObject
     */
    public Peak_new makePeak(){
    	setUpBasics();
    	if( ncells <0)
    		return null;
        if( maxX <0)
        	return null;
        if( ncells == (maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1))
        	return null;
    	Peak_new PP= new Peak_new( getWeightedAverageCol(), getWeightedAverageRow(), getWeightedAverageChan(),
    			     grid, sampOrient, T0, xscl, initialPath);
    	int x = (int)(getWeightedAverageCol()+.5); 
    	int y =(int)(.5+getWeightedAverageRow());
    	int z =(int)(.5+getWeightedAverageChan());
    	float peakIntensity =Float.NaN;
    	try{
    		peakIntensity = grid.getData_entry(y , x).getY_values()[z];
    	}catch( Exception ss){
    		peakIntensity = Float.NaN;
    	}
    	PP.ipkobs( (int)(peakIntensity+.5));
    	PP.inti( getTotIntensity());
    	int[] runs = (int[])DS.getAttributeValue( Attribute.RUN_NUM);
    	if(runs != null) if( runs.length >0)
    		PP.nrun( runs[0]);
    	
    	return PP;
    }
    
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
