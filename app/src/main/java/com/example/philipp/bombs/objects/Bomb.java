package com.example.philipp.bombs.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.philipp.bombs.util.Utilities.*;

import javax.microedition.khronos.opengles.GL10;

public class Bomb extends GameObject {
    // current rotation
    public float rotation = 0.0f;
    public float fallSpeed = 0.0f;
    // rotation speed in deg/s
    public float angularVelocity = 0.0f;
    public float rotationAxis[] = {0.0f, 1.0f, 0.0f};

    private static float colorA[] = {0.85f, 0.85f, 0.85f};
    private static float colorB[] = {0.7f, 0.7f, 0.7f};

    private float currentColor[] = new float[3];
    public long explosionTimer = System.currentTimeMillis();
    public int explosionDelay = 7 + (int)(Math.random() * ((15 - 7) + 1));
    public Explosion explosion = new Explosion();
    public boolean exploded = false;
    public boolean hit = false;

    //@formatter:off
    private static final float bomb_vertices[] = {

            0.5f, 0f, 0f,
            0.462f, 0.191f, 0f,
            0.354f, 0.354f, 0f,
            0.191f, 0.462f, 0f,
            0f, 0.5f, 0f,
            -0.191f, 0.462f, 0f,
            -0.354f, 0.354f, 0f,
            -0.462f, 0.191f, 0f,
            -0.5f, 0f, 0f,
            -0.462f, -0.191f, 0f,
            -0.354f, -0.354f, 0f,
            -0.191f, -0.462f, 0f,
            0f, -0.5f, 0f,
            0.191f, -0.462f, 0f,
            0.354f, -0.354f, 0f,
            0.462f, -0.191f, 0f

    };
    private static final short bomb_lines[] = {
            0, 1,
            1, 2,
            2, 3,
            3, 4,
            4, 5,
            5, 6,
            6, 7,
            7, 8,
            8, 9,
            9, 10,
            10, 11,
            11, 12,
            12, 13,
            13, 14,
            14, 15,
            15, 0

    };
    //@formatter:on

    private static FloatBuffer bombVerticesBuffer;
    private static ShortBuffer bombTrianglesBuffer;

    private static boolean buffersInitialized = false;

    public Bomb() {
        randomizeRotationAxis();
        randomizeColor();

        if (!buffersInitialized) {
            // Initialize buffers
            ByteBuffer asteroidVerticesBB = ByteBuffer.allocateDirect(bomb_vertices.length * 4);
            asteroidVerticesBB.order(ByteOrder.nativeOrder());
            bombVerticesBuffer = asteroidVerticesBB.asFloatBuffer();
            bombVerticesBuffer.put(bomb_vertices);
            bombVerticesBuffer.position(0);

            ByteBuffer asteroidTrianglesBB = ByteBuffer.allocateDirect(bomb_lines.length * 2);
            asteroidTrianglesBB.order(ByteOrder.nativeOrder());
            bombTrianglesBuffer = asteroidTrianglesBB.asShortBuffer();
            bombTrianglesBuffer.put(bomb_lines);
            bombTrianglesBuffer.position(0);

            buffersInitialized = true;
        }
    }
    @Override
    public void draw(GL10 gl) {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPushMatrix();
        {
            gl.glMultMatrixf(transformationMatrix, 0);
            gl.glScalef(scale, scale, scale);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            gl.glLineWidth(5.0f);

            gl.glRotatef(rotation, rotationAxis[0], rotationAxis[1], rotationAxis[2]);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bombVerticesBuffer);
            gl.glColor4f(currentColor[0], currentColor[1], currentColor[2], 0);
            for (int i = 0; i < (bomb_lines.length / 2); i++) {
                bombTrianglesBuffer.position(2 * i);
                gl.glDrawElements(GL10.GL_LINE_LOOP, 2, GL10.GL_UNSIGNED_SHORT, bombTrianglesBuffer);
            }
            bombTrianglesBuffer.position(0);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
        gl.glPopMatrix();
    }

    @Override
    public void update(float fracSec) {
        updatePosition(fracSec);
        rotation += fracSec * angularVelocity;
    }

    public void randomizeRotationAxis() {
        rotationAxis[0] = 0.0f;
        rotationAxis[1] = 0.0f;
        rotationAxis[2] = (float) Math.random();
        normalize(rotationAxis);
    }

    public void randomizeColor() {
        // shades of grey hehe
        float factor = (float) Math.random();
        currentColor[0] = factor * colorA[0] + (1.0f - factor) * colorB[0];
        currentColor[1] = factor * colorA[1] + (1.0f - factor) * colorB[1];
        currentColor[2] = factor * colorA[2] + (1.0f - factor) * colorB[2];
    }
    public void explode(float fracSec) {
        explosion.scale = scale * 5;
        explosion.setPosition(getX(), getY(), getZ());
        explosion.setVelocity(0f, 0f,0f);
        explosion.update(fracSec);
        scale = 0;
        exploded = true;
    }
}
