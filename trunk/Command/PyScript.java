/*
 * File:  PyScript.java 
 *             
 * Copyright (C) 2003, Peter Peterson
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
 * Contact : Peter Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           Argonne, IL 60439-4845, USA
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
 * Revision 1.8  2003/10/15 16:22:30  bouzekc
 * Added the filename to the error Isaw generates when it encounters a
 * Python script syntax error upon startup.
 *
 * Revision 1.7  2003/08/17 23:03:44  bouzekc
 * Fixed off by one error in isValid() when reporting what line a script
 * error is on.
 *
 * Revision 1.6  2003/07/14 20:59:11  bouzekc
 * Removed a println() which printed out an error message if
 * a class definition was not found at the top of a python
 * file.
 *
 * Revision 1.5  2003/07/07 19:44:26  bouzekc
 * Added error message outputs for syntax errors and missing
 * "class" name.
 *
 * Revision 1.4  2003/06/18 18:13:09  pfpeterson
 * Added a method to get the classname that should be in the script.
 * isValid() now checks that the class is defined in the script.
 *
 * Revision 1.3  2003/06/13 20:05:57  pfpeterson
 * Added a comment.
 *
 * Revision 1.2  2003/06/13 16:27:14  pfpeterson
 * Error conversion switches from one index to zero indexed.
 *
 * Revision 1.1  2003/06/13 14:58:34  pfpeterson
 * Added to CVS.
 *
 */
package Command;

import javax.swing.text.Document;
import org.python.core.PyException;

public class PyScript extends Script{
  private static boolean DEBUG = false;
  private        Boolean valid = null;

  // ============================== CONSTRUCTORS
  public PyScript(String filename){
    super(filename);
  }

  public PyScript(StringBuffer script){
    super(script);
  }

  public PyScript(Document doc){
    super(doc);
  }

  // ============================== PUBLIC UTILITY METHODS
  public boolean reload(){
    this.valid=null;
    return super.reload();
  }

  public boolean isValid(){
    if(valid!=null)
      return valid.booleanValue();

    boolean hasClass=false;

    int numLines=this.numLines();
    String line=null;
    for( int i=0 ; i<numLines ; i++ ){
      line=this.getLine(i);
      if(line==null || line.length()<=0 || line.equals("\n")){
        continue;
      }else if(line.startsWith(" ")){
        continue;
      }else if(line.startsWith("#")){
        continue;
      }else if(line.startsWith("class")){
        if(line.indexOf(this.getClassname())>0)
          hasClass=true;
        else {
          DataSetTools.util.SharedData.addmsg( 
          "Class name and file name do not agree:\n" +
          "  line " + i + ".");
          return false;
        }
        continue;
      }else if(line.startsWith("from")){
        continue;
      }else if(line.startsWith("import")){
        continue;
      }else if(line.startsWith("def")){
        continue;
      }else if(line.startsWith("if")){
        int name_index=line.indexOf("__name__");
        int main_index=line.indexOf("__main__");
        if(name_index>0 && main_index>name_index)
          continue;
      }          
      if(DEBUG) System.out.println(">>"+line+"<<");

      //there is an error in the file, so list the name and number
      if( filename != null ) {
        DataSetTools.util.SharedData.addmsg( 
          filename + " has a syntax error on line " + ( i + 1 ) + ".");
      } else {
        DataSetTools.util.SharedData.addmsg( 
          "Script syntax error on line " + ( i + 1 ) + ".");
      }
      valid=new Boolean(false);
      return valid.booleanValue();
    }
    line=null;

    //this adds too much console stuff when ISAW starts up.
    /*if( !hasClass ){
      DataSetTools.util.SharedData.addmsg( 
      "No class name specified:\n" );
    }*/
    valid=new Boolean(hasClass);
    return valid.booleanValue();
  }

  public String getClassname(){
    // the class name must match the filename
    String filename=this.getFilename();
    if(filename==null || filename.equals(UNKNOWN)) return null;

    // ditch the directory name
    int index=filename.lastIndexOf("/");
    if(index<0)
      index=0;
    else
      index++;

    // return the filename with out the '.py'
    return filename.substring(index,filename.length()-3);
  }

  public ParseError generateError(PyException pyexcep){
    if(this.filename.equals(UNKNOWN))
      return generateError(pyexcep,null);
    else
      return generateError(pyexcep,this.filename);
  }

  public static ParseError generateError(PyException pyexcep, String filename){
    String dumpStack=pyexcep.traceback.dumpStack();
    if(filename!=null && filename.length()>0){
      int index=dumpStack.indexOf("<string>");
      if(index>=0)
        dumpStack=dumpStack.substring(0,index)+filename
                                                 +dumpStack.substring(index+8);
    }
    ParseError pe=new ParseError(dumpStack,pyexcep.traceback.tb_lineno-1);
    return pe;
  }

  // ============================== MAIN METHOD FOR TESTING ONLY
  public static void main(String[] args){
    if(args.length<1){
      System.out.println("USAGE: PyScript <filename>");
      System.exit(-1);
    }

    System.out.println("Loading "+args[0]);
    PyScript script=new PyScript(args[0]);
    if(script.isValid())
      System.out.println("is valid");
    else
      System.out.println("is NOT valid");
  }
}
