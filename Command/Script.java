/*
 * File:  Script.java 
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
 * Revision 1.6  2003/06/13 14:59:02  pfpeterson
 * Changed some fields from private to protected.
 *
 * Revision 1.5  2003/04/02 14:52:51  pfpeterson
 * Altered the fix_script() method to also remove carriage return, '
'.
 *
 * Revision 1.4  2003/03/21 17:27:35  pfpeterson
 * Now adds a "
 * " at the end of the script if it isn't already there.
 *
 * Revision 1.3  2003/03/21 15:27:02  pfpeterson
 * No longer throws NullPointerException when working with null script.
 *
 * Revision 1.2  2003/03/14 16:45:39  pfpeterson
 * Removed some debugging messages.
 *
 * Revision 1.1  2003/03/14 15:18:27  pfpeterson
 * Added to CVS.
 *
 */

package Command;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;

public class Script extends Object{
  protected static final String UNKNOWN="UNKNOWN";

  protected String filename; // the name of the file which contains the script
  private   String script;   // contains the actual document
  private   int[] linenum;   // Zero index of linenumber to buffer index. This
                             // has one more element than necessary to mark the
                             // end of the string.

  // ============================== CONSTRUCTORS
  private Script(){
    this.script=null;
    this.linenum=null;
    this.filename=UNKNOWN;
  }

  public Script(String filename){
    this();
    this.filename=filename;
    this.reload();
  }

  public Script(StringBuffer script){
    this();
    this.script=script.toString();
    this.fix_script();
  }

  public Script(Document doc){
    this();
    if( doc==null || doc.getLength()==0 ) return;
    try{
      this.script=doc.getText(0,doc.getLength());
      this.fix_script();
    }catch(BadLocationException e){
      // let it drop on the floor
    }
  }

  // ============================== PUBLIC CONVERSION METHODS
  public String toString(){
    return this.script;
  }

  public StringBuffer toStringBuffer(){
    if(this.script!=null)
      return new StringBuffer(this.script);
    else
      return null;
  }

  public Document toDocument(){
    if(this.script==null) return null;

    Document doc=new PlainDocument();
    try{
      doc.insertString(0,this.script,null);
      return doc;
    }catch(BadLocationException e){
      return null;
    }
  }

  // ============================== PUBLIC UTILITY METHODS
  /**
   * This loads the script from the file
   */
  public boolean reload(){
    if( filename==null || filename.equals(UNKNOWN) || filename.length()<=0 )
      return false;

    StringBuffer buffer=IsawGUI.Util.readTextFile(this.filename);
    if(buffer==null || buffer.length()<=0)
      return false;

    this.script=buffer.toString();
    this.fix_script();
    this.init_linenum(true);
    return true;
  }

  /**
   * The total number of lines in the script
   */
  public int numLines(){
    // just return if there is an empty script
    if(this.script==null || this.script.length()<=0) return -1;
    
    if(this.linenum==null)
      this.init_linenum(true);
    return this.linenum.length-1;
  }

  /**
   * get a particular line from the script
   */
  public String getLine(int line){
    // just return if there is an empty script
    if(this.script==null || this.script.length()<=0) return null;

    // initialize the linenumber array if it isn't already
    if(this.linenum==null)
      this.init_linenum(true);

    // return null if index is out of bounds
    if(line<0 || line+1>=this.linenum.length)
      return null;

    // return the appropriate substring
    return this.script.substring(this.linenum[line],this.linenum[line+1]);
  }

  // ============================== PRIVATE UTILITY METHODS
  /**
   * This method removes "\r" and appends a "\n" at the end of the
   * script if it isn't already there.
   */
  private void fix_script(){
    int index=this.script.indexOf("\r");
    if( index>=0){
      StringBuffer buffer=new StringBuffer(this.script);
      while(index>0){
        buffer.deleteCharAt(index);
        index=buffer.toString().indexOf("\r");
      }
      this.script=buffer.toString();
    }

    if(! this.script.endsWith("\n") ){
      this.script=this.script+"\n";
    }
  }

  /**
   * This method creates the linenum array and initializes its
   * elements to -1. If the parameter is set to true then it will
   * subsequently fill out the array. This is an attempt at a lazy
   * lock method for line numbers.
   */
  private void init_linenum(boolean fill_in){
    int linecount=0;
    int index=0;

    // don't initialize an empty script
    if(this.script==null || this.script.length()<=0) return;

    // first count the number of lines
    //while( index>=0 ){
    while(true){
      index=this.script.indexOf("\n",index+1);
      if(index>=0)
        linecount++;
      else
        break;
    }

    // then initialize all line_numbers to -1
    this.linenum=new int[linecount+1];
    if(fill_in){
      index=0;
      linenum[0]=0;
      for( int i=1 ; i<=linecount ; i++ ){
        index=this.script.indexOf("\n",index+1);
        this.linenum[i]=index+1;
      }
    }else{
      for( int i=0 ; i<=linecount ; i++ )
        this.linenum[i]=-1;
    }
  }

  // ============================== MAIN METHOD FOR TESTING ONLY
  public static void main(String[] args){
    if(args.length<1){
      System.out.println("USAGE: Script <filename>");
      System.exit(-1);
    }

    System.out.println("Loading "+args[0]);
    Script script=new Script(args[0]);
    System.out.print(script.toString());
    for( int i=0 ; i<script.numLines() ; i++ ){
      System.out.println(i+">>"+script.getLine(i)+"<<");
    }
  }
}
