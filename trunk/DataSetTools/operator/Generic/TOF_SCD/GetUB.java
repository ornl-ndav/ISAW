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

import java.lang.reflect.Array;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.*;

import Command.ScriptUtil;
import Command.Script_Class_List_Handler;
import IPNSSrc.blind;
import gov.anl.ipns.Util.SpecialStrings.*;

/**
 * @author Ruth
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
            boolean[] omit , float MaxXtallengthReal )
            throws IllegalArgumentException {

      float[] Res = new float[ 21 ];
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
            Peak pk = (Peak) Peaks.elementAt( i );
            double[] Qvec = pk.getUnrotQ();
            float p = (float) ( Qvec[ 0 ] * x + Qvec[ 1 ] * y + Qvec[ 2 ] * z );
            int index = (int) ( p / delta + .5 );
            if( Math.abs( index ) >= Res.length / 2 ) {
               float[] Res1 = new float[ 2 * Math.abs( index ) + 1 + 10 ];
               Arrays.fill(Res1,0f);
               System.arraycopy( Res , 0 , Res1 ,
                        ( Res1.length - Res.length ) / 2 , Res.length );
               Res = Res1;
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
           sb= dform3.format((double)Dirs[i][j]/L,sb, new FieldPosition( NumberFormat.FRACTION_FIELD));
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
      xixj = new float[ nspans - 2 ];
      float[] xs_end = new float[ maxIndex - minIndex + 1 ];
      Arrays.fill( xixj , 0f );
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
            xixj[ k - i - 2 ] += binnedData[ i ] * binnedData[ k ];
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
         xixj[ i ] += - mu * xs_end[ i + 2 ] + n * mu * mu - mu
                  * ( ( maxIndex - minIndex + 1 ) * mu - z );
         xixj[ i ] = xixj[ i ] / ( n * sigsq );

      }

      boolean done = false;
      int faze = 0;
      int maxIndx = - 1;
      for( int i = 0 ; ( i < xixj.length ) && ! done ; i++ ) {
         if( faze == 0 ) {
            if( xixj[ i ] < 0 )
               faze = 1;
         }
         else if( faze == 1 ) {
            if( xixj[ i ] > .1 )
               faze = 2;
            maxIndx = i;
         }
         else if( faze == 2 ) {
            if( xixj[ i ] < 0 )
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

      // -----Now determine goodness of fit ------------
      int N1 = 0 , 
          N2 = 0 ,
          N3 = 0;
      float perc80 = (float) ( binnedData[ maxDatIndx ] - .2 * ( binnedData[ maxDatIndx ] - minDat ) );
      float perc60 = (float) ( binnedData[ maxDatIndx ] - .4 * ( binnedData[ maxDatIndx ] - minDat ) );
      for( int i = maxDatIndx + maxIndx + 2 ; i < maxIndex ; i += maxIndx + 2 )
         if( binnedData[ i ] > perc80 )
            N1++ ;
         else if( binnedData[ i ] > perc60 )
            N2++ ;
         else
            N3++ ;
      for( int i = maxDatIndx ; i >= minIndex ; i -= maxIndx + 2 )
         if( binnedData[ i ] > perc80 )
            N1++ ;
         else if( binnedData[ i ] > perc60 )
            N2++ ;
         else
            N3++ ;
      float Fit = ( N1 + N2 / 2 ) / (float) ( N1 + N2 + N3 );
      N1 = 0;
      N2 = 0;
      N3 = 0;
      float perc20 = (float) ( binnedData[ maxDatIndx ] - .8 * ( binnedData[ maxDatIndx ] - minDat ) );
      float perc40 = (float) ( binnedData[ maxDatIndx ] - .6 * ( binnedData[ maxDatIndx ] - minDat ) );
      for( int i = maxDatIndx + maxIndx / 2 + 1 ; i < maxIndex ; i += maxIndx + 2 )
         if( binnedData[ i ] < perc20 )
            N1++ ;
         else if( binnedData[ i ] < perc40 )
            N2++ ;
         else
            N3++ ;
      for( int i = maxDatIndx - maxIndx / 2 - 1 ; i >= minIndex ; i -= maxIndx )
         if( binnedData[ i ] < perc20 )
            N1++ ;
         else if( binnedData[ i ] < perc40 )
            N2++ ;
         else
            N3++ ;

      Fit += ( N1 + N2 / 2 ) / (float) ( N1 + N2 + N3 );

      Res[ FIT ] = Fit / 2;
      int center = binnedData.length / 2;
      float Tot = 0;
      float TotInt = 0;
      for( int i = 0 ; i < binnedData.length ; i++ )
         if( binnedData[ i ] > 0 ) {
            Tot += binnedData[ i ];
            float x = ( i - center ) / (float) ( maxIndx + 2 );
            if( Math.abs( x - (int) ( x + .5 ) ) < .2 )
               TotInt += binnedData[ i ];
         }
      Res[ FIT1 ] = 2 * TotInt / (float) Tot;
      return Res;
   }


   private static float[] doOneDirection( Vector Peaks , float x , float y ,
            boolean[] omit , float MaxXtallengthReal ) {

      float line[] = null;
      float delta = MaxXtallengthReal;
      int span = 1 , Mx = - 1 , Mn = - 1;
      float[] Res = null;

      line = ProjectPeakToDir( x , y , Peaks , omit , delta );
      Mx = findMaxNonZero( line );
      Mn = findMinNonZero( line );
      span = Mx - Mn;

      Res = CalcStats( line , Mn , Mx );


      if( Res != null ) {
         Res[ LEN ] *=  1 / MaxXtallengthReal / 4f / 12f;;
         Res[ X ] = x;
         Res[ Y ] = y;
      }
      return Res;
   }


   private static float[] doOneDirection1( Vector Peaks , float x , float y ,
            boolean[] omit , float MaxXtallengthReal ) {

      float line[] = null;
      float delta = MaxXtallengthReal / 8;
      int span = 1 , Mx = - 1 , Mn = - 1;
      float[] Res = null;
      int Nbins = 1;
      while( ( span < 35 ) && ( delta > .0000001 ) && ( Nbins * delta > .01 ) ) {
         delta = delta * 2;
         while( ( span < 35 ) && ( delta > .000001 ) ) {
            delta = delta * 4;
            line = ProjectPeakToDir( x , y , Peaks , omit , delta );
            Mx = findMaxNonZero( line );
            Mn = findMinNonZero( line );
            span = Mx - Mn;
         }
         Res = CalcStats( line , Mn , Mx );
         if( Res != null )
            Nbins = (int) Res[ LEN ];
      }
      if( Res != null ) {
         Res[ LEN ] *= delta;
         Res[ X ] = x;
         Res[ Y ] = y;
      }
      return Res;
   }

   public static void getCandidateDirections( Vector Peaks , boolean[] omit ,
            float gridLength , float MaxXtalLengthReal ){
      List = new float[ 50 ][ 7 ];
      Nelements = 0;
      float[] Res = doOneDirection( Peaks , 0f , 0f , omit , MaxXtalLengthReal );
      if( Res != null ) {
         List = InsertInList( List , Nelements , Res );
         Nelements++ ;
      }
      Res = doOneDirection( Peaks , 1f , 0f , omit , MaxXtalLengthReal );
      if( Res != null ) {
         List = InsertInList( List , Nelements , Res );
         Nelements++ ;
      }
      Res = doOneDirection( Peaks , 0f , 1f , omit , MaxXtalLengthReal );
      if( Res != null ) {
         List = InsertInList( List , Nelements , Res );
         Nelements++ ;
      }

      for( float x = - 1 + gridLength ; x < 1 - gridLength ; x += gridLength )
         for( float y = - (float) Math.sqrt( 1 - x * x ) ; y <= (float) Math
                  .sqrt( 1 - x * x ) ; y += .1f ) {
            if( 1 - x * x - y * y >= 0 )
               Res = doOneDirection( Peaks , x , y , omit , MaxXtalLengthReal );
            else
               Res = null;
            if( Res != null ) {
               List = InsertInList( List , Nelements , Res );
               Nelements++ ;
            }
         }
     
   }
   
   public static float[][] getDirs( float gridLength,float NewDir, float[] code ){

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

      listEntry = FindNextTop( List , Nelements , x , y , - 10 , - 10 , NewDir ,
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

      listEntry = FindNextTop( List , Nelements , x , y , x1 , y1 , NewDir ,
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
      float[] Res = doOneDirection( Peaks , 0f , 0f , omit , MaxXtalLengthReal );
      if( Res != null ) {
         List = InsertInList( List , Nelements , Res );
         Nelements++ ;
      }
      Res = doOneDirection( Peaks , 1f , 0f , omit , MaxXtalLengthReal );
      if( Res != null ) {
         List = InsertInList( List , Nelements , Res );
         Nelements++ ;
      }
      Res = doOneDirection( Peaks , 0f , 1f , omit , MaxXtalLengthReal );
      if( Res != null ) {
         List = InsertInList( List , Nelements , Res );
         Nelements++ ;
      }

      for( float x = - 1 + gridLength ; x < 1 - gridLength ; x += gridLength )
         for( float y = - (float) Math.sqrt( 1 - x * x ) ; y <= (float) Math
                  .sqrt( 1 - x * x ) ; y += .1f ) {
            if( 1 - x * x - y * y >= 0 )
               Res = doOneDirection( Peaks , x , y , omit , MaxXtalLengthReal );
            else
               Res = null;
            if( Res != null ) {
               List = InsertInList( List , Nelements , Res );
               Nelements++ ;
            }
         }

      //show( List );
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

      listEntry = FindNextTop( List , Nelements , x , y , - 10 , - 10 , NewDir ,
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

      listEntry = FindNextTop( List , Nelements , x , y , x1 , y1 , NewDir ,
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


   /**
    * 
    * @param Peaks
    *           A Vector of Peaks
    * @param MaxXtalLengthReal
    *           The maximum length of crystal lattice in real space or -1 for
    *           default and adjustable
    * @param MinDxDyNewUnitDirection
    *           Minimum distance in x direction or y direction of the projection
    *           of a unit direction on he xy plane for 2 directions to be
    *           considered different
    * @return Returns a new UB matrix or null. This matrix has been run through
    *         blind.
    */
   public static float[][] GetUBMatrix( Vector Peaks , float MaxXtalLengthReal ,
            float MinDxDyNewUnitDirection ) throws IllegalArgumentException {

      boolean done = false;
      float MaxLength = MaxXtalLengthReal;
      float MinNewDir = MinDxDyNewUnitDirection;
      if( MaxLength < 0 )
         MaxLength = 20;
      if( MinNewDir < 0 )
         MinNewDir = .5f;
      float[][] Dirs = null;
      boolean[] omit = null;
      float gridLength = .05f;
      float DDir = .5f;
      int Nomitted = 0;
      boolean gridChanged = false;
      while( ! done ) {
         float[] code = new float[ 7 ];

         code[ 6 ] = 0;
         while( ( code[ 6 ] < 100 ) && ( gridLength > .00000001 ) ) {
            
            Dirs = getPlaneVectors( Peaks , omit , gridLength , DDir , code ,
                     MaxLength );
            if( debug){
               System.out.println("------------------ Direcions ------------");
               System.out.println("grid delta=" +gridLength+":: Max unit cell "+
                        MaxLength);
               GetUB.showDirs( Dirs );
               
            }
            System.out.println("-------------------------------------------");
            if( code[ 6 ] < 100 )
               gridLength = gridLength / 2f;
         }
         int NomittedOld = Nomitted;
         for( int i = 0 ; i < 3 ; i++ ) {
            if( ( code[ 2 * i ] > 0 ) && ( code[ 2 * i + 1 ] > 0 ) ) {
               float P = -(code[2*i+1]-.5f)/.75f + .5f;
               if( P < .25f )
                  P = .25f;
               if( P < .47f ) {
                  if( omit == null )
                     omit = new boolean[ Peaks.size() ];
                     boolean[] omit_copy= new boolean[ Peaks.size() ];
                     System.arraycopy( omit, 0,omit_copy,0,omit.length);
                     
                  int N = OmitPeaks( Peaks , Dirs[ i ] , omit_copy , P, false );
                  if( debug)
                     System.out.println("omitted "+N+" peaks for direction "+ i +
                              " at level "+ P);
                  omit= omit_copy;
                  Nomitted +=N;
                    
               }
            }
         }
         done = true;
         for( int i = 0 ; i < 6 ; i++ )
            if( code[ i ] < .75 )
               done = false;
         // add some dode to check if getting better

         if( ( NomittedOld == Nomitted ) && ! done ) {
            if( gridChanged )
               if( (MinDxDyNewUnitDirection < 0)
                        || (DDir / 1.5f > MinDxDyNewUnitDirection) ) {
                  DDir /= 1.5;
                  gridChanged = false;
               }
               else
                  done = true;
            else {
               gridLength /= 2f;
               gridChanged = true;
               if( gridLength < .00000001f )
                  done = true;
            }

         }
         else
            gridChanged = false;

         // Check if getting better

      }// while !done
      // try finer gridLength and finer distance between directions one time
      //System.out.println("N omitted="+Nomitted);
      if( Dirs == null )
         throw new IllegalArgumentException( " No directions found" );
      if( Dirs.length < 3 )
         throw new IllegalArgumentException( " Not enough directions found" );
      
      return UBMatrixFrPlanes( Dirs , Peaks , omit );

   }


   public static float[][] UBMatrixFrPlanes( float[][] PlaneDirs ,
            Vector Peaks , boolean[] omit ) {

      if( PlaneDirs == null )
         return null;
      if( PlaneDirs.length < 3 )
         return null;
      if( Peaks == null )
         return null;
      if( Peaks.size() < 4 )
         return null;


      float[] L = new float[ 3 ];
      float[][] unit = new float[ 3 ][ 3 ];
      for( int i = 0 ; i < 3 ; i++ ) {
         L[ i ] = (float) Math.sqrt( PlaneDirs[ i ][ 0 ] * PlaneDirs[ i ][ 0 ]
                  + PlaneDirs[ i ][ 1 ] * PlaneDirs[ i ][ 1 ]
                  + PlaneDirs[ i ][ 2 ] * PlaneDirs[ i ][ 2 ] );
         for( int j = 0 ; j < 3 ; j++ )
            unit[ i ][ j ] = PlaneDirs[ i ][ j ] / L[ i ];
      }
      int k = Peaks.size();

      double q[][] = new double[ k ][ 3 ];
      double hkl[][] = new double[ k ][ 3 ];
      k = 0;
      int ct;
      for( int i = 0 ; i < Peaks.size() ; i++ ) {
         double[] qvec = ( (Peak) Peaks.elementAt( i ) ).getUnrotQ();
         ct = 0;
         for( int j = 0 ; j < 3 ; j++ ) {
            double x = qvec[ 0 ] * unit[ j ][ 0 ] + qvec[ 1 ] * unit[ j ][ 1 ]
                     + qvec[ 2 ] * unit[ j ][ 2 ];
            x = x / L[ j ];
            if( ( omit == null ) || ( ! omit[ i ] ) ) {
               q[ k ] = qvec;
               hkl[ k ][ j ] = (int) ( x + .5 );
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
      float[][] UB = new float[ 3 ][ 3 ];
      for( int i = 0 ; i < 3 ; i++ )
         for( int j = 0 ; j < 3 ; j++ )
            UB[ i ][ j ] = (float) M[ i ][ j ];
      blind bl = new blind();
      ErrorString S = bl.blaue( UB );

      if( S == null ) {
         M = bl.UB;
         for( int i = 0 ; i < 3 ; i++ )
            for( int j = 0 ; j < 3 ; j++ )
               UB[ i ][ j ] = (float) M[ i ][ j ];

         return UB;
      }

      return null;
   }


   private static float[] FindNextTop( float[][] List , int Nelements ,
            float x1used , float y1used , float x2used , float y2used ,
            float NewDir , float gridLength ) {

      boolean again = false;
      for( int i = Nelements - 1 ; i >= 0 ; i-- ) {
         float[] listElement = List[ i ];
         if( ( listElement[ X ] < x1used - NewDir )
                  || ( listElement[ X ] > x1used + NewDir )
                  || ( listElement[ Y ] < y1used - NewDir )
                  || ( listElement[ Y ] > y1used + NewDir ) )
            if( ( Math.abs( listElement[ X ] - x1used ) <= gridLength )
                     && ( Math.abs( listElement[ Y ] - y1used ) <= gridLength ) )// on
                                                                                 // boundary
                                                                                 // try
                                                                                 // to
                                                                                 // increase
                                                                                 // NewDir
               return FindNextTop( List , Nelements , x1used , y1used , x2used ,
                        y2used , NewDir + 2 * gridLength , gridLength );
            else if( ( listElement[ X ] < x2used - NewDir )
                     || ( listElement[ X ] > x2used + NewDir )
                     || ( listElement[ Y ] < y2used - NewDir )
                     || ( listElement[ Y ] > y2used + NewDir ) )
               if( ( Math.abs( listElement[ X ] - x2used ) <= gridLength )
                        && ( Math.abs( listElement[ Y ] - y2used ) <= gridLength ) )// on
                                                                                    // boundary
                                                                                    // try
                                                                                    // to
                                                                                    // increase
                                                                                    // NewDir
                  return FindNextTop( List , Nelements , x1used , y1used ,
                           x2used , y2used , NewDir + 2 * gridLength ,
                           gridLength );
               else {
                  return listElement;

               }

      }
      return null;
   }


   private static float[][] InsertInList( float[][] List , int Nelements ,
            float[] Res ) {

      if( List.length < Nelements + 1 ) {
         float[][] List1 = new float[ Nelements + 12 ][ 7 ];
         System.arraycopy( List , 0 , List1 , 0 , Nelements );
         List = List1;
      }
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


      return List;
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
            double[] qvec = ( (Peak) Peaks.elementAt( i ) ).getUnrotQ();
            double x = qvec[ 0 ] * qNormal[ 0 ] + qvec[ 1 ] * qNormal[ 1 ]
                     + qvec[ 2 ] * qNormal[ 2 ];
            x = x / Lsq;
            if( Math.abs( x - (int) ( x + .5 ) ) > level ) {
               if( !count) omit[ i ] = true;
               c++ ;
               Res++ ;
            }

         }
     // System.out.println( "omitted " + c + " peaks of" + Peaks.size() );
      return Res;
   }


   /**
    * @param args
    */
   public static void main( String[] args ) {

      Vector Peaks = (Vector) ( new DataSetTools.operator.Generic.TOF_SCD.ReadPeaks(
               args[ 0 ] ) ).getResult();
      float x = 0;
      float y , z;
      x = 0.037499327f;
      y = -0.89929664f;
      float near = .5f;
      float grid = .1f;
      float range = .3f;
      float Xtal = 20f;
      float[] line = null;
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
            line = GetUB.ProjectPeakToDir( x , y , Peaks , omit , 20f );
            if(line != null)
               for( int i=0;i< line.length; i++)
                  if( i%25 ==0)
                    System.out.println( dform.format((double)line[i]));
                  else 
                     System.out.print( dform.format((double)line[i]));
            else
               System.out.println("null");
         }
         else if( L.startsWith( "s" ) ) {
            xixj = null;
            float[] Res = GetUB.CalcStats( line , GetUB.findMinNonZero( line ) ,
                     GetUB.findMaxNonZero( line ) );
            if( Res == null )
               System.out.println( "null" );
            else {
               for( int i = 0 ; i < Res.length ; i++ )
                  if( i%25==0)
                     System.out.println( dform.format((double)Res[ i ] ) );
                  else
                     System.out.print( dform.format((double)Res[ i ] ) );
                     
               System.out.println( "" );
            }

            System.out.print( "Correlations =" );
            if( xixj == null )
               System.out.print( "null" );
            else
               for( int i = 0 ; i < xixj.length ; i++ )
                  if( i%25==0)
                      System.out.println( dform.format((double)xixj[ i ]));
                  else
                     System.out.print( dform.format((double)xixj[ i ]));
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
               GetUB.OmitPeaks( Peaks , Dirs[ 0 ] , omit , range,false );
            }

         }
         else if( L.startsWith( "o2" ) ) {
            if( range < 0 )
               omit = null;
            else {
               if( omit == null )
                  omit = new boolean[ Peaks.size() ];
               GetUB.OmitPeaks( Peaks , Dirs[ 1 ] , omit , range,false );
            }

         }
         else if( L.startsWith( "o3" ) ) {
            if( range < 0 )
               omit = null;
            else {
               if( omit == null )
                  omit = new boolean[ Peaks.size() ];
               GetUB.OmitPeaks( Peaks , Dirs[ 2 ] , omit , range, false );
            }
         }
         else if( L.startsWith( "w" ) ) {
            UB = GetUB.GetUBMatrix( Peaks , 20f , .3f );
            ScriptUtil.display( UB );
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


            if( Dirs.length == 3 ) {
               UB = GetUB.UBMatrixFrPlanes( Dirs , Peaks , omit );
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
                     Xtal ) );

         }
      }
   }
}
