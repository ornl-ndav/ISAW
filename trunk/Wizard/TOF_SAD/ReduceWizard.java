/*
 * File:  ReduceWizard.java
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonR@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.7  2006/07/10 22:10:21  dennis
 * Removed unused imports after refactoring to use new Parameter GUIs
 * in gov.anl.ipns.Parameters.
 *
 * Revision 1.6  2006/07/10 16:26:13  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.5  2004/01/30 02:46:52  bouzekc
 * Removed unused variable LOAD_FILE_TYPE.
 *
 * Revision 1.4  2004/01/05 23:44:13  rmikk
 * Last form now shows resultant data sets
 *
 * Revision 1.3  2004/01/05 15:24:22  bouzekc
 * Removed unused imports.
 *
 * Revision 1.2  2003/11/13 18:23:33  rmikk
 * -Added Hook to get Wizard documentation
 *
 * Revision 1.1  2003/11/11 20:47:48  rmikk
 * Initial Checkin
 *
 */
package Wizard.TOF_SAD;

import DataSetTools.util.*;

import DataSetTools.wizard.*;

import gov.anl.ipns.Parameters.ArrayPG;

import java.io.*;

/**
 * This class constructs a Wizard used for initially finding peaks.  In this
 * Wizard, BlindJ is used for creating a matrix file.
 */
public class ReduceWizard extends Wizard {
  //~ Constructors *************************************************************

  /**
   * Default constructor.  Sets standalone in Wizard to true.
   */
  public ReduceWizard(  ) {
    this( true );
  }

  /**
   * Constructor for setting the standalone variable in Wizard.
   *
   * @param standalone Boolean indicating whether the Wizard stands alone
   *        (true) or is contained in something else (false).
   */
  public ReduceWizard( boolean standalone ) {
    super( "SAD Reduce Peaks Wizard", standalone );
    this.createAllForms(  );

    StringBuffer s = new StringBuffer(  );

    s.append( "This Wizard is designed for the Reduce operation\n" );
    s.append( "for SAD instruments");
    this.setHelpMessage( s.toString(  ) );
  }

  //~ Methods ******************************************************************

  /**
   * Method for running the Initial Peaks wizard as standalone.
   */
  public static void main( String[] args ) {
    ReduceWizard w = new ReduceWizard( true );
    w.wizardLoader( args );
  }

  /**
   * Adds and coordinates the necessary Forms for this Wizard.
   */
  private void createAllForms(  ) {
     int[][] fpi = {
       { 2,-1,-1,3},{4,-1,-1,0},  
       { -1,11,-1,1 },{-1,-1,7,4},{-1,-1,8,5},
       {-1,-1,9,2}
     };  //matrix
     int[] consts = {0,1,2,3,4,5};
     
     
      
     String Path = SharedData.getProperty("ISAW_HOME");
     Path = Path +"/Wizard/TOF_SAD/";
     if( Path == null){
        SharedData.addmsg("No ISAW_HOME in the System Properties");
        return;
     }
     if( !(new File(FilenameUtil.setForwardSlash( Path+"Reduce_Form1.py"))).exists()){
        SharedData.addmsg( "Cannot find the form files in "+ Path);
        return;
     }
     this.addForm( new JyScriptForm( Path+"Reduce_Form1.py",new ArrayPG("Calib Data",null)));
       
     this.addForm( new JyScriptForm( Path+"Reduce_Form2.py",new ArrayPG("DataSets",null )));
     
     this.addForm( new JyScriptForm( Path+"Reduce_Form3.py",new ArrayPG("Q bins",null )));
     
     this.addForm( new JyScriptForm( Path+"ReducePy.py",new ArrayPG("DataSet Result",null),consts ));
     
     setHelpURL( "Reduce.html");
 
     super.linkFormParameters( fpi );
  }
}
