/*
 * File:  GsasUtil.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2002/11/27 23:15:00  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/08/06 21:25:22  pfpeterson
 *  New methods to get an xscale and units from an XInfo.
 *
 *  Revision 1.1  2002/07/25 19:28:40  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.gsastools;

import DataSetTools.dataset.*;
import DataSetTools.util.Format;
import DataSetTools.util.SharedData;

/**
 * This class contains only static methods intended to deal with
 * "common" gsas tests, methods, constants.
 */
public class GsasUtil{
    // string constants for bintypes
    public static String COND    = "COND";
    public static String CONS    = "CONST";
    public static String CONQ    = "CONQ";
    public static String SLOG    = "SLOG";
    public static String TIMEMAP = "TIME_MAP";

    // string constants for types (how errors are written)
    public static String STD = "   ";
    public static String ESD = "ESD";

    // constants for what the x-axis is
    public static int TIME = 1;
    public static int Q    = 2;
    public static int D    = 3;

    // constants for labeling things
    public static String BANK = "BANK";

    /**
     * Return a constant for the type of units for the given axis
     */
    public static int getUnits(String units){
	units=units.toLowerCase();

        if(units.indexOf("time")>=0){
            return TIME;
        }else if(units.indexOf("inverse angstroms")>=0){
            return Q;
        }else if(units.indexOf("angstroms")>=0){
            return D;
        }

        return 0;
    }

    /**
     * Deal with the time units by determining a scale factor to
     * mutiple times by in order to get to micro-seconds.
     */
    public static float getTimeScale(String units){
        units=units.toLowerCase();
        if(units.indexOf("ns")>0)
            return 1000f;
        if(units.indexOf("us")>0)
            return 1f;
        else if(units.indexOf("ms")>0)
            return 0.001f;
        else
            return 0f;
    }

    /**
     * Determine the TYPE. This is done by calculating the percent
     * difference between sqrt(I) and sigmaI. If it is more than
     * 0.0001 different then the bank is labeled ESD.
     */
    public static String getType(Data data){
        float tol=0.999f;

        float[] I  = data.getCopyOfY_values();
        float[] dI = data.getCopyOfErrors();

	for( int i=0 ; i<dI.length ; i++ ){
	    if((tol*(float)Math.sqrt((double)I[i])<dI[i])||(dI[i]==0.0f)){
                // do nothing
            }else{
                System.out.println((float)Math.sqrt(I[i])+">"+dI[i]);
                return ESD;
	    }
	}

        return STD;
    }
    /**
     * Determine if the XScale given is constant binning. If it is
     * constant the spacing is returned, otherwise zero.
     */
    public static float getStepSize(XScale xscale){
        if(xscale instanceof UniformXScale)
            return (float)((UniformXScale)xscale).getStep();
        
        float tol=0f;
        float dX=0f;
        float[] x=xscale.getXs();
        
        if(x.length>2) dX=x[1]-x[0];

        for( int i=1 ; i<x.length ; i++ ){
            if(Math.abs(x[i]-x[i-1]-dX)>tol)return 0f;
        }

        return dX;
    }

    /**
     * Create a bank header line in gsas format. The result must be
     * padded out to 80 characters for proper use in gsas.
     */
    public static String getBankHead( int banknum, XInfo info){
        StringBuffer sb=new StringBuffer(80);
        
        sb.append(BANK).append(" ").append(Format.integer(banknum,6))
            .append(" ").append(info);

        return sb.toString();
    }

    /**
     * Create an XScale from a given XInfo object. This is originally
     * written to deal with loading gsas powder data.
     */
    public static XScale getXScale( XInfo info ){
        XScale xscale = null;
        float start   = 0f;
        float end     = 0f;
        int   numX    = 0;

        if( info.bintype().equals(CONS) || info.bintype().equals(CONQ) 
            || info.bintype().equals(COND) ){
            start = info.coef1();
            numX  = info.nchan()+1;
            end   = start+(float)info.nchan()*info.coef2();
            xscale=new UniformXScale(start,end,numX);
        }else if(info.bintype().equals(TIMEMAP)){
            SharedData.addmsg("Cannot currently read in files with time map");
            TimeMap timemap=info.timemap();
            if(timemap==null) return null;
            xscale=timemap.getXScale();
            //System.out.println("XSCALE:"+xscale);
        }

        return xscale;
    }

    public static String getUnit(XInfo info){
        String unit=null;
        if( info.bintype().equals(CONS) || info.bintype().equals(TIMEMAP) 
                                        || info.bintype().equals(SLOG) ){
            unit="Time(us)";
        }else if( info.bintype().equals(CONQ) ){
            unit="Inverse Angstroms";
        }else if( info.bintype().equals(COND) ){
            unit="Angstroms";
        }

        return unit;
    }
}
