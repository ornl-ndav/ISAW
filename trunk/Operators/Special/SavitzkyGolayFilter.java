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
 * Revision 1.1  2004/07/28 20:29:27  kramer
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
 * 
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
    * @param ds         The DataSet to process.
    * @param nL         The number of points to the left of the point 
    *                   being processed that are used to approximate the 
    *                   point being processed.
    * @param nR         The number of points to the right of the point 
    *                   being processed that are used to approximate the 
    *                   point being processed.
    * @param M          The order of the polynomial used to approximate 
    *                   the data.  This is also equal to the highest 
    *                   moment that is conserved.
    * @pram createNewDS True if this operator should create a new 
    *                   DataSet from the data modified.
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
    * Replaces all y values with Savitzky-Golay smoothed values.
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
      
      System.out.println("The elapsed time was "+timer.elapsed());
      return returnedObject;
   }
   
   //-----------=[ Methods that do the quick procedure for smoothing the data]=------------
   /**
    * Uses the quick algorithm to smooth the data from all of the Data objects from the 
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
   private Object performQuickSmoothing(DataSet new_ds, int nL, int nR, int M)
   {
      float[] c = calculateCoefficients(nL,nR,M);
      
      if (c != null)
      {
         Data    data    = null;
         float[] f       = null;
         int     n       = -nL;
         int     i       = 0;
         float   sum     = 0;
         
         for (int dataNum=0; dataNum<new_ds.getNum_entries(); dataNum++)
         {
            data = new_ds.getData_entry(dataNum);
            f = data.getY_values();
                        
            for (i=0; i<f.length; i++)
            {
               sum = 0;
               for (n = -nL; n<=nR; n++)
                  if ((i+n)>=0 && (i+n)<f.length)
                     sum += c[n+nL]*f[i+n];
               
               f[i] = sum;
            }
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
    * Get the coefficients used to smooth the data.
    * @param nL The number of points to the left of the point being 
    *           processed that are used to approximate the point 
    *           being processed.
    * @param nR The number of points to the right of the point being 
    *           processed that are used to approximate the pont 
    *           being processed.
    * @param M  The order of the polynomial used to approximate the 
    *           data.  This is also equal to the highest moment that 
    *           is conserved.
    * @return   The coefficients used to smooth the data or null if 
    *           they cannot be determined.
    */
   private float[] calculateCoefficients(int nL, int nR, int M)
   {
      float[][] B = calculateInverseOfATransposeA(nL,nR,M);
      
      if (B != null)
      {
         float[] coefArr = new float[nL+nR+1];
         for (int n=-nL; n<=nR; n++)
            coefArr[n+nL] = getCoefficient(B,n,nL,nR,M);
         return coefArr;
      }
      else
         return null; //because the ATransposeDotA is not invertable
   }
   
   /**
    * Get the nth coefficient used to smooth the data.
    * @param B  The matrix returned by {@link 
    *           calculateInverseOfATransposeDotA(int nL, int nR, int M) 
    *           calculateInverseOfATransposeDotA(int nL, int nR, int M)}.
    * @param n  The number of the desired corefficient. (the 
    *           first coefficient is found at n=-nL and the last 
    *           coefficient is found at n=nR).
    * @param nL The number of points to the left of the point being 
    *           processed that are used to approximate the point 
    *           being processed.
    * @param nR The number of points to the right of the point being 
    *           processed that are used to approximate the pont 
    *           being processed.
    * @param M  The order of the polynomial used to approximate the 
    *           data.  This is also equal to the highest moment that 
    *           is conserved.
    * @return   The nth coefficient.
    */
   private float getCoefficient(float[][] B, int n, int nL, int nR, int M)
   {
      float sum = 0;
      for (int m=0; m<=M; m++)
         sum += B[0][m]*Math.pow(n,m);
      return sum;
   }
   
   /**
    * Determines the matrix that would result by taking the inverse of 
    * th matrix that would result by taking the product 
    * of transpose of the matrix 'A' with the matrix 'A'.
    * <p>
    * 'A' is the name of the matrix used to determine coefficients used 
    * for smoothing the data.  This method creates the matrix 'A' which 
    * is defined as Aij = i^j (ie the element in the ith row and jth 
    * column of 'A' is equal to i^j).
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
    * of transpose of the supplied matrix with the supplied matrix.
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
    * supplied matrix with the supplied matrix.
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
   private Object performLeastSquaresSmoothing(DataSet new_ds, int nL, int nR, int M)
   {
      //first to test if the numbers entered are valid
      if (nR+nL<M)
         return new ErrorString("  The sum of the left and right points and 1 must be at least equal to the degree of the polynomial");
      
      int counter = 0;
      
      double[][] A = null;
      double[]   b = null;
      
      //TODO This might be wrong
      Data    currentData   = null;
      float   smoothedValue = Float.NaN;
      float[] yValues       = null;
      for (int i=0; i<new_ds.getNum_entries(); i++)
      {
         //now to get the current Data object
         currentData   = new_ds.getData_entry(i);
         //and its y values
         yValues       = currentData.getY_values();
         
         for (int yIndex = 0; yIndex<yValues.length; yIndex++)
         {
            counter++;
            
         	smoothedValue = getSmoothedValue(currentData,yIndex,nL,nR,M);
            if (!Float.isNaN(smoothedValue))
            	yValues[yIndex] = smoothedValue;
         }
      }

      System.out.println("getSmoothedValue() should have been called "+counter+" times");
      
      return new_ds;
   }
      
   private double[] getBMatrix(Data data, int yIndex, int nL, int nR)
   {
      double[] b = new double[nR+nL+1];
      
      for (int i=0; i<b.length; i++)
         b[i] = data.getY_values()[yIndex-nL+i];
      return b;
   }

   /**
    * Get the x value corresponding the to y value at the 
    * index <code>yIndex</code>.
    * @param data   The Data object from which the x and y 
    *               values are acquired.
    * @param yIndex The index from (data.getY_values()) 
    *               for which you want to get the 
    *               corresponding x value.
    * @return       The corresponding x value or Float.NaN 
    *               if yIndex is invalid.
    */
   private float getXValue(Data data, int yIndex)
   {
      if (yIndex<0 && yIndex>=data.getY_values().length)
         return Float.NaN;
      
      if (data.isHistogram())
      {
         if ((yIndex+1)<data.getY_values().length)
            return (data.getX_values()[yIndex]+data.getX_values()[yIndex+1])/2.0f;
         else
            return Float.NaN;
      }
      else
         return data.getX_values()[yIndex];
   }
   
   private double[][] getAMatrix(Data data, int yIndex, int nL, int nR, int M)
   {
      //the matrix is in the form [column][row]
      double[][] A = new double[nR+nL+1][M+1];
      
      //this holds the x value corresponding to the y value at 'yIndex'
      double currentX = Double.NaN;
      //now to have the index of the first y value be 'nL' indices back
      yIndex -= nL;
      
      for (int row=0; row<nR+nL+1; row++)
      {
         currentX = getXValue(data,yIndex);
         A[row][0] = 1;
         for (int column=1; column<(M+1); column++)
            A[row][column] = A[row][column-1]*currentX;

         yIndex++;
      }

      return A;
   }
   
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
         return Float.NaN;
   }
      
   private float getSmoothedValue(Data data, int yIndex, int nL, int nR, int M)
   {
      boolean leftCompensate = false;
      boolean rightCompensate = false;
      
      if (nL > yIndex)
      {
         leftCompensate = true;
         yIndex = nL;
      }
      else if (nR > data.getY_values().length-1-yIndex)
      {
         rightCompensate = true;
         yIndex = data.getY_values().length-1-nR;
      }
      
      //now to constuct matrix 'A'
      double[][] A = getAMatrix(data, yIndex, nL, nR, M);

      //now to construct matrix 'b'
      double[]   b = getBMatrix(data, yIndex, nL, nR);
      
      double returnedVal = LinearAlgebra.solve(A,b);
          
      if (returnedVal == Double.NaN)
         return Float.NaN;
      if (leftCompensate)
         return (float)computeApproximateValueAt(getXValue(data,yIndex-nL),b,M+1);
      else if (rightCompensate)
         return (float)computeApproximateValueAt(getXValue(data,yIndex+nR),b,M+1);
      else
         return (float)computeApproximateValueAt(getXValue(data,yIndex),   b,M+1);
   }
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
}    
/*
      Data    currentData   = null;
      float   smoothedValue = Float.NaN;
      float[] yValues       = null;
      for (int i=0; i<new_ds.getNum_entries(); i++)
      {
         currentData   = new_ds.getData_entry(i);
         yValues       = currentData.getY_values();
         for (int yIndex = 0; yIndex<yValues.length; yIndex++)
         {
            smoothedValue = getSmoothedValue(currentData,yIndex,nL,nR,M);
            if (smoothedValue != Float.NaN)
               yValues[yIndex] = smoothedValue;
         }
      }
      return new_ds;
   }
   
   private float getSmoothedValue(Data data, int currentYIndex, int nL, int nR, int M)
   {
      double[] b = generateYMatrix(data,currentYIndex,nL,nR);
      if (b == null)
         return Float.NaN;
      double[][] A = generateCoefficientMatrix(data,currentYIndex,nL,nR,M);
      if (A == null)
         return Float.NaN;
      
      LinearAlgebra.solve(A,b);
      
      if (b.length >= 1)
         return (float)b[0];
      else
         return Float.NaN;
   }
   
   private double[] generateYMatrix(Data data, int currentYIndex, int nL, int nR)
   {
      double[] yArr = null;
      int i = 0;
      try
      {
         if (data.getY_values() == null)
            return null;
         if (currentYIndex<0 || currentYIndex>=data.getY_values().length)
            return null;
      
         nL = getSafeNL(currentYIndex,nL);
         nR = getSafeNR(data.getY_values(),currentYIndex,nR);
      
         yArr = new double[nL+nR+1];
         for (i=0; i<yArr.length; i++)
            yArr[i] = data.getY_values()[currentYIndex-nL+i];
      }
      catch (ArrayIndexOutOfBoundsException ex)
      {
         System.out.println("At index="+i);
         if (yArr == null)
            System.out.println("yArr is null");
         else
            System.out.println("yArr.length="+yArr.length);
         System.out.println("data.getY_values().length="+data.getY_values().length);
      }
      
      return yArr;
   }
   
   private double[][] generateCoefficientMatrix(Data data, int currentYIndex, int nL, int nR, int M)
   {
      if (data.getX_values() == null)
         return null;
      if (currentYIndex<0 || currentYIndex>=data.getY_values().length)
         return null;
      
      float currentXValue = Float.NaN;
      if (data.isHistogram())
      {
         //this is the number of bins (intervals in the histogram)
         int numOfBins = data.getX_values().length - 1;
         nL = getSafeNL(currentYIndex,nL);
         nR = Math.min(nR, numOfBins-currentYIndex-1);
         currentXValue = (data.getX_values()[currentYIndex]+data.getX_values()[currentYIndex+1])/2.0f;
      }
      else
      {
         //if the Data object is not a histogram, its a function
         //then the x value at an index "i" corresponds to the y 
         //value at the index "i".
         nL = getSafeNL(currentYIndex,nL);
         nR = getSafeNR(data.getX_values(),currentYIndex,nR);
         currentXValue = data.getX_values()[currentYIndex];
      }
      
      if (nL+nR<M)
      {
         int remainder = (M-nR-nL)%2;
         int extra     = 0;
         if (remainder != 0) //then M-nR-nL is odd
            extra = 1;
         int deltaN = (M-nR-nL+extra)/2;
         
         nL += deltaN;
         nR += deltaN;
      }
      
      double[][] coeffArr = new double[nL+nR+1][M+1];
      for (int row=0; row<nL+nR+1; row++)
         for (int column=0; column<M+1; column++)
            coeffArr[row][column] = (float)Math.pow(currentXValue-nL+row,column);
      
      return coeffArr;
   }
   
   private int getSafeNL(int currentIndex, int nL)
   {
      return Math.min(currentIndex,nL);
   }
   
   private int getSafeNR(float[] valueArr, int currentIndex, int nR)
   {
      return Math.min(valueArr.length-currentIndex+1,nR);
   }
*/
