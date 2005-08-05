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

import DataSetTools.wizard.*;

import DataSetTools.parameter.*;
public class DailyPeaksWizard_new extends Wizard 
{
     int[][] ParamTable= {     {0,-1,-1,-1,0}
     ,{1,-1,4,4,1}
     ,{2,-1,2,5,2}
     ,{3,-1,1,9,3}
     ,{12,0,0,0,-1}
     ,{10,-1,-1,-1,8}
     ,{8,-1,-1,-1,5}
     ,{-1,2,-1,6,-1}
     ,{-1,-1,7,3,-1}
   

            };

     int[][]ConstList ={
        {}
        ,{}
        ,{}
        ,{}
        ,{}
     };
   public DailyPeaksWizard_new( ){
        this( false);
      };

   public DailyPeaksWizard_new(boolean standalone ){
        super("Daily Peaks Wizard",standalone);
     

   addForm( new ScriptForm("TOF_SCD/find_multiple_peaks1.iss", new PlaceHolderPG("Peaks",null),new int[0]));
   addForm( new ScriptForm("TOF_SCD/JIndxSave1.iss", new StringPG("Result1",null),new int[0]));
    addForm( new ScriptForm(  "TOF_SCD/LSqrs.iss",new ArrayPG( "Result",null ),ConstList[2]));
   addForm( new ScriptForm("TOF_SCD/JIndxSave2.iss"));
   addForm( new ScriptForm("TOF_SCD/integrate_multiple_runs.iss"));
      linkFormParameters( ParamTable );
   String S="" ;
    S+="This wizard does the following is the replacement for the";
    S+="initial Daily Peaks Wizard that assumes that the detector(s)";
    S+="are perpendicular to the beam plane and centered at the";
    S+="level of the sample";
    S+="  ";
    setHelpMessage( S);

   }

   public static void main( String[] args){
      DailyPeaksWizard_new Wiz= new DailyPeaksWizard_new(true);
    Wiz.wizardLoader( args );
   }

}
