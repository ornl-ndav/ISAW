/*
 * File: SCD_LogUtils.java 
 *
 * Copyright (C) 2002-2006, Peter Peterson, Ruth Mikkelson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * Some of this work was supported by the National Science Foundation under
 * grant number DMR-0218882
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.2  2008/01/29 19:45:49  rmikk
 * Replaced Peak by IPeak
 *
 * Revision 1.1  2006/12/19 05:16:28  dennis
 * Collection of static methods used by the various versions of the
 * SCD integrate operators.  These particular methods were factored
 * out of the Integrate_new class.
 *
 *
 */

package Operators.TOF_SCD;

import DataSetTools.operator.Generic.TOF_SCD.*;
import gov.anl.ipns.Util.Numeric.*;


/**
 *  This class contains common static methods shared by the various
 *  integrate operators.
 */

public class SCD_LogUtils
{
  
  public static String formatInt(double num){
    return formatInt(num,3);
  }

  public static String formatInt(double num, int width){
    return Format.integer(num,width);
  }

  public static String formatFloat(double num){
    StringBuffer text=new StringBuffer(Double.toString(num));
    int index=text.toString().indexOf(".");
    if(index<0){
      text.append(".");
      index=text.toString().indexOf(".");
    }
    text.append("00");
    text.delete(index+3,text.length());

    while(text.length()<7)
      text.insert(0," ");

    return text.toString();
  }

  public static void formatRange(int[] rng, StringBuffer log){
    if(log==null) return;
    final int MAX_LENGTH=14;

    if(log.length()>MAX_LENGTH)
      log.delete(MAX_LENGTH,log.length());
    log.append("  "+formatInt(rng[0])+" "+formatInt(rng[2])+"  "
               +formatInt(rng[1])+" "+formatInt(rng[3]));
  }

  public static void addLogHeader( StringBuffer log, IPeak peak )
  {
    // add some information to the log file (if necessary)
    if(log!=null){
      log.append("\n******************** hkl = "+formatInt(peak.h())+" "
                 +formatInt(peak.k())+" "+formatInt(peak.l())
                 +"   at XYT = "+formatInt(peak.x())+" "+formatInt(peak.y())
                 +" "+formatInt(peak.z())+" ********************\n");
      log.append("Layer  T   maxX maxY  IPK     dX       dY      Ihkl     sigI"
                 +"  I/sigI   included?\n");
    }
  }


  public static void addLogSlice( StringBuffer log,
                                   int          layer,
                                   int          cenZ,
                                   int          cenX,
                                   int          cenY,
                                   int          slice_peak,
                                   int          minX,
                                   int          maxX,
                                   int          minY,
                                   int          maxY,
                                   float        sliceI,
                                   float        slice_sigI,
                                   String       included,
                                   boolean      border_peak )
  {
    if ( log != null )
    {
      log.append( formatInt(layer)              +
                  "   "  + formatInt(cenZ)      +
                  "  "  + formatInt(cenX)       +
                  "  "  + formatInt(cenY)       +
                  "   " + formatInt(slice_peak) +
                  "  "  + formatInt(minX)       +
                  " "   + formatInt(maxX)       +
                  "  "  + formatInt(minY)       +
                  " "   + formatInt(maxY)       +
                  " " + formatFloat(sliceI)     +
                  "  " + formatFloat(slice_sigI) );
      if ( slice_sigI != 0 )
        log.append( " " + formatFloat(sliceI/slice_sigI) );
      else
        log.append( " " + formatFloat(0) );

      log.append( "      " + included );
      if ( border_peak )
        log.append(" *BP" + "\n");
      else
        log.append("\n");
    }
  }



  public static void addLogPeakSummary( StringBuffer log,
                                        float        Itot,
                                        float        sigItot )
  {
    if ( log != null )
    {
      log.append("***** Final       Ihkl = "+formatFloat(Itot)+"       sigI = "
                 +formatFloat(sigItot)+"       I/sigI = ");
      if(sigItot>0f)
        log.append(formatFloat(Itot/sigItot));
      else
        log.append(formatFloat(0f));
      log.append(" *****\n");
    }
  }

  
} 
