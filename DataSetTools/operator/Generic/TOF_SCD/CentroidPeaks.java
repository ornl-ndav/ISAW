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
 * $Log$
 * Revision 1.8  2003/01/15 20:54:25  dennis
 * Changed to use SegmentInfo, SegInfoListAttribute, etc.
 *
 * Revision 1.7  2002/11/27 23:22:20  pfpeterson
 * standardized header
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Attribute.LoadSCDCalib;
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
public class CentroidPeaks extends GenericTOF_SCD implements HiddenOperator{
  private static final String     TITLE                 = "Centroid Peaks";
  private static final int        time_notice_frequency = 20;
  private static final SharedData shared                = new SharedData();
  private              int        run_number            = -1;
  
  /**
   *  Creates operator with title "Centroid Peaks" and a default
   *  list of parameters.
   */  
  public CentroidPeaks(){
    super( TITLE );
  }
  
  /** 
   *  Creates operator with title "Centroid Peaks" and the specified
   *  list of parameters. The getResult method must still be used to
   *  execute the operator.
   *
   *  @param data_set DataSet to find peak in
   *  @param peaks Vector of peaks. Normally created by FindPeaks.
   */
  public CentroidPeaks( DataSet data_set, Vector peaks){
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("Histogram", data_set) );
    addParameter( new Parameter("Vector of Peaks", peaks) );
  }
  
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return "CentroidPeaks", the command used to invoke this
   * operator in Scripts
   */
  public String getCommand(){
    return "CentroidPeaks";
  }
  
  /** 
   * Sets default values for the parameters. This must match the
   * data types of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter( new Parameter("Histogram",DataSet.EMPTY_DATA_SET) );
    addParameter( new Parameter("Vector of Peaks", new Vector()) );
  }
  
  /** 
   *  Executes this operator using the values of the current
   *  parameters.
   *
   *  @return If successful, this operator prints out a list of
   *  x,y,time bins and intensities.
   */
  public Object getResult(){
    DataSet data_set = (DataSet)(getParameter(0).getValue());
    Vector  peaks    = (Vector) (getParameter(1).getValue());
    Vector  cpeaks   = new Vector();
    
    float[] times=data_set.getData_entry(0).getX_scale().getXs();
    
    Peak peak=new Peak();
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      float[][][] surround=makeSurround(data_set,peak);
      //System.out.print("t["+(int)peak.z()+" -> ");
      peak=centroid(peak,surround);
      //System.out.print(peak.z()+"]="+peak.t()+" -> ");
      peak.times(times[(int)peak.z()],times[(int)peak.z()+1]);
      //System.out.println(peak.t());
    }
    
    return peaks;
  }
  
  /**
   * This method takes a peak and finds the surrounding area needed
   * for centroiding it.
   *
   * @return a 7x7x3 array of counts centered around the peak
   */
  private float[][][] makeSurround( DataSet data, Peak peak){
    float[][][] surround=new float[7][7][3];
    int column = (int)peak.x();
    int row    = (int)peak.y();
    int time   = (int)peak.z();
    
    // initialize the array
    for( int i=0 ; i<surround.length ; i++ ){
      for( int j=0 ; j<surround[0].length ; j++ ){
        for( int k=0 ; k<surround[0][0].length ; k++ ){
          surround[i][j][k]=0f;
        }
      }
    }
    
    // fill the array with the appropriate values
    int x;
    int y;
    Data spectrum;
    SegInfoListAttribute segI;
    SegmentInfo det;
    float[] intens;
    for( int i=0 ; i<data.getNum_entries() ; i++ ){
      spectrum=data.getData_entry(i);
      segI=(SegInfoListAttribute)
        spectrum.getAttribute(Attribute.SEGMENT_INFO_LIST);
      det=((SegmentInfo[])segI.getValue())[0];
      x=det.getColumn();
      y=det.getRow();
      intens=spectrum.getCopyOfY_values();
      if( x>=column-3 && x<=column+3 && y>=row-3 && y<=row+3 ){
        surround[x-column+3][y-row+3][0]=getIntens(intens,time-1);
        surround[x-column+3][y-row+3][1]=getIntens(intens,time  );
        surround[x-column+3][y-row+3][2]=getIntens(intens,time+1);
      }
    }
    // lets add a bug to set the background to something bad and
    // be in agreement with the original software
    for( int i=0 ; i<data.getNum_entries() ; i++ ){
      spectrum=data.getData_entry(i);
      segI=(SegInfoListAttribute)
        spectrum.getAttribute(Attribute.SEGMENT_INFO_LIST);
      det=((SegmentInfo[])segI.getValue())[0];
      x=det.getColumn();
      y=det.getRow();
      intens=spectrum.getCopyOfY_values();
      if( y==1 ){
        if(x>0 && x<=7){
          surround[x-1][y-1][0]=getIntens(intens,time-1);
          surround[x-1][y-1][1]=getIntens(intens,time  );
          surround[x-1][y-1][2]=getIntens(intens,time+1);
        }
      }
    }
    for( int i2=0 ; i2<7 ; i2++ ){
      surround[i2][6][0]=surround[i2][0][0];
      surround[i2][6][1]=surround[i2][0][1];
      surround[i2][6][2]=surround[i2][0][2];
    }
    for( int i2=1 ; i2<6 ; i2++ ){
      surround[0][i2][0]=surround[0][0][0];
      surround[0][i2][1]=surround[0][0][1];
      surround[0][i2][2]=surround[0][0][2];
      surround[6][i2][0]=surround[6][0][0];
      surround[6][i2][1]=surround[6][0][1];
      surround[6][i2][2]=surround[6][0][2];
    }
    // end of the intentional bug
    
    return surround;
  }
  
  /**
   * Method to get the intensity of a given time channel that checks
   * the array boundaries.
   */
  private float getIntens( float[] intensity, int time_bin ){
    if(time_bin<0)
      return 0f;
    else if(time_bin>=intensity.length)
      return 0f;
    else
      return intensity[time_bin];
  }
  
  /**
   * This does the actual centroiding. 
   */
  private Peak centroid(Peak peak, float[][][] surround){
    
    double asum  = 0.;
    double xsum  = 0.;
    double ysum  = 0.;
    double zsum  = 0.;
    double back  = 0.;
    double count = 0.;
    
    float x,y,z;
    int reflag=peak.reflag();
    reflag=(reflag/100)*100+reflag%10;
    
    if( peak.nearedge()<=4.0f ){ //too close to edge
      peak.reflag(reflag+20);
      return peak;
    }
    
    for( int k=0 ; k<3 ; k++ ){
      // determine the background for this time slice
      back=0.;
      for( int j=0 ; j<7 ; j++ ){
        for( int i=0 ; i<7 ; i++ ){
          if( i==0 || i==6 ){ // left and right borders
            back=back+(double)surround[i][j][k];
          }else{
            if( j==0 || j==6 ){ // top and bottom borders
              back=back+(double)surround[i][j][k];
            }
          }
        }
      }
      back=back/24f; // normalize the background by number of points
      // find the sums for the centroid
      for( int j=2 ; j<5 ; j++ ){
        for( int i=2 ; i<5 ; i++ ){
          count=(double)surround[i][j][k]-back;
          xsum=xsum+count*((double)i+1.);
          ysum=ysum+count*((double)j+1.);
          zsum=zsum+count*(peak.z()+(double)k);
          asum=asum+count;
        }
      }
    }
    
    // total count must be greater than zero for this to make sense
    if(asum<=0){
      peak.reflag(reflag+30);
      return peak;
    }
    // centroid the peaks
    x=(float)(xsum/asum)+(float)peak.x()-4f;
    y=(float)(ysum/asum)+(float)peak.y()-4f;
    z=(float)(zsum/asum)-1f; // -1 is to convert to java counting
    
    // find out how far the peaks were moved
    float dx=Math.abs(x-(float)peak.x());
    float dy=Math.abs(y-(float)peak.y());
    float dz=Math.abs(z-(float)peak.z());
    
    if( dx>1.0 || dy>1.0 || dz>1.0 ){
      // don't shift positions if it is moving more than one bin
      peak.reflag(reflag+30);
      return peak;
    }else{
      // update the peak
      peak.pixel(x,y,z);
    }
    
    // return the updated peak
    peak.reflag(reflag+10);
    return peak;
  }
  
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Operator op = new CentroidPeaks();
    op.CopyParametersFrom( this );
    return op;
  }
  
  /** 
   * Test program to verify that this will complile and run ok.
   */
  public static void main( String args[] ){
    
    String datfile="/IPNShome/pfpeterson/data/SCD/SCD06496.RUN";
    DataSet rds = (new RunfileRetriever(datfile)).getDataSet(1);
    LoadSCDCalib lsc=new
      LoadSCDCalib(rds,"/IPNShome/pfpeterson/data/SCD/instprm.dat",1,null);
    lsc.getResult();
    
    FindPeaks fo = new FindPeaks(rds,138568f,10,1);
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
    System.exit(0);
  }
}
