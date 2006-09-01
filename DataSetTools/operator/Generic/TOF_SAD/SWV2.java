/*
 * File:  SWV2.java 
 *             
 * Copyright (C) 2006, Julian Tao 
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
 * Contact : Julian Tao <achatterjee@anl.gov>
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
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;

import DataSetTools.components.View.*;
import DataSetTools.util.SharedData;

import gov.anl.ipns.Parameters.LoadFilePG;
import gov.anl.ipns.Parameters.BooleanPG;
import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.Util.File.TextWriter;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Components.AxisInfo;
import gov.anl.ipns.ViewTools.Components.IVirtualArray2D;
import gov.anl.ipns.ViewTools.Components.VirtualArray2D;


public class SWV2  extends GenericTOF_SAD{

   public static final String Title = "Sand Wedge Viewer Plus/Minus";

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *  Default Constructor
    */
    public SWV2()
    {
      super( Title );
    }

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *    Constructor for SWV.
    *    @param  filename   the name of the file
    */
   public SWV2(String filename1, String filename2) 
     {
      this();
      parameters = new Vector();
      addParameter( new LoadFilePG( "Enter Filename", filename1));
      addParameter( new LoadFilePG( "Enter Filename", filename2));
      addParameter( new BooleanPG ("Subtract: ", true));
    }


   /* ----------------------- setDefaultParameters  ------------------------ */

    public void setDefaultParameters()
    {
      parameters = new Vector();
      addParameter( new LoadFilePG( "Enter Filename", null));
      addParameter( new LoadFilePG( "Enter Filename", null));
      addParameter( new BooleanPG ("Subtract: ", true));
     
    }

  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "SWV2", the command used to invoke this 
   *           operator in Scripts
   */
  	public String getCommand()
  	{
    	return "SWV2";
  	}


  /* ------------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */
   public String getDocumentation()
   {
     StringBuffer s = new StringBuffer("");                                        
     s.append("@overview  This operator loads two data files containing ");
     s.append(" S(Qx,Qy), adds/subtracts the second set of data to/from the first one, ");
     s.append(" pops up a viewer showing the data.  Integration ");
     s.append(" over rings and wedges is supported by the viewer.");

     s.append("@assumptions  The data files must be in the proper format. ");
     s.append(" Specifically, each data must be stored as an ASCII file");
     s.append(" with four columns per line.  Each line consists of the");
     s.append(" four values Qx, Qy, S(Qx,Qy) and err(S(Qx,Qy)), for one");
     s.append(" pixel of an array.  This is the form of file produced by");
     s.append(" the older FORTRAN codes for SAND at IPNS.  A simple" );
     s.append(" header portion listing the number of rows and columns on");
     s.append(" two lines of the form '# Rows: 100' and '# Columns: 100' ");
     s.append(" for a 100x100 array of pixels may optionally be included.");
     s.append(" If the number or rows and columns is not specified, it ");
     s.append(" is assumed to be 200x200, as was produced by the legacy ");
     s.append(" FORTRAN codes at IPNS. Qx and Qy arrays in the second data file ");
     s.append(" are assumed to match exactly to those in the first one. ");
     s.append("@param String specifying the name of the first file containing ");
     s.append(" the pixel data.");
     s.append("@param String specifying the name of the second file containing ");
     s.append(" the pixel data.");
     s.append("@param to sum ? ");
     s.append("@param or to subtract ? ");
     s.append("@return A string indicating that the file was displayed " );
     return s.toString();
   }


  /* ---------------------------- getResult ------------------------------- */
    /**  
     * Calls the SANDWedgeViewer
     */
     public Object getResult()
     {
        String filename1 =   getParameter(0).getValue().toString();
        String filename2 =   getParameter(1).getValue().toString();
        boolean doMinus = (Boolean) getParameter(2).getValue();
        float plusminus = -1.0f;
        String fout = filename1 + ((doMinus)?".m":".p");
        StringBuffer output = new StringBuffer();
        
        // String filename = "C:/sasi/sn2d44.dat" ;
        SANDWedgeViewer swv1 = new SANDWedgeViewer();
        swv1.loadData(filename1);
        SANDWedgeViewer swv2 = new SANDWedgeViewer();
        swv2.loadData(filename2);
        
        VirtualArray2D va2D1 = (VirtualArray2D) swv1.getData();
        VirtualArray2D va2D2 = (VirtualArray2D) swv2.getData();
        
        int m = va2D1.getNumRows();
        int n = va2D1.getNumColumns();
        float[][] a1 = va2D1.getRegionValues(0, m-1, 0, n-1);
        float[][] a2 = va2D2.getRegionValues(0, m-1, 0, n-1);
        float[][] a1e = va2D1.getErrors();
        float[][] a2e = va2D2.getErrors();
        float[] qxs = swv1.getQxs();
        float[] qys = swv1.getQys();
        
        if (!doMinus) plusminus = 1.0f;
        float sigx, sigy;
        for (int j = 0; j < n; j++ ){
          for (int i = m-1; i > -1; i--){
            a1[i][j] += plusminus*a2[i][j];
            sigx = a1e[i][j];
            sigy = a2e[i][j];
            a1e[i][j] = ((Double)Math.sqrt(sigx*sigx+sigy*sigy)).floatValue();
            output.append(qxs[j]+" "+qys[i]+" "+a1[i][j]+" "+a1e[i][j]+"\n");
          }
        }
//        swv1.setData(a1, a1e);         
                
//        System.out.println(output);
        TextWriter.writeASCII( fout, output.toString() );
        WindowShower.show(swv1);
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
