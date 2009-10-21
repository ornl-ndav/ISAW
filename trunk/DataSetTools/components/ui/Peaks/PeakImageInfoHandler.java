/* 
 * File: PageImageInfoHandler.java
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
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import javax.swing.*;
import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.ViewTools.Panels.PeakArrayPanel.*;

import javax.swing.JPanel;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;


/**
 * This Handles the image display of data around the selected peak. These
 * images can be created using the the first form of InitialPeaksWizard_SNS or 
 * InitialPeaksWizard_SNS1. The option to save the image must be set and
 * the IsawProps.dat file has the key "KeepPeakImageFiles" is set ( currently
 * to anything) to "yes". These images should be  copied to another folder
 * because the temp folder's contents are quite often erased.
 * 
 * @author Ruth
 * 
 */
public class PeakImageInfoHandler implements InfoHandler
{



   String fileNamePrefix;

   String Directory;


   public void kill()
   {
      fileNamePrefix =Directory = null;
   }
   /**
    * Constructor
    * 
    * @param Directory
    *           Directory Name where the peak images are stored. If null the 
    *           ISAW/tmp directory in the user's home directory will be used
    *           
    * @param fileNamePrefix
    *           The prefix for the names of these image files NOTE: The files
    *           are assumed to have the extension "pvw"
    */
   public PeakImageInfoHandler( String Directory, String fileNamePrefix )
   {

      this.fileNamePrefix = fileNamePrefix;
      this.Directory = Directory;
      
      if( Directory == null )
         this.Directory = FileIO.appendPath( System.getProperty( "user.home" ) ,
                  "ISAW" + File.separatorChar + "tmp" + File.separatorChar );
   }


   /**
    * Will show the image view of data around the given peak in the panel
    * 
    * @param pk
    *           the peak for which the image will be shown
    * @param transformation
    *           Not used here
    * @param panel
    *           The panel where the image is to be displayed
    */
   public void show( IPeak pk , Tran3D transformation , JPanel panel )
   {

     
      panel.removeAll();
      if( pk == null )
         return;

      String filename = fileNamePrefix + pk.nrun() + "_"
               + String.format( "%4d" , pk.detnum() ).replace( ' ' , '0' )
               + ".pvw";

      try
      {
         filename = Directory + filename;
         File F = new File( filename );
         FileInputStream fin = new FileInputStream( F );
         
         ObjectInputStream inp = new ObjectInputStream( fin );

         PeakDisplayInfo[] disp = (PeakDisplayInfo[]) inp.readObject();

         String name = ": " + (int) pk.x() + ", " + (int) pk.y() + ", "
                  + (int) pk.z();
         
         if( disp == null || disp.length < 1 )
            return;
         
         int k = - 1;
         for( int i = 0 ; i < disp.length && k < 0 ; i++ )
         {
            if( disp[ i ].getName().endsWith( name ) )
               k = i;
         }
         
         if( k < 0 )
            return;

         PeakDisplayInfo[] D = new PeakDisplayInfo[ 1 ];
         
         D[ 0 ] = disp[ k ];
         
         panel.setLayout( new GridLayout( 1 , 1 ) );

         PeaksDisplayPanel PP = new PeaksDisplayPanel( D );
         
         panel.add( PP );

         PP.invalidate();
         PP.repaint();
         panel.invalidate();
         panel.repaint();


      }
      catch( Exception s )
      {
         System.out.println( "Cannot Find file " + filename
                  + " in ISAW/tmp sudirectory of the user's home directory " );
      }


   }

}
