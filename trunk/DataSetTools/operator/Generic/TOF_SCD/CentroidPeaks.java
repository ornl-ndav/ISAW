/*
 * File:  CentroidPeaks.java 
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
 *           Intense Pulsed Neutron Source
 *           Argonne National Laboratory
 *           Argonne, IL 60439-4845
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import DataSetTools.util.SharedData;
import DataSetTools.retriever.RunfileRetriever;
import java.util.*;
import java.util.Vector;
import java.lang.reflect.Array;
import java.text.DecimalFormat;

/** 
 * This operator is a small building block of an ISAW version of
 * A.J.Schultz's PEAKS program. This program takes a list of peaks and
 * calculates their centers using a centroid method.
 */
public class CentroidPeaks extends GenericTOF_SCD{
    private static final String     TITLE                 = "Centroid Peaks";
    private static final int        time_notice_frequency = 20;
    private static final SharedData shared                = new SharedData();
    private              int        run_number            = -1;


 /* ------------------------ Default constructor ------------------------- */ 
 /**
  *  Creates operator with title "Centroid Peaks" and a default list of
  *  parameters.
  */  
    public CentroidPeaks()
    {
	super( TITLE );
    }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Creates operator with title "Centroid Peaks" and the specified list
  *  of parameters. The getResult method must still be used to execute
  *  the operator.
  *
  *  @param  data_set    DataSet to find peak in
  *  @param  peaks       Vector of peaks. Normally created by FindPeaks.
  */
    public CentroidPeaks( DataSet data_set, Vector peaks){
	this(); 
	parameters = new Vector();
	addParameter( new Parameter("Histogram", data_set) );
	addParameter( new Parameter("Vector of Peaks", peaks) );
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "CentroidPeaks", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "CentroidPeaks";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters. This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Histogram",       DataSet.EMPTY_DATA_SET ) );
    addParameter( new Parameter("Vector of Peaks", new Vector())            );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return If successful, this operator prints out a list of x,y,time
  *  bins and intensities.
  */
  public Object getResult()
  {
    DataSet data_set = (DataSet)(getParameter(0).getValue());
    Vector  peaks    = (Vector) (getParameter(1).getValue());
    Vector  cpeaks   = new Vector();

    Peak peak=new Peak();
    for( int i=0 ; i<peaks.size() ; i++ ){
	peak=(Peak)peaks.elementAt(i);
	float[][][] surround=makeSurround(data_set,peak);
	peak=centroid(peak,surround);
    }

    return peaks;
  }

 /* --------------------------- makeSurround ----------------------------- */ 
    private float[][][] makeSurround( DataSet data, Peak peak){
	float[][][] surround={{{0,0,0},{0,0,0},{0,0,0}},
			      {{0,0,0},{0,0,0},{0,0,0}},
			      {{0,0,0},{0,0,0},{0,0,0}}};
	int column = (int)peak.x();
	int row    = (int)peak.y();
	int time   = (int)peak.z();
	surround[1][1][1]=(float)peak.ipkobs();

	int x;
	int y;
	Data spectrum;
	DetInfoListAttribute detI;
	DetectorInfo det;
	float[] intens;
	for( int i=0 ; i<data.getNum_entries() ; i++ ){
	    spectrum=data.getData_entry(i);
	    detI=(DetInfoListAttribute)
		spectrum.getAttribute(Attribute.DETECTOR_INFO_LIST);
	    det=((DetectorInfo[])detI.getValue())[0];
	    x=det.getColumn();
	    y=det.getRow();
	    if(       x==column-1 && y==row-1 ){
		intens=spectrum.getCopyOfY_values();
		surround[0][0][0]=intens[time-1];
		surround[0][0][1]=intens[time  ];
		surround[0][0][2]=intens[time+1];
	    }else if( x==column-1 && y==row   ){
		intens=spectrum.getCopyOfY_values();
		surround[0][1][0]=intens[time-1];
		surround[0][1][1]=intens[time  ];
		surround[0][1][2]=intens[time+1];
	    }else if( x==column-1 && y==row+1 ){
		intens=spectrum.getCopyOfY_values();
		surround[0][2][0]=intens[time-1];
		surround[0][2][1]=intens[time  ];
		surround[0][2][2]=intens[time+1];
	    }else if( x==column   && y==row-1 ){
		intens=spectrum.getCopyOfY_values();
		surround[1][0][0]=intens[time-1];
		surround[1][0][1]=intens[time  ];
		surround[1][0][2]=intens[time+1];
	    }else if( x==column   && y==row   ){
		intens=spectrum.getCopyOfY_values();
		surround[1][1][0]=intens[time-1];
		surround[1][1][1]=intens[time  ];
		surround[1][1][2]=intens[time+1];
	    }else if( x==column   && y==row+1 ){
		intens=spectrum.getCopyOfY_values();
		surround[1][2][0]=intens[time-1];
		surround[1][2][1]=intens[time  ];
		surround[1][2][2]=intens[time+1];
	    }else if( x==column+1 && y==row-1 ){
		intens=spectrum.getCopyOfY_values();
		surround[2][0][0]=intens[time-1];
		surround[2][0][1]=intens[time  ];
		surround[2][0][2]=intens[time+1];
	    }else if( x==column+1 && y==row   ){
		intens=spectrum.getCopyOfY_values();
		surround[2][1][0]=intens[time-1];
		surround[2][1][1]=intens[time  ];
		surround[2][1][2]=intens[time+1];
	    }else if( x==column+1 && y==row+1 ){
		intens=spectrum.getCopyOfY_values();
		surround[2][2][0]=intens[time-1];
		surround[2][2][1]=intens[time  ];
		surround[2][2][2]=intens[time+1];
	    }
	}

	return surround;
    }

 /* ----------------------------- centroid ------------------------------- */ 
    private Peak centroid(Peak peak, float[][][] surround){

	float asum=0.0f;
	float xsum=0.0f;
	float ysum=0.0f;
	float zsum=0.0f;
	
	float x,y,z;
	int reflag=peak.reflag();
	reflag=(reflag/100)*100+reflag%10;

	if( peak.nearedge()<=4.0f ){ //too close to edge
	    peak.reflag(reflag+20);
	    return peak;
	}

	for( int I3=0 ; I3<3 ; I3++ ){
	    for( int I2=0 ; I2<3 ; I2++ ){
		for( int I1=0 ; I1<3 ; I1++ ){
		    xsum+=surround[I1][I2][I3]*((float)I1+1.0f);
		    ysum+=surround[I1][I2][I3]*((float)I2+1.0f);
		    zsum+=surround[I1][I2][I3]*((float)I3+1.0f);
		    asum+=surround[I1][I2][I3];
		}
	    }
	}

	if(asum<=0){
	    peak.reflag(reflag+30);
	    return peak;
	}
	x=xsum/asum+(float)peak.x()-2;
	y=ysum/asum+(float)peak.y()-2;
	z=zsum/asum+(float)peak.z()-2;

	float dx=Math.abs(x-(float)peak.x());
	float dy=Math.abs(y-(float)peak.y());
	float dz=Math.abs(z-(float)peak.z());

	if( dx>1.0 || dy>1.0 || dz>1.0 ){
	    peak.reflag(reflag+30);
	    return peak;
	}else{
	    peak.x(x);
	    peak.y(y);
	    peak.z(z);
	}

	peak.reflag(reflag+10);
	return peak;
    }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new CentroidPeaks();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
    public static void main( String args[] ){
	
	String datfile="/IPNShome/pfpeterson/data/SCD/SCD06496.RUN";
	DataSet rds = (new RunfileRetriever(datfile)).getDataSet(1);
	
	FindPeaks fo = new FindPeaks(rds,10,1);
	Vector peaked=(Vector)fo.getResult();
	Peak peak=new Peak();
	for( int i=0 ; i<peaked.size() ; i++ ){
	    peak=(Peak)peaked.elementAt(i);
	    System.out.println(peak);
	}
	System.out.println("done with FindPeaks");

	CentroidPeaks co = new CentroidPeaks();
	co = new CentroidPeaks( rds, peaked );
	peaked=(Vector)co.getResult();
	for( int i=0 ; i<peaked.size() ; i++ ){
	    peak=(Peak)peaked.elementAt(i);
	    System.out.println(peak);
	}
	System.out.println("done with CentroidPeaks");
    }
}
