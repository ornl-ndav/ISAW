/*
 * File:  IssScript.java 
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
 * Revision 1.1  2003/06/13 20:05:40  pfpeterson
 * Added to CVS.
 *
 */

package Command;

import javax.swing.text.Document;

public class IssScript extends Script{
  private String documentation=null;

  // ============================== CONSTRUCTORS
  public IssScript(String filename){
    super(filename);
  }

  public IssScript(StringBuffer script){
    super(script);
  }

  public IssScript(Document doc){
    super(doc);
  }

  // ============================== PUBLIC UTILITY METHODS
  public boolean reload(){
    this.documentation=null;
    return super.reload();
  }

  public String getDocumentation(){
    if(this.documentation==null){
      int lastdocline=this.findEndOfDoc();
      StringBuffer docbuffer=new StringBuffer(80*lastdocline);
      boolean pastfirst_nonempty=false;
      String line;

      for( int i=0 ; i<lastdocline ; i++ ){
        line=this.getLine(i).substring(1).trim();
        if(line==null || line.length()<=0){
          if(pastfirst_nonempty)
            docbuffer.append("\n");
        }else{
          pastfirst_nonempty=true;
          docbuffer.append(line+"\n");
        }
      }
      this.documentation=docbuffer.toString();
    }

    return this.documentation;
  }

  private int findEndOfDoc(){
    String line=null;
    int endofdoc=0;

    for( int i=0 ; i<this.numLines() ; i++ ){
      line=this.getLine(i);
      if( line.startsWith("#") || line.startsWith("\n"))
        endofdoc=i;
      else if(line.trim().length()<=0)
        endofdoc=i;
      else
        break;
    }        
    return endofdoc;
  }

  // ============================== MAIN METHOD FOR TESTING ONLY
  public static void main(String[] args){
    if(args.length<1){
      System.out.println("USAGE: Script <filename>");
      System.exit(-1);
    }

    System.out.println("Loading "+args[0]);
    IssScript script=new IssScript(args[0]);
    System.out.println(">>"+script.getDocumentation()+"<<");
  }
}
