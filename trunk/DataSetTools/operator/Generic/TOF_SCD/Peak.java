/*
 * File:  Peak.java
 *
 * Copyright (C) 2002, Peter Peterson
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
 * Contact : Peter Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           Argonne, IL 60439-4845
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 */
package DataSetTools.operator.Generic.TOF_SCD;

import java.io.*;
import java.text.DecimalFormat;

/**
 * Methods for getting information about atoms dependent on the
 * isotope.
 */

public class Peak{
    // instance variables
    private int    seqnum   = 0;
    private int    h        = 0;
    private int    k        = 0;
    private int    l        = 0;
    private float  x        = Float.NaN;
    private float  y        = Float.NaN;
    private float  z        = Float.NaN;
    private float  xcm      = 0f;
    private float  ycm      = 0f;
    private float  t        = 0f;
    private float  wl       = 0f;
    private int    ipkobs   = 0;
    private float  inti     = 0f;
    private float  sigi     = 0f;
    private int    reflag   = 0;
    private int    nrun     = 0;
    private int    detnum   = 0;
    private float  nearedge = 0;
    private float  detA     = 0f;
    private float  detD     = 0f;
    private float  chi      = 0f;
    private float  phi      = 0f;
    private float  omega    = 0f;

    /* --------------------Constructor Methods-------------------- */
    /**
     *  Create a peak
     */
    public Peak( ){
	super();
    }

    /**
     *  Create a peak specifing only the pixel position and intensity.
     *
     *  @param x      The pixel column the peak is in.
     *  @param y      The pixel row the peak is in.
     *  @param z      The time bin the peak is in.
     *  @param ipkobs The intesity of the peak.
     */
    public Peak( float x, float y, float z, int ipkobs){
	this();
	this.x(x);
	this.y(y);
	this.z(z);
	this.ipkobs(ipkobs);
    }

    /**
     *  Create a peak specifing only the pixel position, real
     *  position, and intensity.
     *
     *  @param seqnum The sequence number
     *  @param x      The pixel column the peak is in.
     *  @param y      The pixel row the peak is in.
     *  @param z      The time bin the peak is in.
     *  @param ipkobs The intesity of the peak.
     */
    public Peak( int seqnum, float x, float y, float z, int ipkobs){
	this(x,y,z,ipkobs);
	this.seqnum(seqnum);
    }

    /**
     *  Create a peak specifing only the pixel position, real
     *  position, and intensity.
     *
     *  @param seqnum The sequence number
     *  @param x      The pixel column the peak is in.
     *  @param y      The pixel row the peak is in.
     *  @param z      The time bin the peak is in.
     *  @param ipkobs The intesity of the peak.
     *  @param detnum The detector number.
     */
    public Peak( int seqnum, float x, float y, float z, 
		 int ipkobs, int detnum){
	this(seqnum,x,y,z,ipkobs);
	this.detnum(detnum);
    }

    /**
     *  Create a peak specifing only the pixel position, real
     *  position, and intensity.
     *
     *  @param seqnum The sequence number
     *  @param x      The pixel column the peak is in.
     *  @param y      The pixel row the peak is in.
     *  @param z      The time bin the peak is in.
     *  @param ipkobs The intesity of the peak.
     *  @param nrun   The run number.
     *  @param detnum The detector number.
     */
    public Peak( int seqnum, float x, float y, float z, 
		 int ipkobs, int nrun, int detnum){
	this(seqnum,x,y,z,ipkobs,detnum);
	this.nrun(nrun);
    }

    /**
     *  Create a peak specifing everything.
     *
     *  @param seqnum The sequence number
     *  @param h      h index.
     *  @param k      k index.
     *  @param l      l index.
     *  @param x      The pixel column the peak is in.
     *  @param y      The pixel row the peak is in.
     *  @param z      The time bin the peak is in.
     *  @param xcm    The horizontal position.
     *  @param ycm    The vertical position.
     *  @param wl     The wavelength.
     *  @param ipkobs The intesity of the peak.
     *  @param inti   The integrated intesity of the peak.
     *  @param sigi   The uncertainty in integrated intesity of the peak.
     *  @param reflag The reflection flag.
     *  @param nrun   The run number.
     *  @param detnum The detector number.
     */
    public Peak( int seqnum, int h, int k, int l, float x, float y, float z, 
		 float xcm, float ycm, float wl, int ipkobs, 
		 float inti, float sigi, int reflag, int nrun, int detnum){
	this(seqnum,x,y,z,ipkobs,nrun,detnum);
	this.h(h);
	this.k(k);
	this.l(l);
	this.xcm(xcm);
	this.ycm(ycm);
	this.wl(wl);
	this.inti(inti);
	this.sigi(sigi);
	this.reflag(reflag);
    }

    /* ------------------- accessor and mutator methods -------------------- */
    /**
     *  Accessor method for the sequence number.
     */
    public int seqnum(){
	return this.seqnum;
    }
    /**
     *  Mutator method for the sequence number.
     */
    public int seqnum(int SEQNUM){
	this.seqnum=SEQNUM;
	return this.seqnum();
    }

    /**
     *  Accessor method for h
     */
    public int h(){
	return this.h;
    }
    /**
     *  Mutator method for h
     */
    public int h(int H){
	this.h=H;
	return this.h();
    }

    /**
     *  Accessor method for k
     */
    public int k(){
	return this.k;
    }
    /**
     *  Mutator method for k
     */
    public int k(int K){
	this.k=K;
	return this.k();
    }

    /**
     *  Accessor method for l
     */
    public int l(){
	return this.l;
    }
    /**
     *  Mutator method for l
     */
    public int l(int L){
	this.l=L;
	return this.l();
    }

    /**
     *  Accessor method for the pixel column.
     */
    public float x(){
	return this.x;
    }
    /**
     *  Mutator method for the pixel column.
     */
    public float x(float X){
	this.x=X;
	return this.x();
    }

    /**
     *  Accessor method for the pixel row.
     */
    public float y(){
	return this.y;
    }
    /**
     *  Mutator method for the pixel row.
     */
    public float y(float Y){
	this.y=Y;
	return this.y();
    }
    /**
     *  Accessor method for the time bin.
     */
    public float z(){
	return this.z;
    }
    /**
     *  Mutator method for the time bin.
     */
    public float z(float Z){
	this.z=Z;
	return this.z();
    }

    /**
     *  Accessor method for the horizontal position.
     */
    public float xcm(){
	return this.xcm;
    }
    /**
     *  Mutator method for the horizontal position.
     */
    public float xcm(float XCM){
	this.xcm=XCM;
	return this.xcm();
    }
    /**
     * Mutator method for the horizontal position.
     */
    public float xcm( float ax, float bx){
	return 	this.xcm(ax*this.x() + bx);
    }

    /**
     *  Accessor method for the vertical position.
     */
    public float ycm(){
	return this.ycm;
    }
    /**
     *  Mutator method for the vertical position.
     */
    public float ycm(float YCM){
	this.ycm=YCM;
	return this.ycm();
    }
    /**
     *  Mutator method for the vertical position.
     */
    public float ycm(float ay, float by){
	return 	this.ycm(ay*this.y()+by);
    }

    /**
     *  Accessor method for the time of flight.
     */
    public float t(){
	return this.t;
    }
    /**
     *  Mutator method for the time of flight.
     */
    public float t(float T){
	this.t=T;
	return this.t();
    }
    /**
     *  Mutator method for the time of flight.
     */
    public float t(float T0, float T1){
	float deltaT=T1-T0;
	float remain=this.z%1f;

	return 	this.t(T0+remain*deltaT);
    }

    /**
     *  Accessor method for the wavelength.
     */
    public float wl(){
	return this.wl;
    }
    /**
     *  Mutator method for the wavelength.
     */
    public float wl(float WL){
	this.wl=WL;
	return this.wl();
    }
    /**
     *  Mutator method for the wavelength.
     */
    public float wl(float l1, float t0){
	float h=(float)(1E6*6.62606876E-34); // in kg*cm*A/us
	float m=(float)1.67492716E-27; // in kg
	float l2=this.detD();
	//System.out.print(wl+"("+l1+","+l2+","+t0+")=");

	// set the time to be the new time
	this.t(this.t()+t0);

	// if any of the values are not properly defined set the wl to
	// zero
	if( l1==0f || l2==0f || this.xcm()==0f 
	    || this.ycm()==0f || this.t()==0){
	    return this.wl(0f);
	}

	// calculate the corrected path length
	float l=(float)Math.pow((double)l2,2.0)
	    +(float)Math.pow((double)this.xcm(),2.0)
	    +(float)Math.pow((double)this.ycm(),2.0);
	l=l1+(float)Math.sqrt((double)l);

	//System.out.println(h*(this.t())/(m*l));
	return 	this.wl(h*(this.t())/(m*l));
    }

    /**
     *  Accessor method for the intensity.
     */
    public int ipkobs(){
	return this.ipkobs;
    }
    /**
     *  Mutator method for the intensity.
     */
    public int ipkobs(int IPKOBS){
	this.ipkobs=IPKOBS;
	return this.ipkobs();
    }

    /**
     *  Accessor method for the integrated intensity
     */
    public float inti(){
	return this.inti;
    }
    /**
     *  Mutator method for the integrated intensity
     */
    public float inti(float INTI){
	this.inti=INTI;
	return this.inti();
    }

    /**
     *  Accessor method for the uncertainty in integrated intensity
     */
    public float sigi(){
	return this.sigi;
    }
    /**
     *  Mutator method for the uncertainty in integrated intensity
     */
    public float sigi(float SIGI){
	this.sigi=SIGI;
	return this.sigi();
    }

    /**
     *  Accessor method for the reflection flag.
     */
    public int reflag(){
	return this.reflag;
    }
    /**
     *  Mutator method for the reflection flag.
     */
    public int reflag(int REFLAG){
	this.reflag=REFLAG;
	return this.reflag();
    }

    /**
     *  Accessor method for the run number.
     */
    public int nrun(){
	return this.nrun;
    }
    /**
     *  Mutator method for the run number.
     */
    public int nrun(int NRUN){
	this.nrun=NRUN;
	return this.nrun();
    }

    /**
     *  Accessor method for the detector number.
     */
    public int detnum(){
	return this.detnum;
    }
    /**
     *  Mutator method for the detector number.
     */
    public int detnum(int DETNUM){
	this.detnum=DETNUM;
	return this.detnum();
    }

    /**
     *  Accessor method for the pixel distance from edge.
     */
    public float nearedge(){
	return this.nearedge;
    }
    /**
     *  Mutator method for the pixel distance from edge.
     */
    public float nearedge(float NEAREDGE){
	this.nearedge=NEAREDGE;
	return this.nearedge();
    }

    /**
     *  Mutator method for the pixel distance from edge.
     */
    public float nearedge(int MINX, int MAXX,int MINY, int MAXY, int MINZ, int MAXZ){
	float dx=Math.abs((float)MINX-this.x);
	float dy=Math.abs((float)MINY-this.y);
	float dz=Math.abs((float)MINY-this.z);
	float min=1000.0f;
	
	if(dx>Math.abs((float)MAXX-this.x)){
	    dx=Math.abs((float)MAXX-this.x);
	}
	if(dy>Math.abs((float)MAXY-this.y)){
	    dy=Math.abs((float)MAXY-this.y);
	}
	if(dz>Math.abs((float)MAXZ-this.z)){
	    dz=Math.abs((float)MAXZ-this.z);
	}

	if(dx<min) min=dx;
	if(dy<min) min=dy;
	if(dz<min) min=dz;

	//System.out.println("("+this.x+","+this.y+","+this.z+")"+min);

	this.nearedge(min);
	return this.nearedge();
    }

    /**
     *  Accessor method for the detector angle
     */
    public float detA(){
	return this.detA;
    }
    /**
     *  Mutator method for the detector angle
     */
    public float detA(float DETA){
	this.detA=DETA;
	return this.detA();
    }

    /**
     *  Accessor method for the detector distance
     */
    public float detD(){
	return this.detD;
    }
    /**
     *  Mutator method for the detector distance
     */
    public float detD(float DETD){
	this.detD=DETD;
	return this.detD();
    }

    /**
     *  Accessor method for the sample chi
     */
    public float chi(){
	return this.chi;
    }
    /**
     *  Mutator method for the sample chi
     */
    public float chi(float CHI){
	this.chi=CHI;
	return this.chi();
    }

    /**
     *  Accessor method for the sample phi
     */
    public float phi(){
	return this.phi;
    }
    /**
     *  Mutator method for the sample phi
     */
    public float phi(float PHI){
	this.phi=PHI;
	return this.phi();
    }

    /**
     *  Accessor method for the sample omega
     */
    public float omega(){
	return this.omega;
    }
    /**
     *  Mutator method for the sample omega
     */
    public float omega(float OMEGA){
	this.omega=OMEGA;
	return this.omega();
    }

    /**
     *  Mutator method for the sample orientation
     */
    public void sample_orientation(float CHI, float PHI, float OMEGA){
	this.chi(CHI);
	this.phi(PHI);
	this.omega(OMEGA);
    }


    /* --------------------- clone method --------------------- */
    /**
     *  Produce a copy of the object.
     */
    public Object clone(){
	Peak peak=new Peak(seqnum,h,k,l,x,y,z,xcm,ycm,wl,ipkobs,inti,sigi,reflag,nrun,detnum);
	peak.nearedge(this.nearedge());
	peak.t(this.t());
	peak.detA(this.detA());
	peak.detD(this.detD());
	peak.sample_orientation(this.chi(),this.phi(),this.omega());
	return peak;
    }

    /* ------------------- toString method -------------------- */
    /**
     *  Format the toString method to be the full version of how it
     *  can be specified. This is in the Schultz format.
     */
    public String toString( ){
	String rs="";	

	DecimalFormat df_se_tw=new DecimalFormat("###0.00");
	DecimalFormat df_ei_fo=new DecimalFormat("##0.0000");
	DecimalFormat df_ni_tw=new DecimalFormat("#####0.00");

	rs="3"+format(seqnum,6)+format(h,4)+format(k,4)+format(l,4);
	if(Float.isNaN(x)){
	    rs=rs+"     "+x;
	}else{
	    rs=rs+format(df_se_tw.format(x),7);
	}
	if(Float.isNaN(y)){
	    rs=rs+"     "+y;
	}else{
	    rs=rs+format(df_se_tw.format(y),7);
	}
	if(Float.isNaN(z)){
	    rs=rs+"     "+z;
	}else{
	    rs=rs+format(df_se_tw.format(z+1),7);
	}
	rs=rs+format(df_se_tw.format(xcm),7)
	    +format(df_se_tw.format(ycm),7)
	    +format(df_ei_fo.format(wl),8)
	    //+format(df_ei_fo.format(t),8)
	    +format(ipkobs,6)
	    +format(df_ni_tw.format(inti),9)
	    +format(df_ni_tw.format(sigi),9)
	    +format(reflag,5)+format(nrun,6)+format(detnum,3);

	return rs;
    }

    /**
     * Format an integer by padding on the left.
     */
    static private String format(int number, int length){
	String rs=new Integer(number).toString();
	while(rs.length()<length){
	    rs=" "+rs;
	}
	return rs;
    }
    
    /**
     * Format a string by padding on the left.
     */
    static private String format(String rs, int length){
	while(rs.length()<length){
	    rs=" "+rs;
	}
	return rs;
    }

    /* --------------------------- MAIN ---------------------------------- */
    /**
     *  The main program for testing purposes.
     */
    public static void main( String[] args ){
	Peak me=new Peak();
	System.out.println(me);

	me=new Peak(1f,2f,3f,4);
	System.out.println(me);

	me=new Peak(1,2f,3f,4f,5);
	System.out.println(me);

	me=new Peak(1,2f,3f,4f,5,6,7);
	System.out.println(me);

    }
}
