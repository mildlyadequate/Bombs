package com.example.philipp.bombs.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import static com.example.philipp.bombs.util.Utilities.normalize;

public class PowerUp extends GameObject {
    // current rotation
    public float rotation = 0.0f;
    public float fallSpeed = 0.0f;
    // rotation speed in deg/s
    public float angularVelocity = 0.0f;
    public float rotationAxis[] = {0.0f, 1.0f, 0.0f};
    private int cooldown = 60 + (5 + (int)(Math.random() * ((60 - 5) + 1)));
    private int decayTime = 12;
    private boolean decayed = false;
    private long creationTime = System.currentTimeMillis();
    private float currentColor[] = {0.05f, 0.8f, 1.0f};

    //@formatter:off
    private static final float powerUp_vertices[] = {

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
            0.462f, -0.191f, 0f,
            0.191f, 0.28f, 0f,
            -0.191f, 0.28f, 0f,
            -0.191f, 0.191f, 0f,
            -0.28f, 0.191f, 0f,
            -0.28f, -0.191f, 0f,
            -0.191f, -0.191f, 0f,
            -0.191f, -0.28f, 0f,
            0.191f, -0.28f, 0f,
            0.191f, -0.191f, 0f,
            0.28f, -0.191f, 0f,
            0.28f, 0.191f, 0f,
            0.191f, 0.191f, 0f,



    };
    private static final short powerUp_lines[] = {
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
            15, 0,
            16, 17,
            17, 18,
            18, 19,
            19, 20,
            20, 21,
            21, 22,
            22, 23,
            23, 24,
            24, 25,
            25, 26,
            26, 27,
            27, 16

    };
    //@formatter:on

    private static FloatBuffer powerUpVerticesBuffer;
    private static ShortBuffer powerUpTrianglesBuffer;

    private static boolean buffersInitialized = false;

    public PowerUp() {
        randomizeRotationAxis();

        if (!buffersInitialized) {
            // Initialize buffers
            ByteBuffer asteroidVerticesBB = ByteBuffer.allocateDirect(powerUp_vertices.length * 4);
            asteroidVerticesBB.order(ByteOrder.nativeOrder());
            powerUpVerticesBuffer = asteroidVerticesBB.asFloatBuffer();
            powerUpVerticesBuffer.put(powerUp_vertices);
            powerUpVerticesBuffer.position(0);

            ByteBuffer asteroidTrianglesBB = ByteBuffer.allocateDirect(powerUp_lines.length * 2);
            asteroidTrianglesBB.order(ByteOrder.nativeOrder());
            powerUpTrianglesBuffer = asteroidTrianglesBB.asShortBuffer();
            powerUpTrianglesBuffer.put(powerUp_lines);
            powerUpTrianglesBuffer.position(0);

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

            gl.glLineWidth(1.0f);

            gl.glRotatef(rotation, rotationAxis[0], rotationAxis[1], rotationAxis[2]);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, powerUpVerticesBuffer);
            setCurrentColor(gl);
            for (int i = 0; i < (powerUp_lines.length / 2); i++) {
                powerUpTrianglesBuffer.position(2 * i);
                gl.glDrawElements(GL10.GL_LINE_LOOP, 2, GL10.GL_UNSIGNED_SHORT, powerUpTrianglesBuffer);
            }
            powerUpTrianglesBuffer.position(0);

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
        rotationAxis[1] = (float) Math.random();
        rotationAxis[2] = 0.0f;
        normalize(rotationAxis);
    }
    private void setCurrentColor(GL10 gl) {
        float r, g, b;
        long currentTime = System.currentTimeMillis();
        if ((decayTime - 3) == (int) ((currentTime - creationTime) / 1000)) {
            r = currentColor[0] * 0.75f;
            g = currentColor[1] * 0.75f;
            b = currentColor[2] * 0.75f;
        } else if ((decayTime - 2) == (int) ((currentTime - creationTime) / 1000)) {
            r = currentColor[0] * 0.5f;
            g = currentColor[1] * 0.5f;
            b = currentColor[2] * 0.5f;
        } else if ((decayTime - 1) == (int) ((currentTime - creationTime) / 1000)) {
            r = currentColor[0] * 0.25f;
            g = currentColor[1] * 0.25f;
            b = currentColor[2] * 0.25f;
        } else if (!decayed){
            r = currentColor[0];
            g = currentColor[1];
            b = currentColor[2];
        } else {
            r = 0;
            g = 0;
            b = 0;
        }
        gl.glColor4f(r, g, b, 0);
    }
    public boolean decay() {
        long currentTime = System.currentTimeMillis();
        if (decayTime == (int) ((currentTime - creationTime) / 1000)) {
            decayed = true;
        }
        return decayed;
    }
    public int getCooldown() {
        return cooldown;
    }

}
