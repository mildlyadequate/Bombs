package com.example.philipp.bombs;

import static com.example.philipp.bombs.util.Utilities.normalize;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.MotionEvent;

import com.example.philipp.bombs.objects.GameObject;
import com.example.philipp.bombs.objects.Bomb;
import com.example.philipp.bombs.objects.PlayGroundTriangleDown;
import com.example.philipp.bombs.objects.PlayGroundTriangleUp;
import com.example.philipp.bombs.objects.PlayerBall;
import com.example.philipp.bombs.objects.PlayGround;

public class GameGLSurfaceView extends GLSurfaceView {

    private SceneRenderer renderer;
    public Context context;  // activity context

    private static final int bombCount = 15;
    private static final float minSpawnDistanceToPlayer = 1.5f;
    private static final float minSpawnDistanceBetweenBombs = 1.5f;
    private static final float bombMinScale = 0.8f;
    private static final float bombMaxScale = 1.2f;
    private static final float maxFallSpeed = -6.0f;
    private float terrainLimit = -2.0f;
    private static float fallSpeed = 0f;
    private static boolean jump = false;
    private static boolean doubleJump = false;

    private ArrayList<Bomb> bombs = new ArrayList<Bomb>();
    private PlayerBall ball = new PlayerBall();
    private PlayGround terrain = new PlayGround();


    public GameGLSurfaceView(Context context) {
        super(context);
        renderer = new SceneRenderer();
        setRenderer(renderer);

        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    // called from sensor
    public void setBallVelocity(float vx, float vy) {
        ball.setVelocity(vx, vy, fallSpeed);
    }
    //jump
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (jump == true) {
                    fallSpeed = 4.5f;
                    jump = false;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (jump == false && doubleJump == true) {
                    fallSpeed = 4.5f;
                    doubleJump = false;
                }
                break;
        }
        return true;
    }

    private class SceneRenderer implements Renderer {
        private float[] modelViewScene = new float[16];

        public float boundaryTop, boundaryBottom, boundaryLeft, boundaryRight;

        long lastFrameTime;

        public SceneRenderer() {
            lastFrameTime = System.currentTimeMillis();
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            // update time calculation
            long delta = System.currentTimeMillis() - lastFrameTime;
            float fracSec = (float) delta / 1000;
            lastFrameTime = System.currentTimeMillis();

            // scene updates
            updateBall(fracSec);
            updateBombs(fracSec, gl);
            updateTerrain(fracSec);


            // clear screen and depth buffer
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            GL11 gl11 = (GL11) gl;

            // load local system to draw scene items
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl11.glLoadMatrixf(modelViewScene, 0);
            float desired_height = 10.0f;
            float y = (float) (desired_height / 2 / Math.tan(45.0f / 2 * (Math.PI / 180.0f)));
            // look at the ball, but only move with it's z-axis
            GLU.gluLookAt(gl, 0.0f, -y, ball.getZ() + 1.0f, 0.0f,0.0f, ball.getZ() + 1.0f, 0.0f,0.0f,1.0f);
            terrain.draw(gl);
            ball.draw(gl);
            for (Bomb bomb : bombs) {
                bomb.draw(gl);
                bomb.explosion.draw(gl);
            }
        }


        private void updateBall(float fracSec) {
            ball.update(fracSec);
            // keep ball within window boundaries
            if (ball.getX() < boundaryLeft + ball.scale / 2)
                ball.setX(boundaryLeft + ball.scale / 2);
            if (ball.getX() > boundaryRight - ball.scale / 2)
                ball.setX(boundaryRight - ball.scale / 2);
            // Gravity
            if (terrain.objectToGroundCollision(ball)) {
                ball.setZ(terrain.getCollisionZ() + ball.scale / 2);
                if (fallSpeed > -0.5f && fallSpeed < -0.0f) {
                    fallSpeed = 0f;
                    jump = true;
                    doubleJump = true;
                } else {
                    fallSpeed = (fallSpeed/2.0f) * (-1);
                }
            } else {
                if (fallSpeed >= maxFallSpeed) {
                    fallSpeed -= 0.0981f;
                }
            }
        }


        private boolean areColliding(GameObject obj1, GameObject obj2) {
            float obj1X = obj1.getX();
            float obj1Z = obj1.getZ();
            float obj2X = obj2.getX();
            float obj2Z = obj2.getZ();
            float squaredHitDistance = ((obj1.scale + obj2.scale) / 2) * ((obj1.scale + obj2.scale) / 2);
            float squaredDistance = (obj1X - obj2X) * (obj1X - obj2X) + (obj1Z - obj2Z) * (obj1Z - obj2Z);

            if (squaredDistance < squaredHitDistance)
                return true;
            return false;
        }


        private void updateBombs(float fracSec, GL10 gl) {
            ArrayList<Bomb> bombsToBeRemoved = new ArrayList<Bomb>();
            // Bomb to Ground Collision
            for (Bomb bomb : bombs) {
                if (terrain.objectToGroundCollision(bomb)) {
                    bomb.setZ(terrain.getCollisionZ() + bomb.scale / 2);
                    if (bomb.fallSpeed > -0.5 && bomb.fallSpeed < 0.0f) {
                        bomb.fallSpeed = 0f;
                    } else {
                        bomb.fallSpeed = (bomb.fallSpeed / 2.0f) * (-1);
                    }
                    if (bomb.velocity[0] > 0f) {
                        if (bomb.velocity[0] <= 0.2f) {
                            bomb.velocity[0] = 0f;
                        } else {
                            bomb.velocity[0] -= 0.0981f;
                        }
                    } else if (bomb.velocity[0] < 0f) {
                        if (bomb.velocity[0] >= -0.2f) {
                            bomb.velocity[0] = 0f;
                        } else {
                            bomb.velocity[0] += 0.0981f;
                        }
                    }
                }
                if (bomb.fallSpeed >= maxFallSpeed && !(terrain.objectToGroundCollision(bomb))) {
                    bomb.fallSpeed -= 0.0981f;
                }
            }

            // position update on all obstacles
            for (Bomb bomb : bombs) {
                bomb.velocity[2] = bomb.fallSpeed;
                bomb.update(fracSec);
            }


            // check for obstacles that flew out of the viewing area and remove
            // or deactivate them
            for (Bomb bomb : bombs) {
                // offset makes sure that the obstacles don't get deleted or set
                // inactive while visible to the player.
                float offset = bomb.scale;

                if ((bomb.getX() > boundaryRight + offset)
                        || (bomb.getX() < boundaryLeft - offset)
                        || (bomb.getZ() > boundaryTop + offset)) {
                        bombsToBeRemoved.add(bomb);
                }
            }
            // remove obsolete obstacles
            for (Bomb bomb : bombsToBeRemoved) {
                bombs.remove(bomb);
            }
            bombsToBeRemoved.clear();


            // TODO proper Bomb and Ball Collision handling
            for (Bomb bomb : bombs) {
                if (areColliding(ball, bomb)) {
                        bomb.setZ(ball.getZ() + ball.scale / 2 + bomb.scale / 2);
                        bomb.velocity[0] = ball.velocity[0];
                        bomb.fallSpeed = (bomb.fallSpeed / 2) * (-1);
                        bomb.update(fracSec);
                }
            }
            // remove obsolete obstacles
            for (Bomb bomb : bombsToBeRemoved) {
                bombs.remove(bomb);
            }
            bombsToBeRemoved.clear();


            // TODO proper Bomb and Bomb Collision handling
            for (int i = 0; i <= bombs.size() - 2; i++) {
                Bomb bomb = bombs.get(i);

                // check for collision with other Bomb
                for (int j = i + 1; j <= bombs.size() - 1; j++) {
                    Bomb otherBomb = bombs.get(j);

                    if (areColliding(bomb, otherBomb)) {
                       float cv1[] = bomb.velocity;
                       float cv2[] = otherBomb.velocity;
                       if (terrain.objectToGroundCollision(bomb) && !terrain.objectToGroundCollision(otherBomb)) {
                           cv1[0] = cv2[0] / 2;
                           cv2[0] = (cv2[0] / 2) * (-1);
                           cv2[2] = (cv2[2] / 2) * (-1);
                       } else if (terrain.objectToGroundCollision(otherBomb) && !terrain.objectToGroundCollision(bomb)) {
                           cv2[0] = cv1[0] / 2;
                           cv1[0] = (cv1[0] / 2) * (-1);
                           cv1[2] = (cv1[2] / 2) * (-1);
                       } else {
                           cv1[0] = (cv1[0] / 2) * (-1);
                           cv2[0] = (cv2[0] / 2) * (-1);
                       }

                    }
                }
            }
            // Bomb Explosion - after a random time between 7 and 15 seconds the bomb explodes
            // TODO possibly add repulsion to explosion
            for (Bomb bomb: bombs) {
                long currentTime = System.currentTimeMillis();
                if (bomb.explosionDelay == (int) ((currentTime - bomb.explosionTimer) / 1000) & !bomb.exploded) {
                    bomb.explode(fracSec);
                    for (PlayGroundTriangleDown tri : terrain.trianglesDown){
                        if (areColliding(tri, bomb.explosion)) {
                            tri.scale = 0;
                        }
                    }
                    for(PlayGroundTriangleUp tri : terrain.trianglesUp) {
                        if (areColliding(tri, bomb.explosion)) {
                            tri.scale = 0;
                        }
                    }
                }
                if ((bomb.explosionDelay + 1) == (int) ((currentTime - bomb.explosionTimer) / 1000) & bomb.exploded) {
                    bombsToBeRemoved.add(bomb);
                }
                if (areColliding(ball, bomb.explosion) && bomb.exploded && !bomb.hit) {
                    ball.damage(0.25f); // add some damage to the Ball
                    bomb.hit = true;
                }
            }
            // remove oboslete Bombs
            for (Bomb bomb : bombsToBeRemoved) {
                bombs.remove(bomb);
            }
            bombsToBeRemoved.clear();


            // Spawn new Bomb TODO minimize the Count at the beginning and rise the amount the further the Player gets down (rising difficulty)
            if (bombCount > bombs.size()) {
                for (int i = 0; i < bombCount - bombs.size(); ++i) {

                    float scale = (float) Math.random() * (bombMaxScale - bombMinScale) + bombMinScale;


                    float spawnX = 0.0f;
                    float spawnZ = 0.0f;
                    float spawnOffset = scale * 0.5f;
                    float velocity[] = new float[3];

                    // determine source and destination quadrant
                    int sourceCode = ((Math.random() < 0.5 ? 0 : 1) << 1) | (Math.random() < 0.5 ? 0 : 1);  // source quadrant
                    int destCode = sourceCode ^ 3;    // destination quadrant is opposite of source
                    //Log.d("Code", sourceCode+" "+destCode);

                    /* sourceCode, destCode
                     * +----+----+
                     * | 00 | 01 |
                     * +----+----+
                     * | 10 | 11 |
                     * +----+----+
                     */

                    // calculate source vertex position, <0.5 horizontal, else vertical
                    if (Math.random() < 0.5) {
                        spawnZ = boundaryTop + spawnOffset;
                        spawnX = (sourceCode & 1) > 0 ? boundaryRight * (float) Math.random() : boundaryLeft * (float) Math.random();
                    } else {  // vertical placing, left or right
                        spawnZ = boundaryTop * (float) Math.random();
                        spawnX = (sourceCode & 1) > 0 ? boundaryRight + spawnOffset : boundaryLeft - spawnOffset;
                    }

                    // calculate destination vertex position, <0.5 horizontal, else vertical
                    if (Math.random() < 0.5) {  // horizontal placing, top or bottom
                        velocity[2] = boundaryBottom - spawnOffset;
                        velocity[0] = (destCode & 1) > 0 ? boundaryRight * (float) Math.random() : boundaryLeft * (float) Math.random();
                    } else {  // vertical placing, left or right
                        velocity[2] = boundaryBottom * (float) Math.random();
                        velocity[0] = (destCode & 1) > 0 ? boundaryRight + spawnOffset : boundaryLeft - spawnOffset;
                    }

                    // calculate velocity
                    velocity[0] -= spawnX;
                    velocity[2] -= spawnZ;
                    normalize(velocity);


                    boolean positionOk = true;

                    // check distance to other Bombs
                    for (Bomb bomb : bombs) {
                        float minDistance = 0.5f * scale + 0.5f * bomb.scale + minSpawnDistanceBetweenBombs;
                        if (Math.abs(spawnX - bomb.getX()) < minDistance
                                && Math.abs(spawnZ - bomb.getZ()) < minDistance)
                            positionOk = false;    // Distance too small -> invalid position
                    }

                    // check distance to player
                    float minPlayerDistance = 0.5f * scale + 0.5f * ball.scale + minSpawnDistanceToPlayer;
                    if (Math.abs(spawnX - ball.getX()) < minPlayerDistance &&
                            Math.abs(spawnZ - ball.getZ()) < minPlayerDistance)
                        positionOk = false;    // Distance to player too small -> invalid position

                    if (!positionOk)
                        continue; // Invalid spawn position -> try again next time


                    Bomb newBomb = new Bomb();
                    newBomb.scale = scale;
                    newBomb.randomizeRotationAxis();
                    newBomb.angularVelocity = 50;
                    newBomb.setPosition(spawnX, 0, spawnZ);
                    newBomb.velocity = velocity;
                    bombs.add(newBomb);


                }
            }
        }
        private void updateTerrain(float fracSec) {
            terrain.setPosition(0.0f, 0.0f, -14.0f);
            // velocity of the terrain must always be 0
            terrain.setVelocity(0.0f,0.0f,0.0f);
            // add new Terrain at the bottom to ensure endless game
            for (Bomb bomb : bombs) {
                if (bomb.getZ() < terrainLimit) {
                    terrainLimit--;
                    terrain.addRow();
                }
            }
            // remove Rows that are too far at the top in order to save performance
            terrain.removeObsolete(ball.getZ());
            terrain.update(fracSec);
        }

        @Override
        // Called when surface is created or the viewport gets resized
        // set projection matrix
        // precalculate modelview matrix
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GL11 gl11 = (GL11) gl;
            gl.glViewport(0, 0, width, height);

            float aspectRatio = (float) width / height;
            float fovy = 45.0f;

            // set up projection matrix for scene
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            GLU.gluPerspective(gl, fovy, aspectRatio, 0.001f, 100.0f);

            // set up modelview matrix for scene
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            float desired_height = 10.0f;
            gl11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewScene, 0);

            // window boundaries
            // z range is the desired height
            boundaryTop = desired_height / 2;
            boundaryBottom = -desired_height / 2;
            // x range is the desired width
            boundaryLeft = -(desired_height / 2 * aspectRatio);
            boundaryRight = (desired_height / 2 * aspectRatio);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glDisable(GL10.GL_DITHER);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glShadeModel(GL10.GL_FLAT);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);
            gl.glShadeModel(GL10.GL_SMOOTH);
            gl.glEnable(GL10.GL_DEPTH_TEST);
        }

    }

}
