package com.example.philipp.bombs.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import static com.example.philipp.bombs.util.Utilities.normalize;

public class Explosion extends GameObject {
    // current rotation
    public float rotation = 0.0f;
    // rotation speed in deg/s
    public float angularVelocity = 0.0f;
    public float rotationAxis[] = {0.0f, 1.0f, 0.0f};

    private static float colorA[] = {1.0f, 0.74f, 0.0f};
    private static float colorB[] = {0.94f, 0.5f, 0.05f};

    private float currentColor[] = new float[3];

    //@formatter:off
    private static final float bomb_vertices[] = {
            0f, 0f, 0f,
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

    };
    private static final short bomb_lines[] = {
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 5,
            0, 5, 6,
            0, 6, 7,
            0, 7, 8,
            0, 8, 9,
            0, 9, 10,
            0, 10, 11,
            0, 11, 12,
            0, 12, 13,
            0, 13, 14,
            0, 14, 15,
            0, 15, 16,
            0, 16, 1
    };
    //@formatter:on

    private static FloatBuffer bombVerticesBuffer;
    private static ShortBuffer bombTrianglesBuffer;

    private static boolean buffersInitialized = false;

    public Explosion() {
        scale = 0;
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

            gl.glLineWidth(1.0f);

            gl.glRotatef(rotation, rotationAxis[0], rotationAxis[1], rotationAxis[2]);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bombVerticesBuffer);
            gl.glColor4f(currentColor[0], currentColor[1], currentColor[2], 0);
            for (int i = 0; i < (bomb_lines.length / 3); i++) {
                bombTrianglesBuffer.position(3 * i);
                gl.glDrawElements(GL10.GL_LINE_LOOP, 3, GL10.GL_UNSIGNED_SHORT, bombTrianglesBuffer);
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
        rotationAxis[1] = (float) Math.random();
        rotationAxis[2] = 0.0f;
        normalize(rotationAxis);
    }

    public void randomizeColor() {
        // shades of orange
        float factor = (float) Math.random();
        currentColor[0] = factor * colorA[0] + (1.0f - factor) * colorB[0];
        currentColor[1] = factor * colorA[1] + (1.0f - factor) * colorB[1];
        currentColor[2] = factor * colorA[2] + (1.0f - factor) * colorB[2];
    }
}
