/*
 * Created on May 26, 2004
 * Authors: Ruth Mikkelson and Joshua Robertson
 * Operator PrinterNamePG finds all the available printers and allows you to choose What printer you 
 * would like to print with.
 */
package DataSetTools.parameter;
import javax.print.*;
import javax.print.attribute.*;




/**
 * @author robertson
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PrinterNamePG extends ChooserPG {
  public static String TYPE = "PrinterName";
  protected static int DEF_COLS = ChooserPG.DEF_COLS;

  public PrinterNamePG(String name, Object val){
	 super(name, val);
	 this.setType(TYPE);
	 if (val != null)
	   setValue(val.toString()); 
	 HashPrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
	 DocFlavor myFormat = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
	 PrintService[] services =
 		PrintServiceLookup.lookupPrintServices(myFormat, aset); 
	 for (int i = 0; i < services.length; i++){
 		super.addItem(services[i].getName());
	 }
	
  }
  public Object getValue(){
    String S = (String)super.getValue();
    if( S == null)
       return"";
    return S;
  }

	
}










	
	

