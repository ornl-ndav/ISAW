package  DataSetTools.dataset;





import java.io.*;





public interface IAttributeList 

{



  /**

   *  Get a copy of the list of attributes for an object.

   */

  public AttributeList getAttributeList();



  /**

   *  Set the list of attributes for an object to be a COPY of the 

   *  specified list of attributes.

   */

  public void setAttributeList( AttributeList attr_list );



} 



