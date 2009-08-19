package EventTools.ShowEventsApp.Command;

public class DisplaySliceCmd
{
   public enum moveSlice { X, Y, Z };
   
   private boolean   showImageX;
   private boolean   showImageY;
   private boolean   showImageZ;
   private boolean   showSliceX;
   private boolean   showSliceY;
   private boolean   showSliceZ;
   private moveSlice move;
   private int       sliceNumber;
   
   public DisplaySliceCmd(boolean   showImageX,
                          boolean   showImageY,
                          boolean   showImageZ,
                          boolean   showSliceX,
                          boolean   showSliceY,
                          boolean   showSliceZ,
                          moveSlice move,
                          int       sliceNumber)
   {
      this.showImageX = showImageX;
      this.showImageY = showImageY;
      this.showImageZ = showImageZ;
      this.showSliceX = showSliceX;
      this.showSliceY = showSliceY;
      this.showSliceZ = showSliceZ;
      this.move =  move;
      this.sliceNumber = sliceNumber;
   }
   
   public boolean isShowImageX()
   {
      return showImageX;
   }

   public boolean isShowImageY()
   {
      return showImageY;
   }

   public boolean isShowImageZ()
   {
      return showImageZ;
   }

   public boolean isShowSliceX()
   {
      return showSliceX;
   }

   public boolean isShowSliceY()
   {
      return showSliceY;
   }

   public boolean isShowSliceZ()
   {
      return showSliceZ;
   }

   public moveSlice getMoveSlice()
   {
      return move;
   }

   public int getSliceNumber()
   {
      return sliceNumber;
   }
   
   public String toString()
   {
      return "\nShow Image X : " + isShowImageX() +
             "\nShow Image Y : " + isShowImageY() +
             "\nShow Image Z : " + isShowImageZ() + 
             "\nShow Slice X : " + isShowSliceX() + 
             "\nShow Slice Y : " + isShowSliceY() + 
             "\nShow Slice Z : " + isShowSliceZ() + 
             "\nMove Slice   : " + getMoveSlice() + 
             "\nSlice Number : " + getSliceNumber();
   }
}
