package com.yalin.style.engine.advance;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author jinyalin
 * @since 2017/7/24.
 */

public class AdvanceRenderer implements GLSurfaceView.Renderer {
    private static final float TOUCH_SCALE = 0.2f; // Proved to be good for normal rotation
    /* Rotation speed values */
    private static final float xspeed = 0.5f; // X Rotation Speed
    private static final float yspeed = 0.5f; // Y Rotation Speed

    private Cube cube;
    private Context context;

    private float xrot; // X Rotation
    private float yrot; // Y Rotation

    private float oldX;
    private float oldY;
    private float z = -5.0f; // Depth Into The Screen

    public AdvanceRenderer(Context context) {
        this.cube = new Cube();
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glEnable(GL10.GL_LIGHT0); // Enable Light 0

        // Blending
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f); // Full Brightness. 50% Alpha ( NEW )
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE); // Set The Blending Function For Translucency ( NEW )

        gl.glDisable(GL10.GL_DITHER); // Disable dithering
        gl.glEnable(GL10.GL_TEXTURE_2D); // Enable Texture Mapping
        gl.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // Black Background
        gl.glClearDepthf(1.0f); // Depth Buffer Setup
        gl.glEnable(GL10.GL_DEPTH_TEST); // Enables Depth Testing
        gl.glDepthFunc(GL10.GL_LEQUAL); // The Type Of Depth Testing To Do

        // Really Nice Perspective Calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        cube.loadGLTexture(gl, context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (height == 0) { // Prevent A Divide By Zero By
            height = 1; // Making Height Equal One
        }

        gl.glViewport(0, 0, width, height); // Reset The Current Viewport
        gl.glMatrixMode(GL10.GL_PROJECTION); // Select The Projection Matrix
        gl.glLoadIdentity(); // Reset The Projection Matrix

        // Calculate The Aspect Ratio Of The Window
        GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW); // Select The Modelview Matrix
        gl.glLoadIdentity(); // Reset The Modelview Matrix
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity(); // Reset The Current Modelview Matrix

        // Check if the light flag has been set to enable/disable lighting
        gl.glEnable(GL10.GL_LIGHTING);

        // Check if the blend flag has been set to enable/disable blending
        gl.glEnable(GL10.GL_BLEND); // Turn Blending On ( NEW )
        gl.glDisable(GL10.GL_DEPTH_TEST); // Turn Depth Testing Off ( NEW )

        // Drawing
        gl.glTranslatef(0.0f, 0.0f, z); // Move z units into the screen
        // Scale the Cube to 80 percent, otherwise it would be too large for the screen
        gl.glScalef(0.8f, 0.8f, 0.8f);

        // Rotate around the axis based on the rotation matrix (rotation, x, y, z)
        gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f); // X
        gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f); // Y

        cube.draw(gl, 0);

        // Change rotation factors
        xrot += xspeed;
        yrot += yspeed;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // If a touch is moved on the screen
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // Calculate the change
            float dx = x - oldX;
            float dy = y - oldY;
            // Define an upper area of 10% on the screen
            int upperArea = 0;

            // Zoom in/out if the touch move has been made in the upper
            if (y < upperArea) {
                z -= dx * TOUCH_SCALE / 2;

                // Rotate around the axis otherwise
            } else {
                xrot += dy * TOUCH_SCALE;
                yrot += dx * TOUCH_SCALE;
            }
        }

        // Remember the values
        oldX = x;
        oldY = y;

        // We handled the event
        return true;
    }

    /**
     * Called when the engine is destroyed. Do any necessary clean up because
     * at this point your renderer instance is now done for.
     */
    public void release() {
    }
}
