//import javax.swing.*;
//import javax.swing.event.*;
package IsawGUI;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.IOException;

public class HTMLPage extends JFrame {
	private JEditorPane editorPane = new JEditorPane();

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
		      	try { 
					editorPane.setPage(e.getURL());
				}
				catch(IOException ex) { ex.printStackTrace(); }
}
			}
		});
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
