
/*
 * $Id$
 *
 * $Log$
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

  public final static String HDF     = "hdf";
  public final static String NEXUS   = "nxs";
  public final static String RUNFILE = "run";


  public NeutronDataFileFilter()
  {
    super();
  }


  public boolean accept( File f )
  {
    if(  f.isDirectory()  ) 
      return true;

    String extension = getExtension(f);
    if( 
      extension != null  &&
      (
        extension.equals( HDF     ) ||
        extension.equals( NEXUS   ) ||
        extension.equals( RUNFILE )  
      )
    )
      return true;

    else 
      return false;
  }


  public String getDescription()
  {
    return new String( 
      "Neutron Data Files (*." + HDF     + ", " +
                          "*." + NEXUS   + ", " +
                          "*." + RUNFILE + ")"  );
  }


  /*
   * Get the extension of a file.
   */  
  public static String getExtension( File f )
  {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 &&  i < s.length() - 1) 
    {
      ext = s.substring(i+1).toLowerCase();
    }

    return ext;
  }

} 

