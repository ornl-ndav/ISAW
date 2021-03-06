/*
 * File:  SavitzkyGolayFilter.java
 *
 * Copyright (C) 2004, Dominic Kramer
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Dominic Kramer   <kramerd@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.12  2005/08/24 20:22:02  dennis
 * Added/moved to category DATA_SET_FILTERS_MACROS
 *
 * Revision 1.11  2005/07/13 14:56:26  kramer
 *
 * An 'ElapsedTimer' and an unnecessary call to getDataBlockRange() were
 * made in the getResult() method.  I removed these.  The timer was used
 * for testing purposes and the data returned by the getDataBlockRange()
 * method was not used in the getResult() method.  Also, fixed up some
 * javadocs.
 *
 * Revision 1.10  2005/02/18 06:39:39  kramer
 *
 *
 * Added user documentation for the HTML "Help" window for this operator.
 *
 * Revision 1.9  2004/12/01 20:03:46  kramer
 *
 * Fixed some of the javadoc statements so that there weren't @param
 * arguments referring to variables that didn't exist.
 *
 * Revision 1.8  2004/11/04 00:00:48  kramer
 *
 * Modified this operator to use the classes in the package
 * gov.anl.ipns.MathTools.Smoothing to do the actual work of smoothing data.
 *
 * Revision 1.7  2004/10/14 20:13:02  kramer
 *
 * If the specified group IDs don't exist or the specified x range is invalid
 * the data isn't smoothed (because it can't be), a warning is sent to
 * SharedData, and the original DataSet is returned (if a new DataSet was to
 * be created).  Also, the operator now also logs (in the log of the DataSet
 * being smoothed) the Data blocks that were smoothed and the x range that
 * they were smoothed on.
 *
 * Revision 1.6  2004/10/05 00:55:03  kramer
 *
 * Modified the order of the parameters passed to this operator and created
 * convenience methods to acquire each parameter.  The order of the
 * parameters are:
 * The DataSet to process
 * The number of points to the left of the center to use
 * The number of points to the right of the center to use
 * The degree of the smoothing polynomial
 * The Data blocks to use
 * The x value where smoothing starts
 * The x value where smoothing ends
 * Use quick method
 * Create new DataSet
 *
 * Revision 1.5  2004/10/03 07:00:12  kramer
 *
 * Now this operator can smooth a selected x range if the long method is used
 * as well as the quick method.
 *
 * Revision 1.4  2004/10/03 03:44:30  kramer
 *
 * Now this operator can smooth a selected range of x values if and only if
 * the quick method is used.
 *
 * Revision 1.3  2004/09/22 20:52:02  kramer
 *
 * Now the user can choose to smooth only particular data blocks by
 * specifying their IDs.  Previously, ALL of the data blocks in a DataSet
 * were smoothed.  The selection of smoothing particular data blocks works
 * with both the "quick" and "long" methods for smoothing the data.
 *
 * Revision 1.2  2004/08/10 23:56:59  kramer
 *
 * Now this class uses a copy of a DataSet's y values to determine smoothed
 * values.  As a result, previously smoothed values are not used to determine
 * newly smoothed values.  I also improved the Javadoc documentation and added
 * methods that will eventually be used to determine smoothed values near the
 * start and the end of the arrays of the data used.
 *
 * Revision 1.1  2004/07/28 20:29:27  kramer
 *
 * This is an operator which smoothes the data in all of the Data blocks in
 * a DataSet.  Currently, this operator allows the user to smoothe the data in
 * the selected DataSet or to create a new DataSet with the smoothed data.  It
 * also allows the user to select if he/she wants to use the 'quick' method for
 * smoothing the data or not.  The algorithms for the 'quick' and long methods
 * are both complete.  They also verify good values are enterd so that they can
 * properly smooth the data.  However, the documentation is incomplete.
 *
 */
package Operators.Special;

import gov.anl.ipns.MathTools.Smoothing.LeastSquaresSavitzkyGolaySmoother;
import gov.anl.ipns.MathTools.Smoothing.QuickSavitzkyGolaySmoother;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.SpecialStrings.IntListString;

import java.util.Vector;

import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.VariableXScale;
import DataSetTools.operator.Operator;
import DataSetTools.operator.Parameter;
import DataSetTools.operator.Generic.Special.GenericSpecial;
import DataSetTools.util.SharedData;

/**
 * An Operator for smoothing the data from Data blocks in a DataSet.  This 
 * class uses the Savitzky-Golay Smoothing filter to smooth the data.  
 * Basically, if a y value is to be smoothed, the algorithm looks at a 
 * specified number of x values  to the left and right of the point, generates 
 * a polynomial of a specified degree that best fits the points, and uses the 
 * value of that polynomial as the smoothed value.
 * <p>
 * The algorithm can be mathematically reduced to a much quicker algorithm.  
 * This faster algorithm will only produce the exact same results as the 
 * slower non-reduced algorithm if the data's x values are evenly spaced.  
 * However, it still does a good job at smoothing for x values that are not 
 * evenly spaced if they are close to being evenly spaced.  This class 
 * implements both the quick algorithm and the non-reduced slower algorithm.
 */
public class SavitzkyGolayFilter extends GenericSpecial
{
   /** The command used in scripts to invoke this Operator. */
   private static final String COMMAND = "SavitzkyGolayFilter";
   /** The title of this Operator that shows up in menus. */
   private static final String TITLE = "Savitzky-Golay Filter";
   /** The default number of points used to the left of the center. */
   private static final int DEFAULT_NL = 3;
   /** The default number of points used to the right of the center. */
   private static final int DEFAULT_NR = 3;
   /** The default degree of the smoothing polynomial. */
   private static final int DEFAULT_M  = 2;
   /** Whether to make a new DataSet or not be default. */
   private static final boolean DEFAULT_CREATE_NEW_DATASET = true;
   /** Whether the quick method should be used to smooth the data. */
   private static final boolean DEFAULT_USE_QUICK_METHOD   = true;
   /** The default x value where smoothing is started. */
   private static final float DEFAULT_X_MIN = 0;
   /** The default x value where smoothing ends. */
   private static final float DEFAULT_X_MAX = 100000;
   /**
    * String that signals all Data objects/x values should be used in a 
    * calculation.
    */
   private static final String ALL = "ALL";
   
   /**
    * Creates operator with title "Savitzky-Golay Filter" and a 
    * default list of parameters.
    */
   public SavitzkyGolayFilter()
   {
      super(TITLE);
      //in the top superclass the setDefaultParameters() 
      //method is called to set the dafault parameters.
   }
   
   /**
    * Creates operator with title "Savitzky-Golay Filter" and the 
    * specified list of parameters.  The getResult method must still 
    * be used to execute the operator.
    * @param ds          The DataSet to process.
    * @param nL          The number of points to the left of the point 
    *                    being processed that are used to approximate the 
    *                    point being processed.
    * @param nR          The number of points to the right of the point 
    *                    being processed that are used to approximate the 
    *                    point being processed.
    * @param M           The order of the polynomial used to approximate 
    *                    the data.  This is also equal to the highest 
    *                    moment that is conserved.
    * @param createNewDS True if this operator should create a new 
    *                    DataSet from the data modified.
    * @param useQuickMethod True if the quick method for smoothing the data 
    *                       should be used and false otherwise.
    */
   public SavitzkyGolayFilter(DataSet ds, int nL, int nR, int M, 
         IntListString dataBlockList, int xmin, int xmax, boolean useQuickMethod,
            boolean createNewDS)
   {
      this();
      getParameter(0).setValue(ds);
      getParameter(1).setValue(new Integer(nL));
      getParameter(2).setValue(new Integer(nR));
      getParameter(3).setValue(new Integer(M));
      getParameter(4).setValue(dataBlockList);
      getParameter(5).setValue(new Integer(xmin));
      getParameter(6).setValue(new Integer(xmax));
      getParameter(7).setValue(new Boolean(useQuickMethod));
      getParameter(8).setValue(new Boolean(createNewDS));
   }
   
   /** 
    * Get the name of this operator to use in scripts
    * 
    * @return  "SavitzkyGolayFilter", the command used to invoke this operator 
    * in Scripts
    */
   public String getCommand()
   {
      return COMMAND;
   }


   /* ---------------------------- getCategoryList -------------------------- */
   /**
    *  Get the list of categories describing where this operator should appear
    *  in the menu system.
    *
    *  @return an array of strings listing the menu where the operator 
    *  should appear.
    */
    public String[] getCategoryList()
    {
      return Operator.DATA_SET_FILTERS_MACROS;
    }

      
   /** 
    * Sets default values for the parameters.
    */
   public void setDefaultParameters()
   {
      parameters = new Vector();
      addParameter( new Parameter(
            "DataSet to process", DataSet.EMPTY_DATA_SET));
      addParameter( new Parameter(
            "Number of points left of center", new Integer(DEFAULT_NL)));
      addParameter( new Parameter(
            "Number of points right of center", new Integer(DEFAULT_NR)));
      addParameter( new Parameter(
            "Degree of smoothing polynomial", new Integer(DEFAULT_M)));
      addParameter( new Parameter(
            "Data blocks to use", new IntListString("0:100000")));
      addParameter( new Parameter(
            "X value where smoothing starts", new Float(DEFAULT_X_MIN)));
      addParameter( new Parameter(
            "X value where smoothing ends", new Float(DEFAULT_X_MAX)));
      addParameter( new Parameter(
            "Use quick method", new Boolean(DEFAULT_USE_QUICK_METHOD)));
      addParameter( new Parameter(
            "Create new DataSet", new Boolean(DEFAULT_CREATE_NEW_DATASET)));
   }
   
   /**
    * Get the DataSet to use as specified by the user.
    * @return The DataSet to use.
    */
   private DataSet getDataSet()
   {
      return (DataSet)getParameter(0).getValue();
   }
   
   /**
    * Get the number of points to the left of the center to use 
    * in the calculation, as specified by the user.
    * @return The number of points to the left of the center to 
    *         use.
    */
   private int getNumPointsLeft()
   {
      return ((Integer)getParameter(1).getValue()).intValue();
   }
   
   /**
    * Get the number of points to the right of the center to use 
    * in the calculation, as specified by the user.
    * @return The number of points to the right of the center to 
    *         use.
    */
   private int getNumPointsRight()
   {
      return ((Integer)getParameter(2).getValue()).intValue();
   }
   
   /**
    * Get the degree of the smoothing polynomial to use, as 
    * specified by the user.
    * @return The degree of the smoothing polynomial to use.
    */
   private int getDegreeOfPolynomial()
   {
      return ((Integer)getParameter(3).getValue()).intValue();
   }
   
   /**
    * Get a String representation of the Data objects to smooth, 
    * as specified by the user.
    * @return A String representation of the Data objects to smooth.  
    *         For example, "1,2,10:15" would mean that Data objects 
    *         1, 2, 10, 11, 12, 13, 14, 15 should be smoothed.  An 
    *         {@link IntList IntList} can be used to convert this 
    *         String to an array of integers.
    */
   private String getDataBlockRange()
   {
      return ((IntListString)getParameter(4).getValue()).toString();
   }
   
   /**
    * Get the minimum x value from which smoothing starts, as 
    * specified by the user.
    * @return The x value from which smoothing starts.
    */
   private float getXMin()
   {
      return ((Float)getParameter(5).getValue()).floatValue();
   }
   
   /**
    * Get the maximum x value at which smoothing stops, as 
    * specified by the user.
    * @return The x value at which smoothing stops.
    */
   private float getXMax()
   {
      return ((Float)getParameter(6).getValue()).floatValue();
   }
   
   /**
    * Get the user's response to the question if he/she wants 
    * to use the quick method to smooth the data.
    * @return True if the quick method should be used to 
    *         smooth the data and false if it shouldn't be 
    *         used.
    */
   private boolean getUseQuickMethod()
   {
      return ((Boolean)getParameter(7).getValue()).booleanValue();
   }
   
   /**
    * Get the user's response to the question if he/she wants a 
    * new DataSet to be created from the smoothed data.
    * @return True if a new DataSet should be created from the 
    *         smoothed value.  False if the smoothed data should 
    *         replace the data in the DataSet being smoothed.
    */
   private boolean getMakeNewDataSet()
   {
      return ((Boolean)getParameter(8).getValue()).booleanValue();
   }
   
   /** 
    *  Returns the documentation for this method as a String.  The format 
    *  follows standard JavaDoc conventions.  
    */
   public String getDocumentation()
   {
     StringBuffer s = new StringBuffer("");
     
     s.append("@overview   This operator smooths a user defined ");
     s.append("            selection of Data blocks in a DataSet using the ");
     s.append("            Savitzky-Golay smoothing filter.");
     
     s.append("@param      The DataSet to process.");
     
     s.append("@param      A given point of data is smoothed based on ");
     s.append("            points around it.  This parameter inputs the ");
     s.append("            number of points to use to the left of the ");
     s.append("            point being smoothed.");
     
     s.append("@param      A given point of data is smoothed based on ");
     s.append("            points around it.  This parameter inputs the ");
     s.append("            number of points to use to the right of the ");
     s.append("            point being smoothed.");
     
     s.append("@param      After the points to the left and right of the ");
     s.append("            point being smoothed are collected, a ");
     s.append("            polynomial of best fit is constructed.  This ");
     s.append("            parameter inputs the degree of this polynomial.");
     
     s.append("@param      Here the user can select the Data blocks in a ");
     s.append("            DataSet to smooth by specitying their IDs.  A ':' ");
     s.append("            is used to specify a range of Data blocks to ");
     s.append("            use.  For example, '40:50' specifies all Data ");
     s.append("            blocks with IDs between 40 and 50 including 40 ");
     s.append("            and 50.  A ',' are used to specify specific ");
     s.append("            IDs.  For example, '40,50' specifies Data blocks ");
     s.append("            with IDs 40 and 50.");
     
     s.append("@param      This parameter specifies the x value at which ");
     s.append("            smoothing starts.  Smoothing will start at this ");
     s.append("            x value for all Data blocks.");
     
     s.append("@param      This parameter specifies the x value at which ");
     s.append("            smoothing ends.  Smoothing will end at this ");
     s.append("            x value for all Data blocks.");
     
     s.append("@param      If this box is checked, the quick method for ");
     s.append("            smoothing the data will be used.  See the ");
     s.append("            algorithm section for a description of the ");
     s.append("            quick method and how it compares to the regular ");
     s.append("            algorithm used to smooth the data.");
     
     s.append("@param      If this box is checked, a new DataSet is created ");
     s.append("            with the smoothed data.  If this box is not ");
     s.append("            checked, the DataSet whose data is being smoothed ");
     s.append("            has its data replaced with the smoothed data.");
     
     s.append("@algorithm ");
     s.append("With this operator, the Savitzky-Golay smoothing operator is ");
     s.append("used to smooth a user defined section of a DataSet.  The user ");
     s.append("can select specific Data blocks in a DataSet to smooth.  ");
     s.append("Futhermore, a specific range of x values from these Data ");
     s.append("blocks can be choosen.  This range of x values is the only ");
     s.append("data that will in turn be smoothed for each Data block.");
     s.append("\n\n");
     s.append("To smooth the data, each Data block is smoothed one at a ");
     s.append("time.  Smoothing starts at the first x value and ends at the ");
     s.append("last x value in the range of x values to smooth.  To smooth ");
     s.append("a specific point (x0,y0) in the data, a selected number ");
     s.append("of points to the left and right of the point are collected.  ");
     s.append("Then, a curve of best fit, called the smoothing polynomial, ");
     s.append("is constructed that fits the points collected.  The degree ");
     s.append("of this 'smoothing polynomial' is user defined.  Next, after ");
     s.append("the smoothing polynomial is constructed, the y value of the ");
     s.append("smoothing polynomial at the x value, x0, is used as the ");
     s.append("smoothed value of the data.  This process is repeated for ");
     s.append("every data point in every Data block being smoothed.");
     s.append("\n\n");
     s.append("It is important to note that a point of data in a DataSet ");
     s.append("is not replaced by its smoothed value counterpart in the ");
     s.append("middle of the smoothing process.  Instead the data is ");
     s.append("recorded in a copy of the DataSet being smoothed.  By ");
     s.append("doing this, a point's smoothed value isn't calculated ");
     s.append("based on previously smoothed values.  If smoothed values ");
     s.append("were used to smooth data found later in the DataSet, the ");
     s.append("smoothing would introduce extra errors in the data.");
     s.append("\n\n");
     s.append("Next, the user can select if he/she wants to make a new ");
     s.append("DataSet.  If he/she selects to do this, a new DataSet will ");
     s.append("be created that has the smoothed data from the old DataSets ");
     s.append("as its data.  If a new DataSet is not selected to be ");
     s.append("created, the data in the DataSet being smoothed will have ");
     s.append("its data replaced with its smoothed data.");
     s.append("\n\n");
     s.append("Lastly, the algorithm to smooth the data is very time ");
     s.append("consuming.  However, a 'quick' version of the algorithm ");
     s.append("has been mathematically constructed from the original ");
     s.append("algorithm.  If the data in a particular section of a DataSet ");
     s.append("being smoothed has x values that are evenly spaced, the ");
     s.append("quick method smooths data exactly the same as the original ");
     s.append("algorithm.  However, the quick method will smooth the data ");
     s.append("in a lot less time.  In other words, both algorithms produce ");
     s.append("the exact same results in this situation.");
     s.append("\n\n");               
     s.append("The downside of using the quick method is that if it is ");
     s.append("used to smooth data with x values that are not evenly ");
     s.append("spaced, extra errors can be introduced in the smoothed ");
     s.append("data.  Thus, when using this operator the user should compare ");
     s.append("how much less time the quick algorithm can smooth the data ");
     s.append("to the extra errors it may introduce.");
     return s.toString();
   }
   
   /**
    * Smooths the data from all of the Data blocks from the specified DataSet.
    * 
    * @return The DataSet containing the smoothed values or an 
    *         ErrorString if an error occured.  Note:  If an error message 
    *         is returned, the data has not been smoothed at all.
    */
   public Object getResult()
   {
      DataSet ds        = getDataSet();
      int     nL        = getNumPointsLeft();
      int     nR        = getNumPointsRight();
      int     M         = getDegreeOfPolynomial();
      boolean makeNewDs = getMakeNewDataSet();
      boolean useQuick  = getUseQuickMethod();
      float   xmin      = getXMin();
      float   xmax      = getXMax();

      //first to test "ds"
      if (ds == null)
         return new ErrorString("Error:  Please specify a DataSet.");
      
      //now to determine if a copy of the DataSet should be used
      //or if the actual DataSet should be used
      DataSet new_ds    = null;
      if (makeNewDs)
         new_ds = (DataSet)ds.clone();
      else
         new_ds = ds;
      
      new_ds.addLog_entry("Savitzky-Golay Filtered");
      if (nR == 1)
         new_ds.addLog_entry("  "+nL+" left and "+nR+
               " right point were used with a polynomial of degree "+M);
      else
         new_ds.addLog_entry("  "+nL+" left and "+nR+
               " right points were used with a polynomial of degree "+M);
      
      new_ds.addLog_entry("  Attempting to use the x range, ["+
            xmin+", "+xmax+"]");
      
      //now to use the appropriate smoothing algorithm
      Object returnedObject = null;
      if (useQuick)
         returnedObject = 
            performQuickSmoothing(new_ds,nL,nR,M,xmin,xmax);
      else
         returnedObject = 
            performLeastSquaresSmoothing(new_ds,nL,nR,M,xmin,xmax);
            
      return returnedObject;
   }
   
   //---=[ Methods that do the quick procedure for smoothing the data]=--------
   /**
    * Uses the quick algorithm to smooth the data from all of the Data objects 
    * from the DataSet given.  The quick method will return the exact same 
    * results as the long method if the x values for the data are all equally 
    * spaced.  Otherwise, it is a good approximation to the long method's 
    * results.
    * @param new_ds The DataSet to process.
    * @param nL     The number of points to the left of the point being 
    *               processed that are used to approximate the point 
    *               being processed.
    * @param nR     The number of points to the right of the point being 
    *               processed that are used to approximate the pont 
    *               being processed.
    * @param M      The order of the polynomial used to approximate the 
    *               data.  This is also equal to the highest moment that 
    *               is conserved.
    * @return       The Object that this method wants the 
    *               {@link #getResult() getResult()} to return.
    */
   private Object performQuickSmoothing(DataSet new_ds, int nL, int nR, int M, 
                                        float xmin, float xmax)
   {
      try
      {
        QuickSavitzkyGolaySmoother quickSmoother = 
           new QuickSavitzkyGolaySmoother(nL,nR,M);
      
           Data    data    = null;
         
           String usedDataBlocksString = getDataBlockRange();
           Object dataBounds = getDataObjectBounds(new_ds,usedDataBlocksString);
         
           if (dataBounds instanceof String && 
                 ((String)dataBounds).equalsIgnoreCase(ALL))
           {
              //keeps track if any data blocks were smoothed
              boolean success = false;
              //keeps track if a specific data object was smoothed
              boolean semiSuccess = false;
            
              for (int i=0; i<new_ds.getNum_entries(); i++)
              {
                 //now to get the Data object to use
                   data = new_ds.getData_entry(i);
                 semiSuccess = quickSmoothDataObject(quickSmoother,
                                                     data,xmin,xmax);
                 success = success || semiSuccess;
              }
            
              if (!success)
                 SharedData.addmsg("Warning:  No Data blocks could be " +
                       "Savitsky-Golay smoothed with the specified values " +
                          "on the DataSet " + new_ds);
           }
           else if (dataBounds instanceof int[])
           {
         	  int[] dataIndexArray = (int[])dataBounds;
            
              //for (int i=0; i<dataIndexArray.length; i++)
              //System.out.println("dataIndexArray["+i+"]="+dataIndexArray[i]);
            
              //keeps track if any data blocks were smoothed
              boolean success = false;
              //keeps track if a specific data object was smoothed
              boolean semiSuccess = false;
              
              for (int i=0; i<dataIndexArray.length; i++)
              {
                //now to get the Data object to use
         		  data = new_ds.getData_entry_with_id(dataIndexArray[i]);
                 
                //if data == null then there isn't a Data object for the 
                //index "i".  Thus, ignore quick smoothing it.
                if (data != null)
                {
                   semiSuccess = 
                      quickSmoothDataObject(quickSmoother,
                                            data,xmin,xmax);
                   success = success || semiSuccess;
                }
          	  }
            
              if (!success)
                 SharedData.addmsg("Warning:  No Data blocks could be " +
                       "Savitsky-Golay smoothed with the specified values " +
                          "on the DataSet " + new_ds);
           }
           else if (dataBounds instanceof ErrorString)
              return dataBounds;
           else if (dataBounds instanceof DataSet)
              return (DataSet)dataBounds;
         
           return new_ds;
      }
      catch (IllegalArgumentException e)
      {
         //convert the exception to an ErrorString and send it to the 
         //  calling method.
         return new ErrorString(e.getMessage());
      }
   }
   
   /**
    * @param data  
    * @param xmin  The raw minimum x value as passed to this operator.
    * @param xmax  The raw maximum x value as passed to this operator.
    */
   private boolean quickSmoothDataObject(QuickSavitzkyGolaySmoother smoother,
                                         Data data, float xmin, float xmax)  
                                                throws IllegalArgumentException
   {
      float[] functionizedXVals = getFunctionizedXValues(data);
      int[] xIndexRange = getXIndexRange(functionizedXVals,xmin,xmax);
      if (xIndexRange == null)
         return false;
      
      //this method call will throw an IllegalArgumentException if any of the 
      //  parameters are invalid.
      smoother.smooth(data.getY_values(),xIndexRange[0],xIndexRange[1]);
      
      return true;
   }
   
   /**
    * Uses the long algorithm to smooth the data from all of the Data objects 
    * from the DataSet given.
    * @param new_ds The DataSet to process.
    * @param nL     The number of points to the left of the point being 
    *               processed that are used to approximate the point 
    *               being processed.
    * @param nR     The number of points to the right of the point being 
    *               processed that are used to approximate the pont 
    *               being processed.
    * @param M      The order of the polynomial used to approximate the 
    *               data.  This is also equal to the highest moment that 
    *               is conserved.
    * @return       The Object that this method wants the 
    *               {@link #getResult() getResult()} to return.
    */
   private Object performLeastSquaresSmoothing(DataSet new_ds, int nL, int nR, 
                     int M, float xmin, float xmax)
   {
      try
      {
        LeastSquaresSavitzkyGolaySmoother leastSquaresSmoother = 
           new LeastSquaresSavitzkyGolaySmoother(nL,nR,M);
        
        Data    currentData   = null;
      
        String idBoundsString = getDataBlockRange();
        Object idBounds = getDataObjectBounds(new_ds,idBoundsString);
      
        if (idBounds instanceof ErrorString)
           return idBounds;
        else if (idBounds instanceof String && 
                    ((String)idBounds).equalsIgnoreCase(ALL))
        {
           //keeps track if any data object was smoothed
           boolean success = false;
           //keeps track if a specific data object was smoothed
           boolean semiSuccess = false;
         
           for (int i=0; i<new_ds.getNum_entries(); i++)
           {
              //now to get the current Data object
              currentData   = new_ds.getData_entry(i);
              semiSuccess = 
                 leastSquaresSmoothDataObject(leastSquaresSmoother,
                                              currentData,xmin,xmax);
              success = success || semiSuccess;
           }
         
           if (!success)
              SharedData.addmsg("Warning:  No Data blocks could be " +
                    "Savitsky-Golay smoothed with the specified values " +
                       "on the DataSet " + new_ds);
        }
        else if (idBounds instanceof int[])
        {
           int[] ids = (int[])idBounds;
         
           //keeps track if any data object was smoothed
           boolean success = false;
           //keeps track if a specific data object was smoothed
           boolean semiSuccess = false;
         
           for (int i=0; i<ids.length; i++)
           {
              currentData = new_ds.getData_entry_with_id(ids[i]);
              if (currentData != null)
              {
                 semiSuccess = 
                    leastSquaresSmoothDataObject(leastSquaresSmoother,
                                                 currentData,xmin,xmax);
                 success = success || semiSuccess;
              }
           }
         
           if (!success)
              SharedData.addmsg("Warning:  No Data blocks could be " +
                    "Savitsky-Golay smoothed with the specified values " +
                       "on the DataSet " + new_ds);
        }
        else if (idBounds instanceof DataSet)
           return (DataSet)idBounds;
      
        return new_ds;
      }
      catch (IllegalArgumentException e)
      {
         //convert the exception to an ErrorString and send it to the 
         //  calling method.
         return new ErrorString(e.getMessage());
      }
   }

   private boolean leastSquaresSmoothDataObject(
                                    LeastSquaresSavitzkyGolaySmoother smoother, 
                                    Data currentData, float xmin, float xmax)
                                                throws IllegalArgumentException
   {
      float[] functionizedXVals = getFunctionizedXValues(currentData);
      int[] xIndexArr = getXIndexRange(functionizedXVals,xmin,xmax);
      
      if (xIndexArr == null)
         return false;
      
      //this method call will throw an IllegalArgumentException if any of the 
      //  parameters are invalid.
      smoother.smooth(currentData.getY_values(),functionizedXVals,
                      xIndexArr[0],xIndexArr[1]);
      
      return true;
   }
   
   private Object getDataObjectBounds(DataSet ds, String dataBounds)
   {
      if (dataBounds.equalsIgnoreCase(ALL))
      {
         ds.addLog_entry(
               "  All Data blocks from the DataSet were smoothed");
         return ALL;
      }
      else
      {
         int[] dataArray = IntList.ToArray(dataBounds);
         int[] actualArray = new int[dataArray.length];
         int finalIndex = 0;
         int firstID = ds.getData_entry(0).getGroup_ID();
         int lastID = ds.getData_entry(ds.getNum_entries()-1).getGroup_ID();
         
         for (int i=0; i<dataArray.length; i++)
         {
            if (dataArray[i]>=firstID && dataArray[i]<=lastID)
            {
               actualArray[finalIndex] = dataArray[i];
               finalIndex++;
            }
         }
         
         if (finalIndex == 0)
         {
            SharedData.addmsg("Warning:  The DataSet "+ds+
                  " does not contain any data blocks in the specified range");
            return ds;
         }
         
         int[] finalArray = new int[finalIndex];
         System.arraycopy(actualArray,0,finalArray,0,finalIndex);
         
         ds.addLog_entry(
            "  The data blocks with the following IDs were smoothed:  "+
               IntList.ToString(finalArray));
         
         return finalArray;
      }
   }
   
   /**
    * Determines the starting and ending indices (in the array 
    * <code>xValues</code>) that correspond to closed set (of the x values) 
    * that is a superset of the x values corresponding to the set 
    * [xmin, xmax].  Note:  The array of x values is desgined to work as if it 
    * corresponds to a function.
    * @param xValues 
    * @param xmin
    * @param xmax
    */
   private int[] getXIndexRange(float[] xValues, float xmin, float xmax)
   {
      VariableXScale xScale = new VariableXScale(xValues);
      float startX = xScale.getStart_x();
      float endX = xScale.getEnd_x();
      
      if (xmax < xmin)
         return null;
      else if (xmin > endX)
         return null;
      else if (xmax < startX)
         return null;
            
      float actualMin = startX;
      float actualMax = endX;
      
      if (xmin >= startX)
         actualMin = xmin;
      
      if (xmax <= endX)
         actualMax = xmax;
      
      int[] indexArr = new int[2];
      indexArr[0] = xScale.getI_GLB(actualMin);
      indexArr[1] = xScale.getI(actualMax);
        
      return indexArr;
   }
   
   private float[] getFunctionizedXValues(Data data)
   {
      float[] actualXValues = data.getX_values();
      if (data.isHistogram())
      {
          float[] realXValues = new float[actualXValues.length-1];
          for (int i=0; i<realXValues.length; i++)
             realXValues[i] = (actualXValues[i]+actualXValues[i+1])/2.0f;
          
          return realXValues;
      }
      else
         return actualXValues;
   }
}
