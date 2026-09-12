package SonicGBA;

import Lib.Animation;
import Lib.AnimationDrawer;
import Lib.MyAPI;
import com.sega.mobile.framework.device.MFGraphics;
import com.sega.mobile.framework.device.MFImage;

class FrontManagerStage2_1 extends BackGroundManager {
   private static final int LIGHT_CIRCLE_1 = 32;
   private static final int LIGHT_CIRCLE_2 = 120;
   private static final int LIGHT_DEGREE_CENTER_1 = -90;
   private static final int LIGHT_DEGREE_CENTER_2 = -90;
   private static final int LIGHT_RANGE_1 = 40;
   private static final int LIGHT_RANGE_2 = 68;
   private int degree1;
   private int degree2;
   // Project 60fps: остатки деления угловых скоростей прожекторов на SCALE.
   private int fpsRemDegree1;
   private int fpsRemDegree2;
   private AnimationDrawer drawer;
   private MFImage screenMask;
   private int[] screenMaskRGB;

   public FrontManagerStage2_1() {
      this.screenMask = MFImage.createImage(SCREEN_WIDTH, SCREEN_HEIGHT);
      this.drawer = Animation.getInstanceFromQi("/animation/searchlight.dat")[0].getDrawer();
      this.degree1 = 0;
      this.degree2 = 0;
      this.screenMaskRGB = new int[SCREEN_WIDTH * SCREEN_HEIGHT];
   }

   public void close() {
      this.screenMask = null;
      this.drawer = null;
      this.screenMaskRGB = null;
   }

   public void draw(MFGraphics var1) {
      int var2 = MapManager.getCamera().x;
      if (var2 <= 1456) {
         if (!GameObject.IsGamePause) {
            // Project 60fps: 11 и 3 градуса за исходный кадр, остаток переносим.
            this.fpsRemDegree1 += 11;
            this.degree1 += this.fpsRemDegree1 >> Lib.FPS.SHIFT;
            this.fpsRemDegree1 -= this.fpsRemDegree1 >> Lib.FPS.SHIFT << Lib.FPS.SHIFT;
            this.degree1 %= 360;
            this.fpsRemDegree2 += 3;
            this.degree2 += this.fpsRemDegree2 >> Lib.FPS.SHIFT;
            this.fpsRemDegree2 -= this.fpsRemDegree2 >> Lib.FPS.SHIFT << Lib.FPS.SHIFT;
            this.degree2 %= 360;
         }

         MFGraphics var3 = this.screenMask.getGraphics();
         var3.setColor(16777215);
         MyAPI.fillRect(var3, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
         var3.saveCanvas();
         var3.translateCanvas((SCREEN_WIDTH >> 1) - 60, (SCREEN_HEIGHT >> 1) + 119);
         var3.rotateCanvas((float)(MyAPI.dSin(this.degree1) * 40 / 100 + 0));
         this.drawer.draw(var3, 0, 0, 0, false, 0);
         var3.restoreCanvas();
         var3.saveCanvas();
         var3.translateCanvas((SCREEN_WIDTH >> 1) + 80, (SCREEN_HEIGHT >> 1) + 161);
         var3.rotateCanvas((float)(MyAPI.dSin(this.degree2) * 68 / 100 + 0));
         this.drawer.draw(var3, 1, 0, 0, false, 0);
         var3.restoreCanvas();
         this.screenMask.getRGB(this.screenMaskRGB, 0, SCREEN_WIDTH, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

         for(var2 = 0; var2 < this.screenMaskRGB.length; ++var2) {
            if (this.screenMaskRGB[var2] == -1) {
               this.screenMaskRGB[var2] = Integer.MIN_VALUE;
            } else {
               this.screenMaskRGB[var2] = 0;
            }
         }

         var1.drawRGB(this.screenMaskRGB, 0, SCREEN_WIDTH, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, true);
      }

   }
}
