/*
 * File:  XInfo.java
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
 *  Revision 1.2  2002/08/06 21:27:09  pfpeterson
 *  Added constructor which takes a gsas powder file bank header.
 *
 *  Revision 1.1  2002/07/25 19:28:42  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.gsastools;

import DataSetTools.dataset.*;
import DataSetTools.util.*; 
import java.awt.*;
import java.io.*;
import java.lang.Character;
import javax.swing.*;
import java.text.DateFormat;
import DataSetTools.math.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.RunfileRetriever;

/**
 * This class is intended to contain the bank header information for
 * GSAS powder data files. Most of the method are accessor methods and
 * do not have documentation since their names are descriptive.
 */
public class XInfo{
    // some static constants for easing usage
    private static String COND    = GsasUtil.COND;
    private static String CONS    = GsasUtil.CONS;
    private static String CONQ    = GsasUtil.CONQ;
    private static String TIMEMAP = GsasUtil.TIMEMAP;

    private static String STD     = GsasUtil.STD;
    private static String ESD     = GsasUtil.ESD;

    private static int TIME = 1;
    private static int Q    = 2;
    private static int D    = 3;

    // instance variables
    private String  bintype;
    private float   coef1;
    private float   coef2;
    private float   coef3;
    private float   coef4;
    private int     nxchan;
    private int     nchan;
    private int     nrec;
    private String  type;
    private boolean usetimemap;
    private TimeMap timemap;

    /**
     * The main constructor that does most of the work. If this
     * constructor is used not all information for the Xscale that is
     * needed by gsas is recorded. Use the other constructor to have
     * the proper value for the number of records and the type set.
     */
    public XInfo(XScale xscale, String units){
        // get the number of channels right away
        this.nxchan=xscale.getNum_x();
        this.nchan=nxchan-1;
        // initialize all of the coefficients to zero
        this.coef1=0f;
        this.coef2=0f;
        this.coef3=0f;
        this.coef4=0f;
        // initialize some of the other stuff to null
        this.nrec=0;
        this.type=null;
        this.usetimemap=false;
        this.bintype="";

        // get the units and time scale
        int x_units=GsasUtil.getUnits(units);
	float scale=0.0f;
        if(x_units==TIME) scale=GsasUtil.getTimeScale(units);

        // determine if this is a constant stepping
        this.coef2=GsasUtil.getStepSize(xscale);
        //this.coef2=0f; // this forces using a time map
        if(this.coef2>0f){
            this.coef1=xscale.getStart_x();
            if(scale>0f){
                this.coef1*=scale;
                this.coef2*=scale;
            }
            if(x_units==TIME){
                this.bintype=CONS;
            }else if(x_units==Q){   // not supported by gsas yet
                this.bintype=CONQ;  // not supported by gsas yet
            }else if(x_units==D){   // not supported by gsas yet
                this.bintype=COND;  // not supported by gsas yet
            }else{
                SharedData.addmsg("Cannot create appropriate XInfo Object");
                return;
            }
            return;
        }

        // otherwise it better be a time-map
        if(x_units==TIME){
            this.bintype=TIMEMAP;
            this.usetimemap=true;
            this.timemap=new TimeMap(xscale);
            this.coef1=this.timemap.getMapNum();
        }else{
            SharedData.addmsg("Cannot create appropriate XInfo Object");
            return;
        }

	return;
    } // end of constructor

    /**
     * The prefered constructor that does little work. By the
     * additional parameter of type the constructor can set the number
     * of records and the type making XInfo.toString() be more
     * completely what is needed for a gsas bank header.
     */
    public XInfo(XScale xscale, String units, String type){
        this(xscale,units);
        this.type=type;
        if(type.equals(STD)){
            nrec=(int)((float)nchan/10f+0.9f);
        }else{
            nrec=(int)((float)nchan/5f+0.9f);
        }
    } // end of constructor

    /**
     * A constructor that takes a gsas bank header as a string.
     */
    public XInfo(String bankhead){
        this.bintype="invalid";
        this.coef1=0f;
        this.coef2=0f;
        this.coef3=0f;
        this.coef4=0f;
        this.nxchan=0;
        this.nchan=0;
        this.nrec=0;
        this.type="invalid";
        this.usetimemap=false;
        this.timemap=null;

        StringBuffer sb=new StringBuffer(bankhead.trim());
        //System.out.println("SB0:"+sb);
        Integer tempI=null;
        String  tempS=null;
        int end=0;
        // pull off the bank number if necessary
        if(sb.toString().startsWith(GsasUtil.BANK)){
            sb.delete(0,4);
            StringUtil.trim(sb);
            StringUtil.getInt(sb);
        }
        //System.out.println("SB1:"+sb);

        // determine the number of channels
        this.nchan=StringUtil.getInt(sb);
        //System.out.println("SB2:"+sb);
        
        // determine the number of records
        this.nrec=StringUtil.getInt(sb);
        //System.out.println("SB3:"+sb);

        // get the bintype
        tempS=StringUtil.getString(sb);
        if(tempS.startsWith("CONS")) tempS=GsasUtil.CONS;
        this.bintype=tempS;
        //System.out.println("SB4:"+sb);
        
        // get the coefs
        this.coef1=StringUtil.getFloat(sb);
        this.coef2=StringUtil.getFloat(sb);
        this.coef3=StringUtil.getFloat(sb);
        this.coef4=StringUtil.getFloat(sb);
        
        // get the type
        if(sb.length()<=0){
            this.type=STD;
        }else{
            this.type=sb.toString();
        }

    }

    public String bintype(){
        return new String(this.bintype);
    }
    public float coef1(){
        return this.coef1;
    }
    public float coef2(){
        return this.coef2;
    }
    public float coef3(){
        return this.coef3;
    }
    public float coef4(){
        return this.coef4;
    }
    public int nchan(){
        return this.nchan;
    }
    public int nrec(){
        return this.nrec;
    }
    public String type(){
        return new String(this.type);
    }
    /**
     * Returns the timemap (reference, not copy) if the XScale is
     * described by one. Otherwise returns null.
     */
    public TimeMap timemap(){
        if(this.usetimemap){
            return this.timemap;
        }else{
            return null;
        }
    }

    /**
     * Sets the time map number of the timemap of the xscale. Returns
     * true if sucessful (there is a time map) and false if not (no
     * time map description).
     */
    public boolean setTimeMapNum(double mapnum){
        if(!this.usetimemap)
            return false; // nothing to do if not using a time map

        this.timemap.setMapNum(mapnum);
        this.coef1=this.timemap.getMapNum();
        return true;
    }

    /**
     * Overrides the toString method specified by Object. This method
     * is inteded to be the majority of what is needed for a gsas bank
     * header.
     */
    public String toString(){
        StringBuffer sb=new StringBuffer(80);
        
        if(nrec>0) sb.append(Format.integer(nchan,7)+" ")
                     .append(Format.integer(nrec,7)+" ");
        sb.append(Format.string(this.bintype,8)).append(" ");
        if(this.usetimemap)
            sb.append(Format.integer(this.coef1,4));
        else
            sb.append(this.coef1);
        sb.append(" ");
        if(this.type.equals(STD)){
            if(this.coef2!=0f){
                sb.append(this.coef2).append(" ");
                if(this.coef3!=0f){
                    sb.append(this.coef3).append(" ");
                    if(this.coef4!=0f) sb.append(this.coef4).append(" ");
                }
            }
        }else{
            if(!this.bintype.equals(TIMEMAP))
                sb.append(this.coef2).append(" ")
                    .append(this.coef3).append(" ")
                    .append(this.coef4).append(" ");
        }
        if(this.type!=null) sb.append(this.type);

        return sb.toString();
    } // end of toString()

    /**
     * Overrides the equals method specified by Object. This method
     * simply compares all of the instance variables for equality.
     */
    public boolean equals( Object other ){
        XInfo other_info=null;
        // comparing with null returns false
        if(other==null) return false;

        // the other object better be an XInfo
        if(other instanceof XInfo)
            other_info=(XInfo)other;
        else
            return false;

        // if they are the same return true
        if(this==other_info)return true;

        // then just compare all of the fields
        if( this.coef1      != other_info.coef1      ) return false;
        if( this.coef2      != other_info.coef2      ) return false;
        if( this.coef3      != other_info.coef3      ) return false;
        if( this.coef4      != other_info.coef4      ) return false;
        if( this.nxchan     != other_info.nxchan     ) return false;
        if( this.nchan      != other_info.nchan      ) return false;
        if( this.nrec       != other_info.nrec       ) return false;
        if( this.usetimemap != other_info.usetimemap ) return false;
        if(! this.bintype.equals(other_info.bintype) ) return false;
        if(! this.type.equals(other_info.type)       ) return false;
        if(this.usetimemap)
            if(! this.timemap.equals(other_info.timemap) ) return false;

        // they must be the same
        return true;
    }
}
