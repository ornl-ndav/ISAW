/*
 * File:  LoadSCDCalib.java 
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
 * Revision 1.19  2006/07/10 16:25:53  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.18  2006/03/10 05:14:47  dennis
 * Fixed javadoc error.
 *
 * Revision 1.17  2004/04/12 21:33:18  dennis
 * Now gets detector ID using the Util.detectorID() method, rather
 * than assuming that there is an explict DETECTOR_IDS attribute
 * present.
 *
 * Revision 1.16  2004/03/15 03:28:23  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.15  2004/03/02 17:17:02  dennis
 * Moved some prints into   if (DEBUG)  statement.
 *
 * Revision 1.14  2004/01/22 02:39:54  bouzekc
 * Removed/commented out unused imports/variables.
 *
 * Revision 1.13  2004/01/07 14:40:11  dennis
 * Now also uses the calibration file to adjust the data grid positions
 * and to set the effective detector positions based on the calibrated
 * data grid positions.
 *
 * Revision 1.12  2003/12/15 02:20:38  bouzekc
 * Removed unused imports.
 *
 * Revision 1.11  2003/06/09 19:48:31  pfpeterson
 * Now can read 'new' experiment files. Also added ability to read in all
 * unique detectors from a file when -1 is specified as the 'line to use'.
 *
 * Revision 1.10  2003/06/09 15:01:47  pfpeterson
 * Better memory usage since only one copy of each attribute is created
 * and added to the data.
 *
 * Revision 1.9  2003/05/15 18:43:39  pfpeterson
 * Return string now tells detector number used.
 *
 * Revision 1.8  2003/05/15 17:37:48  pfpeterson
 * Trimed off extra whitespace from result string.
 *
 * Revision 1.7  2003/05/08 19:52:50  pfpeterson
 * Added check that the calibration file is a regular file.
 *
 * Revision 1.6  2003/03/14 17:28:57  pfpeterson
 * Changed errors into warnings when calibration file is not found or
 * not readable. Warnings are printed to the StatusPane and returned.
 *
 * Revision 1.5  2003/02/12 15:30:55  pfpeterson
 * Moved various debug statements into if(DEBUG) conditionals.
 *
 * Revision 1.4  2003/02/11 23:05:03  pfpeterson
 * Added ability to read calibration from an experiment file.
 *
 * Revision 1.3  2003/02/06 20:34:47  dennis
 * Added getDocumentation() method. (Tyler Stelzer)
 *
 * Revision 1.2  2002/11/27 23:16:41  pfpeterson
 * standardized header
 *
 *
 */
package DataSetTools.operator.DataSet.Attribute;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Parameters.IntegerPG;
import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.SpecialStrings.IntListString;
import gov.anl.ipns.Util.SpecialStrings.LoadFileString;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.Float1DAttribute;
import DataSetTools.dataset.FloatAttribute;
import DataSetTools.dataset.StringAttribute;
import DataSetTools.dataset.Grid_util;
import DataSetTools.dataset.UniformGrid;
import DataSetTools.operator.Operator;
import DataSetTools.operator.Parameter;
import DataSetTools.operator.Generic.TOF_SCD.Util;
import DataSetTools.util.SharedData;


/** 
 * This operator will add the calibration information from the file
 * produced by A.J. Schultz's code. This sets the primary flight path,
 * detector center angle, secondary flight path, and the bin to real
 * space conversion information.
 */
public class LoadSCDCalib extends DS_Attribute{
    private static final String     TITLE  = "Load SCD Calibration";
    private static final boolean    DEBUG  = false;

    private Vector    calibrations = null;
    private Hashtable calibrations_table = null;
    private Vector    detNums = null;

    /**
     *  Creates operator with title "Load SCD Calibration" and a
     *  default list of parameters.
     */  
    public LoadSCDCalib(){
	super( TITLE );
    }
    
    /** 
     *  Creates operator with title "Load SCD Calibration" and the
     *  specified list of parameters. The getResult method must still
     *  be used to execute the operator.
     *
     *  @param calib_file Calibration file to use
     */
    public LoadSCDCalib( DataSet ds, 
                         String  calib_file, 
                         int     linenum,
                         String  groups) {
	this(); 
	parameters = new Vector();
        addParameter( new Parameter("Calibration File",
                                           new LoadFileString(calib_file)));
        addParameter( new Parameter("Line to use", new Integer(linenum)));
        addParameter( new Parameter("Group IDs", new IntListString(groups)));

        setDataSet(ds); // record referece to the DataSet that this
                        // operator should operate on
    }

    /** 
     * Get the name of this operator to use in scripts
     * 
     * @return "LoadSCDCalib", the command used to invoke this
     * operator in Scripts
     */
    public String getCommand(){
        return "LoadSCDCalib";
    }

    /** 
     * Sets default values for the parameters.  This must match the
     * data types of the parameters.
     */
    public void setDefaultParameters(){
        parameters = new Vector();
        addParameter( new Parameter("Calibration File", new LoadFileString()));
        addParameter( new IntegerPG("Line to use",      -1 ) );
        addParameter( new Parameter("Group IDs",        new IntListString()) );
    }
    
    /** 
     *  Executes this operator using the values of the current parameters.
     *
     *  @return If successful, this operator returns a vector of Peak
     *  objects.
     */
    public Object getResult(){
        DataSet ds=getDataSet();
        Data    d=null;
        this.calibrations=null;
        this.detNums=null;
        String calibfile   = getParameter(0).getValue().toString();
        int    linenum;
        try{
          linenum=((Integer)getParameter(1).getValue()).intValue();
        }catch(NumberFormatException e){
          linenum=-1;
        }
        String list_string = getParameter(2).getValue().toString();

        // determine if the calibration file is usable
        File file=new File(calibfile);
        if(! file.exists() ){
          String warn_msg="WARNING(LoadSCDCalib): file does not exist "
            +calibfile;
          SharedData.addmsg(warn_msg);
          return(warn_msg);
        }else if(! file.canRead() ){
          String warn_msg="WARNING(LoadSCDCalib): cannot read file "+calibfile;
          SharedData.addmsg(warn_msg);
          return warn_msg;
        }else if(! file.isFile() ){
          String warn_msg="WARNING(LoadSCDCalib): not regular file "+calibfile;
          SharedData.addmsg(warn_msg);
          return warn_msg;
        }
        file=null;

        // read in the calibration(s)
        ErrorString err=this.readCalib(calibfile,linenum);
        if(err!=null)
            return err;

        // associate the attributes with the Data
        StringAttribute filenameAttr=
                       new StringAttribute(Attribute.SCD_CALIB_FILE,calibfile);
        if( list_string!=null && list_string.trim().length()!=0 ){
            // use the list we were given
            int[] ids=IntList.ToArray(list_string);
            for( int i=0 ; i<ids.length ; i++ ){
                d=ds.getData_entry_with_id(i);
                assoc(d,filenameAttr);
            }
        }else{
            // don't bother with the id list, it is empty
            for( int i=0 ; i<ds.getNum_entries() ; i++ ){
                d=ds.getData_entry(i);
                assoc(d,filenameAttr);
            }
        }

        // return an error if nothing was associated
        if(this.detNums==null || this.detNums.size()<=0)
          return new ErrorString("Did not associate any detectors");

        // print some information to StatusPane and create return string
        Calib kalib=null;
        String detList="Loaded calibration for det#";
        boolean inList;
        for( int i=0 ; i<this.calibrations.size() ; i++ ){
          kalib=(Calib)this.calibrations.elementAt(i);
          inList=(this.detNums.contains(new Integer(kalib.detNum)));
          if(inList || kalib.detNum<0){
            SharedData.addmsg("Using '"+kalib.descr.trim()
                              +"' on det#"+kalib.detNum);
            if(inList)
              detList=detList+" "+kalib.detNum;
            else
              detList="Loaded calibration for all detectors";
          }
        }

        loadCalibrations( calibfile );
        applyCalibration( ds );

        return detList;
    }
    
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){
        Operator op = new LoadSCDCalib();
        op.CopyParametersFrom( this );
        return op;
    }
    
    /**
     * Associate the calibration to the data as appropriate.
     */
    private void assoc( Data d, StringAttribute filenameAttr ){
        if(d==null) return;
            
        int oldDetNum = Util.detectorID( d );
        if ( oldDetNum == -1 )
          oldDetNum = 0;         // later code uses 0 to indicate not found

        Calib kalib=null;
        for( int i=0 ; i<this.calibrations.size() ; i++ ){
          kalib=(Calib)this.calibrations.elementAt(i);

          // if detNum=-1 don't bother checking against existing number
          if(kalib.detNum!=-1 && oldDetNum!=0 && oldDetNum!=kalib.detNum)
            continue;

          // add the attributes
          d.setAttribute(kalib.detA_Attr);
          d.setAttribute(kalib.detD_Attr);
          d.setAttribute(kalib.L1_Attr);
          d.setAttribute(kalib.calib_Attr);
          d.setAttribute(filenameAttr);

          // add the detector number to the list of what was used
          if(kalib.detNum>0)
            this.addDetNum(kalib.detNum);
          else
            this.addDetNum(oldDetNum);
        }
    }

    /**
     * The line number parameter is ignored if this is an experiment file.
     */
    public static Object readCalib(TextFileReader tfr, boolean isexpfile,
                         int linenum)throws IOException, NumberFormatException{
      float[] calib=null;
      String descr=null;

      if(isexpfile){
        descr="";
        int i=1;
        String start=null;
        calib=new float[9];
        calib[0]=-1f;
        String detTag=null;
        while(!tfr.eof() && i<9 ){
          start=tfr.read_String();
          if(start.equals("INST")){ // old style candidate
            start=tfr.read_String();
            if(start.equals("DETA")){ // detector angle in plane
              calib[1]=tfr.read_float();
              i++;
            }else if(start.equals("DETD")){ // detector distance
              calib[2]=tfr.read_float();
              i++;
            }else if(start.equals("L1")){ // initial flight path
              calib[3]=tfr.read_float();
              i++;
            }else if(start.equals("TZERO")){ // time offset
              calib[4]=tfr.read_float();
              i++;
            }else if(start.equals("X2CM")){ // x-pixel to cm
              calib[5]=tfr.read_float();
              i++;
            }else if(start.equals("Y2CM")){ // y-pixel to cm
              calib[6]=tfr.read_float();
              i++;
            }else if(start.equals("XLEFT")){ // x-offset in cm
              calib[7]=tfr.read_float();
              i++;
            }else if(start.equals("YLOWER")){ // y-offset in cm
              calib[8]=tfr.read_float();
              i++;
            }
          }else if(start.equals("DET")){ // new style candidate
            tfr.unread();
            
            start=tfr.read_String(6);
            if(detTag==null){
              detTag=start;
              try{
                calib[0]=Float.parseFloat(start.substring(3).trim());
              }catch(NumberFormatException e){
                // let it drop on the floor
              }
            }
            if(!start.equals(detTag)){
              tfr.unread();
              break;
            }
            start=tfr.read_String();
            if(start.equals("DETA")){ // detector angle in plane
              calib[1]=tfr.read_float();
              i++;
            }else if(start.equals("DETD")){ // detector distance
              calib[2]=tfr.read_float();
              i++;
            }else if(start.equals("L1")){ // initial flight path
              calib[3]=tfr.read_float();
              i++;
            }else if(start.equals("TZERO")){ // time offset
              calib[4]=tfr.read_float();
              i++;
            }else if(start.equals("X2CM")){ // x-pixel to cm
              calib[5]=tfr.read_float();
              i++;
            }else if(start.equals("Y2CM")){ // y-pixel to cm
              calib[6]=tfr.read_float();
              i++;
            }else if(start.equals("XLEFT")){ // x-offset in cm
              calib[7]=tfr.read_float();
              i++;
            }else if(start.equals("YLOWER")){ // y-offset in cm
              calib[8]=tfr.read_float();
              i++;
            }
          }
          tfr.read_line(); // gobble the rest of the line
        }
      }else{ // when isn't experiment file
        if(linenum>0){
          for( int i=0 ; i<=linenum ; i++ )
              tfr.read_line();
          tfr.unread();
        }
        calib=new float[9];
        int numErrors=0;
        while(true){
          try{
            for( int i=0 ; i<9 ; i++ )
              calib[i]=tfr.read_float();
            descr=tfr.read_line();
            break;
          }catch(NumberFormatException e){
            numErrors++;
            if(numErrors>9) return new ErrorString("Error reading file");
            descr=tfr.read_line();
          }
        }
      }

      if(DEBUG){
        for( int i=0 ; i<9 ; i++ )
          System.out.println(i+":"+calib[i]);
      }

      // return the appropriate type
      if(calib!=null){
        return new Object[] {calib,descr};
      }else{
        return new ErrorString("Something went wrong");
      }
    }

    /**
     * Read in the calibration parameters.
     */
    private ErrorString readCalib( String filename, int linenum ){
        TextFileReader tfr=null;

        try{
          tfr=new TextFileReader(filename);
          Object res=null;
          if(filename.toUpperCase().endsWith(".X")){
            while(!tfr.eof()){
              res=readCalib(tfr,true,linenum);
              if(res instanceof ErrorString) return (ErrorString)res;
              addCalib(new Calib(filename,(float[])((Object[])res)[0]));
            }
          }else{
            while(true){
              try{
                res=readCalib(tfr,false,linenum);
              }catch(NumberFormatException e){
                // let it drop on the floor
              }
              if(res instanceof ErrorString)
                return (ErrorString)res;
              else if(res==null)
                return null;
              addCalib(new Calib((String)((Object[])res)[1],
                                 (float[])((Object[])res)[0]));
              if(linenum>0) break;
            }
          }
        }catch(EOFException e){ // this is not a big deal
          return null;
        }catch(IOException e){
          if(!e.getMessage().toUpperCase().startsWith("END OF FILE")){
            e.printStackTrace();
            return new ErrorString(e);
          }
        }finally{
          if(tfr!=null){
            try{
              tfr.close();
            }catch(IOException e){
              // let it drop on the floor
            }
          }
        }

        return null;
    }
    
    /**
     * Add a detector number to the list of unique detectors modified
     */
    private void addDetNum(int detNum){
      if(this.detNums==null)
        this.detNums=new Vector();
      Integer DetNum=new Integer(detNum);
      if(! this.detNums.contains(DetNum) )
        this.detNums.add(DetNum);
    }

    /**
     * Add a calibration to the list of unique calibrations available
     */
    private void addCalib(Calib kalib){
      // initialize the calibration vector if necessary
      if(this.calibrations==null)
        this.calibrations=new Vector();

      // get the detector number of the supplied calibration
      int detNum=kalib.detNum;

      // is this unique?
      Calib myCalib=null;
      for( int i=0 ; i<this.calibrations.size() ; i++ ){
        myCalib=(Calib)this.calibrations.elementAt(i);
        if(detNum==myCalib.detNum) return; // jump out
      }

      // fix the L1_Attr of other calibrations if necessary/possible
      if(!(new Float(0f)).equals(kalib.L1_Attr.getValue())){
        for( int i=0 ; i<this.calibrations.size() ; i++ ){
          myCalib=(Calib)this.calibrations.elementAt(i);
          if((new Float(0f)).equals(myCalib.L1_Attr.getValue()))
            myCalib.L1_Attr=kalib.L1_Attr;
        }
      }

      // finally add the new calibration
      this.calibrations.add(kalib);
    }

    public String getDocumentation()
    {
      StringBuffer Res = new StringBuffer();
      Res.append("@overview This operator will add the calibration");
      Res.append(" information from the file produced by A.J. Schultz's code");

      Res.append("@algorithm This operator will add the calibration");
      Res.append(" information from the file produced by A.J. Schultz's");
      Res.append(" code. This sets the primary flight path, detector center");
      Res.append(" angle, secondary flight path, and the bin to real space");
      Res.append(" conversion information.");

      Res.append("@param ds The DataSet to operate on.");
      Res.append("@param calib_file Calibration file to use.");
      Res.append("@param linenum The line number to use. If this is blank or "
                 +"set to -1 then all unique detector numbers will be read in "
                 +"with the those at the top of the file taking precedence "
                 +"over the lower ones.");
      Res.append("@param groups The group ID numbers to use."); 

      Res.append("@return Returns an error string if the file does not exist");
      Res.append(" or the file is not readable.  If it is successful, it");
      Res.append(" returns A vector of Peak objects.");

      Res.append("@error FAILURE: file does not exist");
      Res.append("@error FAILURE: file is not readable");
      Res.append("@error FAILURE");
  
     return Res.toString();
    }

  /**
   * Inner class to make dealing with calibrations easier.
   */
  private class Calib{
    String           descr      = null;
    int              detNum     = 0;
    FloatAttribute   detA_Attr  = null;
    FloatAttribute   detD_Attr  = null;
    FloatAttribute   L1_Attr    = null;
    Float1DAttribute calib_Attr = null;

    Calib(String descr,float[] innercalib){
      // copy over information from the supplied information
      this.descr=descr;
      this.detNum=(int)Math.round(innercalib[0]);
      float detA=innercalib[1];
      float detD=innercalib[2];
      float L1=innercalib[3];
      float[] calib=new float[5];
      for( int i=0 ; i<5 ; i++ )
        calib[i]=innercalib[i+4];

      // convert from cm to m
      detD = detD/100f;
      L1   = L1/100f;

      // create attributes from the given values
      this.detA_Attr=new FloatAttribute(Attribute.DETECTOR_CEN_ANGLE,detA);
      this.detD_Attr=new FloatAttribute(Attribute.DETECTOR_CEN_DISTANCE,detD);
      this.L1_Attr=new FloatAttribute(Attribute.INITIAL_PATH,L1);
      calib_Attr=new Float1DAttribute(Attribute.SCD_CALIB,calib);
    }
  }


/* ----------------------------- loadCalibrations -------------------- */
/*
 *  Load calibration data into form used for adjusting the data grid
 *  size and position. 
 */ 
  private void loadCalibrations( String file_name )
  {
    try
    {
      TextFileReader tfr = new TextFileReader( file_name );
      calibrations_table = new Hashtable();
      tfr.read_line();
      boolean done = false;
      while ( !done && !tfr.end_of_data() )
      {
        int det_num = tfr.read_int();
        float det_A   = tfr.read_float();
        float det_D   = tfr.read_float();
        float l1      = tfr.read_float();
        float t0      = tfr.read_float();
        float x2cm    = tfr.read_float();
        float y2cm    = tfr.read_float();
        float xleft   = tfr.read_float();
        float ylower  = tfr.read_float();
        String calib_name = tfr.read_line();

        float calib[] = { det_A, det_D, l1, t0, x2cm, y2cm, xleft, ylower };
        Integer key = new Integer( det_num );
        if ( calibrations_table.get( key ) == null )    // new detector
        {
          System.out.println("Using calibration: " +calib_name +
                             " for " + det_num );
          calibrations_table.put( key, calib );
        }
        else
          done = true;                           // just use first calibrations
      }
    }
    catch ( Exception e )
    {
      System.out.println("Exception reading calibration file is " + e );
      e.printStackTrace();
      calibrations_table = null;
    }

    if ( calibrations_table != null )
    {
      Enumeration e = calibrations_table.elements();
      while ( e.hasMoreElements() )
        LinearAlgebra.print( (float[])e.nextElement() );
    }
  }


/* ----------------------------- applyCalibrations -------------------- */
/*
 *  Apply calibration data loaded by loadCalibrations to adjust the data
 *  grid positions and effective position information. 
 */
 private void applyCalibration( DataSet ds )
  {
    if ( calibrations_table == null )
    {
      System.out.println("No calibrations for DataSet " + ds );
      return;
    }
 
    int ids[] = Grid_util.getAreaGridIDs( ds );
    for ( int i = 0; i < ids.length; i++ )
    {
      UniformGrid grid = (UniformGrid)Grid_util.getAreaGrid( ds, ids[i] );
      float cal[] = (float[])calibrations_table.get( new Integer( ids[i] ) );
      if ( cal == null )
      {
        System.out.println("ERROR: No calibration for detector ID " + ids[i] );
        return;
      }
                                        // First adjust the grid according to
                                        // the calibration information
      int n_rows = grid.num_rows();
      int n_cols = grid.num_cols();

      float width  = n_cols * cal[4]/100;
      float height = n_rows * cal[5]/100;

      grid.setWidth( width );
      grid.setHeight( height );

      float xleft   = cal[6]/100;
      float ylower  = cal[7]/100;
      Vector3D base = grid.x_vec();
      Vector3D up   = grid.y_vec();
      base.normalize();
      up.normalize();

      float xcenter = xleft  + width / 2;
      float ycenter = ylower + height / 2;
      Vector3D center = grid.position();

      if ( DEBUG )
      {
        System.out.println("ORGINAL CENTER IS: " + center );
        System.out.println("Shift in X is : " + xcenter );
        System.out.println("Shift in Y is : " + ycenter );
      }

      base.multiply( xcenter );
      up.multiply( ycenter );
      center.add( base );
      center.add( up );
      grid.setCenter( center );

      Attribute T0_attribute = new FloatAttribute( Attribute.T0_SHIFT, cal[3] );
      Attribute l1_attribute = new FloatAttribute( Attribute.INITIAL_PATH,
                                                   cal[2]/100 );
      for ( int row = 1; row <= n_rows; row++ )
        for ( int col = 1; col <= n_cols; col++ )
        {
          grid.getData_entry( row, col ).setAttribute( l1_attribute );
          grid.getData_entry( row, col ).setAttribute( T0_attribute );
        }

      if ( DEBUG )
      {
        System.out.println ("NEW GRID IS " );
        System.out.println ("" + grid );
      }
                                               // Finally, adjust the
                                               // effective detector pixel
                                               // positions
      Grid_util.setEffectivePositions( ds, ids[i] );
    }
  }

}
