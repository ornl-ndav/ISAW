/*
 * File:  MakeDataSet.java
 *
 * Copyright (C) 2004 Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.6  2004/03/15 19:36:53  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.5  2004/03/15 03:37:00  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.4  2004/03/10 17:58:15  rmikk
 * Added two parameters for width and height of grids
 *
 * Revision 1.3  2004/03/09 17:56:52  rmikk
 * Fixed Javadoc errors
 *
 * Revision 1.2  2004/03/06 19:05:21  rmikk
 * Tested the code to work with Vectors. This can now be used with the ISAW
 * Scripting language
 *
 * Revision 1.1  2004/02/29 17:59:32  rmikk
 * Initial Checkin. This operator attempts to make a DataSet from data stored
 * in (possibly multidimensional) arrays or Vectors.
 *
 *
 */

package Operators.Generic;


import DataSetTools.operator.Wrappable;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import java.lang.reflect.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.DataSet.Attribute.*;

/*
 * This operator makes a DataSet out of Vectors containing arrays and/or
 * Vectors of the Data along with the errors. 
 */

public class MakeDataSet implements Wrappable {
 
    public Vector xbins = new Vector(); //must be one xbin for one DataSet
    public Vector yvals = new Vector();// Can be float[chan],float[gr/det][chan],float[r][c][chan]
    // or float[det][r][c][chan]. int,double and vector will
    // be converted.
    public Vector errs = new Vector(); 
    public String Title = "DataSet Name";
    public String XLabel = "time label";
    public String XUnits = "Time units";
    public String YLabel = "Y label";
    public String YUnits = "Y units";
    public Vector GridWidths = new Vector();  //Elements must be Floats,1 for @ grid
    public Vector GridHeights = new Vector();

    private boolean hasRowCol = false;

    /**
     * Entry via Java and Jython code. It Executes the calculate() method.
     * This method makes a DataSet out of Vectors containing arrays and/or
     * Vectors of the Data along with the errors.
     * @param xbins   A vector containing data that can be converted to float[]
     *                 of x(time) bins.
     * @param yvals A Vector with the y values( intensity). This vector can 
     *           contain multidimensional arrays or Vectors. 
     * @param errs A Vector containing the errors in the same format as
     *              the y values
     * @param Title the Title for the data set
     * @param XLabel  the label for the x(time/Q) values
     * @param XUnits the Units of the xvalues
     * @param YLabel the label for the Y values( Intensities)
     * @param YUnits the Units for the Y values
     * @param GridWidths  The list of widths for any Grids
     * @param GridHeights The list of heights for any Grids
     * @return a DataSet with the given values, titles, etc.
     */
    public Object calculate(Vector xbins, Vector yvals, Vector errs, 
                 String Title, String XLabel,String XUnits, String YLabel,
                 String YUnits, Vector GridWidths, Vector GridHeights) {

        this.errs = errs;
        this.Title = Title;
        this.XLabel = XLabel;
        this.XUnits = XUnits;
        this.YLabel = YLabel;
        this.YUnits = YUnits;

        return calculate();
    }       

    

    /**
     * Returns "MakeDataSet", the command used to execute the operator in the
     * Scripting language.
     */
    public String getCommand() {

        return "MakeDataSet";
    }


    /**
     * Returns a string for the on-line documentation of this operator
     */
    public String getDocumentation() {
    
        StringBuffer s = new StringBuffer();

        s.append("@overview  This method makes a DataSet out of Vectors ");
        s.append("containing arrays and/orVectors of the Data along with ");
        s.append("the errors.");
        s.append("@param xbins   A vector containing data that can be");
        s.append("converted to float[] of x(time) bins.");
        s.append("@param yvals A Vector with the y values( intensity). This");
        s.append("vector can contain multidimensional arrays or Vectors.");
        s.append("@param errors A Vector containing the errors in the same ");
        s.append("format as the y values");
        s.append(" @param the Title for the data set");
        s.append("@param XLabel  the label for the x(time/Q) values");
        s.append("@param XUnits the Units of the xvalues");
        s.append(" @param YLabel the label for the Y values( Intensities)");
        s.append("@param YUnits the Units for the Y values");
        s.append("@param GridWidths  The list of widths for any Grids");
        s.append("@param GridHeights The list of heights for any Grids");
        s.append("@return a DataSet with the given values, titles, etc.");
        s.append("@error Improper match in size of yvalues and xvalues");
        s.append("@error Improper Inputs");
       
        return s.toString();

    }


    /**
     * This method makes a DataSet out of Vectors containing arrays and/or
     * Vectors of the Data along with the errors.
     */
    public Object calculate() {

        if (xbins == null)
            return new ErrorString("No X values entered");

        if (yvals == null)
            return new ErrorString(" No Y values are entered");

        if (yvals.size() < 1)
            return new ErrorString(" No Y values are entered");


        XScale xscl;

        try {
            float[] O = NexIO.Util.ConvertDataTypes.
                                      floatArrayValue(xbins.firstElement());
            if( O == null)
              return new ErrorString( "Cannot convert xvals to a float[]");
            xscl = new VariableXScale(O);
        
        } catch (Exception ss) {
            return new ErrorString(ss);
        }

        Object result = null;
    
        float[] yvalues = null;
        float[] errors = null;
        boolean hist = true;
   
        DataSet DS = new DataSet(Title, new OperationLog(), XUnits, XLabel, 
                                                                YUnits, YLabel);
        int[] dd = {0, 0, 0, 0};//position det,row,col
        Object X1 = yvals, X2 = errs;
        ElementExtractor ext = new VectorElementExtractor();

        //If the Vector yvals is just a wrapper( dim 1) unwrap it
        if (((Vector) X1).size() == 1) {
            X1 = ((Vector) X1).elementAt(0);
            if (X2 != null) if (((Vector) X2).size() > 0)
                    X2 = ((Vector) X2).elementAt(0);
       
            ext = new ArrayElementExtractor();
        }
       
        String S = AddNextDataBlocks(DS, dd, xscl, X1, X2, ext);

        if (S.length() > 1)
            return new ErrorString(S);

        DataSetFactory.addOperators(DS);
    
        if (hasRowCol) {

            DS.addOperator(new GetPixelInfo_op());

            if (theGrid != null)
                theGrid.setDataEntriesInAllGrids(DS);
        }
        return DS;
    }

    int[] dimensions = new int[3];  // Used to get nrows, ncols, and ngrids

    private String AddNextDataBlocks(DataSet DS, int[] dd, XScale xscl, 
        Object yvals, Object errs, ElementExtractor inf) {

        ElementExtractor inf1;
        int n = inf.dim(yvals);

        if (n <= 0)
            return "No data";
      
        if (inf.Data(yvals) == null) {

            insert(n, dimensions);
            for (int i = 0; i < n; i++) {

                Object X = inf.ElementAt(yvals, i);
                Object X1 = inf.ElementAt(errs, i);

                if (X instanceof Vector) 
                    inf1 = new VectorElementExtractor();
                else 
                    inf1 = new ArrayElementExtractor();

                dd = next(dd, false);
                int[] newID = next(dd, true);
                String S = AddNextDataBlocks(DS, newID, xscl, X, X1, inf1);

                if (S.length() > 1) 
                   return S; 
            }
            return "";

        } else { //at base can add

            float[] yvalues = inf.Data(yvals);
            float[] errors = inf.Data(errs);
            Data D = null;

            if (yvalues.length == xscl.getNum_x())

                if (errors != null)
                    D = new FunctionTable(xscl, yvalues, errors, 
                                                     DS.getNum_entries() + 1);

                else
                    D = new FunctionTable(xscl, yvalues, 
                                                     DS.getNum_entries() + 1);

            else if (yvalues.length + 1 == xscl.getNum_x())

                if (errors != null)
                    D = new HistogramTable(xscl, yvalues, errors, 
                                                     DS.getNum_entries() + 1);

                else
                    D = new HistogramTable(xscl, yvalues, 
                                                     DS.getNum_entries() + 1);

            else

                return "Num X's and Y's do not match";

            SetDetInf(D, dd);
            DS.addData_entry(D);
            return "";

        }

    }

    // Inserts size at the start of dimension array
    void insert(int size, int[]dimensions) {
        for (int i = dimensions.length - 1; i >= 1; i--) {

            dimensions[i] = dimensions[i - 1];

        }

        dimensions[0] = size;
    }


    // Sort of a counter. downLevel starts incrementing the next ...
    int[] next(int[] dd, boolean downLevel) {

        int[] Res = new int[dd.length];

        System.arraycopy(dd, 0, Res, 0, dd.length);

        int k = dd[0];

        if (downLevel) {

            k++;
            Res[0]++;

        } else

            Res[1 + k]++;

        return Res;

    }

    // Sets up the IPIXEL_INFO attribute for a data block
    UniformGrid theGrid = null;
    int n = -1;

    void SetDetInf(Data D, int[] dd) {

        if (dd[0] < 2) 
            return;
        if (dd[0] > 3) 
            return;
        if ((theGrid == null) || ((dd[0] == 3) && (n < dd[1]))) {

            if (n < 0) 
                n = 1;

            else 
               n = dd[1];
            
            float width =.8f;
            float height =.8f;
            if( GridWidths != null) if( GridWidths.size()>n-1)
            if( GridWidths.elementAt(n-1) instanceof Number)
                width = ((Number)(GridWidths.elementAt(n-1))).floatValue();
            if( GridHeights != null) if( GridHeights.size()>n-1)
            if( GridHeights.elementAt(n-1) instanceof Number)
                height = ((Number)(GridHeights.elementAt(n-1))).floatValue();

            theGrid = new UniformGrid(n, "m", new Vector3D(0f, 0f, 1f), 
                        new Vector3D(1f, 0f, 0f),
                        new Vector3D(0f, 1f, 0f), width, height, .1f, 
                        Max(1, dimensions[1]), Max(1, dimensions[0]));

        }

        DetectorPixelInfo[] dpi = new DetectorPixelInfo[1];
        int k = dd[0] - 2;

        dpi[0] =
                new DetectorPixelInfo(1, (short) dd[1 + k], (short) dd[2 + k],
                                                                      theGrid);

        D.setAttribute(new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST, 
                new PixelInfoList(dpi)));


        hasRowCol = true;

    }



    int Max(int n1, int n2) {

        if (n1 > n2)
            return n1;

        return n2;
    }



    /**
     * Test program for this module
     */
    public static void main(String[] args) {

        float[] xvals = {1f, 2f, 3f, 4f, 5f, 6f};
        // row col
        float[][][] yvals = {
                              { {1f, 2f, 3f, 4f, 5f},
                                {  2f, 3f, 4f, 5f, 6f},
                                {3f, 4f, 5f, 6f, 7f},
                                {  2f, 3f, 4f, 5f, 6f}
                              },
                              { {8f, 9f, 10f, 11f, 12f},
                                {  13f, 14f, 15f, 16f, 17f},
                                {18f, 19f, 25f, 26f, 27f},
                                {  2f, 3f, 4f, 5f, 6f}

                              },
                              { {31f, 32f, 33f, 34f, 35f},
                                {  32f, 33f, 34f, 35f, 36f},
                                {33f, 34f, 35f, 36f, 37f},
                                {  2f, 3f, 4f, 5f, 6f}

                              },
                              { {31f, 32f, 33f, 34f, 35f},
                                {  32f, 33f, 34f, 35f, 36f},
                                {33f, 34f, 35f, 36f, 37f},
                                {  2f, 3f, 4f, 5f, 6f}

                             }

                          };
        float[][][] errs = {
                              { {1f, 1f, 1f, 1f, 1f},
                                {  1f, 1f, 1f, 1f, 1f},
                                {1f, 1f, 1f, 1f, 1f},
                                {  1f, 1f, 1f, 1f, 1f}
                              },
                              { {2f, 2f, 2f, 2f, 2f},
                                {  2f, 2f, 2f, 2f, 2f},
                                {2f, 2f, 2f, 2f, 2f},
                                {  2f, 2f, 2f, 2f, 2f}

                              },
                              { {3f, 3f, 3f, 3f, 3f},
                                {  3f, 3f, 3, 3f, 3f},
                                {3f, 3f, 3f, 3f, 3f},
                                {  3f, 3f, 3, 3f, 3f}

                              },
                              { {3f, 3f, 3f, 3f, 3f},
                                {  3f, 3f, 3, 3f, 3f},
                                {3f, 3f, 3f, 3f, 3f},
                                {  3f, 3f, 3, 3f, 3f}

                              }
                          };

        /* //2D no detectors
         float[][] yvals ={ 
                            {1f,2f,3f,4f,5f},
                            {  2f,3f,4f,5f,6f},
                            {3f,4f,5f,6f,7f}
         
         
                          };
         float[][] errs ={
                           {1f,1f,1f,1f,1f},
                           {1f,1f,1f,1f,1f},
                           {1f,1f,1f,1f,1f}
                         }; 
         */
        
        /*// 2d by 1 chan array
         float[] xvals = {0f,1f};
         
         float[][][] yvals= {
                             {  {1f},{2f},{3f},{4f}


                              },//end row 1

                             {                    
                                {5f},{6f},{7f},{8f}

                              },//end row 2
                             {
                               {9f},{10f},{11f},{12f}
                            }//end row 3



                           };

         
         float [][][]errs =null;
         */   
        MakeDataSet mds = new MakeDataSet();
        Vector V = new Vector();

        V.add(xvals);
        mds.xbins = V;
        V = new Vector();
        V.add(yvals);
        mds.yvals = V;
        V = new Vector();
        V.add(errs);
        mds.errs = V;

        Object O = mds.calculate();

        Command.ScriptUtil.display(O);
    }



    /**
     *  An Interface to make accessing elements of an array
     *  and elements of a Vector the same
     */
    interface ElementExtractor {
     
        public int  dim(Object O);

        /** Returns an Array or Vector with 2 or more dims
         */
        public Object ElementAt(Object O, int i);

        /**
         *  If primitive  array use this
         */
        public float  DataAt(Object O, int i);

        /**
         * Returns null if it cannot be converted to float[]
         */
        public float[] Data(Object O);
    }


    /**
     * This class extracts elements of a Vector
     */
    class VectorElementExtractor implements ElementExtractor {

        public int  dim(Object O) { 
 
            if (O == null)
                return 0;

            if (((Vector) O).size() < 1)
                return 0;

            return ((Vector) O).size();
        }

        /** Returns an Array or Vector that is NOT a data Array
         *  i.e. cannot be converted into float[]
         */
        public Object ElementAt(Object O, int i) {

            if (O == null)
                return null;

            if (i < 0)
                return null;

            if (i >= dim(O))
                return null;

            Object X = ((Vector) O).elementAt(i);

            if (X == null)
                return X;

            if (X instanceof Vector) 
                return X;
            if( X instanceof Number)
                return X;

            if (!(X.getClass().isArray())) 
               return null;
           
               

            return X;
     
        }

        /**
         *  If primitive  array use this
         */
        public float  DataAt(Object O, int i) {

            Object X = ElementAt(O,i);

            if (X == null) 
                return Float.NaN;

            if (X instanceof Number)
                return ((Number) X).floatValue();

            return Float.NaN;
        }

        public float[] Data(Object O) {

            float[] f = new float[dim(O)];

            for (int i = 0; i < f.length; i++) {

                f[i] = DataAt(O, i);
                if (Float.isNaN(f[i])) 
                   return null;

            }

            return f;   
        }
    }


    /** 
     *  This class extracts elements of an Array
     */
    class ArrayElementExtractor implements ElementExtractor {
        public int  dim(Object O) {
 
            if (O == null)
                return 0;

            if (!(O.getClass().isArray()))
                return 0;
       
            return Array.getLength(O);

        }

        /** Returns an Array or Vector with 2 or more dims
         */
        public Object ElementAt(Object O, int i) {

            if (O == null) 
                return null;

            if (i < 0) 
                return null;

            if (i >= dim(O)) 
                return null;

            Object X = Array.get(O, i);

            if (X != null)
                if ((X.getClass().isArray()) || (X instanceof Vector))
                    return X;

                else 
                    return null;

            return null;
        }

        /**
         *  If primitive  array use this
         */
        public float  DataAt(Object O, int i) {
            if (O == null) 
                return Float.NaN;

            try {
                return Array.getFloat(O, i);

            } catch (Exception ss) {

                return Float.NaN;

            }
      
        }

        public float[] Data(Object O) {
            if (O == null) 
                return null;

            if (!(O.getClass().isArray()))
                return null;

            float[] Res = new float[ Array.getLength(O)];

            try {

                System.arraycopy(O, 0, Res, 0, Res.length);

            } catch (Exception ss) {

                return null;

            }

            return Res;
        }
    
    }//ArrayElementExtractor

}//MakeDataSet
