/*
 * File:  SWV.java 
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
 * Revision 1.1  2004/08/11 18:30:09  chatterjee
 * Operator to invoke the SANDWedgeViewer.
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import gov.anl.ipns.Util.Numeric.*;

import java.util.Vector;

import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.parameter.*;
import DataSetTools.components.View.*;
import DataSetTools.operator.DataSet.Attribute.*;
import gov.anl.ipns.Util.Sys.WindowShower;


public class SWV  extends GenericTOF_SAD{

   String filename;

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *  Default Constructor
    */
    public SWV()
    {
      super("Sand Wedge Viewer");
    }

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *    Constructor for SWV.
    *    @param  filename   the name of the file
    */
   public SWV(String filename) 
     {
        
      this();
      parameters = new Vector();
      addParameter( new LoadFilePG( "Enter Filename", filename));
    }


   /* ----------------------- setDefaultParameters  ------------------------ */

    public void setDefaultParameters()
    {
      parameters = new Vector();
      addParameter( new LoadFilePG( "Enter Filename", null));
     
    }

  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "SWV", the command used to invoke this 
   *           operator in Scripts
   */
  	public String getCommand()
  	{
    	return "SWV";
  	}

  /* ---------------------------- getResult ------------------------------- */
    /**  
		 * Calls the SANDWedgeViewer
     */
     public Object getResult()
     {
        String filename =   getParameter(0).getValue().toString();
        // String filename = "C:/sasi/sn2d44.dat" ;
				SANDWedgeViewer swv = new SANDWedgeViewer();
        swv.loadData(filename);
				WindowShower shower = new WindowShower(swv);
        java.awt.EventQueue.invokeLater(shower);
        shower = null;
        return "SANDWedgeView displayed";
    }



  /* ----------------------------- Main ---------------------------------- */
    /**
     *   Main program for testing purposes
     */
    public static void main(String[] args) 
    {

    }

}
