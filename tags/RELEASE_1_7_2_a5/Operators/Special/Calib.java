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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
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
    return Operator.UTILS_DATA_SET;
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
     s.append( "@This class is central point to calibrate \"All\" data sets" );
      
     s.append( "@algorithm The Data Set filename, instrument type, ");
     s.append( "calibration filename and extension are used to determine the" );
     s.append( " calibration procedure that is used" );
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
        .setValue( new Integer(1 ) );
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
    }catch(Exception ss){
       ss.printStackTrace();
       return new ErrorString( "Calib Error="+ss);
    }
  
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
   
  }
