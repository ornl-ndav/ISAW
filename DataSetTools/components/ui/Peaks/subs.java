/* 
 * File: subs.java
 *
 * Copyright (C) 2009, Ruth Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Rev$
 */

package DataSetTools.components.ui.Peaks;

import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;

import DataSetTools.operator.Generic.TOF_SCD.*;
import Operators.TOF_SCD.IndexingUtils;
import IPNSSrc.blind;
import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Geometry.*;
//import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.File.*;


/**
 * static methods to carry out primitive-like operations
 * 
 * @author Ruth
 * 
 */
public class subs
{



   /**
    * Returns a String for displaying orientation matrix information
    * 
    * @param Peaks
    *           The list of peaks( or null)
    * @param orientationMatrix
    *           The orientation matrix
    * @param omittedPeakIndex
    *           Boolean mask for Peaks vector. True means omit
    * @param errs
    *           The errors in the crystal lattice parameters
    * @param html
    *           If true, an html string will be returned starting with <html><body>.
    *           Text uses \n
    * @return  A string with information on the orientation matrix
    */
   public static String ShowOrientationInfo( Vector< IPeak > Peaks ,
                                             float[][] orientationMatrix ,
                                             boolean[] omittedPeakIndex ,
                                             float[] errs ,
                                             boolean html )
   {

      String Res , 
             eoln  , 
             end;
      
      String textType ="html";
      
      if( !html)
         textType ="plain";
      TextSeparators u = new TextSeparators(textType);
      // For lining up, need to use tables. Spaces are collapsed in html
      
      Res = u.start();
      eoln =u.eol();
      end = u.end();
     
      
      if( orientationMatrix == null )
         return "";
      Res +=u.table();
    
      for( int col = 0 ; col < 3 ; col++ )
      {  Res +=u.row();
         for( int row = 0 ; row < 3 ; row++ )
         {
            Res += String.format( "%10.6f" , orientationMatrix[ row ][ col ] );
            if( row < 2)
               Res += u.col();
         }
         Res += u.rowEnd();
      }
      Res += u.tableEnd();
      
      double[] XtalParams = DataSetTools.operator.Generic.TOF_SCD.Util
               .abc( LinearAlgebra.float2double( orientationMatrix ) );
      if( XtalParams == null)
      {
         Res += u.end();
         return Res;
      }
      Res +=u.table();
      Res +=u.row();
      for( int i = 0 ; i < Math.max( XtalParams.length , 6 ) ; i++ )
      {
         
         Res += String.format( "%10.4f" , XtalParams[ i ] );
         if( i < Math.max( XtalParams.length , 6 )-1)
            Res +=u.col();
         
      }
      Res +=u.rowEnd();
     
      Res += u.row();
      for( int i = 0 ; i < Math.max( XtalParams.length , 6 ) ; i++ )
         if( errs != null && errs.length > i )
         {   
            Res += String.format( "%10.4f" , errs[ i ] );
            if( i <  Math.max( XtalParams.length , 6 )-1)
               Res +=u.col();
         }else
         {   
            Res += String.format( "%10.4f" , 0f );
            if( i <  Math.max( XtalParams.length , 6 )-1)
               Res +=u.col();
            
         }
      Res +=u.rowEnd();
      Res += u.tableEnd();
      
      if( Peaks == null || Peaks.size() < 1 )
         return Res + end;
      
      Res += eoln ;
      
      float[] Perc = GetPeakFitInfo( Peaks , orientationMatrix ,
               omittedPeakIndex );
      
      float[] AllPerc = GetPeakFitInfo( Peaks , orientationMatrix , null );
      Res += u.indent();
      Res += "Percent Peaks where h,k, and l are within .1,.2,.. of an integer";
      Res += u.indentEnd();
      Res += u.table()+u.row();
      Res += "closeness "+u.col();
      if( omittedPeakIndex != null)
         Res +=" Used  "+u.col();
      Res +="   All"+u.rowEnd();
      Res += u.row();
      Res += "to int "+u.col()+"   Peaks "+u.col();
      if( omittedPeakIndex != null)
         Res +="   Peaks";
      Res += u.rowEnd();
      
      Res += u.row()+"---------"+u.col()+"-----";
      if( omittedPeakIndex != null)
         Res +=u.col()+"------";
       Res +=u.rowEnd();
      
      if( AllPerc !=null &&  Perc != null && AllPerc.length >=4 &&
               Perc.length >=4)
      {  
         for( int i = 0 ; i < 4 ; i++ )
         {
            Res +=u.row();
           // Res += " "
            //      + String.format( "%2.1f%14.3f%11.3f" , .1 + i * .1 ,
            //               Perc[ i ] * 100 , AllPerc[ i ] * 100 ) + u.col();

            Res += " "
                  + String.format( "%2.1f" , .1 + i * .1  ) +  u.col();
            if( omittedPeakIndex != null)
            Res += 
                String.format( "%14.3f" , Perc[ i ] * 100  ) +  u.col();

           
               Res += String.format( "%11.3f" , AllPerc[ i ] * 100 );
            
            Res += u.rowEnd();
         }
      }
      Res += u.tableEnd();
         Res += u.end();
    

      return Res;
   }


   /**
    * Returns the fraction of peaks whose h,k, and l values lie within .1, .2,.3
    * and .4 of an integer.
    * 
    * @param Peaks
    *           The Peaks
    * @param orientationMatrix
    *           The orientation matrix
    * @param omittedPeakIndex
    *           The indices in Peaks of the omitted peaks
    * @return the fraction of peaks whose h,k, and l values lie within .1, .2,.3
    *          and .4 of an integer
    */
   public static float[] GetPeakFitInfo( Vector< IPeak > Peaks ,
            float[][] orientationMatrix , boolean[] omittedPeakIndex )
   {

      if( Peaks == null || orientationMatrix == null
               || orientationMatrix.length != 3 )
         return null;

      int[] Nbad = new int[ 4 ];
      int NPeaks = 0;
      java.util.Arrays.fill( Nbad , 0 );
      
      float[][] InvOrient = LinearAlgebra.getInverse( orientationMatrix );


      for( int i = 0 ; i < Peaks.size() ; i++ )
         if( omittedPeakIndex == null || omittedPeakIndex.length <= i
                  || ! omittedPeakIndex[ i ] )
         {
            NPeaks++ ;
            float[] Q = Peaks.elementAt( i ).getUnrotQ();
            if( Q != null && Q.length == 3 )
            {
               boolean bad = false;
               int kk = - 1;
               
               for( int row = 0 ; row < 3 && ! bad ; row++ )
               {
                  float S = 0;
                  for( int col = 0 ; col < 3 ; col++ )
                     S += InvOrient[ row ][ col ] * Q[ col ];

                  double err = Math.abs( S - Math.floor( S ) );
                  err = Math.min( err , 1-err );
                  if( err > .4 )
                     
                     kk = Math.max( kk , 3 );
                  
                  else if( err > .3 )
                     
                     kk = Math.max( kk , 2 );
                  
                  else if( err > .2 )
                     
                     kk = Math.max( kk , 1 );
                  
                  else if( err > .1 )
                     
                     kk = Math.max( kk , 0 );
                  

               }
               if( kk >= 0 )
                  Nbad[ kk ]++ ;
               
            }
         }
      
      if( NPeaks <= 0 )
         return null;

      float[] Res = new float[ 4 ];

      for( int i = 2 ; i >= 0 ; i-- )
         Nbad[ i ] += Nbad[ i + 1 ];


      for( int i = 0 ; i < 4 ; i++ )
         Res[ i ] = (float) ( NPeaks - Nbad[ i ] ) / NPeaks;
      
      return Res;
   }


   /**
    * Returns Information on the coordinate system, and other conventions for
    * the orientation matrix
    * 
    * @param html
    *           if true, text is html encoded
    * @return  A string explaining the Coordinate system being used
    */
   public static String getCoordinateInformation( boolean html )
   {

      String Res , 
            eoln ;
      
      
      String textType ="html";
      if( !html)
         textType ="plain";
      TextSeparators u = new TextSeparators(textType);
    
      
         Res = u.start();
         eoln = u.eol();
     


      Res += "The above orientation matrix in the file is the TRANSPOSE of the UB Matrix "
               + eoln;
      
      Res += "that maps the column vector (h,k,l ) to the column vector (q'x,q'y,q'z)."
               + eoln;
      
      Res += "|Q'|=1/dspacing and its coordinates are \"currently\" relative to IPNS's"
               + eoln;
      
      Res += "right-hand coordinate system where x(1st coord) is the beam direction and "
               + eoln;
      
      Res += " z(3rd coord) is vertically upward" + eoln;
      
      return Res;
   }


   /**
    * Returns the distance a miller index is from an integer
    * 
    * @param Peaks
    *           The vector of peaks
    * @param omittedMask
    *           The omitted peak mask
    * @param orientationMatrix
    *           The orientation matrix
    * @param xvals
    *           an increasing range of offsets from an int. It should be have
    *           equal lengthed intervals.
    * @param weighted
    *           If weighted, the peak intensity is used, otherwise only the
    *           number of peaks is considered
    * @param indexChar
    *           Either h,k,or l to specify which miller index is used
    * @return  the distances a miller index is from an integer
    */
   public static float[] getMillerOffsets( Vector< IPeak > Peaks ,
            boolean[] omittedMask , float[][] orientationMatrix ,
            float[] xvals , boolean weighted , char indexChar )
   {

      if( Peaks == null || orientationMatrix == null || xvals == null
               || xvals.length < 2 )
         return null;

      float[][] InvMat = LinearAlgebra.getInverse( orientationMatrix );
      if( InvMat == null )
         return null;

      float minX = xvals[ 0 ];
      float maxX = xvals[ xvals.length - 1 ];
      float delta = ( maxX - minX ) / ( xvals.length - 1 );

      if( delta <= 0 )
         return null;

      int indx = 0;
      if( indexChar == 'k' )
         
         indx = 1;
      
      else if( indexChar == 'l' )
         
         indx = 2;

      float[] Res = new float[ xvals.length - 1 ];
      java.util.Arrays.fill( Res , 0f );
      int NPeaks = 0;
      //ignore h=0,k=0,k=0
      for( int i = 0 ; i < Peaks.size() ; i++ )
         if( omittedMask == null || omittedMask.length <= i || ! omittedMask[ i ] )
         {
            NPeaks++ ;
            IPeak P = Peaks.elementAt( i );
            float[] Qs = P.getUnrotQ();
            
            float miller = InvMat[ indx ][ 0 ] * Qs[ 0 ] + InvMat[ indx ][ 1 ]
                     * Qs[ 1 ] + InvMat[ indx ][ 2 ] * Qs[ 2 ];
            
            float offset = miller - (int) Math.floor( miller +.5 );
            
            int Index = (int) ( ( offset - minX ) / delta );
            
            if( Index >= 0 && Index < Res.length )
               if( weighted )
                  
                  Res[ Index ] += P.ipkobs();
            
               else
                  
                  Res[ Index ]++ ;
         }
      
      if( NPeaks > 0 )
         for( int i = 0 ; i < Res.length ; i++ )
            
            Res[ i ] = Res[ i ] / NPeaks;
      
      return Res;

   }

  /**
   * Calculated the max absolute value for h,k, and l values.
   * 
   * @param UB  the UB matrix
   * @param Q   the Qx,Qy, and Qz values(or max Q value)
   * 
   * @return  the Max value that h,k, and l could  be  to match the given q value
   */
   public static int MaxHKLVal( float[][] UB , float  Q)
   {
      
      float Mx = Float.NEGATIVE_INFINITY;
      for( int i = 0 ; i < 3 ; i++ )
      {
         float M = Math.abs( UB[ i ][ 0 ] );
         
         for( int j = 1 ; j < 3 ; j++ )
            if( Math.abs( UB[ i ][ j ] ) > M )
               
               M = Math.abs( UB[ i ][ j ] );
         
         if( Q / M > Mx )
            
            Mx = Q / M;
         
      }
      
      return (int) ( Mx + .01 );
   }


   /**
    * Finds the list of possible hkl tuples that will yield q values with same
    * length as q
    * 
    * @param B
    *           The orientation matrix
    * @param q
    *           A Q from a peak whose length is to be matched by B*hkl q value
    * @param Qtol
    *           Closeness of the two Q values
    * @param Qtol2
    *           Not used
    * @param Center
    *           Centering character P,A,B,C,F,I, or R
    * @return the list of possible hkl tuples that will yield q values with same
    *           length as q
    */
   public static int[][] FindPossibleHKLs( float[][] B , float[] q ,
            float Qtol , float Qtol2 , char Center )
   {

      Vector< int[] > choices = new Vector< int[] >();
      
      if( B == null || q == null || q.length < 3 || Qtol < 0 )
         return null;

      float Qsq = q[ 0 ] * q[ 0 ] + q[ 1 ] * q[ 1 ] + q[ 2 ] * q[ 2 ];
      
      if( Qsq <= 0 )
         return null;

      float Q = (float) Math.sqrt( Qsq );
      float Q1sq;
      int M = MaxHKLVal( B , Q );
      
      for( int h = - M ; h <= M ; h++ )
         for( int k = - M ; k <= M ; k++ )
            for( int l = - M ; l <= M ; l++ )
               if( CenteringOK( h , k , l , Center ) )
               {
                  Q1sq = 0;
                  for( int r = 0 ; r < 3 ; r++ )
                  {
                     float qq = B[ r ][ 0 ] * h + B[ r ][ 1 ] * k + B[ r ][ 2 ]
                              * l;
                     Q1sq += qq * qq;
                  }

                  if( Math.abs( Qsq - Q1sq ) / 2 / Q < Qtol )
                  {
                     choices.add( new int[] {h , k , l  } );

                  }
               }
      
      return choices.toArray( new int[ 0 ][ 0 ] );
   }


   /**
    * Converts an 1D int array to a float array
    * 
    * @param intArray
    *           The int array
    * @return the corresponding float array
    */
   public static float[] cvrt2float( int[] intArray )
   {

      if( intArray == null )
         return null;

      float[] Res = new float[ intArray.length ];

      for( int i = 0 ; i < intArray.length ; i++ )
         Res[ i ] = intArray[ i ];

      return Res;
   }


   /**
    * Finds the list of possible hkl tuples that will yield q values with same
    * length as q2 and whose dot products agree to within Qtol2
    * 
    * @param B
    * @param q1
    *           The first Q vector of a Peak. B*OthHKL has length = q1 length
    * @param q2
    *           The second Q vector of a Peak
    * @param OthHKL
    *           The hkl corresponding to peak q1
    * @param Qtol
    *           Closeness between lengths of q2 and B*new HKL
    * @param Qtol2
    *           Closeness beween q1 dot q2 and Corresp Q values from hkl vals
    * @param Center
    *           Centering type character P,A,B,C,F,I, or R
    * @return a list of possible hkl values for q2 that jibe with q1
    */
   public static int[][] FindPossibleHKLs( float[][] B , float[] q1 ,
            float[] q2 , int[] OthHKL , float Qtol , float Qtol2 , char Center )
   {

      Vector< int[] > choices = new Vector< int[] >();

      if( B == null || q1 == null || q1.length < 3 || Qtol < 0 )
         return null;

      float Qsq = q2[ 0 ] * q2[ 0 ] + q2[ 1 ] * q2[ 1 ] + q2[ 2 ] * q2[ 2 ];

      if( Qsq <= 0 )
         return null;

      float Q = (float) Math.sqrt( Qsq );
      float[] Q3 = null;

      if( OthHKL != null && OthHKL.length >= 3 )
      {
         Q3 = new float[ 3 ];
         java.util.Arrays.fill( Q3 , 0f );

         for( int row = 0 ; row < 3 ; row++ )
            for( int j = 0 ; j < 3 ; j++ )

               Q3[ row ] += B[ row ][ j ] * OthHKL[ j ];

         if( Q3[ 0 ] == 0 && Q3[ 1 ] == 0 && Q3[ 2 ] == 0 )
            Q3 = null;

      }
      float dot1 = q1[ 0 ] * q2[ 0 ] + q1[ 1 ] * q2[ 1 ] + q1[ 2 ] * q2[ 2 ];
      float Q1sq;

      int M = MaxHKLVal( B , Q );
      for( int h = - M ; h < M ; h++ )
         for( int k = - M ; k < M ; k++ )
            for( int l = - M ; l < M ; l++ )
               if( OthHKL == null || OthHKL.length < 3 || h != OthHKL[ 0 ]
                        || k != OthHKL[ 1 ] || l != OthHKL[ 2 ] )

                  if( CenteringOK( h , k , l , Center ) )
                  {
                     Q1sq = 0;
                     float dot = 0;
                     for( int r = 0 ; r < 3 ; r++ )
                     {
                        float QQ = B[ r ][ 0 ] * h + B[ r ][ 1 ] * k
                                 + B[ r ][ 2 ] * l;

                        if( Q3 != null )

                           dot += Q3[ r ] * QQ;

                        Q1sq += QQ * QQ;
                     }

                     if( Math.abs( Qsq - Q1sq ) / 2 / Q < Qtol
                              && Math.abs( dot - dot1 ) < Qtol2 )
                     {
                        choices.add( new int[]{ h , k , l} );

                     }
                  }

      return choices.toArray( new int[ 0 ][ 0 ] );
   }


   /**
    * Returns true if this hkl is allowed for the given centering P,A,B,C,F,I,
    * or R
    * 
    * @param H
    *           h, miller index,value
    * @param K
    *           k value
    * @param L
    *           l value
    * @param Center
    *           the character P,A,B,C,F,I, or R
    * @return true if the hkl value satisfies the centering constraints
    *         otherwise false is returned
    */
   public static boolean CenteringOK( int H , int K , int L , char Center )
   {

      int ICELL = "PABCFIR".indexOf( Center );
      
      if( ICELL <= 0 )
         return true;
      
      if( ICELL == 1 )
      {
         int KL = Math.abs( K + L );
         if( KL % 2 != 0 )
            return false;
         
         return true;
      }


      if( ICELL == 2 )
      {
         int HL = Math.abs( H + L );
         if( HL % 2 != 0 )
            return false;
         
         return true;
      }

      if( ICELL == 3 )
      {
         int HK = Math.abs( H + K );
         if( HK % 2 != 0 )
            return false;
         
         return true;
      }

      if( ICELL == 4 )
      {
         int HK = Math.abs( H + K );
         int HL = Math.abs( H + L );
         int KL = Math.abs( K + L );
         if( HK % 2 != 0 | HL % 2 != 0 | KL % 2 != 0 )
            return false;
         
         return true;
      }

      if( ICELL == 5 )
      {
         int HKL = Math.abs( H + K + L );
         if( HKL % 2 != 0 )
            return false;
         
         return true;
      }

      if( ICELL == 6 )
      {

         int HKL = Math.abs( - H + K + L );
         if( HKL % 3 != 0 )
            return false;
         
         return true;
      }
      
      return true;
   }


   /**
    * Calculates the orientation matrix with the given information
    * 
    * 
    * @param q1
    *           First q vector
    * @param hkl1
    *           Corresponding hkl value
    * @param q2
    *           First q vector
    * @param hkl2
    *           Corresponding hkl value
    * @param BMat
    *           The unrotated orientation matrix.
    * @return the orientation matrix with the given parameters
    */
   public static float[][] getOrientationMatrix( float[] q1 , float[] hkl1 ,
            float[] q2 , float[] hkl2 , float[][] BMat )
   {

      if( q1 == null || q2 == null || hkl1 == null || hkl2 == null
               || BMat == null )
         return null;
      
      if( hkl1.length < 3 || hkl2.length < 3 || q1.length < 3 || q2.length < 3
               || BMat.length < 3 )
         return null;
      
      float[] Q1 = new float[ 3 ];
      float[] Q2 = new float[ 3 ];
      java.util.Arrays.fill( Q1 , 0f );
      java.util.Arrays.fill( Q2 , 0f );

      for( int i = 0 ; i < 3 ; i++ )
      {
         Q1[ i ] += BMat[ i ][ 0 ] * hkl1[ 0 ] + BMat[ i ][ 1 ] * hkl1[ 1 ]
                  + BMat[ i ][ 2 ] * hkl1[ 2 ];
         Q2[ i ] += BMat[ i ][ 0 ] * hkl2[ 0 ] + BMat[ i ][ 1 ] * hkl2[ 1 ]
                  + BMat[ i ][ 2 ] * hkl2[ 2 ];
      }
      
      Vector3D Q_n = new Vector3D( Q1 );
      Vector3D q_n = new Vector3D( q1 );
      
      Vector3D Q0 = new Vector3D( Q1 );
      Vector3D q0 = new Vector3D( q1 );
      
      Q_n.cross( new Vector3D( Q2 ) );
      q_n.cross( new Vector3D( q2 ) );
      
    // Q_n.normalize();
    // q_n.normalize();
      
      Vector3D Qy = new Vector3D( Q1 );
      Vector3D qy = new Vector3D( q1 );
      
      Qy.cross( Q_n );
      qy.cross( q_n );

      //Q0.normalize( );
     // Qy.normalize( );
     // Q_n.normalize( );
      float[][] M2I = new float[ 3 ][ 3 ];
      M2I[ 0 ] = Q0.get();
      M2I[ 1 ] = Qy.get();
      M2I[ 2 ] = Q_n.get();
      
      Tran3D M_to_I = new Tran3D( M2I );
      M_to_I.transpose();
      if( ! M_to_I.invert() )
         return null;
      
     //q0.normalize( );
     //qy.normalize( );
     //q_n.normalize( );
      float[][] m2I = new float[ 3 ][ 3 ];
      m2I[ 0 ] = q0.get();
      m2I[ 1 ] = qy.get();
      m2I[ 2 ] = q_n.get();
      Tran3D m_to_I = new Tran3D( m2I );
      m_to_I.transpose();

      m_to_I.multiply_by( M_to_I );
      m_to_I.multiply_by( new Tran3D( BMat ) );
      
      float[][] Res = new float[ 3 ][ 3 ];
      float[][] M = m_to_I.get();
      
      for( int i = 0 ; i < 3 ; i++ )
         System.arraycopy( M[ i ] , 0 , Res[ i ] , 0 , 3 );

      return Res;

   }


   /**
    * Calculates the orientation matrix with the given information
    * 
    * @param q1
    *           First Q vector
    * @param hkl1
    *           Corresponding hkl values
    * @param q2
    *           First Q vector
    * @param hkl2
    *           Corresponding hkl values
    * @param q3
    *           First Q vector
    * @param hkl3
    *           Corresponding hkl values
    * @return orientation matrix satisfying the given information
    */
   public static float[][] CalcUB( float[] q1 , float[] hkl1 , float[] q2 ,
            float[] hkl2 , float[] q3 , float[] hkl3 )
   {

      if( q1 == null || q2 == null || q3 == null )
         return null;
      
      if( hkl1 == null || hkl2 == null || hkl3 == null )
         return null;
      
      if( q1.length < 3 || q2.length < 3 || q3.length < 3 )
         return null;
      
      if( hkl1.length < 3 || hkl2.length < 3 || hkl3.length < 3 )
         return null;
      
      float[][] Q =
      {
               {
                        q1[ 0 ] , q1[ 1 ] , q1[ 2 ]
               } ,
               {
                        q2[ 0 ] , q2[ 1 ] , q2[ 2 ]
               } ,
               {
                        q3[ 0 ] , q3[ 1 ] , q3[ 2 ]
               }
      };
      float[][] HKL =
      {
               {
                        hkl1[ 0 ] , hkl1[ 1 ] , hkl1[ 2 ]
               } ,
               {
                        hkl2[ 0 ] , hkl2[ 1 ] , hkl2[ 2 ]
               } ,
               {
                        hkl3[ 0 ] , hkl3[ 1 ] , hkl3[ 2 ]
               }
      };
      float[][] invHKL = LinearAlgebra.getInverse( HKL );
      
      if( invHKL == null )
         return null;
      
      float[][] QQ = LinearAlgebra.mult( invHKL , Q );
      
      QQ = LinearAlgebra.getTranspose( QQ );
      

      return QQ;
    
   }


   /**
    * Converts a button, checkbox etc into the corresponding menu item with all
    * the correct listeners(Action only), tooltip text, and labels
    * 
    * @param button
    *           The JButton, JCheckBox,JRadioButton or JMenuItem to be converted
    * @return The corresponding JMenuItem with the same text,tool tips, and
    *         action listeners as the original button
    */
   public JMenuItem getJMenuItem( AbstractButton button )
   {

      String Label = button.getText();
      String ToolTip = button.getToolTipText();
      ActionListener[] list = button.getActionListeners();
      JMenuItem jmen;
      
      if( button instanceof JButton )
         
         jmen = new JMenuItem( Label );
      
      else if( button instanceof JCheckBox )
         
         jmen = new JCheckBoxMenuItem( Label );
      
      else if( button instanceof JRadioButton )
         
         jmen = new JRadioButtonMenuItem( Label );
      
      else if( button instanceof JMenuItem )
         
         return (JMenuItem) button;
      
      else
         
         return null;
      
      if( ToolTip != null)
          jmen.setToolTipText( ToolTip );
      
      if( list != null )
         for( int i = 0 ; i < list.length ; i++ )
            jmen.addActionListener( list[ i ] );
      
      return jmen;


   }

   //NiggReal not need inverse of interchange two rows is = itself and
   // its transpose
  private static boolean Sortt( float[][] Tensor, float[][] UB, boolean NiggReal)
  {
  
     boolean changed = false;
     for(int k=0; k<2; k++)
     for( int i=0; i<2; i++)
       
        if( Tensor[i][i] >=  Tensor[i+1][i+1] )
        {
           int sgn =1;
           if( Tensor[i][i+1]< 0)
              sgn = -1;
           boolean ok = false;
           if( Tensor[i][i] == Tensor[i+1][i+1] && 
                 Math.abs( Tensor[(i+2)%3][i+1]) <= Math.abs( Tensor[(i+2)%3][i]) )
                 ok = true;
           if( !ok)//Xchg row i and row i+1
           { 
              changed = true;
              for( int col = 0; col < 3; col++)//Interchange row i, i+1
              {
                 float sav = Tensor[i][col];
                 Tensor[i][col] = Tensor[i+1][col];
                 Tensor[i+1][col] = sav;
              }
              for( int row =0; row < 3; row++)// Interchange col i and i+1
              {
                 float sav = Tensor[row][i];
                 Tensor[row][i] = Tensor[row][i+1];
                 Tensor[row][i+1] = sav;
                 sav = UB[row][i];
                 UB[row][i] = UB[row][i+1];
                 UB[row][i+1] = sav;
              }
              
              
           }
        }
     return changed;
  }
  private static void showNig( float[][] Tensor, float[][]UB, boolean NiggReal)
  {
     System.out.println("Tensor");
     LinearAlgebra.print( Tensor );
     System.out.println("UB");
     LinearAlgebra.print( UB);
     System.out.println(  "Compare to tensor");
     float[][] UB1 = UB;
     if(  NiggReal)
        UB1 = LinearAlgebra.getInverse(LinearAlgebra.getTranspose(  UB) );
     LinearAlgebra.print(  LinearAlgebra.mult( LinearAlgebra.getTranspose(UB1), UB1) );
     System.out.println( "-------------------------------------");
     
  }
  

  /**
   
   * Returns a new matrix whose columns are the basis with minimum length
   * and form a RIGHT HANDED SYSTEM
   * 
   * @param UB  A UB matrix, whose columns form a basis in 3D
   * 
   * @return  a new matrix whose columns are the basis with minimum length
   * and form a RIGHT HANDED SYSTEM
   */
   public static float[][] Nigglify( float[][]UB )
   {
     Tran3D UB_tran   = new Tran3D( UB );
     Tran3D Niggli_UB = new Tran3D( UB );

     try
     {
       if ( !IndexingUtils.MakeNiggliUB( UB_tran, Niggli_UB ) )
         Niggli_UB.set( UB_tran );
     }
     catch ( Exception ex )
     {
       System.out.println("Warning: Find Niggli reduced cell failed, " +
                          "UB matrix not changed");
     }
     float[][] Niggli_UB_array = Niggli_UB.get();
     float[][] result = new float[3][3];
     for ( int row = 0; row < 3; row++ )
       for ( int col = 0; col < 3; col++ )
         result[row][col] = Niggli_UB_array[row][col];

     return result;
   }


  /**
   * **$$$$$$$$$$$$$$$$$$$$$$ Messed up somehow $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
   * Obsolete method to return a new matrix whose columns are the basis with 
   * minimum length and form a RIGHT HANDED SYSTEM
   * 
   * @param UB  A UB matrix, whose columns form a basis in 3D
   * 
   * @return  a new matrix whose columns are the basis with minimum length
   * and form a RIGHT HANDED SYSTEM
   */
   public static float[][] OLD_Nigglify( float[][] UB )
   {
       boolean NiggReal = true;
      
      if( UB == null)
         return null;
      float[][] UB1 = LinearAlgebra.copy( UB );
      float[][] Tensor = LinearAlgebra.mult( LinearAlgebra.getTranspose( UB1 ) , UB1 );
      int invSgn =1;
      if( NiggReal)
      {
         Tensor = LinearAlgebra.getInverse(  Tensor  );
         invSgn=-1;
      }
      if( Tensor== null)
         return null;
      float[][] ident = new float[][]{{1f,0f,0f},{0f,1f,0f},{0f,0f,1f}};
      boolean done = false;
      while( !done)
      { 
         done = true;
         boolean changed = Sortt( Tensor, UB1, NiggReal);
        
         for( int i=0; i<2 && !changed; i++)
            for(  int j=i+1; j< 3 && !changed; j++)
            { 
               int sgn = 1;
               if( Tensor[i][j] < 0)
                  sgn = -1;
               int oth = i+1;
               
               if( oth ==j)
                  oth = (oth+1)%3;
               if( oth ==0)
                  oth =1;
               
               if(  Tensor[i][i]/2 < sgn*Tensor[i][j] ||
                        (Tensor[i][i]/2 == sgn*Tensor[i][j] &&  sgn>0 && //must be pos.
                           Math.abs(Tensor[oth][i]) > 2*Math.abs(Tensor[oth][j]) //
                           ))
               {
                  changed = true;
                  ident[j][i]= -sgn;
                  Tensor = LinearAlgebra.mult( ident , Tensor );
                  ident[j][i]=-sgn*invSgn;
                  UB1 = LinearAlgebra.mult( UB1 , ident  );
                  ident[j][i]=0;
                  ident[i][j]= -sgn;
                  Tensor = LinearAlgebra.mult( Tensor, ident);
                  
                  ident[i][j]=0;
               } else if( Tensor[i][i]/2 == -Tensor[i][j] && Tensor[0][oth] !=0 && sgn<0)
               {
                  //replace c by c+b(for exampe) --> positive case
                  int oth1= oth;
                  if( oth > j)
                     oth1 = i;
                  ident[j][oth1] =1;
                  Tensor = LinearAlgebra.mult( ident , Tensor );
                  ident[j][oth] = invSgn;
                  UB1 = LinearAlgebra.mult( UB1 , ident  );
                  ident[j][oth]= 0;
                  ident[oth][j] = 1;
                  Tensor = LinearAlgebra.mult( Tensor, ident);
                  ident[oth][j] = 0;
                  changed = true;
               }
            }
         
         if( !isRightHanded( UB1 ))
         {
            ident[2][2] = -1;
            Tensor = LinearAlgebra.mult( ident , Tensor );
            Tensor = LinearAlgebra.mult( Tensor, ident );
            UB1 = LinearAlgebra.mult( UB1 , ident );
            ident[2][2] = 1;
            changed = true;
         }
         done = !changed;
         if( !changed )// Currently Assumes no two are equal.
         {
            /*int c=0;
            if( Tensor[0][1]<= 0)c++;
            if( Tensor[0][2]<= 0)c++;
            if( Tensor[1][2] <= 0)c++;
            if( c >0 && c <3)
            {
               int sgn =1;
               if( c == 2)
                  sgn = -1;
               int kk=-1;
               if( sgn* Tensor[0][1] <= 0)kk = 2;
               if( sgn*Tensor[0][2] <= 0) kk = 1;
               if( sgn*Tensor[1][2] <= 0)kk = 0;
               // off sign. change common of the two with same signs
               ident[(kk+1)%3][(kk+1)%3] = -1;
               ident[(kk+2)%3][(kk+2)%3] = -1;
               UB1 =LinearAlgebra.mult( UB1,ident);
               Tensor = LinearAlgebra.mult( ident , Tensor );
               Tensor=  LinearAlgebra.mult( Tensor, ident );
               ident[(kk+1)%3][(kk+1)%3] = 1;
               ident[(kk+2)%3][(kk+2)%3] = 1;
               changed = true;
               //showNig( Tensor, UB1, NiggReal);
              */
            changed = AllPosNeg( Tensor, UB1, ident);
               
            
            // now check for C -a-b
            
            float sgn = 1;
            if( Tensor[0][1] < 0) sgn =-1; // All should have same sign
            float x = sgn*Tensor[0][1] +sgn*Tensor[0][2] +sgn*Tensor[1][2] ;
            if( x > .5*(Tensor[0][0]+ Tensor[1][1]) && sgn < 0 )
            {
                boolean xchanged = true;
               
                if( x == .5*(Tensor[0][0]+ Tensor[1][1]))
                 if( Tensor[0][0] <=2*(sgn*Tensor[0][2] +sgn*Tensor[0][1] ) 
                                                       || sgn > 0 ) {
                    xchanged = false;
                 }
                
                if( xchanged)
                {
                   ident[2][1] = ident[2][0] = -sgn;
                   Tensor = LinearAlgebra.mult(  ident , Tensor );
                   ident[2][1] = ident[2][0] = -sgn*invSgn;
                   UB1 = LinearAlgebra.mult( UB1 , ident );
                   
                   ident[2][1] = ident[2][0] = 0;
                   ident[1][2] = ident[0][2] = -sgn;
                   Tensor = LinearAlgebra.mult( Tensor , ident );
                   ident[1][2] = ident[0][2] = 0;  // inverse
                   changed = true;
                }
                  
           
            }else if (  x == .5*(Tensor[0][0]+ Tensor[1][1]) && sgn < 0 &&
                  Tensor[0][0]>2*sgn*Tensor[0][2]+ sgn*Tensor[0][1])
            {
               //may be circular if get into this case NOPE just barely
               ident[2][0]=ident[2][1] = 1;
               Tensor = LinearAlgebra.mult(  ident , Tensor );
               ident[2][0]=ident[2][1] = invSgn;
               UB1 = LinearAlgebra.mult( UB1 , ident );
               ident[2][0]=ident[2][1] = 0;
               ident[0][2] = ident[1][2] = 1;
               Tensor = LinearAlgebra.mult( Tensor , ident );
               ident[0][2] = ident[1][2] = 0;
               changed = true;
            }
            
            //!changed
         }
         done =done && !changed;
         boolean x =!Sortt(Tensor, UB1, NiggReal);
         done = done && x;
         x=!AllPosNeg( Tensor, UB1,ident);
         done = done && x;
      
            
      }//while not done
      if( Sortt( Tensor, UB1, NiggReal))
         showNig( Tensor, UB1, NiggReal);
     
      
      return UB1;
   }

private static boolean  AllPosNeg(float[][] Tensor,float[][]UB1, float[][] ident)
{
   int c=0;
   int z=0;
   if( Tensor[0][1]< 0)c++;
   if( Tensor[0][2]< 0)c++;
   if( Tensor[1][2] < 0)c++;
   if( Tensor[0][1]== 0)z++;
   if( Tensor[0][2]== 0)z++;
   if( Tensor[1][2] == 0)z++;
   c +=z;
   
   if( c >0 && c <3)
   {
      int sgn =1;
      if( c == 2)
         sgn = -1;//more negatives
      
         
      int kk=-1; //The one that is Different
      if( sgn* Tensor[0][1] < 0)kk = 2; 
      if( sgn*Tensor[0][2] < 0) kk = 1;
      if( sgn*Tensor[1][2] < 0)kk = 0;
      if( z >= 2)// zeroes cannot change sign
         kk = (kk+1)%3;
      else if( z==1 )
        if( Tensor[0][1] ==0 )if(c==1) kk=2; else if( c==2) if( kk==1)kk=0;else kk=1;
        else if( Tensor[0][2] ==0 )if(c==1) kk=1; else if( c==2) if( kk==2) kk=0;else kk= 2;
        else if( Tensor[1][2] ==0 )if(c==1) kk=0; else if( c==2)if(kk==2) kk=1;else kk=2;
           
      
      if( kk < 0 )
         if( c==1)//1 zero and 2 positives
         {

            if( sgn* Tensor[0][1] <= 0)kk = 2; 
            if( sgn*Tensor[0][2] <= 0) kk = 1;
            if( sgn*Tensor[1][2] <= 0)kk = 0;
         }
         else if( c==2)//2 zeroes and 1 positive. Declare one zero as positive
            {
              if( Tensor[0][1]==0 ) kk=2;
              if( Tensor[0][2]==0 ) if( kk < 0)kk=1;              
              if( Tensor[1][2]==0) if( kk < 0) kk=0;
       
            }
            
            
      // off sign. change common of the two with same signs
      ident[(kk+1)%3][(kk+1)%3] = -1;
      ident[(kk+2)%3][(kk+2)%3] = -1;
      UB1 =LinearAlgebra.mult( UB1,ident);
      float[][] Tensor1 = LinearAlgebra.mult( ident , Tensor );
      Tensor1=  LinearAlgebra.mult( Tensor1, ident );
      LinearAlgebra.copy( Tensor1 , Tensor );
      ident[(kk+1)%3][(kk+1)%3] = 1;
      ident[(kk+2)%3][(kk+2)%3] = 1;
     return true;
      //showNig( Tensor, UB1, NiggReal);
   }  
   return false;
   
}
   
   public static void main1( String[] args )
   {

      //System.out.println( subs.getCoordinateInformation( false ) );
      //System.exit( 0 );
      String filename = "C:\\ISAW\\SampleRuns\\SNS\\Snap\\QuartzRunsFixed\\quartz.peaks";

      Vector pks = (Vector) ( new DataSetTools.operator.Generic.TOF_SCD.ReadPeaks(
               filename ) ).getResult();
      float[][] orientMat = (float[][]) Operators.TOF_SCD.IndexJ
               .readOrient( "C:\\ISAW\\SampleRuns\\SNS\\Snap\\QuartzRunsFixed\\quartz.mat" );
      String S = subs.ShowOrientationInfo( pks , orientMat , null ,
               null , false );
      System.out.println(S );
      JFrame jf  = new JFrame("PLAIN");
      JEditorPane ed= new JEditorPane("text/plain", S);
      jf.getContentPane().add(ed);
      jf.setSize(400,400);
      jf.show();


   }
   
   /**
    * Determines whether the vectors in the 3 by 3 UB matrix represents a right
    * handed system.
    * 
    * @param UB   3 by 3 matrix considered as row(column) vectors.
    * 
    * @return  true if the rows( or columns) in UB represent a right handed
    *           system, otherwise false is returned
    */
   public static boolean isRightHanded( float[][]UB)
   {
      float MaxAbs = Float.NEGATIVE_INFINITY;
      if( UB == null || UB.length < 3)
         return false;
      
      for( int i=0; i< 3; i++)
         if( UB[i].length < 3)
            return false;
         else
            for( int j=0;j < 3; j++)
            {
               if( Math.abs( UB[i][j] )> MaxAbs)
                  MaxAbs = Math.abs( UB[i][j] );
            }
      double det = LinearAlgebra.determinant( 
                                 LinearAlgebra.float2double( UB ) );
      
      if( !Double.isNaN( det ) && det > 0)
         return true;
      
      return false;
      
   }
   
  
   //Tests new nigglify for = cases
   public static void main2( String[] args )
   {
      
      //Test results: cannot get === so gave up on testing
      // This tests 
     double[] LatConsts = new double[]{5.454 , 5.455 , 5.455  ,  89.017 , 70.512,  90.456 ,   162.28};

     double[][] RealTensor = lattice_calc.A_matrix( LatConsts );
                        
                               //{{4  ,.4f  ,.2f},{.4f  ,4  ,.3f}, {.2f , .3f  ,9}};
     double[]LatParams = lattice_calc.LatticeParamsOfA( RealTensor );
     System.out.println("Lat params=");
     LinearAlgebra.print( LatParams );
     
     double[][] RealUB =   RealTensor;
     System.out.println("RealUB=");
     LinearAlgebra.print( RealUB );
     System.out.println("Real Tensor");
     LinearAlgebra.print( LinearAlgebra.mult(LinearAlgebra.getTranspose( RealUB), RealUB  ));
     double[][] UB1 = LinearAlgebra.getInverse( RealUB);
     UB1= LinearAlgebra.getTranspose(  UB1 );// Why forgot this???
     double[][] DD =(  LinearAlgebra.mult(LinearAlgebra.getTranspose(UB1),UB1) );
     System.out.println(" Real tensor again??");
     LinearAlgebra.print( LinearAlgebra.getInverse( DD) );
     float[][] UB = LinearAlgebra.double2float( UB1);// LinearAlgebra.mult( UB1 , T));
     UB=subs.Nigglify( UB );
     LinearAlgebra.print( UB);
     System.out.println("New UB lattice parameters");
     LinearAlgebra.print( lattice_calc.LatticeParamsOfA( 
           LinearAlgebra.float2double( 
           LinearAlgebra.getInverse( ( UB )) )));
     
     System.out.println("det ="+ LinearAlgebra.determinant(
              LinearAlgebra.float2double( UB )));
     /*blind B = new blind();
     B.blaue( LinearAlgebra.double2float( UB1 ) );
     System.out.println("From blind");
     LinearAlgebra.print( B.UB );
     System.out.println("Recip tensor");
     LinearAlgebra.print(  LinearAlgebra.getInverse( LinearAlgebra.mult(LinearAlgebra.getTranspose( B.UB ),B.UB) ) );
     
*/

   }
   
   public static void main( String[] args)
   {
      float[][]UB={{ 0.1221567f,-0.0058656f,-0.1454910f},
                   {0.1622174f , 0.0039880f , 0.1131304f},
                   {  -0.1181297f , -0.2347778f,  0.0060742f }
                    };
      
      float[][] UB1 = subs.Nigglify( UB );
      
   }
}
