/*
 * File: NeutronDataFileFilter.java
 *
 * Copyright (C) 2001, Kevin Neff
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
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
 * $Log$
 * Revision 1.10  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 * Revision 1.9  2002/08/06 21:30:37  pfpeterson
 * Gsas files are no longer accepted by this filter.
 *
 * Revision 1.8  2002/06/18 19:48:19  rmikk
 * Added file types xmi, xmn and zip. xml replaced
 *
 * Revision 1.7  2002/06/18 18:57:18  rmikk
 * Added filters for xmi and xmn files. XML files, either
 *   ISAW or Nexus forms
 *
 * Revision 1.6  2002/01/08 21:26:21  rmikk
 * Fixed the display of the filter to only show xm.,gsas and
 * isd output and input.
 *
 */

package IsawGUI;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * filters neutron data file types.
 */
public class NeutronDataFileFilter
  extends FileFilter
{

  public final static String HDF         = "hdf";
  public final static String NEXUS       = "nxs";
  public final static String RUNFILE     = "run";
  public final static String ISAW_NATIVE = "isd";
  public final static String XML         = "xmi";
  public final static String NXML        = "xmn";
  public final static String GSAS        = "gsa";
  public final static String ZIP         = "zip";
  boolean SaveFilter;

  public NeutronDataFileFilter()
  {
    super();
    SaveFilter = false;
  }
  public NeutronDataFileFilter( boolean SaveFilter)
   {super();
       this.SaveFilter = SaveFilter;
   }

  public boolean accept( File f )
  {  
    if(  f.isDirectory()  ) 
      return true;

    return accept_filename(  f.getName()  );
  }


  /**
   * provides filename checking capability where it's not convenient
   * to use File objects as parameters.
   */ 
  public boolean accept_filename( String filename )
  { 
                                 //if the filename has a bang (!)
                                 //appended to it, then accept it
                                 //reguardless of its extension.
    int bang_index = filename.lastIndexOf( '!' );
    if(  bang_index == filename.length() - 1  )
      return true;

    String extension = getExtension( filename );
   if( extension == null) return false;
   if( 
      extension != null  &&
      (
        extension.equals( HDF         ) ||
        extension.equals( NEXUS       ) ||        
        extension.equals( ISAW_NATIVE ) ||  
        extension.equals( XML         ) ||
        extension.equals( ZIP         )        
  
      )
    )
      return true;
    else if( extension != null && extension.equals( RUNFILE) &&!SaveFilter )
       return true;
    else if( !SaveFilter) return false;
    else if( extension.equals( NXML) || extension.equals(GSAS))
       return true;
    else  
      return false;
  }

  
  /**
   * returns a description of this filter for use in the the file chooser.
   */ 
  public String getDescription()
  {
    String S = new String( 
      "Neutron Data Files (*." + HDF         + ", " +
                          "*." + NEXUS       + ", ") ;
                          
                         
   if(!SaveFilter)
       S =  "*."+ISAW_NATIVE+"(Temporary) *."+XML+"(Isaw XML) *.zip";
   else
       S =  "*." + ISAW_NATIVE+"(Temporary),*."+XML+"(Isaw XML) *.zip"   ;
   /*S =  "*." + ISAW_NATIVE+"(Temporary),*."+XML+",*."+GSAS+
     "(gsas) *."+XML+"(Isaw XML) *.zip"   ;*/
   //S += " )";
   return S;
  }


  /*
   * Get the extension of a file.
   */  
  public static String getExtension( String filename )
  {
    String ext = null;
    int i = filename.lastIndexOf('.');

    if (i > 0 &&  i < filename.length() - 1) 
    {
      ext = filename.substring(i+1).toLowerCase();
    }

    return ext;
  }

} 

