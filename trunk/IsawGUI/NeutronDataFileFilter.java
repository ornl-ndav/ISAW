
/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2001/08/02 19:15:31  neffk
 * added *.isd, ISAW's binary data format, to the filter.
 *
 * Revision 1.2  2001/07/23 20:36:04  neffk
 * added a method to filter filenames using a String parameter instead
 * of a File object.
 *
 * Revision 1.1  2001/07/12 15:00:46  neffk
 * file filter that only shows neutron data files.
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


  public NeutronDataFileFilter()
  {
    super();
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
    if( 
      extension != null  &&
      (
        extension.equals( HDF         ) ||
        extension.equals( NEXUS       ) ||
        extension.equals( RUNFILE     ) ||
        extension.equals( ISAW_NATIVE ) 
      )
    )
      return true;

    else 
      return false;
  }

  
  /**
   * returns a description of this filter for use in the the file chooser.
   */ 
  public String getDescription()
  {
    return new String( 
      "Neutron Data Files (*." + HDF         + ", " +
                          "*." + NEXUS       + ", " +
                          "*." + RUNFILE     + ", " +
                          "*." + ISAW_NATIVE + ")"  );
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

