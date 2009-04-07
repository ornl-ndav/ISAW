/*
 *File:  DailyPeaksWizard_SNS.java
 *
 * Copyright (C) Ruth Mikkelson 2008
 *
 * This program is free software; you can redistribute it and/or 
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 *Contact:Ruth Mikkelson,mikkelsonr@uwstout.edu
 *        Menomonie, WI 54751
 *
 * This work was supported by the the Spallation Neutron Source at Oak Ridge 
 * National Laboratory in Oak Ridge, Tennessee.
 * 
 *  For further information, see <http://www.pns.anl.gov/ISAW/>
 * 
 *
 *
 *
 * Modified:   Log:DailyPeaksWizard_SNS.java, v$
 *
 *
 */ 


package Wizard.TOF_SCD;

import java.util.Vector;

import gov.anl.ipns.Parameters.ArrayPG;
import gov.anl.ipns.Parameters.PlaceHolderPG;
import gov.anl.ipns.Parameters.StringPG;
import DataSetTools.wizard.*;

public class DailyPeaksWizard_SNS extends Wizard 
{
     int[][] ParamTable= {   
     {0,  -1,-1,-1, 0}  //PATH data
     ,{1,  6, 4, 4, 1}   //output path
     ,{2, -1, 2, 5, 2}   //Runnums
     ,{3, -1,-1,-1, 3}   //DataSet NUmbers
     ,{4,  7, 1, 8, 4}   //exp name
                         
     ,{28, 0, 0, 0,-1}   //peaks
     ,{23,-1,-1,-1,11} //instName
     ,{22,-1,-1,-1,12} //FileExt
     ,{11,-1,-1,-1, 7}  //calibFilename
     ,{12,-1,-1,-1, 8}    // line2 use
     ,{10,-1,-1,-1, 6}  //use Calib file
     ,{-1, 2,-1, 6,-1}  //RestrRuns ????
     ,{-1, 4,-1,-1,-1}  //Filename to save peaks to ??
   

            };

       public DailyPeaksWizard_SNS( ){
        this( false);
      };

   public DailyPeaksWizard_SNS(boolean standalone ){
        super("Daily Peaks Wizard",standalone);
     
     String path = System.getProperty("ISAW_HOME");
     path = path.replace('\\','/');
     if( !path.endsWith("/"))
        path +="/";
     path += "Wizard/TOF_SCD/Scripts_new/";

     addForm( new OperatorForm(
              new findCentroidedPeaks(),
              new PlaceHolderPG("Peaks", new Vector()),
              new int[0]));
     
     addForm( new ScriptForm(path+"JIndxSave1.iss", 
    		            new ArrayPG("Result1",null),new int[]{0,6,7}));
     
     addForm( new ScriptForm(  path+"LSqrs.iss",
    		            new StringPG( "Result",null ),new int[]{0,1,2,4}));
     
     addForm( new ScriptForm(path+"JIndxSave2.iss", 
    		            new PlaceHolderPG("Peaks",null),new int[]{0,4,5,6,8}));
     
     addForm( new OperatorForm(new IntegrateMultipleRuns(), 
    		            new PlaceHolderPG("Result",""),new int[]{0,1,2,3,4,6,7,8,11,12}));
     
    linkFormParameters( ParamTable );
    String S="" ;
    S+="This wizard conducts the reduction of the daily peak data from SCD ";
    S+=" instruments. It starts with a good orientation matrix, then repeatedly ";
    S+="indexes the peaks and refines the per run orientation matrices until a ";
    S+="sufficient number of peaks are indexed.  Then the peaks are integrated ";
    S+="  ";
    setHelpMessage( S);

   }

   public static void main( String[] args){
      
      DailyPeaksWizard_SNS Wiz= new DailyPeaksWizard_SNS(true);
    Wiz.wizardLoader( args );
   }

}
