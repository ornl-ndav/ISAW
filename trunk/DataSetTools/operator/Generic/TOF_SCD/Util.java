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
 * Revision 1.10  2003/04/28 21:11:57  pfpeterson
 * Added ability to write orientation matrix to experiment files.
 *
 * Revision 1.9  2003/04/22 14:32:14  pfpeterson
 * Changed formatting when writting a matrix file.
 *
 * Revision 1.8  2003/04/21 14:15:02  pfpeterson
 * Changed detector_angle, and detector_distance to look at the attributes
 * of one of the spectra rather than the DataSet.
 *
 * Revision 1.7  2003/04/09 16:21:26  pfpeterson
 * Moved the code to write a matrix file here from BlindJ.
 *
 * Revision 1.6  2003/03/26 20:52:45  pfpeterson
 * Finished implementing the setting of reflection flags as disscussed
 * with A.Schultz.
 *
 * Revision 1.5  2003/03/20 21:56:54  pfpeterson
 * Centroid now deals with ArrayOutOfBoundsException in an appropriate manner.
 *
 * Revision 1.4  2003/02/13 17:03:44  pfpeterson
 * Methods to determine detector center angle and distance now try to
 * get attribute before calculating.
 *
 * Revision 1.3  2003/02/12 20:03:11  dennis
 * Switched to use PixelInfoList instead of SegmentInfoList
 *
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
import DataSetTools.math.*;
import DataSetTools.util.ErrorString;
import DataSetTools.util.Format;
import DataSetTools.util.TextFileReader;
import java.io.FileWriter;
import java.io.IOException;

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
    Data data=ds.getData_entry(0); 
    Object attr_val=data.getAttributeValue(Attribute.DETECTOR_CEN_ANGLE);
    if(attr_val!=null && attr_val instanceof Float)
      return ((Float)attr_val).floatValue();

    PixelInfoListAttribute pil_attr =
          (PixelInfoListAttribute)data.getAttribute(Attribute.PIXEL_INFO_LIST);
    PixelInfoList pil  = (PixelInfoList)pil_attr.getValue();   
    Vector3D      vec  = pil.pixel(0).DataGrid().position();
    Position3D    position     = new Position3D( vec );
    float         cyl_coords[] = position.getCylindricalCoords(); 
    
    return (float)(cyl_coords[1]*180./Math.PI);
  }

  /**
   * Find the detector center angle (out of plane). This currently
   * just returns zero.
   *
   * @return the out of plane angle in degrees
   */
  static public float detector_angle2(DataSet ds){
    return 0f;
    /*Data data=ds.getData_entry(0); 

      PixelInfoListAttribute pil_attr =
      (PixelInfoListAttribute)data.getAttribute(Attribute.PIXEL_INFO_LIST);
      PixelInfoList pil  = (PixelInfoList)pil_attr.getValue();   
      Vector3D      vec  = pil.pixel(0).DataGrid().position();
      Position3D    position     = new Position3D( vec );
      float         sph_coords[] = position.getSphericalCoords(); 
    
      return (float)(90.-180.*sph_coords[2]/Math.PI);*/
  }

  /**
   * Determine the detector center distance from the sample
   *
   * @return the distance in cm
   */
  static public float detector_distance(DataSet ds, float avg_angle){
    // NOTE: avg_angle is no longer needed.
  
    PixelInfoListAttribute pil_attr;
    Data data=ds.getData_entry(0);
   
    Object attr_val=data.getAttributeValue(Attribute.DETECTOR_CEN_DISTANCE);
    if(attr_val!=null && attr_val instanceof Float)
      return ((Float)attr_val).floatValue();

    pil_attr =
          (PixelInfoListAttribute)data.getAttribute(Attribute.PIXEL_INFO_LIST);
    PixelInfoList pil  = (PixelInfoList)pil_attr.getValue();
    Vector3D      vec  = pil.pixel(0).DataGrid().position();
    float         dist = vec.length();

    return dist*100f;
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

    // determine the maximum number of rows and columns
    Attribute attr=ds.getData_entry(0).getAttribute(Attribute.PIXEL_INFO_LIST);
    if( attr == null || !(attr instanceof PixelInfoListAttribute) )
      return null;

    PixelInfoList  pil  = (PixelInfoList)attr.getValue();
    IDataGrid      grid = pil.pixel(0).DataGrid();
 
    rowMax = grid.num_rows();
    colMax = grid.num_cols();
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
      attr = data.getAttribute(Attribute.PIXEL_INFO_LIST);;
      if( attr!=null && attr instanceof PixelInfoListAttribute ){
        pil = (PixelInfoList)attr.getValue();
        row = (int)pil.row();
        col = (int)pil.col();
        if( (row>=0) && (col>=0) )
          ids[col][row] = id;
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
    try{
      for( int i=col-3 ; i<=col+3 ; i++ ){
        for( int j=row-3 ; j<=row+3 ; j++ ){
          data=ds.getData_entry(ids[i][j]);
          if(data==null) return null;
          surround[i-col+3][j-row+3][0]=data.getY_values()[time-1];
          surround[i-col+3][j-row+3][1]=data.getY_values()[time  ];
          surround[i-col+3][j-row+3][2]=data.getY_values()[time+1];
        }
      }
    }catch(ArrayIndexOutOfBoundsException e){
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
      peak.reflag(reflag+20);
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
      peak.reflag(reflag+20);
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
   * Write out the orientation matrix and lattice parameters to the
   * matrix file.
   *
   * @param filename name of file to write to
   * @param UB the orientation matrix. What is actually written is the
   * transpose of what is used in memory.
   * @param abc a float[7] with members (in order) being a, b, c,
   * alpha, beta, gamma, Dvolume
   * @param sig a float[8] with members (in order) being Da, Db, Dc,
   * Dalpha, Dbeta, Dgamma, and Dvolume
   *
   * @return A descriptive ErrorString if anything goes wrong or null
   * if all is well
   */
  public static ErrorString writeMatrix( String filename, float[][] UB,
                                         float[] abc, float[] sig){
    StringBuffer sb;
    if(filename.endsWith(".x")){ // writing an experiment file
      sb=new StringBuffer(81*8);
      TextFileReader tfr=null;
      try{
        tfr=new TextFileReader(filename);
        String line=null;
        while(!tfr.eof()){
          line=tfr.read_line();
          if(line.startsWith("CRS0  VSIGV")){ // cell volume
            sb.append("CRS0  VSIGV"+Format.real(abc[6],11,4)
                      +Format.real(sig[6],10,4)+"\n");
          }else if(line.startsWith("CRS1  ABC   ")){ // lattice param lengths
            sb.append("CRS1  ABC   ");
            for(int i=0 ; i<3 ; i++ )
              sb.append(Format.real(abc[i],10,4));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS1  ABCSIG")){ // uncertainties in param lengths
            sb.append("CRS1  ABCSIG");
            for(int i=0 ; i<3 ; i++ )
              sb.append(Format.real(sig[i],10,4));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS1  ANGLES")){ // lattice param angles
            sb.append("CRS1  ANGLES");
            for(int i=3 ; i<6 ; i++ )
              sb.append(Format.real(abc[i],10,3));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS1  ANGSIG")){ // uncertainties in param angles
            sb.append("CRS1  ANGSIG");
            for(int i=3 ; i<6 ; i++ )
              sb.append(Format.real(sig[i],10,3));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS11 UBMAT1")){ // orientation matrix
            sb.append("CRS11 UBMAT1");
            for(int j=0 ; j<3 ; j++)
              sb.append(Format.real(UB[j][0],10,6));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS11 UBMAT2")){ // orientation matrix
            sb.append("CRS11 UBMAT2");
            for(int j=0 ; j<3 ; j++)
              sb.append(Format.real(UB[j][1],10,6));
            sb.append(Format.string("\n",39));
          }else if(line.startsWith("CRS11 UBMAT3")){ // orientation matrix
            sb.append("CRS11 UBMAT3");
            for(int j=0 ; j<3 ; j++)
              sb.append(Format.real(UB[j][2],10,6));
            sb.append(Format.string("\n",39));
          }else{
            sb.append(line+"\n");
          }
        }
      }catch( IOException e ){
        return new ErrorString("Reading Experiment File: "+e.getMessage());
      }finally{
        try{
          if(tfr!=null) tfr.close();
        }catch(IOException e){
          // let it drop on the floor
        }
      }
    }else{ // writing a matrix file
      sb= new StringBuffer(10*3+1);

      // the UB matrix
      for( int i=0 ; i<3 ; i++ ){
        for (int j=0 ; j<3 ;j++ )
          sb.append(Format.real(UB[j][i],10,6));
        sb.append("\n");
      }

      // lattice parameters
      for( int i=0 ; i<6 ; i++ )
        sb.append(Format.real(abc[i],10,5));
      sb.append(Format.real(abc[6],10,2)+"\n");
      // sigmas
      for( int i=0; i < 6; i++)
        sb.append(Format.real(sig[i],10,5));
      sb.append(Format.real(sig[6],10,2)+"\n");
    }
    //Write results to the matrix file
    FileWriter fw=null;
    try{
      fw=new FileWriter(filename,false);
      fw.write(sb.toString());
      fw.flush();
    }catch(IOException e){
      return new ErrorString("Writing Matrix File: "+e.getMessage());
    }finally{
      if(fw!=null){
        try{
          fw.close();
        }catch(IOException e){
          // let it drop on the floor
        }
      }
    }
    return null;
  }
}
