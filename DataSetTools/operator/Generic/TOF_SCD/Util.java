/*
 * File:  Util.java 
 *
 * Copyright (C) 2003, Peter Peterson
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
 * Revision 1.2  2003/02/10 23:04:21  pfpeterson
 * Changed the constructor from being public and throwing an
 * InstantiationError to being private and doing nothing.
 *
 * Revision 1.1  2003/01/30 21:07:23  pfpeterson
 * Added to CVS.
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.instruments.*;

public class Util{
  /**
   * Don't let anyone try to instantiate the class
   */
  private Util(){
  }

  /**
   * Determine the detector center angle (in plane).
   * 
   * @return the in plane angle in degrees
   */
  static public float detector_angle(DataSet ds){
    SegInfoListAttribute detI;
    SegmentInfo det;
    Data data=ds.getData_entry(0);
    float angle=0f;
    Float Fangle=new Float(0f);
    int total=0;

    Fangle=(Float)data.getAttributeValue(Attribute.DETECTOR_CEN_ANGLE);
    if(Fangle!=null){
      angle=Fangle.floatValue();
    }
    if(angle==0f){
      for( int i=0 ; i< ds.getNum_entries() ; i++ ){
        data=ds.getData_entry(i);
        detI=(SegInfoListAttribute)
          data.getAttribute(Attribute.SEGMENT_INFO_LIST);
        det=((SegmentInfo[])detI.getValue())[0];
        angle+=det.getPosition().getScatteringAngle();
        total++;
      }
      angle=(float)((180f*angle)/((float)(total+1)*Math.PI));
    }
    
    return angle;
  }

  /**
   * Find the detector center angle (out of plane). This currently
   * just returns zero.
   *
   * @return the out of plane angle in degrees
   */
  static public float detector_angle2(DataSet ds){
    return 0f;
  }

  /**
   * Determine the detector center distance from the sample
   *
   * @return the distance in cm
   */
  static public float detector_distance(DataSet ds, float avg_angle){
    SegInfoListAttribute detI;
    SegmentInfo det;
    Data data=ds.getData_entry(0);
    float angle=0f;
    float distance=0f;
    Float Fdistance=new Float(distance);
    int total=0;
    
    Fdistance=(Float)data.getAttributeValue(Attribute.DETECTOR_CEN_DISTANCE);
    if(Fdistance!=null){
      distance=Fdistance.floatValue();
    }
    
    if(distance==0f){
      for( int i=0 ; i< ds.getNum_entries() ; i++ ){
        data=ds.getData_entry(i);
        detI=(SegInfoListAttribute)
          data.getAttribute(Attribute.SEGMENT_INFO_LIST);
        det=((SegmentInfo[])detI.getValue())[0];
        
        angle=det.getPosition().getScatteringAngle();
        angle=angle-2f*avg_angle/(float)Math.PI;
        
        angle=(float)Math.abs(Math.cos((double)angle));
        
        distance+=angle*det.getPosition().getDistance();
        total++;
      }
      distance=distance/((float)(total+1));
    }
    
    return distance*100f;
  }

  /**
   * From the given dataset create a 2D array to map row and column to
   * id. This assumes that there is only one detector
   *
   * @return a 2D matrix of ids from detector column and row (in
   * order). Elements that do not have an id are set to -1.
   */
  public static int[][] createIdMap(DataSet ds){
    Data data=null;
    int row,col;
    int rowMax=-1;
    int colMax=-1;
    SegmentInfo[] seginfo=null;

    // determine the maximum number of rows and columns
    for( int i=0; i< ds.getNum_entries(); i++){
      Attribute attr =
        ds.getData_entry( i ).getAttribute(Attribute.SEGMENT_INFO_LIST);;
      if( attr!=null && attr instanceof SegInfoListAttribute ){
        seginfo=(SegmentInfo[])(attr.getValue());
        if( seginfo!=null && seginfo.length>0 ){
          row = seginfo[0].getRow();
          col = seginfo[0].getColumn();
          if( row>rowMax && row>0)
            rowMax = row;
          if( col>colMax && col>0)
            colMax = col;
        }
      }
    }
    // check that we got sensible values
    if( rowMax < 1 || colMax<1 ) return null;

    // initialize the return matrix
    int[][] ids = new int[colMax+1][rowMax+1];
    for( int i=0 ; i<ids.length ; i++ ){
      for( int j=0 ; j<ids.length ; j++ ){
        ids[i][j]=-1;
      }
    }

    // create a reference 2D array to locate the proper group for the
    // given row and column
    Object idObj=null;
    for( int id=0; id<ds.getNum_entries(); id++){
      data=ds.getData_entry(id);
      Attribute attr = data.getAttribute(Attribute.SEGMENT_INFO_LIST);;
      if( attr!=null && attr instanceof SegInfoListAttribute ){
        seginfo=(SegmentInfo[])(attr.getValue());
        if( seginfo!=null && seginfo.length>0 ){
          row = seginfo[0].getRow();
          col = seginfo[0].getColumn();
          if( (row>=0) && (col>=0) )
            ids[col][row] = id;
        }
      }
    }

    return ids;
  }

  /**
   * Find the centroided location of a peak.
   *
   * @param peak the peak to centroid which already has an initial
   * position
   * @param ds the dataset to use for centroiding
   * @param ids a 2D array of ids where the indices are column and row
   * of the data block.
   */
  public static Peak centroid(Peak peak, DataSet ds, int[][] ids){
    
    double asum  = 0.;
    double xsum  = 0.;
    double ysum  = 0.;
    double zsum  = 0.;
    double back  = 0.;
    double count = 0.;
    
    // create the new reflection flag
    float x,y,z;
    int reflag=peak.reflag();
    reflag=(reflag/100)*100+reflag%10;
    
    // check that the peak isn't too close to the edge
    if( peak.nearedge()<=4.0f ){
      peak.reflag(reflag+20);
      return peak;
    }
    
    // create the surrounding area
    float[][][] surround=new float[7][7][3];
    int col=(int)Math.round(peak.x());
    int row=(int)Math.round(peak.y());
    int time=(int)Math.round(peak.z());
    Data data=null;
    for( int i=col-3 ; i<=col+3 ; i++ ){
      for( int j=row-3 ; j<=row+3 ; j++ ){
        //        if( (i>groups.length-1) || (j>groups[0].length-1) )
        //peak.reflag(peak20)
        data=ds.getData_entry(ids[i][j]);
        surround[i-col+3][j-row+3][0]=data.getY_values()[time-1];
        surround[i-col+3][j-row+3][1]=data.getY_values()[time  ];
        surround[i-col+3][j-row+3][2]=data.getY_values()[time+1];
      }
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
}
