
/*
 * File:  Print3Col1D.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2003/10/22 20:00:25  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.2  2003/08/19 20:57:31  rmikk
 * Will print Histogram and Function data correctly
 *
 * Revision 1.1  2003/08/15 19:33:00  rmikk
 * Initial Checkin. Prints 1D reduce results and Efficiency ratios.
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;
import java.io.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.util.*;
import java.util.*;
import DataSetTools.parameter.*;
import Command.*;

/**
* This module saves efficiency data sets and data sets and results from the
*  Reduce operator in their standard formats
*/
public class Print3Col1D extends GenericTOF_SAD{
   
   public static String EFFICIENCY = "Efficiency";
   public static String REDUCE     = "Reduce Results";
   private String FileTypes =";Efficiency;Reduce Results;";

   /**
   *   Default Constructor for this operator whose title is Print 3Col for 1D.
   *    The command name is Print3Col1D
   */
   public Print3Col1D(){
      super( "Print 3Col for 1D");
   }

   /**
   *    Constructor for Print3Col1D(Title is Print 3Col for 1D)
   *    @param DS  the Data Set to be saved
   *    @param filename the name of the file to print to
   *    @param fileType  Either Efficiency or Reduce Results
   *    @param DelayedNeutron   The Neutron Delay fraction(for Efficiency)
   */
   public Print3Col1D( DataSet DS, String filename, String fileType,
               float DelayedNeutron){
      this();
      parameters = new Vector();
      parameters.add(  new DataSetPG( "DataSet",DS));
      parameters.add( new SaveFilePG( "Save File", filename));
      parameters.add( new ChoiceListPG("File Type", fileType));
      parameters.add( new FloatPG("Delayed Neutron Fraction",
                        new Float(DelayedNeutron)));

   }
  
   /**
   *   sets Default parameters
   */
   public void setDefaultParameters(){
      parameters = new Vector();
      parameters.add(  new DataSetPG( "DataSet",null));
      parameters.add( new SaveFilePG( "Save File", null));
      ChoiceListPG list= new ChoiceListPG("File Type", Print3Col1D.EFFICIENCY);
      list.addItem( Print3Col1D.REDUCE);
       parameters.add( list);
      parameters.add( new FloatPG("Delayed Neutron Fraction",new Float(.0011)));
   }

   /**
   *   Writes the DataSet to the specified file in a 3 column format. The header
   *   and tailers correspond to the fileType selected.  
   *   @return   null if successful or an ErrorString describing the error
   */
   public Object getResult(){
     DataSet DS = (DataSet)(getParameter(0).getValue());
     String filename =   getParameter(1).getValue().toString();
     String fileType = getParameter(2).getValue().toString().trim();

     float DelayNeutron = ((Float)(getParameter(3).getValue())).floatValue();
     if(FileTypes.indexOf( ";"+fileType+";")<0)
        return new ErrorString( "File Type "+fileType+" is not supported");
     
     if( DS.getNum_entries() != 1)
        return new ErrorString("Wrong number of Spectra");
     String Format = setFormat( fileType);
     WriteHeader( filename, DS, fileType);

     float[] xvals= DS.getData_entry(0).getX_scale().getXs();
     int N=1;
     if( xvals.length == DS.getData_entry(0).getY_values().length)
        N = 0;
     float[] xav = new float[xvals.length-N];
     for(int i=0; i < xav.length; i++)
        xav[i]= (xvals[i]+xvals[i+N])/2;
     Vector V = new Vector();
     V.addElement( xav);
     V.addElement( DS.getData_entry(0).getY_values());
     V.addElement( DS.getData_entry(0).getErrors());
     Object Res=FileIO.Write( filename, true, true,V,  Format);
     if( !(Res instanceof Integer))
         return Res;
     if( ((Integer)Res).intValue() != xav.length)
         return new ErrorString( "Incorrect number of values written");

     WriteTailer( filename,DS,fileType, DelayNeutron);
     return Res;
   }

  private ErrorString WriteHeader( String filename,DataSet DS, String fileType){
     Vector V = new Vector();
     String Format= null;
     if( fileType.equals( Print3Col1D.EFFICIENCY)){
        Integer N = new Integer( DS.getData_entry(0).getY_values().length);
        V.addElement( N);
        Format = "I5";
        
        
     }else{
         V.addElement( "'B'  0.0  0.0");
         Format = "S13";
     }
    Object Res = FileIO.Write( filename, false, true, V, Format);
    if( Res instanceof ErrorString)
       return (ErrorString)Res;
    return null;
  }

  private ErrorString WriteTailer( String filename, DataSet DS, String fileType,
                  float DelayNeutron){
    int[] RunNums = (int[])(DS.getData_entry(0).getAttributeValue(Attribute.RUN_NUM))
;
    Vector V = new Vector();
    Object Res=null;
    String Format= null;
    if( fileType.equals( Print3Col1D.EFFICIENCY)){
      V.addElement( "AD-TO-M1 EF RATIO"+RunNums[0]);
      float Max = FindMax( DS.getData_entry(0).getY_values());
      V.addElement( "DELAYED NEUTRON FRACTION =");
      V.addElement( new Float( DelayNeutron));
      V.addElement( " EMAX =");
      V.addElement( new Float(Max));
      Format ="S"+V.elementAt(0).toString().length()+",/";
      Format += ",S"+V.elementAt(1).toString().length();
      Format +=",F11.5";
      Format +=",S"+V.elementAt(3).toString().length();
      Format += ",F15.5";
      Res = FileIO.Write( filename,true,true,V, Format); 
      
    }
    else{
       V.addElement( " -999.0	-999.0	-999.0");
       Format="S21,/,";
       String S="";
       if( RunNums != null)
       for( int i =0; i< RunNums.length;i++){
         if( i ==0) S +=' ';
         else S +='-';
         S +=RunNums[i];
       }
      S+="-(2)";
      V.addElement( S);
      Format +="S"+S.length();
      Res = FileIO.Write( filename,true,true,V, Format); 
    }
    if( Res instanceof ErrorString)
      return (ErrorString)Res;
    return null;

  }
  private String setFormat( String fileType){
     if( fileType.equals( Print3Col1D.EFFICIENCY)){
        return "F11.5,F15.5,F15.5,/";
     }else{
       return "F10.5,E18.5,E18.5,/";
     }
  }
  float FindMax( float[]list){
    if( list == null)
      return Float.NaN;
    if( list.length <1)
      return Float.NaN;
    float res = list[0];
    for( int i=0; i< list.length; i++)
      if( list[i] > res)
        res = list[i];
    return res;
  }

 public static void showUsage(){
  System.out.println("This module saves efficiency data sets and data sets");
  System.out.println("  Result from the Reduce operator.  The arguments are");
  System.out.println("  1. The name of the file storing the data set");
  System.out.println("  2. The file type( Efficiency or \"Reduce Results\")");
  System.out.println("  3. Delayed Neutron Fraction( for Eff)");
  System.out.println("");
  System.out.println("");
  System.exit(0);
 }

 public String getDocumentation(){
    StringBuffer Res = new StringBuffer();
    Res.append("@overview  Saves efficiency data sets and data sets that ");
    Res.append(" result from the Reduce operator in their standard formats");
    Res.append("@param DS - the Data Set to be saved");
    Res.append("@param filename- the name of the file to print to");
    Res.append("@param fileType - Either Efficiency or Reduce Results");
    Res.append("@param DelayedNeutron -   The Neutron Delay fraction(for Efficiency)");
    Res.append("@return  null or an error message");
    Res.append("@error - \"File Type not supported\" must be Efficiency ");
    Res.append("or Reduce Results ");

    Res.append("@error - \"Wrong number of Spectra\"-must have exactly one ");
    Res.append("spectra ");
    Res.append("@error -  \"Incorrect number of values written\"");
    Res.append("@error -  Error Messages from the underlying write routines");
  
    return Res.toString();


 }
 /**
 *  Test program for this module
 *  Args[0] name of a file storing a desired DataSet
 *  Args[1] filetype
 *  Args[2]  Delayed Neutron
 *  @return file will be written in current directory to xxx.dat
 */
 public static void main( String args[]){

   if( args == null)
     Print3Col1D. showUsage();
   if( args.length <2)
     Print3Col1D.showUsage();
   String filename = args[0];
   String fileType = args[1];
   float DelayNeutron = 0;
   if( args.length >2)
     DelayNeutron = (new Float(args[2])).floatValue();
   DataSet[] DS=null;
   try{
     DS = ScriptUtil.load( filename);
   }catch( Exception sss){
     System.out.println("Could not load data set "+sss.toString());
     System.exit(0);
   }
   System.out.println( "Operator Result="+
     (new Print3Col1D(  DS[0],"xxx.dat", fileType, DelayNeutron)).getResult());

 }//main

}

