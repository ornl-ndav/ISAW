/*
 * File:  ScaleWrap.java
 *
 * Copyright (C) 2003 Ruth Mikkelson, Alok Chatterjee
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.1  2004/04/26 13:37:00  rmikk
 * Initial Checkin
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import DataSetTools.operator.Wrappable;
import java.util.*;
import DataSetTools.util.*;
import java.io.*;
import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.MathTools.Functions.*;
import gov.anl.ipns.Util.SpecialStrings.*;

/**
 *  This class has code for an operator to calculate the scale factor for sand
 *  runs.
 */

public class ScaleWrap implements Wrappable {
 

  /**
    *  Returns "Scale", the name used to invoke the associated operator 
    *  in scripts
    */
  public String getCommand(  ) {
    return "Scale";
  }

  
  public String getDocumentation(  ) {
   
    StringBuffer s = new StringBuffer(  );
       s.append( "@overview This operator calculates the scale factor for sand");
 
       s.append( "runs." );
       s.append( "@algorithm The 1D Q analysis from a standard run and a run ");
       s.append( " from an initial set up " );
       s.append( "from a new or updated instrument are compared. The Data in" );
       s.append( " these runs are fit to a polynomial over an internal range");
       s.append( " of relevant Q values. The corresponding y values ");
       s.append( " corresponding to equally spaced x-values are then summed ");
       s.append( "up in both cases." );
       s.append( " The ratio of these sums is the scale factor" );
       s.append( "@param filenameSt The name of the file with the Q " );
       s.append( "distribution of data from a standard run");
       s.append( "@param filenameCo The name of the file with the Q " );
       s.append( "distribution of data from a run from a new instrument ");
       s.append( "or setup" );
       s.append( "@param Qmin The minimum Q value to use for fitting/");
       s.append( "comparing" );
       s.append( "@param Qmax The maximum Q value to use for fitting/");
       s.append( "comparing" );
       s.append( "@param degree The degree of the polynomial used to fit ");
       s.append( "the data" );
       s.append( "@return A Vector with degree +3 elements as follows: (1)" );
       s.append( " The scale factor. (2) The number of Q values used for"); 
       s.append( " calculating the sums. (3) The coefficients of the fit"); 
       s.append( " polynomial starting with the degree 0 coefficient");
       s.append( "@error IO Errors");
       return s.toString(  );

  }

  //  public String filenameSt ="C:\\Ruth\\batesnist_jun2001.dat";
    public LoadFileString filenameSt =new LoadFileString("C:/ISAW/batesnist.dat");
    public LoadFileString filenameCo = new LoadFileString("C:/ISAW/sn17.dat");
    public float Qmin = .03f;
    public float Qmax = .1f;
    public int degree = 3;

  
  public Object calculate(  ) {
    
    float[] xvalsT,
            yvalsT,
            errsT,
            xvals,
            yvals,
            errs;
    try{
     FileInputStream finSt = new FileInputStream( filenameSt.toString());
     FileInputStream finCo = new FileInputStream( filenameCo.toString());
     TextFileReader tex1 = new TextFileReader( finSt);
     TextFileReader tex2 = new TextFileReader( finCo);
    
     tex1.read_line();
     tex2.read_line();
     boolean done = false;
     linkFloat Act=null;
     int n=0;

     // Read in the data from the standard run. They are first stored
     //    in a linked list, then extracted from the linked list and
     //    stored in the arrays xvals, yvals and errrs
     while( !done){
       float f1 = tex1.read_float(), 
             f2= tex1.read_float(),
             f3= tex1.read_float();
       if( f1 >=0){
       Act = new linkFloat( f1, Act);
       Act = new linkFloat( f2, Act);
       Act = new linkFloat( f3, Act);

       n++;
       }else
         done = true;
     
     }
  
     xvals = new float[n]; 
     yvals=new float[n]; 
     errs= new float[n];
   
  
     for( int i=n-1; i >=0; i--){
       errs[i] = Act.value;
       Act = Act.next;
       yvals[i] = Act.value;
       Act = Act.next;
       xvals[i] = Act.value;
       Act = Act.next;
 
     }


     // Read in the data from the run on the new instrument. They are first stored
     //    in a linked list, then extracted from the linked list and
     //    stored in the arrays xvalsT, yvalsT and errrsT
     done = false;
     linkFloat exp=null;
     int m=0;
     while( !done){
       float f1 = tex2.read_float(), 
       f2= tex2.read_float(),
       f3= tex2.read_float();
       if( f1 >=0){
       exp = new linkFloat( f1, exp);
       exp = new linkFloat( f2, exp);
       exp = new linkFloat( f3, exp);
 
       m++;
       }else
         done = true;
      
     }
      
     xvalsT = new float[m]; 
     yvalsT=new float[m]; 
     errsT= new float[m];
 
     for( int i=m-1; i>=0; i--){
       errsT[i] = exp.value;
       exp = exp.next;
       yvalsT[i] = exp.value;
       exp = exp.next;
       xvalsT[i] = exp.value;
       exp = exp.next;

     }
    }catch( Throwable ss){

      return new ErrorString( ss.toString());
    }  


    // ------------------ Fit a polynomial to the data---------------
    // First find indicies in the Standard and Sample arrays
    int npoints = 20;
    int polyfitindex1 = Adjust(Arrays.binarySearch( xvals, Qmin));
 
    int polyfitindex2 = Adjust(Arrays.binarySearch( xvals, Qmax));
    int polyfitindex1T = Adjust(Arrays.binarySearch( xvalsT, Qmin));
 
    int polyfitindex2T = Adjust(Arrays.binarySearch( xvalsT, Qmax));

  
    //Copy the portion of the arrays to one starting at index 0
    double[] xvalsA = new double[ polyfitindex2-polyfitindex1+1],
             yvalsA =new double[ polyfitindex2-polyfitindex1+1];
 
    for( int i= polyfitindex1; i<=polyfitindex2; i++){
       xvalsA[i-polyfitindex1]= xvals[i];
       yvalsA[i-polyfitindex1]= yvals[i];
    }  

  
    double[] xvalsTA = new double[ polyfitindex2T-polyfitindex1T+1],
             yvalsTA =new double[ polyfitindex2T-polyfitindex1T+1];

    for( int i= polyfitindex1T; i<=polyfitindex2T; i++){
       xvalsTA[i-polyfitindex1T]= xvalsT[i];
       yvalsTA[i-polyfitindex1T]= yvalsT[i];
    }  
  

    //------------ Calculate Polynomial Coefficients --------------
    //                Standard
    double[] coeff = new double[degree + 1];
    double errr= CurveFit.Polynomial(xvalsA,
                            yvalsA,coeff, false);


    //                Sample
    double[] coeffT = new double[degree + 1];
    double errrT= CurveFit.Polynomial(xvalsTA,
                            yvalsTA,coeffT, true);


    // -------------- "Integrate" y values for Standard and for Sammpe-----
    double Sum=0, 
           SumT=0;
    for( int i = 0; i < 20; i++){
       float xx = Qmin +i*(Qmax-Qmin)/(float)npoints;
       Sum += EvalPoly( coeff,xx);
       SumT +=EvalPoly(coeffT,xx);

    }
  
    Vector V = new Vector();
    V.addElement(new Float(Sum/SumT));
    V.addElement( new Integer(npoints));
    for( int i = 0; i<= degree; i++)
       V.addElement( new Float( (float)(coeffT[i]))); 
    return V;
  }


 double EvalPoly( double coeff[] , float xx){
    double powxx = 1;
    double Res = 0;
    for( int i=0; i < coeff.length; i++){
       Res += coeff[i]*powxx;
       powxx *=xx;
     }
    return Res;
 }

 private int Adjust( int i){

  if( i >=0)
    return i;
  return -i-1;
 }

 /**
   * List is maintained as a linked list instead of a Vector
   */
 class linkFloat{
   public float value;
   public linkFloat next;
   
   public linkFloat( float value, linkFloat next){
      this.value = value;
      this.next = next;
     
   }
  }
}
