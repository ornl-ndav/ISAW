/*
 * File:  LoadUtil.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.7  2005/08/19 19:58:10  rmikk
 * Added method to "Transpose" the data in a dataset that was improperly
 *     read from a NeXus file because the dimensions are in the incorrect
 *    order.
 * 
 * Introduced a main method to test this routine
 *
 * Revision 1.6  2005/08/15 19:45:55  rmikk
 * Fixed up the command line info to show usage for parameters and to allow 
 * for entering filenames and data set names
 *
 * Revision 1.5  2005/08/15 19:33:41  rmikk
 * Added Method to read through a GSAS file and process the lines in the
 *    special lanl fomat for specifying div_c, dif_a and t0
 *
 * Revision 1.4  2005/08/05 20:54:11  dennis
 * Added support for Instrument_Type specification in the
 * detector information file, so that the correct operators
 * can be added to the LANSCE/NeXus DataSet that is
 * being "fixed".
 *
 * Revision 1.3  2005/07/06 14:21:38  dennis
 * Cleaned up test code in main program.  Enabled test of both
 * loading SMARTS detector info and loading GLAD detector info.
 * Removed a debug print.
 *
 * Revision 1.2  2005/07/05 17:10:19  dennis
 * Added method Load_GLAD_LPSD_Info() that will serve as the
 * "core" method for an Operator to add DataGrids corresponding
 * to the LPSDs on GLAD, to a GLAD DataSet as read from the IPNS
 * runfile.
 *
 * Revision 1.1  2005/06/27 05:05:33  dennis
 * File for static utility methods for loading missing information
 * into DataSets.  Currently, there is just one public method:
 * LoadDetectorInfo(), that will load information about area, LPSD or
 * single tube detectors, from a text file, into a DataSet, in the form
 * of "DataGrids".  Effective pixel positions are also set from the
 * DataGrids and references from the individual pixels will be set
 * pointing back to the spectrum (or group) with data from that pixel.
 * This is needed to "fix" the Data from LANSCE instruments AND can also
 * be used for the GLAD at IPNS.
 *
 */

package Operators.Generic.Load;

import java.io.*;
import java.util.*;

import Command.ScriptUtil;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.instruments.*;
import NexIO.*;
import NexIO.Write.*;
import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import java.util.regex.*;
import DataSetTools.gsastools.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
/**
 *  This class contains static method for Loading detector information
 *  into a DataSet.  This is necesary if the raw data file does not 
 *  contain enough information about the instrument and detector geometry 
 *  in a form that can be handled by the ISAW retrievers.
 */

public class LoadUtil 
{
  private static boolean debug_glad = false;       // debug flag for GLAD code

  private LoadUtil()
  {};

  /**
   *  Load detector information (and initial path, and instrument type,
   *  if present) for the specified DataSet from the specified file.  
   *  The file must list the values on separate lines, with a single 
   *  identifier at the start of the line.  Lines starting with "#" 
   *  are ignored.  The accepted identifiers are:
   *        Instrument_Type
   *        Initial_Path
   *        Num_Grids
   *        Grid_ID
   *        Num_Rows
   *        Num_Cols
   *        Width
   *        Height
   *        Depth
   *        Center
   *        X_Vector
   *        Y_Vector
   *        First_Index
   *  The identifier MUST appear at the start of the line, followed by spaces
   *  and  a single number, except for the Center, X_Vector and Y_Vector, which
   *  require three numbers separated by spaces.  
   *  NOTE: Values can be omitted, and the previous or default values will 
   *        be used.  This allows, for example, specifying the width, height
   *        and depth once for the first detector and omitting them for 
   *        later detectors that have the same dimensions.
   *
   *  @param  ds         DataSet for which the detector and initial path
   *                     info is read.
   *
   *  @param  file_name  Name of file containing the detector information 
   *
   */
  public static void LoadDetectorInfo( DataSet ds, String file_name )
  {
    Hashtable hash = new Hashtable( 2000 );
    try
    {
      TextFileReader f = new TextFileReader( file_name );

      Vector grid_names = LoadHashtable( f, hash );
      f.close();
                                    // set initial path iattribute in meters
                                    // if it's specified in the file
      Float Length_0 = getFloat( hash, "INITIAL_PATH" );
      if ( Length_0 != null )
      { 
        float length_0 = Length_0.floatValue();
        FloatAttribute initial_path = 
                               new FloatAttribute( "Initial Path", length_0 );
        ds.setAttribute( initial_path );
        int num_data = ds.getNum_entries();
        for ( int i = 0; i < num_data; i++ )
           ds.getData_entry(i).setAttribute( initial_path );
      }
                                                 // set up default values
      int      num_grids   = grid_names.size();
      int      grid_id     = -1;
      int      n_rows      = 1;
      int      n_cols      = 1;
      float    width       = 1;
      float    height      = 1;
      float    depth       = 1;
      Vector3D center      = new Vector3D( 0, 0.01f, 0 );
      Vector3D x_vec       = new Vector3D( 5, 0, 0 );
      Vector3D y_vec       = new Vector3D( 0, 0, -5 );
      int      first_index = 0;
                                                // now for each grid, change
                                                // current values if set
      for ( int i = 0; i < num_grids; i++ )
      {
        String prefix = (String)grid_names.elementAt(i);

        Integer Grid_id = getInteger( hash, prefix + "GRID_ID" );
        if ( Grid_id != null )
          grid_id = Grid_id.intValue();
       
        Integer N_rows = getInteger( hash, prefix + "NUM_ROWS" );
        if ( N_rows != null )
          n_rows = N_rows.intValue();

        Integer N_cols = getInteger( hash, prefix + "NUM_COLS" );
        if ( N_cols != null )
          n_cols = N_cols.intValue();
      
        Float Width = getFloat( hash, prefix + "WIDTH" );
        if ( Width != null )
          width = Width.floatValue();

        Float Height = getFloat( hash, prefix + "HEIGHT" );
        if ( Height != null )
          height = Height.floatValue();

        Float Depth = getFloat( hash, prefix + "DEPTH" );
        if ( Depth != null )
          depth = Depth.floatValue();
 
        Vector3D Center = getVector( hash, prefix + "CENTER" );
        if ( Center != null )
          center = new Vector3D( Center );

        Vector3D X_vector = getVector( hash, prefix + "X_VECTOR" );
        if ( X_vector != null )
          x_vec = new Vector3D( X_vector );

        Vector3D Y_vector = getVector( hash, prefix + "Y_VECTOR" );
        if ( Y_vector != null )
          y_vec = new Vector3D( Y_vector );

        Integer First_index = getInteger( hash, prefix + "FIRST_INDEX" );
        if ( First_index != null )
        {
          first_index = First_index.intValue();
          first_index--;    // shift index down by 1 to match the "C" style 
                            // numbering system, starting at 0.
        }

        UniformGrid grid = new UniformGrid( grid_id, "m",
                                            center, x_vec, y_vec,
                                            width, height, depth,
                                            n_rows, n_cols );
        grid.AddGridToDataSet( ds, first_index, true );

        first_index += n_rows * n_cols;
      }
    }
    catch ( Exception e )
    {
       System.out.println("Exception reading detecdtor file \n" +
                           file_name + "\n" + e );
    }
                                             // add the needed operators, if 
                                             // instrument type was specified
    Integer InstrumentType = getInteger( hash, "INSTRUMENT_TYPE" );
    if ( InstrumentType != null )
    {
      int instrument_type = InstrumentType.intValue();
      DataSetFactory.addOperators( ds, instrument_type );
    }
  }


  /**
   *  Load the hashtable with all of the "name", "value string" pairs from
   *  the file containing the detector information.  The "name" from the
   *  file has GRID_IDN prepended where N is ID number for the grid.  This
   *  allows the information for a particular detector to accessed from
   *  the hashtable.
   *
   *  @param     f      The open TextFileReader object
   *  @param     hash   The empty hashtable that is loaded from the file.
   *
   *  @return  A vector containing Strings identifying all of the DataGrids
   *           read from the file, in the form GRID_IDN where N is the 
   *           Grid ID assigned in the file.
   */
  private static Vector LoadHashtable( TextFileReader f, Hashtable hash ) 
                        throws IOException
  {
     Vector grid_names = new Vector( 1000 );
     String grid_name  = "";
     String name;
     String val_string;
     while ( !f.end_of_data() )
     {
       f.SkipLinesStartingWith( "#" ); 
       name = f.read_String();
       val_string = f.read_line();
       val_string = val_string.trim(); 
       if ( name.equalsIgnoreCase( "Grid_ID" ) )
       {
         grid_name = name + val_string;
         grid_names.add( grid_name.toUpperCase() ); 
       }
       hash.put( (grid_name +  name).toUpperCase(), val_string );
     }
     return grid_names;
  }


  /**
   *  Get the specified entry from the hashtable and interpret it as
   *  a Float value.
   *
   *  @param  hash   the hashtable that may or may not contain the named
   *                 entry.
   *  @param  name   the name of the entry to be returned as a Float
   *
   *  @return  A Float object giving the value of the entry specified by
   *           the name, or null if the named entry is not present, or 
   *           can not be interpreted as a Float.
   */
  private static Float getFloat( Hashtable hash, String name )
  {
    String  val_string = (String)hash.get( name );
    try
    {
       return new Float( val_string );
    }
    catch ( Exception e )
    {
       return null;
    }
  }


  /**
   *  Get the specified entry from the hashtable and interpret it as
   *  an Integer value.
   *
   *  @param  hash   the hashtable that may or may not contain the named
   *                 entry.
   *  @param  name   the name of the entry to be returned as an Integer 
   *
   *  @return  A Float object giving the value of the entry specified by
   *           the name, or null if the named entry is not present, or 
   *           can not be interpreted as an Integer.
   */
  private static Integer getInteger( Hashtable hash, String name )
  {
    String  val_string = (String)hash.get( name );
    try
    {
       return new Integer( val_string );
    }
    catch ( Exception e )
    {
       return null;
    }
  }

  /**
   *  Get the specified entry from the hashtable and interpret it as
   *  a Vector3D object.
   *
   *  @param  hash   the hashtable that may or may not contain the named
   *                 entry.
   *  @param  name   the name of the entry to be returned as a Vector3D
   *
   *  @return  A Float Vector3D giving the value of the entry specified by
   *           the name, or null if the named entry is not present, or 
   *           can not be interpreted as a Vector3D.
   */
  private static Vector3D getVector( Hashtable hash, String name )
  {
    String  val_string = (String)hash.get( name );
    try
    {
      int first_space = val_string.indexOf( " " ); 
      int last_space  = val_string.lastIndexOf( " " ); 
      String x_str = val_string.substring( 0, first_space );
      String y_str = val_string.substring( first_space, last_space );
      String z_str = val_string.substring( last_space, val_string.length() );
      float x = (float)Double.parseDouble( x_str.trim() ); 
      float y = (float)Double.parseDouble( y_str.trim() ); 
      float z = (float)Double.parseDouble( z_str.trim() ); 
      return new Vector3D( x, y, z );
    }
    catch ( Exception e )
    {
       return null;
    }
  }


  /**
   *  Load LPSD information for the IPNS GLAD instrument from the 
   *  configuration file, gladdets6.par, as used on the old VAX systems.
   *
   *  @param  ds         DataSet for IPNS GLAD, in the form it is 
   *                     currently (7/1/05) read from the IPNS Runfile.
   *
   *  @param  file_name  Name of file containing the GLAD detector information 
   */
  public static void Load_GLAD_LPSD_Info( DataSet ds, String file_name )
  {
    int N_ROWS      = 64;
    int N_COLS      = 1;
    float LPSD_HEIGHT = 0.64f;                  // NOTE: 64 needed to match 
                                                // results from IPNS package
    float LPSD_WIDTH  = 0.01074f;
    float LPSD_DEPTH  = 0.01074f;

    int grid_id = 1;
    Vector3D y_vec  = new Vector3D(  0, 0, 1 );
    Vector3D x_vec  = new Vector3D( -1, 1, 0 ); 
    Vector3D center = new Vector3D(  0, 1, 0 ); 
    int      bank;
    int      n_det;
    int      first_segment;
    try
    {
      TextFileReader f = new TextFileReader( file_name );
      while ( !f.end_of_data() )
      {
        bank  = f.read_int();
        n_det = f.read_int();
        if ( bank > 0 && n_det > 0 )       // ignore bank 0, which are monitors 
        {                                  // and banks with 0 detectors
           for ( int i = 0; i < n_det; i++ )
           {
             f.read_int();                 // skip det_in_bank
             f.read_int();                 // skip crate
             f.read_int();                 // skip slot
             f.read_int();                 // skip input
             first_segment = f.read_int(); 
             if ( first_segment >0 )
             {
               // for now, synthesize some positions
               float x = (float)(2 * Math.cos( grid_id/2000f * 8 * Math.PI ));
               float y = (float)(2 * Math.sin( grid_id/2000f * 8 * Math.PI ));
               center = new Vector3D( x, y, 0.5f );
               UniformGrid grid = new UniformGrid( grid_id, "m",
                                         center, x_vec, y_vec,
                                         LPSD_WIDTH, LPSD_HEIGHT, LPSD_DEPTH,
                                         N_ROWS, N_COLS );
               grid_id++;

               if ( first_segment == 10817 )  // NOTE: this is fix for segment 
                 first_segment = 10815;       // IDs in this particular detector

               AssignGridForPixels( ds, grid, first_segment, N_ROWS ); 
             }  
           }
         }
         else
         {               
            f.read_line();
            for ( int i = 0; i < n_det; i++ )    // skip lines for monitors
              f.read_line(); 
         }
       }
     }
     catch ( Exception e )
     {
        System.out.println("Exception reading file " + file_name + "\n" + e );
        e.printStackTrace();
     }
  }


  /**
   *  Go through all Data blocks in the DataSet, to find the Data blocks
   *  for the specified grid, as determined by the first segment id for
   *  this grid and the number of segments for this grid.
   *  NOTE: This method assumes that all pixels contributing to one
   *  Data block come from the same detector. 
   */
  private static void AssignGridForPixels( DataSet     ds, 
                                           UniformGrid grid,
                                           int         first_seg_id,
                                           int         num_segs_in_det )
  {
     int n_cols = grid.num_cols();

     for ( int i = 0; i < ds.getNum_entries(); i++ )
     {
       IData d = ds.getData_entry(i);
       PixelInfoList pil = AttrUtil.getPixelInfoList( d );
       if ( pil != null )
       {
         IPixelInfo pixel = pil.pixel(0);        // check first pixel, if it is
         int pixel_id = pixel.ID();              // from this detector, assume
         if ( pixel_id >= first_seg_id &&        // other pixeis in pil do too.
              pixel_id < first_seg_id + num_segs_in_det ) 
         {
           IPixelInfo new_pix_arr[] = new IPixelInfo[ pil.num_pixels() ]; 
           for ( int k = 0; k < new_pix_arr.length; k++ )
           {
             pixel = pil.pixel(k);
             pixel_id = pixel.ID();
             short row = (short)((pixel_id - first_seg_id) / n_cols + 1);
             short col = (short)((pixel_id - first_seg_id) % n_cols + 1);
             DetectorPixelInfo new_pixel = 
                              new DetectorPixelInfo( pixel_id, row, col, grid );
             new_pix_arr[k] = new_pixel;

             if ( row == 5 )                     // set grid position based on
             {                                   // the pixel in row 5, 
                                                 // projected onto the x,y plane
               DetectorPosition pos = AttrUtil.getDetectorPosition( d );
               Vector3D center = new Vector3D(pos);
               float xyz[] = center.get();
               xyz[2] = 0;
               
               grid.setCenter( center );
               Vector3D x_vec = new Vector3D( -xyz[1], xyz[0], 0 );
               Vector3D y_vec = new Vector3D( 0, 0, 1 );
               grid.setOrientation( x_vec, y_vec );
             }
           }
           pil      = new PixelInfoList( new_pix_arr );
           PixelInfoListAttribute pil_attr = 
                   new PixelInfoListAttribute( Attribute.PIXEL_INFO_LIST, pil );
           d.setAttribute( pil_attr );
/*
  PROPER CALCULATON OF EFFECTIVE POSITIONS, STILL NEEDS WORK.

           Attribute attr = new DetPosAttribute( 
                                    Attribute.DETECTOR_POS,
                                    pil.effective_position() );
           d.setAttribute( attr );
*/
           if ( debug_glad )
             if ( grid.ID() >= 205 && grid.ID() <= 209 )
             {
               System.out.println("CHECK GRID_ID = " + grid.ID() );
               for ( int k = 0; k < new_pix_arr.length; k++ )
                 System.out.println( new_pix_arr[k] );
             }
         }
       }
     }
  }

  /**
   * This Loads difc, difa and T0 from a GSAS parameter file where the information
   * is given on the special lanl format lines
   * @param ds   The data set
   * @param paramFileName   The name of the parameter file
   * @return  null or an ErrorString
   */
  public static Object LoadDifsGsas_lanl( DataSet ds, String paramFileName){
    
      if( paramFileName == null)
         return new ErrorString("No parameter File Name is given in LoadDifsGsas");
         
      if( ds == null)
         return new ErrorString("No data set given in LoadDifsGsas");
         
      FileInputStream f= null;
      try{
           f=new FileInputStream( paramFileName);
      }catch(Exception ss){
         return new ErrorString(ss.toString()+" in LoadDifsGsas");
      }
         
      String match1= ds.getTitle();
      match1="[\n\r]+INS   "+match1+" ";
      String eoln="[\n\r]+";
      Pattern pat= Pattern.compile(match1);
      Pattern eolnPat= Pattern.compile(eoln);
      int size=500*match1.length();
      byte[] buffer = new byte[size ];
      String line=null;
      try{
         int nread = f.read(buffer);
         int offset =0;
         int readResult=nread,
             nextPosition=0;
        String SS= new String( buffer, nextPosition , nread );
        Matcher match= pat.matcher( SS);
        Matcher eolnmatch = eolnPat.matcher(SS);
         while( nextPosition < nread){
             if(match.find()){//put rest of line into line and process
               
                int end= match.end();
               
                if( eolnmatch.find(end)){
                  
                   int endLine=eolnmatch.end();
                   line=new String(buffer,end, endLine-1-end);
                   nextPosition=endLine-1;
                  
                }else{
                  
                  line =new String(buffer, end, nread-end);
                  readResult= f.read(buffer);
                  if(readResult >=0){
                    
                    nextPosition=0;
                    nread =readResult;
                    line +=new String( buffer,nextPosition,nread);
                    SS= new String(buffer,nextPosition,nread-nextPosition);
                    //match= pat.matcher( SS);
                    eolnmatch = eolnPat.matcher(SS);
                    
                  }
                 
                  while((readResult >=0)&&!eolnmatch.find()){
                    
                    readResult= f.read(buffer);
                    offset = 0;
                    if( readResult >=0){
                      
                      nread =readResult;
                      nextPosition=0;
                      line +=new String( buffer,0,nread);
                      SS= new String(buffer,nextPosition,nread-nextPosition);
                      eolnmatch = eolnPat.matcher(SS);
                    }
                  }
                  
                  if( readResult >=0){
                    
                    int kk= eolnmatch.end()-1;
                    match= pat.matcher( SS);
                    int NN= line.length()-(nread-kk)+1;
                    line =line.substring(0,NN);
                    nextPosition= kk-1;
                    
                  }else{
                    
                    nextPosition=nread=-1;
                    line=null;
                  }
                }
                
                Object Res=null;
                if( line !=null)
                    Res=process_lanlGSASLine(ds,line.trim());
              
                if( Res instanceof ErrorString)
                   return Res;
                
             }else if(nread >=match1.length()+2){
               
                nextPosition= nread=-1;
             
               
             }else{//did not find a match
               
               nextPosition=nread=-1;
             }
             if( readResult >=0)
               if( nextPosition +match1.length()+4 >nread){
                 
                offset = Xlate(buffer,nextPosition,nread,0);
                nread=offset;
                nextPosition=0;
                readResult=f.read(buffer,offset, size-offset);
                if(  readResult >=0){
                
                  nread +=readResult;
                  SS= new String(buffer,nextPosition,nread);
                  match= pat.matcher( SS);
                  eolnmatch = eolnPat.matcher(SS);
                  
                } 
              }
               
         }//while nread >=0
      
      }catch(Exception s2){
        
         return new ErrorString( s2.toString()+"in LoadDifsGsas");
      }
         
      return null;
  }
  
  
  
  
  
  /**
   *   Translates the elements of the buffer down 
   * @param buffer  The buffer with elements
   * @param start    Start position in buffer that is to be translated down
   * @param end      One more than the last position in buffer that is to be translated down
   * @param downto   The position(usually 0) to translate down to
   * @return   The offset where next data can be added 
   */
  public static int Xlate( byte[] buffer, int start, int end, int downto){
     if( buffer == null)
        return 0;
    if( end > buffer.length)
       end= buffer.length;
    if( end <0)
       end =0;
    if( start<0) start=0;
      if((start >= end))
         return 0;
    for( int i=start; i<end;i++)
        buffer[downto+i-start]=buffer[i];
        
    return downto+end-start;
  }
  
  
  
  //Processes one line in the GSAS parameter file that is in lanl format
  private static  Object process_lanlGSASLine( DataSet ds, String line){
    
    if( (line == null)||(line.length()<1))
       return new ErrorString("There is no other info on special lines. Improper lanl GSAS format");
       
    line=line.trim();
    String[] Data = line.split("[ ]+");
   
      int i=0;
      
      int bankNum =-1;
      for(i=0; (i<Data.length)&&(bankNum<0);i++){
            try{
              bankNum= Integer.parseInt( Data[i]);
            }catch(Exception ss){
               bankNum=-1;
            }
         }
    if( bankNum<0)
       return new ErrorString("Data on line in lanl GSAS format has no legitimate numbers");
      
    float difc= Float.NaN;
    for(; (i<Data.length)&&Float.isNaN(difc);i++){
       try{
         difc= Float.parseFloat( Data[i]);
       }catch(Exception ss){
        difc=Float.NaN;
       }
      }
    if( Float.isNaN(difc))
       return new ErrorString("Data on line in lanl GSAS format has no legitimate numbers");
         
         

    float difa= Float.NaN;
    for(; (i<Data.length)&&Float.isNaN(difa);i++){
       try{
         difa= Float.parseFloat( Data[i]);
       }catch(Exception ss){
          difa=Float.NaN;
       }
    }
    if( Float.isNaN(difa))
       return new ErrorString("Data on line in lanl GSAS format has no legitimate numbers");
        

    float t0= Float.NaN;
    for(; (i<Data.length)&&Float.isNaN(t0);i++){
       try{
         t0= Float.parseFloat( Data[i]);
       }catch(Exception ss){
         t0=Float.NaN;
       }
    }
      
    if( Float.isNaN(t0))
       return new ErrorString("Data on line in lanl GSAS format has no legitimate numbers");
         
    Data d=ds.getData_entry(bankNum-1);
    if(d==null)
       return new ErrorString("No data block at "+bankNum);

    GsasCalibAttribute calibAttr=new GsasCalibAttribute(Attribute.GSAS_CALIB,
                                      new DataSetTools.gsastools.GsasCalib(difc,difa,t0));
    d.setAttribute(calibAttr); 
    return null;   
    
  }
  
  
  /**
   * Will transpose dimensions as specified in the Xlate array
   * @param ds  The dataset with dimensions messed up
   * @param Xlate  A translation array. [detector,col,row,time]=[3,2,1,0] is natural
   *      ISAW dimension order. This array should contain corresponding dimension position 
   *      (a la C starting at 0) in the NeXus file with the property given above.
   *      i.e. if the time axis/dimension is the 2nd array position(a la C starting at 0) in the NeXus file, 
   *      then the last entry of of Xlate should be 2.If there is only one detector, then the first entry
   *      of Xlate should be 3
   * @param ntimes the #of times( not time bins if Histogram)
   * @param nrows  number of rows in all detectors in Nexus file
   * @param ncols the number of columns in all detectors(must be in separate NXdata if not same)
   * @param ndet  the number of detectors in this data set
   * @param Xscale  If the time dimension is not the first dimension, this information
   *                must be given.
   * @return  A dataset with the data in the proper order.
   */
  public static Object Transpose( DataSet ds, int[]Xlate,int nrows,
     int ncols, int ndet,XScale Xscale, boolean isHistogram){
    
    
    if( ds ==null)
       return new ErrorString("DataSet does not exist");
    if( Xlate==null)
       return null;
    if( Xlate.length <1)
       return null;
    
   if( Xscale==null)
          if( Xlate[0]==0){
          
              Xscale = ds.getData_entry(0).getX_scale();
              isHistogram =ds.getData_entry(0) instanceof HistogramTable;
          }else{
              return new ErrorString("An XScale is needed");
          }
    int H=0;
    if( isHistogram)
       H=1;
    int ntimes = Xscale.getNum_x()-H;
    int nys=ds.getData_entry(0).getY_values().length ;
    if( ds.getNum_entries()*nys!= nrows*ncols*ndet*ntimes)
       return new ErrorString("Number of data blocks do not correspond to the rows,cols, and detectors");
    DataSet Res = new DataSet(ds.getTitle(),"Transposed","us","Time","Counts","Intensity");

    float[]yvals = new float[ntimes]; 
    float[]errs = null;
    if( ds.getData_entry(0).getErrors() !=null)
        errs=new float[ntimes];
   
   int[] size = new int[4];
   size[0]= ntimes;
   size[1]=nrows;
   size[2]=ncols;
   size[3]=ndet;
   int P=0;
   
   int[]mult= new int[4];
   //mult[0] is time, mult[1]=row, mult[2] col mult;
   mult[0]=mult[1]=mult[2]=mult[3]=1;
   for( int i=0; i < 4 ; i++ ){
     int c=Xlate[3-i];
     for(int j=0;j <= 3; j++)
        if( Xlate[j] < c )
           mult[i] *= size[3-j];
   }
   
    System.out.println("mults="+mult[0]+","+mult[1]+","+mult[2]+","+mult[3]);
   
    int Group_ID=1;
    for( int d=0; d <ndet; d++){
       P=d*mult[3];
     
      for( int r=0; r< nrows; r++){
      int Prow= P+r*mult[2];
      for( int c=0; c<ncols; c++){
        Arrays.fill( yvals, 0f);
        if( errs != null)
            Arrays.fill( errs, 0f);
        int Pcol =Prow+c*mult[1];
        for(int t=0; t < ntimes; t++){
           
           int Ptime=Pcol+t*mult[0];
           yvals[t]= ds.getData_entry(Ptime/nys).getY_value(Ptime%nys,IData.SMOOTH_NONE);
           if( errs != null)
              errs[t]=ds.getData_entry(Ptime/nys).getErrors()[Ptime%nys];
           
        }  //time
        Data D=null;
        if( isHistogram)
           D= new HistogramTable(Xscale, yvals, errs,Group_ID++);
        else
           D= new FunctionTable(Xscale, yvals, errs,Group_ID++);
        Res.addData_entry(D);
      }//col      
    }//row
    }//det
    return Res;
   
  }
  /**
   * Test program for Transpose
   * @param args
   */
  public static void main( String args[]){
    DataSet[] DDs=null;
    try{
      
   
     DDs= Command.ScriptUtil.load( args[0]);
    }catch(Exception s){
      System.exit(0);
    }
    // ----- Now get XScale------------
    float[] xvals = new float[199];
    xvals[0]=21568;
    double r= Math.log(499071.8/21568.)/(double)198;
    r = Math.pow(Math.E,r);
    for( int i=1; i<199;i++)
       xvals[i]=(float)(xvals[i-1]*r);
    VariableXScale Xscale= new VariableXScale(xvals);
    //-----------------------------
    int[]Xlate= new int[4];
    Xlate[0]=3; Xlate[1]=0;Xlate[2]=1;Xlate[3]=2;
    //----------------------------------
    Object Res =LoadUtil.Transpose( DDs[3],Xlate,128,128,1,Xscale,true);
    if( Res instanceof ErrorString){
       System.out.println("Error="+Res);
       System.exit(0);
    }
    DataSet ds=(DataSet)Res;
    LoadUtil.LoadDetectorInfo(ds,
                            "C:/ISAW/SampleRuns/LansSand/lanlSand.det");
    Command.ScriptUtil.display(ds);
    try{
    
    Command.ScriptUtil.save("C:/ISAW/SampleRuns/LansSand/xxx.isd",ds);
    }catch(Exception s){
       System.out.println("Could not save:"+s);
    }
    
  }
  private static void ShowUsage(){
      System.out.println(" There are 2 arguments");
      System.out.println("    argument 1: is the filename");
      System.out.println("    argument 2: the name of the dataset to convert");
      System.out.println("    argument 3: the name of the parameter file");
  }
  
  
  /**
   *  A test program for the method LoadDifsGsas_lanl using "fixed" filename
   * @param args
   */
  public static void main5( String args[]){
    
    if( (args==null)||(args.length <2)){
      ShowUsage();
      System.exit(0);
    }
       
    DataSet[] DS=null;
    try{
    
      DS = Command.ScriptUtil.load(args[0]);
      
    }catch(Exception ss){
      
       System.out.println("Error:"+ss);
       System.exit(0);
    }
    
   DataSet DD =null;
   for( int i=0; (i< DS.length)&&(DD==null);i++){
       System.out.println("Data Set Name:"+ DS[i].getTitle() );
       if( DS[i].getTitle().equals(args[1]))
          DD=DS[i];
   }
   
   if( DD==null){
     System.out.println(" Could not find a data set with the name "+ args[1]);
     System.exit(0);
   }
   
   try{
   
   System.out.println( LoadUtil.LoadDifsGsas_lanl( DD,args[2]));
   
   }catch(Exception ss){
     
      ss.printStackTrace();
   }
   
   
   DataSetFactory.addOperators(DD, DataSetTools.instruments.InstrumentType.TOF_DIFFRACTOMETER);
   
   //Remove spectra with 0 for both dif_c and dif_a
   for(  int i=DD.getNum_entries()-1;i>=0;i--){
     
     Data D=DD.getData_entry(i);
     Object A = D.getAttributeValue( Attribute.GSAS_CALIB);
     GsasCalib G=(GsasCalib)A;
     if(G.dif_a()==0)if( G.dif_c()==0)
        DD.removeData_entry(i);
      
   }
   
   Command.ScriptUtil.display(DD,"Image View");
 
  }
 
   
   
  
  /**
   *  Main program for testing purposes.
   */
  public static void main1( String args[] )
  {
    String    detector_path = null;
    String    data_path     = null;
    String    file_name     = null;
    Retriever retriever;
    DataSet   ds;

/*  Thes first section tests the LoadDetectorInfo method, using data 
    and detector information from the SMARTS instrument at LANSCE
*/

    data_path = "/usr2/LANSCE_DATA/smarts/";
    file_name = data_path + "/SMARTS_E2005004_R020983.nx.hdf";
    retriever = new NexusRetriever( file_name );
    ds        = retriever.getDataSet(2);

    detector_path = "/home/dennis/WORK/ISAW/LANSCE/SMARTS/";
    file_name     = detector_path + "smarts_detectors.dat";
    LoadDetectorInfo( ds, file_name );

    new ViewManager( ds, "Image View" );
    new ViewManager( ds, "3D View" );

/*  The second section tests the Load_GLAD_LPSD_Info method, using data
    and detector information from the GLAD instrument at IPNS
*/
    data_path = "/usr2/ARGONNE_DATA/";
    file_name = data_path + "glad6942.run";
    retriever = new RunfileRetriever( file_name );
    ds        = retriever.getDataSet(1); 

    detector_path = "/home/dennis/WORK/ISAW/Databases/";
    file_name = detector_path + "gladdets6.par";
    Load_GLAD_LPSD_Info( ds, file_name );

    new ViewManager( ds, "3D View" );
  }
}

