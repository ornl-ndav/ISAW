/*
 * File:  SetL2.java 
 *             
 * Copyright (C) 2004, Alok Chatterjee 
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2005/01/10 15:28:50  dennis
 * Removed unused imports.
 *
 * Revision 1.1  2004/10/12 22:20:37  chatterjee
 * Operator to set the L2 distance (sample to detector).
 * Sets the L2 value in the dataset instead of the Runfile.
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import java.util.Vector;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

public class SetL2  extends GenericTOF_SAD{

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *  Default Constructor
    */
    public SetL2()
    {
      super("Set L2");
    }

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *    Constructor for SetL2.
    *    @param  ds    DataSet containing the run file
    *    @param  L2    The new value of L2 to be set in the runfile
    */
   public SetL2(DataSet ds, float L2) 
     {
        
      this();
      parameters = new Vector();
      addParameter( new Parameter("Histogram with new L2", ds) );
      addParameter( new FloatPG("New L2 value", new Float(L2)));
    }


   /* ----------------------- setDefaultParameters  ------------------------ */

    public void setDefaultParameters()
    {
      parameters = new Vector();
      addParameter( new Parameter("Histogram with new L2",DataSet.EMPTY_DATA_SET) );
      addParameter( new FloatPG("L2 value",null));
     
    }

  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "SetL2", the command used to invoke this 
   *           operator in Scripts
   */
  	public String getCommand()
  	{
    	return "SetL2";
  	}

  /* ---------------------------- getResult ------------------------------- */
    /**  
		 * Calls the SetL2
     */
     public Object getResult()
     {

        DataSet ds         = (DataSet)(getParameter(0).getValue());
        float   L2    		 = ((Float)(getParameter(1).getValue())).floatValue();
        int grid_ids[]     = Grid_util.getAreaGridIDs( ds );
        UniformGrid Grid   = (UniformGrid)DataSetTools.dataset.Grid_util.getAreaGrid(ds, grid_ids[0] );
        Vector3D vec       = (Vector3D)Grid.position();
       // System.out.println("Grid old Center...."+vec.toString());
        float len          = (float)vec.length();   
        float ratio        = (float)L2/len;
        vec.multiply(ratio); 
        Grid.setCenter( vec);
        //System.out.println("Grid new Center...."+vec.toString()+','+"len = "+len+','+"ratio = "+ratio);
        Grid_util.setEffectivePositions(ds, grid_ids[0]); 
   
       // Data d = ds.getData_entry( 32896 );
       // DetectorPosition pos = d.getDetectorPosition();
        //System.out.println("After change pixel center position = " + pos );
        return ds;
    }



  /* ----------------------------- Main ---------------------------------- */
    /**
     *   Main program for testing purposes
     */
    public static void main(String[] args) 
    {

    }

}
