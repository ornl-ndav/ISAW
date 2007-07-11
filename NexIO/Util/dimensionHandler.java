/**
 * 
 */
package NexIO.Util;


/**
 * 
 * @author Ruth
 *
 */
public class dimensionHandler {

   /**
    *   Handles multiDimensioned arrays where entries are not in the
    *   correct sequence for automatic incrementing.  Allows for 
    *   incrementing on one dimension, and returning the index in
    *   a linearized multidimensioned array
    */
    
      int[] dims;//full dimension with -1's for *'s
      
      int timeIncr,
          rowIncr,
          colIncr;
      
      int timeDim, 
          rowDim, 
          colDim, 
          Det1Dim;
      
      int index;
      
      int[] countTuple, 
            mult;
      /**
       * Constructor
       * 
       * @param dims   The dimensions for the multidimensioned array
       * 
       * @param timeDim  The position in dims and the multidimensional array
       *                that represents time-the should be the fastest changing
       *                dimension.  0 is the rightmos position in dims
       *                
       * @param colDim  The position in dims and the multidimensional array
       *                that represents col-the should be the 2nd fastest
       *                 changing dimension.  0 is the rightmos position in dims
       *                 
       * @param rowDim   The position in dims and the multidimensional array
       *                that represents row-the should be the 3rd fastest
       *                 changing dimension.  0 is the rightmos position in dims
       *                 
       * NOTE: The other dimensions will be automatically assigned 4th, 5th fastest
       *       etc. 
       *                 
       */
      public dimensionHandler( int[] dims, int timeDim, int colDim, int rowDim){
         
         this.dims = dims;
         int multt = 1;
         timeIncr= rowIncr= colIncr =0;
         
         this.rowDim = rowDim;
         this.colDim = colDim;
         this.timeDim = timeDim;
         
         mult = new int[ dims.length];
       
         if( dims != null)
            for( int i= dims.length-1 ; i>=0 ; i-- ){
               if( dims[i] >=1)
               if( i == dims.length - 1-timeDim )
                  timeIncr = multt;
               else if( i== dims.length - 1-rowDim)
                  rowIncr = multt;
               else if ( i == dims.length - 1- colDim )
                  colIncr = multt;
               if( dims[i] >= 1 ){
                  mult[i] = multt;
                  multt *=dims[i];
               }
            }
         
           index = -1;
           countTuple = null;
           Det1Dim = Math.max( Math.max( timeDim,rowDim),colDim)+1;
      }
      
      /**
       * Returns the index of the current position in the linearlized 
       *     multidimensional array
       *     
       * @return the index of the current position in the linearlized 
       *     multidimensional array
       */
      int getIndex(){
         if( index < 0)
            return 0;
         return index;
      }
      
      /**
       * Resets the index to the given tuple, updating the index
       * 
       * @param countTuple  The new multidimenaional tuple
       * 
       * @throws IllegalArgumentException if countTuple is null or
       *                           its length is incorrect
       */
     public void resetIndex( int[] countTuple)throws IllegalArgumentException{
        
         if( countTuple == null)
            throw new IllegalArgumentException( " Dimension cannot be null int resetIndex ");
         if( countTuple.length != dims.length)
            throw new IllegalArgumentException( " Dimensions must all have the same length ");
       
         index = 0;
         this.countTuple = countTuple;
         for( int i= 0; i< dims.length; i++){
            
            if( dims[i] >0){
              index *=dims[i];
              
              index += countTuple[i];
             
          }
          
         }
         
      }
      
     
     /**
      * Increments the Time dimension adjusting the current index and tuple to
      * correspond to the new time.  If the time is at the maxTime, the column
      * is automatically incremented.
      */
      public void IncrTime(){
         
         if( countTuple == null)
            return;
         
         if( timeDim >=0)
            
            if( countTuple[dims.length-1-timeDim]+1 <dims[dims.length-1-timeDim]){
               
               countTuple[dims.length-1-timeDim]++;
                index +=timeIncr;
                
            }else {
               
               index =index -(dims[dims.length-1-timeDim]-1)*timeIncr;
               countTuple[dims.length-1-timeDim]=0;
               IncrCol();
            }
         
      }
      
      
      /**
       * Increments the Row dimension adjusting the current index and tuple to
       * correspond to the new row.  If the row is at the maxRow, the 1st
       * detector is automatically incremented.
       */
      public void IncrRow(){
         
         if( countTuple == null)
            return;
         
         if( rowDim >=0)
            
            if( countTuple[dims.length-1-rowDim]+1 <dims[dims.length-1-rowDim]){
               
              countTuple[dims.length-1-rowDim]++;
               index +=  rowIncr;
               
            }else{
               
               countTuple[dims.length-1-rowDim]=0;
               index = index -(dims[dims.length-1-rowDim]-1)*rowIncr;
               IncrPlace( Det1Dim);               
            }
      }
  
      
      /**
       * Increments the Column dimension adjusting the current index and tuple to
       * correspond to the new column.  If the column is at the maxCol, the row
       * is automatically incremented.
       */
      public void IncrCol(){
         
         if( countTuple == null)
            return;
         
         if( colDim >=0)
            
            if( countTuple[dims.length-1-colDim]+1 <dims[dims.length-1-colDim]){
               
               countTuple[dims.length-1-colDim]++;
                index +=  colIncr;
                
             }else{
                
                countTuple[dims.length-1-colDim]=0;
                index = index -(dims[dims.length-1-colDim]-1)*colIncr;
                IncrRow();
             }
      }
      
      
      /**
       * increments a position and carries if the position is too high
       * 
       * @param position  the position (pos =0 right most) 
       */
      public void IncrPlace( int position){
         
         int p = dims.length-1-position;
         if( (p < 0) ||(p >= dims.length))
            return;
         
         if( dims[p] < 0)
            return;
         
         if( countTuple == null)
            return;
         
         if( countTuple[p] +1 < dims[p]){
            
            index += mult[ p ];
            countTuple[p]++;
            
         }else{
            
            index -= (dims[p]-1)*mult [ p ];
            countTuple[p]=0;
            IncrPlace( position+1);
            
         }
         
      }
      
      
      /**
       * Returns the current tuple
       * @return he current tuple
       */
      public int[] getCounter(){
         return countTuple;
      }
      
      
      /**
       * returns the original dims which gives the max values for each 
       * entry in the tuple
       * 
       * @return the original dims
       */
      public int[] getLimits(){
         return dims;
      }
      
      
      /**
       * Shows the current tuple
       *
       */
      public void show(){
         for( int i=0; i< countTuple.length ; i++)
         System.out.print( countTuple[i]+",");
         System.out.println("");
      }
      
      
   /**
    * 
    * Test program for this module
    * 
    * @param args  Not used
    */
   public static void main( String[] args ) {

      int[] dim1 ={2,3,2,4,3};
      int[] countdim = new int[5];
      countdim[0] = 0;//( new Integer( args[0])).intValue();
      countdim[1] = 0;// ( new Integer( args[1])).intValue();
      countdim[2] =  0;//( new Integer( args[2])).intValue();
      countdim[3] =  0;//( new Integer( args[3])).intValue();
      countdim[4] =  0;//( new Integer( args[3])).intValue();
      dimensionHandler hand = new dimensionHandler( dim1, 1,0,2);
      hand.resetIndex( countdim);
      for( int i=0; i < 147; i++){
         System.out.println("------------------------");
         System.out.print( hand.getIndex()+"::");
         int[] cnt = hand.getCounter();
         
         hand.show();
         hand.resetIndex( cnt);
         System.out.println( hand.getIndex());
         hand.IncrTime();
      }
      

   }

}
