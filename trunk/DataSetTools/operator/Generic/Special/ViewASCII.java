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
 *
 *
 */

package DataSetTools.operator.Generic.Special;

import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  java.awt.*;
import  java.awt.event.*;
import  java.io.*;
import  java.util.Vector;
import  javax.swing.text.*;
import  javax.swing.*;
import DataSetTools.viewer.*;
import IsawGUI.*;

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
    private JTextArea textarea;
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

        if(mw==null)
        {
            // set the font for the text display
            Font font=new Font("monospaced",Font.PLAIN,FONT_SIZE);

            // set up the maximum size of the text box
            if(MAX_WIDTH==0 && MAX_HEIGHT==0)
            {
               int fontwidth=12;
               int fontheight=20;

               Dimension screenSize =
                        Toolkit.getDefaultToolkit().getScreenSize();
               MAX_WIDTH=(int)(screenSize.height*4f/(3f*fontwidth))-2;
               MAX_HEIGHT=(int)(screenSize.height/fontheight)-5;
            }

            // create the TextArea
            textarea=new JTextArea();//doc);
            textarea.setEditable(false);
            textarea.setFont(font);
            JScrollPane sp=new JScrollPane(textarea);

            // fill up the text area
            String text=readFile(filename);

            //an error occurred
            if(text!=null)
              return text;

            //create a menubar
            JMenuBar menuBar=new JMenuBar();
            JMenu fileMenu=new JMenu("File");
            JMenuItem reloadMenu=new JMenuItem(RELOAD);
            JMenuItem closeMenu=new JMenuItem(CLOSE);
            menuBar.add(fileMenu);
            DataSetTools.viewer.PrintComponentActionListener.setUpMenuItem(
                                                                  menuBar, mw);
            fileMenu.add(reloadMenu);
            fileMenu.add(closeMenu);
            MyActionListener mal=new MyActionListener(this);
            reloadMenu.addActionListener(mal);
            closeMenu.addActionListener(mal);

            // create the main window to hold it all
            mw=new JFrame(filename);
            mw.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // add the text area
            mw.setJMenuBar(menuBar);
            mw.getContentPane().add(sp);

            // put it up on screen
            mw.pack();
        }
        else
        {
            // just fill up the text area
            String text=readFile(filename);

            //an error occurred
            if(text!=null)
              return text;
        }

        mw.setTitle(filename);
        mw.show();

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
    String readFile(String filename)
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
            if(width>MAX_WIDTH) textarea.setColumns(MAX_WIDTH);
            if(height>MAX_HEIGHT) textarea.setRows(MAX_HEIGHT);
        }
        return null;
    }

    class MyActionListener implements ActionListener{
        ViewASCII VA;

        public MyActionListener( ViewASCII va){
            VA=va;
        }

        public void actionPerformed(ActionEvent e){
            if(e.getActionCommand().equals(RELOAD)){
                VA.readFile(null);
            }else if(e.getActionCommand().equals(CLOSE)){
                VA.mw.dispose();
                VA.mw=null;
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
