package SonicGBA;

import Lib.MyAPI;

public class HobinCal implements SonicDef {
   private static final int MAX_DISTANCE = 512;
   private static final int MAX_POWER = 800;
   private int degree;
   private int distance;
   private int power;
   private int timeCount;

   public int getPosOffsetX() {
      return this.distance * MyAPI.dCos(this.degree) / 100;
   }

   public int getPosOffsetY() {
      return this.distance * MyAPI.dSin(this.degree) / 100;
   }

   public boolean isStop() {
      boolean var1;
      if (this.timeCount == 0) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   public void logic() {
      if (this.timeCount > 0) {
         --this.timeCount;
      }

      if (this.timeCount > 0) {
         if (this.timeCount == 9 * Lib.FPS.SCALE) {
            this.distance = this.power;
         } else if (this.timeCount % Lib.FPS.SCALE == 0) {
            // Project 60fps: затухание -- раз в SCALE тиков, иначе колебание гаснет вчетверо быстрее
            this.distance = -this.distance >> 1;
         }

         if (this.timeCount == 1 * Lib.FPS.SCALE) {
            this.distance = 0;
            this.power = 0;
         }
      }

   }

   public void startHobin(int var1, int var2, int var3) {
      for(this.power = 1200; var2 < 0; var2 += 360) {
      }

      this.degree = var2 % 360;
      this.timeCount = 10 * Lib.FPS.SCALE; // Project 60fps
   }
}
