package SonicGBA;

import Lib.Animation;
import Lib.AnimationDrawer;
import Lib.MyAPI;
import Lib.SoundSystem;
import com.sega.mobile.framework.device.MFGraphics;
import com.sega.mobile.framework.utility.MFMath;

class BossF1 extends BossObject {
   private static final int Accy1 = 96;
   private static final int BALL_ACC_ACC = 22;
   private static final int BALL_ACC_ACC2 = 17;
   private static final int BALL_ACC_MAX;
   private static final int BALL_BOTTOM_Y = 48064;
   private static final int BALL_RANGE = 2048;
   private static final int BALL_RASIUS = 4992;
   private static final int BALL_START_Y = 42240;
   private static final int BOSS_1_STOP_POSY = 39936;
   private static final int BOSS_2_STOP_POSY = 43264;
   private static final int BOSS_3_STOP_POSX = 55296;
   private static final int BOSS_3_STOP_POSY = 43008;
   private static final int BOSS_LEFT_POSX = 53760;
   private static final int BOSS_MOVE_1_POSX = 54528;
   private static final int BOSS_MOVE_1_POSY = 42880;
   private static final int BOSS_MOVE_2_POSY = 43136;
   private static final int BOSS_RIGHT_POSX = 56832;
   private static final int BOSS_START_POSX = 59392;
   private static final int BOSS_START_POSY = 33920;
   private static final int BROKEN_OFFSET_Y = 1280;
   private static final int BROKEN_STOP_POSY = 46336;
   private static final int COLLISION_HEIGHT = 2560;
   private static final int COLLISION_HEIGHT_OFFSET = -384;
   private static final int COLLISION_WIDTH = 3008;
   private static final int END_POSX = 117120;
   private static final int ENTER_SCREEN_FRAME_MAX = 18;
   private static final int ENTER_SPEED_1_Y = 270;
   private static final int ENTER_SPEED_3_X = -240;
   private static final int ESCAPE_OUT_POSX = 65536;
   private static final int ESCAPE_SPEED_X = 480;
   private static final int ESCAPE_STOP_POSY = 45056;
   private static final int FACE_BROKEN = 3;
   private static final int FACE_HURT = 2;
   private static final int FACE_NORMAL = 0;
   private static final int FACE_OFFSET_X = 192;
   private static final int FACE_OFFSET_Y = -1920;
   private static final int FACE_SMILE = 1;
   private static final int INIT_STOP_POSX = 55296;
   private static final int INIT_STOP_POSY = 43008;
   private static final int LAUGH_TIME = 10;
   private static final int MACHINE_ESCAPE_MOVE = 3;
   private static final int MACHINE_ESCAPE_WAIT = 2;
   private static final int MACHINE_MOVE = 1;
   private static final int MACHINE_MOVE_HURT = 5;
   private static final int MACHINE_WAIT = 0;
   private static final int MACHINE_WAIT_HURT = 4;
   private static final int PRO_STEP_BOTTOM_2_LEFT = 0;
   private static final int PRO_STEP_BOTTOM_2_RIGHT = 2;
   private static final int PRO_STEP_LEFT_2_BOTTOM = 1;
   private static final int PRO_STEP_RIGHT_2_BOTTOM = 3;
   private static final int RING_RANGE = 896;
   private static final int SHOW_BOSS_END = 4;
   private static final int SHOW_BOSS_ENTER_1 = 0;
   private static final int SHOW_BOSS_ENTER_2 = 1;
   private static final int SHOW_BOSS_ENTER_3 = 2;
   private static final int SHOW_BOSS_GOTO_PRO = 5;
   private static final int SHOW_BOSS_LAUGH = 3;
   private static final int SIDE_DOWN1 = 784;
   private static final int SIDE_DOWN2 = 784;
   private static final int SIDE_LEFT1 = 864;
   private static final int SIDE_RIGHT1 = 864;
   private static final int SIDE_UP0 = 544;
   private static final int SIDE_UP1 = 624;
   private static final int SIDE_UP2 = 544;
   private static final int STATE_BROKEN = 3;
   private static final int STATE_ENTER_SHOW = 1;
   private static final int STATE_ESCAPE = 4;
   private static final int STATE_INIT = 0;
   private static final int STATE_PRO = 2;
   private static Animation ballAni;
   private static final int cnt_max = 8;
   private static Animation faceAni;
   private static Animation machineAni;
   private int Accy = 0;
   private int RADIUS = 4992;
   private BossF1Ball ball;
   private AnimationDrawer[] ballDrawer;
   private int[][] ballPos;
   private int[] ballVel;
   private int ballvely;
   private BossBroken bossbroken;
   private int degree;
   private boolean directTrans = false;
   private boolean displayFlag;
   private int drop_cnt;
   private int drop_vely;
   private int enter_screen_frame_cn;
   private AnimationDrawer faceDrawer;
   private int face_cnt;
   private int face_state;
   private int frameCn;
   private boolean isDisplayBall = false;
   private int laugh_cn;
   private int lineVelocity;
   // Project 60fps: остатки покадрового шага маятника и падения шара.
   private int fpsRemLineVel;
   private int fpsRemDegree;
   private int fpsRemBallPos;
   private int fpsRemDropVel;

   private static final int[] SWING_Y_TABLE = new int[]{
      4992, 4906, 4743, 4511, 4219, 3875, 3488, 3066, 2618, 2152, 1677, 1201, 733, 281, -146, -540, -892, -1194
   };

   private static final int[] ENTER_SWING_Y_TABLE = new int[]{
      4992, 4950, 4823, 4618, 4341, 4000, 3609, 3179, 2726, 2266, 1813, 1383, 992, 651, 374, 169, 42, 0
   };

   /** Project 60fps: шаг падающего шара за тик с переносом остатка. */
   private int fpsDropStepY(int perFrameAmount) {
      this.fpsRemDropVel += perFrameAmount;
      int applied = this.fpsRemDropVel >> Lib.FPS.SHIFT;
      this.fpsRemDropVel -= applied << Lib.FPS.SHIFT;
      return applied;
   }

   /** Project 60fps: интерполяция Y координаты маятника при подъеме вверх (4992 -> -1194). */
   private int getSwingYUp(int tick) {
      if (tick <= 0) return 4992;
      int f = tick >> Lib.FPS.SHIFT;
      int rem = tick & (Lib.FPS.SCALE - 1);
      if (f >= 17) return -1194;
      return (SWING_Y_TABLE[f] * (Lib.FPS.SCALE - rem) + SWING_Y_TABLE[f + 1] * rem) >> Lib.FPS.SHIFT;
   }

   /** Project 60fps: симметричная интерполяция Y координаты маятника при спуске вниз (-1194 -> 4992). */
   private int getSwingYDown(int tick) {
      return 3798 - getSwingYUp(tick);
   }

   /** Project 60fps: интерполяция Y координаты маятника при появлении босса (4992 -> 0). */
   private int getEnterSwingY(int tick) {
      if (tick <= 0) return 4992;
      int f = tick >> Lib.FPS.SHIFT;
      int rem = tick & (Lib.FPS.SCALE - 1);
      if (f >= 17) return 0;
      return (ENTER_SWING_Y_TABLE[f] * (Lib.FPS.SCALE - rem) + ENTER_SWING_Y_TABLE[f + 1] * rem) >> Lib.FPS.SHIFT;
   }
   private AnimationDrawer machineDrawer;
   private int machine_state;
   private int oppoBallPosX;
   private int oppoBallPosY;
   private int pro_step;
   private int show_step;
   private int state;
   private int velocity;
   private int vely;

   static {
      BALL_ACC_MAX = ORIGINAL_GRAVITY; // Project 60fps: база для покадровых Accy
   }

   protected BossF1(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      super(var1, var2, var3, var4, var5, var6, var7);
      this.posX -= this.iLeft * 8;
      this.posY -= this.iTop * 8;
      this.posX = 59392;
      this.posY = 33920;
      if (machineAni == null) {
         machineAni = new Animation("/animation/bossf1_machine");
      }

      this.machineDrawer = machineAni.getDrawer(0, true, 0);
      if (faceAni == null) {
         faceAni = new Animation("/animation/bossf2_face");
      }

      this.faceDrawer = faceAni.getDrawer(0, true, 0);
      if (ballAni == null) {
         ballAni = new Animation("/animation/bossf1_ring_ball");
      }

      this.ballDrawer = new AnimationDrawer[6];

      for(var4 = 0; var4 < 4; ++var4) {
         this.ballDrawer[var4] = ballAni.getDrawer(0, true, 0);
      }

      this.ballDrawer[4] = ballAni.getDrawer(1, true, 0);
      this.ballDrawer[5] = ballAni.getDrawer(2, true, 0);
      this.ballPos = new int[6][2];

      for(var4 = 0; var4 < 6; ++var4) {
         this.ballPos[var4][0] = 55296;
         this.ballPos[var4][1] = 42240;
      }

      this.ballVel = new int[6];
      this.isDisplayBall = false;
      this.directTrans = false;
      this.ball = new BossF1Ball(var1, var2, var3, 0, 0, 0, 0);
      addGameObject(this.ball, this.posX >> 6, this.posY >> 6);
      this.displayFlag = false;
      this.state = 0;
      this.HP = 4;
   }

   private void bossStateChange() {
      if (this.ball != null && this.ball.getPlayerHurt()) {
         this.face_state = 1;
         this.ball.resetPlayerHurt();
         this.face_cnt = 0;
      }

      if (this.face_state != 0) {
         if (this.face_cnt < 8 * Lib.FPS.SCALE) {
            ++this.face_cnt;
         } else {
            if (this.machine_state == 4) {
               this.machine_state = 0;
            }

            if (this.machine_state == 5) {
               this.machine_state = 1;
            }

            this.face_state = 0;
            this.face_cnt = 0;
         }
      }

      this.changeAniState(this.machineDrawer, this.machine_state);
      this.changeAniState(this.faceDrawer, this.face_state);
   }

   private void changeAniState(AnimationDrawer var1, int var2) {
      if (this.velocity > 0) {
         var1.setActionId(var2);
         var1.setTrans(2);
         var1.setLoop(true);
      } else {
         var1.setActionId(var2);
         var1.setTrans(0);
         var1.setLoop(true);
      }

   }

   private void degreeCal() {
      // Project 60fps: 60 -- исходная покадровая константа, поэтому здесь нужна
      // ORIGINAL_GRAVITY; результат дробим на тики с накоплением остатка.
      this.fpsRemLineVel += (ORIGINAL_GRAVITY - 60) * MyAPI.dSin((this.degree >> 6) - 90) / 100;
      int var1 = this.fpsRemLineVel >> Lib.FPS.SHIFT;
      this.fpsRemLineVel -= var1 << Lib.FPS.SHIFT;
      this.lineVelocity += var1;
      this.fpsRemDegree += ((this.lineVelocity << 6) / this.RADIUS << 6) * 180 / 201;
      var1 = this.fpsRemDegree >> Lib.FPS.SHIFT;
      this.fpsRemDegree -= var1 << Lib.FPS.SHIFT;
      this.degree -= var1;
   }

   public static void releaseAllResource() {
      Animation.closeAnimation(machineAni);
      Animation.closeAnimation(faceAni);
      Animation.closeAnimation(ballAni);
      machineAni = null;
      faceAni = null;
      ballAni = null;
   }

   public void balllogic(boolean var1) {
      // Project 60fps: ballvely хранится в исходных покадровых единицах,
      // поэтому за тик применяем её четверть с накоплением остатка.
      this.fpsRemBallPos += this.ballvely;
      int var3 = this.fpsRemBallPos >> Lib.FPS.SHIFT;
      this.fpsRemBallPos -= var3 << Lib.FPS.SHIFT;
      this.oppoBallPosY += var3;
      if (this.oppoBallPosY > 4992) {
         this.oppoBallPosY = 4992;
      }

      this.oppoBallPosX = MFMath.sqrt(24920064 - this.oppoBallPosY * this.oppoBallPosY) >> 6;

      for(int var2 = 0; var2 < 4; ++var2) {
         if (var1) {
            this.ballPos[var2][0] = this.posX + this.oppoBallPosX * (var2 * 14 + 13) / 78;
         } else {
            this.ballPos[var2][0] = this.posX - this.oppoBallPosX * (var2 * 14 + 13) / 78;
         }

         this.ballPos[var2][1] = this.posY + this.oppoBallPosY * (var2 * 14 + 13) / 78;
      }

      if (var1) {
         this.ballPos[4][0] = this.posX + this.oppoBallPosX;
      } else {
         this.ballPos[4][0] = this.posX - this.oppoBallPosX;
      }

      this.ballPos[4][1] = this.posY + this.oppoBallPosY;
      this.ballPos[5][0] = this.posX;
      this.ballPos[5][1] = this.posY + 256;
      this.ball.logic(this.ballPos[4][0], this.ballPos[4][1]);
   }

   public void close() {
      this.machineDrawer = null;
      this.faceDrawer = null;
      this.bossbroken = null;
   }

   public void doWhileBeAttack(PlayerObject var1, int var2, int var3) {
      if (this.state != 3 && this.state != 4 && this.state != 0 && (this.state != 1 || this.show_step >= 4) && this.face_state != 2) {
         --this.HP;
         player.doBossAttackPose(this, var2);
         if (this.HP > 0) {
            if (this.machine_state == 0) {
               this.machine_state = 4;
            }

            if (this.machine_state == 1) {
               this.machine_state = 5;
            }

            this.face_state = 2;
            this.face_cnt = 0;
         } else {
            this.state = 3;
            this.face_state = 3;
            this.machine_state = 0;
            BossBroken var4 = new BossBroken(28, this.posX >> 6, this.posY >> 6, 0, 0, 0, 0);
            this.bossbroken = var4;
            addGameObject(this.bossbroken, this.posX >> 6, this.posY >> 6);
            this.directTrans = true;
            this.drop_cnt = 0;
         }

         if (this.HP == 0) {
            SoundSystem.getInstance().playSe(35);
         } else {
            SoundSystem.getInstance().playSe(34);
         }
      }

   }

   public void doWhileCollision(PlayerObject var1, int var2) {
      if (!this.dead && this.state != 3 && this.state != 4 && (this.state != 1 || this.show_step >= 4) && var1 == player) {
         if (player.isAttackingEnemy()) {
            if (this.face_state != 2) {
               --this.HP;
               player.doBossAttackPose(this, var2);
               if (this.HP > 0) {
                  if (this.machine_state == 0) {
                     this.machine_state = 4;
                  }

                  if (this.machine_state == 1) {
                     this.machine_state = 5;
                  }

                  this.face_state = 2;
                  this.face_cnt = 0;
               } else {
                  this.state = 3;
                  this.face_state = 3;
                  this.machine_state = 0;
                  BossBroken var3 = new BossBroken(28, this.posX >> 6, this.posY >> 6, 0, 0, 0, 0);
                  this.bossbroken = var3;
                  addGameObject(this.bossbroken, this.posX >> 6, this.posY >> 6);
                  this.directTrans = true;
                  this.drop_cnt = 0;
               }

               if (this.HP == 0) {
                  SoundSystem.getInstance().playSe(35);
               } else {
                  SoundSystem.getInstance().playSe(34);
               }
            }
         } else if (this.state != 3 && this.state != 4 && this.machine_state != 4 && this.machine_state != 5 && this.face_state != 2) {
            player.beHurt();
            this.face_state = 1;
         }
      }

   }

   public void draw(MFGraphics var1) {
      if (this.displayFlag && !this.dead) {
         if (this.state >= 1) {
            if (this.isDisplayBall) {
               for(int var2 = 0; var2 < 6; ++var2) {
                  this.drawInMap(var1, this.ballDrawer[var2], this.ballPos[var2][0], this.ballPos[var2][1]);
               }
            }

            this.drawInMap(var1, this.machineDrawer);
            if (!this.directTrans) {
               this.drawInMap(var1, this.faceDrawer, this.posX + 192, this.posY - 1920);
            } else {
               this.drawInMap(var1, this.faceDrawer, this.posX - 192, this.posY - 1920);
            }

            if (this.ball != null) {
               this.ball.draw(var1);
            }

            if (this.state == 3) {
               this.bossbroken.draw(var1);
            }
         }

         this.drawCollisionRect(var1);
      }

   }

   public int getPaintLayer() {
      return 3;
   }

   public void logic() {
      if (!this.dead) {
         int var3 = this.posX;
         int var4 = this.posY;
         if (this.state > 0 && this.state < 4) {
            isBossEnter = true;
         } else if (this.state == 4) {
            isBossEnter = false;
         }

         int[] var5;
         label202:
         switch(this.state) {
         case 0:
            if (player.getFootPositionX() < 117120) {
               if (player.getFootPositionX() >= 55296) {
                  MapManager.setCameraLeftLimit(864 - SCREEN_WIDTH / 2);
                  MapManager.setCameraRightLimit(SCREEN_WIDTH / 2 + 864);
               }

               if (player.getFootPositionX() >= 55296 && player.getFootPositionX() < SCREEN_WIDTH / 2 + 864 + 20 << 6 && player.getFootPositionY() >= 43008) {
                  this.state = 1;
                  bossFighting = true;
                  bossID = 28;
                  MapManager.setCameraUpLimit(624);
                  MapManager.setCameraDownLimit(784);
                  MapManager.setCameraLeftLimit(864 - SCREEN_WIDTH / 2);
                  MapManager.setCameraRightLimit(SCREEN_WIDTH / 2 + 864);
                  this.show_step = 0;
                  this.enter_screen_frame_cn = 0;
                  SoundSystem.getInstance().playBgm(46);
                  this.displayFlag = true;
               }
            }
            break;
         case 1:
            int var1;
            short var2;
            switch(this.show_step) {
            case 0:
               if (this.enter_screen_frame_cn < 18 * Lib.FPS.SCALE) {
                  ++this.enter_screen_frame_cn;
               } else {
                  this.posY += this.fpsMoveY(270);   // Project 60fps: 270 not divisible by 4, carry remainder
                  if (this.posY >= 39936) {
                     this.posY = 39936;
                     this.show_step = 1;
                  }
               }
               break label202;
            case 1:
               this.posY += this.fpsMoveY(270);   // Project 60fps: 270 not divisible by 4, carry remainder
               if (this.posY >= 43264) {
                  this.posY = 43264;
                  this.show_step = 2;
                  this.changeAniState(this.machineDrawer, 1);
               }
               break label202;
            case 2:
               this.posX += this.fpsMoveX(-240);
               if (this.posX > 55296) {
                  break label202;
               }

               this.posX = 55296;
               this.posY = 43008;
               this.fpsResetMove();
               this.show_step = 3;
               this.changeAniState(this.faceDrawer, 1);
               this.changeAniState(this.machineDrawer, 0);
               this.isDisplayBall = true;

               for(var1 = 4; var1 >= 0; --var1) {
                  var5 = this.ballVel;
                  if (var1 < 4) {
                     var2 = 512;
                  } else {
                     var2 = 0;
                  }

                  // Project 60fps: скорость спуска цепи масштабирована на тики
                  var5[var1] = (5824 - (4 - var1) * 896 - var2) / (17 * Lib.FPS.SCALE);
               }

               this.ballVel[5] = this.ballVel[0] >> 1;
               break label202;
            case 3:
               if (this.laugh_cn < 10 * Lib.FPS.SCALE) {
                  ++this.laugh_cn;
               } else {
                  this.changeAniState(this.faceDrawer, 0);
               }

               for(var1 = 0; var1 < 6; ++var1) {
                  var5 = this.ballPos[var1];
                  var5[1] += this.ballVel[var1];
               }

               if (this.ballPos[4][1] < 48064) {
                  break label202;
               }

               for(var1 = 4; var1 >= 0; --var1) {
                  var5 = this.ballPos[var1];
                  if (var1 < 4) {
                     var2 = 512;
                  } else {
                     var2 = 0;
                  }

                  var5[1] = '므' - (4 - var1) * 896 - var2;
               }

               this.ballPos[5][0] = this.posX;
               this.ballPos[5][1] = this.posY + 256;
               this.show_step = 4;
               this.machine_state = 1;
               MapManager.setCameraUpLimit(544);
               MapManager.setCameraDownLimit(784);
               this.velocity = -45;
               this.vely = -7;
               this.changeAniState(this.machineDrawer, 1);
               this.oppoBallPosX = 0;
               this.oppoBallPosY = 4992;
               this.frameCn = 0;
               this.ballvely = 0;
               this.fpsRemBallPos = 0;
               break label202;
            case 4:
               this.bossStateChange();
               if (this.show_step != 5) {
                  if (this.frameCn < 17 * Lib.FPS.SCALE) {
                     ++this.frameCn;
                  }
                  this.oppoBallPosY = this.getEnterSwingY(this.frameCn);
                  this.oppoBallPosX = (int)Math.sqrt(4992 * 4992 - this.oppoBallPosY * this.oppoBallPosY);
               }

               if (this.posX > 54528) {
                  this.posY += this.fpsMoveY(this.vely);
                  this.posX += this.fpsMoveX(this.velocity);
               } else {
                  this.show_step = 5;
                  this.frameCn = 0;
                  this.posY = 42880;
                  this.posX = 54528;
                  this.vely = 7;
                  this.fpsResetMove();
               }

               this.balllogic(true);
               break label202;
            case 5:
               this.bossStateChange();
               if (this.state != 2) {
                  if (this.frameCn < 17 * Lib.FPS.SCALE) {
                     ++this.frameCn;
                  }
                  this.oppoBallPosY = 4992 - this.getEnterSwingY(this.frameCn);
                  this.oppoBallPosX = (int)Math.sqrt(4992 * 4992 - this.oppoBallPosY * this.oppoBallPosY);
               }

               if (this.posX > 53760) {
                  this.posY += this.fpsMoveY(this.vely);
                  this.posX += this.fpsMoveX(this.velocity);
               } else {
                  this.state = 2;
                  this.posY = 43136;
                  this.posX = 53760;
                  this.pro_step = 0;
                  this.frameCn = 0;
                  this.vely = -15;
                  this.ballvely = 0;
                  this.machine_state = 0;
                  this.fpsResetMove();
               }

               this.balllogic(true);
            default:
               break label202;
            }
         case 2:
            this.bossStateChange();
            switch(this.pro_step) {
            case 0:
               if (this.frameCn < 17 * Lib.FPS.SCALE) {
                  ++this.frameCn;
               }
               this.oppoBallPosY = this.getSwingYUp(this.frameCn);
               this.oppoBallPosX = -(int)Math.sqrt(4992 * 4992 - this.oppoBallPosY * this.oppoBallPosY);

               if (this.posY > 42880) {
                  this.posY += this.fpsMoveY(this.vely);
               } else {
                  this.posY = 42880;
                  this.pro_step = 1;
                  this.velocity = 180;
                  this.vely = 15;
                  this.frameCn = 0;
                  this.machine_state = 1;
                  this.face_state = 0;
                  this.directTrans = true;
                  this.ballvely = 0;
                  this.fpsResetMove();
               }

               this.balllogic(false);
               break label202;
            case 1:
               if (this.frameCn < 17 * Lib.FPS.SCALE) {
                  ++this.frameCn;
               }
               this.oppoBallPosY = this.getSwingYDown(this.frameCn);
               this.oppoBallPosX = -(int)Math.sqrt(4992 * 4992 - this.oppoBallPosY * this.oppoBallPosY);

               if (this.posX < 56832) {
                  this.posX += this.fpsMoveX(this.velocity);
                  this.posY += this.fpsMoveY(this.vely);
               } else {
                  this.posX = 56832;
                  this.posY = 43136;
                  this.pro_step = 2;
                  this.vely = -15;
                  this.machine_state = 0;
                  this.frameCn = 0;
                  this.ballvely = 0;
                  this.fpsResetMove();
               }

               this.balllogic(false);
               break label202;
            case 2:
               if (this.frameCn < 17 * Lib.FPS.SCALE) {
                  ++this.frameCn;
               }
               this.oppoBallPosY = this.getSwingYUp(this.frameCn);
               this.oppoBallPosX = (int)Math.sqrt(4992 * 4992 - this.oppoBallPosY * this.oppoBallPosY);

               if (this.posY > 42880) {
                  this.posY += this.fpsMoveY(this.vely);
               } else {
                  this.posY = 42880;
                  this.pro_step = 3;
                  this.frameCn = 0;
                  this.directTrans = false;
                  this.velocity = -180;
                  this.vely = 15;
                  this.machine_state = 1;
                  this.face_state = 0;
                  this.ballvely = 0;
                  this.fpsResetMove();
               }

               this.balllogic(true);
               break label202;
            case 3:
               if (this.frameCn < 17 * Lib.FPS.SCALE) {
                  ++this.frameCn;
               }
               this.oppoBallPosY = this.getSwingYDown(this.frameCn);
               this.oppoBallPosX = (int)Math.sqrt(4992 * 4992 - this.oppoBallPosY * this.oppoBallPosY);

               if (this.posX > 53760) {
                  this.posX += this.fpsMoveX(this.velocity);
                  this.posY += this.fpsMoveY(this.vely);
               } else {
                  this.posX = 53760;
                  this.posY = 43136;
                  this.pro_step = 0;
                  this.vely = -15;
                  this.machine_state = 0;
                  this.frameCn = 0;
                  this.ballvely = 0;
                  this.fpsResetMove();
               }

               this.balllogic(true);
            default:
               break label202;
            }
         case 3:
            if (this.posY >= 46336) {
               this.posY = 46336;
            } else {
               this.posY += this.fpsMoveY(270);   // Project 60fps: 270 not divisible by 4, carry remainder
            }

            this.bossbroken.logicBoom(this.posX, this.posY - 1280);
            int var10 = this.fpsDropStepY(this.drop_vely);
            if (this.ballPos[4][1] + var10 > this.getGroundY(this.ballPos[4][0], this.ballPos[4][1]) && this.drop_cnt == 0) {
               this.ballPos[4][1] = this.getGroundY(this.ballPos[4][0], this.ballPos[4][1]);
               this.drop_vely = -640;
               this.drop_cnt = 1;
               this.fpsRemDropVel = 0;
            } else if (this.ballPos[4][1] + var10 > this.getGroundY(this.ballPos[4][0], this.ballPos[4][1]) && this.drop_cnt == 1) {
               this.ballPos[4][1] = this.getGroundY(this.ballPos[4][0], this.ballPos[4][1]);
               this.drop_vely = -320;
               this.drop_cnt = 2;
               this.fpsRemDropVel = 0;
            } else if (this.ballPos[4][1] + var10 > this.getGroundY(this.ballPos[4][0], this.ballPos[4][1]) && this.drop_cnt == 2) {
               this.ballPos[4][1] = this.getGroundY(this.ballPos[4][0], this.ballPos[4][1]);
               this.drop_cnt = 3;
               this.fpsRemDropVel = 0;
            } else if (this.drop_cnt != 3) {
               this.drop_vely += ORIGINAL_GRAVITY / Lib.FPS.SCALE;
               var5 = this.ballPos[4];
               var5[1] += var10;
               this.ball.setEnd();
               this.isDisplayBall = false;
            }

            if (this.bossbroken.getEndState()) {
               this.state = 4;
               bossFighting = false;
               player.getBossScore();
               this.velocity = 270;
               this.machine_state = 1;
               this.face_state = 0;
               SoundSystem.getInstance().playBgm(18);
            }

            this.changeAniState(this.machineDrawer, this.machine_state);
            this.changeAniState(this.faceDrawer, this.face_state);
            break;
         case 4:
            if (this.posX >= 65536) {
               this.posX = 65536;
               MapManager.releaseCamera();
               MapManager.setCameraLeftLimit(864 - SCREEN_WIDTH / 2);
               MapManager.setCameraDownLimit(MapManager.getPixelHeight());
               MapManager.setCameraRightLimit(MapManager.getPixelWidth());
               this.dead = true;
            } else {
               this.posX += this.fpsMoveX(480);
            }

            if (this.posY >= 45056) {
               this.posY = 45056;
            } else {
               this.posY += this.fpsMoveY(270);   // Project 60fps: 270 not divisible by 4, carry remainder
            }
         }

         this.refreshCollisionRect(this.posX, this.posY);
         this.checkWithPlayer(var3, var4, this.posX, this.posY);
      }

   }

   public void refreshCollisionRect(int var1, int var2) {
      CollisionRect var3 = this.collisionRect;
      var3.setRect(var1 - 1504, var2 - 2560, 3008, 2176);
   }
}
