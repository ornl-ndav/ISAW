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
 * Revision 1.12  2006/07/13 14:23:28  rmikk
 * Removed commented out code
 *
 * Revision 1.11  2006/07/10 21:48:02  dennis
 * Removed unused imports after refactoring to use New Parameter
 * GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.10  2006/07/10 16:26:14  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.9  2006/06/08 22:01:49  rmikk
 * Updated the links to conform with the changes in the scripts
 *
 * Revision 1.8  2006/06/08 19:36:02  rmikk
 * Fixed an error in the linking of form parameters
 *
 * Revision 1.7  2006/06/08 18:33:51  rmikk
 * Updated to correspond to the new scripts
 *
 * Revision 1.6  2006/06/06 19:42:02  rmikk
 * Replaced an operator form for indexing to a script form that calls that
 *   operator and saves the resultant file
 *
 * Revision 1.5  2006/01/16 04:54:53  rmikk
 * Added Constant arguments for the forms
 * Updated the argument link list for the new parameters added to the forms
 *
 * Revision 1.4  2006/01/15 02:18:58  rmikk
 * Replace the Blind form with a script to read in an orientation matrix
 *
 * Revision 1.3  2006/01/05 22:25:32  rmikk
 * Now uses the scripts from the scratch scripts directory.  These scripts 
 *   can be customized
 *
 * Revision 1.2  2005/08/05 20:25:08  rmikk
 * Fixed parameters, etc. so that they now run.  The Initial peaks wizard gives
 * correct answers.
 *
 * Revision 1.1  2004/07/14 16:31:48  rmikk
 * Initial Checkin
 * This is the initial peaks wizard that uses the Peak_new objects  so it can 
 * analyze detectors that are not vertical.
 *
 */

package Wizard.TOF_SCD;

import DataSetTools.wizard.*;
import gov.anl.ipns.Parameters.ArrayPG;
import gov.anl.ipns.Parameters.PlaceHolderPG;
import gov.anl.ipns.Parameters.StringPG;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * @author MikkelsonR
 *
 * This is the original initial peaks wizard that works with the Peaks_new 
 * Object.  It should be able to work with detectors that are not vertical.
 */
public class InitialPeaksWizard_SNS extends Wizard {

  public InitialPeaksWizard_SNS(  ) {
     this( true );
   }

   /**
    * Constructor for setting the standalone variable in Wizard.
    *
    * @param standalone Boolean indicating whether the Wizard stands alone
    *        (true) or is contained in something else (false).
    */
   public InitialPeaksWizard_SNS( boolean standalone ) {
     super( "Initial SCD Peaks Wizard", standalone );
    
     this.setHelpURL( "IPWN.html" );
     createForms();
   }
   
   private void createForms(){
     String path = System.getProperty("ISAW_HOME");
     path = path.replace('\\','/');
     if( !path.endsWith("/"))
       path +="/";
     path += "Wizard/TOF_SCD/Scripts_new/";
     addForm( new OperatorForm(
                new findCentroidedPeaks(),
                new PlaceHolderPG("Peaks", new Vector()),
                new int[0]));
     
     addForm( new ScriptForm(path+"Blind.iss",new ArrayPG("Orientation Matrix", null),
                       new int[]{0,8})); 
     
     addForm( new ScriptForm(path+"JIndex_Init1.iss", new StringPG("Result",""),
    		 new int[]{0,1,6} ));
     
     addForm( new ScriptForm(path +"Scalar.iss", new StringPG("Transformation from Scalar",""),
                new int[]{0,3}));
     addForm( new ScriptForm(path+"LsqrsInit.iss",
                 new ArrayPG("Orientation Matrix", new Vector()),
                 new int[]{0,5,10}));
     addForm( new ScriptForm(path+"JIndxSave.iss",
                    new StringPG("Result", ""),new int[]{0,1,6,7}));
     
     
     
      int[][] Xlate= { {22, 0, 0,-1, 0, 0}, //peaks vector
                       {-1, 9, 1, 0,-1,-1}, //init UB matrix
                       {-1,-1,-1, 6, 5,-1}, //Transformation
                       {-1,-1,-1,-1,12, 1},  //lsqrs UB matrix
                       {1 , 8 ,6, 3,10, 6},//path
                       {4 ,-1 ,7,-1, -1,7}//,expname
                                         // {14,6,-1,-1,-1,-1}  //Max dSpacing
                     
                     };
     linkFormParameters(Xlate);
   }
 
  /**
   * Method for running the Initial Peaks wizard as standalone.
   */
  public static void main( String[] args ) {
    InitialPeaksWizard_SNS w = new InitialPeaksWizard_SNS( true );
    w.wizardLoader( args );
  }
  
  
 public static JMenu CreateMenuItems( String MenuText ){
    if( MenuText == null)
       MenuText ="SNS SCD Wizards";
     JMenu Res = new JMenu(MenuText);
     JMenuItem initial = new JMenuItem("Initial Peaks Wizard");
     Res.add( initial );
     JMenuItem daily = new JMenuItem("Daily Peaks Wizard");
     Res.add( daily );
     initial.addActionListener( new InitialPeaksWizard_SNS.WizardMenuListener());
     daily.addActionListener( new InitialPeaksWizard_SNS.WizardMenuListener());
     
     return Res;
    
 }
 
 
 
 static class WizardMenuListener implements ActionListener{
    
    public void actionPerformed( ActionEvent evt){
       if( evt.getActionCommand().startsWith( "Initial" )){
          new InitialPeaksWizard_SNS( false ).wizardLoader( null );
          
       }else{
          new DailyPeaksWizard_SNS( false ).wizardLoader( null );
          
       }
    }
    
    
 }

}
