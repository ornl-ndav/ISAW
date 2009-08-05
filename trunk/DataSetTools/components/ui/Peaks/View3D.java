/* 
 * File: RotatePeaksInfoHandler.java
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

import java.util.Vector;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;
import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.Util.Sys.SharedMessages;
import gov.anl.ipns.ViewTools.Panels.Image.IndexColorMaker;
import gov.anl.ipns.ViewTools.Panels.ThreeD.*;
import gov.anl.ipns.ViewTools.Panels.Transforms.CoordBounds;
import gov.anl.ipns.ViewTools.Panels.Transforms.CoordJPanel;
import gov.anl.ipns.ViewTools.Panels.Transforms.CoordTransform;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.UI.*;
import java.awt.*; // import java.awt.event.KeyAdapter;
// import java.awt.event.KeyEvent;
// import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// import javax.swing.JFileChooser;
import javax.swing.*;


/**
 * This class implements a ThreeD_JPanel that displays and manipulates Peaks.
 * The manipulations supported are 1) Rotating- ActionListener Message
 * ROTATED_DISPLAY and getCoordTransform 2) Selecting Peaks- ActionListener
 * Message SELECTEC_PEAK and getLastSelectedSeqNum
 * 
 * Planes and families of parallel planes can also be displayed Peaks can be
 * omitted from this display.
 * 
 * NOTE: It is assumed throughout that the sequence number of a peak is one more
 * than the index of the peak in the peaks vector.
 */
public class View3D extends ThreeD_JPanel
{


    public static Color UP_AXIS_COLOR = Color.GREEN.darker();
    public static Color BEAM_AXIS_COLOR = Color.red;
    public static Color THIRD_AXIS_COLOR = Color.blue;
    public static Color  SELECTED_PEAK_COLOR = Color.white;
   /*
    * Mouse mode that is used for selecting peaks
    */
   public static int    PickMode              = 31;

   /**
    * Mouse mode that is used for rotating the peak display
    */
   public static int    RotateMode            = 32;

   /**
    * Mouse mode that disables the mouse
    */
   public static int    NoMouseMode           = 30;

   /**
    * Action event command name signifying the view was rotated. The value is
    * "Display has rotated"
    */
   public static String ROTATED_DISPLAY       = "Display has rotated";

   /**
    * Action event command name signifying a peak was chosen. The value is "New
    * Peak Selected"
    */
   public static String SELECTED_PEAK_CHANGED = "New Peak Selected";

   /**
    * Action event command name signifying the right mouse button was Clicked.
    * The value is "Right Button Clicked"
    * 
    */
   public static String RIGHT_CLICKED         = "Right Button Clicked";

   Vector< IPeak >      Peaks;

   float                MinQ , // Min Q value of all the peaks
                        MaxQ;  // Max Q value of all the peaks


   float                MinIntensity , // Min intensity for all the peaks
                        MaxIntensity;  // Max intensity for all peaks

   // Show peaks from different runs with different colors
   boolean              RunsColored           = false;

   // Show peaks from different detectors with different colors
   boolean              DetsColored           = false;


   Vector< Integer >    omittedSeqNums;

   Vector< Integer >    runNums;// Vector of all run numbers for these peaks

   Vector< Integer >    detNums;  // Vector of all detector numbers for these peaks

   Tran3D               currentTransformation;  // The current transformation from points to the
                                                // 2D display

   MyMouseListener      MouseListener;  // Mouse listener

   // Help String for this view
   public static String Help                  = "<html><body> *The green axis is Vertical<BR>*The red axis is in "
                                                       + "the beam direction<BR>*The other axis is blue and<BR> the up,beam and other "
                                                       + "axis make a right-hand orthogonal system<P> ";


   /*
    * Constructor
    * @param peaks   The Vector of peaks
    * 
    */
   public View3D( Vector< IPeak > peaks )
   {

      super();
      this.Peaks = peaks;
      if( peaks == null )
         return;
      
      omittedSeqNums = new Vector< Integer >();
      
      MinQ = MinIntensity = Float.POSITIVE_INFINITY;
      MaxQ = MaxIntensity = Float.NEGATIVE_INFINITY;
      
      runNums = new Vector< Integer >();
      detNums = new Vector< Integer >();
      
      for( int i = 0 ; i < peaks.size() ; i++ )
      {
         IPeak P = peaks.elementAt( i );
         float[] Qs = P.getUnrotQ();
         
         float QQ = (float) Math.sqrt( Qs[ 0 ] * Qs[ 0 ] + Qs[ 1 ] * Qs[ 1 ]
                  + Qs[ 2 ] * Qs[ 2 ] );
         if( QQ < MinQ )
            MinQ = QQ;
         if( QQ > MaxQ )
            MaxQ = QQ;
         
         float Intensity = P.ipkobs();
         
         if( Intensity < MinIntensity )
            
            MinIntensity = Intensity;
         
         
         if( Intensity > MaxIntensity )
            
            MaxIntensity = Intensity;
         
         
         if( ! runNums.contains( P.nrun() ) )
            
            runNums.add( P.nrun() );
         
         
         if( ! detNums.contains( P.detnum() ) )
            
            detNums.add( P.detnum() );


      }
      MaxQ = Math.max(  Math.abs(MaxQ) , Math.abs(MinQ) );
      MinQ = -MaxQ;
      setGlobalWorldCoords( new CoordBounds( - MaxQ , MaxQ , MaxQ , - MaxQ ) );
      setLocalWorldCoords( new CoordBounds( - MaxQ , MaxQ , MaxQ ,- MaxQ ) );
      
      setBackground( Color.lightGray );
      
      CreateAdd3DObjects( peaks );
      drawAxes();
      
      currentTransformation = new Tran3D();
      
      currentTransformation.setIdentity();
      currentTransformation.setRotation(-90, new Vector3D(1f,0f,0f));
      SetViewing( currentTransformation );
      
      MouseListener = new MyMouseListener( this );
      addMouseMotionListener( MouseListener );
      addMouseListener( MouseListener );
      
      MouseListener.setMode( RotateMode );
      
      CJP_handle_arrow_keys = false;//Do not let CoordJPanel handle the mouse
   }


   /**
    * Sets the mouse mode
    * 
    * @param newMode
    *           the desired mode, either "PickMode", RotateMode, or NoMouseMode
    * 
    */
   public void setMouseMode( int newMode )
   {

      MouseListener.setMode( newMode );

   }


   /**
    * 
    * @return the current transformation that maps 3D Q vectors to the 2D
    *         display
    */
   public Tran3D getCurrentTransformation()
   {

      return currentTransformation;
   }


   /**
    * Returns the last selected id or -1 if no peaks have been selected
    * 
    * @return the last selected id or first Peak if none have been selected
    */
   public int getLastSelectedSeqNum()
   {

      int id = MouseListener.getPickedID();
      
      if( id > 0 && id <= Peaks.size() )
         
         return id;
      
      
      return 1;
   }


   public void showOrientPeaks( float[][] orientationMatrix)
   {
      if( orientationMatrix == null || orientationMatrix.length !=3)
      {
         removeObjects("PredPeaks");
         repaint();
         return;
      }
      float[] q = new float[3];
      q[0] = q[1] = q[2] = Math.max(  Math.abs( MinQ) , Math.abs( MaxQ) );
      int M=subs.MaxHKLVal( orientationMatrix , (float)Math.sqrt( 3*q[0]*q[0] ) );
      Vector<Vector3D> obj = new Vector<Vector3D>();
      for( int h=-M; h<=M; h++)
         for( int k=-M; k<=M; k++)
           for( int l=-M; l<=M; l++)
           {
              float[] Q = new float[3];
              boolean keep = true;
              for(int s=0; s<3;s++)
              {
                 Q[s]= orientationMatrix[s][0]*h+orientationMatrix[s][1]*k+
                 orientationMatrix[s][2]*l;
                 if( Q[s] > MaxQ)
                    keep =false;
                 else if( Q[s]< MinQ)
                    keep = false;
              }
             if( keep)
                obj.addElement( new Vector3D(Q)) ;
            
           }
      Vector3D[] Vs = obj.toArray( new Vector3D[0]);
      Polymarker[] Pm= new Polymarker[1];
      Pm[0]= new Polymarker(Vs, Color.blue);
      Pm[0].setType( Polymarker.BOX);
      Pm[0].setSize(3);
      setObjects("PredPeaks", Pm);
      repaint();
      
   }
   /**
    * Causes the 3D view of Q space to show the orientation matrix
    * 
    * @param OrientationMatrix
    *           The orientation matrix to view
    * @param seqNum
    *           The sequence number of the peak on which the orientation matrix
    *           will be originated.
    */
   public void showOrientation( float[][] OrientationMatrix , int seqNum )
   {

      if( OrientationMatrix == null )
      {
         this.removeObjects( "OrientationMatrix" );
         repaint();
         return;
      }
      
      if( seqNum < 1 || seqNum + 1 >= Peaks.size()
               || omittedSeqNums.contains( seqNum ) )
         
         return;
      
      
      float[] Qs = Peaks.elementAt( seqNum - 1 ).getUnrotQ();
      
      IThreeD_Object[] orMat = new IThreeD_Object[ 3 ];
      
      Vector3D[] verts = new Vector3D[ 2 ];
      verts[ 0 ] = new Vector3D( Qs[ 0 ] , Qs[ 1 ] , Qs[ 2 ] );
      verts[ 1 ] = new Vector3D( Qs[ 0 ] + OrientationMatrix[ 0 ][ 0 ] , Qs[ 1 ]
               + OrientationMatrix[ 1 ][ 0 ] , Qs[ 2 ]
               + OrientationMatrix[ 2 ][ 0 ] );
      orMat[ 0 ] = new Polyline( verts , new Color( 255 , 0 , 0 , 150 ) );

      verts = new Vector3D[ 2 ];
      verts[ 0 ] = new Vector3D( Qs[ 0 ] , Qs[ 1 ] , Qs[ 2 ] );
      verts[ 1 ] = new Vector3D( Qs[ 0 ] + OrientationMatrix[ 0 ][ 1 ] , Qs[ 1 ]
               + OrientationMatrix[ 1 ][ 1 ] , Qs[ 2 ]
               + OrientationMatrix[ 2 ][ 1 ] );
      orMat[ 1 ] = new Polyline( verts , new Color( 255 , 255 , 0 , 150 ) );

      verts = new Vector3D[ 2 ];
      verts[ 0 ] = new Vector3D( Qs[ 0 ] , Qs[ 1 ] , Qs[ 2 ] );
      verts[ 1 ] = new Vector3D( Qs[ 0 ] + OrientationMatrix[ 0 ][ 2 ] , Qs[ 1 ]
               + OrientationMatrix[ 1 ][ 2 ] , Qs[ 2 ]
               + OrientationMatrix[ 2 ][ 2 ] );
      orMat[ 2 ] = new Polyline( verts , new Color( 0 , 0 , 255 , 150 ) );
      
      this.setObjects( "OrientationMatrix" , orMat );

      repaint();
   }

  Polymarker[] HighLightPeak( int[] seqNums, boolean dull)
   {
      if( seqNums == null || seqNums.length < 1)
      {
         return null;
      }
      Color C = SELECTED_PEAK_COLOR;
      //if( dull)
      //   C = C.darker();
      Vector<Vector3D> V = new Vector<Vector3D>();
      for( int i=0; i< seqNums.length; i++)
         if( seqNums.length >=1 && seqNums.length <= Peaks.size())
         {
            float[] Qs = Peaks.elementAt( seqNums[i]-1 ).getUnrotQ();
            if( Qs != null)
            {
              V.add(  new Vector3D(Qs) );
            }
         }
      Polymarker P = new Polymarker( V.toArray( new Vector3D[0] ),C);
      P.setSize( 5 );
      if( dull)
         P.setType( Polymarker.CROSS );
      else
         P.setType( Polymarker.BOX );
      Polymarker[] res = new Polymarker[1];
      res[0] = P;
      return res;
      
   }
   /**
    * Will highlight the given peak. 
    * @param seqNum  The sequence number of the peak. If not in
    *                range, no selected peak will be shown
    * @param dull    If dull, the color will be darker than usual
    */
   public void showSelectedPeak( int seqNum, boolean dull)
   {
     // return;
    
      if( seqNum < 1 || seqNum >Peaks.size())
      {
         removeObjects("Selected Peaks");
         return;
      }
      int[] seqNums = new int[1];
      seqNums[0] =  seqNum;
      if( dull)
      {
         Polymarker[] res = HighLightPeak( seqNums, true);
         setObjects("Selected Peaks", res);
         return;
      }
      showSelectedPeaks( seqNums );
     
   }
   
   public void showSelectedPeaks(int[] seqNums)
   {
      // return;
    
      if( seqNums == null)
      {
         removeObjects("Selected Peaks");
         repaint();
         return;
      }
     
      
     Polymarker[] B = HighLightPeak( seqNums, false);
    
      setObjects("Selected Peaks", B);
      repaint();
   
     
   }
   private float getVal( float[] normalUnit , int maxCoeffIndex ,
            float[] pt , float MinMax1 , float MinMax2)
   {
      if( normalUnit[maxCoeffIndex] == 0 )
         return Float.NaN;
      
      float zz = normalUnit[ ( maxCoeffIndex + 1 ) % 3 ]
                             * ( MinMax1 - pt[ ( maxCoeffIndex + 1 ) % 3 ] )
                             + normalUnit[ ( maxCoeffIndex + 2 ) % 3 ]
                             * ( MinMax2 - pt[ ( maxCoeffIndex + 2 ) % 3 ] );
                    
       zz = pt[ maxCoeffIndex ] - zz / normalUnit[ maxCoeffIndex ];
       
       return zz;
                  
   }
   // Gets the point on the plane with normal(unit) and through pt. The other
   // point
   // has coordinates MinMax1 and MinMax2 in the two positions that follow
   // maxCoeffIndex. The coordinate with the maxCoeffIndex is calculated
   private Vector3D getPoint( float[] normalUnit , int maxCoeffIndex ,
            float[] pt , float MinMax1 , float MinMax2 )
   {

      float zz =getVal( normalUnit, maxCoeffIndex,pt, MinMax1, MinMax2);
      float[] MinMax = new float[3];

      MinMax[ (maxCoeffIndex+1)%3] = MinMax1;
      MinMax[ (maxCoeffIndex+2)%3] = MinMax2;
      
     /* if( Float.isNaN( zz ) || zz < MinQ || zz > MaxQ)
      {
         if( Float.isNaN( zz ))
         {
            System.out.println("All are zero "+ normalUnit[0]+","+ normalUnit[1]+","+ normalUnit[2]);
            return null; //Should not happen unless everything is zero
         }else if( zz < MinQ)
            MinMax[ maxCoeffIndex ]= MinQ;
         else
            MinMax[ maxCoeffIndex ]= MaxQ;
         
         int i1 = (maxCoeffIndex+1)%3;
         int i2 = (maxCoeffIndex+2)%3;
         if( Math.abs( normalUnit[i1]) < Math.abs(  normalUnit[(i1+1)%3] ))
         {
            int sav = i1;
            i1 = i2;
            i2 = sav;  
         }

         maxCoeffIndex = i1;
         zz = getVal( normalUnit, i1,pt, MinMax1, MinMax2);
         if( Float.isNaN( zz) || zz < MinQ || zz > MaxQ)
         {
            zz = getVal( normalUnit, i2,pt, MinMax1, MinMax2);
            maxCoeffIndex = i2;
         }
         if( Float.isNaN( zz) || zz < MinQ || zz > MaxQ)
         {  System.out.println("XXXXXX"+ normalUnit[0]+","+ normalUnit[1]+","+ normalUnit[2]);
            return null;
         }
            
      }
      */
      float[] Res = new float[ 3 ];
      Res[ maxCoeffIndex ] = zz;
      Res[ ( maxCoeffIndex + 1 ) % 3 ] = MinMax[( maxCoeffIndex + 1 ) % 3 ];
      Res[ ( maxCoeffIndex + 2 ) % 3 ] =  MinMax[( maxCoeffIndex + 2 ) % 3 ];;


      return new Vector3D( Res );
   }


   // Finds the plane through the 3 q values and returns it as an
   // IThreeD_Object. The sides of the plane are where the plane hits
   // the side of the 3D container.
   private IThreeD_Object getPlane( float[] q1 , float[] q2 , float[] q3 )
   {

      Vector3D v1 = new Vector3D( q2 );
      Vector3D V1 = new Vector3D( q1 );
      
      v1.subtract( V1 );
      
      Vector3D v2 = new Vector3D( q3 );
      
      v2.subtract( V1 );
      v1.cross( v2 );
      v1.standardize();
      v1.normalize();
      
      float[] coeff = v1.get();
      
      float Max = Math.abs( coeff[ 0 ] );
      int i1 = 0;
      for( int i = 1 ; i < 3 ; i++ )
         if( Math.abs( coeff[ i ] ) > Max )
         {
            Max = Math.abs( coeff[ i ] );
            i1 = i;
         }

      if( Max < .001 )
         return null;

      Vector3D[] verts = new Vector3D[ 4 ];
      verts[ 0 ] = getPoint( coeff , i1 , q1 , MinQ/2 , MinQ/2 );
      verts[ 1 ] = getPoint( coeff , i1 , q1 , MinQ/2 , MaxQ/2 );
      verts[ 2 ] = getPoint( coeff , i1 , q1 , MaxQ/2 , MaxQ/2);
      verts[ 3 ] = getPoint( coeff , i1 , q1 , MaxQ/2 , MinQ/2 );
      
      if( verts[0] == null || verts[1] == null || verts[2] == null ||
               verts[3] == null )
          return null;
      int npatterns = 3;
      Vector3D[] V = new Vector3D[ 8*npatterns+3 ];
      V[0] =verts[0];
      Vector3D HDir = new Vector3D(verts[1]);
      HDir.subtract( verts[0] );
      HDir.multiply(1f/(2*npatterns+1));
      Vector3D VDir = new Vector3D(verts[3]);
      VDir.subtract(verts[0]);
      for( int j=0; j<2; j++)
      {
         int startj=1+ j*(1+ npatterns*4);
         for( int i = 0 ; i < npatterns ; i++ )
         {
            V[ startj+ 4*i  ] = new Vector3D( V[ startj+ 4*i -1] );
            V[ startj+4*i  ].add( HDir );
            V[  startj+4*i + 1 ] = new Vector3D( V[startj+4*i ] );
            V[ startj+ 4*i + 1 ].add( VDir );
            V[ startj+ 4*i + 2 ] = new Vector3D( V[ startj+ 4*i + 1 ] );
            V[ startj+ 4*i + 2 ].add( HDir );
            V[ startj+ 4*i + 3 ] = new Vector3D( V[  startj+ 4*i + 2  ] );
            V[ startj+ 4*i + 3 ].subtract( VDir );

         }
         if( j < 1)
           V[ 1+4*npatterns ] = verts[ 1 ];
         else
            V[8*npatterns+2]= verts[2];

        
         HDir = new Vector3D( verts[ 2 ] );
         HDir.subtract( verts[ 1 ] );
         HDir.multiply(  1f/(2*npatterns+1) );
         VDir = new Vector3D( verts[ 0 ] );
         VDir.subtract( verts[ 1 ] );
      }
      
      //gov.anl.ipns.ViewTools.Panels.ThreeD.Polyline P = new gov.anl.ipns.ViewTools.Panels.ThreeD.Polyline( V ,
      //         new Color( .5f , 0f , .5f , .1f ) ); 
      gov.anl.ipns.ViewTools.Panels.ThreeD.Polygon P = new gov.anl.ipns.ViewTools.Panels.ThreeD.Polygon( verts ,
                        new Color( .5f , 0f , .5f , .1f ) );
     // P.setType(  gov.anl.ipns.ViewTools.Panels.ThreeD.Polygon.HOLLOW );
      return P;

   }


   /**
    * Shows the plane in the 3D view of Q space through the 3 q points
    * 
    * @param q1
    *           First Q on the plane
    * @param q2
    *           Second Q on the plane
    * @param q3
    *           Third Q on the plane
    */
   public void showPlane( float[] q1 , float[] q2 , float[] q3 )
   {

      if( q1 == null || q2 == null || q3 == null )
      {
         this.removeObjects( "Plane" );
         repaint();
         return;
      }
      
      IThreeD_Object[] objs = new IThreeD_Object[ 1 ];
      objs[ 0 ] = getPlane( q1 , q2 , q3 );
      if( objs == null || objs[0] == null)
      {
         JOptionPane.showMessageDialog( null , "Peaks may be null or collinear");
         return;
      }
      setObjects( "Plane" , objs );

      repaint();
   }


   /**
    * Shows the family of planes parallel to the plane through q1, q2, and q3
    * and with the direction between the planes as a multilplt of the direction
    * from q1 to q4
    * 
    * @param q1
    *           First Q on the plane
    * @param q2
    *           Second Q on the plane
    * @param q3
    *           Third Q on the plane
    * @param q4
    *           Direction from q1 to q4 gives the direction to the next plane
    */
   public void showPlanes( float[] q1 , float[] q2 , float[] q3 , float[] q4 )
   {

      if( q1 == null || q2 == null || q3 == null || q4 == null )
      {
         this.removeObjects( "Plane" );
         repaint();
         return;
      }
      
      float[][] Q , Q_Sav;
      Q = new float[ 4 ][ 3 ];
      Q_Sav = new float[ 4 ][ 3 ];
      for( int i = 0 ; i < 3 ; i++ )
      {
         Q[ 0 ][ i ] = 0;
         Q[ 1 ][ i ] = q2[ i ] - q1[ i ];
         Q[ 2 ][ i ] = q3[ i ] - q1[ i ];
         Q[ 3 ][ i ] = q4[ i ] - q1[ i ];
         
         Q_Sav[ 0 ][ i ] = 0;
         Q_Sav[ 1 ][ i ] = q2[ i ] - q1[ i ];
         Q_Sav[ 2 ][ i ] = q3[ i ] - q1[ i ];
         Q_Sav[ 3 ][ i ] = q4[ i ] - q1[ i ];
      }

      // Shifts to get new vectors back to center
      float[][] shifts = new float[ 3 ][ 3 ];
      System.arraycopy( q2 , 0 , shifts[ 0 ] , 0 , 3 );
      System.arraycopy( q3 , 0 , shifts[ 1 ] , 0 , 3 );
      for( int i = 0 ; i < 3 ; i++ )
         shifts[ 2 ][ i ] = Q[ 2 ][ i ] - Q[ 1 ][ i ];

      Vector< IThreeD_Object > objs = new Vector< IThreeD_Object >();
      
      IThreeD_Object newPlane =getPlane( Q[ 0 ] , Q[ 1 ] , Q[ 2 ] );
      
      boolean NoPlaneShownMessage = false;
      if( newPlane != null)
         
         objs.addElement( newPlane );
      
      else
      {   
         NoPlaneShownMessage = true;
         SharedMessages.addmsg( " Cannot create plane from points. "+
                          "May be collinear or null" );
      }
      
      float MM = Math.max( Math.abs( MaxQ ) , Math.abs( MinQ ) );
      float MxQ = MM / 2;
      
      for( int time = 0 ; time < 20 && MxQ < MM ; time++ )
      {
         
         for( int i = 0 ; i < 3 ; i++ )
            for( int j = 0 ; j < 3 ; j++ )
               Q[ i ][ j ] += Q[ 3 ][ j ];
         
         MxQ = ShiftQs( Q , shifts );
         
         newPlane =getPlane( Q[ 0 ] , Q[ 1 ] , Q[ 2 ] );
         
         if( newPlane != null)
            
            objs.addElement( newPlane );
         
         else if( !NoPlaneShownMessage)
         {   
            NoPlaneShownMessage = true;
            SharedMessages.addmsg( " Cannot create plane from points. "+
                             "May be collinear or null" );
         }
      }
      
      MxQ = MM / 2;
      for( int time = 0 ; time < 20 && MxQ < MM ; time++ )
      {
         for( int i = 0 ; i < 3 ; i++ )
            for( int j = 0 ; j < 3 ; j++ )
               Q_Sav[ i ][ j ] -= Q_Sav[ 3 ][ j ];
         
         MxQ = ShiftQs( Q_Sav , shifts );
          newPlane =getPlane(Q_Sav[ 0 ] , Q_Sav[ 1 ] , Q_Sav[ 2 ] );
         
         if( newPlane != null)
            
             objs.addElement( newPlane );

         else if( !NoPlaneShownMessage)
         {   
            NoPlaneShownMessage = true;
            SharedMessages.addmsg( " Cannot create plane from points. "+
                             "May be collinear or null" );
         }
      }
      
  
      
      IThreeD_Object[] obj = objs.toArray( new IThreeD_Object[ 0 ] );
      this.setObjects( "Plane" , obj );
      
      repaint();

   }


   private float ShiftQs( float[][] Q , float[][] Shifts )
   {

      float Maxx = Math.abs( Q[ 0 ][ 0 ] );
      
      for( int i = 0 ; i < 3 ; i++ )
         for( int j = 0 ; j < 3 ; j++ )
            if( Math.abs( Q[ i ][ j ] ) < Maxx )
               Maxx = Math.abs( Q[ i ][ j ] );

      return Maxx;
   }


   private int absMax( float[] list )
   {

      float Mx = Math.abs( list[ 0 ] );
      int i1 = 0;
      
      for( int i = 1 ; i < 3 ; i++ )
         if( Math.abs( list[ i ] ) > Mx )
         {
            Mx = Math.abs( list[ i ] );
            i1 = i;
         }
      
      return i1;

   }


   /**
    * Display peaks from different runs and/or detectors with different colors
    * 
    * @param runs
    *           if true, peaks in different runs will have different colors
    * @param Detectors
    *           if true, peaks from different detectors will have different
    *           colors
    */
   public void ColorRunsDetectors( boolean runs , boolean Detectors )
   {

      int ncolors = 1;
      
      if( runs )
         ncolors *= runNums.size();
      
      if( Detectors )
         ncolors *= detNums.size();
    
      RunsColored = runs;
      DetsColored = Detectors;
      Color[] colorTable = getColorTable();

      int D = 1;
      if( Detectors )
         D = detNums.size();
      
      int R = 1;
      
      if( runs )
         R = runNums.size();

      float ratio = 1;
      
      if( R * D > 256 ) // colorTable has max of 256 elements
         ratio = 256f / ( R * D );

      for( int i = 0 ; i < runNums.size() ; i++ )
         for( int j = 0 ; j < detNums.size() ; j++ )
         {
            int index = 0;
            
            if( runs )
               index = i * D;
            
            if( Detectors )
               index += j;
            
            index = Math.min( colorTable.length - 1 , (int) ( index * ratio ) );
            
            this.setColors( "R" + runNums.elementAt( i ).toString() + "D"
                     + detNums.elementAt( j ) , colorTable[ index ] );
            
         }
      
      repaint();
      
   }


   private Color[] getColorTable()
   {

      int ncolors = 1;
      if( RunsColored )
         ncolors *= runNums.size();
      
      if( DetsColored )
         ncolors *= detNums.size();

      Color[] colorTable = IndexColorMaker.getColorTable(
               IndexColorMaker.RAINBOW_SCALE , ncolors );

      if( ncolors <= 1 )
         for( int i = 0 ; i < colorTable.length ; i++ )
            colorTable[ i ] = Color.blue;
      
      return colorTable;
   }


   private float getRatio()
   {

      int ncolors = 1;
      if( RunsColored )
         ncolors = runNums.size();
      
      if( DetsColored )
         ncolors *= detNums.size();
      
      if( ncolors < 256 )
         return 1f;
      
      return 256f / ncolors;
   }


   private Color getDefaultColor( int runNum , int DetNum )
   {

      Color[] colorTable = getColorTable();
      int i = runNums.indexOf( runNum );
      int j = detNums.indexOf( DetNum );
      int D = detNums.size();
      
      if( ! DetsColored )
      {
         D = 1;
         j = 0;
      }
      if( ! RunsColored )
         i = 0;
      // float ratio = getRatio();
      
      return colorTable[ i * D + j ];


   }


   // TODO Manipulate planes
   /**
    * Highlights as white(mark=true) or default( mark= false) the indicated
    * sequence numbers
    */
   public void HighlightSeqNums( int[] seqNums , boolean mark )
   {

      if( seqNums == null || seqNums.length < 1 )
         return;
      
      Vector< Integer > CseqNums = new Vector< Integer >( seqNums.length );
      
      for( int i = 0 ; i < seqNums.length ; i++ )
         if( seqNums[ i ] >= 0 && seqNums[ i ] < Peaks.size() )
            CseqNums.addElement( seqNums[ i ] );
      
      int runNum = - 1;
      int detNum = - 1;
      IThreeD_Object[] objs = null;
      
      for( int i = 0 ; i < seqNums.length ; i++ )
         if( seqNums[ i ] >= 1 && seqNums[ i ] <= Peaks.size() )
         {
            IPeak Pk = Peaks.elementAt( seqNums[ i ] - 1 );
            
            if( Pk.nrun() != runNum || Pk.detnum() != detNum )
            {
               runNum = Pk.nrun();
               detNum = Pk.detnum();
               String name = "R" + runNum + "D" + detNum;
               
               objs = this.getObjects( name );
               
               Color color = Color.white;
               if( ! mark )
                  color = getDefaultColor( runNum , detNum );
               
               if( objs != null )
                  for( int k = 0 ; k < objs.length ; k++ )
                     
                     if( CseqNums.contains( objs[ k ].getPickID() ) )
                        objs[ k ].setColor( color );

            }
         }

      repaint();

   }


   private Vector< Integer > getRunNums( int[] seqNums )
   {

      Vector< Integer > res = new Vector< Integer >();
      
      if( seqNums == null || seqNums.length < 1 )
         return res;
      
      for( int i = 0 ; i < seqNums.length ; i++ )
         if( seqNums[ i ] >= 0 && seqNums[ i ] <= Peaks.size() )
         {
            IPeak Pk = Peaks.elementAt( seqNums[ i ] - 1 );
            if( ! res.contains( Pk.nrun() ) )
               res.addElement( Pk.nrun() );
         }
      
      return res;
   }


   private Vector< Integer > getDetNums( int[] seqNums )
   {

      Vector< Integer > res = new Vector< Integer >();
      
      if( seqNums == null || seqNums.length < 1 )
         return res;
      
      for( int i = 0 ; i < seqNums.length ; i++ )
         if( seqNums[ i ] >= 0 && seqNums[ i ] <= Peaks.size() )
         {
            IPeak Pk = Peaks.elementAt( seqNums[ i ] - 1 );
            
            if( ! res.contains( Pk.detnum() ) )
               res.addElement( Pk.detnum() );
         }
      
      return res;

   }


   /**
    * Adds the following sequence numbers to the omitted sequence numbers
    * 
    * @param seqNums
    *           The sequence numbers to omit
    */
   public void omitSeqNums( int[] seqNums )
   {

      if( seqNums == null || seqNums.length < 1 )
         return;
      
      Vector CseqNums = new Vector( seqNums.length );
      
      for( int i = 0 ; i < seqNums.length ; i++ )
         
         if( seqNums[ i ] >= 0 && seqNums[ i ] < Peaks.size() )
            CseqNums.addElement( seqNums[ i ] );

      Vector< Integer > runs_loc = getRunNums( seqNums ) , dets_loc = getDetNums( seqNums );
      
      for( int i = 0 ; i < runs_loc.size() ; i++ )
         for( int j = 0 ; j < dets_loc.size() ; j++ )
         {
            String name = "R" + runs_loc.elementAt( i ).toString() + "D"
                     + dets_loc.elementAt( j );
            IThreeD_Object[] objs = this.getObjects( name );
            
            if( objs != null )
            {
               Vector< IThreeD_Object > newObjs = new Vector< IThreeD_Object >();
               
               for( int k = 0 ; k < objs.length ; k++ )
                  if( ! CseqNums.contains( objs[ k ].getPickID() ) )
                     
                     newObjs.addElement( objs[ k ] );
               
               if( newObjs.size() < 1)
               {
                  this.removeObjects( name );
                  
               }else
               {
               IThreeD_Object[] new3DObjs = newObjs
                        .toArray( new IThreeD_Object[ 0 ] );
               
               this.setObjects( name , new3DObjs );
               }
            }

         }
      
      for( int i = 0 ; i < seqNums.length ; i++ )
         if( seqNums[ i ] >= 0 && seqNums[ i ] < Peaks.size() )
            
            omittedSeqNums.addElement( seqNums[ i ] );

      getAllObjects();
      
      repaint();
   }


   /**
    * The list of sequence numbers that should be included( not omitted)
    * 
    * @param seqNums
    *           seqNums The sequence numbers to be included
    */
   public void IncludeSeqNums( int[] seqNums )
   {

      if( seqNums == null || seqNums.length < 1 )
         return;

      int[] seq_numsCopy = new int[ seqNums.length ];
      System.arraycopy( seqNums , 0 , seq_numsCopy , 0 , seqNums.length );
      java.util.Arrays.sort( seq_numsCopy );

      int runNum = Peaks.elementAt( seq_numsCopy[ 0 ] ).nrun();
      int detNum = Peaks.elementAt( seq_numsCopy[ 0 ] ).detnum();
      
      IThreeD_Object[] objs = this.getObjects( "R" + runNum + "D" + detNum );
      
      Vector< IThreeD_Object > added = new Vector< IThreeD_Object >();
      
      for( int i = 0 ; i < seqNums.length ; i++ )
      {
         IPeak Pk = Peaks.elementAt( i );
         if( this.omittedSeqNums.contains( Pk.seqnum() ) )
         {
            if( Pk.nrun() != runNum || Pk.detnum() != detNum )
            {
               if( added.size() > 0 )
               {
                  updateObjects( runNum , detNum , objs , added );
                  runNum = Pk.nrun();
                  detNum = Pk.detnum();
                  added.clear();
                  
                  objs = this.getObjects( "R" + runNum + "D" + detNum );
               }
            }
            
            omittedSeqNums.remove( new Integer( Pk.seqnum() ) );
            Vector3D[] marks = new Vector3D[ 1 ];
            marks[ 0 ] = new Vector3D( Pk.getUnrotQ() );
            int size = 5 + (int) ( ( Pk.ipkobs() - MinIntensity )
                     / ( MaxIntensity - MinIntensity ) * 15 );
            RoundBall pk = new RoundBall( marks[0] , (float)size,Color.blue );
           
            
           // pk.setSize( size );
           // pk.setType( Polymarker.STAR );
            pk.setPickID( Pk.seqnum() );
            
            added.addElement( pk );
         }


      }
      
      updateObjects( runNum , detNum , objs , added );
      
      getAllObjects();
      
      repaint();


   }


   private void updateObjects( int runNum , int detNum , IThreeD_Object[] objs ,
            Vector< IThreeD_Object > added )
   {

      if( added.size() < 1 )
         return;
      
      if( objs != null )
         for( int k = 0 ; k < objs.length ; k++ )
            
            added.addElement( objs[ k ] );
      
      objs = added.toArray( new IThreeD_Object[ 1 ] );
      
      setObjects( "R" + runNum + "D" + detNum , objs );
   }


   /**
    * Handles all mouse event actions. Keyboard actions have not been been
    * implemented yet. Also, separate handlers should be used for separate
    * modes.
    * 
    * @author Ruth
    * 
    */
   class MyMouseListener implements MouseListener , MouseMotionListener
   {



      Point    Drag;

      boolean  dragStarted;

      View3D   panel;

      Vector3D posPicked;

      float    lengthPick_orig;

      int      mode;

      int      id;


      /**
       * Constructor
       * 
       * @param View The 3D view of Q space
       */
      public MyMouseListener( View3D View )
      {

         Drag = null;
         this.panel = View;
         mode = PickMode;
         id = IThreeD_Object.INVALID_PICK_ID;
         dragStarted = false;
      }


      /**
       * Sets the mouse mode
       * 
       * @param new_mode
       *           the new mode
       * @see View3D.MouseMode(int)
       */
      public void setMode( int new_mode )
      {

         mode = new_mode;
         Drag = null;
         posPicked = null;
         lengthPick_orig = Float.NaN;
      }


      /**
       * 
       * @return The sequence number of the last selected peak
       */
      public int getPickedID()
      {

         return id;
      }


      /*
       * If in RotateMode, will cause the display to rotate as the mouse is
       * dragged (non-Javadoc)
       * 
       * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
       */
      @Override
      public void mouseDragged( MouseEvent e )
      {

         
        
         if( mode != RotateMode )
            return;
         
         if( ! dragStarted )
            return;
         
         Point Drag_old = Drag;
         Drag = panel.getCurrent_pixel_point();
         
         if( Drag_old == null )
         {

            int idd = panel.pickID( Drag.x , Drag.y , 12 );
            IThreeD_Object obj = panel.pickedObject();
            
            if( obj == null || idd == IThreeD_Object.INVALID_PICK_ID )
            {
               Drag = null;
               dragStarted = false;
               System.out.println( "No item picked" );
               return;
            }
            else
            {
               posPicked = obj.position();
               Vector3D v2 = new Vector3D();
               panel.currentTransformation.apply_to( posPicked , v2 );

               float x = panel.getLocal_transform().MapXTo( v2.getX() );
               float y = panel.getLocal_transform().MapYTo( v2.getY() );

               if( Math.abs( x - Drag.x ) >= 4 || Math.abs( y - Drag.y ) >= 4 )
               {
                  Drag = null;
                  dragStarted = false;
                  System.out.println( "out of kilter points" );
                  return;
                  
               }
               posPicked = v2;

            }
            
            lengthPick_orig = posPicked.length();


            return;
         }

         // Now dragging

         int dx = Drag.x - Drag_old.x;
         int dy = Drag.y - Drag_old.y;
         
         CoordTransform transform = panel.getLocal_transform();
         
         float xPix = transform.MapXTo( posPicked.getX() ) + dx;
         float yPix = transform.MapYTo( posPicked.getY() ) + dy;
         
         float xQ = transform.MapXFrom( xPix );
         float yQ = transform.MapYFrom( yPix );
         
        
         float zQsq = lengthPick_orig * lengthPick_orig - xQ * xQ - yQ * yQ;
         
         float zQ = Float.NaN;
         if( zQsq < 0 )
         {
            /*
             * zQ = MaxQ / 900; //keeps cursor going if( posPicked.getZ() < 0 )
             * zQ = - zQ;
             */

            posPicked = new Vector3D( posPicked.getX() , posPicked.getY() ,
                     - posPicked.getZ() );
            
            panel.setCurrent_pixel_point( Drag_old );
            Drag = Drag_old;

            javax.swing.SwingUtilities.invokeLater( new drawCursor( panel
                     .getCurrent_pixel_point() , panel ) );
            return;

         }
         else
         {
            zQ = (float) Math.sqrt( zQsq );
            
            if( posPicked.getZ() <= - MaxQ / 1000 )
               
               zQ = - zQ;
            
            else if( posPicked.getZ() <= MaxQ / 1000 )
               
               if( posPicked.getZ() > 0 )
                  
                  zQ = - MaxQ / 1000;
            
               else
                  
                  zQ = MaxQ / 1000;
         }
         // Now get a transformation from old PosPicked to Qx,Qy,Qz and apply it
         // ti
         // the currenttransformation.

         Vector3D vpos2 = new Vector3D( xQ , yQ , zQ );
         Vector3D norm = new Vector3D();
         norm.cross( posPicked , vpos2 );
         
         double angle = Math.acos( posPicked.dot( vpos2 ) / posPicked.length()
                  / vpos2.length() )* 180 / Math.PI;
         
         Tran3D trans = new Tran3D();
         trans.setRotation( (float) angle , norm );
         
         trans.multiply_by( panel.currentTransformation );
         
         panel.currentTransformation = trans;
         panel.SetViewing( trans );

         posPicked = vpos2;
         lengthPick_orig = posPicked.length();
         
         panel.repaint();
         
         javax.swing.SwingUtilities.invokeLater( new drawCursor( panel
                  .getCurrent_pixel_point() , panel ) );
         
         send_message( ROTATED_DISPLAY );

      }

      // May have to do this in the Swing thread depending on whether it is
      // called from the swing thread or not.
      class drawCursor extends Thread
      {



         Point       pos;

         CoordJPanel Panel;


         public drawCursor( Point currentPosition, CoordJPanel pan )
         {

            pos = currentPosition;
            Panel = pan;
         }


         public void run()
         {

            Panel.set_crosshair( pos );
         }
      }


      /*
       * Causes a reset of the drag start position (non-Javadoc)
       * 
       * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
       */
      @Override
      public void mouseMoved( MouseEvent e )
      {

         Drag = null;

      }


      /*
       * If mode is PickMode, will see if the point corresponds to a peak, then
       * cause it to be the selected peak (non-Javadoc)
       * 
       * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
       */
      @Override
      public void mouseClicked( MouseEvent e )
      {

         if( mode == PickMode )
         {

            Point P = panel.getCurrent_pixel_point();

            id = panel.pickID( P.x , P.y , 13 );
            if( id != IThreeD_Object.INVALID_PICK_ID )
            {
               showSelectedPeak(id, false);
               send_message( SELECTED_PEAK_CHANGED );
            }
            
         }
         Drag = null;
         dragStarted = false;
      }


      /*
       * (non-Javadoc)
       * 
       * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
       */
      @Override
      public void mouseEntered( MouseEvent e )
      {

      }


      /*
       * Causes the drag start to be reset (non-Javadoc)
       * 
       * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
       */
      @Override
      public void mouseExited( MouseEvent e )
      {

         Drag = null;
         dragStarted = false;

      }


      /*
       * Causes the drag start to be reset and checks for a pressed right mouse
       * button (non-Javadoc)
       * 
       * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
       */
      @Override
      public void mousePressed( MouseEvent e )
      {

         Drag = null;
         if( e.getButton() == MouseEvent.BUTTON3 )
            
            panel.send_message( View3D.RIGHT_CLICKED );

         if( mode == RotateMode && e.getButton() == MouseEvent.BUTTON1 )
        
            dragStarted = true;

         
         else
            dragStarted = false;


      }


      /*
       * Resets drag start (non-Javadoc)
       * 
       * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
       */
      @Override
      public void mouseReleased( MouseEvent e )
      {

         Drag = null;
         dragStarted = false;
      }

   }


   /**
    * Creates the 3D objects initially
    * 
    * @param peaks
    *           The peaks to be displayed
    */
   private void CreateAdd3DObjects( Vector< IPeak > peaks )
   {

      int n = peaks.size();// Count those in form
      if( n < 1 )
         return;
      
      IThreeD_Object[] allObjects = new IThreeD_Object[ n ];
      
      String Name = "";
      int k = 0;
      IPeak PP = Peaks.elementAt( 0 );
      
      int run_num = PP.nrun();
      int det_num = PP.detnum();
      
      for( int i = 0 ; i < n ; i++ )
      {
         IPeak P = Peaks.elementAt( i );
         
         if( P.nrun() != run_num || P.detnum() != det_num )
            
            if( k > 0 )//New run num or detector number
               
            {         //Previous one had some information
               IThreeD_Object[] thisName = new IThreeD_Object[ k ];
               System.arraycopy( allObjects , 0 , thisName , 0 , k );
               
               Name = "R" + run_num + "D" + det_num;
               setObjects( Name , thisName );
               
               k = 0;
               run_num = P.nrun();
               det_num = P.detnum();

            }
       
         int size = 5 + (int) ( ( P.ipkobs() - MinIntensity )
                  / ( MaxIntensity - MinIntensity ) * 15 );
         
         Vector3D[] marks = new Vector3D[ 1 ];
         marks[ 0 ] = new Vector3D( P.getUnrotQ() );
         
         RoundBall pk = new RoundBall( marks[0] ,size, Color.blue );
         //pk.setSize( size );
         //pk.setType( Polymarker.STAR );
         
         pk.setPickID( P.seqnum() );
         allObjects[ k ] = pk;
         
         k++ ;

      }

      if( k > 0 )
      {
         IThreeD_Object[] thisName = new IThreeD_Object[ k ];
         System.arraycopy( allObjects , 0 , thisName , 0 , k );
         
         Name = "R" + run_num + "D" + det_num;
         setObjects( Name , thisName );
      }

      repaint();


   }


   // draws the axes
   private void drawAxes()
   {

      Polyline[] lines = new Polyline[ 3 ];
      Vector3D[] line = new Vector3D[ 2 ];
      line[ 0 ] = new Vector3D( 0 , 0 , 0 );
      line[ 1 ] = new Vector3D( MaxQ * .8f , 0 , 0 );
      lines[ 0 ] = new Polyline( line , BEAM_AXIS_COLOR);
      lines[ 0 ].setPickID( Peaks.size() + 10 );

      line = new Vector3D[ 2 ];
      line[ 0 ] = new Vector3D( 0 , 0 , 0 );
      line[ 1 ] = new Vector3D( 0 , MaxQ * .8f , 0 );
      lines[ 1 ] = new Polyline( line , THIRD_AXIS_COLOR);
      lines[ 1 ].setPickID( Peaks.size() + 11 );

      line = new Vector3D[ 2 ];
      line[ 0 ] = new Vector3D( 0 , 0 , 0 );
      line[ 1 ] = new Vector3D( 0 , 0 , MaxQ * .8f );
      lines[ 2 ] = new Polyline( line , UP_AXIS_COLOR );
      lines[ 2 ].setPickID( Peaks.size() + 12 );

      setObjects( "Axes" , lines );


   }


   private void SetViewing( Tran3D matrix )
   {

      if( matrix == null )
      {
         matrix = ( new Tran3D() );
         matrix.setIdentity();
      }

      setViewTran( matrix );
      currentTransformation = matrix;
   }


   /*
    * Test program. 
    * @param args      [0]-The peaks filename.
    */

   public static void main( String[] args )
   {
      String filename = null;
      String ImageFilePrefix = null;
      String ImageFileDirectory = null;
 /*     if( args == null || args.length < 1)
      {
         JFileChooser jfc = new JFileChooser( System.getProperty("Data_Directory"));
         if( jfc.showOpenDialog( null )==JFileChooser.APPROVE_OPTION)
            filename = jfc.getSelectedFile().toString();
      }else if( args[0].trim().startsWith( "-" ))
      {
         System.out.println("A. With no arguments, a file chooser dialog "+
                               "appears to select the peaks file\n");
                               		
         System.out.println("B. To see the peak images, run View3D on the"+
                  " command line with the ");
         System.out.println("    peaks filename , the prefix(eg. SNAP_) ");
         System.out.println(" then the directory where the image files are saved ");
         System.out.println("   if it is not in the ISAW/tmp directory in the user's "+
                  "home directory\n\n");
         System.out.println("   To keep the peak image files add to your "+
                  "IsawProps.dat file");
         System.out.println("-----KeepPeakImageFiles=true-----");
         System.out.println(" Then run the first form of SNS's initial peaks wizard");
         System.out.println("----------------------------------------------");
         System.out.println("\n\n") ; 
         System.exit(0);
         
      }else
      {
         filename = args[0];
         if( args.length >1)
            ImageFilePrefix = args[1];
         if( args.length >2)
            ImageFileDirectory = args[2];
         
      }
 */        
      System.out.println( "Enter Peaks filename" );
      // JFileChooser jf = new JFileChooser();
      filename = "C:\\ISAW\\SampleRuns\\SNS\\Snap\\QuartzRunsFixed\\quartz.peaks";
      ImageFilePrefix = "SNAP_";
     // String filename = "C:\\ISAW1\\SampleRuns\\INITIAL_WITH_CAL\\quartz.peaks";
      // String filename = "C:\\ISAW1\\anvred\\ox80nxs.integrate";
      //System.out.println( filename );
      /* if( jf.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION)
       {
          filename = jf.getSelectedFile().getAbsolutePath();
          
          
       }else
          System.exit( 0 );
       */
      Vector pks = (Vector) ( new DataSetTools.operator.Generic.TOF_SCD.ReadPeaks(
               filename ) ).getResult();

      View3D V = new View3D( pks );
      
      View3DControl vcontrol = new View3DControl( V , pks );
      
      Info inf = new Info( vcontrol );
      
      if( ImageFilePrefix != null)
      {
         PeakImageInfoHandler pkImage = new PeakImageInfoHandler( 
                                     ImageFileDirectory , ImageFilePrefix );
         inf.addInfoHandler( "Peak Image" , pkImage );
      }
      
      JPanel ControlPanel = new JPanel();
      BoxLayout bLayout = new BoxLayout( ControlPanel , BoxLayout.Y_AXIS );
      ControlPanel.setLayout( bLayout );
      
      OrientMatrixControl orient = new OrientMatrixControl( inf , V , pks ,
               vcontrol );
      ControlPanel.add( orient );
      
      SetPeaks peakSetter = new SetPeaks( V , pks );
      
      ControlPanel.add( new View3DItems( V , pks.size() , peakSetter ) );
      
      PeakFilterer pkFilt = new PeakFilterer( pks );
      ControlPanel.add( pkFilt );
      orient.setPeakFilterer( pkFilt );
      
      ControlPanel.add( peakSetter );
      orient.setPeakSelector( peakSetter );
      
      XtalLatticeControl LatControl = new XtalLatticeControl( peakSetter , inf );
      orient.setCrystalLatticeHandler( LatControl , true );
      
      ControlPanel.add( inf );
      
      JFrame jfr = new JFrame( "Test" );
      jfr.getContentPane().setLayout( new GridLayout( 1 , 1 ) );
      SplitPaneWithState splt = new SplitPaneWithState(
               JSplitPane.HORIZONTAL_SPLIT , V , ControlPanel , .7f );
      jfr.getContentPane().add( splt );
      jfr.setSize( 3000 , 2500 );
      jfr.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
      jfr.setVisible( true );


      splt.setDividerLocation( .8 );


   }

}
