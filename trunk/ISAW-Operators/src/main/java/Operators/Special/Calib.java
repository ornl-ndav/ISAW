/*
 * File:  Calib.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General License for more details.
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.16  2006/01/16 04:42:54  rmikk
 * Removed the calibrated offset of 91.5 from omega
 *
 * Revision 1.15  2006/01/13 21:12:27  rmikk
 * Added 90 degrees to omega
 *
 * Revision 1.14  2006/01/13 21:05:19  rmikk
 * Used Dennis' routine to set the orientation matrix
 *
 * Revision 1.13  2006/01/12 19:15:49  rmikk
 * Fixed FixLANSCESCD to give line number of error for extra file and Only
 *    large data sets are fixed( indices reordered)
 *
 * Revision 1.12  2006/01/09 22:42:22  rmikk
 * Removed sample orientation attributes before adding the correct ones.
 *
 * Revision 1.11  2006/01/08 01:38:31  dennis
 * Minor fix of javadoc warnings that showed up when building with
 * the java 1.5.0_06 compiler.
 *
 * Revision 1.10  2005/10/28 15:49:00  dennis
 * Fixed "spelling" error in javadoc comment.
 *
 * Revision 1.9  2005/08/25 15:18:25  dennis
 * Added/moved to menu category DATA_SET_TWEAK_MACROS
 *
 * Revision 1.8  2005/08/24 19:51:04  dennis
 * Changed logical name of menu from UTILS_DATA_SET to
 * DATA_SET_MACROS
 *
 * Revision 1.7  2005/08/24 18:43:40  rmikk
 * Fixed  a documentation error
 * Added x_units/label and Y_units/label, instr type and operators to the
 *   resultant data set
 *
 * Revision 1.6  2005/08/24 17:04:21  rmikk
 * Added "Fix/Calib" for lansce SAND and Hippo instruments
 *
 * Revision 1.5  2005/08/10 17:16:33  rmikk
 * Now Calibrates the LANSCE SCD instrument given extra info
 *
 * Revision 1.4  2005/08/05 20:11:55  rmikk
 * Use line number = -1  when using LoadSCDCalib
 *
 * Revision 1.3  2005/05/27 03:12:04  dennis
 * Changed to use get attribute method from AttrUtil, rather than
 * the old get attribute method from DataSet and Data
 *
 * Revision 1.2  2005/01/10 15:36:00  dennis
 * Added getCategoryList method to place operator in menu system.
 *
 * Revision 1.1  2004/07/14 16:06:27  rmikk
 * Initial Checkin.  Does arbitrary calibration.  Also does full calibration
 *
 */

package Operators.Special;

import DataSetTools.operator.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import DataSetTools.operator.DataSet.Attribute.*;
import java.util.*;
import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.MathTools.Geometry.*;
import DataSetTools.math.*;
import NexIO.Util.ConvertDataTypes;
import DataSetTools.instruments.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import Operators.Generic.Load.*;
/**
 * This class is central point for calibrating "All" data sets.
 * It is currently differentiated on filename for a DataSet and the extension 
 * on the calibration file.  Other fields are added for further
 * differentiation.
 */


public class Calib implements Wrappable, IWrappableWithCategoryList {

  //~ Instance fields **********************************************************
  
  public DataSet DS;
  public LoadFileString CalibFile1;
  public LoadFileString CalibFile2;
  public Vector otherInformation= new Vector();

  //~ Methods ******************************************************************

  /**
   * Get an array of strings listing the operator category names  for 
   * this operator. The first entry in the array is the 
   * string: Operator.OPERATOR. Subsequent elements of the array determine
   * which submenu this operator will reside in.
   * 
   * @return  A list of Strings specifying the category names for the
   *          menu system 
   *        
   */
  public String[] getCategoryList()
  {
    return Operator.DATA_SET_TWEAK_MACROS;
  }


  /**
   *  Returns "Calib", the name used to invoke this operator in Scripts
   */
  public String getCommand(  ) {
    return "Calib";
  }

  /**
   *  Returns an HTML-Like documentation for the ISAW documentation system
   */
  public String getDocumentation(  ) {

     StringBuffer s = new StringBuffer(  );
     s.append( "@This class is central point to calibrate/fix \"All\" data sets" );
      
     s.append( "@algorithm The Data Set filename, instrument type, ");
     s.append( "calibration filename and extension are used to determine the" );
     s.append( " calibration procedure that is used.  For example <UL>" );
     s.append( " <LI>If the filename starts with SCD and has an extension .dat ");
     s.append( " The LoadSCDCalib operator will fix up the data set");
     s.append( "<LI> If the extension is .FUL, it will use Dennis' routine");
     s.append( " to adjust the detector information");
     s.append( "<li> If the filename starts with SCD and has an extension .lanl");
     s.append( " Routines to extract the xml info and operators are applied to ");
     s.append( " fix the DataSet");
     s.append( "<li>If the filename starts with HIPPO and ends in .lanl,detector");
     s.append( " position information will be added to the data set. In addition");
     s.append( " if the 2nd calib file is a special lanl GSAS param file,Difc,Difa and ");
     s.append( " T0 will be added to the data set");
     s.append("  <LI>If the filename starts with SAND and ends in .lanl, the data in");
     s.append("  the data set will be reorganized to fit ISAW. If the 4th argument is");
     s.append("  a vector of time bins(in us) that time scale will be used");
     s.append( "@param DS  the DataSet to use" );
     s.append( "@param CalibFile1  The first calibration file" );
     s.append( "@param CalibFile2" );
     s.append( "@param otherInformation" );
       
     s.append( "@return null or an error String " );
     return s.toString(  );
  }

  float width = Float.NaN ,
        height = Float.NaN ,
        x_off = Float.NaN ,
        y_off = Float.NaN ,
        detd = Float.NaN ,
        phi = Float.NaN ,
        chi = Float.NaN ,
        omega = Float.NaN ;
  /**
   * This method determines which load routine to use and uses it.
   */
  public Object calculate(  ) {
    String fil = AttrUtil.getFileName(DS);
    clearGridData();
    if(fil == null)
      return new ErrorString("No filename for DataSet");
    fil = fil.replace('\\','/');
    int indx= fil.lastIndexOf("/");
    if( indx >=0)
       fil = fil.substring(indx+1);
    String calibFile = CalibFile1.toString();
    if( fil.toUpperCase().startsWith("SCD") && (calibFile !=null)&&
     (calibFile.length() >4) && calibFile.toUpperCase().endsWith(".DAT")){

    LoadSCDCalib loadSCD    = new LoadSCDCalib(  ); 
    loadSCD.getParameter( 0 )
        .setValue( CalibFile1.toString() );
    loadSCD.getParameter( 1 )
        .setValue( new Integer(-1 ) );
    loadSCD.getParameter( 2 )
        .setValue( "" );
     loadSCD.setDataSet( DS );
     return loadSCD.getResult();
     }
  if( calibFile.toUpperCase().endsWith(".FUL"))
    try{  
      TextFileReader fin = new TextFileReader( calibFile);
      UniformGrid grid = null;
      int currDet = -1;
      while(!fin.eof()){
        String line = fin.read_line();
        if(line.startsWith("L1:"))
           DS.setAttribute(new FloatAttribute( Attribute.INITIAL_PATH,
           (new Float(line.substring(4).trim()).floatValue()) ));
        else if(line.startsWith("T0:"))  
          DS.setAttribute(new FloatAttribute( Attribute.T0_SHIFT,
          (new Float(line.substring(4).trim()).floatValue()) ));
        else if( line.startsWith("Det ")){
          int detNum= getDetNum(line);
          if( detNum != currDet){
            setUpGrid( grid );
            clearGridData();
            currDet = detNum;
            grid =(UniformGrid) DataSetTools.dataset.Grid_util.getAreaGrid(DS, detNum);
          }

          int colonPos = line.indexOf(':',5);
          if( (detNum >=0) && (colonPos >0)){
            int start =colonPos-1;
            while((start >=3) &&(line.charAt(start)==' '))
               start --;
           int j = line.lastIndexOf(' ',start);
           if( j >=3){
               String command = line.substring( j+1,start+1).trim().toUpperCase();
               if( command.equals("WIDTH"))
                  width =(new Float(line.substring(colonPos+1).trim())).floatValue();
               else if (command.equals("HEIGHT"))
                  height =(new Float(line.substring(colonPos+1).trim())).floatValue();
             else if (command.equals("X_OFFSET"))
                x_off =(new Float(line.substring(colonPos+1).trim())).floatValue();
             else if (command.equals("Y_OFFSET"))
                y_off =(new Float(line.substring(colonPos+1).trim())).floatValue();
             else if (command.equals("DISTANCE"))
                detd =(new Float(line.substring(colonPos+1).trim())).floatValue();
             else if (command.equals("PHI"))
                phi =(new Float(line.substring(colonPos+1).trim())).floatValue();
             else if (command.equals("CHI"))
                chi =(new Float(line.substring(colonPos+1).trim())).floatValue();
             else if (command.equals("OMEGA"))
                omega =(new Float(line.substring(colonPos+1).trim())).floatValue();
           }//if command legitimate
          }
        }
        
      }//while !eof
      setUpGrid(grid);
      return null;

    }catch(Exception ss){
       ss.printStackTrace();
       return new ErrorString( "Calib Error="+ss);
    }
   String Fil=calibFile.replace('\\','/');
   int k= Fil.lastIndexOf( "/");
   if( k > 0)
     Fil= Fil.substring(k+1);
   
   if( Fil.toLowerCase().endsWith("lanl"))
     if(Fil.toUpperCase().startsWith("SCD"))
       return FixLansceSCDDataFiles( DS,calibFile);
     else if(Fil.toUpperCase().startsWith("HIPPO"))
       return FixLansceHippoDataFiles( DS,calibFile, CalibFile2.toString());
     else if( Fil.toUpperCase().startsWith("SAND"))
       return FixLansceSandDataFiles( DS, calibFile,otherInformation);
     
   return null;
  }
  
   private void clearGridData(){
     width = Float.NaN ;
     height = Float.NaN ;
     x_off = Float.NaN ;
     y_off = Float.NaN ;
     detd = Float.NaN ;
     phi = Float.NaN ;
     chi = Float.NaN ;
     omega = Float.NaN ;
     
   }
   
   private void setUpGrid( UniformGrid grid){
     if( Float.isNaN(width))
        return ;

     if( Float.isNaN(height ))
       return ; 
     if( Float.isNaN(x_off))
       return ;
     if( Float.isNaN(y_off ))
       return ;
     if( Float.isNaN(detd))
       return ;
     if( Float.isNaN(phi ))
       return ;
     if( Float.isNaN(chi))
       return ;
     if( Float.isNaN(omega))
       return ;
     if( grid == null)
       return;
     Vector3D nom_pos = grid.position();
     Vector3D center =nom_pos;
     grid.setWidth( width );
     grid.setHeight( height );
     
     Vector3D minus_z_vec = new Vector3D( center );
     minus_z_vec.normalize();
     Vector3D vert_vec = new Vector3D( 0, 0, 1 );
     Vector3D x_vec  = new Vector3D();
     x_vec.cross( minus_z_vec, vert_vec );
     Vector3D y_vec = new Vector3D();
     y_vec.cross( x_vec, minus_z_vec );
     x_vec.normalize();
     y_vec.normalize();

     Vector3D x_shift = new Vector3D( x_vec );
     Vector3D y_shift = new Vector3D( y_vec );
     x_shift.multiply( x_off );
     y_shift.multiply( y_off );
     center.normalize();
     center.multiply( detd );
     center.add( x_shift ); 
     center.add( y_shift ); 
     grid.setCenter( center );

     Tran3D euler_rotation = tof_calc.makeEulerRotation(phi, chi, omega);
     euler_rotation.apply_to( x_vec, x_vec );
     euler_rotation.apply_to( y_vec, y_vec );
     grid.setOrientation( x_vec, y_vec );
     Grid_util.setEffectivePositions(DS, grid.ID());
   }
   
  // Assumes line starts with Det space  then detnum
   private int getDetNum( String line){
     int i=3;
     while( (i < line.length()) && (line.charAt(i)<=' '))
         i++;
     if( i >= line.length())
       return -1;     
     int j= line.indexOf(' ',i);
     try{
       return (new Integer(line.substring(i,j))).intValue();
     }catch(Exception ss){
        return -1;
     }
   }
   private static Node getXmlDoc( String fileName){
      try{
        Node N1= DocumentBuilderFactory.newInstance().newDocumentBuilder().
                  parse( new java.io.FileInputStream(fileName));
        if( N1 == null) return null;          
        NodeList Nlist= N1.getChildNodes();
        if( Nlist == null) return null;
        if( Nlist.getLength()<1) return null;
        return Nlist.item(0);
       }catch(Exception s){
        String S ="Error in "+ fileName+":"+s.getMessage();
        if( s instanceof org.xml.sax.SAXParseException)
           S +=" at line "+((org.xml.sax.SAXParseException)s).getLineNumber();
        (new javax.swing.JOptionPane()).showMessageDialog(null,S);
        return null;
      }
   }
   
   /**
    * This method fixes Lansce SCD files that are stored in their preNeXus
    * mode.
    *
    * @param DS    The DataSet that is to be fixed
    * @param file  An specially formatted xml file with SampleOrientation 
    *              info for each run and also some data to  fix detector 
    *              positions.
    * @return an ErrorString if a problem was encountered, or null if
    *         the work was completed.
    */
   public static Object FixLansceSCDDataFiles( DataSet DS, String file ){
                               
      Node doc = getXmlDoc( file);
      if( doc == null)
         return  new ErrorString("Improper Fix file Name");
      Object X= DS.getAttributeValue(Attribute.FILE_NAME);
     
      if( (X==null)||!(X instanceof String))
        return new ErrorString("Need FileName for DataSet");
      String fileName=(String) X;
      int k= fileName.lastIndexOf( java.io.File.separator);
      if( k >=0)
        fileName=fileName.substring(k+1);
      
      Hashtable Info=new Hashtable();
      Info.put("chi",new Float( Float.NaN ));

     Info.put("phi", new Float( Float.NaN ));
     Info.put("omega",new Float( Float.NaN ));
     Info.put("L0", new Float( Float.NaN ));
     Info.put("L1", new Float( Float.NaN ));
     Info.put("detnum", new Integer( -1 ));
     Info.put("width", new Float( Float.NaN ));
     Info.put("height", new Float( Float.NaN ));
     Info.put("tmin", new Float( Float.NaN ));
     Info.put("tmax", new Float( Float.NaN ));
     NodeList list = doc.getChildNodes();
          
     for( int i=0; i< list.getLength(); i++){
      Node elt= list.item(i);
      if( elt.getNodeName().toUpperCase().equals("COMMON")){
      
         setNodeValues( Info, elt);
         //Only one det so get it
         NodeList CommonList = elt.getChildNodes();
         boolean done = false;
         for( int j=0; (j< CommonList.getLength()) && !done ;j++){
            Node elt1 = CommonList.item(j);
            if( elt1.getNodeName().equals("det")){
               setNodeValues( Info, elt1);
               NamedNodeMap attr= elt1.getAttributes();
               if( attr !=null){
                 int detnum = (new Integer( attr.getNamedItem("num").getNodeValue())).intValue();
                 if(detnum >= 0)
                    Info.put("detnum", new Integer(detnum));
                 done = true;
              
            }
          }  
         }  
           
      }else if( elt.getNodeName().toUpperCase().equals("RUNS")){
          NodeList RunList= elt.getChildNodes();
          boolean done = false;
          for( int j=0;( j< RunList.getLength())&& !done; j++){
            Node elt1 = RunList.item(j);
            NamedNodeMap attr= elt1.getAttributes();
            if( attr !=null){ 
              Node fileAtt= attr.getNamedItem("filename");
              if( fileAtt !=null){
                 String v = fileAtt.getNodeValue();
                  if( v !=null) 
                    if( v.equals(fileName)){ 
                    
                     setNodeValues( Info, elt1); 
                     done = true;
                    }
                
              }
            }
       }
        
      }
     }//for    
      float chi= ((Float)(Info.get("chi"))).floatValue();

     float phi= ((Float)(Info.get("phi"))).floatValue();
     float omega= ((Float)(Info.get("omega"))).floatValue();
     float L0= ((Float)(Info.get("L0"))).floatValue();
     float L1= ((Float)(Info.get("L1"))).floatValue();
     int detnum= ((Integer)(Info.get("detnum"))).intValue();
     float width= ((Float)(Info.get("width"))).floatValue();
     float height= ((Float)(Info.get("height"))).floatValue();
     float tmin= ((Float)(Info.get("tmin"))).floatValue();
     float tmax= ((Float)(Info.get("tmax"))).floatValue();
     DataSet DS1= DS;
     if( Float.isNaN(tmin)||Float.isNaN(tmax)||Float.isNaN(width)||Float.isNaN(height)||Float.isNaN(L1)||
          Float.isNaN(L0)){
             return new ErrorString("Not enough info to fix the DataSet");
          }
     else if( DS.getNum_entries() > 200*300)
       DS1= Operators.Example.LansceUtil.FixSCD_Data(DS, tmin,tmax,width, height,L1,L0);
     
     if( Float.isNaN(chi)||Float.isNaN(phi)||Float.isNaN(omega)) 
        return new ErrorString(" Cannot set Crystal orientation");
     else{
       float conv = (float)(180/Math.PI);
       Operators.Example.LansceUtil.AddSampleOrientationAttribute( DS1, phi*conv, chi*conv, omega*conv);
       DS.copy(DS1);
       
      
      }
     
     return null;
   }
   
   
   /**
    * This method fixes Lansce Hippo files that are stored in their preNeXus mode
    * @param  DS          The DataSet that is to be fixed
    * @param  calibFile   The specially formatted file with detector position information
    * @param  calibFile2  A GSAS parameter file with special lanl lines
    * @return null or an ErrorString
    */
   public static Object FixLansceHippoDataFiles( DataSet DS,
                                                 String  calibFile, 
                                                 String  calibFile2 ){
                                                      
     Operators.Generic.Load.LoadUtil.LoadDetectorInfo(DS, calibFile);
     File F = new File( calibFile2);
     Object Res = null;
     if( F.exists())
       Res= Operators.Generic.Load.LoadUtil.LoadDifsGsas_lanl(DS, calibFile2);
     return Res;
   }
   
   
  /**
   * This method fixes Lansce Sand files that are stored in their preNeXus mode
   * @param DS The DataSet that is to be fixed
   * @param calibFile  The specially formatted file with detector position information
   * @param xvalues  An optional set of xvalues for the time dimension.
   * @return  null or an ErrorString
   * NOTE: The detector is assumed to be a 128 by 128 detector that is stored
   *     [det,time,row,col] in the NeXus file( but was read in as [det,col,row,time])
   */ 
  public static Object FixLansceSandDataFiles( DataSet DS, String calibFile,
            Vector xvalues){

       float[] xvals=null; 
       if((xvalues != null ) &&(xvalues.size()>=2)){
           xvals = new float[xvalues.size()];
           int N= xvalues.size();
           for( int i=0; (i< N)&&(xvals != null); i++){
               try{
                  xvals[i]=((Float)(xvalues.elementAt(i))).floatValue();
                  if( i>0)
                    if( xvals[i] <= xvals[i+1])
                       xvals = null;
               }catch(Exception s){
                 xvals= null;
               }
           }
       }    
       if((xvalues == null ) ||(xvalues.size()<2)||(xvals == null)){
         xvals= new float[199];
         xvals[0]=21568;
         double r= Math.log(499071.8/21568.)/(double)198;
         r = Math.pow(Math.E,r);
         for( int i=1; i<199;i++)
            xvals[i]=(float)(xvals[i-1]*r);
       }
         
      
     VariableXScale Xscale= new VariableXScale(xvals);
     //-----------------------------
     int[]Xlate= new int[4];
     Xlate[0]=3; Xlate[1]=0;Xlate[2]=1;Xlate[3]=2;
     //----------------------------------
     Object Res =LoadUtil.Transpose(DS,Xlate,128,128,1,Xscale,true);
     if( Res instanceof ErrorString)
         return Res;
     DataSet ds=(DataSet)Res;
     ds.setTitle(DS.getTitle());
     ds.setX_units("us");
     ds.setY_units(DS.getY_units());
     ds.setX_label("Time");
     ds.setY_label(DS.getY_label());
     
     LoadUtil.LoadDetectorInfo(ds,calibFile);
     DS.copy(ds);
     DS.removeAllOperators();
     
     DS.setAttribute( new IntAttribute(Attribute.INST_TYPE,
                  DataSetTools.instruments.InstrumentType.TOF_SAD));
     DataSetFactory.addOperators(DS);
     DataSetFactory.addOperators( DS, DataSetTools.instruments.InstrumentType.TOF_SAD);
     
    return null;          
  }
   
   private static void setNodeValues( Hashtable tab, Node elt){
     if( elt == null)
       return;
     if( tab == null)
       return;
     NodeList children = elt.getChildNodes();
     if( children == null)
        return;
     for( int i=0; i< children.getLength(); i++){
         Node elt1= children.item(i);
         String name= elt1.getNodeName();
         if(  ";chi;phi;omega;l0;l1;width;height;tmin;tmax;".indexOf(";"+
                          name.toLowerCase()+";") >=0){
            NamedNodeMap attr= elt1.getAttributes();
            
            String units = null;
            if( attr.getNamedItem("units")!=null)
               units = attr.getNamedItem("units").getNodeValue();                
            NodeList elt1kids= elt1.getChildNodes();
            if( elt1kids != null)if( elt1kids.getLength()==1){
               String Value= elt1kids.item(0).getNodeValue();
               
               try{
                  float f = new Float(Value).floatValue();
                  if( !Float.isNaN(f)){
                     f=AdjustUnits(f, units, name);
                    tab.put(name, new Float(f));
                  }
               }catch(Exception s){
               } 
            }
         }                   
 
     }
     
  }
  private static float AdjustUnits( float value, String units, String FieldName){
     String newUnits= units;
     if( ";chi;phi;omega;".indexOf(FieldName)>=0) 
        newUnits="radians";
     else if(";L0;L1;width;height;".indexOf(FieldName)>=0)
        newUnits="meter";
     else if(";tmin;tmax".indexOf(FieldName)>=0)
        newUnits= "microseconds";
     float[] v= new float[1];
     v[0]= value;
     ConvertDataTypes.UnitsAdjust(v,units,newUnits,1f,0f);
     return v[0];
   }
  
}
