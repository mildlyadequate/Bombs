package com.example.philipp.bombs.objects;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class PlayGround extends GameObject {
    private static FloatBuffer borgCubeVerticesBuffer;
    private static ShortBuffer borgCubeQuadsBuffer;
    private static boolean buffersInitialized = false;
    PlayGroundTriangleUp collisionUp;
    PlayGroundTriangleDown collisionDown;
    public ArrayList<PlayGroundTriangleUp> trianglesUp = new ArrayList<PlayGroundTriangleUp>();
    public ArrayList<PlayGroundTriangleDown> trianglesDown = new ArrayList<PlayGroundTriangleDown>();
    private int rowTriangleCounter = 19;
    private int rowNumber = 1;
    private int rows = 4;
    private int trianglesCount = 0;
    private boolean rotUp = false;
    private static float mX = -9.0f;
    private static float mZ = -2.0f;
    private boolean rowOffset = true;

    public PlayGround() {
        // calculate amount of triangles needed to match the row count
        for (int i = 1; i <= rows; i++) {
            if (i % 2 == 1) {
                trianglesCount += 39;
            } else if (i % 2 == 0) {
                trianglesCount += 41;
            }
        }
        // create every triangle in the terrain
        for (int i = 0; i < trianglesCount; i++) {
            // ensure that the row offsets are placed right and that the correct triangle is chosen
            if (rowNumber % 2 == 0 && rowTriangleCounter == 0) {
                if (rowOffset) {
                    rowTriangleCounter = 20;
                    mX = -9.5f;
                } else {
                    rowTriangleCounter = 21;
                    mX = -10f;
                }

                rotUp = true;
            }
            if (rowNumber % 2 == 1 && rowTriangleCounter == 0) {
                if (rowOffset) {
                    rowTriangleCounter = 20;
                    mX = -9.5f;
                    rowOffset = false;
                } else {
                    rowTriangleCounter = 19;
                    mX = -9.0f;
                    rowOffset = true;
                }

                mZ--;
                rotUp = false;
            }
            if (rotUp) {
                PlayGroundTriangleUp newTriangle = new PlayGroundTriangleUp();
                newTriangle.setPosition(mX++, 0.0f, mZ);
                newTriangle.setVelocity(0.0f, 0.0f, 0.0f);
                trianglesUp.add(newTriangle);
            } else {
                PlayGroundTriangleDown newTriangle = new PlayGroundTriangleDown();
                newTriangle.setPosition(mX++, 0.0f, mZ);
                newTriangle.setVelocity(0.0f, 0.0f, 0.0f);
                trianglesDown.add(newTriangle);
            }
            rowTriangleCounter--;
            if (rowTriangleCounter == 0) {
                rowNumber++;
            }
        }
    }

    public boolean objectToGroundCollision(GameObject obj1) {
        float obj1X = obj1.getX();
        float obj1Z = obj1.getZ();
        for (PlayGroundTriangleUp test : trianglesUp) {
            float obj2X = test.getX();
            float obj2Z = test.getZ();
            float squaredHitDistance = ((obj1.scale + test.scale) / 2) * ((obj1.scale + test.scale) / 2);
            float squaredDistance = (obj1X - obj2X) * (obj1X - obj2X) + (obj1Z - obj2Z) * (obj1Z - obj2Z);

            if (squaredDistance < squaredHitDistance) {
                collisionUp = test;
                return true;
            }
        }
        for (PlayGroundTriangleDown test : trianglesDown) {
            float obj2X = test.getX();
            float obj2Z = test.getZ();
            float squaredHitDistance = ((obj1.scale + test.scale) / 2) * ((obj1.scale + test.scale) / 2);
            float squaredDistance = (obj1X - obj2X) * (obj1X - obj2X) + (obj1Z - obj2Z) * (obj1Z - obj2Z);

            if (squaredDistance < squaredHitDistance) {
                collisionDown = test;
                return true;
            }
        }
        return false;
    }
     public float getCollisionZ() {
        if (collisionUp != null) {
            float mZ = collisionUp.getZ() + collisionUp.scale / 2;
            collisionUp = null;
            return mZ;

        } else if (collisionDown != null) {
            float mZ = collisionDown.getZ() + collisionDown.scale / 2;
            collisionDown = null;
            return mZ;
        } else {
            return 0.0f;
        }
     }

    @Override
    public void draw(GL10 gl) {
        for (PlayGroundTriangleUp test: trianglesUp) {
            test.draw(gl);
        }
        for (PlayGroundTriangleDown test: trianglesDown) {
            test.draw(gl);
        }
    }

    @Override
    public void update(float fracSec) {
        updatePosition(fracSec);
    }
    public void removeObsolete(float ballZ) {
        ArrayList<PlayGroundTriangleDown> downsToBeRemoved = new ArrayList<>();
        ArrayList<PlayGroundTriangleUp> upsToBeRemoved = new ArrayList<>();
        for (PlayGroundTriangleUp up : trianglesUp) {
            if (up.scale == 0) {
                upsToBeRemoved.add(up);
            }
        }
        for (PlayGroundTriangleDown down : trianglesDown) {
            if (down.scale == 0) {
                downsToBeRemoved.add(down);
            }
        }
        for (PlayGroundTriangleUp up : upsToBeRemoved) {
            trianglesUp.remove(up);
        }
        for (PlayGroundTriangleDown down: downsToBeRemoved) {
            trianglesDown.remove(down);
        }
        upsToBeRemoved.clear();
        downsToBeRemoved.clear();
        for (PlayGroundTriangleUp up : trianglesUp) {
            if (up.getZ() > ballZ + 10.0f) {
                upsToBeRemoved.add(up);
            }
        }
        for (PlayGroundTriangleDown down : trianglesDown) {
            if (down.getZ() > ballZ + 10.0f) {
                downsToBeRemoved.add(down);
            }
        }
        for (PlayGroundTriangleUp up : upsToBeRemoved) {
            trianglesUp.remove(up);
        }
        for (PlayGroundTriangleDown down: downsToBeRemoved) {
            trianglesDown.remove(down);
        }
    }
    public void addRow() {
        int triangles = 0;
        // calculate amount of triangles needed considering the row number (because of the offset)
        if ((rowNumber / 2) % 2 == 1) {
            triangles = 39;
        } else if ((rowNumber / 2) % 2 == 0) {
            triangles = 41;
        }
            for (int i = 0; i < triangles; i++) {
                if (rowNumber % 2 == 0 && rowTriangleCounter == 0) {
                    if (rowOffset) {
                        rowTriangleCounter = 20;
                        mX = -9.5f;
                    } else {
                        rowTriangleCounter = 21;
                        mX = -10f;
                    }

                    rotUp = true;
                }
                if (rowNumber % 2 == 1 && rowTriangleCounter == 0) {
                    if (rowOffset) {
                        rowTriangleCounter = 20;
                        mX = -9.5f;
                        rowOffset = false;
                    } else {
                        rowTriangleCounter = 19;
                        mX = -9.0f;
                        rowOffset = true;
                    }

                    mZ--;
                    rotUp = false;
                }
                if (rotUp) {
                    PlayGroundTriangleUp newTriangle = new PlayGroundTriangleUp();
                    newTriangle.setPosition(mX++, 0.0f, mZ);
                    newTriangle.setVelocity(0.0f, 0.0f, 0.0f);
                    trianglesUp.add(newTriangle);
                } else {
                    PlayGroundTriangleDown newTriangle = new PlayGroundTriangleDown();
                    newTriangle.setPosition(mX++, 0.0f, mZ);
                    newTriangle.setVelocity(0.0f, 0.0f, 0.0f);
                    trianglesDown.add(newTriangle);
                }
                rowTriangleCounter--;
                if (rowTriangleCounter == 0) {
                    rowNumber++;
                }
            }
    }
}
