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
    private int decayTime = 10;
    private long creationTime = System.currentTimeMillis();
    private float currentColor[] = new float[3];

    //@formatter:off
    private static final float powerUp_vertices[] = {

            0.5f, 0f, 0f,
            0.462f, 0f, 0.191f,
            0.354f, 0f, 0.354f,
            0.191f, 0f, 0.462f,
            0f, 0f, 0.5f,
            -0.191f, 0f, 0.462f,
            -0.354f, 0f, 0.354f,
            -0.462f, 0f, 0.191f,
            -0.5f, 0f, 0f,
            -0.462f, 0f, -0.191f,
            -0.354f, 0f, -0.354f,
            -0.191f, 0f, -0.462f,
            0f, 0f, -0.5f,
            0.191f, 0f, -0.462f,
            0.354f, 0f, -0.354f,
            0.462f, 0f, -0.191f,

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
            15, 0

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
        getCurrentColor();
        gl.glPushMatrix();
        {
            gl.glMultMatrixf(transformationMatrix, 0);
            gl.glScalef(scale, scale, scale);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            gl.glLineWidth(1.0f);

            gl.glRotatef(rotation, rotationAxis[0], rotationAxis[1], rotationAxis[2]);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, powerUpVerticesBuffer);
            gl.glColor4f(currentColor[0], currentColor[1], currentColor[2], 0);
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
    public void getCurrentColor() {
        long currentTime = System.currentTimeMillis();
        if ((decayTime - 3) == (int) ((currentTime - creationTime) / 1000)) {
            currentColor[0] = 0.0f;
            currentColor[1] *= 0.75f;
            currentColor[2] *= 0.75f;
        } else if ((decayTime - 2) == (int) ((currentTime - creationTime) / 1000)) {
            currentColor[0] = 0.0f;
            currentColor[1] *= 0.5f;
            currentColor[2] *= 0.5f;
        } else if ((decayTime - 1) == (int) ((currentTime - creationTime) / 1000)) {
            currentColor[0] = 0.0f;
            currentColor[1] *= 0.25f;
            currentColor[2] *= 0.25f;
        } else {
            currentColor[0] = 0.0f;
            currentColor[1] = 0.75f;
            currentColor[2] = 1.0f;
        }
    }
}
