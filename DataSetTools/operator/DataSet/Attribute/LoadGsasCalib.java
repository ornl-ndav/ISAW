/*
 * File:  LoadGsasCalib.java
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.7  2003/03/06 23:21:05  pfpeterson
 *  Changed call from fixSeparator to setForwardSlash
 *
 *  Revision 1.6  2003/02/06 20:34:37  dennis
 *  Added getDocumentation() method. (Tyler Stelzer)
 *
 *  Revision 1.5  2002/11/27 23:16:41  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/09/27 15:02:03  pfpeterson
 *  Makes sure iparm file is readable and exists.
 *
 *  Revision 1.3  2002/08/06 21:29:06  pfpeterson
 *  Fixed small bug with the specification of the parameter file.
 *
 *  Revision 1.2  2002/08/05 19:14:51  pfpeterson
 *  Improved reading method and now updates the effective/bank
 *  positions in the Data Attributes.
 *
 *  Revision 1.1  2002/07/10 16:04:22  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.retriever.RunfileRetriever;
import  DataSetTools.gsastools.GsasCalib;

/**
 * This operator loads the time-of-flight to d-space conversion
 * parameters from a GSAS instrument parameter file. The only lines
 * that are read are the ICONS lines.
 */

public class LoadGsasCalib extends    DS_Attribute {
    private static boolean DEBUG=false;
    private boolean seq_numbering;

    /* ---------------------- DEFAULT CONSTRUCTOR ------------------------ */
    /**
     * Construct an operator with a default parameter list.  If this
     * constructor is used, the operator must be subsequently added to
     * the list of operators of a particular DataSet.  Also,
     * meaningful values for the parameters should be set ( using a
     * GUI ) before calling getResult() to apply the operator to the
     * DataSet this operator was added to.
     */

    public LoadGsasCalib( ){
        super( "Load Gsas Calibration" );
        this.seq_numbering=false;
    }
    
  
    /* -------------------- FULL CONSTRUCTOR -------------------------- */
    /**
     *  Construct an operator for a specified DataSet and with the
     *  specified parameter values so that the operation can be
     *  invoked immediately by calling getResult().
     *
     *  @param  ds          The DataSet to which the operation is applied
     *  @param  iparm       The name of the iparm file.
     */

    public LoadGsasCalib( DataSet ds, LoadFileString  iparm, boolean seq_num ){
        this();                       // do the default constructor, then set
                                      // the parameter value(s) by altering a
                                      // reference to each of the parameters

        parameters=new Vector();
        addParameter( new Parameter("Instrument Parameter File", iparm) );
        addParameter( new Parameter("Sequential Bank Numbering",
                                    new Boolean(seq_num)) );

        setDataSet( ds );         // record reference to the DataSet that
                                  // this operator should operate on
    }


    /* -------------------------- getCommand ----------------------------- */
    /**
     * @return     the command name to be used with script processor: in
     *             this case, LoadGsasCalib
     */
    public String getCommand(){
        return "LoadGsasCalib";
    }


    /* ------------------------ setDefaultParmeters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        parameters = new Vector();  // must do this to clear any old parameters
        
        addParameter(new Parameter( "Instrument Parameter File",
                                    new LoadFileString("")));
        addParameter( new Parameter("Sequential Bank Numbering", 
                                    new Boolean(false)) );
    }
    
    
    /* -------------------------- getResult ----------------------------- */
    
    public Object getResult(){  
        DataSet ds = getDataSet();
        String iparm = getParameter(0).getValue().toString();
        iparm=FilenameUtil.setForwardSlash(iparm);
        this.seq_numbering=
            ((Boolean)getParameter(1).getValue()).booleanValue();
        if(DEBUG)System.out.println("seq_numbering="+this.seq_numbering);
        TextFileReader fr=null;

        File iparmfile=new File(iparm);
        if(! iparmfile.exists() ){
            return new ErrorString(iparm+" does not exist");
        }
        if(! iparmfile.canRead() ){
            return new ErrorString(iparm+" is not readable");
        }

        try{
            fr = new TextFileReader(iparm);
            String temp;
            int bankNum  = 0;
            float dif_c  = 0f;
            float dif_a  = 0f;
            float t_zero = 0f;
            float l_one  = 0f;
            float l_two  = 0f;
            float bragg  = 0f;
            float tilt   = 0f;;
            String line;
            while(!fr.eof()){
                fr.skip_blanks();
                line=fr.read_line();
                if(line.indexOf("ICONS")>0){
                    fr.unread(); // found a good line, put it back in the stream
                    //fr.read_String(); // skip the INS tag
                    bankNum=this.readBankNum(fr,"ICONS");
                    dif_c=fr.read_float();
                    dif_a=fr.read_float();
                    t_zero=fr.read_float();
                    fr.read_line(); // gooble the rest of the line
                    this.associate(bankNum,dif_c,dif_a,t_zero);
                }else if(line.indexOf("BNKPAR")>0){
                    fr.unread(); // found a good line, put it back in the stream
                    //fr.read_String(); // skip the INS tag
                    bankNum=this.readBankNum(fr,"BNKPAR");
                    l_two=fr.read_float();
                    bragg=fr.read_float();
                    fr.read_line(); // gooble the rest of the line
                    this.assocDetPos(bankNum,l_one,l_two,bragg);
                }else if(line.indexOf("FPATH1")>0){
                    fr.unread(); // found a good line, put it back in the stream
                    fr.read_String(); // skip the INS tag
                    fr.read_String(); // skip the FPATH1 tag
                    l_one=fr.read_float(); // get l_1
                    fr.read_line(); // gooble the rest of the line
                }else if(line.indexOf("DETAZM")>0){
                    fr.unread(); // found a good line, put it back in the stream
                    //fr.read_String(); // skip the INS tag
                    bankNum=this.readBankNum(fr,"BNKPAR");
                    tilt=fr.read_float();
                    fr.read_line(); // gooble the rest of the line
                    this.assocDetAzm(bankNum,tilt);
                }
            }
        }catch(IOException e){
            System.err.println("IOException: "+e.getMessage());
        }finally{
            if(fr!=null) try{
                fr.close();
            }catch(IOException e){
                System.err.println("Could not close "+iparm+":"
                                   +e.getMessage());
            }
        }
        int index=iparm.lastIndexOf("/");
        if(index>=0)iparm=iparm.substring(index+1,iparm.length());
        ds.setAttribute(new StringAttribute(Attribute.GSAS_IPARM,iparm));
        ds.addLog_entry("Read Instrument Parameter File: "+iparm);
        return "Read Instrument Parameter File";
    }  

    /**
     * Determines the bank number and goobles the tag it may or may
     * not be attached to. 
     */
    private int readBankNum(TextFileReader fr, String tag){
        String temp=null;
        try{
            fr.read_String(3);
            int banknum=fr.read_int(3);
            fr.read_String(6);
            return banknum;
            /*temp=fr.read_String();
              if(temp.endsWith(tag)){
              int index=temp.indexOf(tag);
              temp=temp.substring(0,index);
              return (new Integer(temp)).intValue();
              }else{
              fr.unread();
              int banknum=fr.read_int();
              fr.read_String();
              return banknum;
              }*/
        }catch(IOException e){
            System.err.println("IOException: "+e.getMessage());
            return -1;
        }
    }

    /**
     * Method to associate the gsas information with the data block
     * specified.
     */
    private void associate(int bankNum,float dif_c,float dif_a,float t_zero){
        GsasCalibAttribute calibAttr;
        Data d=this.getData(bankNum);
        if(d==null)return;

        // add the attribute to the data block
        if(DEBUG)System.out.println(bankNum+"A,C,T:"+dif_c+", "+dif_a+", "
                                    +t_zero);
        calibAttr=new GsasCalibAttribute(Attribute.GSAS_CALIB,
                                         new GsasCalib(dif_c,dif_a,t_zero));
        d.setAttribute(calibAttr);
    }

    /**
     * Associate the detector position with the given Data block.
     *
     * @param bankNum The bank number.
     * @param l_one   The source to sample distace.
     * @param l_two   The sample to detector distace.
     * @param bragg   The bragg angle of the detector in degrees,
     *                frequently called two-theta.
     */
    private void assocDetPos(int bankNum,float l_one,float l_two,float bragg){
        Data d  = this.getData(bankNum);
        if(d==null)return;

        if(DEBUG)System.out.println(bankNum+"L,L,B:"+l_one+", "+l_two
                                    +", "+bragg);
        // set the primary flight path
        FloatAttribute l_one_attr=new FloatAttribute(Attribute.INITIAL_PATH,
                                                     l_one);
        d.setAttribute(l_one_attr);

        // set the detector position relative to the sample
        DetectorPosition det_pos=(DetectorPosition)
            d.getAttributeValue(Attribute.DETECTOR_POS);
        if(det_pos!=null){
            float[] cyl_coords=det_pos.getCylindricalCoords();
            det_pos.setCylindricalCoords(l_two,(float)(bragg*Math.PI/180f),
                                         cyl_coords[2]);
        }else{
            Position3D pos=new Position3D();
            pos.setCylindricalCoords(l_two,(float)(bragg*Math.PI/180f),0f);
            det_pos=new DetectorPosition(pos);
        }
        d.setAttribute(new DetPosAttribute(Attribute.DETECTOR_POS,det_pos));
    }
    /**
     * Associate the detector azimuthal angle with the given data block.
     *
     * @param tilt    The angle of the detector out of the scattering 
     *                plane in degrees. 
     */
    private void assocDetAzm(int bankNum, float tilt){
        Data d = this.getData(bankNum);
        if(d==null)return;
        
        if(DEBUG)System.out.println(bankNum+"T:"+tilt);
        DetectorPosition det_pos=(DetectorPosition)
            d.getAttributeValue(Attribute.DETECTOR_POS);
        if(det_pos!=null){
            float[] sph_coords=det_pos.getSphericalCoords();
            det_pos.setSphericalCoords(sph_coords[0],
                                       (float)((90f-tilt)*Math.PI/180f),
                                       sph_coords[1]);
        }else{
            Position3D pos=new Position3D();
            pos.setSphericalCoords(0f, (float)((90f-tilt)*Math.PI/180f), 0f);
            det_pos=new DetectorPosition(pos);
        }
        d.setAttribute(new DetPosAttribute(Attribute.DETECTOR_POS,det_pos));
    }

    /**
     * Get the appropriate Data block according to numbering scheme.
     */
    private Data getData(int bankNum){
        DataSet ds = getDataSet();
        Data    d  = null;

        // get the appropriate data block
        if(DEBUG)System.out.print(bankNum+": ");
        if(this.seq_numbering){
            d=ds.getData_entry(bankNum-1);
        }else{
            d=ds.getData_entry_with_id(bankNum);
        }

        // don't bother if can't do anything
        if(d==null){ 
            if(DEBUG)System.out.println("failed");
            return null;
        }else{
            return d;
        }
        
    }

    /* ---------------------------- clone ----------------------------- */
    /**
     * Get a copy of the current LoadGsasCalib Operator.  The list of
     * parameters and the reference to the DataSet to which it applies
     * is copied.
     */
    public Object clone(){
        LoadGsasCalib new_op    = new LoadGsasCalib( );
                                              // copy the data set associated
                                              // with this operator
        new_op.setDataSet( this.getDataSet() );
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }
    
    
    public String getDocumentation()
    {
      StringBuffer Res = new StringBuffer();
      Res.append("@overview This operator loads the time-of-flight to d-space");
       Res.append(" conversion parameters from a GSAS instrument parameter");
       Res.append(" file. The only lines that are read are the ICONS lines.");

      Res.append("@algorithm Open the file if it exists and is readable.");
       Res.append(" Then Read the time-of-flight to d-space conversion");
       Res.append(" only reading the ICONS lines.");

      Res.append("@param  ds  The DataSet to which the");
       Res.append(" operation is applied");
      Res.append("@param  iparm  The name of the iparm file.");
      Res.append("@param  seq_num  Whether to use sequential bank");
       Res.append(" numbering or not.");

      Res.append("@return Returns an error string if the file does not exist");
      Res.append(" or the file is not readable.  If it is successful, it");
      Res.append(" returns the String \"Read Instrument Parameter File\"");

      Res.append("@error file does not exist");
      Res.append("@error file is not readable");
  
     return Res.toString();
    }
    
    
    

    /*public static void main(String args[]){
      String runfile="/IPNShome/pfpeterson/data/CsC60/SEPD18805.RUN";
      LoadFileString calfile=new LoadFileString("/IPNShome/pfpeterson/data/CsC60/iparm_oct00_pdf.sepd");
      //System.out.println(runfile);
      DataSet rds=(new RunfileRetriever(runfile)).getDataSet(1);
      System.out.println(rds+" "+calfile);
      LoadGsasCalib op=new LoadGsasCalib(rds,calfile,false);
      op.getResult();
      }*/
}
