package com.example.philipp.bombs.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class PlayGroundTriangleUp extends GameObject {

    private static FloatBuffer triangleVerticesBuffer;
    private static ShortBuffer triangleQuadsBuffer;
    private static boolean buffersInitialized = false;

    public PlayGroundTriangleUp() {
        if (!buffersInitialized) {
            float vertices[] = {
                    -0.5f, -0.5f, 0f,    // btl 0
                    0.5f, -0.5f, 0f,    // bbl 1
                    0f, 0.5f, 0f,    // bbr 2
            };
            short quads[] = {
                    0, 1, 2, 0 // front
            };


            ByteBuffer borgCubeVerticesBB = ByteBuffer.allocateDirect(vertices.length * 4);
            borgCubeVerticesBB.order(ByteOrder.nativeOrder());
            triangleVerticesBuffer = borgCubeVerticesBB.asFloatBuffer();
            triangleVerticesBuffer.put(vertices);
            triangleVerticesBuffer.position(0);

            ByteBuffer borgCubeQuadsBB = ByteBuffer.allocateDirect(quads.length * 2);
            borgCubeQuadsBB.order(ByteOrder.nativeOrder());
            triangleQuadsBuffer = borgCubeQuadsBB.asShortBuffer();
            triangleQuadsBuffer.put(quads);
            triangleQuadsBuffer.position(0);

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
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triangleVerticesBuffer);
            gl.glColor4f(0.0f, 1.0f, 0.0f, 0.0f);
            for (int i = 0; i < (triangleQuadsBuffer.capacity() / 4); i++) {
                triangleQuadsBuffer.position(4 * i);
                gl.glDrawElements(GL10.GL_LINE_LOOP, 4, GL10.GL_UNSIGNED_SHORT, triangleQuadsBuffer);
            }
            triangleQuadsBuffer.position(0);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
        gl.glPopMatrix();
    }

    @Override
    public void update(float fracSec) {
        updatePosition(fracSec);
    }

}

