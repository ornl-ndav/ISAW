/* 
 * File: SetPeakInfoHandler.java
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

import javax.swing.*;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;


/**
 * Used to display text information about a newly selected peak
 * 
 * @author Ruth
 * 
 */
public class SelPeakInfoHandler implements InfoHandler
{



   public SelPeakInfoHandler()
   {

   }


   /**
    * Shows information about the selected peak. This includes run number,
    * detector number, sequence number, row, col, time, channel, wl, etc.
    */
   public void show( IPeak pk , Tran3D transformation , JPanel panel )
   {

      if( panel == null )
         return;

      panel.removeAll();
      if( pk == null )
         return;
     
      panel.setLayout( new GridLayout( 1 , 1 ) );
      
      JTextArea Information = new JTextArea( 11 , 30 );
      
      Information.setFont( gov.anl.ipns.ViewTools.UI.FontUtil.MONO_FONT );
      
      JScrollPane scr = new JScrollPane( Information );
      
      Information.setText( "  -Seq Num    " + pk.seqnum() );
      Information.append( " \n  -Run Num    " + pk.nrun() );
      Information.append( " \n  -Det Num    " + pk.detnum() );
      Information.append( " \n  -Intensity  " + pk.ipkobs( ) );
      
      
      Information.append( " \n  -Row        " + pk.y() );
      Information.append( " \n  -Col        " + pk.x() );
      Information.append( " \n  -Channel    " + pk.z() );
      Information.append( " \n  -wl         " + pk.wl() );
      
      float[] Q = pk.getUnrotQ();
      if( Q != null )
      {
         Information.append( "\n  -d-spacing  " + 1
                  / ( new Vector3D( Q ) ).length() );
      }
      Information.append( " \n  -Time       " + pk.time() );
      Information.append( " \n  -h          " + pk.h() );
      Information.append( " \n  -k          " + pk.k() );
      Information.append( " \n  -l          " + pk.l() );
      
      Information.append( " \n  -qx          " + Q[ 0 ] );
      Information.append( " \n  -qy          " + Q[ 1 ] );
      Information.append( " \n  -qz          " + Q[ 2 ] );
      
      panel.add( scr );
      
      panel.validate();
      panel.repaint();
   }

}
