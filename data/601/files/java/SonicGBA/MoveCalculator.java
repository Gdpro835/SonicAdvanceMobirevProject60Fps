package SonicGBA;

import Lib.MyAPI;

class MoveCalculator {
   private static final int DEGREE_VELOCITY = 5;
   private static final int HALF_MOVE_TIME = 17;
   private static final int MOVE_TIME = 34;
   private static int degree;
   public static boolean direction;
   private static boolean isSide = false;
   private static int moveCount;
   private static int moveCount2;
   // Project 60fps: накопитель дробной части шага угла (5 за исходный кадр).
   private static int fpsRemDegree;
   private int centerPosition;
   private int position;
   private int radius;
   private boolean ratio;

   public MoveCalculator(int var1, int var2, boolean var3) {
      this.centerPosition = var1;
      this.radius = var2;
      this.ratio = var3;
      this.position = var1;
      isSide = false;
   }

   public static void staticLogic() {
      if (direction) {
         isSide = false;
         ++moveCount2;
         // Project 60fps: длительность хода растянута в SCALE раз.
         if (moveCount2 > 34 * Lib.FPS.SCALE) {
            moveCount2 = 34 * Lib.FPS.SCALE;
            direction = false;
            isSide = true;
         }
      } else {
         isSide = false;
         --moveCount2;
         if (moveCount2 < 0) {
            moveCount2 = 0;
            direction = true;
            isSide = true;
         }
      }

      moveCount = moveCount2 - 17 * Lib.FPS.SCALE;
      // Project 60fps: 5 градусов за исходный кадр раздаём по тикам.
      fpsRemDegree += 5;
      degree += fpsRemDegree >> Lib.FPS.SHIFT;
      fpsRemDegree -= fpsRemDegree >> Lib.FPS.SHIFT << Lib.FPS.SHIFT;
      degree %= 360;
   }

   public int getPosition() {
      int var2 = this.centerPosition;
      int var1 = MyAPI.dSin(degree) * this.radius / 100;
      return var2 + var1;
   }

   public boolean getSide() {
      boolean var1;
      if (degree != 90 && degree != 270) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   public void logic() {
   }
}
