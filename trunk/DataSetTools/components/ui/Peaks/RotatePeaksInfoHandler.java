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



   public RotatePeaksInfoHandler()
   {

   }


   /**
    * Shows information on the transformation in a 3D viewer. Assumes the
    * transformation is orthonormal.
    */
   @Override
   public void show( IPeak pk , Tran3D transformation , JPanel panel )
   {

      if( panel == null )
         return;
      
      panel.removeAll();
      
      panel.setLayout( new GridLayout( 1 , 1 ) );
      
      if( transformation == null )
         return;

      JTextArea area = new JTextArea( 12 , 40 );

      float[][] aa = transformation.get();
      float[][] a = new float[ 3 ][ 3 ];
      
      for( int col = 0 ; col < 3 ; col++ )
      {
         float sav = aa[ 0 ][ col ];
         a[ 0 ][ col ] = aa[ 1 ][ col ];
         a[ 1 ][ col ] = aa[ 2 ][ col ];
         a[ 2 ][ col ] = sav;
      }
      
      float[] orient = DataSetTools.math.tof_calc.getEulerAngles( new Vector3D(
               a[ 0 ][ 0 ] , a[ 1 ][ 0 ] , a[ 2 ][ 0 ] ) , new Vector3D(
               a[ 0 ][ 1 ] , a[ 1 ][ 1 ] , a[ 2 ][ 0 ] ) );
      
      area.setText( "  -------Euler Angles--------    " );
      area.append( "\n   phi   " + orient[ 0 ] + " deg" );
      area.append( "\n   chi   " + orient[ 1 ] + " deg" );
      area.append( "\n   omega " + orient[ 2 ] + " deg" );
      area.append( "\n  ------------------------------" );

      // Needed for finer rotations, say rotating around a fixed line(vertical)

      area.append( "\n -------  up,right,out directions---" );
      Tran3D invT = new Tran3D( transformation );
      
      if( invT.invert() )
      {
         float[][] b = invT.get();
         
         area.append( "\n    up(y)   =( " + b[ 0 ][ 1 ] + "," + b[ 1 ][ 1 ]
                  + "," + b[ 2 ][ 1 ] + ")" );
         
         area.append( "\n    right(x)=( " + b[ 0 ][ 0 ] + "," + b[ 1 ][ 0 ]
                  + "," + b[ 2 ][ 0 ] + ")" );
         
         area.append( "\n    out(z)   =( " + b[ 0 ][ 2 ] + "," + b[ 1 ][ 2 ]
                  + "," + b[ 2 ][ 2 ] + ")" );
      }


      String text = "<html><body> <OL >Euler angles applied as follows:";
      text += "<LI> phi rotation about z axis <LI>chi rotation about x axis";
      text += "<LI> omega rotation about z axis</OL> All Rotations are in degrees ";
      text += "and positive rotation follows the right hand rule <P> Note that";
      text += "z is up and x is in the beam direction";

      area.setToolTipText( text );
      
      JScrollPane scr = new JScrollPane( area );
      panel.add( scr );
      
      panel.validate();
      panel.repaint();


   }

}
