package NexIO; 
import java.io.File; 
import javax.swing.*; 
import javax.swing.filechooser.*;  

public class NexusfileFilter extends FileFilter{
  public boolean accept(File f) {
    if (f.isDirectory() ) {return true;}      
    String s = f.getName();     
    int index = s.lastIndexOf('.');      
    String extension = null;     
    if ( index > 0 && index < s.length() - 1 ) {
      extension = s.substring(index +1).toLowerCase();         
    }   
    if ( extension != null ) {
      if ( extension.equals("nxs")|| extension.equals("hdf")){
        return true;            
      }else{
        return false;       
      }        
    }
    return false;      
  }  

  public String getDescription(){
    return "NeXus file (*.nxs/hdf)";      
  } 
} 
