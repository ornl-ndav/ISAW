/*
 * File:  TimeMap.java
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
 *           Argonne, IL 60439-4845
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2002/09/26 21:45:05  pfpeterson
 *  Now properly determines time maps.
 *
 *  Revision 1.3  2002/08/06 21:26:15  pfpeterson
 *  Added hooks for returning an XScale associated with the TimeMap.
 *
 *  Revision 1.2  2002/07/26 19:02:44  pfpeterson
 *  Fixed bug in code. Now produces gsas compliant time maps.
 *
 *  Revision 1.1  2002/07/25 19:28:41  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.gsastools;

import DataSetTools.dataset.XScale;
import DataSetTools.util.Format;

public class TimeMap{
    private static String TIMEMAP = GsasUtil.TIMEMAP;
    private static String EOL     = "\n";

    private float tmax;
    private int nval;
    private int nrec;
    private int[] chan;
    private float[] time;
    private float[] dt;
    private int mapno;
    private float clockwidth;
    private XScale xscale;

    /**
     * Creates a time map from the given XScale. This assumes that the
     * XScale is a histogram and in time of flight. The time map
     * number and clock width are given default values.
     */
    public TimeMap(XScale xscale){
        this.xscale=xscale;
        tmax=xscale.getEnd_x();
        nval=0;
        clockwidth=1000f;
        mapno=0;

        // initialize temporary arrays
        float[] orig_xval      = xscale.getXs();
        float[] xval      = new float[orig_xval.length];
        int[]   temp_chan = new int  [xval.length];
        float[] temp_time = new float[xval.length];
        float[] temp_dt   = new float[xval.length];

        // convert the xvals to ns
        for( int i=0 ; i<xval.length ; i++ ){
            xval[i]=(float)((int)(1000f*orig_xval[i]));
            if( (int)(xval[i]%10)==9 ){
                xval[i]+=1;
            }else if( (int)(xval[i]%10)==8 ){
                xval[i]+=2;
            }else if( (int)(xval[i]%10)==2 ){
                xval[i]-=2;
            }else if( (int)(xval[i]%10)==1 ){
                xval[i]-=1;
            }
        }

        // set up the first value in the temporary array
        float dx=(xval[1]-xval[0]);
        temp_chan[0]=1;
        temp_time[0]=xval[0];
        temp_dt[0]=dx;
        nval++;

        // fill the temporary arrays
        for( int i=0 ; i<xval.length-1 ; i++ ){
            if((xval[i+1]-xval[i])!=dx){
                dx=(xval[i+1]-xval[i]);
                temp_chan[nval]=i+1;
                temp_time[nval]=xval[i];
                temp_dt[nval]=dx;
                nval++;
            }
        }

        // copy the temporary information into the instance variables
        chan = new int[nval];
        time = new float[nval];
        dt   = new float[nval];
        for( int i=0 ; i<nval ; i++ ){
            chan[i] = temp_chan[i];
            time[i] = temp_time[i];
            dt[i]   = temp_dt[i];
        }

        // determine the clock width
        this.clockwidth=increaseClock(time,dt);
        for( int i=0 ; i<nval ; i++ ){
            time[i] = time[i]/this.clockwidth;
            dt[i]   = dt[i]/this.clockwidth;
        }

        // determine the number of records/lines when written out
        nrec=(int)(((float)nval+1f)/10f+0.9f);
    }

    /**
     * Creates a time map from the given XScale with the specified map
     * number. The clock width is given a default value. See
     * TimeMap(XScale) for full description.
     */
    public TimeMap(XScale xscale, int mapnum){
        this(xscale);
        this.setMapNum(mapnum);
    }

    /**
     * Creates a time map from the given XScale with the specified map
     * number and clock width. See TimeMap(XScale) for full
     * description.
     */
    public TimeMap(XScale xscale, int mapnum, float clockwidth){
        this(xscale,mapnum);
        this.clockwidth = clockwidth;
    }

    /**
     * This mimics a clone method in creating a new copy of the
     * specified TimeMap.
     */
    public TimeMap(TimeMap timemap){
        this.tmax       = timemap.tmax;
        this.nval       = timemap.nval;
        this.nrec       = timemap.nrec;
        this.mapno      = timemap.mapno;
        this.clockwidth = timemap.clockwidth;
        this.chan       = new int  [this.nval];
        this.time       = new float[this.nval];
        this.dt         = new float[this.nval];

        for( int i=0 ; i<nval ; i++ ){
            this.chan[i] = timemap.chan[i];
            this.time[i] = timemap.time[i];
            this.dt[i]   = timemap.dt[i];
        }
    }

    /**
     * Return the map number.
     */
    public int getMapNum(){
        return this.mapno;
    }

    /**
     * Set the map number after construction.
     */
    public void setMapNum(double mapnum){
        this.mapno=(int)mapnum;
    }

    /**
     * Create the header line of the time map.
     */
    public String getHeader(){
        StringBuffer sb=new StringBuffer(60);

        sb.append(TIMEMAP).append("    ")
            .append(this.mapno).append("  ")
            .append(Format.integer(this.nval*3+1,3)).append("  ")
            .append(Format.integer(this.nrec,3)).append(" ")
            .append(TIMEMAP).append("   ")
            .append(Format.real(this.clockwidth,7,1)).append("  ");

        return sb.toString();
    }

    /**
     * Get a XScale that this time map could be made from. If the
     * TimeMap was initialized with an XScale then a reference to that
     * XScale is returned.
     */
    public XScale getXScale(){
        if(this.xscale!=null) return this.xscale;
        XScale xscale=null;

        return xscale;
    }

    /**
     * Overrides the default clone() method as specified by Object.
     */
    public Object clone(){
        return new TimeMap(this);
    }

    /**
     * Overrides the default toString() method as specified by
     * Object. In this case it returns the full time map in the format
     * specified in the GSAS manual.
     */
    public String toString(){
        StringBuffer sb=new StringBuffer(80*nrec+1);

        // add the header line
        sb.append(Format.string(getHeader(),80,false)).append(EOL);

        // put in the time map triplets
        int colcount=0;
        for( int i=0 ; i<nval ; i++ ){
            if(colcount>=80){
                sb.append(EOL);
                colcount=0;
            }
            sb.append(Format.integer(chan[i],8));
            colcount+=8;
            if(colcount>=80){
                sb.append(EOL);
                colcount=0;
            }
            sb.append(Format.integer(time[i],8));
            colcount+=8;
            if(colcount>=80){
                sb.append(EOL);
                colcount=0;
            }
            sb.append(Format.integer(dt[i],  8));
            colcount+=8;
        }

        // add the final time boundary
        if(colcount>=80){
            sb.append(EOL);
            colcount=0;
        }
        sb.append(Format.integer(tmax*1000/this.clockwidth,8));
        sb.append(Format.string(EOL,81-8-colcount));
                  
        // return a nice string
        return sb.toString();
    }

    /**
     * Overrides the default equals method in Object. This is built to
     * return false as quickly as possible.
     */
    public boolean equals(Object other){
        TimeMap othermap=null;
        // comparing with null returns false;
        if(other==null)return false;
        
        // the other object should be a TimeMap
        if(other instanceof TimeMap)
            othermap=(TimeMap)other;
        else
            return false;
        
        // if they are the same return true
        if(this==othermap)return true;

        // then just compare all of the fields
        if( this.tmax       != othermap.tmax       ) return false;
        if( this.nval       != othermap.nval       ) return false;
        if( this.nrec       != othermap.nrec       ) return false;
        if( this.mapno      != othermap.mapno      ) return false;
        if( this.clockwidth != othermap.clockwidth ) return false;
        for( int i=0 ; i<nval ; i++ ){
            if( this.chan[i] != othermap.chan[i] ) return false;
            if( this.time[i] != othermap.time[i] ) return false;
            if( this.dt[i]   != othermap.dt[i]   ) return false;
        }

        // they must be the same
        return true;
    }

    private float increaseClock(float[] t, float[] dt){
        int tcount=0;
        int dtcount=0;
        float newclock=0f;

        // try a 125ns clockwidth
        newclock=125f;
        for( int i=0 ; i<t.length ; i++ ){
            if(  (t[i]%newclock)==0f ) tcount++;
            if( (dt[i]%newclock)==0f ) dtcount++;
        }
        if( tcount==dtcount && tcount!=0 ){
            /*for( int i=0 ; i<t.length ; i++ ){
              t[i]=t[i]/newclock;
              dt[i]=dt[i]/newclock;
              }*/
            return newclock;
        }

        tcount=0;
        dtcount=0;

        // try a 100ns clockwidth
        newclock=100f;
        for( int i=0 ; i<t.length ; i++ ){
            if(  (t[i]%newclock)==0f ) tcount++;
            if( (dt[i]%newclock)==0f ) dtcount++;
        }
        if( tcount==dtcount && tcount!=0 ){
            /*for( int i=0 ; i<t.length ; i++ ){
              t[i]=t[i]/newclock;
              dt[i]=dt[i]/newclock;
              }*/
            return newclock;
        }

        /*for( int i=0 ; i<t.length ; i++ ){
          t[i]  =  t[i]*1000f;
          dt[i] = dt[i]*1000f;
          }*/
        return 1000f;
    }
}
