/*
 * File: GetUB.java 
 *             
 * Copyright (C) 2005, Ruth Mikkelson
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
 * Revision 1.8  2008/01/29 19:16:46  rmikk
 * Repllaced Peak by IPeak
 *
 * Revision 1.7  2008/01/11 17:04:13  rmikk
 * Replaced (int) by Math.floor
 * Deleted some dead code
 * Eliminated unused parameters and added a Stats(output) parameter
 * Zero in searching for negative or positive numbers is now a percentage of a
 *    maximum value
 *
 * Revision 1.6  2008/01/07 20:14:39  rmikk
 * Replace one argument(unused) by an output float[] value with stats
 *
 * Revision 1.5  2007/08/23 21:05:03  dennis
 * Removed unused imports.
 *
 * Revision 1.4  2007/08/06 14:29:43  rmikk
 * Changed some bounds to determine that determines when correlations go from
 *   positive to negative and go from negative to positive
 *
 * Revision 1.3  2007/05/14 22:02:28  rmikk
 * Added public methods and variables so an external program can be used for
 *    testing and viewing the results of subprocesses
 *
 * Revision 1.2  2007/04/27 12:58:00  rmikk
 * Fixed javadoc errors
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import gov.anl.ipns.MathTools.LinearAlgebra;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.*;

import Command.ScriptUtil;
import Command.Script_Class_List_Handler;
import IPNSSrc.blind;
import gov.anl.ipns.Util.SpecialStrings.*;

/**
 * @author Ruth
 * TODO:
 *    1. Do not save those where 1/d> maxXtal length
 *    2. Eliminate middle 30%. If that is none or r increases then include
 *       otherwise don't include
 *    3. Check maxCorrIndex and minCorrIndex.  
 *     if( minCorrIndex < .3 maxCorrIndex or minCorrIndex >.7 maxCorrIndex
 *      do not cache.
 * 
 */
public class GetUB {

   public static int FIT            = 6; // Not used

   public static int CORR           = 1;

   public static int LEN            = 2; // length on line in dir

   public static int X              = 3;

   public static int Y              = 4;

   public static int NBINS_PER_LINE = 5;

   public static int FIT1           = 0; // crude approx for fraction of
                                          // intensity that
   public static float[][] List     = null;
   
   public static int Nelements      =0;
   
   public static boolean debug      = false;

   // lie within .3q of a plane.

   public static float[]    xixj     = null;
   
  // private static float[] line = new float[61];

   /**
    * 
    */
   public GetUB() {

      super();
      List = null;
      Nelements =0;
      xixj = null;
      // TODO Auto-generated constructor stub
   }


   public static float[][] getUB( Vector Peaks ) {

      return null;
   }


   /**
    * Project all the peaks onto a line( thru the origin and unit vector
    * (x,y,z), z>=0).
    * 
    * @param x
    *           x coordinate of the unit vector on the line to project the peaks
    *           to
    * @param y
    *           y coordinate of the unit vector on the line to project the peaks
    *           to
    * @param Peaks
    *           A Vector of peaks
    * @param omit
    *           a boolean array telling which peaks to omit if true
    * @param MaxXtallengthReal
    *           The maximum length in real space of the side of the crystal
    * @return Each bin width on the line is delta. The center bin represents 0
    */
   public static float[] ProjectPeakToDir( float x , float y , Vector Peaks ,
            boolean[] omit , float MaxXtallengthReal, float[] line )
            throws IllegalArgumentException {

      float[] Res = line;
      Arrays.fill( Res,0f);
      float delta = 1 / MaxXtallengthReal / 4f / 12f;
      if( Peaks == null )
         throw new IllegalArgumentException( "null Peaks Vector" );
      if( omit == null ) {
         omit = new boolean[ Peaks.size() ];
         Arrays.fill( omit , false );
      }
      if( omit.length < Peaks.size() ) {
         boolean[] omit1 = new boolean[ Peaks.size() ];
         Arrays.fill( omit1 , false );
         System.arraycopy( omit , 0 , omit1 , 0 , omit.length );
         omit = omit1;
      }
      if( delta <= 0 )
         throw new IllegalArgumentException( "step size must be positive" );
      float z = 0;
      if( 1 - x * x - y * y >= 0 )
         z = (float) Math.sqrt( 1 - x * x - y * y );
      int center = Res.length / 2;
      for( int i = 0 ; i < Peaks.size() ; i++ )
         if( ! omit[ i ] ) {
            IPeak pk = (IPeak) Peaks.elementAt( i );
            double[] Qvec = pk.getUnrotQ();
            float p = (float) ( Qvec[ 0 ] * x + Qvec[ 1 ] * y + Qvec[ 2 ] * z );
            int index = (int)Math.floor ( p / delta + .5 );
            if( Math.abs( index ) >= Res.length / 2 ) {
               float[] Res1 = new float[ 2 * Math.abs( index ) + 1 + 10 ];
               Arrays.fill(Res1,0f);
               System.arraycopy( Res , 0 , Res1 ,
                        ( Res1.length - Res.length ) / 2 , Res.length );
               Res = Res1;
               line = Res;
               center = Res.length / 2;
            }
            Res[ center + index ] += pk.ipkobs();
         }
      
      return Res;
   }


   public static int findMinNonZero( float[] binnedData ) {

      int minIndex = - 1;
      for( int i = 0 ; ( i < binnedData.length ) && ( minIndex < 0 ) ; i++ )
         if( binnedData[ i ] != 0 )
            minIndex = i;
      return minIndex;
   }


   public static int findMaxNonZero( float[] binnedData ) {

      int maxIndex = - 1;
      for( int i = binnedData.length - 1 ; i >= 0 && ( maxIndex < 0 ) ; i-- )
         if( binnedData[ i ] != 0 )
            maxIndex = i;
      return maxIndex;
   }


   private static void show( float[][] List ) {

      System.out.println( "------------------------------" );
      if( List == null )
         return;
      for( int i = 0 ; i < List.length ; i++ ) {
         System.out.print( i + ":" );
         for( int j = 0 ; j < List[ i ].length ; j++ )
            System.out.print( List[ i ][ j ] + "  " );
         System.out.println( "" );
      }
      System.out.println( "----------------------------------------" );
   }

   public static void showDirs( float[][] Dirs){
      java.text.DecimalFormat dform3 =new java.text.DecimalFormat(" 0000.0000 ;-0000.0000");
      StringBuffer sb = new StringBuffer(35);
      if( Dirs == null){
         System.out.println("Directions of plane normals is null");
         return;
      }
      for( int i=0; i< Dirs.length; i++){
         double L =Math.sqrt(Dirs[i][0]*Dirs[i][0]+Dirs[i][1]*Dirs[i][1]+
                                                        Dirs[i][2]*Dirs[i][2]);
         for( int j=0; j< 3; j++)
           sb= dform3.format(Dirs[i][j]/L,sb, new FieldPosition( NumberFormat.FRACTION_FIELD));
         System.out.println( sb.append(":length="+L) );
         sb.setLength( 0 );
      }
      
 }

   /**
    * Finds best autocorrelation value, a several goodness of fits for planes
    * 
    * @param binnedData
    *           A bunch of histogram info
    * @param minIndex   The start index of binnedData to be considered
    * @param maxIndex   the last index of the binnedData to be considered
    * @return a 7 tuple where first pair is correlation of leading direction
    *         then fraction that map to planes withih .2 for this direction. The
    *         next two are the analogous information for the second leading
    *         direction and the third pair is the analogous information for the
    *         3rd leading direction. The last position contains the number of
    *         candidates pick from.
    * 
    */
   public static float[] CalcStats( float[] binnedData , int minIndex ,
            int maxIndex ) {

      //

      int nspans = ( maxIndex - minIndex ) / 3;
      if( nspans <= 1 )
         return null;
      float[] xi_xj = new float[ nspans - 2 ];
      float[] xs_end = new float[ maxIndex - minIndex + 1 ];
      Arrays.fill( xi_xj , 0f );
      float sxx = 0;
      float minDat = 0;
      int maxDatIndx = minIndex;
      for( int i = minIndex ; i <= maxIndex ; i++ ) {
         if( binnedData[ i ] > binnedData[ maxDatIndx ] )
            maxDatIndx = i;
         if( binnedData[ i ] < minDat )
            minDat = binnedData[ i ];

         for( int k = 0 ; k <= i - minIndex ; k++ )
            xs_end[ k ] += binnedData[ i ];

         sxx += binnedData[ i ] * binnedData[ i ];
         for( int k = i + 2 ; ( k <= maxIndex ) && ( k - i - 2 < nspans - 2 ) ; k++ ) {
            xi_xj[ k - i - 2 ] += binnedData[ i ] * binnedData[ k ];
         }
      }

      float mu = xs_end[ 0 ] / ( maxIndex - minIndex + 1 );

      float sigsq = sxx - ( maxIndex - minIndex + 1 ) * mu * mu;
      sigsq = sigsq / ( maxIndex - minIndex );

      for( int i = 0 ; i < nspans - 2 ; i++ ) {
         int n = ( maxIndex - minIndex + 1 ) - i - 2;
         float z = 0;
         if( maxIndex - minIndex - i - 1 < xs_end.length )
            z = xs_end[ maxIndex - minIndex - i - 1 ];
         xi_xj[ i ] += - mu * xs_end[ i + 2 ] + n * mu * mu - mu
                  * ( ( maxIndex - minIndex + 1 ) * mu - z );
         xi_xj[ i ] = xi_xj[ i ] / ( n * sigsq );

      }
      xixj = xi_xj;
      boolean done = false;
      int faze = 0;
      int maxIndx = - 1;
      float zero = 0f;
      float max_xixj_start =0;
      for( int i = 0 ; ( i < xixj.length ) && ! done ; i++ ) {
         if( faze == 0 ) {  
            
            if( xixj[i] > max_xixj_start){
               
               max_xixj_start = xixj[i];
               zero = max_xixj_start/10;
            }
            
            if( xixj[ i ] < -zero )
               faze = 1;
               
            
         }
         else if( faze == 1 ) {
            if( xixj[ i ] > zero )
               faze = 2;
            maxIndx = i;
         }
         else if( faze == 2 ) {
            if( xixj[ i ] < -zero )
               faze = 3;
            else if( xixj[ i ] > xixj[ maxIndx ] ) {

               maxIndx = i;
            }
         }
         else
            done = true;
      }
      float[] Res = new float[ 7 ];
      if( faze >= 2 ) {
         Res[ CORR ] = xixj[ maxIndx ];
         Res[ LEN ] = maxIndx + 2;
         Res[ 5 ] = maxIndx + 2;
      }
      else
         return null;

 
      int center = binnedData.length / 2;
     
      Res[FIT] =0;
      float Tot = 0;
      float TotInt = 0;
      for( int i = 0 ; i < binnedData.length ; i++ )
         if( binnedData[ i ] > 0 ) {
            Tot += binnedData[ i ];
            float x = ( i - center ) / (float) ( maxIndx + 2 );
            if( Math.abs( x - Math.floor ( x + .5 ) ) < .2 )
               TotInt += binnedData[ i ];
         }
      Res[ FIT1 ] = 2 * TotInt /  Tot;
      return Res;
   }


   private static float[] doOneDirection( Vector Peaks , float x , float y ,
            boolean[] omit , float MaxXtallengthReal, float[] line ) {

      
      float delta = MaxXtallengthReal;
      int  Mx = - 1 , Mn = - 1;
      float[] Res = null;

      line = ProjectPeakToDir( x , y , Peaks , omit , delta, line );
      Mx = findMaxNonZero( line );
      Mn = findMinNonZero( line );
      

      Res = CalcStats( line , Mn , Mx );


      if( Res != null ) {
         Res[ LEN ] *=  1 / MaxXtallengthReal / 4f / 12f;;
         Res[ X ] = x;
         Res[ Y ] = y;
      }
      return Res;
   }


 
   //Can be deleted when Xplore disappears
   public static void getCandidateDirections( Vector Peaks , boolean[] omit ,
            float gridLength , float MaxXtalLengthReal, float[] line ){
      List = new float[ 50 ][ 7 ];
      Nelements = 0;
 
      float[]Res = doOneDirection( Peaks , 1f , 0f , omit , MaxXtalLengthReal,
               line);
      if( Res != null ) {
         InsertInList(  Res );
         
      }
      Res = doOneDirection( Peaks , 0f , 1f , omit , MaxXtalLengthReal,
               line);
      if( Res != null ) {
          InsertInList(  Res );
         
      }

      for( float x = - 1 + gridLength ; x < 1 - gridLength ; x += gridLength )
         for( float y = - (float) Math.sqrt( 1 - x * x ) ; y <= (float) Math
                  .sqrt( 1 - x * x ) ; y += gridLength ) {
            if( 1 - x * x - y * y >= 0 )
               Res = doOneDirection( Peaks , x , y , omit , MaxXtalLengthReal,line );
            else
               Res = null;
            if( Res != null )
              if( Res[ LEN ] >= 1/MaxXtalLengthReal){
                InsertInList(  Res );
               
            }
         }
     
   }
  /* //unused here and in Xplore
   private static float[][] getDirss( float gridLength,float NewDir, float[] code ){

      code[ 0 ] = 0;
      if( Nelements <= 0 )
         return null;
      float[] q1 , q2 , q3;
      q1 = new float[ 3 ];
      Arrays.fill( code , - 1f );
      code[ 6 ] = Nelements;
      float[] listEntry = List[ Nelements - 1 ];
      code[ 0 ] = listEntry[ CORR ];
      code[ 1 ] = listEntry[ FIT1 ] / 2f;
      float x = listEntry[ X ] , y = listEntry[ Y ] , scale = listEntry[ LEN ];
      q1[ 0 ] = x * scale;
      q1[ 1 ] = y * scale;
      q1[ 2 ] = (float) Math.sqrt( 1 - x * x - y * y ) * scale;

      listEntry = FindNextTop1( List , Nelements , x , y , - 10 , - 10 , NewDir ,
               gridLength );
      if( listEntry == null ) {
         float[][] Res1 = new float[ 1 ][ 3 ];
         Res1[ 0 ] = q1;
         //show( Res1 );
         return Res1;
      }
      code[ 2 ] = listEntry[ CORR ];
      code[ 3 ] = listEntry[ FIT1 ] / 2f;
      q2 = new float[ 3 ];
      float x1 = listEntry[ X ] , y1 = listEntry[ Y ];
      scale = listEntry[ LEN ];
      q2[ 0 ] = x1 * scale;
      q2[ 1 ] = y1 * scale;
      q2[ 2 ] = (float) Math.sqrt( 1 - x1 * x1 - y1 * y1 ) * scale;

      listEntry = FindNextTop1( List , Nelements , x , y , x1 , y1 , NewDir ,
               gridLength );
      if( listEntry == null ) {
         float[][] Res1 = new float[ 2 ][ 3 ];

         Res1[ 0 ] = q1;
         Res1[ 1 ] = q2;
         //show( Res1 );
         return Res1;
      }
      code[ 4 ] = listEntry[ CORR ];
      code[ 5 ] = listEntry[ FIT1 ] / 2f;
      q3 = new float[ 3 ];
      x = listEntry[ X ];
      y = listEntry[ Y ];
      scale = listEntry[ LEN ];
      q3[ 0 ] = x * scale;
      q3[ 1 ] = y * scale;
      q3[ 2 ] = (float) Math.sqrt( 1 - x * x - y * y ) * scale;
      float[][] Res1 = new float[ 3 ][ 3 ];
      Res1[ 0 ] = q1;
      Res1[ 1 ] = q2;
      Res1[ 2 ] = q3;
      //show( Res1 );


      return Res1;

   }
 */
   private static void insert( float x, float[]elimList){
      for( int i=0; i<elimList.length; i++){
         if( elimList[i] <-5){
            elimList[i]=x;
            return;
         }
      }
   }
   /**
    * Attempts to automatically create a vector of normals to 3 planes whose
    * lengths correspond to the distance between the corresponding planes from a
    * Vector of Peaks
    * 
    * @param Peaks
    *           The vector of peaks
    * @param omit
    *           The peaks to omit
    * @param gridLength
    *           Directions are determined by a point in the upper half of the
    *           unit circle( z is sqrt(1-x^2-y^2). This is the length between
    *           two consecutive "directions" in x and in the y direction(.01 is
    *           best)
    * @param NewDir
    *           This determines if two directions are the "same". It is the
    *           distance in x dir and y dir.
    * @param code
    *           a code that, if this algorithm fails to find 3 vectors, what
    *           parameters to tweak code[0]=# of directions to choose from
    *           code[0] = Min corr for the directions chosen code[0]=2- two of
    *           the resultant vectors are close( lower newDir or up gridLength)
    * @return  at most 3 float[3] values  each representing a direction of a normal
    *          and whose length is the distance between successive planes
    */
   public static float[][] getPlaneVectors( Vector Peaks , boolean[] omit ,
            float gridLength , float NewDir , float[] code ,
            float MaxXtalLengthReal ) {

      List = new float[ 50 ][ 7 ];
      Nelements = 0;
      code[ 0 ] = 0;
      float[] line = new float[300];
      float[] Res = doOneDirection( Peaks , 0f , 0f , omit , MaxXtalLengthReal,
               line);
      if( Res != null ) {
         InsertInList(  Res );
         
      }
      Res = doOneDirection( Peaks , 1f , 0f , omit , MaxXtalLengthReal,
               line);
      if( Res != null ) {
          InsertInList(  Res );
        
      }
      Res = doOneDirection( Peaks , 0f , 1f , omit , MaxXtalLengthReal,
               line);
      if( Res != null ) {
          InsertInList(  Res );
         
      }
     int k=0;
     for( float x = - 1 + gridLength ; x < 1 - gridLength ; x += gridLength )
         for( float y = - (float) Math.sqrt( 1 - x * x ) ; y <= (float) Math
                  .sqrt( 1 - x * x ) ; y += gridLength ) {
            
            if( 1 - x * x - y * y >= 0 )
               Res = doOneDirection( Peaks , x , y , omit , MaxXtalLengthReal,
                        line);
            else
               Res = null;
            if( Res != null ) {
               InsertInList( Res );
            }
            
         }
      
    
/*
     Thread[] thrds = new Thread[4];
       for( int i=0; i<4;i++)
          thrds[i] = new DoQuadrantDirections( Peaks, omit,  gridLength,
               MaxXtalLengthReal, i);
       Execute1( thrds);
*/
     //show( List );
      if( Nelements <= 0 )
         return null;
      float[] q1 , q2 , q3;
      q1 = new float[ 3 ];
      Arrays.fill( code , - 1f );
      float[] elimX= new float[12];
      float[] elimY = new float[12];
      Arrays.fill( elimX,-10 );
      Arrays.fill( elimY,-10 );
      code[ 6 ] = Nelements;
      float[] listEntry = List[ Nelements - 1 ];
      code[ 0 ] = listEntry[ CORR ];
      code[ 1 ] = listEntry[ FIT1 ] / 2f;
      float x = listEntry[ X ] ;
      float y = listEntry[ Y ] ;
      float scale = listEntry[ LEN ];
     
      q1[ 0 ] = x * scale;
      q1[ 1 ] = y * scale;
      q1[ 2 ] = (float) Math.sqrt( 1 - x *x - y * y ) * scale;
      elimX[0]=x;
      elimY[0]=y;
      listEntry = FindNextTop( List , Nelements , elimX , elimY, NewDir ,
               gridLength );
      if( listEntry == null ) {
         float[][] Res1 = new float[ 1 ][ 3 ];
         Res1[ 0 ] = q1;
         //show( Res1 );
         return Res1;
      }

      code[ 2 ] = listEntry[ CORR ];
      code[ 3 ] = listEntry[ FIT1 ] / 2f;
      q2 = new float[ 3 ];
      float x1 = listEntry[ X ] , y1 = listEntry[ Y ];

      elimX[1]=x1;
      elimY[1]=y1;
      scale = listEntry[ LEN ];
      q2[ 0 ] = x1 * scale;
      q2[ 1 ] = y1 * scale;
      q2[ 2 ] = (float) Math.sqrt( 1 - x1 * x1 - y1 * y1 ) * scale;
      boolean done = false;
      while( !done){
          listEntry = FindNextTop( List , Nelements , elimX , elimY , NewDir ,
               gridLength );
          if( listEntry == null)
            done = true;
          else if(check(listEntry,q1,q2))
             done = true;
          else{
             insert(listEntry[X], elimX);
             insert(listEntry[Y], elimY);
          }
          
          if( listEntry != null && listEntry[FIT1] < .5){
             done = true;
             listEntry = null;
          }
          
      }
      if( listEntry == null ) {
         float[][] Res1 = new float[ 2 ][ 3 ];

         Res1[ 0 ] = q1;
         Res1[ 1 ] = q2;
         //show( Res1 );
         return Res1;
      }
      code[ 4 ] = listEntry[ CORR ];
      code[ 5 ] = listEntry[ FIT1 ] / 2f;
      q3 = new float[ 3 ];
      x = listEntry[ X ];
      y = listEntry[ Y ];
      scale = listEntry[ LEN ];
      q3[ 0 ] = x * scale;
      q3[ 1 ] = y * scale;
      q3[ 2 ] = (float) Math.sqrt( 1 - x * x - y * y ) * scale;
      float[][] Res1 = new float[ 3 ][ 3 ];
      Res1[ 0 ] = q1;
      Res1[ 1 ] = q2;
      Res1[ 2 ] = q3;
      //show( Res1 );


      return Res1;


   }
   
   public static void Execute( Thread[] thrds){
      for( int i=0; i<4;i++){
         thrds[i].run();
         System.out.println("after thread "+i);
      }
      System.out.println("Through executing threads serially");
      
   }
   
   public static void Execute1( Thread[] thrds){
      for( int i=0; i<4;i++)
         thrds[i].start();
      
      for( int i=0; i<4; i++)
         try{
           thrds[i].join();
           System.out.println("Joined thread " + i);
         }catch( Throwable s){
            System.out.println("Thread "+i+" interrupted \n");
            s.printStackTrace();
         }
      
   }
   
   //Determines if this listEntry is coplanar
   private static boolean check( float[] listEntry, float[]q1, float[]q2){
      float x = listEntry[ X ];
      float y = listEntry[ Y ];
      float scale = listEntry[ LEN ];
      float[] q3= new float[3];
      q3[ 0 ] = x * scale;
      q3[ 1 ] = y * scale;
      q3[ 2 ] = (float) Math.sqrt( 1 - x * x - y * y ) * scale;
      float[][] M = new float[3][3];
      M[0]=q1;
      M[1]=q2;
      M[2]=q3;
      float Max = Math.max( MaxList(M[2]),Math.max( MaxList(M[0]) , MaxList(M[1]) ));
      Max = Max*Max*Max;
      if( Math.abs( LinearAlgebra.determinant( LinearAlgebra.float2double( M ) ))
               <.001*Max)
         return false;
      return true;
      
      
  
      
   }
   public static float MaxList( float[] list){
      
      if( list == null || list.length < 1)
         return Float.NaN;
      
      float Max = Math.abs( list[0]);
      for( int i=1; i< list.length; i++)
         if( Math.abs( list[i] )>Max)
            Max = Math.abs( list[i] );
      
      return Max;
      
   }

   
   
   /**
    *  Finds a new UB matrix. This matrix has been run through
    *         blind.
    * @param Peaks
    *           A Vector of Peaks
    * @param MaxXtalLengthReal
    *           The maximum length of crystal lattice in real space or -1 for
    *           default and adjustable
    * @param Stats (output)
    *           Will Contain the percent of all peaks whose index values are within
    *           .1, .2, .3,..  of an integer 
    * @return Returns a new UB matrix or null. This matrix has been run through
    *         blind.
    */
   public static float[][] GetUBMatrix( Vector Peaks , float MaxXtalLengthReal ,
            float[] Stats ) throws IllegalArgumentException {
      return GetUB.GetUBMatrix1(Peaks, MaxXtalLengthReal , Stats, -1f );
   }
   
   
   /**
    *  Finds a new UB matrix. This matrix has been run through
    *         blind.
    * @param Peaks
    *           A Vector of Peaks
    * @param MaxXtalLengthReal
    *           The maximum length of crystal lattice in real space or -1 for
    *           default and adjustable
    * @param Stats (output)
    *           Will Contain the percent of all peaks whose index values are within
    *           .1, .2, .3,..  of an integer 
    * @param Lengthgrid  Adjusts the fineness of search grid. A negative value or too
    *                    large of a value will be ignored and a default value
    *                    will be used
    * @return Returns a new UB matrix or null. This matrix has been run through
    *         blind.
    */
   
   public static float[][] GetUBMatrix1( Vector Peaks , float MaxXtalLengthReal ,
            float[] Stats, float Lengthgrid ) throws IllegalArgumentException {
   
      
      boolean done = false;
      float MaxLength = MaxXtalLengthReal;
      float MinNewDir =.2f;//not used
      if( MaxLength < 0 )
         MaxLength = 20;
      if( MinNewDir < 0 )
         MinNewDir = .5f;
      if( Stats != null)
         java.util.Arrays.fill( Stats , 0f );
      float[][] Dirs = null;
      boolean[] omit = null;
      float gridLength = .02f;
      if(Lengthgrid >0 && Lengthgrid <.3)
          gridLength = Lengthgrid;
      float DDir = .2f;
      int Nomitted = 0;
      
     // while( ! done ) 
      {
         float[] code = new float[ 7 ];

         code[ 6 ] = 0;
         while( ( code[ 6 ] < 100 ) && ( gridLength > .005 ) ) {
            
            Dirs = getPlaneVectors( Peaks , omit , gridLength , DDir , code ,
                     MaxLength );
            if( debug){
               System.out.println("------------------ Direcions ------------");
               System.out.println("grid delta=" +gridLength+":: Max unit cell "+
                        MaxLength);
               GetUB.showDirs( Dirs );
               
            }
            System.out.println("-------------------------------------------");
            if( code[ 6 ] < 100 || Dirs== null || Dirs.length < 3){
               gridLength = gridLength / 2f;
               
            }
         }
         int NomittedOld = Nomitted;
         for( int i = 0 ; i < 3 ; i++ ) {
            if( ( code[ 2 * i ] > 0 ) && ( code[ 2 * i + 1 ] > 0 ) ) {
               float P = -(code[2*i+1]-.5f)*.75f + .5f;
               if( P < .2f )
                  P = .2f;
               if( P < .47f ) {
                  if( omit == null )
                     omit = new boolean[ Peaks.size() ];
                     boolean[] omit_copy= new boolean[ Peaks.size() ];
                     System.arraycopy( omit, 0,omit_copy,0,omit.length);
                     
                  int N = 0;
                  if( Dirs != null && Dirs.length >i )
                      N = OmitPeaks( Peaks , Dirs[ i ] , omit_copy , P, false );
                  if( debug)
                     System.out.println("omitted "+N+" peaks for direction "+ i +
                              " at level "+ P);
                  omit= omit_copy;
                  Nomitted +=N;
                    
               }
            }
         }
         done = true;
         for( int i = 1 ; i < 6 ; i+=2 )  //done if a lot of high correlations
            if( code[ i ] < .75 )
               done = false;
        
         if( code[0] >.92 && code[2] >  .92 && code[4] > .92) //done if most points
               done = true;                                   //are on a plane
        
        if( (2f*Peaks.size())/Nomitted  >1.7)                 //but not if there are too
              done = false;                                   //many omitted points
        
         if( ( NomittedOld == Nomitted ) && ! done ) {
           
              
            gridLength /= 2f;
            
               
              
            if( gridLength < .005f )
                done = true;
            else 
               omit = null;
           
         }

      }// while !done
   
      if( Dirs == null )
         throw new IllegalArgumentException( " No directions found" );
      if( Dirs.length < 3 )
         throw new IllegalArgumentException( " Not enough directions found" );
      if( omit == null)
         omit = new boolean[ Peaks.size()];
     
      float[][] UB = UBMatrixFrPlanes( Dirs, Peaks, omit, Stats,1);
      IndexStat( UB,  Peaks, .3f, omit);
      return UBMatrixFrPlanes( Dirs , Peaks , omit, Stats,0 );

   }

   //stop==1 --> returns UB coresponding to the dirs
   //stop ==2 --> returns UB after optimization
   //otherwise it goes through blind
   public static float[][] UBMatrixFrPlanes( float[][] PlaneDirs ,
            Vector Peaks , boolean[] omit, float[]Stats , int stop) {

      if( PlaneDirs == null )
         return null;
      if( PlaneDirs.length < 3 )
         return null;
      if( Peaks == null )
         return null;
      if( Peaks.size() < 4 )
         return null;


      float[][] UB = new float[ 3 ][ 3 ];
      float[] L = new float[ 3 ];
      float[][] unit = new float[ 3 ][ 3 ];
      for( int i = 0 ; i < 3 ; i++ ) {
         L[ i ] = (float) Math.sqrt( PlaneDirs[ i ][ 0 ] * PlaneDirs[ i ][ 0 ]
                  + PlaneDirs[ i ][ 1 ] * PlaneDirs[ i ][ 1 ]
                  + PlaneDirs[ i ][ 2 ] * PlaneDirs[ i ][ 2 ] );
         for( int j = 0 ; j < 3 ; j++ ){
            unit[ i ][ j ] = PlaneDirs[ i ][ j ] / L[ i ];
            UB[i][j] = PlaneDirs[i][j]/(L[i]*L[i]);
         }
      }
      if( stop ==1)
         return UpdateStats(LinearAlgebra.getInverse( UB),Peaks,Stats) ;
      int k = Peaks.size();
      double q[][] = new double[ k ][ 3 ];
      double hkl[][] = new double[ k ][ 3 ];
      k = 0;
      int ct;
     
      for( int i = 0 ; i < Peaks.size() ; i++ ) {
         double[] qvec = ( (IPeak) Peaks.elementAt( i ) ).getUnrotQ();
         ct = 0;
         for( int j = 0 ; j < 3 ; j++ ) {
            double x = qvec[ 0 ] * unit[ j ][ 0 ] + qvec[ 1 ] * unit[ j ][ 1 ]
                     + qvec[ 2 ] * unit[ j ][ 2 ];
            x = x / L[ j ];
            if( ( omit == null ) || ( ! omit[ i ] ) ) {
               q[ k ] = qvec;
               hkl[ k ][ j ] = Math.floor( x + .5 );
               ct++ ;
            }
         }
         if( ct == 3 )
            k++ ;
      }
      double M[][] = new double[ 3 ][ 3 ];
      double q1[][] = new double[ k ][ 3 ];
      double hkl1[][] = new double[ k ][ 3 ];
      System.arraycopy( q , 0 , q1 , 0 , k );
      System.arraycopy( hkl , 0 , hkl1 , 0 , k );
      double std_dev = LinearAlgebra.BestFitMatrix( M , hkl1 , q1 );
      if( debug)
         System.out.println(" Best fit UB has an error of "+ std_dev);
      
      for( int i = 0 ; i < 3 ; i++ )
         for( int j = 0 ; j < 3 ; j++ )
            UB[ i ][ j ] = (float) M[ i ][ j ];
      if( stop ==2)
         return  UpdateStats(UB,Peaks,Stats);
      blind bl = new blind();
      ErrorString S = bl.blaue( UB );

      if( S == null ) {
         M = bl.UB;
         for( int i = 0 ; i < 3 ; i++ )
            for( int j = 0 ; j < 3 ; j++ )
               UB[ i ][ j ] = (float) M[ i ][ j ];
         
         return UpdateStats(UB, Peaks,Stats);
      }

      return null;
   }

   /**
    * Returns the fraction of peaks indexed with the UB matrix
    * @param UB   The UB matrix
    * @param Peaks  The peaks
    * @param level   The closeness a peak must be to the plane in 
    *                miller indices units
    * @return   the fraction of peaks indexed with the UB matrix  
    */
   public static float IndexStat( float[][] UB, Vector Peaks, float level,boolean[] omit){
      if( UB== null)
      return 0f;
      if( Peaks == null)
         return 0f;
      float[][] invUB = LinearAlgebra.getInverse( UB );
      if( invUB == null)
         return 0f;
      if( Peaks.size() <=0)
         return 0;
      if( level < 0)
         return 0;
      if( level > .5)
         return Peaks.size();
      if( omit != null && omit.length == Peaks.size())
         java.util.Arrays.fill(  omit , false );
      else
         omit = null;
      int ct = 0;
      for( int i=0; i< Peaks.size(); i++){
         double[] qvec = ( (IPeak) Peaks.elementAt( i ) ).getUnrotQ();
         int k=0;
         for( int j = 0 ; j < 3 ; j++ ) {
            double x = qvec[ 0 ] * invUB[ j ][ 0 ] + qvec[ 1 ] * invUB[ j ][ 1 ]
                     + qvec[ 2 ] * invUB[ j ][ 2 ];
          /*  boolean same = false;
            double y= x;
           if( x < 0) y=-x;
           if( y -(int)y < level) same=true;
           else if( 1-y+(int)y < level)same = true;
           */ 
            if( Math.abs( x - Math.floor ( x + .5 ) ) < level )
               k++;
         }
         
         if( k==3)
            ct++;
         else if( omit != null)
            omit[i]=true;
        
      }
      return ct/(float)Peaks.size();
     
   }
   private static float[][] UpdateStats( float[][]UB, Vector Peaks, float[]Stats){
      if( Stats == null || Stats.length < 1)
         return UB;
      float[][]UBinv = gov.anl.ipns.MathTools.LinearAlgebra.getInverse( UB );
      if( UBinv == null)
         return UB;
      java.util.Arrays.fill( Stats , 0f );
      for( int i=0; i< Peaks.size(); i++){
         IPeak P = (IPeak)Peaks.elementAt( i );
         double[] Qs = P.getUnrotQ();
         float Max =0;
         for(int r=0; r<3;r++){
            
            double MillerIndex = UBinv[r][0]*Qs[0]+ UBinv[r][1]*Qs[1]
                                                          + UBinv[r][2]*Qs[2];
            if( MillerIndex < 0)
               MillerIndex = -MillerIndex;  //(int) is truncate
            float err = (float)Math.min( 
                                        Math.abs( MillerIndex -(int)MillerIndex ),  
                                        Math.abs( (int)(MillerIndex+1 )-MillerIndex));
            
            if( err > Max)
               Max = err;
         }
         
         int StatInd = (int)(Max/.1f);
         if( StatInd < Stats.length)
            Stats[StatInd]++;
         
      }
      //Accumulate
      for( int i=1;i<Stats.length;i++)
         Stats[i] +=Stats[i-1];
      
      //Convert to Percent on Peaks.size()
      int L = Peaks.size();
      for( int i=0; i< Stats.length; i++)
         Stats[i]=Stats[i]*100.f/L;
      
      return UB;
      
   }
   
   private static float[] FindNextTop( float[][] Listt , int Nelts ,
            float[] elimX, float[] elimY,
            float NewDir , float gridLength ) {

    
      
      for( int i = Nelts - 1 ; i >= 0 ; i-- ) {
         float[] listElement = Listt[ i ];
         int nelims=0,
             nCandidates =0;
         for( int j=0; j<elimX.length && elimX[j] >-3 && nelims == nCandidates; j++){
            nelims++;
            if( ( listElement[ X ] < elimX[j] - NewDir )
                     || ( listElement[ X ] > (elimX[j] + NewDir) )
                     || ( listElement[ Y ] < (elimY[j] - NewDir) )
                     || ( listElement[ Y ] > (elimY[j] + NewDir) ) )
               if( ( Math.abs( listElement[ X ] - elimX[j] ) <= gridLength )
                        && ( Math.abs( listElement[ Y ] - elimY[j ]) <= gridLength ))// on
                                                                                    // boundary
                                                                                    // try
                                                                                    // to
                                                                                    // increase
                                                                                    // NewDir
                  return FindNextTop( Listt , Nelts , elimX , elimY , 
                                        NewDir + 2 * gridLength , gridLength );
               else
                 nCandidates++;
              
         }
         if( nCandidates == nelims)
            return listElement;
         
      }
      
      return null;
   }
   
   
   
   private static synchronized void InsertInList( float[] Res ){
      int x;
      x=3;
      
      if( List.length < Nelements + 1 ) {
         float[][] List1 = new float[ Nelements + 12 ][ 7 ];
         System.arraycopy( List , 0 , List1 , 0 , Nelements );
         List = List1;
      }
      if( Nelements >0 &&( Math.abs( List[Nelements-1][X]+.26)>.00001 ||
               Math.abs( List[Nelements-1][Y]-.44)>.00001))
           x=3;
      boolean done = false;
      float key = Res[ FIT1 ] + Res[ CORR ];
      for( int i = Nelements - 1 ; ( i >= 0 ) && ! done ; i-- ) {
         if( List[ i ][ FIT1 ] + List[ i ][ CORR ] > key )
            List[ i + 1 ] = List[ i ];
         else {
            List[ i + 1 ] = Res;
            done = true;
         }
      }
      if( ! done )
         List[ 0 ] = Res;
      Nelements++;

     
   }


   public static int OmitPeaks( Vector Peaks , float[] qNormal ,
            boolean[] omit , float level, boolean count ) {

      int Res = 0;
      if( omit == null )
         return Res;
      if( Peaks == null )
         return Res;
      if( qNormal == null )
         return Res;
      if( qNormal.length != 3 )
         return Res;
      if( Peaks.size() != omit.length )
         return Res;
      int c = 0;
      float Lsq = qNormal[ 0 ] * qNormal[ 0 ] + qNormal[ 1 ] * qNormal[ 1 ]
               + qNormal[ 2 ] * qNormal[ 2 ];
      for( int i = 0 ; i < Peaks.size() ; i++ )
         if( ! omit[ i ] ) {
            double[] qvec = ( (IPeak) Peaks.elementAt( i ) ).getUnrotQ();
            double x = qvec[ 0 ] * qNormal[ 0 ] + qvec[ 1 ] * qNormal[ 1 ]
                     + qvec[ 2 ] * qNormal[ 2 ];
            x = x / Lsq;
            
            if( Math.abs( x - Math.floor ( x + .5 ) ) > level ) {
               if( !count) omit[ i ] = true;
               c++ ;
               Res++ ;
            }

         }
     // System.out.println( "omitted " + c + " peaks of" + Peaks.size() );
      return Res;
   }
    static class DoQuadrantDirections extends Thread{
      Vector Peaks;
      boolean[] omit;
      float gridLength;
      float MaxXtalLengthReal;
      int Quadrant;
      public DoQuadrantDirections( Vector Peaks, boolean[] omit, float gridLength,
              float  MaxXtalLengthReal, int Quadrant){
         this.Peaks = Peaks;
         this.omit = omit;
         this.gridLength = gridLength;
         this.MaxXtalLengthReal = MaxXtalLengthReal;
         this.Quadrant = Quadrant;
      }
      public void run(){
         float[] Res=null;
         float xstart = -1+gridLength;
         float xend = 0-gridLength/2; 
         int Q = Quadrant;
         if( Quadrant >1){
            xstart = 0;
            xend = 1 - gridLength;
            Q = Quadrant-2;
         }
         float[] line = new float[300];
         
         for( float x = xstart ; x < xend ; x += gridLength )
            for( float y = - (float) Math.sqrt( 1 - x * x )*Q ; y <= (float) Math
                     .sqrt( 1 - x * x )*(1-Q) ; y += gridLength ) {
               if( 1 - x * x - y * y >= 0 )
                  Res = GetUB.doOneDirection( Peaks , x , y , omit , MaxXtalLengthReal,
                           line);
               else
                  Res = null;
               if( Res != null ) {
                 if( Math.abs( x+.26 )<.00001 && Math.abs( y-.44 )<.0001)
                    System.out.println( "**"+ Res[0]+" "+Res[1]+"  "+Res[2]+
                             "  "+Res[5]);
                 GetUB.InsertInList( Res );
               }
            }
      }
   }

   /**
    * @param args
    */
   public static void main( String[] args ) {
      String filename = null;
      if( args!= null && args.length >0)
         filename = args[0];
      else{
         javax.swing.JFileChooser jf=(new javax.swing.JFileChooser());
         if( jf.showOpenDialog( null )!=javax.swing.JFileChooser.APPROVE_OPTION){
            System.out.println("Need to have a peaks file)");
            System.exit(0);
         }
         java.io.File f = jf.getSelectedFile();
         filename = f.toString();
         System.out.println("filename is "+filename);
         
      }
      Vector Peaks = (Vector) ( new DataSetTools.operator.Generic.TOF_SCD.ReadPeaks(
               filename ) ).getResult();
      if( Peaks == null){
         System.out.println("Cannot read Peaks file");
         System.exit(0);
            
      }
      float x = 0;
      float y ;
      x = 0.037499327f;
      y = -0.89929664f;
      float near = .5f;
      float grid = .1f;
      float range = .3f;
      float Xtal = 20f;
      float[] linee = new float[300];
      boolean[] omit = null;
      float[][] Dirs = null;
      float[][] UB = null;
      java.text.DecimalFormat dform =new java.text.DecimalFormat("#.####  ");
      while( grid > 0 ) {
         System.out.println( "Enter option desired" );
         System.out.println( "  x: enter new x value" );
         System.out.println( "  y: enter new y value" );
         System.out.println( "  g: enter new grid length" );
         System.out.println( "  x: enter new max xtal length" );
         System.out.println( "  n: enter min dist between directions" );
         System.out.println( "  1: Get projection on 1 Line " );
         System.out.println( "  s: statistics on one line" );
         System.out.println( "  O:   doOneDirection" );
         System.out.println( "  a:  Do all directions" );
         System.out.println( "  o1,o2,o3: Omit peaks out of range" );
         System.out.println( "  r: The range to omit peaks" );
         System.out.println( "  u: Calc UB matrix" );
         System.out.println( "  S: Save UB matrix" );
         System.out.println( "  w: Whole thing" );

         System.out.println( "  v: view settings" );
         String L = Script_Class_List_Handler.getString();
         L = L.trim();
         if( L.startsWith( "x" ) ) {
            L = Script_Class_List_Handler.getString();
            x = ( new Float( L.trim() ) ).floatValue();
         }
         else if( L.startsWith( "y" ) ) {
            L = Script_Class_List_Handler.getString();
            y = ( new Float( L.trim() ) ).floatValue();

         }
         else if( L.startsWith( "g" ) ) {
            L = Script_Class_List_Handler.getString();
            grid = ( new Float( L.trim() ) ).floatValue();

         }
         else if( L.startsWith( "x" ) ) {
            L = Script_Class_List_Handler.getString();
            Xtal = ( new Float( L.trim() ) ).floatValue();

         }
         else if( L.startsWith( "n" ) ) {
            L = Script_Class_List_Handler.getString();
            near = ( new Float( L.trim() ) ).floatValue();

         }
         else if( L.startsWith( "l" ) ) {
            linee = GetUB.ProjectPeakToDir( x , y , Peaks , omit , 20f ,
                     linee);
            if(linee != null)
               for( int i=0;i< linee.length; i++)
                  if( i%25 ==0)
                    System.out.println( dform.format(linee[i]));
                  else 
                     System.out.print( dform.format(linee[i]));
            else
               System.out.println("null");
         }
         else if( L.startsWith( "s" ) ) {
            xixj = null;
            float[] Res = GetUB.CalcStats( linee , GetUB.findMinNonZero( linee ) ,
                     GetUB.findMaxNonZero( linee ) );
            if( Res == null )
               System.out.println( "null" );
            else {
               for( int i = 0 ; i < Res.length ; i++ )
                  if( i%25==0)
                     System.out.println( dform.format(Res[ i ] ) );
                  else
                     System.out.print( dform.format(Res[ i ] ) );
                     
               System.out.println( "" );
            }

            System.out.print( "Correlations =" );
            if( xixj == null )
               System.out.print( "null" );
            else
               for( int i = 0 ; i < xixj.length ; i++ )
                  if( i%25==0)
                      System.out.println( dform.format(xixj[ i ]));
                  else
                     System.out.print( dform.format(xixj[ i ]));
            System.out.println( "" );
         }
         else if( L.startsWith( "a" ) ) {

            float[] code = new float[ 7 ];
            Dirs = GetUB.getPlaneVectors( Peaks , omit , grid , near , code ,
                     Xtal );
            ScriptUtil.display( code );
         }
         else if( L.startsWith( "o1" ) ) {
            if( range < 0 )
               omit = null;
            else {
               if( omit == null )
                  omit = new boolean[ Peaks.size() ];
               if( Dirs != null && Dirs.length >=1)
               GetUB.OmitPeaks( Peaks , Dirs[ 0 ] , omit , range,false );
            }

         }
         else if( L.startsWith( "o2" ) ) {
            if( range < 0 )
               omit = null;
            else {
               if( omit == null )
                  omit = new boolean[ Peaks.size() ];
               if( Dirs != null && Dirs.length >= 2)
                  GetUB.OmitPeaks( Peaks , Dirs[ 1 ] , omit , range,false );
            }

         }
         else if( L.startsWith( "o3" ) ) {
            if( range < 0 )
               omit = null;
            else {
               if( omit == null )
                  omit = new boolean[ Peaks.size() ];
               if( Dirs != null && Dirs.length >= 3)
                  GetUB.OmitPeaks( Peaks , Dirs[ 2 ] , omit , range, false );
            }
         }
         else if( L.startsWith( "w" ) ) {
            java.util.GregorianCalendar Cal = new java.util.GregorianCalendar();
            long start_time =Cal.getTimeInMillis();
            float[]Stats = new float[8];
            UB = GetUB.GetUBMatrix( Peaks , 20f , Stats);
            long end_time = Cal.getTimeInMillis();
            System.out.println("Time(ms) ="+ (end_time - start_time));
            ScriptUtil.display( UB );
            System.out.print("Stats=");
            ScriptUtil.display( Stats);
         }
         else if( L.startsWith( "r" ) ) {

            L = Script_Class_List_Handler.getString();
            range = ( new Float( L.trim() ) ).floatValue();
         }
         else if( L.startsWith( "v" ) ) {
            System.out.println( "x,y,near,gridlength,range=" + x + ";" + y + ";"
                     + near + ";" + grid + ";" + range );
         }
         else if( L.startsWith( "u" ) ) {


            if( Dirs != null && Dirs.length == 3 ) {
               UB = GetUB.UBMatrixFrPlanes( Dirs , Peaks , omit ,null,0);
               ScriptUtil.display( UB );
            }
            else
               ScriptUtil.display( " Not enough directions" );
         }
         else if( L.startsWith( "S" ) ) {

            if( UB != null ) {
               double[][] UBd = new double[ 3 ][ 3 ];
               for( int i = 0 ; i < 3 ; i++ )
                  for( int j = 0 ; j < 3 ; j++ )
                     UBd[ i ][ j ] = UB[ i ][ j ];
               double[] abcd = DataSetTools.operator.Generic.TOF_SCD.Util
                        .abc( UBd );
               float[] abc = new float[ 7 ];
               for( int i = 0 ; i < 7 ; i++ )
                  abc[ i ] = (float) abcd[ i ];
               String xx = "";
               if( L.length() > 1 )
                  xx = L.substring( 1 );
               DataSetTools.operator.Generic.TOF_SCD.Util.writeMatrix( "c:/UB"
                        + xx + ".mat" , UB , abc , new float[ 8 ] );
               System.out.println( "File written to UB" + xx + ".mat" );
            }
         }
         else if( L.startsWith( "O" ) ) {
            ScriptUtil.display( GetUB.doOneDirection( Peaks , x , y , omit ,
                     Xtal,linee ) );

         }
      }
   }
   
   
}
