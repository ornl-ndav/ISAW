package DataSetTools.components.ui;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.File.TextSeparators;
import gov.anl.ipns.Util.Sys.Browser;
import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.ViewTools.Panels.ThreeD.IThreeD_Object;

import java.awt.event.*;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.Arrays;




public class MouseListener3D implements iMouse_MotionListener
{

    WeakReference<IMotion3D> Display;
    float[] center;
    Point PrevPoint;
    float scale;
    boolean first;
    // ----- drag  extended modifier masks ------
    public int ROTATE = InputEvent.BUTTON1_DOWN_MASK+
                        InputEvent.CTRL_DOWN_MASK;
    public int ZOOM =  InputEvent.BUTTON1_DOWN_MASK+
                         InputEvent.SHIFT_DOWN_MASK;
    public int CENTER_MOVE = InputEvent.BUTTON3_DOWN_MASK+
                          InputEvent.ALT_DOWN_MASK;
    public int SPEED = InputEvent.BUTTON3_DOWN_MASK;
    
    //------------click extended modifier masks ----
    public int SELECT =  InputEvent.BUTTON1_DOWN_MASK;
    public int CENTER = InputEvent.BUTTON1_DOWN_MASK+
                        InputEvent.ALT_DOWN_MASK;
    
   public MouseListener3D( IMotion3D display)
   {
      this.Display = new WeakReference<IMotion3D>(display);
      center = new float[3];
      java.util.Arrays.fill( center , 0f );
      PrevPoint = null;
      scale = 1;
      first = false;
   }
   @Override
   public void mouseClicked( MouseEvent arg0 )
   {
     int button = arg0.getButton();
     boolean ctrlDown = arg0.isControlDown();
     boolean altDown   =arg0.isAltDown();
     boolean shiftDown = arg0.isShiftDown();
     if( button== MouseEvent.BUTTON1 )
     {
        if( !altDown && !ctrlDown && !shiftDown)
           SelectObject( arg0.getPoint());
        else if( altDown && !ctrlDown && !shiftDown )
           NewCenter(  arg0.getPoint());
     }else if( button == MouseEvent.BUTTON3)
     {
        //show help
        String S = getHelp( true);
        Browser B = new Browser( S);
        
        
     }
       

   }


   @Override
   public void mouseEntered( MouseEvent arg0 )
   {

   

   }


   @Override
   public void mouseExited( MouseEvent arg0 )
   {

   }


   @Override
   public void mousePressed( MouseEvent arg0 )
   {
     
     PrevPoint = arg0.getPoint();
     first = true;
   }


   @Override
   public void mouseReleased( MouseEvent arg0 )
   {
      PrevPoint = null;
      first = false;
   }


   @Override
   public void mouseDragged( MouseEvent arg0 )
   {

      boolean button1, button2 ,button3;
      int mask = arg0.getModifiersEx();
      button1 = (mask & InputEvent.BUTTON1_DOWN_MASK) >0;
      button2 = (mask & InputEvent.BUTTON2_DOWN_MASK) >0;
      button3 = (mask & InputEvent.BUTTON3_DOWN_MASK) >0;
      
      boolean ctrlDown = arg0.isControlDown();
      boolean altDown   =arg0.isAltDown();
      boolean shiftDown = arg0.isShiftDown();
      if(mask == ROTATE)//button1  && ctrlDown && !altDown && !shiftDown)
         Rotate( arg0.getPoint());
      else if(mask ==ZOOM)// button1 && shiftDown && !ctrlDown && !altDown)
         Zoom( arg0.getPoint());
      else if( mask == CENTER_MOVE)//button3 &&  altDown &&!shiftDown)
         MoveCenter( arg0.getPoint(), ctrlDown );
      else if( mask == SPEED)//button3  && !shiftDown &&!ctrlDown && !altDown)
         ChangeSpeed( arg0.getPoint());
      
      PrevPoint = arg0.getPoint();
   }

   public String getHelp( boolean html)
   {
      String textType = "html";
      if(!html)
         textType = "plain";
      TextSeparators u = new TextSeparators(textType);
      
      String Res = u.start();
      Res += "Below are the mouse, alt,shift,and control settings for certain operations";
      Res +=u.eol();
      Res +=u.tableBorder1();
      Res += u.bld()+u.row()+"Mouse Button "+u.col()+"Ctrl"+u.col()+"Shft"+u.col()+"Alt"+ u.col()+"Operation"+u.rowEnd()+
             u.bldEnd();
      Res+=u.row()+" Left(click)"+u.col()+"    "+u.col()+"    "+u.col()+"   "+u.col()+"Select Object"+u.rowEnd();

      Res+=u.row()+" Left(click)"+u.col()+"    "+u.col()+"    "+u.col()+" X "+u.col()+"Select New Center"+u.rowEnd();
      Res+=u.row()+" Left(drag )"+u.col()+" X  "+u.col()+"    "+u.col()+"   "+u.col()+"Rotate display around center"+u.rowEnd();
      Res+=u.row()+" Left(drag )"+u.col()+"    "+u.col()+" X  "+u.col()+"   "+u.col()+"Zoom in to Center"+u.rowEnd();
      Res+=u.row()+"Right(drag )"+u.col()+" ?  "+u.col()+"    "+u.col()+" X "+u.col()+"Move Center**"+u.rowEnd();
      Res+=u.row()+"Right(drag) "+u.col()+"    "+u.col()+"    "+u.col()+"  "+u.col()+"Incr(up)/decr(down) speed***"+u.rowEnd();
      Res +=u.tableEnd()+u.eol();
      Res +="** Moves in x direction and up if control up or in/out if control is down"+u.eol();
      Res +="*** Moving sideways resets to the initial speed";
      return Res;
      
   }
   
   @Override
   public void mouseMoved( MouseEvent arg0 )
   {

  

   }
   

   private void SelectObject( Point P)
   {
      IMotion3D display = Display.get();
      if( display == null)
         return;
      int id = display.pickID( P.x , P.y , 6 );
      display.SetSelectID( id );
      
   }
   
   private void NewCenter( Point P)
   {
      IMotion3D display = Display.get();
      if( display == null)
         return;
      int id = display.pickID( P.x , P.y , 6 );
      int z=0;
      if( id >=0)
      {
        z= display.getSelPixZ( P.x , P.y );
         
      }
      center[0] = P.x;
      center[1] = P.y;
      center[2] = z;
      display.setCenter( P.x, P.y, z , false);
     
   }
   

   public void  Rotate( Point P)
   {
      if( PrevPoint == null)
      { PrevPoint = P;
        return;
      }
      IMotion3D display = Display.get();
      if( display == null)
         return;
     float dx = P.x -PrevPoint.x;
     float dy = P.y -PrevPoint.y;
      
     display.setRotate( scale*dx , scale*dy );

     PrevPoint = P;
      
   }
   
      public void  Zoom( Point P)
      {
         if( PrevPoint == null)
            PrevPoint = P;
         IMotion3D display = Display.get();
         if( display == null)
            return;
         
         float up = scale*(P.y-PrevPoint.y);
         
         if( up < 0)
            display.setZoomRelative( 1f-Math.min( .8f,-.1f*up/5 ));
         else if( up > 0)
            display.setZoomRelative( 1f + Math.min( .1f*up/5, .8f ) );
   
         PrevPoint = P;
         
         
      }
      public void  MoveCenter(Point P, boolean useYvsZ)
      {
         IMotion3D display = Display.get();
         if( display == null)
            return;
         
         if( PrevPoint == null)
         { PrevPoint = P;
            return;
         }

         float upy = scale*(P.y-PrevPoint.y);
         float upx = scale*(P.x-PrevPoint.x);
         float upz =0;
         if(useYvsZ)
         {

            upz=upy;
            upy=0;
         }
         
         center[0]+= upx;
         center[1] +=upy;
         center[2] +=upz;
         display.setCenter( upx,upy,upz, true);

         PrevPoint = P;
         
      }
      
      public void ChangeSpeed( Point P)
      {
         if( !first )
            return;
         
         if( PrevPoint == null)
         { PrevPoint = P;
           return;
         }
         first = false;
         float up = -(P.y-PrevPoint.y);
         if( Math.abs( P.x-PrevPoint.x )> Math.abs( up ))
            scale = 1;
         else if( up > 0)
            scale *=1.2f;
         else if( up < 0)
            scale *=.85f;
         else
            scale = 1f;
         
         PrevPoint = P;
      }


}
