/*
 * File:  logTransform.java
 *
*
 * Copyright (C) 2002, Ruth Mikkelson
 *
 * This software is used with NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
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
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 * Modified:
 *
 *  $Log$
 *  Revision 1.12  2004/07/17 16:06:28  dennis
 *  Removed some unreachable code.
 *
 *  Revision 1.11  2004/07/16 18:51:07  rmikk
 *  Improved Ranges on the intensity scale
 *
 *  Revision 1.10  2004/01/24 22:22:24  bouzekc
 *  Removed unused imports and local variables.
 *
 *  Revision 1.9  2003/10/15 03:56:36  bouzekc
 *  Fixed javadoc errors.
 *
 *  Revision 1.8  2003/09/04 16:09:57  rmikk
 *  Improved range for intensities
 *
 *  Revision 1.7  2003/08/11 22:11:32  rmikk
 *  Improves the color response to match the other views better.
 *
 *  Revision 1.6  2002/11/27 23:24:30  pfpeterson
 *  standardized header
 *
 *  Revision 1.5  2002/08/23 13:48:58  rmikk
 *  -Eliminated reporting a 0 delta value for  requested ranges
 *
 *  Revision 1.4  2002/08/01 22:12:39  rmikk
 *  Changed the transform algorithm so the readout is
 *    lighter longer
 *
 *  Revision 1.3  2002/07/15 22:14:01  rmikk
 *  Added the intensity to the log transform
 *
 *  Revision 1.2  2002/07/15 14:38:58  rmikk
 *  Fixed an error caused by a misunderstanding of a method.
 *    Now peaks are white
 *
 *  Revision 1.1  2002/07/12 21:14:43  rmikk
 *  Initial check in
 *
 */


package DataSetTools.viewer.Contour;
import gov.noaa.pmel.sgt.*;
import gov.noaa.pmel.util.*;

public class logTransform  implements Transform
  {
    double pstart,pend,ustart,uend;
    double pstart0,pend0,ustart0,uend0;
    float intensity;
    double mu,bu,mp,bp,sg;
    double a,b,K;
    int sgn;
   /** 
    * Transforms [ustart,uend] to [pstart, pend] as follows:
    *
    * <br><br>
   *   p = a*log( u+b) +K, where b is intensity -min(ustart,uend) and
   *  getTransU( pstart) = ustart and getTransu(pend) = uend.
   */
   public logTransform( double pstart, double pend, double ustart, 
        double uend, int intensity) 
    {this.pstart=pstart;
     this.pend=pend;
     this.ustart=ustart;
     this.uend=uend;
      pstart0=pstart;
      pend0=pend;
      ustart0=ustart;
      uend0=uend;
     
     setIntensity( intensity);

    }
   /** Sets the intensity to a value between .1 and 20
   *
   */
   public void setIntensity( int intensity)
     { if( intensity <0)
        this.intensity = 0;
       if( intensity > 100)
          intensity = 100;
       
       this.intensity = intensity;
       calc();
     }
   /** Sets the physical range
   */
   public void setRangeP(double p1,
                      double p2)
    {pstart =p1;
     pend = p2;

       calc();
    }
  /** Sets the physical range
   */
    public void setRangeP(Range2D prange)
    {pstart =prange.start;
     pend = prange.end;

       calc();
    }

   /** Gets the physical range
   */
   public Range2D getRangeP()
    {return new Range2D( pstart, pend);
     }

   /** Sets the user range
   */
   public void setRangeU(double u1,
                      double u2)
    {ustart=u1;
     uend= u2;

       calc();
     }

   /** Sets the user range
   */
   public void setRangeU(Range2D urange)
    {ustart = urange.start;
     uend = urange.end;

       calc();
     }

   /** Gets the physical range
   */
   public Range2D getRangeU()
    {return new Range2D( ustart,uend);
     }

  /** 
   * Translate user value to its physical value.
   *
   * <br>
  * Value out of range get mapped to their corresponding extreme values
  */
   int k=0;
   public double getTransP(double u)
     {if( u < ustart) 
         return pstart;
      if( u > uend) 
        {
         return pend;
        }
        double x = bu+ mu*(u-ustart);
       double y;
       if( sg*x+b < 0)
          if( sg >0)
             y= -1;
          else 
             y = 101;
        else  
          y = a*Math.log( sg*x+ b)/Math.log(10)+K;
      
        
       
         
        /*if( y >=100)
          return pend;
        if( y <=0)
          return pstart;
          */
       y = gety(x);
       
       double uu=pstart+(y-bp)/mp;
      
       if( uu < pstart) return pstart;
       if( uu > pend) return pend;
       return uu;
	
      }

  /** 
   * Translate physical value to its user value.
   *
   * <br>
  * Value out of range get mapped to their corresponding extreme values
  */

   public double getTransU(double p)
      { 
        if( p < pstart)
         return ustart;
        if( p>pend)
          return uend;
        double y = mp*(p-pstart)+bp;
        double x= sg*(Math.pow(10.0,(y-K)/a) -b);
        x = getx(y);
        double u = (x-bu)/mu +ustart;
          
        
        if( u<ustart)
          return ustart;
        if( u > uend)
          return uend;
        return u;
       }
  //p:[0:100]-> u:[0:100]
  private void calc(){
    mu = 100/(uend-ustart);   //x = mu(u-ustart)+bu
       bu =0;
       mp = 100/(pend-pstart);   //y = mp(p-pstart)+bp
       bp=0;
    float intensity = 100-this.intensity;
    if( intensity <= 50){
    
    lowerA =  Math.pow(10.,-30.) +intensity*.9/50;
    upperA = lowerA + 1;
    a= 100*Math.log(10.)/(Math.log(upperA)-Math.log(lowerA));
    K= -a*Math.log(lowerA)/Math.log(10.);
    }else{
     intensity =100-intensity;
     lowerA =  Math.pow(10.,-30.) +intensity*.9/50;
     upperA = lowerA + 1;
     a= 100*Math.log(10.)/(Math.log(upperA)-Math.log(lowerA));
     K= -a*Math.log(lowerA)/Math.log(10.);
    }
    
  }
  double lowerA, upperA;
  /**
   * Returns the y value(0->100) corresponding to the x value(0:100)
   * @param x  x value, normalized u value
   * @return   yvalue -normalized p value
   */
  private double gety(double x){
     float intensity =100-this.intensity;
     if( intensity  <= 50 ){
          double xx = lowerA + x/100.*(upperA-lowerA);
          double yy=a*Math.log(xx)/Math.log(10.)+K;
          if( yy < 0) return 0;
          if( yy > 100) return 100.;
          return yy;
     }else{
        x = 100-x;
       double xx = lowerA + x/100.*(upperA-lowerA);
       double yy =a*Math.log(xx)/Math.log(10.)+K;
       yy =-yy+100;
       if( yy<0) return 0;
       if( yy > 100) return 100;
       return yy;
     }   
  }
  
  /**
   * Returns the x value(0->100) corresponding to the y value(0:100)
   * @param x  y value, normalized p value
   * @return   x value -normalized u value
   */
  private double getx( double y){
    float intensity =100-this.intensity;
    if( intensity  <= 50 ){
       double xx= Math.pow(10.0,(y-K)/a);
       double x = (xx-lowerA)*100/(upperA-lowerA);
       
       if( x < 0) return 0;
       if( x > 100) return 100.;
         return x;
    }else{
      y = -y+100;
      double xx= Math.pow(10.0,(y-K)/a);
      double x = (xx-lowerA)*100/(upperA-lowerA);
      x = 100-x;
      if( x<0) return 0;
      if( x > 100) return 100;
      return x;
    }
  }
  
/*  Old version ... keep for now  
  private void calc2(){
    mu = 100/(uend-ustart);   //x = mu(u-ustart)+bu
    bu =0;
    mp = 100/(pend-pstart);   //y = mp(p-pstart)+bp
    bp=0;
    
                           // y=a*log(sg*x+b)+K
    if( intensity < 50){
       sg=1;
       b= Math.pow(10.0,-30.0)+intensity*Math.pow(10.0,-.02)/50.;
       a = 100*Math.log(10)/(Math.log(100+b)-Math.log(b));
       K =  -a*Math.log(b)/Math.log(10.0);
    }else{
       sg = -1;
       b = 100+Math.pow(10.,-30.) -(1-Math.pow(10.0,-.02)/50.)*
                 (100-intensity);
       if( b <100)
          a = 100*Math.log(10)/(Math.log(-100+b)-Math.log(b));
       else
          a = 100*Math.log(10)/(-30-Math.log(b));
      
       K =  -a*Math.log(b)/Math.log(10.0);
    }
  }
  //from mapped to 0 to 100(p)   to mapped to .1 to 100(u)
   private void calc1()
    { 
      mu = 99.9/(uend-ustart);   //x = mu(u-ustart)+bu
      bu =.1f;
      mp = 100/(pend-pstart);   //y = mp(p-pstart)+bp
      bp=0;
      //float u = intensity;     // y = a*Math.log( x+ b)/Math.log(10)+K;
      a = 332.2;
      if( intensity > 50)
        a = 4.7418*(intensity-50)+332.3;
      if( intensity <50)
        b = intensity*2;
      else b = 100;
      if( intensity < 60) K= 100-a*Math.log(100+b)/Math.log(10);
      else K = -a*Math.log(.1+b)/Math.log(10);
       //a = 30+ 70*u;
      //b= 0 ;
      // K = 37-137*u;
   }
*/

   /** Unused
   */
   public void addPropertyChangeListener(java.beans.PropertyChangeListener listener)
    {}

   /** Unused
   */
   public void removePropertyChangeListener(java.beans.PropertyChangeListener listener)
     {}

    public static void main( String[] args )
   { System.out.println("Here");
     logTransform lt = new logTransform( 0.,100., 0, 100., 
                    ( new Integer(args[0])).intValue());
      //logTransform lt1=new logTransform( 0.,10., 20., 50., 0);
     //logTransform lt2=new logTransform( 0.,10., 20., 50., 100);
   /*  for( int i=0; i<50; i++)
      { double f=20.+30.*i/50.;
       // System.out.println(f+","+lt1.getTransP(f)+","+lt.getTransP(f)+","+lt2.getTransP(f)+
       //       ","+(f-20.)*10./30.);
        System.out.println( f+","+ lt.getTransU(lt.getTransP(f))+","+
                  lt1.getTransU(lt1.getTransP(f)));
      }
   */
    //double f = (new Double( args[0])).doubleValue();
   for( double x = 0; x<=100; x+=10){
   
      double y =lt.getTransP(x);
      double xx = lt.getTransU(y);
      System.out.println( x+"  "+y+","+xx);
   }
   }	
   }
