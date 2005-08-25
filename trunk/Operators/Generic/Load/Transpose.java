/* 
 * File: Transpose.java
 *  
 * Copyright (C) 2005     Ruth Mikkelson
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
 *            University of Wisconsin-Stout
 *            Menomonie, WI 54751, USA
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
 * Revision 1.1  2005/08/25 16:42:14  rmikk
 * Initial Checkin for operator Generated operators
 *
 *
 */

package Operators.Generic.Load;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.parameter.*;

import gov.anl.ipns.Util.SpecialStrings.*;

import Command.*;
public class Transpose extends GenericOperator{
   public Transpose(){
     super("Transpose");
     }

   public String getCommand(){
      return "Transpose";
   }

   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new DataSetPG("DataSet to transpose",null));
      addParameter( new PlaceHolderPG("Dimension reorder",new int[]{3,2,1,0}));
      addParameter( new IntegerPG("# of rows",-1));
      addParameter( new IntegerPG("# of cols",-1));
      addParameter( new IntegerPG("# of detectors",-1));
      addParameter( new PlaceHolderPG("Time Scale",null));
      addParameter( new BooleanPG("Is timeScale histogram",true));
   }

   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Will transpose dimensions as specified in the Xlate array");
      S.append("@algorithm    "); 
      S.append("Fixed_dataset.getData_entry(row,col).getY_values(time)=");
      S.append("oldDataSet(w).getY_values()[u]  where<br>");
      S.append("w=pos/nys  and u= pos%nys.  nys = oldDataSet(0).getY_values().length.");
      S.append("pos is the pos in the original data block.  pos= detNum*mult_det+col*mult_col+row*mult_row+");
      S.append("time*mult_time.  The mult's are determined by the Xlate, ndet, nrows, ncols, and the number of");
      S.append("bins in the XScale.");
      S.append("");
      S.append("@assumptions    "); 
      S.append("The current data set has consecutive values of a block of (NeXus) data in consecutive");
      S.append("data spectra.");
      S.append("@param   ");
      S.append("Data Set that must be transposed");
      S.append("@param   ");
      S.append("A translation array. [detector,col,row,time]=[3,2,1,0] is natural");
      S.append("ISAW dimension order. This array should contain corresponding dimension position");
      S.append("(a la C starting at 0) in the NeXus file with the property given above.");
      S.append("i.e. if the time axis/dimension is the 2nd array position(a la C starting at 0) in the NeXus file,");
      S.append("then the last entry of of Xlate should be 2.If there is only one detector, then the first entry");
      S.append("of Xlate should be 3");
      S.append("@param   ");
      S.append("number of rows in all detectors in Nexus NXdata corresponding to this data set");
      S.append("@param   ");
      S.append("the number of columns in all detectors(must be in separate NXdata if they are not all the same)");
      S.append("@param   ");
      S.append("the number of detectors in this data set.  This is very likely 1, unless several LPSD's are placed in");
      S.append("one NXdata");
      S.append("@param   ");
      S.append("If the time dimension is not the first dimension, the time XScale is needed");
      S.append("@param   ");
      S.append("true If Tscale represent bin boundaries for histogrammed data. Otherwise it is false");
      S.append("@return A dataset with the data with the data blocks in the proper order and the proper length so that ");
      S.append("");
      S.append("they can easily be assigned to data grids.");
      S.append("@error ");
      S.append("");
      return S.toString();
   }


   public String[] getCategoryList(){
            return new String[]{
                     "Macros",
                     "DataSet",
                     "Tweak",
                     "LANSCE"
                     };
   }


   public Object getResult(){
      try{

         DataSetTools.dataset.DataSet DS = (DataSetTools.dataset.DataSet)(getParameter(0).getValue());
         int[] Xlate = (int[])(getParameter(1).getValue());
         int nrows = ((IntegerPG)(getParameter(2))).getintValue();
         int ncols = ((IntegerPG)(getParameter(3))).getintValue();
         int ndet = ((IntegerPG)(getParameter(4))).getintValue();
         DataSetTools.dataset.XScale Tscale = (DataSetTools.dataset.XScale)(getParameter(5).getValue());
         boolean isHistogram = ((BooleanPG)(getParameter(6))).getbooleanValue();
         java.lang.Object Xres=Operators.Generic.Load.LoadUtil.Transpose(DS,Xlate,nrows,ncols,ndet,Tscale,isHistogram);
         return Xres;
       }catch( Throwable XXX){
         return new ErrorString( XXX.toString()+":"
             +ScriptUtil.GetExceptionStackInfo(XXX,true,1)[0]);
      }
   }

}


