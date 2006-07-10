/*
 *File:  DailyPeaksWizard_new.java
 *
 * Copyright (C) Ruth Mikkelson
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
 *This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 * *
 * * For further information, see <http://www.pns.anl.gov/ISAW/>
 * *
 *
 *
 *
 * Modified:   Log:DailyPeaksWizard_new.java, v$
 *
 *
 */ 


package Wizard.TOF_SCD;

import gov.anl.ipns.Parameters.ArrayPG;
import gov.anl.ipns.Parameters.PlaceHolderPG;
import gov.anl.ipns.Parameters.StringPG;
import DataSetTools.wizard.*;

import DataSetTools.parameter.*;
public class DailyPeaksWizard_new extends Wizard 
{
     int[][] ParamTable= {   
     {0,  -1,-1,-1, 0}  //PATH data
     ,{1,  6, 4, 4, 1}   //output path
     ,{2, -1, 2, 5, 2}   //Runnums
     ,{3,  7, 1, 8, 3}   //exp name
     ,{13, 0, 0, 0,-1}   //peaks
     ,{11,-1,-1,-1, 9} //instName
     ,{12,-1,-1,-1,10} //FileExt
     ,{9, -1,-1,-1, 6}  //calibFilename
     ,{8, -1,-1,-1, 5}  //use Calib file
     ,{-1, 2,-1, 6,-1}  //RestrRuns ????
     ,{-1, 4,-1,-1,-1}  //Filename to save peaks to ??
   

            };

       public DailyPeaksWizard_new( ){
        this( false);
      };

   public DailyPeaksWizard_new(boolean standalone ){
        super("Daily Peaks Wizard",standalone);
     
     String path = System.getProperty("ISAW_HOME");
     path = path.replace('\\','/');
     if( !path.endsWith("/"))
        path +="/";
     path += "Wizard/TOF_SCD/Scripts_new/";

     addForm( new ScriptForm(path+"find_multiple_peaks1.iss", 
    		            new PlaceHolderPG("Peaks",null),new int[0])); 
     
     addForm( new ScriptForm(path+"JIndxSave1.iss", 
    		            new ArrayPG("Result1",null),new int[]{0,6,7}));
     
     addForm( new ScriptForm(  path+"LSqrs.iss",
    		            new StringPG( "Result",null ),new int[]{0,1,2,4}));
     
     addForm( new ScriptForm(path+"JIndxSave2.iss", 
    		            new PlaceHolderPG("Peaks",null),new int[]{0,4,5,6,8}));
     
     addForm( new ScriptForm(path+"integrate_multiple_runs.iss", 
    		            new StringPG("Result",""),new int[]{0,1,2,3,5,6,9,10}));
     
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
      
      DailyPeaksWizard_new Wiz= new DailyPeaksWizard_new(true);
    Wiz.wizardLoader( args );
   }

}
