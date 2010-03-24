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
import DataSetTools.components.ui.Peaks.subs;
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
   
   public static float      DMIN    = Float.NaN;
   
   public static boolean   ELIM_EQ_CRYSTAL_PARAMS  = false;
   
  // private static float[] line = new float[61];

   /**
    * 
    */
   public GetUB() {

      super();
      List = null;
      Nelements =0;
      xixj = null;
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
    *           
    * @param MaxXtallengthReal
    *           The maximum length in real space of the side of the crystal
    *           
    * @return A line where each bin width on the line corresponds to length 
    *         delta=1/(48*MaxXtallengthReal).  The center bin is the first 
    *         element of the returned result.
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
            float[] Qvec = pk.getUnrotQ();
            float p =  ( Qvec[ 0 ] * x + Qvec[ 1 ] * y + Qvec[ 2 ] * z );
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


   private static void show( float[][] Listt ) {

      System.out.println( "------------------------------" );
      if( Listt == null )
         return;
      for( int i = 0 ; i < Listt.length ; i++ ) {
         System.out.print( i + ":" );
         for( int j = 0 ; j < Listt[ i ].length ; j++ )
            System.out.print( Listt[ i ][ j ] + "  " );
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
    * @return an array Res where
    *             Res[FIT1]=2*fraction of intensity within 20% of a plane
    *             Res[CORR] =the correlation between points that are
    *             Res[LEN] steps apart on the binnedData line. 
    *               The values
    *                at CORR and LEN in Res are the best possible.
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
      float maxr=-1, minr=1;
      for( int i = 0 ; i < nspans - 2 ; i++ ) {
         int n = ( maxIndex - minIndex + 1 ) - i - 2;
         float z = 0;
         if( maxIndex - minIndex - i - 1 < xs_end.length )
            z = xs_end[ maxIndex - minIndex - i - 1 ];
         xi_xj[ i ] += - mu * xs_end[ i + 2 ] + n * mu * mu - mu
                  * ( ( maxIndex - minIndex + 1 ) * mu - z );
         xi_xj[ i ] = xi_xj[ i ] / ( n * sigsq );
         if(xi_xj[i] >maxr)maxr=xi_xj[i];
         if( xi_xj[i] < minr)minr=xi_xj[i];
      }
      xixj = xi_xj;
      boolean done = false;
      int faze=-1,
          maxIndx=-1;
      for( int k = 0 ; k < 3 && ! done ; k++ ) {
         faze = 0;
         maxIndx = - 1;
         float middle = ( maxr + minr ) / 2;
         float zero = ( maxr - minr ) / 30;
         for( int i = 0 ; ( i < xixj.length ) && ! done ; i++ ) {
            if( faze == 0 ) {

              
               if( xixj[ i ] < middle - zero )
                  faze = 1;


            }
            else if( faze == 1 ) {
               if( xixj[ i ] > middle + zero )
                  faze = 2;
               maxIndx = i;
            }
            else if( faze == 2 ) {
               if( xixj[ i ] < middle - zero )
                  faze = 3;
               else if( xixj[ i ] > xixj[ maxIndx ] ) {

                  maxIndx = i;
               }
            }
            else
               done = true;
         }
         if( done && maxIndx < 8 && k<2){//tried for 10 between
            done = false;
            maxr=-1; minr=1;
            for( int ii=0; ii< xixj.length-1;ii++){
               xixj[ii]=(xixj[ii]+xixj[ii+1])/2;
               if( xixj[ii]>maxr)
                  maxr=xixj[ii];
               if( xixj[ii] < minr)
                  minr =xixj[ii];
            }
            
         }
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
    *  @param MaxXtalLengthReal  
    *           The maximum length of the side of a unit cell in real spacel
    *            
    * @return  A Vector of possible orientation matrices. max 1st 50
    */
   public static Vector<float[][]> getAllOrientationMatrices( Vector Peaks , boolean[] omit ,
            float gridLength ,float MaxXtalLengthReal ) {

      List = new float[ 50 ][ 7 ];
      Nelements = 0;
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

     Thread[] thrds = new Thread[4];
     for( int i=0; i<4;i++)
        thrds[i] = new DoQuadrantDirections( Peaks, omit,  gridLength,
             MaxXtalLengthReal, i);
     Execute1( thrds);
     
     Vector<float[][]> MRes = new Vector<float[][]>();
     if( Nelements < 1)
        return MRes;
     float FitMax = List[Nelements-1][FIT1]+List[Nelements-1][CORR];
     FitMax = .65f*FitMax;
     System.out.println("Through initial find directions,nelets, FitMax= "+Nelements+
              ","+FitMax);
     int n=Nelements-1;
     for( int i=Nelements-1; i>0 && (List[i][FIT1]+List[i][CORR])> FitMax; i--)
        n=i;     
     
     for( int i=Nelements-1; i>= n; i--)
        optimize( List[i],Peaks,omit,MaxXtalLengthReal);
     
     EliminateDuplicates( List,n,gridLength,MaxXtalLengthReal);
     
     n = Math.max(  n , Nelements-1-70 );
     
     //n = Nelements - 10;
     float[] weights = new float[7];
     Arrays.fill( weights , 0f );
     weights[LEN] = -1;
     weights[FIT1] = -.3f;
     weights[CORR] = -.3f;
     
     ListComparator comp = new ListComparator( List, n, Nelements-1,weights );
     comp.sort();
     
     Integer[] sortList = comp.getRangList();
      
       MRes=GetUBs(  sortList,null,Peaks);
       
       MRes = ReSort( MRes, Peaks, MaxXtalLengthReal, ELIM_EQ_CRYSTAL_PARAMS);
      
       System.out.println( "Through finding UB's");
       return MRes;
   }
   
   /**
    * Eliminates directions that are essentially the same( dist < gridLength/3) than a
    * better one
    * @param List      The list of directions,etc
    * @param start     start index to consider
    * @param end       # elements in List
    * @param gridLength The dist(proj to x-y plane) between adjacent tested unit
    *                    directions
    */
   private static void EliminateDuplicates( float[][] List, int start, 
                float gridLength, float MaxXtalLength)
   {
      if( List == null || start <0 || gridLength < 0)
         return;
      int end= Nelements;
      //if( end > List.length)end = List.length;
      if( start >= end)
         return;
      int top =start;
      for( int i=start; i < end-1; i++)
      {
         float[] L = List[i];
         boolean same = false;
         for( int j=i+1; j< end && !same ; j++)
         {
            float[] L2 = List[j];
            if( Math.abs( L2[X]-L[X] )< gridLength/3)
               if( Math.abs( L2[Y]-L[Y] )< gridLength/3)
                  if( Math.abs( L2[LEN]-L[LEN] )< .5/MaxXtalLength )
                     same = true;
                  
         }
         if( !same)
        
            List[top++]= List[i];
        
      }
      System.out.println(" duplicates elim prev/next"+ Nelements+"/"+top);
      List[top++] = List[ Nelements-1];
      Nelements = top; 
   }
   private static Vector<float[][]> GetUBs( Integer[] sortList,int[] elts, Vector Peaks)
   {
      Vector<float[][]> Res = new Vector<float[][]>();
      int[] tuple = new int[3];
      
      tuple[0]=0; tuple[1]=1; tuple[2] =2;
      int N=2;
      boolean done = tuple[2] >=sortList.length;
      
      while(!done)
      {
         int i1 = sortList[tuple[0]];
         int i2 = sortList[tuple[1]];
         int i3 = sortList[tuple[2]];
         float[][]UB = List2UBinv( Peaks,i1, i2, i3);
        
        
         
         if(UB != null && isDifferentFrom( Res, UB) )
         {
            Res.add(UB);
            if( elts != null && elts.length ==3)
            {
               elts[0]=i1;
               elts[1]= i2;
               elts[2] =i3;
               return Res;
            }
         }
         //next tuple
         if( tuple[0]+1 < tuple[1] )
            tuple[0]++;
         else if( tuple[1]+1 < tuple[2])
         {   tuple[1]++;
             tuple[0]=0;
         }else
         {tuple[0]=0; tuple[1]=1;
          N++;
           tuple[2]=N;
         }
         done = tuple[2] >=sortList.length || Res.size()>200;
        
      }
      return Res;
      
   }
   
   //returns true if can add to Res otherwise false
  private static boolean isDifferentFrom( Vector<float[][]> Res, float[][]UB)
  {
     if( UB == null || UB.length !=3)
        return false;
     
     if( !DspaceMinOK( UB, DMIN))
        return false;
     
     if( Res == null || Res.size() < 1)
        return true;
     
     float mx = Math.abs( UB[0][0]);
     for( int j=0; j< 3; j++)
     { 
        for( int i=0; i<3;i++)
        {  
           
           if( Math.abs(UB[i][j]) > mx)
              mx= Math.abs( UB[i][j] );
        }
       
     }
     
     if( mx ==0)
        return false;
    
     for( int k=0; k< Res.size(); k++)
     {
       float[][]UB1 = Res.elementAt( k );
       boolean res = false;
        for( int i=0; i < 3; i++)
           for( int j=0; j<3; j++)
           {
              if( Math.abs( UB1[i][j]-UB[i][j] )/mx>.001 )           
                 res = true;
           }
        if( !res)
           return false;
     }
     return true; 
     
  }
  
  private static boolean DspaceMinOK( float[][]UB, float Dmin)
  {
     float[][] UBinv = LinearAlgebra.getInverse( UB );
     if( UB == null || UBinv == null)
        return false;
     float dsq = Dmin*Dmin;
     for( int i=0; i<3;i++)
        if( UBinv[i][0]*UBinv[i][0] +
                 UBinv[i][1]*UBinv[i][1] +
                 UBinv[i][2]*UBinv[i][2]<dsq )
           return false;
     return true;
     
  }
  
  private static  Vector<float[][]> ReSort( Vector<float[][]> UBvec, Vector Peaks, 
           float MaxXtalLength,  boolean ElimEqXtalParams)
  {
     if( UBvec == null || Peaks==null || MaxXtalLength <=0)
        return null;
     if( Peaks.size() < 2)
        return UBvec;
     
    float[][][] MRes2= new float[UBvec.size()][4][];
     for( int i=0; i< UBvec.size(); i++)
     {
        
        float[][] elt = UBvec.elementAt( i );
        float[] sortVec = new float[7];
        sortVec[0]= 1- GetUB.IndexStat( elt, Peaks, .2f, null);
        sortVec[0] = (int)(100*sortVec[0]+.5f);
        sortVec[0] = 5*(int)(sortVec[0]/5);
        double[] Xtals = Util.abc( LinearAlgebra.float2double( 
                                                  elt));
        //sort this 
        SortXtals( Xtals);
        for( int j=1; j< 7; j++)
           sortVec[j]= (float) Xtals[j-1];
        
        MRes2[i][0]= sortVec;
        
        MRes2[i][1] = elt[0]; 
        MRes2[i][2] = elt[1]; 
        MRes2[i][3] = elt[2];
        
     }
     UBvec.clear();
     java.util.Arrays.sort( MRes2 , new MultiArrayComparator());
     Vector<float[][]> Res = new Vector<float[][]>();
     float[] prev = null;
     for( int i=0; i < MRes2.length; i++)
     {
        boolean use = prev == null || !ElimEqXtalParams;
        float[] ordering = (MRes2[i])[0];
        
        if( prev == null || !ElimEqXtalParams)
           prev = ordering;
        else
        for( int j=0; j < 7 && !use ; j++)
           if( Math.abs( ordering[j]-prev[j]) >.1f)
           {
              use = true;
              prev = ordering;
           }
          
              
        if( use && (Float.isNaN( DMIN )|| MRes2[i][0][1] >= DMIN) && 
                 MRes2[i][0][3] <= MaxXtalLength )
        {
           float[][] subArray = new float[3][];
           subArray[0]= MRes2[i][1];
           subArray[1]= MRes2[i][2];
           subArray[2]= MRes2[i][3];
          
           Res.addElement( subArray  );
        }
     }
     return Res;
  }
  
  private static void SortXtals( double[] Xtals)
  {
    
     for( int j=0; j<2;j++)
     for( int i=0; i<2;i++)
     {
        if( Xtals[i] > Xtals[i+1])
        {
           double sav = Xtals[i];
           Xtals[i]= Xtals[i+1];
           Xtals[i+1] = sav;
           sav= Xtals[3+i];
           Xtals[3+i]= Xtals[4+i];
           Xtals[4+i]= sav;
           
           
        }
     }
  }
   private static float[][] List2UBinv( Vector Peaks,int i1, int i2, int i3)
   {
      if( i1 <0 || i2<0|| i3 <0)
         return null;
      
      if( i1>= List.length || i2 >= List.length || i3 >= List.length)
         return null;
      
      float[][] Dirs = new float[3][3];
      
      Dirs[0] = PlaneNormal( i1);
      Dirs[1] = PlaneNormal( i2);
      Dirs[2] = PlaneNormal( i3);
      
      float[][] Res = UBMatrixFrPlanes( Dirs, Peaks,null,null,4);
      
      if( Res == null)
         return null;
      
      float Max = Float.MIN_VALUE;
      float Min = Float.MAX_VALUE;
      for( int i=0; i<3;i++)
         for( int j=0; j<3; j++)
         {
            if( Res[i][j] >Max)
               Max = Res[i][j];
            if( Res[i][j] < Min)
               Min = Res[i][j];
            
         }
            
      Max = Math.max(  Math.abs(Max) , Math.abs(Min) );
      if( Math.abs(LinearAlgebra.determinant( LinearAlgebra.float2double( Res ) ))
               < Max*Max*Max/1000)
         return null;
      //Nigglify
     // blind B = new blind();
     // B.blaue( Res  );
      
      return Res;
      
   }
  private static float[] PlaneNormal( int i1){

      float[] coeff = new float[3];
      coeff[0]= List[i1][X];
      coeff[1]= List[i1][Y];
      float len = List[i1][LEN];
      
      coeff[2]=(float) Math.sqrt(1- coeff[0]*coeff[0]-coeff[1]*coeff[1]);
      if( Float.isNaN( coeff[2] ))
         coeff[2]=0;
      coeff[0] *= len;
      coeff[1] *= len;
      coeff[2] *= len; 
      return coeff;
   }
   private static boolean optimize( float[] ListElt, Vector Peaks,boolean[] omit,
            float MaxXtalLengthReal)
   {
     
      int k=0;
      float[] coeff = new float[3];
      coeff[0]= ListElt[X];
      coeff[1]= ListElt[Y];
      float len = ListElt[LEN];
      
      coeff[2]=(float) Math.sqrt(1- coeff[0]*coeff[0]-coeff[1]*coeff[1]);
      if( Float.isNaN(  coeff[2] ))
         coeff[2]=0;
      coeff[0]/=len;
      coeff[1]/=len;
      coeff[2]/=len;
      float[][] QQ = new float[3][3];
      float[]IQ = new float[3];
      java.util.Arrays.fill(QQ[0],0f);
      java.util.Arrays.fill(QQ[1],0f);
      java.util.Arrays.fill(QQ[2],0f);
      java.util.Arrays.fill(IQ,0f);
      
      for( int i=0;i< Peaks.size(); i++)
         if( omit == null || omit.length <= i || !omit[i])
      {
         float[] Qs =((IPeak)Peaks.elementAt( i )).getUnrotQ();
         float indx = Qs[0]*coeff[0]+Qs[1]*coeff[1]+Qs[2]*coeff[2];
         int IND = (int)Math.floor(  indx +.1 );
         if( Math.abs( IND-indx)<.1)
           for( int j=0; j<3;j++) 
           {
              IQ[j] +=Qs[j]*IND;
              QQ[0][j]+= Qs[0]*Qs[j];
              QQ[1][j] += Qs[1]*Qs[j];
              QQ[2][j] += Qs[2]*Qs[j];
           }
         else k++;
      }else
         k++;
      if( k == Peaks.size())
         return false;
     float[][] inv = LinearAlgebra.getInverse( QQ );
     if( inv == null)
        return false;
     float[] Res = LinearAlgebra.mult( inv ,IQ );
     if( Res == null)
        return false;
     len = (float)Math.sqrt( Res[0]*Res[0]+Res[1]*Res[1]+Res[2]*Res[2]);
     if( len <=0)
        return false;
     if( Res[2] < 0)
        len = -len;
     float[] Listt = doOneDirection( Peaks ,Res[0]/len ,Res[1]/len ,
              omit , MaxXtalLengthReal, new float[100] );
     if( Listt == null)
        return false;
     
     if( Listt[FIT1]+ Listt[CORR] <= ListElt[FIT1]+ ListElt[CORR])
        return false;
    
     System.arraycopy( Listt,0,ListElt , 0 , Math.min(ListElt.length, Listt.length) );
     return true;
      
   }
   
   private boolean showError(String message)
   {
      System.out.println( message );
      return  false;
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
 /* int k=0;
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
      
 */   

     Thread[] thrds = new Thread[4];
       for( int i=0; i<4;i++)
          thrds[i] = new DoQuadrantDirections( Peaks, omit,  gridLength,
               MaxXtalLengthReal, i);
       Execute1( thrds);
  //----------- added code to sort by length major-----
     int n = 0 ;
       float[] weights = new float[7];
       Arrays.fill( weights , 0f );
       weights[LEN]= -1;
       weights[FIT1]=-.5f;
       weights[CORR]=-.5f;
       
       ListComparator comp = new ListComparator( List, n, Nelements-1,weights );
       comp.sort();
       Integer[] sortList = comp.getRangList();
       float[][] FList = new float[sortList.length][];
       System.out.println("NElts, sortList, List ="+ sortList.length+","+Nelements);
       for( int i=0; i< sortList.length; i++)
          System.out.print( sortList[i]+",");
       int[] poss = new int[3];
       Vector<float[][]> OO = GetUBs( sortList, poss, Peaks);
       if( OO == null || OO.size() < 1)
          return null;
       else
       {  
          Arrays.fill( code , - 1f );
          code[6] = Nelements;
          
          float[][] Res1= new float[3][3];
          for( int ii=0;ii<3; ii++ )
          {

             float[] listEntry = List[ poss[ii] ];
             code[ 0+2*ii ] = listEntry[ CORR ];
             code[ 1+2*ii ] = listEntry[ FIT1 ] / 2f;
             float x = listEntry[ X ] ;
             float y = listEntry[ Y ] ;
             float scale = listEntry[ LEN ];
            
             Res1[ii][ 0 ] = x * scale;
             Res1[ii][ 1 ] = y * scale;
             Res1[ii][ 2 ] = (float) Math.sqrt( 1 - x *x - y * y ) * scale;
          }
          
          
       }
          
          
      // List = FList;
 
   //----------end added code to sort the list------
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
      
      if( thrds == null)
         return;
      int k = Runtime.getRuntime().availableProcessors();
      if( k <=0)
         k=1;
      if( k > thrds.length)
         k= thrds.length;
      
      for( int i=0; i < k; i++)
         thrds[i].start();
      
      int nDone = 0;
      while( nDone < thrds.length )
      for( int i=0; i< k ; i++)
         if( thrds[i] != null)
         try{
           thrds[i].join(10000);
           if( thrds[i].getState().equals( Thread.State.TERMINATED )){
            
              thrds[i] = null;
              nDone ++;
              if( k < thrds.length )
                 thrds[k].start();
              if( k < thrds.length)
                 k++;
              
           }
         }catch( Throwable s){
            System.out.println("Thread "+i+" interrupted \n");
            s.printStackTrace();
            thrds[i] = null;
            nDone++;
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
            //System.out.println("-------------------------------------------");
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
      float[][] Res = UBMatrixFrPlanes( Dirs , Peaks , omit, Stats,0 );
      if( Res == null)
         throw new IllegalArgumentException(" Not enough directions to get UB matrix");
      return Res;

   }

   /**
    *  Calculates the UB matrix from a list of normals to planes whose
    *  length is the length between consecutive planes
   
    * 
    * @param PlaneDirs array of 3 plane normals whose lengths are the
    *                  length between consecutive planes
    * @param Peaks    The Vector of peaks
    * @param omit     The peaks to omit from consideration
    * @param Stats    Output of stats. Stats[0] is fraction within 10% of a 
    *             plane Stats[1] is the fraction within 20% of plane, Stats[3]
    *              within 30%, etc.
    * @param stop   if 1, gets UB corresponding to dirs only
    *               if 2, gets UB after optimization
    *               otherwise the UB matrix is optimized and goes through
    *               blind.
    * @return  The UB matrix for the Peaks
    */
   public static float[][] UBMatrixFrPlanes( float[][] PlaneDirs ,
            Vector Peaks , boolean[] omit, float[]Stats , int stop) {

      if( PlaneDirs == null )
         return null;
      
      if( PlaneDirs.length < 3 )
         return null;
      
      if( Peaks == null && stop !=1 )
         return null;
      
      if( Peaks != null && Peaks.size() < 4 )
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
      
      if( Singular(UB))
         return null;
      
      int k = Peaks.size();
      double q[][] = new double[ k ][ 3 ];
      double hkl[][] = new double[ k ][ 3 ];
      k = 0;
      int ct;
     
      for( int i = 0 ; i < Peaks.size() ; i++ ) {
         float[] qvec = ( (IPeak) Peaks.elementAt( i ) ).getUnrotQ();
         ct = 0;
         for( int j = 0 ; j < 3 ; j++ ) {
            double x = qvec[ 0 ] * unit[ j ][ 0 ] + qvec[ 1 ] * unit[ j ][ 1 ]
                     + qvec[ 2 ] * unit[ j ][ 2 ];
            x = x / L[ j ];
            if( ( omit == null ) || ( ! omit[ i ] ) ) {
               for ( int component = 0; component < 3; component++ )
                 q[ k ][component] = qvec[component];
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
      for( int j=0; j < k; j++)
      {
          System.arraycopy( q[j] , 0 , q1[j] , 0 , 3 );
          System.arraycopy( hkl[j] , 0 , hkl1[j] , 0 , 3 );
      }
      
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
    * Determines if the matrix is singular or nearly singular.
    * @param UB  The 3x3 matrix to test
    * @return   true if |determinant| < .1/M^3 where M is the max
    *         of the absolute values of the entries in the matrix UB
    */
   public static boolean Singular( float[][] UB)
   {
      if( UB == null)
         return true;
      if( UB.length != 3)
         return true;
      float mx = Math.abs( UB[0][0] );
      for( int i=0; i < 3; i++)
         for( int j=0; j<3;j++)
            if(Math.abs( UB[i][j] )> mx)
               mx= Math.abs( UB[i][j] );
      if( mx ==0)
         return true;
      if( LinearAlgebra.determinant( LinearAlgebra.float2double( UB ) )
                <= .1*(mx*mx*mx))
         return true;
      return false;
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
         float[] qvec = ( (IPeak) Peaks.elementAt( i ) ).getUnrotQ();
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
         float[] Qs = P.getUnrotQ();
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
   
   
   static float MinKeyVal = Float.NaN;
   private static synchronized void InsertInList( float[] Res ){
   
      
      if( List.length < Nelements + 1 ) {
         float[][] List1 = new float[ Nelements + 12 ][ 7 ];
         System.arraycopy( List , 0 , List1 , 0 , Nelements );
         List = List1;
      }
      
      boolean done = false;
      float key = Res[ FIT1 ] + Res[ CORR ];
      if( Nelements <=0)
         MinKeyVal = .5f*key;
      else if(  List[Nelements-1][FIT1]+List[Nelements-1][CORR] < key)
         MinKeyVal = .5f*key;
      
      if( key < MinKeyVal)
        return;
      
      for( int i = Nelements - 1 ; ( i >= 0 ) && ! done ; i-- ) {
         if( List[ i ][ FIT1 ] + List[ i ][ CORR ] > key )
            List[ i + 1 ] = List[ i ];
         else {
            List[ i + 1 ] = Res;
            done = true;
         }
      }
      if( ! done )
      {
         List[ 0 ] = Res;
      }
      Nelements++;

     
   }

   /**
    * Fills up the omit array with false for all the peaks that do not fall
    * within the specified level of closeness to a plane in a family of planes
    * defined by the normal to the plane whose length is the distance between
    * planes. If count is true, the omit array is not changed but the number 
    * that would have been omitted is returned.
    * 
    * @param Peaks  The vector of Peaks objects
    * @param qNormal  The plane normal whose length is the distance between
    *                 the parallel planes. It is assumed the first in the set
    *                 of planes starts at 0.
    *                 
    * @param omit    the array of booleans that will be set( if count is false)
    *                false if the peaks is not close to one of the parallel 
    *                planes
    *                
    * @param level   The measure at how close a peak must be "indexed" to a 
    *                plane before it will not be omitted
    *                
    * @param count   If true, the omit array will not be changed. This only
    *                returns the number of Peaks that will not be indexed to
    *                the specified level. A negative number indicates an error
    *                
    * @return  The number of peaks that are not indexed within the specified
    *          level.
    */
   public static int OmitPeaks( Vector Peaks , float[] qNormal ,
            boolean[] omit , float level, boolean count ) {

      int Res = -1;
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
      Res = 0;
      float Lsq = qNormal[ 0 ] * qNormal[ 0 ] + qNormal[ 1 ] * qNormal[ 1 ]
               + qNormal[ 2 ] * qNormal[ 2 ];
      for( int i = 0 ; i < Peaks.size() ; i++ )
         if( ! omit[ i ] ) {
            float[] qvec = ( (IPeak) Peaks.elementAt( i ) ).getUnrotQ();
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
            else if( Peaks != null){
               if( omit == null )
                  omit = new boolean[ Peaks.size() ];
               if( Dirs != null && Dirs.length >=1)
               GetUB.OmitPeaks( Peaks , Dirs[ 0 ] , omit , range,false );
            }else
               omit = null;

         }
         else if( L.startsWith( "o2" ) ) {
            if( range < 0 )
               omit = null;
            else if( Peaks != null){
               if( omit == null )
                  omit = new boolean[ Peaks.size() ];
               if( Dirs != null && Dirs.length >= 2)
                  GetUB.OmitPeaks( Peaks , Dirs[ 1 ] , omit , range,false );
            }else 
               omit = null;

         }
         else if( L.startsWith( "o3" ) ) {
            if( range < 0 )
               omit = null;
            else if( Peaks != null ){
               if( omit == null )
                  omit = new boolean[ Peaks.size() ];
               if( Dirs != null && Dirs.length >= 3)
                  GetUB.OmitPeaks( Peaks , Dirs[ 2 ] , omit , range, false );
            }else
               omit= null;
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
class MultiArrayComparator implements Comparator
{

   /* (non-Javadoc)
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   @Override
   public int compare( Object arg0 , Object arg1 )
   {

      if( arg0 == null ||!(arg0 instanceof float[][]))
         if( arg1 == null || !(arg1 instanceof float[][]))
            return 0;
         else
            return 1;
      
      if( arg1 == null || !(arg1 instanceof float[][]))
         return -1;
      float[] arg11 = ((float[][])arg0)[0];
      float[] arg21 = ((float[][])arg1)[0];
      if( arg11 == null || arg11.length < 7)
         if( arg21 == null || arg21.length < 7)
            return 0;
         else
            return 1;

      if( arg21 == null || arg21.length < 7)
         return -1;
      int x ;
      if(arg11[0]==0f && Math.abs( arg11[1]-7.2958)<.01 &&
               Math.abs( arg11[2]-7.3156)<.01 && Math.abs( arg11[3]-9.8411)<.01)
         x= 1;
      if(arg21[0]==0f && Math.abs( arg21[1]-7.2958)<.01 &&
               Math.abs( arg21[2]-7.3156)<.01 && Math.abs( arg21[3]-9.8411)<.01)
         x= 1;
     
      for( int i=0; i<7;i++)
      { 
         float xx = arg11[i]-arg21[i];
         if( xx <.01 && xx > -.01)
         {
            
         }else  if(xx <.01f)
             
            return -1;
         else if( xx > -.01f)
            return 1;
      }
      
      return 0;
         
    
   }
   
}
class ListComparator implements Comparator<Integer>
{
   float[][] list;
   float[] weights;
   int start,
       end;
   Integer[] sortInfo;
   public ListComparator( float[][] list,int start, int end,float[] weights)
   {
      this.list = list;
      this.weights = weights;
      this.start = Math.min( start, list.length);
      this.end  = Math.max(0,end);
      if(start > end)
         sortInfo = null;
      else
      {
         sortInfo = new Integer[ end - start +1];
         for( int i =0; i< sortInfo.length; i++ )
            sortInfo[i]= start +i;
      }
   }

   /* (non-Javadoc)
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   @Override
   public int compare( Integer o1 , Integer o2 )
   {
      if( list == null || start > end)
         return 0;
      
      if( o1 == null)
         if( o2 == null)
            return 0;
         else
            return -1;
      if( o2 == null)
         return 1;
      int i1 =(o1).intValue();
      int i2 =(o2).intValue();
      if( i1 < start || i1 >end)
         return -1;
      if( i2 < start || i2 >end)
         return 1;
      float v1 = 0;
      float w;
      for( int i=0; i< list[i1].length; i++)
      {
         w=1;
         if( weights != null && weights.length >i)
            w= weights[i];
         v1 += w*list[i1][i];
      }
      float v2 = 0;
      
      for( int i=0; i< list[i2].length; i++)
      {
         w=1;
         if( weights != null && weights.length >i)
            w= weights[i];
         v2 += w*list[i2][i];
      }
      
      if( v1 < v2)
         return -1;
      if( v1 > v2)
         return 1;
      
      return 0;
   }
   
   public void sort()
   {
      Arrays.sort(  sortInfo, this );
   }
   
   public Integer[] getRangList()
   {
      return sortInfo;
   }
   
  
   
}

