/*
 * File:  GenTable.java
 *
 * Copyright (C) 2004 Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.1  2004/04/19 14:07:54  rmikk
 * Initial Checkin
 * Wrapper around the advanced Table View
 *
 
 */

package DataSetTools.operator.Generic.Save;

import DataSetTools.operator.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import DataSetTools.viewer.Table.*;
import javax.swing.table.*;
import javax.swing.*;
import java.util.*;
import  gov.anl.ipns.Util.Numeric.*;
import DataSetTools.dataset.*;
/**
 * This operator is a wrapper around the advanced table view. 
 *
 */


public class GenTable implements Wrappable {
 
   
  public DataSet DS =  DataSet.EMPTY_DATA_SET;
  public Vector Fields= new Vector();  //List of Field names that appear in
                                       //the advanced Table view
  public MediaList media =new MediaList("File");
  public SaveFileString savFileName = new SaveFileString();
  public String order=new String("HGT,F");
  public IntListString SelGroups = new IntListString();
  

  /**  
    *  Returns "Table" , the name used to invoke this operator in scripts"
    */
  public String getCommand(  ) {
    return "Table";
  }

  /**
    *  Returns a HTML-like documentation string
   */
  public String getDocumentation(  ) {
   

    StringBuffer s = new StringBuffer(  );
    
    s.append("@overview This operator is a wrapper around the advanced table ");

    s.append("view.");
    
    s.append("@assumptions The Field names correspond to field names in the");
    s.append("advanced table view.\n The order parameter is legitimate(starts ");
    s.append("with H, others can be F,T, G(or R and C). \",\" separates fields");
    s.append("enumerated down columns from those enumerated across columns");
                                                                              //
    s.append("@algorithm Uses advanced table view methods ");
    s.append("@param DS the data set with the information ");
    s.append("@param Fields a Vector with the list of Field names(Strings) ");
    s.append("to be displayed. These should correspond to the Field names listed");
    s.append("in the advanced table view");
    s.append("@param media  Should be Table or File or Console ");
    s.append("@param savFileName The file name to save result to if media is File ");
    s.append("@param order  Use HGT,F or HT,GF to specify order in which info ");
    s.append("will be displayed");
    s.append("@param SelGroups  The Group indicies to be displayed");
    
    return s.toString();
  }

  /**
    *   The method access to this operator. This will produce the desired display
    *   as specified in the parameters.
    *   @param  DS  The data set with the information
    *   @param  Fields  The names of the fields to be used. These should match the 
    *                   names in the advanced table view
    *   @param  media  Should be Table, File, or Console
    *   @param  savFileName  The filename to save the File output to
    *   @param  order     Use HGT,F  or HT,GF(to get groups listed in columns)
    *   @param SelGroups  the Group indicies to be considered
    *   @return  "Finished" or an Error. The display will be somewhere
    */
  public Object calculate(DataSet DS , Vector Fields, MediaList media, 
            SaveFileString savFileName ,String order, IntListString SelGroups ){
    this.DS= DS ;
    this.Fields= Fields ;
    this.media= media ;
    this.savFileName=savFileName  ;
    this.SelGroups=SelGroups  ;
    this.order = order;
    return calculate();
 

  }

  /**
    *  The operator entry to this Wrappable
    */
  public Object calculate(  ) {
     int mode = 0;
     //System.out.println("output="+output);
     if( media.toString() .equals("Console"))
         mode = 0;
     else if( media.toString().equals("File"))
         mode = 1;
     else if( media.toString().equals("Table"))
         mode = 2;
   
     if( (mode < 0) || (mode >2))
       mode = 0;
     table_view TB = new table_view( mode );
     TB.setFileName( savFileName.toString() );
     
     DefaultListModel sel = new DefaultListModel();
     String ChanX ;
     for( int i=0; i<Fields.size();i++){ 
        ChanX =(String)( Fields.elementAt(i));
     
        if( TB.getFieldInfo(DS,ChanX) == null)
         { 
           return new ErrorString("No such field "+ChanX);
          }
        sel.addElement( TB.getFieldInfo(DS,ChanX));
      }
     
     
     
     
     DataSet DSS[];
     DSS = new DataSet[1];
     DSS[0] = DS;
     TB.Showw( DSS , sel , order , false, IntList.ToArray(SelGroups.toString()) );
     return "Finished";


  }
}
