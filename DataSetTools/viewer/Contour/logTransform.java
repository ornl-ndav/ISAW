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
import java.lang.Math.*;

public class logTransform  implements Transform
  {
    double pstart,pend,ustart,uend;
    double pstart0,pend0,ustart0,uend0;
    float intensity;
    double mu,bu,mp,bp;
    double a,b,K;
    int sgn;
   /** Transforms [ustart,uend] to [pstart, pend] as follows:<P>
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
      
    // System.out.println("in logTransform "+pstart+","+pend+","+ustart+","+uend);


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

  /** Translate user value to its physical value<P>
  * Value out of range get mapped to their corresponding extreme values
  */

   public double getTransP(double u)
     {if( u < ustart) 
         return pstart;
      if( u > uend) 
        {
         return pend;
        }
        double x = bu+ mu*(u-ustart);
        double y = a*Math.log( x+ b)/Math.log(10)+K;
        //System.out.println("xy="+x+","+y+","+a+","+K);
        if( y >=100)
          return pend;
        if( y <=0)
          return pstart;
        
      return (y-bp)/mp;
	
      }

  /** Translate physical value to its user value<P>
  * Value out of range get mapped to their corresponding extreme values
  */

   public double getTransU(double p)
      { 
        if( p < pstart)
         return ustart;
        if( p>pend)
          return uend;
        double y = mp*p+bp;
        double x= Math.pow(10.0,(y-K)/a) -b;
        double u = (x-bu)/mu +ustart;
        if( u<ustart)
          return ustart;
        if( u > uend)
          return uend;
        return u;
       }

   private void calc()
    { 
      mu = 99.9/(uend-ustart);
      bu =.1f;
      mp = 100/(pend-pstart);
      bp=0;
      float u = (1 - intensity/100);
      u = u*u*u;
       a = 30+ 70*u;
       b = 0 ;
       K = 37-137*u;
     }

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
     logTransform lt = new logTransform( 0.,100., .1, 100., 
                    ( new Integer(args[0])).intValue());
      logTransform lt1=new logTransform( 0.,10., 20., 50., 0);
     logTransform lt2=new logTransform( 0.,10., 20., 50., 100);
   /*  for( int i=0; i<50; i++)
      { double f=20.+30.*i/50.;
       // System.out.println(f+","+lt1.getTransP(f)+","+lt.getTransP(f)+","+lt2.getTransP(f)+
       //       ","+(f-20.)*10./30.);
        System.out.println( f+","+ lt.getTransU(lt.getTransP(f))+","+
                  lt1.getTransU(lt1.getTransP(f)));
      }
   */
    //double f = (new Double( args[0])).doubleValue();
   for( double x = .1; x<100; x+=10)
      System.out.println( x+"  "+lt.getTransP(x));
   }	
   }
