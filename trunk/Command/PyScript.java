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

  public boolean reload(){
    this.valid=null;
    return super.reload();
  }

  public boolean isValid(){
    if(valid!=null)
      return valid.booleanValue();

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
        continue;
      }else if(line.startsWith("from")){
        continue;
      }else if(line.startsWith("import")){
        continue;
      }else if(line.startsWith("if")){
        int name_index=line.indexOf("__name__");
        int main_index=line.indexOf("__main__");
        if(name_index>0 && main_index>name_index)
          continue;
      }          
      if(DEBUG) System.out.println(">>"+line+"<<");
      valid=new Boolean(false);
      return valid.booleanValue();
    }
    line=null;

    valid=new Boolean(true);
    return valid.booleanValue();
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
