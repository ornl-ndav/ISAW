/*
 * File:   DetectorInfo.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2002/06/14 21:22:55  rmikk
 *  Implements IXmlIO interface
 *
 *  Revision 1.1  2001/07/10 18:46:46  dennis
 *  Initial version of Class to hold information on individual
 *  detector positions, sizes, efficiency, etc.
 *
 *
 */
package  DataSetTools.instruments;

import java.io.*;
import java.text.*;
import DataSetTools.math.*;
import DataSetTools.dataset.*;

/**
 * DetectorInfo represents the position, size, ID, and efficiency of an
 * individual detector segment.  This may be only one pixel of an area
 * detector or LPSD, but may also be an entire detector if it is not
 * segmented.
 */
public class DetectorInfo implements Serializable ,
                                     DataSetTools.dataset.IXmlIO
{
  private int               seg_num;   // ID for this particular segment
  private int               det_num;   // "raw" detector ID for detector
                                       // containing this detector segment
  private int               row,       // row and column number for this
                            column;    // segment on the detector

  private DetectorPosition  position; 

  private float             length;
  private float             width;
  private float             depth;

  private float             efficiency;

  /**
   *  Construct a new DetectorInfo object with the specified data values
   */
  public DetectorInfo( int               seg_num, 
                       int               det_num, 
                       int               row, 
                       int               col,
                       DetectorPosition  position,
                       float             length,
                       float             width,
                       float             depth,
                       float             efficiency )
  {
    this.seg_num    = seg_num;
    this.det_num    = det_num;
    this.row        = row;
    this.column     = col;
    this.position   = position;
    this.length     = length;
    this.width      = width;
    this.depth      = depth;
    this.efficiency = efficiency;
  }


  /**
   *  Construct a new DetectorInfo object with the same data values as the
   *  specified DetectorInfo object
   */
  public DetectorInfo( DetectorInfo  info )
  {
    this.seg_num    = info.seg_num;
    this.det_num    = info.det_num;
    this.row        = info.row;
    this.column     = info.column;
    this.position   = info.position;
    this.length     = info.length;
    this.width      = info.width;
    this.depth      = info.depth;
    this.efficiency = info.efficiency;
  }

  /**
  * Constructor needed to use the XMLread and XMLwrite routines.
  */
  public DetectorInfo()
  {this(-1,-1,-1,-1,new DetectorPosition(), 0.0f,0.0f,0.0f,0.0f);
  }

  /**
   *  Get the segment number for this Detector segment. 
   *
   *  @return  The segment number for this object.
   */
  public int getSeg_num()
  {
    return seg_num;
  }

  /**
   *  Get the main detector number for the Detector segment.
   *
   *  @return  The detector number for this object.
   */
  public int getDet_num()
  {
    return det_num;
  }

  /**
   *  Get the row number for the Detector segment.
   *
   *  @return  The row number for this segment.
   */
  public int getRow()
  {
    return row;
  }


  /**
   *  Get the column number for the Detector segment.
   *
   *  @return  The column number for this segment.
   */
  public int getColumn()
  {
    return column;
  }


  /** 
   *  Get the nominal detector position for the Detector segment.
   *
   *  @return  The detector position for this object.
   */
  public DetectorPosition getPosition()
  {
    return position;
  }


  /** 
   *  Get the nominal detector length for the Detector segment.
   *
   *  @return  The detector length for this object.
   */
  public float getLength()
  {
    return length;
  }


  /** 
   *  Get the nominal detector width for the Detector segment.
   *
   *  @return  The detector width for this object.
   */
  public float getWidth()
  {
    return width;
  }


  /** 
   *  Get the nominal detector depth for the Detector segment.
   *
   *  @return  The detector depth for this object.
   */
  public float getDepth()
  {
    return depth;
  }


  /** 
   *  Get the nominal detector efficiency for the Detector sgement.
   *
   *  @return  The detector efficiency for this object.
   */
  public float getEfficiency()
  {
    return efficiency;
  }

  /** Implements the IXmlIO interface so a DetectorInfo can write itself
  *
  * @param stream  the OutputStream to which the data is written
  * @param mode    either IXmlIO.BASE64 or IXmlOP.NORMAL. This indicates 
  *                how a Data's xvals, yvals and errors are written
  * @return true if successful otherwise false<P>
  *
  * NOTE: This routine writes all of the begin and end tags.  These tag names
  *      are DetectorInfo
  */
  public boolean XMLwrite( OutputStream stream, int mode )
  { try
    {stream.write("<DetectorInfo>\n".getBytes());

     stream.write(("<seg_num>"+ seg_num+"</seg_num>\n").getBytes());
     stream.write(("<det_num>"+   det_num+"</det_num>\n").getBytes());
     stream.write(("<row>"+  row +"</row>\n").getBytes()); 
     stream.write(("<column>"+   column  +"</column>\n").getBytes());
     stream.write("<position>\n".getBytes());
     if( !((Position3D)position).XMLwrite( stream, mode))
       return false;
     stream.write(("</position>\n").getBytes());
     stream.write(("<length>"+   length  +"</length>\n").getBytes());
     stream.write(("<width>"+   width   +"</width>\n").getBytes());
     stream.write(("<depth>"+   depth   +"</depth>\n").getBytes());
     stream.write(("<efficiency>"+   efficiency   +"</efficiency>\n").getBytes());

     stream.write("</DetectorInfo>\n".getBytes());
     return true;
    }
    catch(Exception s)
    { return xml_utils.setError("Exception="+s.getMessage());
    }
  }

  /** Implements the IXmlIO interface so a DetectorInfo can read itself
  *
  * @param stream  the InStream to which the data is written

  * @return true if successful otherwise false<P>
  *
  * NOTE: This routine assumes the begin tag has been completely read.  It reads
  *       the end tag.  The tag name is DetectorInfo
  *       
  */
  public boolean XMLread( InputStream stream )
  { try
    { int NN;
      float x;
      
      if(!get_tag(stream, "seg_num"))
        return false;
      NN = getIntValue( stream,"seg_num");
      if( err == null)
        seg_num = NN;
     
      if(!get_tag(stream, "det_num"))
        return false;
      NN  = getIntValue( stream,"det_num");
      if( err == null)
        det_num = NN;
     
      if(!get_tag(stream, "row"))
        return false;
      NN  = getIntValue( stream,"row");
      if( err == null)
        row = NN;
     
      if(!get_tag(stream, "column"))
        return false;
      NN  = getIntValue( stream,"column");
      if( err == null)
        column = NN;
     
      if(!get_tag(stream, "position"))
        return false;
      xml_utils.getTag( stream);
      xml_utils.skipAttributes( stream);
      if( !((Position3D)position).XMLread( stream))
        return false;
      if(!get_tag(stream,"/position"))
        return xml_utils.setError("Improper unclosed tag,position");
      
      if(!get_tag(stream, "length"))
        return false;
      x = getFloatValue( stream,"length");
      if( err == null)
        length = x;
     
      if(!get_tag(stream, "width"))
        return false;
      x = getFloatValue( stream,"width");
      if( err == null)
        width = x;
    
      if(!get_tag(stream, "depth"))
        return false;
      x = getFloatValue( stream,"depth");
      if( err == null)
        depth = x;
     
      if(!get_tag(stream, "efficiency"))
        return false;
      x = getFloatValue( stream,"efficiency");
      if( err == null)
        efficiency = x;
    
      if( !get_tag( stream, "/DetectorInfo"))
        return xml_utils.setError( "Unmatched DetectorInfo tag");
    
      return true;


    }
    catch(Exception s)
    { return xml_utils.setError( s.getMessage());
    }

  }
  
  private boolean get_tag( InputStream stream, String TagName )
  { try
    { String Tag = xml_utils.getTag( stream );
      if( Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if( !Tag.equals(TagName))
        return xml_utils.setError( "Improper tag. Should be seg_num");
      if(!xml_utils.skipAttributes( stream ))
        return xml_utils.setError( xml_utils.getErrorMessage());
      return true;
    }
    catch( Exception s )
    { return xml_utils.setError( "Exception="+s.getMessage());    
    }
  }
  
  String  err;

 private int getIntValue( InputStream stream, String tag) 
                    throws java.lang.Exception
   { err = null;
     String v = xml_utils.getValue( stream );
     if(v == null)
      { err=""+( xml_utils.getErrorMessage());
        return 0;
      }
     String tag1= xml_utils.getEndTag( stream );
     if( tag1 == null )
       { err=xml_utils.getErrorMessage();
         return 0;
       }
     if(!tag1.equals("/"+tag))
       { err="Improper end tag";
         throw new java.lang.Exception( err);
         
        }
     if(! xml_utils.skipAttributes( stream ))
       {err=""+( xml_utils.getErrorMessage());
        return 0;
       }

     return (new Integer( v)).intValue();
    }

private float getFloatValue( InputStream stream, String tag) 
                    throws java.lang.Exception
   { err = null;
     String v = xml_utils.getValue( stream );
     if(v == null)
      { err=""+( xml_utils.getErrorMessage());
        return 0;
      }
     String tag1= xml_utils.getEndTag( stream );
     if( tag1 == null )
       { err=xml_utils.getErrorMessage();
         return 0;
       }
     if(!tag1.equals("/"+tag))
       { err="Improper end tag";
         throw new java.lang.Exception( err);
        
        }
     if(! xml_utils.skipAttributes( stream ))
       {err=""+( xml_utils.getErrorMessage());
        return 0;
       }

     return (new Float( v)).floatValue();
    }
  /**
   *  Form a string listing the detector info.
   *
   *  @return  String containing the detector ID, location and size information.
   */
  public String toString()
  {
     NumberFormat f = NumberFormat.getInstance();
     f.setMaximumFractionDigits( 2 );

     String s = "Seg: " + seg_num + " Det: " + det_num + "\n";
     s += "(row, col) = (" + row + ", " + column + ")\n";
     s += position.toString() + "\n";

     s += "Size: " + f.format( length ) + 
          "x" + f.format( width ) +
          "x" + f.format( depth ) + "\n";
 
     s += "Efficiency: " + f.format( efficiency ); 

     return s;
  }

  static public void main( String[] args )
  {
    DetectorPosition point = new DetectorPosition();

    point.setSphericalCoords( -10, (float)Math.PI/6, (float)Math.PI/4 );

    DetectorInfo det_info = new DetectorInfo( 1, 2, 3, 4, point, 5, 6, 7, 8 );
    System.out.println( ""+ det_info );
  }

}
