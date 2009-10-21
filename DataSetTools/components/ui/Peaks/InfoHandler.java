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

import javax.swing.JPanel;

import gov.anl.ipns.MathTools.Geometry.Tran3D;
import DataSetTools.operator.Generic.TOF_SCD.IPeak;


/**
 * 
 * InfoHandler's are used to display information that may refer to a Peak or a
 * transformation.
 * 
 * @author Ruth
 * 
 */
public interface InfoHandler
{



   /**
    * The information to be displayed must show up in the JPanel, panel. The
    * peak and transformation may be used to represent the proper information
    * 
    * @param pk
    *           The current selected peak in the 3D View of Q space
    * 
    * @param transformation
    *           The current transformation in the 3D View of Q space
    * 
    * @param panel
    *           The JPanel where the information is to be displayed
    */
   public void show( IPeak pk , Tran3D transformation , JPanel panel );
   
   public void kill();
}
