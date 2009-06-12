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

import java.awt.GridLayout;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;


/**
 * Displays information about the transformation of the 3D view to display the
 * information in a 2D window
 * 
 * @author Ruth
 * 
 */
public class RotatePeaksInfoHandler implements InfoHandler
{

   public static float[][] rot90Beam={ {1f,0f,0f,0f},
                                       {0f,0f,-1f,0f},
                                       {0f,1f,0f,0f},
                                       {0f,0f,0f,1f}};
   

   public RotatePeaksInfoHandler()
   {

   }


   /**
    * Shows information on the transformation in a 3D viewer. Assumes the
    * transformation is orthonormal.
    * @param  pk  the selected peak
    * @param   transformation  the current transformation to coordJPanel
    * @param   panel   the panel where the information will be displayed
    * 
    * Algorithm:
    *   -The original orientation has coord 3 as up and coord 0 as right.
    *   -This corresponds to up is z(IPNS) and beam is x(IPNS) convention
    *   -therefore: original transformation is T=[[1,0,0],[0,0,1],[0,-1,0]]
    *   -To show information relative to the new starting position
    *      and starting vectors(right,up,plane) i=(1,0,0),j=(0,0,-1) and
    *      k= (0,1,0).  XT = total transformation from standard start
    *      use XT*T^(-1) times i and j and k to get where original ones went
    *      use X times i and j  to get Euler angles( slight problems
    *      the code needs to start from i,j,k= identity--> mult X*inv(i:j:k)
    *      and use 1,0,0 and 0,1,0 to get the two vectors for Euler angles
    */
   @Override
   public void show( IPeak pk , Tran3D transformation , JPanel panel )
   {

      if( panel == null )
         return;
      Vector3D v1,v2;
      panel.removeAll();
      
      panel.setLayout( new GridLayout( 1 , 1 ) );
      
      if( transformation == null )
         return;

      JTextArea area = new JTextArea( 12 , 40 );
      
      float[][] bb  = transformation.get();
      float[][]aa = LinearAlgebra.mult(  bb,rot90Beam  );//started with up=z
      bb = LinearAlgebra.mult( bb ,rot90Beam  );
     // LinearAlgebra.print( transformation.get()  );
      //LinearAlgebra.print( aa );
      //System.out.println("-----------------------------");
      //Vector3D v1 = new Vector3D(aa[ 0 ][ 0 ]  , aa[ 2 ][ 0] , aa[ 1 ][ 0 ]);
     // Vector3D v2 = new Vector3D(aa[ 0 ][ 1 ]  , aa[ 2 ][ 1 ], aa[ 1 ][ 1 ]);
      
      //orient = DataSetTools.math.tof_calc.getEulerAngles(v1,v2 );
      float[]  orient = getEulerAng( transformation.get());
      area.setText( "  -------Euler Angles from original to current position in view---    " );
      area.append( "\n   phi   " + orient[ 0 ] + " deg" );
      area.append( "\n   chi   " + orient[ 1 ] + " deg" );
      area.append( "\n   omega " + orient[ 2 ] + " deg" );
      area.append( "\n  ------------------------------" );

      // Needed for finer rotations, say rotating around a fixed line(vertical)

      area.append( "\n -------  Vert,beam,back(orig) in view---" );
      //Tran3D invT = new Tran3D( transformation );
      
      //if( invT.invert() )
      {
         float[][] b = transformation.get() ;//LinearAlgebra.getInverse( bb   );
         
         area.append( "\n    Vert   =( " + b[ 0 ][ 2 ] + "," + b[ 1 ][ 2 ]
                  + "," + b[ 2 ][ 2] + ")= new (right, up, front of plane)" );

         area.append( "\n    beam=( " + b[ 0 ][ 0 ] + "," + b[ 1 ][ 0 ]
                  + "," + b[ 2 ][ 0 ] + ")" );
         
         area.append( "\n    back   =( " + b[ 0 ][ 1 ] + "," + b[ 1 ][ 1 ]
                  + "," + b[ 2 ][ 1 ] + ")" );
         
   
      }

      
      String text = "<html><body> <OL >Euler angles applied as follows:";
      text += "<LI> phi rotation about the vertical direction ";
      text += "<LI>chi rotation about the original beam direction";
      text += "<LI> omega rotation about the vertical direction</OL> All Rotations are in degrees ";
      text += "and positive rotation follows the right hand rule  <P> ";
      text += "  Vertical then beam follow the right hand rule"   ;
      text += "  Note: in View right,up, front of plane form a right hand system";

      area.setToolTipText( text );
      
      if( pk != null)
      {
         area.append( "\n\n-------- Selected Peak Info------\n" );
         float[] Qs = pk.getUnrotQ();
         v2 = new Vector3D();
         transformation.apply_to( new Vector3D(Qs) , v2 );
         
         area.append( "position = ("+ v2.getX()+","+v2.getY()+ 
                  ","+v2.getZ()+") = (right, up ,front of plane\n\n");
         area.append( "   keep fixed to rotate around this selected peak\n\n" );
         
         float length = v2.length();
         area.append( "Angle with current right direction " +
                 Math.acos( v2.getX()/length)*180/Math.PI+" deg" );
         area.append( "\nAngle with current up direction "+
                  Math.acos( v2.getY()/length)*180/Math.PI+" deg" ); 
         area.append( "\nAngle with current out of plane direction "+
                  Math.acos( v2.getZ()/length)*180/Math.PI+" deg" ); 
       
         
      }
    
      JScrollPane scr = new JScrollPane( area );
      panel.add( scr );
      
      panel.validate();
      panel.repaint();


   }
   //Takes into account if vx and/or vy =0 assume magnitude of whole vector is 1
   
   private static float  Atan( float vy, float vx)
   {
      if( Math.abs( vx )< Math.pow( 10 , -1 ))
         vx =0;
      if( Math.abs( vy )< Math.pow( 10 , -1 ))
         vy =0;
      if( vx ==0 && vy ==0)
         return 0;
      if( vy==0)
         if( vx >0)
            return 0;
         else
            return (float)Math.PI;
      if( vx ==0)
         if( vy >0)
            return (float)Math.PI/2;
         else 
            return -(float)Math.PI/2;
      return (float)Math.atan2( vy,vx );
         
   }
   //angle to rotate x axis to get to (vy,vx)
   private static float  Acos( float vy, float vx)
   {
      
      
      
      return 0;
   }
   /**
    * Finds the Euler angle giving phi-rotation about vertical, chi-
    *   rotation about beam then omega-rotation about vertical. 
    *   
    * @param aa  The transformation matrix from tuple (beam, up, back) to
    *     new (beam, up, back).NOTE up to bean right hand so other must be
    *     back  (beam, up, back) vector is column vector
    *     
    * @return  The phi, chi, and omega rotations in degrees that is needed
    *     to transform original (up,beam, back) to this current position
    *     
    */
   private static float[] getEulerAng( float[][] aa)
   {
      float[] bm = LinearAlgebra.mult( aa , new float[]{1f,0f,0f,0f} );
      float[] vt = LinearAlgebra.mult( aa , new float[]{0f,0f,1f,0f} );
      //float[] oth =LinearAlgebra.mult( aa , new float[]{0f,1f,0f,0f });
      float phi = -Atan(   vt[0],-vt[2] );
      float chi = -(float)Atan( -vt[2],vt[1]);
      double cosT = Math.cos( phi );
      double sinT = Math.sin(phi);
      float a =(float)(vt[0]*cosT-sinT*vt[2]);
      float b =(float)(vt[0]*sinT+cosT*vt[2]);
     //  System.out.println("vt-->("+ a+
     //             ","+vt[1]+","+b +")=ang"+ (phi/Math.PI*180));
       vt[0]=a; vt[2]=b;
       a=(float)(bm[0]*cosT-sinT*bm[2]);
       b =(float)(bm[0]*sinT+cosT*bm[2]);
       bm[0]=a; bm[2]=b;
       //System.out.println("new ang,vt,bm="+phi/Math.PI*180);
       //LinearAlgebra.print(vt);
      // LinearAlgebra.print( bm );
       cosT = Math.cos( chi );
       sinT = Math.sin(chi);
       a=(float)( vt[2]*cosT-vt[1]*sinT);
       b =(float)(vt[2]*sinT+vt[1]*(cosT));
       vt[2]=a;
       vt[1] =b;
       a =(float)( bm[2]*cosT-bm[1]*sinT);;
       b  =(float)(bm[2]*sinT+bm[1]*(cosT));
       bm[2]=a;
       bm[1] =b;
       //System.out.println("vt-->("+vt[0]+","+b+","+a+")ang"+ (chi/Math.PI*180));
    
       //System.out.println("ang,vt,bm="+chi/Math.PI*180);
      // LinearAlgebra.print(vt);
       //LinearAlgebra.print( bm );
       float omega = -Atan(  -bm[2],bm[0]);

       cosT = Math.cos( omega );
       sinT = Math.sin(omega);
       a =(float)(vt[0]*cosT-sinT*vt[2]);
       b =(float)(vt[0]*sinT+cosT*vt[2]);
       // System.out.println("vt-->("+ a+
       //            ","+vt[1]+","+b +")ang"+ (omega/Math.PI*180));
        vt[0]=a; vt[2]=b;
        a =(float)(bm[0]*cosT-sinT*bm[2]);
        b  =(float)(bm[0]*sinT+cosT*bm[2]);
        //System.out.println("bm-->("+ bm[0]+
       //          ","+bm[1]+","+bm[2] +")");
        bm[0]=a; bm[2]=b;
        //System.out.println("ang,vt,bm="+omega*180/Math.PI);
       // LinearAlgebra.print(vt);
        //LinearAlgebra.print( bm );
       float[] Res = new float[3];
       float scale =(float)( 180/Math.PI);
       Res[0] = -omega*scale;
       Res[1]= -chi*scale;
       Res[2] = -phi*scale;
       return Res;
   }
  public static void main( String args[])
  {              //r,  bak vt
     float[][]  a={{1f,.707f,0f,0f},   //rt
                   {0f,0f,.7070f,0f},   //up
                   {0f,0f,.707f,0f},  //frnt
                   {0f,0f,0f,1f}};
     
     float[] Res = RotatePeaksInfoHandler.getEulerAng( a);
     System.out.println("phi,chi,omega="+Res[0]+","+Res[1]+","+
               Res[2]);
  }
}
