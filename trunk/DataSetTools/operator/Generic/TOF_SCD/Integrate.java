/*
 * File:  Integrate.java 
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
 * Revision 1.3  2003/02/12 15:29:56  pfpeterson
 * Various improvements to user interface including autimatically loading
 * the oirentation matrix and calibration from experiment file if not
 * already preset, and writting the resultant peaks to a file.
 *
 * Revision 1.2  2003/02/10 16:03:41  pfpeterson
 * Fixed semantic error.
 *
 * Revision 1.1  2003/01/30 21:07:23  pfpeterson
 * Added to CVS.
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.instruments.*;
import DataSetTools.util.*;
import DataSetTools.retriever.RunfileRetriever;
import java.io.*;
import java.util.Vector;

/** 
 * This is a ported version of A.J.Schultz's INTEGRATE program. 
 */
public class Integrate extends GenericTOF_SCD{
  private static final String     TITLE   = "Integrate";
  private static       boolean    DEBUG   = false;
  
  /* ------------------------ Default constructor ------------------------- */ 
  /**
   * Creates operator with title "Integrate" and a default list of
   * parameters.
   */  
  public Integrate(){
    super( TITLE );
  }
  
  /** 
   * Creates operator with title "Find Peaks" and the specified list
   * of parameters. The getResult method must still be used to execute
   * the operator.
   *
   * @param ds DataSet to integrate
   * @param path The directory where the experiment and matrix files
   * will be found
   * @param expname The "experiment name" for the analysis. The
   * experiment file is looked for as path+expname+".x"
   * @param intlist A list of which histograms in the experiment file
   * to integrate. If the list is empty the program works on all
   * histograms.
   */
  public Integrate( DataSet ds, String path, String expname){
    this(); 

    getParameter(0).setValue(ds);
    getParameter(1).setValue(path);
    getParameter(2).setValue(expname);
  }
  
  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts: SCDIntegrate
   * 
   * @return  "SCDIntegrate", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "SCDIntegrate";
  }
  
  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets default values for the parameters.  This must match the data types 
   * of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter( new Parameter("Data Set", new SampleDataSet() ) );
    addParameter( new Parameter("Path",new DataDirectoryString()) );
    addParameter( new Parameter("Experiment Name", new String()) );
  }
  
  /**
   * Returns the documentation for this operator as a string. The
   * format follows standard JavaDoc conventions.
   */
  public String getDocumentation(){
    StringBuffer sb=new StringBuffer("");

    // overview
    sb.append("@overveiw This operator is a direct port of A.J.Schultz's INTEGRATE program. This locates peaks in a DataSet for a given orientation matrix and then determine's the integrated peak intensity.");
    // assumptions
    sb.append("@assumptions The DataSet already has the orientation matrix and calibration loaded into it. Also, the experiment file must exist and be user readable.");
    // algorithm
    sb.append("@algorithm ");
    // parameters
    sb.append("@param ds DataSet to integrate.");
    sb.append("@param path The directory where the experiment and matrix files will be found");
    sb.append("@param expname The \"experiment name\" for the analysis. The experiment file is looked for as path+expname+\".x\"");
    // return
    sb.append("@return The set of integrated peaks which can then be passed to WritePeaks to be placed in a file.");
    // errors
    sb.append("@error First parameter is not a DataSet or the DataSet is empty");
    sb.append("@error Second parameter is null or does not specify an existing directory");
    sb.append("@error Third parameter is null or does not specify an existing experiment.");
    sb.append("@error When there is an IOException while reading the experiment file.");
    sb.append("@error If there is no pixel 1,1 in the dataset.");
    sb.append("@error No detector calibration found in the dataset.");
    sb.append("@error No orientation matrix found in the dataset.");
    sb.append("@error No detector number found");
    sb.append("@error When the initial flight path is zero");

    return sb.toString();
  }

  /** 
   *  Executes this operator using the values of the current parameters.
   *
   *  @return If successful, this operator returns a vector of Peak
   *  objects.
   */
  public Object getResult(){
    Object val=null; // used for dealing with parameters
    String expfile=null;
    DataSet ds;
    String path;
    String expname;
    int threashold=1;

    // first get the DataSet
    val=getParameter(0).getValue();
    if( val instanceof DataSet){
      if(((DataSet)val).getNum_entries()>0)
        ds=(DataSet)val;
      else
        return new ErrorString("Specified DataSet is empty");
    }else{
      return new ErrorString("Value of first parameter must be a dataset");
    }

    // then get the Directory
    val=getParameter(1).getValue();
    if(val!=null){
      path=FilenameUtil.fixSeparator(val.toString()+"/");
      File dir=new File(path);
      if(!dir.isDirectory())
        return new ErrorString("Path is not a directory");
    }else{
      return new ErrorString("Path is null");
    }

    // then get the experiment name
    val=getParameter(2).getValue();
    if(val!=null){
      expname=val.toString();
      if(expname.length()==0)
        return new ErrorString("Experiment Name is null");
      expfile=FilenameUtil.fixSeparator(path+expname+".x");
      File file=new File(expfile);
      if(!file.exists())
        return new ErrorString("Could not find experiment file: "+file);
    }else{
      return new ErrorString("Experiment Name is null");
    }

    // now the uncertainty in the peak location
    int dX=2, dY=2, dZ=1;

    if(DEBUG){
      System.out.println("DataSet:"+ds);
      System.out.println("Path   :"+path);
      System.out.println("ExpName:"+expname);
    }

    // create the lookup table
    int[][] ids=Util.createIdMap(ds);

    // determine the boundaries of the matrix
    int[] rcBound=getBounds(ids);
    for( int i=0 ; i<4 ; i++ ){
      if(rcBound[i]==-1)
        return new ErrorString("Bad boundaries on row column matrix");
    }

    // grab pixel 1,1 to get some 'global' attributes from 
    Data data=ds.getData_entry(ids[rcBound[0]][rcBound[1]]);
    if(data==null)
      return new ErrorString("no minimum pixel found");

    // get the calibration for this
    float[] calib=(float[])data.getAttributeValue(Attribute.SCD_CALIB);
    if(calib==null){
      LoadSCDCalib loadcalib=new LoadSCDCalib(ds,expfile,0,"");
      Object res=loadcalib.getResult();
      if(res instanceof ErrorString) return res;
      calib=(float[])data.getAttributeValue(Attribute.SCD_CALIB);
      if(calib==null)
        return new ErrorString("Could not load calibration from experiment "
                               +"file");
    }

    // get the xscale from the data to give to the new peaks objects
    XScale times=data.getX_scale();

    // determine the initial flight path
    float init_path=0f;
    {
      Object L1=data.getAttributeValue(Attribute.INITIAL_PATH);
      if(L1!=null){
        if(L1 instanceof Float)
          init_path=((Float)L1).floatValue();
      }
      L1=null;
    }
    if(init_path==0f)
      return new ErrorString("initial flight path is zero");

    // determine the detector postion
    float detA=Util.detector_angle(ds);
    float detA2=Util.detector_angle2(ds);
    float detD=Util.detector_distance(ds,detA);

    // get the sample orientation
    float chi=0f, phi=0f, omega=0f;
    {
      Object orient_val=null;
      orient_val=data.getAttributeValue(Attribute.SAMPLE_CHI);
      if(orient_val!=null && orient_val instanceof Float)
        chi=((Float)orient_val).floatValue();
      orient_val=data.getAttributeValue(Attribute.SAMPLE_PHI);
      if(orient_val!=null && orient_val instanceof Float)
        phi=((Float)orient_val).floatValue();
      orient_val=data.getAttributeValue(Attribute.SAMPLE_OMEGA);
      if(orient_val!=null && orient_val instanceof Float)
        omega=((Float)orient_val).floatValue();
      orient_val=null;
    }

    // get the orientation matrix
    float[][] UB=null;
    {
      Object UBval=null;
      UBval=data.getAttributeValue(Attribute.ORIENT_MATRIX);
      if(UBval==null)
        UBval=ds.getAttributeValue(Attribute.ORIENT_MATRIX);
      if(UBval!=null && UBval instanceof float[][])
        UB=(float[][])UBval;
      UBval=null;
    }
    if(UB==null){ // try loading it
      LoadOrientation loadorient=new LoadOrientation(ds,
                                                  new LoadFileString(expfile));
      Object res=loadorient.getResult();
      if(res instanceof ErrorString) return res;
      // try getting the value again
      Object UBval=null;
      UBval=data.getAttributeValue(Attribute.ORIENT_MATRIX);
      if(UBval==null)
        UBval=ds.getAttributeValue(Attribute.ORIENT_MATRIX);
      if(UBval!=null && UBval instanceof float[][])
        UB=(float[][])UBval;
      UBval=null;
      if(UB==null) // now give up if it isn't there
        return new ErrorString("Could not load orientation matrix from "
                               +"experiment file");
    }

    // determine the min and max pixel-times
    int zmin=0;
    int zmax=times.getNum_x()-1;

    // get the run number
    int nrun=0;
    {
      Object Nrun=data.getAttributeValue(Attribute.RUN_NUM);
      if(Nrun!=null){
        if( Nrun instanceof Integer)
          nrun=((Integer)Nrun).intValue();
        else if( Nrun instanceof int[])
          nrun=((int[])Nrun)[0];
          }
      Nrun=null;
    }

    // get the detector number
    int detnum=0;
    {
      Object detnumO=null;
      detnumO=data.getAttributeValue(Attribute.SEGMENT_INFO);
      if(detnumO==null)
        detnumO=data.getAttributeValue(Attribute.SEGMENT_INFO_LIST);
      if(detnumO!=null){
        if(detnumO instanceof SegmentInfo[]){
          detnumO=((SegmentInfo[])detnumO)[0];
        }
        if(detnumO instanceof SegmentInfo){
          detnum=((SegmentInfo)detnumO).getDet_num();
        }
      }
    }
    if(detnum==0)
      return new ErrorString("detector number not found");

    // create a PeakFactory for use throughout this operator
    PeakFactory pkfac=new PeakFactory(nrun,detnum,init_path,detD,detA,detA2);
    pkfac.calib(calib);
    pkfac.UB(UB);
    pkfac.sample_orient(chi,phi,omega);
    pkfac.time(times);

    // determine the detector limits in hkl
    int[][] hkl_lim=minmaxhkl(pkfac, ids, times);
    float[][] real_lim=minmaxreal(pkfac, ids, times);

    Peak peak=null;
    Vector peaks=new Vector();
    int seqnum=1;
    // loop over all of the possible hkl values and create peaks
    for( int h=hkl_lim[0][0] ; h<=hkl_lim[0][1] ; h++ ){
      for( int k=hkl_lim[1][0] ; k<=hkl_lim[1][1] ; k++ ){
        for( int l=hkl_lim[2][0] ; l<=hkl_lim[2][1] ; l++ ){
          if( h==0 && k==0 && l==0 ) continue; // cannot have h=k=l=0
          if( ! checkCenter(h,k,l,1) ) continue; // fails centering conditions
          peak=pkfac.getHKLInstance(h,k,l);
          peak.nearedge(rcBound[0],rcBound[2],rcBound[1],rcBound[3],
                        zmin,zmax);
          if( (peak.nearedge()>=2f) && (checkReal(peak,real_lim)) ){
            peak.seqnum(seqnum);
            //getObs(peak,ds,groups);
            peaks.add(peak);
            seqnum++;
          }
        }
      }
    }

    // move peaks to the most intense point nearby
    for( int i=peaks.size()-1 ; i>=0 ; i-- ){
      movePeak((Peak)peaks.elementAt(i),ds,ids,dX,dY,dZ);
      if(((Peak)peaks.elementAt(i)).ipkobs()<=threashold)
        peaks.remove(i); // remove peaks below threashold
    }

    // integrate the peaks
    for( int i=peaks.size()-1 ; i>=0 ; i-- ){
      integratePeak((Peak)peaks.elementAt(i),ds,ids);
      if(((Peak)peaks.elementAt(i)).inti()==0f)
        peaks.remove(i);
    }

    // centroid the peaks
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=Util.centroid((Peak)peaks.elementAt(i),ds,ids);
      peak.seqnum(i+1); // renumber the peaks
      peaks.set(i,peak);
    }

    // write out the peaks
    String intfile=expfile;
    {
      int index=intfile.lastIndexOf(".");
      intfile=intfile.substring(0,index)+".integrate";
    }
    WritePeaks writer=new WritePeaks(intfile,peaks,Boolean.TRUE);
    return writer.getResult();
  }

  /**
   * This method integrates the peak by looking at five time slices
   * centered at the one the peak exsists on. It grows a rectangle on
   * each time slice to get the maximum I/dI for each time slice then
   * adds the results from each time slice to maximize the total I/dI.
   */
  private static void integratePeak(Peak peak, DataSet ds, int[][] ids){
    float[] tempIsigI=null;
    int cenX=(int)Math.round(peak.x());
    int cenY=(int)Math.round(peak.y());
    int cenZ=(int)Math.round(peak.z());

    int[] zrange={cenZ,cenZ-1,cenZ+1,cenZ+2,cenZ+3};
    int minZ=0;
    int maxZ=ds.getData_entry(ids[1][1]).getX_scale().getNum_x();
    for( int i=0 ; i<zrange.length ; i++ ){           // can't integrate past
      if( zrange[i]<minZ || zrange[i]>=maxZ ) return; // ends of time axis
    }
    
    float[][] IsigI=new float[zrange.length][2];

    // integrate each time slice
    for( int k=0 ; k<zrange.length ; k++ ){
      tempIsigI=integratePeakSlice(ds,ids,cenX,cenY,zrange[k]);
      if( tempIsigI[0]>0f ){
        IsigI[k][0]=tempIsigI[0];
        IsigI[k][1]=tempIsigI[1];
      }else{             // if this is the peak's time slice then something
        if(k==0) return; // is wrong and we should just return
      }
    }
    
    // sum up everything
    float Itot=IsigI[0][0];
    float dItot=IsigI[0][1];
    for( int i=1 ; i<zrange.length ; i++ ){
      if( compItoDI(Itot,dItot,IsigI[i][0],IsigI[i][1]) ){
        Itot=Itot+IsigI[i][0];
        dItot=(float)Math.sqrt(dItot*dItot+IsigI[i][1]*IsigI[i][1]);
      }else{
        if(i>=3) i=zrange.length;
      }
    }

    // change the peak to reflect what we just did
    peak.inti(Itot);
    peak.sigi(dItot);
  }

  /**
   * Integrate a peak while varying the range in x and y. This does
   * the hard work of growing the rectangle on a time slice to
   * maximize I/dI.
   */
  private static float[] integratePeakSlice(DataSet ds, int[][] ids,
                                          int Xcen, int Ycen, int z){
    float[] IsigI=new float[2];
    float[] tempIsigI=new float[2];

    int[] rng={Xcen-2,Xcen+2,Ycen-2,Ycen+2};
    int[] step={-1,1,-1,1};

    // initial run with default size for integration
    if( checkRange(ids,rng[0],rng[2],rng[1],rng[3])){
      tempIsigI=integrateSlice(ds,ids,rng[0],rng[2],rng[1],rng[3],z);
      if(tempIsigI[0]==0f || tempIsigI[1]==0f) return IsigI; // something wrong
      if( tempIsigI[0]/tempIsigI[1] < 3f ) return IsigI; // peak is too weak

      if( tempIsigI[0]>0f && tempIsigI[1]>0f ){
        IsigI[0]=tempIsigI[0];
        IsigI[1]=tempIsigI[1];
      }
    }

    int direction=0;
    int itteration=0;
    for( int i=0 ; i<4 ; i++ ){
      itteration=0;
      direction=0;
      while(direction<2 && itteration<10){ // only change direction once
        itteration++;                      // and allow a max num of itteration
        rng[i]=rng[i]+step[i];
        if( checkRange(ids,rng[0],rng[2],rng[1],rng[3])){
          tempIsigI=integrateSlice(ds,ids,rng[0],rng[2],rng[1],rng[3],z);
          if(tempIsigI[0]==0f||tempIsigI[1]==0f) // something wrong
            return IsigI;
          if( tempIsigI[0]/tempIsigI[1] < 3f )   // peak is too weak
            return IsigI;
          if( tempIsigI[0]>0f && tempIsigI[1]>0f ){
            if( IsigI[0]/IsigI[1]<tempIsigI[0]/tempIsigI[1]){
              IsigI[0]=tempIsigI[0];
              IsigI[1]=tempIsigI[1];
            }else{ // change direction
              step[i]=-1*step[i];
              rng[i]=rng[i]+step[i];
              direction++;
            }
          }
        }else{ // change direction
          step[i]=-1*step[i];
          rng[i]=rng[i]+step[i];
          direction++;
        }
      }
    }

    return IsigI;
  }

  /**
   * Integrate around the peak in the given time slice. This
   * integrates the region passed to it.
   */
  private static float[] integrateSlice(DataSet ds, int[][] ids, int minX, 
                                        int minY, int maxX, int maxY, int z){
    float[] IsigI=new float[2];

    int ibxmin=minX-1;
    int ibxmax=maxX+1;
    int ibymin=minY-1;
    int ibymax=maxY+1;
    float stob=((float)(maxX-minX+1)*(maxY-minY+1))
      /((float)(ibxmax-ibxmin+1)*(ibymax-ibymin+1));

    float ibtot=0f;
    float istot=0f;

    float intensity;

    for( int i=minX-1 ; i<=maxX+1 ; i++ ){
      for( int j=minY-1 ; j<=maxY+1 ; j++ ){
        intensity=getObs(ds,ids[i][j],z);
        ibtot=ibtot+intensity;
        if( i>=minX && i<=maxX && j>=minY && j<=maxY )
          istot=istot+intensity;
      }
    }

    ibtot=ibtot-istot;
    IsigI[0]=istot-stob*ibtot;
    IsigI[1]=(float)Math.sqrt(istot+stob*stob*ibtot);

    return IsigI;
  }
  
  /**
   * Utility method to determine whether adding I,dI to Itot,dItot
   * will increase the overall ratio or not
   */
  private static boolean compItoDI(float Itot, float dItot, float I, float dI){
    float myItot=Itot+I;
    float myDItot=(float)Math.sqrt(dItot*dItot+dI*dI);
    
    return ( (Itot/dItot)<(myItot/myDItot) );
  }

  /**
   * Determines whether the integration range lies on the detector.
   */
  private static boolean checkRange(int[][] ids, int minX, int minY,
                                    int maxX, int maxY){
    if(minX<2) return false;

    if(minY<2) return false;

    if(maxX>ids.length-2) return false;

    if(maxY>ids[0].length-2) return false;

    return true;
  }

  /**
   * Put the peak at the nearest maximum within the given deltas
   */
  private static void movePeak( Peak peak, DataSet ds, int[][] ids, 
                                int dx, int dy, int dz){
    int x=(int)Math.round(peak.x());
    int y=(int)Math.round(peak.y());
    int z=(int)Math.round(peak.z());

    int maxP=peak.ipkobs();
    int maxX=x;
    int maxY=y;
    int maxZ=z;

    Data data=null;
    float point=0f;
    for( int i=x-dx ; i<=x+dx ; i++ ){
      for( int j=y-dy ; j<=y+dy ; j++ ){
        for( int k=z-dz ; k<=z+dz ; k++ ){
          point=getObs(ds,ids[i][j],k);
          if(point>maxP){
            maxP=(int)Math.round(point);
            maxX=i;
            maxY=j;
            maxZ=k;
          }
          point=0f;
        }
      }
    }
    if(maxX!=x || maxY!=y || maxZ!=z){ // move to nearby maximum
      peak.pixel(maxX,maxY,maxZ);
      peak.ipkobs(maxP);
    }else{
      peak.pixel(x,y,z); // move it onto integer pixel postion
    }
  }

  /**
   * Determines whether the peak can be within the realspace limits specified
   */
  private static boolean checkReal(Peak peak, float[][] lim){
    float xcm=peak.xcm();
    float ycm=peak.ycm();
    float wl=peak.wl();

    boolean PRINT=true;

    if(wl==0f) return false;

    if( xcm>=lim[0][0] && xcm<=lim[0][1] ){
      if( ycm>=lim[1][0] && ycm<=lim[1][1] ){
        if( wl>=lim[2][0] && wl<=lim[2][1] ){
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Determine the edges of the detector in xcm, ycm, and wl. This
   * assumes that ids[0][0] and ids[maxrow][maxcol] are at
   * opposite corners of the detector
   *
   * @param pkfac A fully configure peak factory. It should contain
   * all of the information necessary to go from hkl to pixel.
   * @param ids The 2D matrix which maps row and column to the
   * linear index of datas in DataSet
   * @param times The detector is assumed to have the same x-axis for
   * all pixels. This should be a safe assumption.
   *
   * @return A 3x2 matrix of the limits in real space. 
   */
  private float[][] minmaxreal(PeakFactory pkfac, int[][] ids,XScale times){
    // Determine the limits in pixel and time. This is set up as
    // arrays to shorten later code in the method.
    int[]     x_lim ={1,ids.length-1};
    int[]     y_lim ={1,ids[0].length-1};
    int[]     z_lim ={0,times.getNum_x()-1};
    float[][] time  = {{times.getX(z_lim[0]),times.getX(z_lim[0]+1)},
                       {times.getX(z_lim[1]-1),times.getX(z_lim[1])}};

    // define a temporary peak that will be each of the corners
    Peak peak=null;

    // The real-space representation of the peaks will be stored in
    // this matrix. The first index is xcm=0, ycm=1, and wl=2. The
    // second index is just the number of the peak, it just needs to
    // be unique
    float[][] real=new float[3][8];

    // This looks scarier than it is. The three indices are x (i), y
    // (j), and z (k). It is set up to reduce the amount of typing
    // that needs to be done if there is an error in the code.
    for( int i=0 ; i<2 ; i++ ){
      for( int j=0 ; j<2 ; j++ ){
        for( int k=0 ; k<2 ; k++ ){
          peak=pkfac.getPixelInstance(x_lim[i],y_lim[j],z_lim[k],
                                      time[k][0],time[k][1]);
          real[0][i+2*j+4*k]=peak.xcm();
          real[1][i+2*j+4*k]=peak.ycm();
          real[2][i+2*j+4*k]=peak.wl();
        }
      }
    }

    // set the peak to null so the garbage collector can reclaim it
    peak=null;


    // the first index is h,k,l and the second index is min,max
    float[][] real_lim={{real[0][0],real[0][0]},
                        {real[1][0],real[1][0]},
                        {real[2][0],real[2][0]}};
    
    // sort out the min and max values
    for( int i=1 ; i<8 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        real_lim[j][0]=Math.min(real_lim[j][0],real[j][i]);
        real_lim[j][1]=Math.max(real_lim[j][1],real[j][i]);
      }
    }

    return real_lim;
  }

  /**
   * Determine the edges of the detector in xcm, ycm, and wl. This
   * assumes that ids[0][0] and ids[maxrow][maxcol] are at
   * opposite corners of the detector
   *
   * @param pkfac A fully configure peak factory. It should contain
   * all of the information necessary to go from hkl to pixel.
   * @param ids The 2D matrix which maps row and column to the
   * linear index of datas in DataSet
   * @param times The detector is assumed to have the same x-axis for
   * all pixels. This should be a safe assumption.
   *
   * @return A 3x2 matrix of the limits in hkl. 
   */
  private int[][] minmaxhkl(PeakFactory pkfac, int[][] ids, XScale times){
    // Determine the limits in pixel and time. This is set up as
    // arrays to shorten later code in the method.
    int[]     x_lim ={1,ids.length-1};
    int[]     y_lim ={1,ids[0].length-1};
    int[]     z_lim ={0,times.getNum_x()-1};
    float[][] time  = {{times.getX(z_lim[0]),times.getX(z_lim[0]+1)},
                       {times.getX(z_lim[1]-1),times.getX(z_lim[1])}};

    // define a temporary peak that will be each of the corners
    Peak peak=null;

    // The hkls of the peaks will be stored in this matrix. The first
    // index is h=0, k=1, and l=2. The second index is just the number
    // of the peak, it just needs to be unique
    int[][] hkl=new int[3][8];

    // This looks scarier than it is. The three indices are x (i), y
    // (j), and z (k). It is set up to reduce the amount of typing
    // that needs to be done if there is an error in the code.
    for( int i=0 ; i<2 ; i++ ){
      for( int j=0 ; j<2 ; j++ ){
        for( int k=0 ; k<2 ; k++ ){
          peak=pkfac.getPixelInstance(x_lim[i],y_lim[j],z_lim[k],
                                      time[k][0],time[k][1]);
          hkl[0][i+2*j+4*k]=Math.round(peak.h());
          hkl[1][i+2*j+4*k]=Math.round(peak.k());
          hkl[2][i+2*j+4*k]=Math.round(peak.l());
        }
      }
    }

    // set the peak to null so the garbage collector can reclaim it
    peak=null;

    // the first index is h,k,l and the second index is min,max
    int[][] hkl_lim={{hkl[0][0],hkl[0][0]},
                     {hkl[1][0],hkl[1][0]},
                     {hkl[2][0],hkl[2][0]}};
    
    // sort out the min and max values
    for( int i=1 ; i<8 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        hkl_lim[j][0]=Math.min(hkl_lim[j][0],hkl[j][i]);
        hkl_lim[j][1]=Math.max(hkl_lim[j][1],hkl[j][i]);
      }
    }

    return hkl_lim;
  }
  
  /**
   * Checks the allowed indices of hkl given the centering type.
   *
   * @param type the type of centering operation. Acceptable values
   * are primative (1), a-centered (2), b-centered (3), c-centered
   * (4), [f]ace-centered (5), [i] body-centered (6), or
   * [r]hombohedral-centered (7)
   *
   * @return true if the hkl is allowed false otherwise
   */
  private boolean checkCenter(int h, int k, int l, int type){
    if(type==1){       // primative
      return true;
    }else if(type==2){ // a-centered
      int kl=(int)Math.abs(k+l);
      return ( (kl%2)==0 );
    }else if(type==3){ // b-centered
      int hl=(int)Math.abs(h+l);
      return ( (hl%2)==0 );
    }else if(type==4){ // c-centered
      int hk=(int)Math.abs(h+k);
      return ( (hk%2)==0 );
    }else if(type==5){ // [f]ace-centered
      int hk=(int)Math.abs(h+k);
      int hl=(int)Math.abs(h+l);
      int kl=(int)Math.abs(k+l);
      return ( (hk%2)==0 && (hl%2)==0 && (kl%2)==0 );
    }else if(type==6){ // [i] body-centered
      int hkl=(int)Math.abs(h+k+l);
      return ( (hkl%2)==0 );
    }else if(type==7){ // [r]hombohedral-centered
      int hkl=Math.abs(-h+k+l);
      return ( (hkl%3)==0 );
    }

    return false;
  }

  /**
   * Determine the observed intensity of the peak at its (rounded)
   * pixel position.
   */
  private void getObs(Peak peak, DataSet ds, int[][] ids){
    if( ds==null || peak==null ) return;
    int id=ids[(int)Math.round(peak.x())][(int)Math.round(peak.y())];
    int z=(int)Math.round(peak.z());

    peak.ipkobs((int)getObs(ds,id,z));
  }

  /**
   * Determine the observed intensity of the given id and time slice
   * number.
   */
  private static float getObs(DataSet ds, int id, int z){
    if( ds==null ) return 0f;

    Data d=ds.getData_entry(id);
    if(d==null) return 0f;

    return (d.getY_values())[z];
  }

  /**
   * @return a 1D array of the form Xmin, Ymin,Xmax, Ymax
   */
  private static int[] getBounds(int[][] ids){
    int[] bounds={-1,-1,-1,-1}; // see javadocs for meaning
    
    // search for min
    outer: for( int i=0 ; i<ids.length ; i++ ){
      for( int j=0 ; j<ids[0].length ; j++ ){
        if(ids[i][j]==-1) continue; // there is nothing here
        bounds[0]=i;
        bounds[1]=j;
        break outer;
      }
    }

    // search for max
    outer: for( int i=ids.length-1 ; i>bounds[0] ; i-- ){
      for( int j=ids[0].length-1 ; j>bounds[1] ; j-- ){
        if(ids[i][j]==-1) continue; // there is nothing here
        bounds[2]=i;
        bounds[3]=j;
        break outer;
      }
    }
    
    if( (bounds[0]==-1 && bounds[2]==-1) || (bounds[0]==-1 && bounds[2]==-1) ){
      for( int i=0 ; i<4 ; i++ )
        bounds[i]=-1;
    }else{
      if(bounds[0]==-1)
        bounds[0]=bounds[2];
      else if(bounds[2]==-1)
        bounds[2]=bounds[0];
      if(bounds[1]==-1)
        bounds[1]=bounds[3];
      else if(bounds[3]==-1)
        bounds[3]=bounds[1];
    }

    return bounds;
  }

  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Operator op = new Integrate();
    op.CopyParametersFrom( this );
    return op;
  }
  
  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] ){
    // create the parameters to pass to all of the different operators
    String prefix="/IPNShome/pfpeterson/data/SCD/";
    String datfile=prefix+"SCD06496.RUN";
    DataSet rds = (new RunfileRetriever(datfile)).getDataSet(1);
    float monct=138568f;
    
    // load the calibration file, note that we are using line 1
    LoadSCDCalib lsc=new LoadSCDCalib(rds,prefix+"instprm.dat",1,null);
    System.out.println("LoadSCDCalib.RESULT="+lsc.getResult());
    
    // load an orientation matrix
    LoadOrientation lo=new LoadOrientation(rds,new LoadFileString(prefix+"int_quartz.mat"));
    System.out.println("LoadOrientation.RESULT="+lo.getResult());

    // integrate the dataset
    Integrate op = new Integrate( rds, prefix, "quartz" );
    Object res=op.getResult();
    // print the results
    System.out.print("Integrate.RESULT=");
    if(res instanceof ErrorString){
      System.out.println(res);
    }else if(res instanceof Vector){
      System.out.println("");
      Vector peaks=(Vector)res;
      for( int i=0 ; i<peaks.size() ; i++ ){
        System.out.println(peaks.elementAt(i));
      }
    }

    System.exit(0);
  }
}
