/* 
 * File: getNewPeak_xyz.java
 *  
 * Copyright (C) 2007     Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson<Mikkelsonr@uwstout.edu>
 *            Department of Mathematics, Statistics and Computer Science
 *            Menomonie, WI 54751
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 *  Last Modified:
 * 
 *  $Author: eu7 $
 *  $Date: 2008-04-08 16:31:08 -0500 (Tue, 08 Apr 2008) $            
 *  $Revision: 19031 $
 *
 * 
 * Modified:
 *
 * $Log$
 * Revision 1.2  2007/08/23 21:05:03  dennis
 * Removed unused imports.
 *
 * Revision 1.1  2007/02/22 15:15:37  rmikk
 * Operator wrapper around DataSetTools.operaotr.Generic.TOF_SCD.
 *         Peaks_new.getNewPeak_xyz
 *
 *
 */

package DataSetTools.operator.Generic.TOF_SCD;
import DataSetTools.dataset.AttrUtil;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.Grid_util;
import DataSetTools.dataset.IDataGrid;
import DataSetTools.dataset.XScale;
import DataSetTools.instruments.SampleOrientation;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;

/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class getNewPeak_xyz extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public getNewPeak_xyz(){
     super("getNewPeak_xyz");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "getNewPeak_xyz";
   }

 
   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet",DataSetTools.dataset.DataSet.EMPTY_DATA_SET));
      addParameter( new IntegerPG("Grid ID",1));
      addParameter( new FloatPG("col of peak",1));
      addParameter( new FloatPG("row of peak",1));
      addParameter( new FloatPG("time channel of peak",1));
      addParameter( new IntegerPG("Sequence Number",1));
   }


   /**
    * Writes a string for the documentation of the operator provided by
    * the user.
    *
	 * @return  The documentation for the operator.
    */
   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("This operator creates a  Peaks_new object from a Data Set, detector ID and the row, col,");
      S.append(" and time channel of the peak");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The DataSet  has area detectors of some size");
      S.append("@param   ");
      S.append("The DataSet with the peak");
      S.append("@param   ");
      S.append("The detector ID with the peak");
      S.append("@param   ");
      S.append("the column of the peak in the detector starting at 1");
      S.append("@param   ");
      S.append("the row with the peak in the detector starting at 1");
      S.append("@param   ");
      S.append("the time channel for the peak in the detector starting at 0");
      S.append("@return   ");
      S.append("A Peak_new object which is also a Peak object");
      S.append("@error ");
      S.append("Data set is null");
      S.append(" Improper Detector is");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "HIDDENOPERATOR"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
      try{

         DataSetTools.dataset.DataSet DS = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         int ID = ((IntegerPG)(getParameter(1))).getintValue();
         float x = ((FloatPG)(getParameter(2))).getfloatValue();
         float y = ((FloatPG)(getParameter(3))).getfloatValue();
         float z = ((FloatPG)(getParameter(4))).getfloatValue();
         int seqNum =(( IntegerPG)(getParameter(5))).getintValue();
         
         int[] run_nums = AttrUtil.getRunNumber( DS );
         IDataGrid grid = Grid_util.getAreaGrid( DS, ID );
         SampleOrientation orientation = AttrUtil.getSampleOrientation( DS );
         Data data = grid.getData_entry( (int)y, (int)x );
         XScale xscl = data.getX_scale();
         float initial_path = AttrUtil.getInitialPath( DS );
         float t_zero = AttrUtil.getT0Shift( DS );
         Peak_new Xres = new Peak_new( run_nums[0],
                                       0,
                                       x,y,z,
                                       grid,
                                       orientation,
                                       xscl.getInterpolatedX( z ),
                                       initial_path,
                                       t_zero
                                       );
         Xres.seqnum( seqNum );
         Xres.setFacility( AttrUtil.getFacilityName( DS ) );
         Xres.setInstrument( AttrUtil.getInstrumentName( DS ) );

         return Xres;
       }catch(java.lang.IllegalArgumentException S0){
         return new ErrorString(S0.getMessage());
      }catch( Throwable XXX){
        String[]Except = ScriptUtil.
            GetExceptionStackInfo(XXX,true,1);
        String mess="";
        if(Except == null) Except = new String[0];
        for( int i =0; i< Except.length; i++)
           mess += Except[i]+"\r\n            "; 
        return new ErrorString( XXX.toString()+":"
             +mess);
                }
   }
}



