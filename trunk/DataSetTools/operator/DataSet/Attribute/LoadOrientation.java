/*
 * File:  LoadOrientation.java
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
 *  Revision 1.8  2003/02/12 22:54:03  pfpeterson
 *  Reads in the cell volume (again) if reading a matrix file.
 *
 *  Revision 1.7  2003/02/12 15:30:55  pfpeterson
 *  Moved various debug statements into if(DEBUG) conditionals.
 *
 *  Revision 1.6  2003/02/11 21:02:23  pfpeterson
 *  Now reads orientation matrix and lattice parameters from '.x' files
 *  as well as '.mat' files.
 *
 *  Revision 1.5  2002/11/27 23:16:41  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/11/21 22:53:14  dennis
 *  Added getDocumentation() method, docs to getResult() and test of
 *  getDocumentation() to main program.  Left old main program as
 *  comment. (Chris Bouzek)
 *
 *  Revision 1.3  2002/10/25 22:18:22  pfpeterson
 *  Uses MatrixFilter for choosing orientation matrix file.
 *
 *  Revision 1.2  2002/09/25 16:42:04  pfpeterson
 *  Now adds SCDhkl operator to the DataSet. Also removed dead code,
 *  updated variable names and updated documentation.
 *
 *  Revision 1.1  2002/08/22 15:13:59  pfpeterson
 *  Added to cvs.
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
import  DataSetTools.operator.DataSet.Information.XAxis.SCDhkl;
import  DataSetTools.operator.Generic.TOF_SCD.MatrixFilter;
import  DataSetTools.parameter.*;
import  DataSetTools.retriever.RunfileRetriever;

/**
 * This operator loads a orientation matrix and lattice parameters
 * from a matrix file produced by blind
 */

public class LoadOrientation extends    DS_Attribute {
    private static boolean DEBUG   = false;
    private static String  FAILURE = "Failed to read Orientation Matrix";
    private static String  SUCCESS = "Read Orientation Matrix";

    /* ---------------------- DEFAULT CONSTRUCTOR ------------------------ */
    /**
     * Construct an operator with a default parameter list.  If this
     * constructor is used, the operator must be subsequently added to
     * the list of operators of a particular DataSet.  Also,
     * meaningful values for the parameters should be set ( using a
     * GUI ) before calling getResult() to apply the operator to the
     * DataSet this operator was added to.
     */

    public LoadOrientation( ){
        super( "Load Orientation Matrix" );
    }
    
  
    /* -------------------- FULL CONSTRUCTOR -------------------------- */
    /**
     *  Construct an operator for a specified DataSet and with the
     *  specified parameter values so that the operation can be
     *  invoked immediately by calling getResult().
     *
     *  @param  ds          The DataSet to which the operation is applied
     *  @param  orient      The name of the iparm file.
     */

    public LoadOrientation(DataSet ds, LoadFileString  orient){
        this();                       // do the default constructor, then set
                                      // the parameter value(s) by altering a
                                      // reference to each of the parameters

        getParameter(0).setValue(orient.toString());
        //parameters=new Vector();
        //addParameter( new Parameter("Orientation Matrix File", orient) );

        setDataSet( ds );         // record reference to the DataSet that
                                  // this operator should operate on
    }


    /* -------------------------- getCommand ----------------------------- */
    /**
     * @return     the command name to be used with script processor: in
     *             this case, LoadOrientation
     */
    public String getCommand(){
        return "LoadOrientation";
    }


    /* ------------------------ setDefaultParameters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        parameters = new Vector();  // must do this to clear any old parameters
        
        LoadFilePG lfpg=new LoadFilePG("Orientation Matrix File",null);
        lfpg.setFilter(new MatrixFilter());
        addParameter(lfpg);

        /*addParameter(new Parameter( "Orientation Matrix File",
          new LoadFileString("")));*/
    }
    
 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator loads a orientation matrix ");
    s.append("and lattice parameters from a matrix file produced ");
    s.append("by blind.");
    s.append("@assumptions The file containing the orientation ");
    s.append("matrix and lattice parameter does indeed exist, .");
    s.append("and contains usable information in the correct ");
    s.append("format.");
    s.append("@algorithm Associate the orientation matrix and ");
    s.append("lattice parameter to the DataSet. The information ");
    s.append("is read from a matrix file.  New attributes, based");
    s.append("on the information in the file, are added to the ");
    s.append("DataSet, and a new SCDhkl Operator is added to the ");
    s.append("DataSet.");
    s.append("@param The DataSet to which the operation is applied.");
    s.append("@param The name of the parameter file.");
    s.append("@return A String which tells the user that the ");
    s.append("orientation matrix was read successfully.  Note ");
    s.append("that the DataSet is modifed, and appropriate ");
    s.append("messages are added to the log file.");
    s.append("@error Returns an error if the file containing the ");
    s.append("orientation matrix cannot be found.");
    s.append("@error Returns an error if the file containing the ");
    s.append("orientation matrix is a directory.");
    s.append("@error Returns an error if the file containing the ");
    s.append("orientation matrix has data with invalid number formats.");
    s.append("@error Returns an error if the file containing the ");
    s.append("orientation matrix cannot be closed after opening.");
    return s.toString();
  }
    
    /* -------------------------- getResult ----------------------------- */ 
    /**
     * Associate the orientation matrix and lattice parameter to the
     * DataSet. The information is read from a matrix file.
     * @return A String which tells the user that the orientation matrix 
     * was read successfully.
     */
    public Object getResult(){  
        DataSet ds = getDataSet();
        String iparm = getParameter(0).getValue().toString();
        iparm=FilenameUtil.fixSeparator(iparm);
        TextFileReader fr=null;
        boolean success=false;

        File orient_file=new File(iparm);
        if( !orient_file.exists()){
            SharedData.addmsg("No such file: "+iparm);
            return new ErrorString(FAILURE);
        }else if( orient_file.isDirectory()){
            SharedData.addmsg("Is a directory: "+iparm);
            return new ErrorString(FAILURE);
        }

        float[]   lat    = new float[6];
        float[][] orient = new float[3][3];
        float     vol    = 0f;
        try{
          fr = new TextFileReader(iparm);
          Object res;
          if(iparm.endsWith(".x")){ // reading from experiment file
            // read in the lattice parameters
            res=readLattice(fr,true);
            if(res instanceof ErrorString)
              return res;
            else
              lat=(float[])res;
            // read in the orientation matrix
            res=readOrient(fr,true);
            if(res instanceof ErrorString)
              return res;
            else
              orient=(float[][])res;
          }else{ // reading from matrix file
            // read in the orientation matrix
            res=readOrient(fr,false);
            if(res instanceof ErrorString)
              return res;
            else
              orient=(float[][])res;
            // read in the lattice parameters
            res=readLattice(fr,false);
            if(res instanceof ErrorString)
              return res;
            else
              lat=(float[])res;
            // grab the cell volume
            vol=fr.read_float();
          }
          success=true;
        }catch(IOException e){
          System.err.println("IOException: "+e.getMessage());
        }catch(NumberFormatException e){
          SharedData.addmsg("NumberFormatException: "+e.getMessage());
        }finally{
          if(fr!=null){
            try{
              fr.close();
            }catch(IOException e){
              System.err.println("Could not close "+iparm+":"
                                 +e.getMessage());
            }
          }else{
            return new ErrorString(FAILURE);
          }
        }
        if(!success) return new ErrorString(FAILURE);

        // print out debug information
        if(DEBUG){
            System.out.println("LATTICE PARAMETERS:");
            for( int i=0 ; i<3 ; i++ ){
                System.out.print(" "+lat[i]);
            }
            System.out.println("");
            for( int i=3 ; i<6 ; i++ ){
                System.out.print(" "+lat[i]);
            }
            System.out.println("");
            System.out.println("CELL VOLUME: "+vol);
            System.out.println("ORIENTATION MATRIX:");
            for( int i=0 ; i<3 ; i++ ){
                for( int j=0 ; j<3 ; j++ ){
                    System.out.print(" "+orient[i][j]);
                }
                System.out.println("");
            }
        }

        // add the new attributes to the DataSet
        int index=iparm.lastIndexOf("/");
        if(index>=0)iparm=iparm.substring(index+1,iparm.length());
        ds.setAttribute(new StringAttribute(Attribute.ORIENT_FILE,iparm));
        System.out.print("VOL="+vol);
        if(vol!=0f)
          ds.setAttribute(new FloatAttribute(Attribute.CELL_VOLUME,vol));
        ds.setAttribute(new Float1DAttribute(Attribute.LATTICE_PARAM,lat));
        ds.setAttribute(new Float2DAttribute(Attribute.ORIENT_MATRIX,orient));
        ds.addLog_entry("Read Orientation Matrix From File: "+iparm);

        // add the hkl information operator to the DataSet
        ds.addOperator(new SCDhkl());

        return SUCCESS;
    }  

    /**
     * Read the orientation matrix out of the specified file.
     */
    public static Object readOrient(TextFileReader tfr, boolean isexpfile){
      float[][] orient=new float[3][3];
    
      try{
        if(isexpfile){
          String start=null;
          int i=0;
          while( (! tfr.eof()) && (i<3) ){
            start=tfr.read_String();
            if(start.equals("CRS11")){ // this is a good line
              start=tfr.read_String(); // skip the next 'word'
              for( int j=0 ; j<3 ; j++ ){
                orient[j][i]=tfr.read_float();
              }
              i++;
            }
          }
        }else{
          for( int i=0 ; i<3 ; i++ )
            for( int j=0 ; j<3 ; j++ )
              orient[j][i]=tfr.read_float();
        }
      }catch(IOException e){
        return new ErrorString("Error while reading matrix: "+e.getMessage());
      }catch(NumberFormatException e){
        return new ErrorString("Error while reading matrix: "+e.getMessage());
      }
      
      float det=(float)DataSetTools.math.LinearAlgebra.determinant(DataSetTools.math.LinearAlgebra.float2double(orient));
      
      if( det==0f )
        return new ErrorString("Zero determinant in orientation matrix");
      
      return orient;
    }
  
    /**
     * Read the orientation matrix out of the specified file.
     */
    public static Object readLattice(TextFileReader tfr, boolean isexpfile){
      float[] lat=new float[6];

      try{
        if(isexpfile){
          String start=null;
          int i=0;
          while( !tfr.eof() && i<2 ){
            start=tfr.read_String();
            if(start.equals("CRS1")){ // candidate
              start=tfr.read_String();
              if(start.equals("ABC")){ // lengths
                for( int j=0 ; j<3 ; j++ )
                  lat[j]=tfr.read_float();
                i++;
              }else if(start.equals("ANGLES")){ // angles
                for( int j=3 ; j<6 ; j++ )
                  lat[j]=tfr.read_float();
                i++;
              }else{
                tfr.read_line();
              }
            }else{
              tfr.read_line();
            }
          }
        }else{
          for( int i=0 ; i<6 ; i++ ){
            lat[i]=tfr.read_float();
          }
        }
      }catch(IOException e){
        return new ErrorString("Error while reading matrix: "+e.getMessage());
      }catch(NumberFormatException e){
        return new ErrorString("Error while reading matrix: "+e.getMessage());
      }

      if(DEBUG){
        for( int i=0 ; i<6 ; i++ )
          System.out.print(lat[i]+", ");
        System.out.println("");
      }

      return lat;
    }

    /* ---------------------------- clone ----------------------------- */
    /**
     * Get a copy of the current LoadOrientation Operator.  The list of
     * parameters and the reference to the DataSet to which it applies
     * is copied.
     */
    public Object clone(){
        LoadOrientation new_op    = new LoadOrientation( );
                                              // copy the data set associated
                                              // with this operator
        new_op.setDataSet( this.getDataSet() );
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }

    /* --------------------------------- main() ------------------------ */
    /**  
     *  Main method for testing purposes.
     */
    public static void main(String args[])
    {
        /* Chris's test program */
	LoadOrientation lo = new LoadOrientation();
	Object ob = lo.getResult();
	System.out.println("The result of calling this operator is:"); 
	System.out.println(ob.toString());
	System.out.println("The documentation for this operator is: ");
	System.out.println(lo.getDocumentation());
	
	System.exit(0);

        /*  Peter's test program
        String prefix="/IPNShome/pfpeterson/data/SCD/";
        String runfile=prefix+"SCD06496.RUN";
        LoadFileString calfile=new LoadFileString(prefix+"quartz_isaw.mat");
        //System.out.println(runfile);
        DataSet rds=(new RunfileRetriever(runfile)).getDataSet(1);
        System.out.println(rds+" "+calfile);
        LoadOrientation op=new LoadOrientation(rds,calfile);
        System.out.println("RESULT:"+op.getResult());
        */
    }
}
