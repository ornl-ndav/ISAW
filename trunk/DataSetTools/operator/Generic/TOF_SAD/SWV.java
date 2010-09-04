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
 * Revision 1.5  2006/07/10 21:28:25  dennis
 * Removed unused imports, after refactoring the PG concept.
 *
 * Revision 1.4  2006/07/10 16:26:00  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.3  2005/05/25 20:24:41  dennis
 * Now calls convenience method WindowShower.show() to show
 * the window, instead of instantiating a WindowShower object
 * and adding it to the event queue.
 *
 * Revision 1.2  2005/02/09 21:05:13  dennis
 * Added getDocumentation() method, so that users can obtain
 * information on this operator and the file format for the
 * SandWedgeViewer.
 * Removed unused imports.
 *
 * Revision 1.1  2004/08/11 18:30:09  chatterjee
 * Operator to invoke the SANDWedgeViewer.
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import java.util.Vector;

import DataSetTools.components.View.*;

import gov.anl.ipns.Parameters.LoadFilePG;
import gov.anl.ipns.Util.Sys.WindowShower;

/**
 * 
 * @see DataSetTools.components.View.SANDWedgeViewer
 *
 */
public class SWV  extends GenericTOF_SAD{

   public static final String Title = "Sand Wedge Viewer";

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *  Default Constructor
    */
    public SWV()
    {
      super( Title );
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


  /* ------------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */
   public String getDocumentation()
   {
     StringBuffer s = new StringBuffer("");                                        
     s.append("@overview  This operator loads a data file containing ");
     s.append(" S(Qx,Qy) and pops up a viewer showing the data.  Integration ");
     s.append(" over rings and wedges is supported by the viewer.");

     s.append("@assumptions  The data file must be in the proper format. ");
     s.append(" Specifically, the data must be stored as an ASCII file");
     s.append(" with four columns per line.  Each line consists of the");
     s.append(" four values Qx, Qy, S(Qx,Qy) and err(S(Qx,Qy)), for one");
     s.append(" pixel of an array.  This is the form of file produced by");
     s.append(" the older FORTRAN codes for SAND at IPNS.  A simple" );
     s.append(" header portion listing the number of rows and columns on");
     s.append(" two lines of the form '# Rows: 100' and '# Columns: 100' ");
     s.append(" for a 100x100 array of pixels may optionally be included.");
     s.append(" If the number or rows and columns is not specified, it ");
     s.append(" is assumed to be 200x200, as was produced by the legacy ");
     s.append(" FORTRAN codes at IPNS.");

     s.append("@param String specifying the name of the file containing ");
     s.append(" the pixel data.");

     s.append("@return A string indicating that the file was displayed " );
     return s.toString();
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
        WindowShower.show(swv);
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
