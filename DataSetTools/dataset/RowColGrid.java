/* 
 * File: RowColGrid.java 
 *  
 * Copyright (C) 2007     Ruth Mikkelson
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * Modified:
 * $Log$
 * Revision 1.7  2008/02/23 06:06:25  dennis
 * Fixed calculation of pixel width, height, base and up vectors
 * for pixels in the last row or column of the grid.
 *
 * Revision 1.6  2008/02/13 20:10:42  dennis
 * Minor fixes to java docs.
 *
 * Revision 1.5  2008/01/02 19:27:41  rmikk
 * Fixed several errors/misinterpretations that gave incorrect results.
 *
 * Revision 1.4  2007/12/20 19:57:18  rmikk
 * Eliminated a debug print and increased checking for null, especially if the
 *    detector position is null.
 *  Fixed an error in SetDataEntries.
 *
 * Revision 1.3  2007/12/19 19:20:33  rmikk
 * Added a clone method
 *
 * Revision 1.2  2007/08/23 21:05:03  dennis
 * Removed unused imports.
 *
 * Revision 1.1  2007/08/08 17:19:44  rmikk
 * Added more documentation
 *
 */
package DataSetTools.dataset;

import gov.anl.ipns.MathTools.Geometry.*;
import Command.ScriptUtil;


/**
 * This class is a basic IDataGrid that stores Data elements
 * at row, column positions.  All calculations for the remaining
 * methods are based on the positions of these Data elements.  It
 * would be best that data elements that are close in terms of
 * rows and columns are also close in terms of their position in
 * 3D
 * 
 * @author Ruth
 *
 */
public class RowColGrid implements IDataGrid {



   int      nrows;

   int      ncols;

   int      ID;

   Data[][] Grid;

   int      NSet;

   boolean  filled;

   String   units;

   float    depth;

   /**
    * Constructor with no Data elements set
    * 
    * @param nrows  The number of rows
    * 
    * @param ncols  The number of columns
    * 
    * @param ID  The ID to be given to this grid
    * 
    * @throws IllegalArgumentException when any of the arguments are nonsense
    */
   public RowColGrid( int nrows, int ncols, int ID )
            throws IllegalArgumentException {
      

      if( nrows < 1 || ncols < 1 )
         throw new IllegalArgumentException(
                  " RowColGrid Must have 1 or more rows and columns" );

      this.nrows = nrows;
      this.ncols = ncols;
      Grid = new Data[ nrows ][ ncols ];

      for( int i = 0 ; i < nrows ; i++ )
         java.util.Arrays.fill( Grid[ i ] , null );

      NSet = 0;
      this.ID = ID;
      filled = false;
      units = "m";
      depth = .05f;


   }
 
   public IDataGrid clone(){
      
      RowColGrid Res = new RowColGrid( nrows, ncols, ID);
      Res.setGridDepth( depth );
      
      for( int row =0; row < nrows; row++)
         for( int col=0; col<  ncols; col++)
            if( Grid[row][col] != null)
               Res.setOneData( Grid[row][col] , row+1 , col+1 );
      
      return Res;
      
   }

   /**
    * Set the depth of this grid in meters
    * 
    * @param depth
    *           The new depth of this grid.
    */
   public void setGridDepth( float depth ) {

      this.depth = depth;
      
   }


   /**
    * Sets the data entry into the grid. This assumes that this Data Entry
    * contains the position information.
    * 
    * @param D
    *           The Data to be added to this grid
    * @param row
    *           The associated row of this Data element
    * @param col
    *           The associated col of this Data element
    */
   public void setOneData( Data D , int row , int col ) {

      if( row < 1 || col < 1 || row > nrows || col > ncols )
         return;

      if( Grid[ row - 1 ][ col - 1 ] == null )
      {
         if( D != null)
             NSet++ ;
      }else if( D == null)
         NSet--;

      Grid[ row - 1 ][ col - 1 ] = D;

   }


   /**
    *  @return the ID for this grid
    */
   public int ID() {

      return ID;

   }

   /**
    * @return the units for lengths of the sides of this grid
    */
   public String units() {


      return units;
   }


   public Vector3D x_vec() {


      return x_vec( .5f + nrows / 2f , .5f + ncols / 2f );
   }


   /**
    * @return The unit vector for the direction to the Data element at
    * the next column up  from the data element(s) at the center of the grid
    */
   public Vector3D y_vec() {

      return y_vec( .5f + nrows / 2f , .5f + ncols / 2f );


   }

   /**
    * @return The unit vector for the direction to the Data element at
    * the next row to the right  from the data element(s) at the center of the 
    * grid
    */
   public Vector3D z_vec() {


      return z_vec( .5f + nrows / 2f , .5f + ncols / 2f );
   }


   
   /**
    *  @return the position of the Data elements close to the center of the 
    *  grid
    */
   public Vector3D position() {

      //
      return position( .5f + nrows / 2f , .5f + ncols / 2f );


   }


   
   /**
    * @return the width of the grid from middle row at largest column to
    * middle row and min column positions
    */
   public float width() {


      Vector3D L = position( 1 + (int) ( nrows / 2f ) , .5f );
      Vector3D R = position( 1 + (int) ( nrows / 2f ) , ncols + .5f );


      if( L == null || R == null )
         return Float.NaN;

      R.subtract( L );

      return R.length();
   }


   /**
    * @return the height of the grid from middle col at largest row to
    * middle column and min row positions
    */
   public float height() {


      Vector3D L = position( .5f , 1 + (int) ( ncols / 2f ) );
      Vector3D R = position( nrows + .5f , 1 + (int) ( ncols / 2f ) );

      if( L == null || R == null )
         return Float.NaN;

      R.subtract( L );

      return R.length();

   }

   /**
    * @return the depth of the grid. 
    * 
    */
   public float depth() {

      return depth;

   }

   /**
    * @return the number of rows in this grid.
    */
   public int num_rows() {

      return nrows;

   }


   /**
    * @return the number of columns in this grid
    */
   public int num_cols() {

      return ncols;
   }


   /**
    *    * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    *  @return the dot product of the 3D position at row, col
    *  with the unit vector in the x direction at the center
    */
   public float x( float row , float col ) {

      Vector3D P = position( row , col );
      if( P == null )
         return Float.NaN;


      Vector3D xDir = x_vec();
      if( xDir == null )
         return Float.NaN;

      return P.dot( xDir );
   }


   /**
    *    * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    *  @return the dot product of the 3D position at row, col
    *  with the unit vector in the y direction at the center
    */
   public float y( float row , float col ) {


      Vector3D P = position( row , col );
      if( P == null )
         return Float.NaN;

      Vector3D yDir = y_vec();
      if( yDir == null )
         return Float.NaN;

      return P.dot( yDir );


   }


   private Vector3D Poss( float x , float y ) {

      Vector3D xvec = x_vec();
      if( xvec == null )
         return null;

      Vector3D yvec = y_vec();
      if( yvec == null )
         return null;

      xvec.multiply( x );
      yvec.multiply( y );
      xvec.add( yvec );

      return xvec;

   }


   /**
    * Finds the Grid element where x(row,col) is within width(row,col)/2
    * of x and y(row,col) is within height(ow,col)/2 of y.
    * * 
    * @param x   The column in this grid where there is a row where
    *             x(row,col) is about x
    * @param y  The row in this grid where there is a row where
    *             y(row,col) is about y
    *  
    * @return the row value interpolated
    */
   public float row( float x , float y ) {

      float[] f = findRowCol( x , y );
      if( f == null )
         return Float.NaN;

      
      return f[ 0 ];
   }



   /**
    * Finds the Grid element where x(row,col) is within width(row,col)/2
    * of x and y(row,col) is within height(row,col)/2 of y.
    * 
    * @param x   The column in this grid where there is a row where
    *             x(row,col) is about x
    * @param y  The row in this grid where there is a row where
    *             y(row,col) is about y
    * 
    * @return the column value interpolated
    */
   public float col( float x , float y ) {


      float[] f = findRowCol( x , y );
      if( f == null )
         return Float.NaN;

      return f[ 1 ];
   }

  //error   x(row,col)*x_vec()+y(row,col)*y_vec()+
   //  z(row,col)*z_vec()= position(row,col)
   private float[] findRowCol( float x , float y ) {

      Vector3D xvec = x_vec();
      Vector3D yvec = y_vec();
      if( xvec == null || yvec == null )
         return null;

      for( int row = 0 ; row < nrows ; row++ )
         for( int col = 0 ; col < ncols ; col++ ) {

            Vector3D P = position( row , col );
            float width = width( row , col );
            float height = height( row , col );

            if( P != null && ! Float.isNaN( width ) && ! Float.isNaN( height ) )
               if( ( Math.abs( P.dot( xvec ) - x ) < width / 2f )
                        && ( Math.abs( P.dot( yvec ) - y ) < height / 2f ) ) {

                  float[] Res = new float[ 2 ];

                  Res[ 0 ] =  row - ( P.dot( yvec ) - y ) / height;
                  Res[ 1 ] =  col - ( P.dot( xvec ) - x ) / width;

                  return Res;
               }


         }
      return null;


   }


   private Vector3D getPos( int row , int col ) {

      if( row < 1 || col < 1 || row > nrows || col > ncols )
         return null;
      
      Data D = Grid[ row - 1 ][ col - 1 ];
      if( D == null )
         return null;
      DetectorPosition dp =AttrUtil.getDetectorPosition( D );
      if( dp == null)
         return null;
      return new Vector3D( dp );
   }


   private int roundd( float f , int max ) {

      int ff = (int) ( f + .5 );
      if( ff > max )
         ff-- ;
      if( ff < 1 )
         ff++ ;
      return ff;

   }


   private void show( String message , Position3D V ) {

      if( V == null ) {
         System.out.println( message + ", null" );
         return;
      }
      float[] F = V.getCartesianCoords();
      System.out.println( message + "(" + F[ 0 ] + "," + F[ 1 ] + "," + F[ 2 ]
               + ")" );

   }

  
   /**
    * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    * @return the weighted average position of the Data elements in the Grid 
    * immediately close to row and column
    */
   public Vector3D position( float row , float col ) {

      if( row < .5 || col < .5 || row > nrows + .5 || col > ncols + .5 )
         return null;

      int rrow = Math.min( (int) row , nrows  );
      int ccol = Math.min( (int) col , ncols  );


      Vector3D pos = new Vector3D( 0f , 0f , 0f ) , pos1;

      
      float rowFrac = row - rrow;
      float colFrac = col - ccol;
      int drow = 0;
      if( rowFrac != 0 )
         drow = 1;
      int dcol = 0;
      if( colFrac != 0 )
         dcol = 1;

      for( int i = 0 ; i < 1 + drow ; i++ )
         for( int j = 0 ; j < 1 + dcol ; j++ ) {
            
            Data D = getData_entry( rrow + i , ccol + j );
            
            if( D != null ) {
               pos1 = new Vector3D( AttrUtil.getDetectorPosition( D ) );

               if( pos1 == null )
                  return null;

               pos1.multiply( Math.abs(1- i - rowFrac )
                                 * Math.abs(1- j - colFrac ) );
               pos.add( pos1 );

            }
            
            else if( rrow + i <= nrows && ccol + j <= ncols && rrow + i >= 1
                     && ccol + j >= 1 )

               return null;// The Grid is not full

            else {// edge or corner , reflect back and extrapolate
               
               int rowDir = 0 , // direction to a valid cell
               colDir = 0;
               if( rrow + i > nrows )
                  rowDir = - 1;
               else if( rrow + i < 1 )
                  rowDir = 1;

               if( ccol + j > ncols )
                  colDir = - 1;
               else if( ccol + j < 1 )
                  colDir = 1;

               D = getData_entry( rrow + i+ rowDir , ccol+j + colDir );
               if( D == null )
                  return null;
               DetectorPosition dp = AttrUtil.getDetectorPosition( D );
               if( dp == null )
                  return null;
               Vector3D P0 = new Vector3D( dp );
               

               D = getData_entry( rrow + i +   2*rowDir , ccol+j +colDir );
               if( D == null )
                  return null;
               dp = AttrUtil.getDetectorPosition( D );
               if( dp == null )
                  return null;

               Vector3D P = new Vector3D( dp );
               Vector3D Prow = new Vector3D( P0 );
               Prow.multiply( 2f );
               Prow.subtract( P );


               D = getData_entry( rrow+i+ rowDir , ccol +j+ 2 * colDir );
               if( D == null )
                  return null;
               dp = AttrUtil.getDetectorPosition( D );
               if( dp == null )
                  return null;

               P = new Vector3D( dp );
               Vector3D Pcol = new Vector3D( P0 );
               Pcol.multiply( 2f );
               Pcol.subtract( P );

               Prow.subtract( P0 );
               Prow.add( Pcol );
               Prow.multiply( Math.abs(1- i - rowFrac )
                                 * Math.abs( 1-j - colFrac ) );

               pos.add( Prow );
            }
         }
      return pos;
   }

   /**
    * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    * @return the width of the Grid element at row, col
    */
   public float width( float row , float col ) {

      int current_row = roundd( row, nrows );

      Vector3D P0 = getPos( current_row, roundd( col, ncols ) );

      if( P0 == null )
         return Float.NaN;
      
      Vector3D P1;

      if( col < ncols )
        P1 = getPos( current_row, roundd( col + 1 , ncols ) );
      else
        P1 = getPos( current_row, roundd( col - 1 , ncols ) );

      if ( P1 == null )
        return Float.NaN;
      
      P1.subtract( P0 );
      return P1.length();
   }

   /**
    * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    * @return the height of the Grid element at row, col
    */
   public float height( float row , float col ) {

      int current_col = roundd( col , ncols );

      Vector3D P0 = getPos( roundd( row , nrows ), current_col );

      if( P0 == null )
         return Float.NaN;
      
      Vector3D P1;

      if ( row < nrows )
        P1 = getPos( roundd( row + 1 , nrows ), current_col );
      else 
        P1 = getPos( roundd( row - 1 , nrows ), current_col );

      if ( P1 == null )
        return Float.NaN;

      P1.subtract( P0 );
      return P1.length();
   }

   /**
    *  * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    * @return the depth of the Grid element at row, col
    */
   public float depth( float row , float col ) {
      
      return depth;
   }


   /**
    * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    * @return the unit vector representing the direction in 3D from the 
    *         Data elements at row,col and the Data element in the next
    *         column
    */
   public Vector3D x_vec( float row , float col ) {

      boolean negate  = false;
      int current_row = roundd( row, nrows );

      Vector3D P0 = getPos( current_row, roundd( col , ncols ) );
      if( P0 == null )
         return null;
      
      Vector3D P1;
      if ( col < ncols ) 
        P1 = getPos( current_row, roundd( col + 1 , ncols ) );

      else {
        P1 = getPos( current_row, roundd( col - 1 , ncols ) );
        negate = true;
      }

      if( P1 == null)
         return null;
      
      P1.subtract( P0 );
      P1.normalize();
      if ( negate )
        P1.multiply(-1);
      
      return P1;
   }

   /**
    * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    * @return the unit vector representing the direction in 3D from the 
    *         Data elements at row,col and the Data element in the next
    *         row
    */
   public Vector3D y_vec( float row , float col ) {

      boolean negate  = false;
      int current_col = roundd( col, ncols );

      Vector3D P0 = getPos( roundd( row , nrows ), current_col );
      if( P0 == null )
         return null;
      
      Vector3D P1;
      if ( row < nrows )
        P1 = getPos( roundd( row + 1 , nrows ), current_col );
      
      else {
        P1 = getPos( roundd( row - 1 , nrows ), current_col );
        negate = true;
      }
      
      if ( P1 == null )
        return null;

      P1.subtract( P0 );
      P1.normalize();
      if ( negate )
        P1.multiply( - 1f );

      return P1;
   }


   /**
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    * @return the cross product of x_vec and y_vec
    */
   public Vector3D z_vec( float row , float col ) {

      Vector3D V = x_vec( row , col );
      
      if( V == null )
         return null;
      
      Vector3D V1 = y_vec( row , col );
      
      if( V1 == null )
         return null;

      V.cross( V , V1 );
      V.normalize();
      return V;
   }


   /**
    *  Sets the data blocks into this grid at the given row
    *  and column in its PixelInfoList Attribute if the ID of
    *  the associated grid matches this Grid's ID
    *  
    *  @param ds   the DataSet with the Data Grids to be added to
    *              this grid
    */
   public boolean setData_entries( DataSet ds ) {

      NSet = 0;
      clearData_entries();
      for( int i = 0 ; i < ds.getNum_entries() ; i++ ) {
         
         PixelInfoList plist = AttrUtil
                  .getPixelInfoList( ds.getData_entry( i ) );
         
         if( plist != null && plist.num_pixels() > 0 ) {
            
            IPixelInfo pinf = plist.pixel( 0 );
            if( pinf.gridID() == ID ) {
               
               if( Grid[ (int) pinf.row() - 1 ][ (int) pinf.col() - 1 ] == null )
                  NSet++ ;
               
               Grid[ (int) pinf.row() - 1 ][ (int) pinf.col() - 1 ] = ds
                        .getData_entry( i );
               
            }
         }
      }
      
      filled = true;
      return true;
   }


   /**
    *  * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    * @return the data entry in this grid at the given row and column
    */
   public Data getData_entry( int row , int col ) {

      if( row < 1 || col < 1 || row > nrows || col > ncols )
         return null;

      return Grid[ row - 1 ][ col - 1 ];

      // TODO Auto-generated method stub

   }

   /**
    * @return true if the data blocks are set into this Grid,
    *              otherwise false 
    */
   public boolean isData_entered() {


      return filled;
   }


   /**
    * Clears out all the data entries in this grid
    */
   public void clearData_entries() {

      Grid = new Data[ nrows ][ ncols ];
      
      for( int i = 0 ; i < nrows ; i++ )
         for( int j = 0 ; j < ncols ; j++ )
            
            Grid[ i ][ j ] = null;

      filled = false;
      NSet = 0;

   }


   /**
    *  * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    * Not Implemented Yet
    */
   public float SolidAngle( float row , float col ) {
      
      return Float.NaN;
   }


   /**
    * 
    * @param row  the row in question in this grid
    * @param col the column in question in this grid
    * 
    * Not implemented yet
    */
   public float Delta2Theta( float row , float col ) {
     
      return Float.NaN;
   }


   /**
    * Attempts to find a matching UniformGrid, CyclinderGrid or Spherical grid
    * to return
    * 
    * @param G           the Grid with an array of data blocks containing 
    *                    position info
    *  
    * @param tolerance   The maximum error allowed between any given position
    *                    and its calculated positions 
    * 
    * @return A matching grid with more structure or this grid.
    * 
    */
   public static IDataGrid GetDataGrid( RowColGrid G , float tolerance ) {

      UniformGrid U = RowColGrid.getUniformDataGrid( G , tolerance );
      
      if( U != null )
         return U;
      
      return G;
   }


   /**
    * Attempts to find a matching UniformGrid
    * 
    * @param G           the Grid with an array of data blocks containing 
    *                    position info
    *  
    * @param tolerance   The maximum error allowed between any given position
    *                    and its calculated positions 
    *              
    * @return A matching grid with more structure or null.
    * 
    * NOTE: The resultant grid must be set in the pixelInfo attribute and the
    * data blocks for this grid should be set.
    */
   public static UniformGrid getUniformDataGrid( RowColGrid G, 
                                                 float tolerance ) {

      float width = G.width();
      float height = G.height();
      Vector3D xDir = G.x_vec();
      Vector3D yDir = G.y_vec();
      Vector3D center = G.position();


      if( Float.isNaN( width ) || Float.isNaN( height ) || xDir == null
               || yDir == null || center == null )
         return null;

      UniformGrid UGrid = new UniformGrid( G.ID() , G.units() , center , xDir ,
               yDir , width , height , G.depth , G.num_rows() , G.num_cols() );

      for( int row = 0 ; row < G.num_rows() ; row++ )
         for( int col = 0 ; col < G.num_cols() ; col++ ) {

            Vector3D P = G.position( row + 1 , col + 1 );
            P.subtract( UGrid.position( row + 1 , col + 1 ) );
            if( P.length() > tolerance ) 

                return null; 

         }

      return UGrid;
   }


   /**
    * Sets this grid as the main(1st) grid in the DataSet and then sets the data
    *  blocks into the grid's table of data blocks
    * 
    * @param Gr
    *           The RowColGrid with info about which data blocks are at a given
    *           row and column
    * @param grid
    *           The grid to place in the pixelinfo attribute
    * 
    * @param DS
    *           The data set containing this grid
    */
   public static void setDataSet( RowColGrid Gr , IDataGrid grid , DataSet DS ) {

      if( grid == null)
         throw new IllegalArgumentException("new grid is null");
      
      if( Gr == null)
         throw new IllegalArgumentException("new grid is null");
      
      if( grid.num_rows() != Gr.num_rows() || grid.num_cols() != Gr.num_cols())
         throw new IllegalArgumentException("Grid sizes do not match");
      
      for( int row = 0 ; row < Gr.nrows ; row++ )
         for( int col = 0 ; col < Gr.ncols ; col++ ) {
            Data D = Gr.getData_entry( row + 1 , col + 1 );

            if( D != null ) {
               
               PixelInfoList plist = new PixelInfoList( new DetectorPixelInfo(
                        grid.ID() , (short) ( row + 1 ) , (short) ( col + 1 ) ,
                        grid ) );

               D.setAttribute( new PixelInfoListAttribute(
                        Attribute.PIXEL_INFO_LIST , plist ) );
            }

         }

      grid.setData_entries( DS );
      
                  // now add data set operators
      
      DS.addOperator( new DataSetTools.operator.DataSet.
                Attribute.GetPixelInfo_op() );

   }


   /**
    * Test program for this module
    * @param args
    */
   public static void main( String[] args ) {

      int nrows = ( new Integer( args[ 0 ] ) ).intValue();
      int ncols = ( new Integer( args[ 1 ] ) ).intValue();
      int ntimes = 20;
      RowColGrid Gr = new RowColGrid( nrows , ncols , 105 );

      DataSet DS = new DataSet( "test" , "new dataset" , "m" , "time" ,
               "counts" , "intensity" );
      UniformGrid UGrid = new UniformGrid( 3 , "m" , new Vector3D( 2 , 1 , 3 ) ,
               new Vector3D( 0 , 1 , 0 ) , new Vector3D( 0 , 0 , 1 ) , 5f , 4f ,
               .1f , nrows , ncols );
      System.out.println( "UGrid LB,RB,LT,RT=" + UGrid.position( .5f , .5f )
               + "::" + UGrid.position( .5f , 8.5f ) + "::"
               + UGrid.position( 6.5f , .5f ) + "::"
               + UGrid.position( 6.5f , 8.5f ) );
      int xx;
      for( int row = 0 ; row < nrows ; row++ )
         for( int col = 0 ; col < ncols ; col++ ) {
            float[] Time = new float[ ntimes ];
            float[] yvals = new float[ ntimes ];
            for( int time = 0 ; time < ntimes ; time++ ) {
               Time[ time ] = 1000 + time * .2f;
               yvals[ time ] = 2 + row + col / (float) ncols + time * col + .1f;
            }
            FunctionTable D = new FunctionTable( new VariableXScale( Time ) ,
                     yvals , 1 + row * ncols + col );
            Gr.setOneData( D , row + 1 , col + 1 );
            if( row == 3 && col == 4 )
               xx = 1;
            Vector3D V = UGrid.position( row + 1 , col + 1 );
            if( row == 3 && col == 4 )
               Gr.show( "pos assigned to center=" , new Position3D( V ) );
            D.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS ,
                     new DetectorPosition( V ) ) );
            if( row < 2 && col < 2 ) {
               float[] F = AttrUtil.getDetectorPosition( D )
                        .getCartesianCoords();
               // System.out.println("rc
               // pos="+row+","+col+"=="+F[0]+","+F[1]+","+F[2]);
               // System.out.println("UGrid value="+UGrid.position(row+1,col+1)
               // );
            }
            DS.addData_entry( D );
         }
      ScriptUtil.display( DS.clone() );
      System.out.println( "Grid width=" + Gr.width() );
      System.out.println( "Grid height= " + Gr.height() );
      System.out.println( "Grid Center =" + Gr.position() );
      UniformGrid grid = RowColGrid.getUniformDataGrid( Gr , .0001f );
      if( grid == null ) {
         System.out.println( "Could not make a uniform Grid" );
      }
      else {
         Gr.setDataSet( Gr , grid , DS );
         DS.setTitle( "Gridded" );
         ScriptUtil.display( DS );
      }

   }

}
