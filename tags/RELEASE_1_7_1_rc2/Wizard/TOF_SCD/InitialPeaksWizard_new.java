/*
 * File:  InitialPeaksWizard_new.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * Revision 1.1  2004/07/14 16:31:48  rmikk
 * Initial Checkin
 * This is the initial peaks wizard that uses the Peak_new objects  so it can 
 * analyze detectors that are not vertical.
 *
 */

package Wizard.TOF_SCD;

import DataSetTools.wizard.*;
import java.util.*;
import DataSetTools.parameter.*;
import Operators.TOF_SCD.*;
/**
 * @author MikkelsonR
 *
 * This is the original initial peaks wizard that works with the Peaks_new 
 * Object.  It should be able to work with detectors that are not vertical.
 */
public class InitialPeaksWizard_new extends Wizard {

  public InitialPeaksWizard_new(  ) {
     this( true );
   }

   /**
    * Constructor for setting the standalone variable in Wizard.
    *
    * @param standalone Boolean indicating whether the Wizard stands alone
    *        (true) or is contained in something else (false).
    */
   public InitialPeaksWizard_new( boolean standalone ) {
     super( "Initial SCD Peaks Wizard", standalone );
    
     this.setHelpURL( "IPWN.html" );
     createForms();
   }
   
   private void createForms(){
     String path = System.getProperty("ISAW_HOME");
     path = path.replace('\\','/');
     if( !path.endsWith("/"))
       path +="/";
     path += "Scripts/";
     addForm( new ScriptForm(path+"TOF_SCD/find_multiple_peaks1.iss",
               new PlaceHolderPG("Peaks", new Vector())));
     addForm( new OperatorForm( new BlindJ_base(),
            new ArrayPG("Orientation Matrix", null),
            new int[]{0}));
     addForm( new OperatorForm( new IndexJ_base(),
                 new StringPG("Log info", ""),
                 new int[]{0,1}));
     addForm( new OperatorForm( new ScalarJ_base(),
                 new StringPG("Transformation", ""), new int[]{0}));
     addForm( new OperatorForm( new LsqrsJ_base(),
                 new ArrayPG("Orientation Matrix", new Vector()),
                 new int[]{0}));
     addForm( new ScriptForm(path+"TOF_SCD/JIndxSave.iss",
                    new StringPG("Result", "")));
    /* addForm( new OperatorForm( new IndexJ_base(),
                 new StringPG("Log info", "JIndxS"),
                 new int[]{0,1}));
     */            
      int[][] Xlate= { {12, 0, 0,-1, 0, 0}, //peaks vector
                       {-1, 3, 1, 0,-1,-1}, //init UB matrix
                       {-1,-1,-1, 3, 3,-1}, //Transformation
                       {-1,-1,-1,-1, 7, 1}  //lsqrs UB matrix
                     
                     };
     linkFormParameters(Xlate);
     
               
   }
 
  /**
   * Method for running the Initial Peaks wizard as standalone.
   */
  public static void main( String[] args ) {
    InitialPeaksWizard_new w = new InitialPeaksWizard_new( true );
    w.wizardLoader( args );
  }

}
