/*
 * File:  PrintFlood.java 
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
 * Revision 1.5  2003/10/22 20:07:22  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.4  2003/08/15 19:28:55  rmikk
 * Set the new parameter in the constructor
 *
 * Revision 1.3  2003/07/25 16:36:18  rmikk
 * Now can Print transmission files. A parameter is added
 * to select transmission type.
 *
 * Revision 1.2  2003/07/22 16:27:02  dennis
 * Fixed formatting.
 *
 * Revision 1.1  2003/07/14 16:50:45  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator.Generic.TOF_SAD;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import java.util.*;
import DataSetTools.parameter.*;

/**
*     Prints out the Flood Pattern DataSets in text form
*/
public class PrintFlood  extends GenericTOF_SAD{

    DataSet Efficiencies;
    DataSet Mask;


    public PrintFlood(){
       super("Print Flood");
    }


    /**
    *   Constructor
    *   @param   EfficiencyDS  The Efficiency Data Set
    *                          the Mask are all the entries that are zero
    *   @param   Outfilename    The filename to print the information to
    *   @param   fileType      "Flood" or "Transmission"
    */
    public PrintFlood( DataSet EfficiencyDS ,String Outfilename, String fileType) {
       this();
       parameters = new Vector();
       addParameter( new DataSetPG("Efficiency DataSet", EfficiencyDS ) );
       
       addParameter( new SaveFilePG("Output FileName", Outfilename) );
       addParameter( new ChoiceListPG( "File Type",fileType));
    }


   public void setDefaultParameters(){
      parameters = new Vector();
      addParameter( new DataSetPG("Efficiency DataSet", null ) );
      addParameter( new SaveFilePG("Output FileName", null) );
      ChoiceListPG list= new ChoiceListPG("File Type", "Flood");
      list.addItem("Transmission");
      addParameter( list);
   }


   /**
   *   Prints the values then the errors of the Efficeiency Data Set
   *   (with one time bin) to the specified file in the specific format.  
   *   Then the Mask file (also with one time bin) is printed to the same file
   *   @return  "Success"  or an ErrorString
   */ 
   public Object getResult(){
     DataSet Efficiency = ((DataSetPG)getParameter(0)).getDataSetValue();
     String  Outfilename    =  ((SaveFilePG)getParameter(1)).getStringValue();
     String FileType = ((ChoiceListPG)getParameter(2)).getValue().toString();
     int n = Efficiency.getNum_entries();
     IDataGrid EffGrid = getDataGrid( Efficiency);
    
     if( (EffGrid == null) )
        return new ErrorString("Grid is null");
     int nrows = EffGrid.num_rows();
     int ncols = EffGrid.num_cols();
     if( n != nrows*ncols)
        return new ErrorString( "rows and cols do not agree with size");

     //Get and print the Efficiency y values
     float[] Vals = new float[ n];
     float[] Mask = new float[n];
     int  i=0;
     if( FileType.equals("Flood")){
     for( int row = 1; row <= nrows; row++)
        for( int col = 1; col <= ncols; col++){
           Vals[i] = EffGrid.getData_entry(row, col).getY_values()[0];
           if( Vals[i] == 0)
              Mask[i] = 0f;
           else
              Mask[i] = 1f;
           i++;
        }
     }else{
        Mask = null;
        Vals = Efficiency.getData_entry(0).getY_values();
     }
         
     String Format = "F10.7,F10.7,F10.7,F10.7,F10.7,F10.7,F10.7,F10.7,/";
     if( FileType.equals( "Transmission"))
         Format = "E15.5,E15.5,E15.5,E15.5,E15.5,/";
     Vector V = new Vector();
     V.addElement( Vals);
     Object R = FileIO.Write( Outfilename,false,true, V, Format);
     if( R instanceof ErrorString)
       if( R.toString() == FileIO.NO_MORE_DATA){}
       else return R;
     else if( R instanceof Integer)
       if( ((Integer) R).intValue() != n)
       SharedData.addmsg("Not all data written");

     //Get and print the errors of the Efficiency DataSet
     i=0;
     if( FileType.equals("Flood"))
     for( int row = 1; row <= nrows; row++)
        for( int col = 1; col <= ncols; col++)
            {Vals[i] = EffGrid.getData_entry(row,col).getErrors()[0];
              i++;
            }
     else
        Vals=Efficiency.getData_entry(0).getErrors();

     V = new Vector();
     V.addElement( Vals);
     R = FileIO.Write( Outfilename,true,true, V, Format);
    if( R instanceof ErrorString)
       if( R.toString() == FileIO.NO_MORE_DATA){}
       else return R;
     else if( R instanceof Integer)
       if(((Integer) R).intValue() != n)
       SharedData.addmsg("Not all data written");


     if(! FileType.equals("Flood"))
          return  PrintTailer( Efficiency, Outfilename); 

     Format ="I10,I10,I10,I10,I10,I10,I10,I10,/";

     V = new Vector();
     V.addElement( Mask);    
     R= FileIO.Write( Outfilename,true,true, V, Format);
    if( R instanceof ErrorString)
       if( R.toString() == FileIO.NO_MORE_DATA){}
       else return R;
     else if( R instanceof Integer)
       if( ((Integer) R).intValue() != n)
       SharedData.addmsg("Not all data written");

     //Get and print the last line identifying the run number etc.
     IntListAttribute A = 
              (IntListAttribute)(Efficiency.getAttribute( Attribute.RUN_NUM));

     int run_num = -1;
     if( A != null)
        run_num = A.getIntegerValue()[0];

     String S="";
     if( run_num > 0)
         S += DataSetTools.util.Format.string( ""+run_num, 6,false);
     S = S.trim();
     S += "-(PU-BE SOURCE)";
    V = new Vector();
    String[] SS = new String[1];
    SS[0] = S;
    
    V.addElement( S);
    Format = ""+(S.length()+2);
    Format = Format.trim();
    Format="S"+Format;
     R= FileIO.Write( Outfilename,true,true, V, Format);
    if( R instanceof ErrorString)
       if( R.toString() == FileIO.NO_MORE_DATA){}
       else return R;
     return "Success";

   }//getResult

   //Gets the IDataGrid from a DataSet
   private IDataGrid getDataGrid( DataSet ds){
      Data D = ds.getData_entry(0);
      PixelInfoListAttribute Res = (PixelInfoListAttribute)
                           (D.getAttribute( Attribute.PIXEL_INFO_LIST));
      if( Res == null) return null;
      PixelInfoList plist = (PixelInfoList)(Res.getValue() );
      if( plist == null) return null;
      if( plist.num_pixels() < 1)
        return null;
      return plist.pixel(0).DataGrid();
   }

  //currently only transmission
  private Object PrintTailer( DataSet ds, String Outfilename){
   
    String S = " ";
    int[] runs = ((IntListAttribute)(ds.getAttribute( Attribute.RUN_NUM))).
                getIntegerValue();
   
    for( int i = 0; i< runs.length; i++)
         S =S+runs[i]+"-";
     S = S+" (2)";
    Vector V = new Vector();
    V.addElement( S);
    Object R = FileIO.Write( Outfilename,true,true, V, "S20");
    if( R instanceof ErrorString)
       if( R.toString() == FileIO.NO_MORE_DATA){}
       else return R;
     return "Success"; 

  } 
   public String getDocumentation(){
      StringBuffer Res = new StringBuffer();
      Res.append("@overview  Prints the values followed by the errors of the");
      Res.append(" Efficiency Data Set(with one time bin) to the specified");
      Res.append(" file in the specific format.  The Mask is also printed");
      Res.append(" to this file");
      Res.append("@param  EfficiencyDS - the Efficiency Data Set. ");
      Res.append(" The mask is set to 0 where the data is 0 and is set ");
      Res.append(" to 1 where the data is non-zero.");
     
      Res.append("@param  OutFileName-  the name of the output file");
      Res.append("@return  Success or an error message");
      Res.append("@error  \"Grids are null\" if the data sets do not");
      Res.append(" have IDataGrid's");
      Res.append("@error  \"size of mask file incorrect\" ");
      Res.append("@error   Error Messages from the underlying write routines");

      return Res.toString();
   }

}//PrintFlood
