
/*
 * File:  IsawMain.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
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
 * Revision 1.1  2003/01/27 15:01:36  rmikk
 * Initial Commit. The java applet in the htm page
 *
 */
import java.applet.*;
import java.awt.*;
import java.net.*;
import IsawGUI.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

/** This applet is run in a browser. It diplays the Introductory Isaw Help
*/
public class IsawMain extends Applet
 { 
  URL url;
  public IsawMain()
    {
     super();
    }

  /** Empty
  */
  public void start()
     {
     }

  /** Initializes the layout
  */
  public void init()
    {
     setLayout( new GridLayout( 2,1));
     TextArea tx = new TextArea( 20, 70);
     add( new MyPanel( tx, getBounds(), this));
     add( tx );
     url = getCodeBase();
     validate();
    }

  /** Utililty method that gets the ith Image, i starts at 1.
  */
  public Image getImage( int i)
    { 
     try
       {
        String file= url.getFile();
        file = file.replace('\\','/');
        int k= file.lastIndexOf('/');
        if( k < 0)
           file ="";
        else 
           file = file.substring( 0,k+1);


        URL url2 = new URL(url.getProtocol(), url.getHost(),file);
        return getImage(url2,("Image"+(i+1)).trim()+".jpg" );
       }
     catch( Exception s)
       {
        return null;
       }

    }
 
  // The panel that is displayed in the applet.
  class MyPanel extends Panel
    {
     TextArea tx;  
     int mainImage;  //The "frame" number
     int subImage;   // Gives the number of different texts that appear in the TextArea
     int width,
         height;
     IsawMain A;     //the parent

     //"Program" that causes text and images to change
     Rectangle[][] newText;          //newText[i][j] Is the Rectangle in Frame i that will
                                     //  cause the text for subImage j to appear       
     Rectangle[][] changeImage;      //changeImage[i][j] is the jth rectangle in Frame i that
                                     //   will cause the Frame number(mainImage) to change
     int[][] changeImageNum;         // The new Frame number corresponding to changeImage

     //To add more text
     //    1. Determine the Rectangle that will cause this new text
     //    2. Determine the Frame number and find the next subImage number.
     //    3. Set newText[Frame number][subImage] to be that rectangle.(change dimension first).
     //         This is done in the constructor
     //    4. Add the new text in the setText method

     //To add a new Image
     //   1. Create an Image and resample to fit. Name it imagex.jpg where x is the next image 
     //        number. NOTE:image5.jpg corresponds to Frame 4
     //   2.For each place that will cause this new image to show,
     //        a) Determine Frame number, and j,the number of change rect, and selection Rectangle
     //        b) Set changeImage[Frame number][j] to that rectangle in the constructor
     //        c) Set changeImageNum[Frame number][j] to be the frame number of this
     //             new image.
     //        d) Change setText method so some text will appear


     public MyPanel( TextArea tx, Rectangle R, IsawMain A)
      { super();
        this.tx = tx;
        mainImage=0;
        subImage = -1;
        this.setSize( R.width, R.height/2);
        width = R.width;
        height = R.height;
        this.A = A;
        this.setLayout( new GridLayout(1,1));
        newText = new Rectangle[7][0];
        newText[0] = new Rectangle[7];
    
        newText[0][0] = new Rectangle( 5,28,106-5,127-18);
        newText[0][1] = new Rectangle( 133,51,563-133,136-38);
        newText[0][2] = new Rectangle( 5,180,528-5,204-180);
        newText[0][3] = new Rectangle( 135,42,176-135,30-19);
        newText[0][4] = new Rectangle( 184,42,235-184,27-18);
        newText[0][5] = new Rectangle( 241,42,294-241,28-17);
        newText[0][6] = new Rectangle( 300,42,361-300,28-17);
       
     
        changeImage = new Rectangle[7][0];
        changeImage[0] = new Rectangle[6];
        changeImage[0][0]= new Rectangle(4,16,22-4,10);
        changeImage[0][1]= new Rectangle(24,16,47-24,10);
        changeImage[0][2]= new Rectangle(52,16,77-52,10);
        changeImage[0][3]= new Rectangle(83,16,130-83,10);
        changeImage[0][4]= new Rectangle(137,16,171-137,10);
        changeImage[0][5]= new Rectangle(372,42,401-371,11);
        changeImageNum = new int[7][0];
        changeImageNum[0] = new int[6];
        changeImageNum[0][0] = 1;
        changeImageNum[0][1] = 2;
        changeImageNum[0][2] = 3;
        changeImageNum[0][3] = 4;
        changeImageNum[0][4] = 5;
        changeImageNum[0][5] = 6;
     
        for( int i=1; i<7;i++)
          {
           newText[i] = new Rectangle[0];
         
           changeImage[i]= new Rectangle[1];
           changeImageNum[i]= new int[1];
           changeImageNum[i][0] = 0;
          
          }
        changeImage[1][0] = new Rectangle( 176,161,298-176,202-161);
        changeImage[2][0] = new Rectangle( 183,163,308-183,195-163);
        changeImage[3][0] = new Rectangle( 213,161,344-213,202-161);
        changeImage[4][0] = new Rectangle(172,161,332-172,211-161);
      
        changeImage[5][0] = new Rectangle( 366,159,495-366,204-159);
        changeImage[6][0] = new Rectangle( 329,166,476-329,213-166);
 

        }

     public void paint( Graphics g)
       {
        Image Im = A.getImage( mainImage);
   
        g.setColor( Color.white);
        g.fillRect(0,0,width,height);
     
        g.drawImage( Im, 0,0, this);
        setText();

       }

     float xscale=1.0f, 
           yscale=1.0f;
     public void setText()
       {
        StringBuffer sb = new StringBuffer( 600);
        if( mainImage == 0)
           if( (subImage < 0) ||(subImage>6))
             {
              sb.append("             ISAW Graphics User Interface\n\n");
              sb.append("This interface is used to access many features of the ISAW System.\n");
              sb.append("  It includes\n");
              sb.append("   1. A Menu bar at the top\n");
              sb.append("   2. A Data Set Tree at the left \n");
              sb.append("   3. A tabbed pane containing other information and features\n");
              sb.append("   4. A status pane at the bottom to report results or error conditions\n\n");
              sb.append( "CLICK on any of these areas for further descriptions");
           
              tx.setText( sb.toString());
             }
           else if( subImage == 0) //Tree part of the Isaw Main
             {
              sb.append("      Data Set Tree \n\n");
              sb.append(" This tree contains the list of active DataSets that can be viewed,\n");
              sb.append(" analyzed, reduced, saved, removed, etc.  These DataSets are under\n");
              sb.append(" the EXPERIMENT nodes. Under the DataSet nodes are the Data block\n");
              sb.append(" or spectra nodes.  The Data Blocks represent one detector element\n");
              sb.append(" or a Group of detector elements\n\n\n") ;
              sb.append("OPERATIONS: (left) clicking the mouse\n");
              sb.append("   In general this selects the experiment, DataSet, or DataBlock \n");
              sb.append("   for several operations.  On operations that applies to all types\n");
              sb.append("   is REMOVAL of the node and all its children. In addition\n\n");
              sb.append("   DataSet Node- is selected for viewing with the View Menu\n");
              sb.append("       option, for executing DataSet Operators when the \n");
              sb.append("       Operations menu is selected, and displays any attributes\n");
              sb.append("       of this data set in the Attribute tabbed pane.\n\n");
              sb.append("  Data Block Nodes- is selected for displaying attributes in\n");
              sb.append("       in the Attribute Pane and for selecting the spectra to\n");
              sb.append("       be displayed in some of the viewers.  Some operators \n");
              sb.append("       use the selected attribute and affect only those Data\n");
              sb.append("       Blocks\n\n\n");
              sb.append("OPERATIONS: right clicking with the mouse\n");
              sb.append("  A menu of options appears. Some of the options are\n");
              sb.append("  -select, clear, clear All apply to data blocks and relate to\n");
              sb.append("       the selecting of Data Blocks\n");
              sb.append("  -delete will delete the DataSet of Data Block.\n");
              sb.append("  -Operations and View for the DataSet parallel the Operations\n");
              sb.append("      the Menu items on the Menu bar of the IsawGUI.\n");
              sb.append("  -Send To for DataSets allow the DataSet to be sent to a different\n");
              sb.append("     EXPERIMENT( group of Data Sets\n");
  
              tx.setText( sb.toString());
             }
           else if( subImage ==1) //tab pane area
             {
              sb.append("     Tabbed Pane Display Area\n\n");
              sb.append(" This area is where each of the tabs display the associated \n");
              sb.append(" information or dialogs\n\n");
              sb.append(" CLICK ON ASSOCIATED TAB FOR MORE INFORMATION");
              tx.setText( sb.toString());
             }

           else if( subImage == 2)  //Status pane area
             {
              sb.append("        Status Pane\n\n");
              sb.append(" The Status Pane is used by various parts of the Isaw System\n");
              sb.append(" to display information like error conditions, comments on \n");
              sb.append(" some aspects of an operation, etc.  The Scripting language\n");
              sb.append(" has a command, DISPLAY, that displays non-data set information\n");
              sb.append(" to the Status Pane\n\n\n");
              sb.append("OPERATIONS:\n");
              sb.append("   Save- allows you to save the contents of the Status Pane to a file\n");
              sb.append("   Clear- Clears the Status Pane\n");
              sb.append("   The Status Pane is EDITABLE. That means comments can be added \n");
              sb.append("      anywhere and undesirable information can be deleted before\n");
              sb.append("      saving\n");
              tx.setText( sb.toString());

             }

           else if( subImage == 3)// Attribute tab
             {
              sb.append("         Attribute Tabbed Pane\n\n");
              sb.append(" This displays any displayable attribute of the 'selected' DataSet\n");
              sb.append(" or Data Block.  These can be selected in the DataSet Tree, in \n");
              sb.append(" in viewers, in scripts, etc.\n\n");
              sb.append(" Attributes store meta-data about a DataSet or Data Block.  Some \n");
              sb.append(" examples of attributes are the filename where the DataSet is stored,\n");
              sb.append(" the experimenter's name, the date the experiment was done, the \n");
              sb.append(" position of a detector group, etc.\n");
              tx.setText( sb.toString());
             }
           else if( subImage == 4)//Data SetLog Tab
             {
              sb.append("        DataSet Log Tabbed Pane\n\n");
              sb.append(" This pane shows all the operations that have changed a data set since\n");
              sb.append("  it was loaded\n");
              tx.setText( sb.toString());
             }
           else if( subImage == 5)//Session Log  tab
             {
               sb.append("     Session Log Tabbed Pane\n\n");
              sb.append(" This Pane keeps track of ALL operations done on the ISAW System since\n");
              sb.append(" it was started.  The format of this log is similar to the format for\n");
              sb.append(" the scripting language. This log can be saved to a file, but it is not\n");
              sb.append(" yet able to be run from the scripting systen\n");
              tx.setText( sb.toString());
             }
            else //if( subImage == 6) //System Properties tab
             {
              sb.append("       System Properties Tabbed Pane\n\n");
              sb.append(" This pane gives properties like build date, java version, operating\n");
              sb.append(" System, etc.  These properties are gotten from the Java System.\n");
              tx.setText( sb.toString());
             
             }
        else if( mainImage ==1)
          {
           sb.append("      File Menu Option \n\n");
           sb.append("The load Data has submenu options to load a local file, a Remote\n");
           sb.append("file or data from a live data server. See IsawProps.dat below to\n");
           sb.append("configure these options\n\n");
           sb.append("Load Scripts load and executes a Script(.iss) file.  These scripts do\n");
           sb.append("not need to be installed\n\n");
           sb.append("SaveAs saves a selected DataSet to a file.  Use the corresponding \n");
           sb.append("extensions from the file filter to determine the save format\n\n");
 
           sb.append("    IsawProps.dat setting for file I//O\n");
           sb.append("Data_Directory  stores the directory where the data is\n");
           sb.append("Instx_Name and Instx_Path (x=1,2,3 etc.) store the Menu Name and \n");
           sb.append("    IP address and port of a LiveDataServer\n");
           sb.append("IsawFileServerx_Name and IsawFileServerx_Path store Menu Name and locations\n");
           sb.append("   where a fileserver is able to retrieve DataSets(x=1,2,..)\n\n");
           sb.append("  CLICK BACK ");
  
           tx.setText( sb.toString());
        
          }
       else if( mainImage ==2)
          {
           sb.append("      Edit Menu Option \n\n");
           sb.append(" The Edit Menu has options to Remove Nodes in the DataSet Tree,\n");
           sb.append(" Edit and Set a few attributes for one Data Block or all Data \n");
           sb.append(" all Data Blocks.  Also the IsawProps.Dat file can be Editted and\n");
           sb.append(" applied so that the latest settings are used.\n\n");
           sb.append("  CLICK BACK ");
           tx.setText( sb.toString());
          }
        else if( mainImage == 3)
          {
           sb.append("      View Menu Option\n\n");
           sb.append(" This Menu option allows for a selection of a viewer for a \n");
           sb.append(" selected DataSet.\n\n");
           sb.append("  CLICK BACK ");
           tx.setText( sb.toString());
          }
        else if( mainImage ==4)
          {
           sb.append("      Operations Menu Option \n\n");
           sb.append(" The submenu options lead to all the DataSet operators belonging\n");
           sb.append(" to the selected DataSet in the DataSet Tree\n\n");
           sb.append(" A Parameters Dialog Box pops up to allow for the entry of needed\n");
           sb.append(" parameters for the operator\n\n");
           sb.append("  CLICK BACK ");
           tx.setText( sb.toString());
          }
        else if( mainImage == 5)
          {
           sb.append("                            Macro Menu Option \n\n");
           sb.append(" The submenu options lead to Generic Operators. These operators\n");
           sb.append(" do NOT belong to a DataSet.  Again a Parameters Dialog Box pops\n");
           sb.append(" up to input parameters for the selected operator\n\n");
           sb.append(" It is SIMPLE to add operators or scripts to this list.  Just\n");
           sb.append(" place the operator or script in one of several directories. The\n");
           sb.append(" directories are determined a follows:\n");
           sb.append(" The main directory, call it DIR, is one of the following:\n");
           sb.append("  1. ISAW_HOME in IsawProps.Dat\n");
           sb.append("  2. GROUP_HOME, GROUP1_HOME, GROUP2_HOME, etc. in IsawProps.dat\n");
           sb.append("  3. ISAW subdirectory of user.home directory\n");
           sb.append("  4. Any operator in a DataSetTools/operator/Generic directory or\n");
           sb.append("      any of its subdirectories.\n\n"); 
           sb.append(" The operator or script can go in the DIR/Operators or DIR/Scripts\n");
           sb.append(" directory or any of their subdirectories.\n\n");
 
           sb.append("  CLICK BACK ");
           tx.setText( sb.toString());
          }  
        else if( mainImage == 6)
          {
           sb.append("             Script/CommandPane\n\n");
           sb.append("The Command Pane consists of an Editor Window, an Immediate Window,\n");
           sb.append("  and a Bar of Buttons. Also output quite often is to the Status Pane\n\n");
           sb.append("Editor Window:\n");
           sb.append("  The Open button loads a file into the editable text area and the\n");
           sb.append("  Save button saves the contents of this area to a file.  The text \n");
           sb.append("  in this area can be created, deleted or changed. The Run button \n");
           sb.append("  executes the script in this area, \n\n");
           sb.append("Immediate Window:\n");
           sb.append("  This is where one line commands can be placed and executed by \n");
           sb.append("  pressing the return keyboard key on that line.  The variables\n");
           sb.append("  from Running the previous script are still active so extra\n");
           sb.append("  instructions from the previous script can also be entered here.\n");
           sb.append("  None of the structure commands (if-the, for, etc.) can be used here.\n\n");
           sb.append("Bar of Buttons\n");
           sb.append("  The Run, Open, and Save buttons apply only to the script in the Editor\n");
           sb.append("  window( See Editor Window above). The Help button gives extensive help\n");
           sb.append("  on the commands supported by this system\n\n");
           sb.append("The CommandPane uses the following keys from IsawProps.Dat\n");
           sb.append("  Help_Directory and ScriptPath\n\n");
          
           sb.append("  CLICK BACK ");

           tx.setText( sb.toString());
          }

    }//SetText


  public boolean mouseDown(Event evt,
                           int x,
                           int y)
    {
     for( int i = 0; i< newText[mainImage].length;i++)
        if( newText[mainImage][i].contains(x,y))
          {
           subImage = i;
           setText();
           return true;
          }
     
     for( int i = 0; i< changeImage[mainImage].length;i++)
        if( changeImage[mainImage][i].contains(x,y))
          {
           subImage = -1;
           mainImage = changeImageNum[mainImage][i];
           this.repaint();
           return true;
          }

     return false;
    }



  }//MyPanel


  public static void main( String args[])
    {
     System.out.println("here i am");
    }
}
