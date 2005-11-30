/*
 * File:  ViewASCII.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.12  2005/11/30 18:41:27  rmikk
 *  ScreenSize variable is now always set to a non-null value.
 *
 *  Revision 1.11  2005/10/15 21:35:59  rmikk
 *  The search and find next have been implemented. A JTextPane was 
 *     used and the load time( using their load) is a little slower.
 *
 *  Revision 1.10  2005/10/09 20:43:15  rmikk
 *  Replaced the file load routine by one already in IsawGUI.Util.  It now 
 *     loads larger files more quickly
 *
 *  Revision 1.9  2005/05/25 19:37:46  dennis
 *  Replaced direct call to .show() method for window,
 *  since .show() is deprecated in java 1.5.
 *  Now calls WindowShower.show() to create a runnable
 *  that is run from the Swing thread and sets the
 *  visibility of the window true.
 *
 *  Revision 1.8  2005/01/10 15:18:17  dennis
 *  Added getCategoryList method to put operator in new position in
 *  menus.
 *
 *  Revision 1.7  2004/03/15 03:28:35  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.6  2003/12/15 02:33:26  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.5  2003/01/23 19:04:39  dennis
 *  Added getDocumentation() method (Chris Bouzek)
 *
 *  Revision 1.4  2002/11/27 23:21:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/10/07 19:45:26  pfpeterson
 *  Now supports print option.
 *
 *  Revision 1.2  2002/10/04 20:08:53  pfpeterson
 *  Changed reload and close buttons to be in a menu-bar. Fixed bug
 *  that no file could be displayed once the dialog was closed using
 *  window decorations. Also fixed bug that the dialog could be larger
 *  than the window.
 *
 *  Revision 1.1  2002/09/25 18:39:56  pfpeterson
 *  Added to CVS.
 *
 *  Revision 1.5  2002/09/17 22:32:28  pfpeterson
 *  Modified to take advantage of new SysUtil class.
 *
 *  Revision 1.4  2002/07/29 18:22:56  pfpeterson
 *  Started working out the details of calling code interactively.
 *
 *  Revision 1.3  2002/07/29 16:35:09  pfpeterson
 *  Now redirects stderr from the process to the console as well.
 *
 *  Revision 1.2  2002/07/29 16:00:07  pfpeterson
 *  Fixed two bugs:
 *   - Confirms that process was started before returning.
 *   - Stops printing once encounters a null string.
 *
 *  Revision 1.1  2002/07/26 22:45:32  pfpeterson
 *  Added to CVS.
 */

package DataSetTools.operator.Generic.Special;

import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.Util.SpecialStrings.LoadFileString;
import gov.anl.ipns.Util.Sys.WindowShower;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.*;

import DataSetTools.operator.*;

/* ------------------------- class ViewASCII ------------------------------- */
/**
 * This operator views the contents of an ASCII file. A given
 * instance of this operator has only one view dialog. To get a second
 * viewer a second operator is needed.
 */
public class ViewASCII extends    GenericSpecial
{
    private static final String FAIL       = "FAILURE";
    private static final String RELOAD     = "Reload";
    private static final String CLOSE      = "Close";
    private static final int    FONT_SIZE  = 12;
    private static int    MAX_WIDTH  = 0;
    private static int    MAX_HEIGHT = 0;

    private JFrame    mw;
    private JTextPane textarea;
    private String    filename;

    /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
    /**
     * Construct an operator with a default parameter list.
     */
    public ViewASCII( )
    {
        super( "ViewASCII" );
        this.mw=null;
        this.textarea=null;
        this.filename=null;
    }

    /* ---------------------- FULL CONSTRUCTOR ----------------------------- */
    /**
     *  Creates operator with title "View ASCII File" and the
     *  specified list of parameters. The getResult method must still
     *  be used to execute the operator.nd.
     *
     *  @param filename  The fully qualified ASCII file name
     */

    public ViewASCII( String filename )
    {
        this();
        parameters=new Vector();
        addParameter(new Parameter("View ASCII File",filename));
    }


    /* ------------------------- setDefaultParameters ---------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters()
    {
        parameters = new Vector();  // must do this to create empty list of
                                    // parameters

        parameters=new Vector();
        addParameter( new Parameter("View ASCII File",new LoadFileString("")));
    }

  /* ------------------------ getCategoryList ------------------------------ */
  /**
   * Get an array of strings listing the operator category names  for 
   * this operator. The first entry in the array is the 
   * string: Operator.OPERATOR. Subsequent elements of the array determine
   * which submenu this operator will reside in.
   * 
   * @return  A list of Strings specifying the category names for the
   *          menu system 
   *        
   */
  public String[] getCategoryList()
  {
    return Operator.UTILS_SYSTEM;
  }


    /* ------------------------------ getCommand --------------------------- */
    /**
     *  @return the command name to be used with script processor, "ViewASCII"
     */
    public String getCommand()
    {
        return "ViewASCII";
    }

    /* ---------------------- getDocumentation --------------------------- */
    /**
     *  Returns the documentation for this method as a String.  The format
     *  follows standard JavaDoc conventions.
     */
    public String getDocumentation()
    {
      StringBuffer s = new StringBuffer("");
      s.append("@overview This operator displays the contents of an ASCII ");
      s.append("file.  A given instance of this operator has only one ");
      s.append("view dialog.  To get a second view dialog a second instance ");
      s.append("of this operator is needed.\n");
      s.append("@assumptions The file exists and is in a readable format ");
      s.append("(i.e. it is an ASCII file).\n");
      s.append("@algorithm Loads the specified file and places its contents ");
      s.append("in a scrollable text area.\n");
      s.append("@param file_name The fully qualified ASCII file name.\n");
      s.append("@return A String indicating that the operator successfully ");
      s.append("displayed the ASCII file.\n");
      s.append("@error Returns an error if the file does not exist.\n");
      s.append("@error Returns an error if the file is not a valid ASCII ");
      s.append("file.\n");
      s.append("@error Returns an error if the file could not be opened.\n");
      s.append("@error Returns an error if the file text could not be added ");
      s.append("to the text area.\n");
      return s.toString();
    }

    /* ------------------------------ getResult ---------------------------- */
    /**
     *   Loads an ASCII file and displays it in a JTextArea.
     */
    public Object getResult()
    {
        filename=getParameter(0).getValue().toString();

        if( (mw!=null) && (! mw.isShowing()) )
          mw=null;
        Dimension screenSize=null;
        //if(mw == null)
        {    
            // set the font for the text display
            Font font=new Font("monospaced",Font.PLAIN,FONT_SIZE);
            screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            // set up the maximum size of the text box
            if(MAX_WIDTH==0 && MAX_HEIGHT==0)
            {
               int fontwidth=12;
               int fontheight=20;

               
               MAX_WIDTH=(int)(screenSize.height*4f/(3f*fontwidth))-2;
               MAX_HEIGHT=(int)(screenSize.height/fontheight)-5;
            }
            mw=new JFrame(filename);
                      mw.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
           try{   
            StyledEditorKit edKit= new StyledEditorKit();
            Document doc = edKit.createDefaultDocument();
            edKit.read(new java.io.FileInputStream(filename),doc,0);
            textarea=new JTextPane((StyledDocument)doc);//doc);
            textarea.setEditable(false);
            textarea.setFont(font);
            textarea.setCaretColor((java.awt.Color.blue));  
            }catch(Exception ss){
              ss.printStackTrace();
              return new gov.anl.ipns.Util.SpecialStrings.ErrorString("io Err:"+ss);
            }
            // fill up the text area


            JScrollPane sp=new JScrollPane(textarea);
            JMenuBar menuBar=new JMenuBar();
            JMenu fileMenu=new JMenu("File");
            JMenu EditMenu = new JMenu("Edit");
            JMenuItem reloadMenu=new JMenuItem(RELOAD);
            JMenuItem closeMenu=new JMenuItem(CLOSE);
            JMenuItem SearchMenu = new JMenuItem( "Search" );
            JMenuItem FindNextMenu = new JMenuItem("Find Next");
            menuBar.add(fileMenu);
            menuBar.add(EditMenu);
            gov.anl.ipns.Util.Sys.PrintComponentActionListener.setUpMenuItem(
                                                                  menuBar, mw);
            fileMenu.add(reloadMenu);
            fileMenu.add(closeMenu);
            EditMenu.add(SearchMenu);
            EditMenu.add( FindNextMenu);
            MyActionListener mal=new MyActionListener(this);
            reloadMenu.addActionListener(mal);
            closeMenu.addActionListener(mal);
            SearchMenu.addActionListener( mal);
            FindNextMenu.addActionListener( mal);
                       

            // create the main window to hold it all
            
            // add the text area
            mw.setJMenuBar(menuBar);
            mw.getContentPane().add( sp );
            // put it up on screen
            
            mw.setSize((int)(.6*screenSize.width),(int)(.85*screenSize.height));
            mw.validate();
        }
     /*   else
        {
            // just fill up the text area
            String text=readFile(filename);

            //an error occurred
            if(text!=null)
              return text;
        }
*/
        mw.setTitle(filename);
        WindowShower.show(mw);

        return "success";
    }

    /* -------------------------------- clone ------------------------------ */
    /**
     * Get a copy of the current SpectrometerEvaluator Operator.  The
     * list of parameters is also copied.
     */
    public Object clone(){
        ViewASCII new_op =  new ViewASCII( );
        new_op.CopyParametersFrom( this );

        return new_op;
    }

    /* ------------------------------- readFile ---------------------------- */
    /**
     * Method to read the file into the document and add the document
     * to the JTextArea.
     *
     * @param filename the file to be loaded. If a null string is
     * passed it uses the filename from the operator when getResult
     * was called.
     *
     * @return a null string is returned if the read went well,
     * otherwise the error message is returned.
     */
    String readFile( String filename){
      if(textarea!=null){
        Document doc = (new IsawGUI.Util()).openDoc( filename);
        try{
          
           textarea.setContentType("text/plain");
           textarea.setText(doc.getText(0,doc.getLength()));
          
        }catch(Exception ss){
           ss.printStackTrace();
           textarea=null; 
        }
       // textarea.setColumns(MAX_WIDTH);
       // textarea.setRows(MAX_HEIGHT);
     }
    return null;

    }
    String readFile1(String filename)
    {
        if(filename==null) filename=this.filename;

        TextFileReader tfr=null;
        String line;
        Document doc=new PlainDocument();
        String text=null;
        int width=0;
        int height=0;

        File file=new File(filename);
        if(! file.exists() )  return FAIL+": File does not exist";
        if(! file.canRead() ) return FAIL+": File is not readable";

        try
        {
            tfr=new TextFileReader(filename);
            while(!tfr.eof())
            {
                height++;
                line=tfr.read_line();
                if(line.length()>width) width=line.length();
                if(text!=null)
                {
                    text=text+"\n"+line;
                }
                else
                {
                    text=line;
                }
            }
            doc.insertString(0,text,null);
        }
        catch(IOException e)
        {
            return "IOException: "+e.getMessage();
        }
        catch(BadLocationException e)
        {
            return FAIL+": Could not add file to TextArea";
        }
        finally
        {
            try
            {
                if(tfr!=null) tfr.close();
            }
            catch(IOException e)
            {
                // let it drop on the floor
            }
        }
        if(textarea!=null){

            textarea.setDocument(doc);
            //if(width>MAX_WIDTH) textarea.setColumns(MAX_WIDTH);
            //if(height>MAX_HEIGHT) textarea.setRows(MAX_HEIGHT);
        }
        return null;
    }
    
    public int Find(int prevpos,  String SearchString, Document doc){
      if( SearchString == null)
         return -1;
      if( doc ==  null)
         return -1;
      int buffsize= Math.max(1000,SearchString.length()*10);
      boolean found =false;
      try{

      int doclength = doc.getLength();
      if( prevpos <0) prevpos=0;
      if(prevpos >= doclength)
        return -1;
      lastpos=prevpos;
            
      String buff = doc.getText(prevpos,Math.min(buffsize,doclength-prevpos));
      int nn=0;
      if( buff.indexOf(SearchString)>=0)
         nn=SearchString.length();
      while(!found){
         int k= buff.indexOf(SearchString,nn);
         nn=0;
         if( k>=0)
            return lastpos+k;
         if( buff.length()< SearchString.length())
            return -1;
         lastpos +=buff.length()-SearchString.length();
         buff = doc.getText( lastpos, Math.min(buffsize,doclength-lastpos));
         if( lastpos+SearchString.length() >= doclength)
            return -1;
         
      }
      }catch(Throwable ss){
         return -1;
      }
      return -1;
    }
    
     public void Positionn( JTextPane textarea, int pos){
       if( pos < 0)
          return;
      
      
      try{
       textarea.getCaret().setDot(pos);
       StyledDocument doc =(StyledDocument)textarea.getDocument();
       SimpleAttributeSet AttrSet = new SimpleAttributeSet();
       if( lastpos >=0) if( lastpos < doc.getLength()){
            // AttrSet.addAttribute(StyleConstants.Bold, new Boolean(false));
       
            doc.setCharacterAttributes(preLastPos,lastLength, AttrSet,true);
        }
        AttrSet = new SimpleAttributeSet();
 
       AttrSet.addAttribute(StyleConstants.Bold, new Boolean(true));
       
       doc.setCharacterAttributes(pos, SearchString.length(), AttrSet,true);
       
      }catch(Throwable ss){
        ss.printStackTrace();
      }
     }
    String SearchString=null;
    int lastpos=0;
    int preLastPos=-1;
    int lastLength =-1;
    class MyActionListener implements ActionListener{
        ViewASCII VA;

        public MyActionListener( ViewASCII va){
            VA=va;
        }

        public void actionPerformed(ActionEvent e){
            if(e.getActionCommand().equals(RELOAD)){
               // VA.readFile(null);
            }else if(e.getActionCommand().equals(CLOSE)){
                VA.mw.dispose();
                VA.mw=null;
            }else if( e.getActionCommand().equals("Search")){
              SearchString = javax.swing.JOptionPane.showInputDialog("Enter Search String");
              lastpos = Find( 0,SearchString, textarea.getDocument());
              if(lastpos <0){
                 SearchString = null;
                 lastpos =0;
              }else{
                  Positionn(textarea,lastpos);
                  preLastPos=lastpos;
                  lastLength= SearchString.length();
              }
              
            }else if( e.getActionCommand().equals("Find Next")){
              
              if(SearchString ==null)
                return;
              lastpos = Find(lastpos,SearchString,textarea.getDocument());
              
              if(lastpos <0){
                 SearchString = null;
                 lastpos =0;
              }else{
                  Positionn(textarea,lastpos);
                  preLastPos = lastpos;
                  lastLength= SearchString.length();
              }
           }   
        }
    }
    /* ------------------------------ main ------------------------------- */
    /**
     * Main method for testing purposes.
     */
    public static void main(String[] args)
    {
        String filename="/IPNShome/pfpeterson/ISAW/Scripts/TestScripts/"
           +"hello.iss";
        //String filename="testASCII.txt";

        ViewASCII op;
        op=new ViewASCII(filename);
        System.out.println("RESULT: "+op.getResult());

        /** ------------ added by Chris Bouzek -------------- */
        System.out.println("Documentation: " + op.getDocumentation());

        //System.exit(0);
    }
}
