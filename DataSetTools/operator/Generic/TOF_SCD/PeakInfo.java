package DataSetTools.operator.Generic.TOF_SCD;
import DataSetTools.dataset.*;

public class PeakInfo {
    int ncells;
    int maxX,maxY,maxZ;
    int minX,minY,minZ;
    int sumX,sumY,sumZ;
    float WsumX,WsumY,WsumZ;
    float WX,Wy,Wz;
    float TotIntensity;
    int detNum;
    IDataGrid grid;
    float TotExtentIntensity;
    float background;
    float backgroundIntensity;
    public PeakInfo( int detNum, IDataGrid grid , float backgroundIntensity) {
        super();
        ncells=maxX=maxY=maxZ=sumX=sumY=sumZ=0;
        minX=minY=minZ=Integer.MAX_VALUE;
        WsumX=WsumY=WsumZ=WX=Wy=Wz=TotIntensity=TotExtentIntensity=0;
        this.detNum = detNum;
        this.grid = grid;
        if( !Float.isNaN(backgroundIntensity) && (backgroundIntensity >0))
            background=this.backgroundIntensity=backgroundIntensity;
        else{
            backgroundIntensity = Float.NaN;
            background = 0f;
            
        }
    }  
       

    public boolean addPeak( int row, int col, int timeChan, float intensity){
        if( !Float.isNaN(backgroundIntensity))
            if( intensity < backgroundIntensity)
                return false;
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
       System.out.println("backgroundIntensity="+backgroundIntensity);
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
    
      
    
    
    
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
