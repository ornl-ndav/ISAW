
/*
 * File:  Read3Col1D.java 
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
 * Revision 1.1  2003/08/15 19:35:03  rmikk
 * Initial Checkin
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
* This module reads in efficiency files or Reduce files to produce a data set

*/
public class Read3Col1D extends GenericTOF_SAD{
   
   private String FileTypes =";Efficiency;Reduce Results;";

   /**
   *   Default Constructor for this operator whose title is Print 3Col for 1D.
   *    The command name is Read3Col1D
   */
   public Read3Col1D(){
      super( "Read 3Col for 1D");
   }

   /**
   *    Constructor for Read3Col1D(Title is Read 3Col for 1D)
   *    @param filename the name of the file,in 3Col format, with data set information
   *    @param fileType  Either Efficiency or Reduce Results
   */
   public Read3Col1D( String filename, String fileType){
      this();
      parameters = new Vector();
      parameters.add( new LoadFilePG( "Save File", filename));
      parameters.add( new ChoiceListPG("File Type", fileType));

   }
  
   /**
   *   sets Default parameters
   */
   public void setDefaultParameters(){
      parameters = new Vector();
      parameters.add( new LoadFilePG( "Save File", null));
      ChoiceListPG list= new ChoiceListPG("File Type", Print3Col1D.EFFICIENCY);
      list.addItem( Print3Col1D.REDUCE);
      parameters.add( list);
   }

   /**
   *     Reads in a file in 3 Column format and produces a data set. The title,
   *     run numbers, x units, etc. are set if there is enough information
   */
   public Object getResult(){
     String filename =   getParameter(0).getValue().toString();
     String fileType = getParameter(1).getValue().toString().trim();
     if(FileTypes.indexOf( ";"+fileType+";")<0)
        return new ErrorString( "File Type "+fileType+" is not supported");
     FileInputStream fin=null;   
     try{
       fin = new FileInputStream( filename);
     }catch( Exception ss){
       return new ErrorString( "File Not found-"+ss.toString());
     }
     String Format = setFormat( fileType);
     
     int nelts=ReadHeader(fin, fileType);
     Vector V = new Vector();
     V.addElement( new float[0]);
     V.addElement( new float[0]);
     V.addElement( new float[0]);
     Object Res=FileIO.Read( fin, V,  Format, nelts,getEndConditions( fileType));
     if( !(Res instanceof Integer))
         return Res;
     DataSet DS = createDataSet( (float[])V.elementAt(0), (float[])V.elementAt(1),
                       (float[])V.elementAt(2), fileType);
     DS.setAttribute( new StringAttribute( Attribute.FILE_NAME, filename));
     ReadTailer( fin,DS,fileType);
     return DS;
   }

  private int ReadHeader( FileInputStream fin, String fileType){
     Vector V = new Vector();
     String Format= null;
     if( fileType.equals( Print3Col1D.EFFICIENCY)){
        Integer N = new Integer(20);
        V.addElement( N);
        Format = "I5";
        
        
     }else{
         V.addElement( "'B'  0.0  0.0");
         Format = "S13";
     }
    Object Res = FileIO.Read( fin, V, Format,1,null);
    if( Res instanceof ErrorString){
       return -2;
    }
    if( !fileType.equals(Print3Col1D.EFFICIENCY))
         return -1;
    if( V.elementAt(0) instanceof Integer[])
       return ((Integer[])(V.elementAt(0)))[0].intValue();
    return -2;
 
  }


  private ErrorString ReadTailer( FileInputStream fin, DataSet DS, String fileType){
    Vector V = new Vector();
    Object Res=null;
    String Format= null;
    if( fileType.equals( Print3Col1D.EFFICIENCY)){
      V.addElement( "AD-TO-M1 EF RATIO19990");
      float Max =200;
      V.addElement( "DELAYED NEUTRON FRACTION =");
      V.addElement( new Float( .0011));
      V.addElement( " EMAX =");
      V.addElement( new Float(Max));
      Format ="S"+V.elementAt(0).toString().length()+",/";
      Format += ",S"+V.elementAt(1).toString().length();
      Format +=",F11.5";
      Format +=",S"+V.elementAt(3).toString().length();
      Format += ",F15.5";
      Res = FileIO.Read( fin,V, Format,1,null); 
      if( Res instanceof ErrorString)
         return (ErrorString)Res;
      int[] RunNum = new int[1];
      String[] Slist = (String[])(V.elementAt(0));
      RunNum[0] = (new Integer(Slist[0].substring(18).trim())).intValue();
      DS.setAttribute( new IntListAttribute(Attribute.RUN_NUM,RunNum));
      DS.getData_entry(0).setAttribute( new IntListAttribute(Attribute.RUN_NUM,RunNum));
      DS.setTitle( "EFR"+RunNum[0]);  

    }
    else{
      
      V.addElement( "                     ");
      Format ="S21";
      Res = FileIO.Read( fin,V, Format,1,null); 
      if( Res instanceof ErrorString)
        return (ErrorString)Res;
      String S = ((String[])(V.firstElement()))[0];
      int[] R = new int[3];
      int j=0, k=0;
      for(int i= S.indexOf('-'); i>0; i= S.indexOf('-', i+1)){
         R[k] = (new Integer( S.substring( j,i))).intValue();
         j = i+1;
         k++;
      }
     int[]RunNum = new int[k];
     System.arraycopy(R,0,RunNum,0,k);
      DS.setAttribute( new IntListAttribute(Attribute.RUN_NUM,RunNum));
      DS.getData_entry(0).setAttribute( new IntListAttribute(Attribute.RUN_NUM,RunNum));
     String fname =DS.getAttributeValue( Attribute.FILE_NAME).toString();
     k =fname.lastIndexOf('.');
     if( k <  0) k = fname.length();
     S = fname.substring(0,k);
     S = S.replace('\\','/');
     k = S.lastIndexOf('/');
     if( k >=0)
        S =S.substring( k);
     for( k = S.length()-1; (k>=0)&& Character.isDigit(S.charAt(k));k--)
          {}
     S = S.substring( 0, k+1);
     DS.setTitle( S+RunNum[0]);
    }
    return null;

  }
  private DataSet createDataSet( float[]xvals,float[]yvals,float[]errs, String fileType){

     DataSet Res = new DataSet();
     Res.setOp_log( new OperationLog());
     Res.addData_entry( new FunctionTable( new VariableXScale( xvals), yvals,errs,0));
     if( fileType.equals( Print3Col1D.EFFICIENCY)){
        Res.setX_units("Channel");
        Res.setX_label("Channel");
        Res.setY_units("Rel Intensity");
        Res.setY_label("Intensity");
     }
     else{
        Res.setX_units("per Angstrom");
        Res.setX_label("Q");
        Res.setY_units("Rel Intensity");
        Res.setY_label("Intensity");
         }
    
     return Res;
    

  }
  private String setFormat( String fileType){
     if( fileType.equals( Print3Col1D.EFFICIENCY)){
        return "F11.5,F15.5,F15.5,/";
     }else{
       return "F-10.5,E-18.5,E-18.5,/";
     }
  }

 public static void showUsage(){
  System.out.println("This module reads in efficiency and Result data files");
  System.out.println("  creating data sets.  The arguments are");
  System.out.println("  1. The name of the file storing the data");
  System.out.println("  2. The file type( Efficiency or \"Reduce Results\")");
  System.out.println("");
  System.out.println(" The resultant DataSet is displayed in an image view.");
  System.out.println(" It can be saved in the x vs Group y view under Selected");
  System.out.println("    the Selected Table View");

  System.out.println("");
  System.out.println("");
  System.exit(0);
 }

 private Vector getEndConditions( String FileType){
    if( !FileType.equals( Print3Col1D.REDUCE))
       return null;
    Vector Result = new Vector();
    Result.addElement( FileIO.getEndCondition( "=",new Float( -999.0)));
    Result.addElement( FileIO.getEndCondition( "=",new Float( -999.0)));
    Result.addElement( FileIO.getEndCondition( "=",new Float( -999.0)));
    //System.out.println("EndCond="+StringUtil.toString( Result));
    return Result;

 }
 public String getDocumentation(){
    StringBuffer Res = new StringBuffer();
    Res.append("@overview  Reads efficiency or Reduce files in 3Col format ");
    Res.append(" and produces a DataSet");
    Res.append("@param filename- the name of the file to print to");
    Res.append("@param fileType - Either Efficiency or Reduce Results");
    Res.append("@return  the DataSet or an error message");
    Res.append("@error - \"File Type not supported\" must be Efficiency ");

    Res.append("@error - File open errors ");
    Res.append("@error - Underlying Read errors, improper data type, etc. ");
  
    return Res.toString();


 }
 /**
 *  @param Args[0] name of a file storing information on a DataSet in 3 Col format 
 *  @param Args[1] filetype
 *  @return a data set will be displayed. It can be saved via the x vs Group y in the
 *     Selected Table View
 */
 public static void main( String args[]){

   if( args == null)
     Read3Col1D. showUsage();
   if( args.length <2)
     Read3Col1D.showUsage();
   String filename = args[0];
   String fileType = args[1];
 
  Object Res= (new Read3Col1D(  filename, fileType).getResult());
  if( Res instanceof ErrorString){
    System.out.println("getREsult err="+Res.toString());
    System.exit(0);
  }
  ScriptUtil.display( Res);

 }//main

}

