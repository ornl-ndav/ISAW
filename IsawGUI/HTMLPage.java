//import javax.swing.*;
//import javax.swing.event.*;
package IsawGUI;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.IOException;
import java.net.*;
import Command.*;
import DataSetTools.operator.*;
public class HTMLPage extends JFrame {
	private JEditorPane editorPane = new JEditorPane();
        String OperatorPane = null;
	public HTMLPage(String url)
	{
		Container contentPane = getContentPane();
		

		try { 
			editorPane.setPage(url);
		}
		catch(IOException ex) { ex.printStackTrace(); }

		contentPane.add(new JScrollPane(editorPane), 
						BorderLayout.CENTER);

		editorPane.setEditable(false);

		editorPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
                          {
                                         try { URL U= e.getURL();
                                        if( U.getFile().indexOf("XX$netscape") >=0 )
                                           {BrowserControl BC = new BrowserControl();
                                            String Fname = U.getFile();
                                            Fname= Fname.replace('\\', '/');
                                            int k = Fname.lastIndexOf(
                                                    "IsawHelp/Command/XX$netscape");
                                            if( k < 0) return;
                                            Fname = Fname.substring( 0,k);
                                            Fname += "docs/DataSetTools/operator/Operator.html";
                                            //U = new URL( "file://"+Fname);
                                            System.out.println("URL=file://"+Fname);
                                            BC.displayURL( "file://"+Fname);
                                            return;
                                           }
                                        else if( U.getFile().indexOf("XX$Panel1") >= 0 )
                                           { SetText( editorPane );
                                           }
                                         else if( U.getFile().indexOf("XX$Panel2") >= 0 )
                                           { 
                                             SetText( editorPane , U.getRef() );
                                           }
					else 
                                             editorPane.setPage(e.getURL());
				}
			catch(IOException ex) { ex.printStackTrace(); }
}
			}
		});
	}
  private void SetText( JEditorPane JP, String ref)
    {Script_Class_List_Handler SH = new Script_Class_List_Handler();
     String Textt="<HTML><BODY><A href=\"XX$Panel1\">BACK</a><P>";
     Textt +=" <Center><H1>Operator</H1></Center><P>";
    int k;
     try{
      k = new Integer( ref).intValue();
       }
     catch(Exception s){return;}
     Operator O = SH.getOperator( k );
     if( O instanceof ScriptOperator)
       Textt += "ScriptOperator from file <B>"+ ((ScriptOperator)O).getFileName()+"</B><BR>";
     else
       Textt += "Java Operator. Class="+ O.getClass().toString() +"<BR>";
     Textt += "Title( in Menu's etc.) ="+ O.getTitle()+"<BR>";
     Textt += "Command(in commandPane) =" + O.getCommand()+"<P><P>";
     Textt += "<Center><H3> Object "+ O.getCommand()+"(";
     int n = SH.getNumParameters( k );
     for( int i = 0; i< n ; i++ )
       {Object XX = SH.getOperatorParameter( k , i );
        if( XX == null)
           Textt += "Object ";
        else
          Textt += XX.getClass().toString();
        if( i < n-1)
          Textt += " , ";      
      
       }
      Textt +=" ) ";
     Textt +="</h3></Center></body></html>";
    
     JP.setText( Textt );
    }
  private void setUpOpPanel()
    {Script_Class_List_Handler SH = new Script_Class_List_Handler();
     String Textt="<HTML><BODY><A href=\"Commands.html\">BACK</a><P>";
     Textt +=" <Center><H1>Current Operators</H1></Center>";
     int n = SH.getNum_operators();
     int nrows = n / 4 ;
     int ncols = 4;
     if( nrows >15 )
        {nrows = 15;
         ncols = n / nrows;
         if( nrows*ncols < n) ncols++;
        }
    else if( nrows <5 )
      {  nrows = 5;
         ncols = n / nrows;
         if( nrows*ncols < n) ncols++;       
      }
    Textt += "<table> <tr><td>";
    for( int i = 0; i < n; i++ )
     {Operator O = SH.getOperator( i );
      String command = O.getCommand();
      Textt += "<A href=\"XX$Panel2#"+i+"\">"+command+"</a><BR>";
      if( nrows*((i+1)/nrows) == i +1)
       {
        Textt += "</TD>";
        if( i < n-1)
          Textt+= "<TD>";
       }
     }
     if( nrows*n/nrows !=n )
        Textt +="</TD>";
     Textt += "</TR></table></body></html>";
     OperatorPane = Textt;
     }
    private void SetText( JEditorPane JP )
    {if( OperatorPane == null)
        setUpOpPanel();   
     JP.setText(OperatorPane);
     
    }
	public static void main(String args[]) {
		GJApp.launch(new HTMLPage("http://www.pns.anl.gov/gppd/index.htm"), 
					"JEditorPane",300,300,450,300);
	}
}
class GJApp extends WindowAdapter {
	static private JPanel statusArea = new JPanel();
	static private JLabel status = new JLabel(" ");
	static private ResourceBundle resources;

	public static void launch(final JFrame f, String title,
							  final int x, final int y, 
							  final int w, int h) {
		launch(f,title,x,y,w,h,null);	
	}
	public static void launch(final JFrame f, String title,
							  final int x, final int y, 
							  final int w, int h,
							  String propertiesFilename) {
		f.setTitle(title);
		f.setBounds(x,y,w,h);
		f.setVisible(true);

		statusArea.setBorder(BorderFactory.createEtchedBorder());
		statusArea.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		statusArea.add(status);
		status.setHorizontalAlignment(JLabel.LEFT);

		f.setDefaultCloseOperation(
							WindowConstants.DISPOSE_ON_CLOSE);

		if(propertiesFilename != null) {
			resources = ResourceBundle.getBundle(
						propertiesFilename, Locale.getDefault());
		}

		f.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
	}
	static public JPanel getStatusArea() {
		return statusArea;
	}
	static public void showStatus(String s) {
		status.setText(s);
	}
	static Object getResource(String key) {
		if(resources != null) {
			return resources.getString(key);
		}
		return null;
	}
      
 
}












