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
 * Revision 1.2  2004/08/10 23:56:59  kramer
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

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.Sys.ElapsedTime;

import java.util.Vector;

import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.operator.Parameter;
import DataSetTools.operator.Generic.Special.GenericSpecial;

/**
 * An Operator for smoothing the data from Data blocks in a DataSet.  This class 
 * uses the Savitzky-Golay Smoothing filter to smooth the data.  Basically, if a 
 * y value is to be smoothed, the algorithm looks at a specified number of x values  
 * to the left and right of the point, generates a polynomial of a specified 
 * degree that best fits the points, and uses the value of that polynomial as the 
 * smoothed value.
 * <p>
 * The algorithm can be mathematically reduced to a much quicker algorithm.  This 
 * faster algorithm will only produce the exact same results as the slower non-reduced 
 * algorithm if the data's x values are evenly spaced.  However, it still does a 
 * good job at smoothing for x values that are not evenly spaced if they are close 
 * to being evenly spaced.  This class implements both the quick algorithm and the 
 * non-reduced slower algorithm.
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
    * @param useQuickMethod True if the quick method for smoothing the data should 
    *                       be used and false otherwise.
    */
   public SavitzkyGolayFilter(DataSet ds, int nL, int nR, int M, boolean createNewDS, boolean useQuickMethod)
   {
      this();
      getParameter(0).setValue(ds);
      getParameter(1).setValue(new Integer(nL));
      getParameter(2).setValue(new Integer(nR));
      getParameter(3).setValue(new Integer(M));
      getParameter(4).setValue(new Boolean(createNewDS));
      getParameter(5).setValue(new Boolean(useQuickMethod));
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
   
   /** 
    * Sets default values for the parameters.
    */
   public void setDefaultParameters()
   {
      parameters = new Vector();
      addParameter( new Parameter("Data set to process", DataSet.EMPTY_DATA_SET) );
      addParameter( new Parameter("Number of points left of center", new Integer(DEFAULT_NL)));
      addParameter( new Parameter("Number of points right of center", new Integer(DEFAULT_NR)));
      addParameter( new Parameter("Degree of smoothing polynomial", new Integer(DEFAULT_M)));
      addParameter( new Parameter("Create new data set", new Boolean(DEFAULT_CREATE_NEW_DATASET)));
      addParameter( new Parameter("Use quick method", new Boolean(DEFAULT_USE_QUICK_METHOD)));
   }
   
   //TODO Make new documentation
   /** 
    *  Returns the documentation for this method as a String.  The format 
    *  follows standard JavaDoc conventions.  
    */                                                                                 
   public String getDocumentation()
   {
     StringBuffer s = new StringBuffer("");
     
     s.append("@overview This operator smoothes all Data blocks in ");
     s.append("          a DataSet using a Savitzky-Golay smoothing ");
     s.append("          filter.");
     
     s.append("@param  ds  The DataSet to process.");
     
     return s.toString();
   }
   
   /**
    * Smooths the data from all of the Data blocks from the specified DataSet.
    * @return The DataSet containing the smoothed values or an 
    * ErrorString if an error occured.  Note:  If an error message 
    * is returned, the data has not been smoothed at all.
    */
   public Object getResult()
   {
      ElapsedTime timer = new ElapsedTime();
      timer.reset();
      
      DataSet ds        = (DataSet)(getParameter(0).getValue());
      int     nL        = ((Integer)(getParameter(1).getValue())).intValue();
      int     nR        = ((Integer)(getParameter(2).getValue())).intValue();
      int     M         = ((Integer)(getParameter(3).getValue())).intValue();
      boolean makeNewDs = ((Boolean)getParameter(4).getValue()).booleanValue();
      boolean useQuick  = ((Boolean)getParameter(5).getValue()).booleanValue();         
         
      //now to determine if a copy of the DataSet should be used
      //or if the actual DataSet should be used
      DataSet new_ds    = null;
      if (makeNewDs)
         new_ds = (DataSet)ds.clone();
      else
         new_ds = ds;
      
      //now to check the validity of the values entered
      if (nL<0)
         return new ErrorString("  The number of points left of the center cannot be negative");
      if (nR<0)
         return new ErrorString("  The number of points right of the center cannot be negative");
      if (M<0)
         return new ErrorString("  The degree of the smoothing polynomial cannot be negative");
      
      //now to use the appropriate smoothing algorithm
      Object returnedObject = null;
      if (useQuick)
         returnedObject = performQuickSmoothing(new_ds,nL,nR,M);
      else
         returnedObject = performLeastSquaresSmoothing(new_ds,nL,nR,M);
            
      return returnedObject;
   }
   
   //-----------=[ Methods that do the quick procedure for smoothing the data]=------------
   /**
    * Uses the quick algorithm to smooth the data from all of the Data objects from the 
    * DataSet given.  The quick method will return the exact same results as the long method 
    * if the x values for the data are all equally spaced.  Otherwise, it is a good 
    * approximation to the long method's results.
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
   private Object performQuickSmoothing(DataSet new_ds, int nL, int nR, int M)
   {
      float[][] c = calculateAll_c_Coefficients(nL,nR,M);
      
      if (c != null)
      {
         Data    data    = null;
         float[] f       = null;
         float[] fCopy   = null;
         int     n       = -nL;
         int     i       = 0;
         float   sum     = 0;
         
         for (int dataNum=0; dataNum<new_ds.getNum_entries(); dataNum++)
         {
            //now to get the Data object to use
              data = new_ds.getData_entry(dataNum);
            //and a copy of its y values
              fCopy = data.getCopyOfY_values();
            //and a reference to its actual array of y values
              f = data.getY_values();
            
            //if (data.getX_scale() instanceof UniformXScale)
            //   System.out.println(data+" has a uniform XScale");
              
            for (i=0; i<fCopy.length; i++)
               f[i] = getQuickSmoothedValue(c,fCopy,i,nL,nR,M);
         }
         
         new_ds.addLog_entry("Savitzky-Golay Filtered");
         if (nR == 1)
            new_ds.addLog_entry("  "+nL+" left and "+nR+" right point used with a polynomial of degree "+M);
         else
            new_ds.addLog_entry("  "+nL+" left and "+nR+" right points used with a polynomial of degree "+M);
         
         return new_ds;
      }
      else
         return new ErrorString("  The data cannot be smoothed using the entered values.");
   }
   
   /**
    * Get the smoothed value for the y value at the index <code>yIndex</code>.
    * @param cArr     The array returned from the method 
    *                 {@link #calculateAll_c_Coefficients(int, int, int) 
    *                 calculateAll_c_Coefficients(int, int, int)}.
    * @param yValues  The array of y values that are used calculate the smoothed value.
    * @param yIndex   The index of the y value from <code>yValues</code> that 
    *                 is to be smoothed.
    * @param nL       The number of points to the left of the point being 
    *                 processed that are used to approximate the point 
    *                 being processed.
    * @param nR       The number of points to the right of the point being 
    *                 processed that are used to approximate the pont 
    *                 being processed.
    * @param M        The order of the polynomial used to approximate the 
    *                 data.  This is also equal to the highest moment that 
    *                 is conserved.
    * @return         The smoothed value.
    */
   private float getQuickSmoothedValue(float[][] cArr, float[] yValues, int yIndex, int nL, int nR, int M)
   {
      boolean leftCompensate = false;
      boolean rightCompensate = false;
      
      if (nL > yIndex)
      {
         leftCompensate = true;
         //yIndex = nL;
      }
      else if (nR > yValues.length-1-yIndex)
      {
         rightCompensate = true;
         //yIndex = yValues.length-1-nR;
      }
      
      if (leftCompensate)
      {
         //This is a quick fix.  If the point is too close to the edge just return 
         //the actual value.
         return yValues[yIndex];
         //return calculateValueOnSmoothedPolynomial(cArr,yValues,yIndex,nL,nR,M,-nL);
      }
      else if (rightCompensate)
      {
         //This is a quick fix.  If the point is too close to the edge just return 
         //the actual value.
         return yValues[yIndex];
         //return calculateValueOnSmoothedPolynomial(cArr,yValues,yIndex,nL,nR,M,nR);
      }
      else
      {
         float sum = 0;
         for (int n = -nL; n<=nR; n++)
            sum += cArr[0][n+nL]*yValues[yIndex+n];
         return sum;
      }
   }
   
   private float calculateValueOnSmoothedPolynomial(float[][] coeff, float[] yValues, int yIndex, int nL, int nR, int M, float value)
   {
      float[] aArr = new float[M+1];
      for (int row=0; row<=M; row++)
         aArr[row] = calculateValueForA(coeff,yValues,yIndex,nL,nR,M,row);
      
      float sum = 0;
      for (int i=0; i<=M; i++)
         sum += aArr[0]*Math.pow(value,i);
      return sum;
   }
   
   private float calculateValueForA(float[][] coeff, float[] yValues, int yIndex, int nL, int nR, int M, int row)
   {
      float sum = 0;
      for (int column=0; column<=M; column++)
         sum += coeff[row][column]*yValues[yIndex-nL+column];
      return sum;
   }
   
   /**
    * Get all of the "c" coefficients used to smooth the data.  The 
    * "c" coefficients are used to determine the coefficients of the 
    * smoothing polynomial.
    * <p>
    * Basically (a of i) = (c of -nL)*f(-nL)+(c of -nL+1)*f(-nL+1)
    * + . . . . +(c of nR)*f(nR) where (a of i) is the coefficient 
    * from the smoothing polynomial that is multiplied by x^i.
    * @param nL The number of points to the left of the point being 
    *           processed that are used to approximate the point 
    *           being processed.
    * @param nR The number of points to the right of the point being 
    *           processed that are used to approximate the pont 
    *           being processed.
    * @param M  The order of the polynomial used to approximate the 
    *           data.  This is also equal to the highest moment that 
    *           is conserved.
    * @return   The "c" coefficients used to smooth the data or null if 
    *           they cannot be determined.
    *           <p>
    *           If <code>doubleArr</code> is the array returned from this 
    *           method, then <code>doubleArr[i]</code> is the array of 
    *           nL+nR+1 floats, each of which is one of the "c" coefficients 
    *           corresponding to ai (the coefficient from the smoothing 
    *           polynomial that is multiplied by x^i).
    */
   private float[][] calculateAll_c_Coefficients(int nL, int nR, int M)
   {
      float[][] B = calculateInverseOfATransposeA(nL,nR,M);
      
      if (B != null)
      {
         float[][] coefArr = new float[M+1][nL+nR+1];
         for (int row=0; row<=M; row++)
            for (int n=-nL; n<=nR; n++)
               coefArr[row][n+nL] = get_c_CoefficientAt(n,row,B,nL,nR,M);
         return coefArr;
      }
      else
         return null; //because the ATransposeDotA is not invertable
   }
   
   /**
    * Get the nth "c" coefficient used to smooth the data.  The "c" 
    * coefficients are used to determine the coefficients in the 
    * polynomial of best fit.
    * <p>
    * Basically (a of i) = (c of -nL)*f(-nL)+(c of -nL+1)*f(-nL+1)
    * + . . . . +(c of nR)*f(nR) where (a of i) is the coefficient 
    * from the smoothing polynomial that is multiplied by x^i.
    * @param n     The number of the desired "c" coefficient. (the 
    *              first coefficient is found at n=-nL and the last 
    *              coefficient is found at n=nR).
    * @param a_num If <code>a_num=i</code> the "c" coefficent returned 
    *              is the nth coefficient used to determine ai (the 
    *              coefficient from the smoothing polynomial that is 
    *              multiplied by x^i).
    * @param B     The matrix returned by {@link 
    *              #calculateInverseOfATransposeA(int, int, int) 
    *              calculateInverseOfATransposeDotA(int nL, int nR, int M)}.
    * @param nL    The number of points to the left of the point being 
    *              processed that are used to approximate the point 
    *              being processed.
    * @param nR    The number of points to the right of the point being 
    *              processed that are used to approximate the pont 
    *              being processed.
    * @param M     The order of the polynomial used to approximate the 
    *              data.  This is also equal to the highest moment that 
    *              is conserved.
    * @return      The nth coefficient.
    */
   private float get_c_CoefficientAt(int n, int a_num, float[][] B, int nL, int nR, int M)
   {
      float sum = 0;
      for (int m=0; m<=M; m++)
         sum += B[a_num][m]*Math.pow(n,m);
      return sum;
   }
   
   /**
    * Determines the matrix that would result by taking the inverse of 
    * th matrix that would result by taking the product 
    * of transpose of the matrix "A" with the matrix "A".
    * <p>
    * The matrix "A" is defined such that the ith row and jth column in 
    * the matrix has the value i^j and is used to calculate the 
    * coefficients of the polynomial of best fit.
    * @param nL The number of points to the left of the point being 
    *           processed that are used to approximate the point 
    *           being processed.
    * @param nR The number of points to the right of the point being 
    *           processed that are used to approximate the pont 
    *           being processed.
    * @param M  The order of the polynomial used to approximate the 
    *           data.  This is also equal to the highest moment that 
    *           is conserved.
    * @return   ((Transpose of A)(A))^(-1)
    */
   private float[][] calculateInverseOfATransposeA(int nL, int nR, int M)
   {
      //first to create the matrix
      float[][] A = new float[nL+nR+1][M+1];
      for (int row=-nL; row<=nR; row++)
         for (int col=0; col<=M; col++)
            A[row+nL][col] = (float)Math.pow(row,col);
      
      //this is A Transpose Dot A (A^T.A)
      float[][] atda = calculateATransposeA(A,nL,nR,M);
      
      //now to get the inverse
      return LinearAlgebra.getInverse(atda);
   }
   
   /**
    * Determines the matrix that would result by taking the product 
    * of transpose of the matrix "A" with the matrix "A".  The 
    * matrix "A" is defined such that the ith row and jth column 
    * in the matrix has the value i^j and is used to calculate the 
    * coefficients of the polynomial of best fit.
    * @param A  The matrix used in the computation.
    * @param nL The number of points to the left of the point being 
    *           processed that are used to approximate the point 
    *           being processed.
    * @param nR The number of points to the right of the point being 
    *           processed that are used to approximate the pont 
    *           being processed.
    * @param M  The order of the polynomial used to approximate the 
    *           data.  This is also equal to the highest moment that 
    *           is conserved.
    * @return   (Transpose of A)(A)
    */
   private float[][] calculateATransposeA(float[][] A, int nL, int nR, int M)
   {
      float[][] AT = LinearAlgebra.getTranspose(A);
      
      float[][] result = new float[M+1][M+1];
      
      for (int i=0; i<=M; i++)
         for (int j=0; j<=M; j++)
            result[i][j] = calculateATransposeA_at_ij(i,j, nL, nR);
      
      return result;
   }
   
   /**
    * Calculates the element in the ith row and jth column in the matrix 
    * that would result by taking the product of transpose of the 
    * matrix "A" with the matrix "A."  The matrix "A" is defined such that 
    * the ith row and jth column in the matrix has the value i^j and is 
    * used to calculate the coefficients of the polynomial of best fit.
    * @param i The row number (starting at 0).
    * @param j The column number (starting at 0).
    * @param nL The number of points to the left of the point being 
    *           processed that are used to approximate the point 
    *           being processed.
    * @param nR The number of points to the right of the point being 
    *           processed that are used to approximate the pont 
    *           being processed.
    * @return   The value at row i and column j.
    */
   private float calculateATransposeA_at_ij(int i, int j, int nL, int nR)
   {
      //the following equation describes how the equation 
      //used to compute Aij was derived.
      /*
       *             n            n
       *              R            R
       *            ----         ----
       *   T        \            \    i+j
       * {A . A}  =  |  A  A   =  |  k
       *        ij  /    ki kj   /
       *            ----         ----
       *           k=-n         k=-n
       *               L            L
       */
      int iPlusJ = i+j;
      float sum = 0;
      for (int k=-nL; k<=nR; k++)
         sum += Math.pow(k,iPlusJ);
      return sum;
   }
   
   //-----------=[ Methods that smooth the data using least-squares fitting]=----------------
   /**
    * Uses the long algorithm to smooth the data from all of the Data objects from the 
    * DataSet given.
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
   private Object performLeastSquaresSmoothing(DataSet new_ds, int nL, int nR, int M)
   {
      //first to test if the numbers entered are valid
      if (nR+nL<M)
         return new ErrorString("  The sum of the left and right points and 1 must be at least equal to the degree of the polynomial");
      
      int counter = 0;
      
      double[][] A = null;
      double[]   b = null;
      
      Data    currentData   = null;
      float   smoothedValue = Float.NaN;
      float[] copyOfYValues = null;
      float[] actualYValues = null;
      for (int i=0; i<new_ds.getNum_entries(); i++)
      {
         //now to get the current Data object
           currentData   = new_ds.getData_entry(i);
         //and its y values.  This is a copy of the array of y values
           copyOfYValues = currentData.getCopyOfY_values();
         //and this is a reference to the actual array of y values
           actualYValues       = currentData.getY_values();
         
         for (int yIndex = 0; yIndex<copyOfYValues.length; yIndex++)
         {
            counter++;
            
         	smoothedValue = getSmoothedValue(currentData.getX_values(), copyOfYValues,
         	                                   currentData.isHistogram(),
         	                                     yIndex,nL,nR,M);
            if (!Float.isNaN(smoothedValue))
               actualYValues[yIndex] = smoothedValue;
         }
      }
      
      return new_ds;
   }
   
   /**
    * Get the matrix "B" used in solving for a polynomial of best fit.  The 
    * elements in "B" are the y values used to are to be fit by the polynomial.  
    * @param yValues The y values used in the data.
    * @param yIndex  The index from <code>yValues</code> that is to be smoothed.
    * @param nL     The number of points to the left of the point being 
    *               processed that are used to approximate the point 
    *               being processed.
    * @param nR     The number of points to the right of the point being 
    *               processed that are used to approximate the pont 
    *               being processed.
    * @return       The matrix "B" from the equation Aa=B
    */
   private double[] getMatrixB(float[] yValues, int yIndex, int nL, int nR)
   {
      double[] b = new double[nR+nL+1];
      
      for (int i=0; i<b.length; i++)
         b[i] = yValues[yIndex-nL+i];
      return b;
   }

   /**
    * Get the x value corresponding the to y value at the 
    * index <code>yIndex</code>.  This method is needed because 
    * it returns the correct x value to use if the data is from 
    * a histogram or a function.
    * <p>
    * If the x and y values specified correspond to a histogram, 
    * the average of x values at the ends of the bin that the 
    * y value is in is returned.  Otherwise, if the x and y values 
    * correspond to a function, the x value at the same index of 
    * the y value is returned.
    * @param xValues     The x values used in the data.
    * @param yValues     The y values used in the data.
    * @param isHistogram True if the data is from a histogram and 
    *                    false if it is from a function.
    * @param yIndex      The index from <code>yValues</code> 
    *                    for which you want to get the 
    *                    corresponding x value.
    * @return            The corresponding x value or Float.NaN 
    *                    if yIndex is invalid.
    */
   private float getXValue(float[] xValues, float[] yValues, boolean isHistogram, int yIndex)
   {
      if (yIndex<0 || yIndex>=yValues.length || yIndex>=xValues.length)
      {
         System.out.println("Invalid paramter (yIndex="+yIndex+") in getXValue\n  Returning Float.NaN");
         return Float.NaN;
      }
      
      if (isHistogram)
      {
         if ((yIndex+1)<xValues.length)
            return (xValues[yIndex]+xValues[yIndex+1])/2.0f;
         else
         {
            System.out.println("Invalid paramter (yIndex="+yIndex+" !< "+(xValues.length-1)+") in getXValue\n  Returning Float.NaN");
            return Float.NaN;
         }
      }
      else
         return xValues[yIndex];
   }
   
   /**
    * Get the matrix "A" used in solving for a polynomial of best fit.
    * @param isHistogram True if the data is from a histogram and false 
    *                    if it is from a function.
    * @param M       The degree of the polynomial of best fit.
    * @param xValues     The x values used in the data.
    * @param yValues The y values used in the data.
    * @param yIndex  The index from <code>yValues</code> that is to be smoothed.
    * @param nL      The number of points to the left of the point being 
    *                processed that are used to approximate the point 
    *                being processed.
    * @param nR      The number of points to the right of the point being 
    *                processed that are used to approximate the pont 
    *                being processed.
    * @return        The matrix "B" from the equation Aa=B
    */
   private double[][] getMatrixA(float[] xValues, float[] yValues, boolean isHistogram, int yIndex, int nL, int nR, int M)
   {
      //the matrix is in the form [column][row]
      double[][] A = new double[nR+nL+1][M+1];
      
      //this holds the x value corresponding to the y value at 'yIndex'
      double currentX = Double.NaN;
      //now to have the index of the first y value be 'nL' indices back
      yIndex -= nL;
      
      for (int row=0; row<nR+nL+1; row++)
      {
         currentX = getXValue(xValues, yValues,isHistogram,yIndex);
         A[row][0] = 1;
         for (int column=1; column<(M+1); column++)
            A[row][column] = A[row][column-1]*currentX;

         yIndex++;
      }
      
      return A;
   }
   
   /**
    * Calculate the value of the smoothed polynomial at the x value <code>x</code>.
    * @param x            The x value for which the y value of the smoothed value is calculated.
    * @param coefficients The coefficients of the smoothing polynomial.
    * @param length       The number of values from <code>coefficients</code> that actually 
    *                     are the coefficients from the smoothing algorithm.  
    *                     <p>
    *                     <code>coefficients[0]</code> should be the first coefficient (the 
    *                     coefficient paried with x^0).
    *                     <p>
    *                     <code>coefficients[length-1]</code> should be the last coefficient (the 
    *                     coefficient paried with x^M (where M is the degree of the polynomial)).
    * @return             The y value of the smoothed polynomial at the x value <code>x</code>.
    */
   private double computeApproximateValueAt(float x, double[] coefficients, int length)
   {
      if (length >= 1)
      {
         double value    = coefficients[0];
         double currentX = x;
         for (int i=1; i<length; i++)
         {
            value += coefficients[i]*currentX;
            currentX *= x;
         }
         return value;
      }
      else
      {
         System.out.println("Invalid paramter in method computeApproximateValueAt()");
         System.out.println("  length="+length+" < 1");
         System.out.println("  Returning Float.NaN");
         return Float.NaN;
      }
   }
   
   /**
    * Get the smoothed value for the y value at the index <code>yIndex</code>.
    * @param xValues     The array of x values used to calculate the smoothed value.
    * @param yValues     The array of y values that are used calculate the smoothed value.
    * @param yIndex      The index of the y value from <code>yValues</code> that 
    *                    is to be smoothed.
    * @param isHistogram True if the x and y values are from a histogram and false if 
    *                    they are from a function.
    * @param nL          The number of points to the left of the point being 
    *                    processed that are used to approximate the point 
    *                    being processed.
    * @param nR          The number of points to the right of the point being 
    *                    processed that are used to approximate the pont 
    *                    being processed.
    * @param M           The order of the polynomial used to approximate the 
    *                    data.  This is also equal to the highest moment that 
    *                    is conserved.
    * @return            The smoothed value.
    */
   private float getSmoothedValue(float[] xValues, float[] yValues, boolean isHistogram, int yIndex, int nL, int nR, int M)
   {
      boolean leftCompensate = false;
      boolean rightCompensate = false;
      
      if (nL > yIndex)
      {
         leftCompensate = true;
         yIndex = nL;
      }
      else if (nR > yValues.length-1-yIndex)
      {
         rightCompensate = true;
         yIndex = yValues.length-1-nR;
      }
      
      //now to constuct matrix 'A'
      double[][] A = getMatrixA(xValues, yValues, isHistogram, yIndex, nL, nR, M);

      //now to construct matrix 'b'
      double[]   b = getMatrixB(yValues, yIndex, nL, nR);
      
      double returnedVal = LinearAlgebra.solve(A,b);
          
      if (returnedVal == Double.NaN)
      {
         System.out.println("Invalid parameter in getSmoothedValue()");
         System.out.println("  LinearAlgebra.solve() returned "+returnedVal);
         System.out.println("  Returning Float.NaN");         
         return Float.NaN;
      }
      if (leftCompensate)
         return (float)computeApproximateValueAt(getXValue(xValues, yValues, isHistogram, yIndex-nL),b,M+1);
      else if (rightCompensate)
         return (float)computeApproximateValueAt(getXValue(xValues, yValues, isHistogram, yIndex+nR),b,M+1);
      else
         return (float)computeApproximateValueAt(getXValue(xValues, yValues, isHistogram, yIndex),   b,M+1);
   }
}    
