package com.interiordesign.interiordesign;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class InteriorImageActivity extends AppCompatActivity implements View.OnTouchListener {

    ImageView chandelier;
    FloatingActionButton fab;
    private static final String TAG = "Touch";
    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    Matrix savedMatrix2 = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    float lastEvent[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagedrag);
        chandelier = (ImageView) findViewById(R.id.imageView);
        /*chandelier.setVisibility(View.INVISIBLE);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                chandelier.setVisibility(View.VISIBLE);
            }
        });*/

        chandelier.setOnTouchListener(this);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        int rotation = 25;
        // Dump touch event to log
        dumpEvent(event);

  /////      fixing();
       ///// setImageMatrix(savedMatrix2);

        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                break;



            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_UP:

                mode = NONE;
                Log.d(TAG, "mode=NONE");
                savedMatrix.set(matrix);
                rotation(event);
             //   matrix.postRotate(90);

                break;

            case MotionEvent.ACTION_SCROLL:
                mode = NONE;
                Log.d(TAG,"mode=button rotate");
                savedMatrix.set(matrix);
                matrix.postRotate(90);
                break;


            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    // ...
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x,
                            event.getY() - start.y);
                }
                else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        fixing();
        view.setImageMatrix(matrix);
        return true; // indicate event was handled
    }



    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.d(TAG, sb.toString());
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event)
    {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            float z = x * x + y * y;
            return (float) Math.sqrt((double) z);
        }
        catch(IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        return 1;
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void fixing()
    {

        float[] value = new float[9];
        matrix.getValues(value);

        float[] savedValue = new float[9];
        savedMatrix2.getValues(savedValue);

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        /*int width = getWidth();
        int height = getHeight();*/

        Drawable d = getDrawable(R.drawable.chandelier_1);
        if (d == null)  return;
        int imageWidth = d.getIntrinsicWidth();
        int imageHeight = d.getIntrinsicHeight();
        int scaleWidth = (int) (imageWidth * value[0]);
        int scaleHeight = (int) (imageHeight * value[4]);

// don't let the image go outside
        if (value[2] > width-1)
            value[2] = width-10;
        else if (value[5] > height - 1)
            value[5] = height - 10;
        else if (value[2] < -(scaleWidth-1))
            value[2] = -(scaleWidth-10);
        else if (value[5] < -(scaleHeight-1))
            value[5] = -(scaleHeight-10);

        // maximum zoom ratio: MAx
        if (value[0] > ZOOM || value[4] > ZOOM){
            value[0] = ZOOM;
            value[4] = ZOOM;
            //value[2] = savedValue[2];
            //value[5] = savedValue[5];
        }

        matrix.setValues(value);
        savedMatrix2.set(matrix);
    }

    private float rotation(MotionEvent event) {
        try {
            double delta_x = (event.getX(0) - event.getX(1));
            double delta_y = (event.getY(0) - event.getY(1));
            double radians = Math.atan2(delta_y, delta_x);

            return (float) Math.toDegrees(radians);
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        return 1.0f;
    }
}

