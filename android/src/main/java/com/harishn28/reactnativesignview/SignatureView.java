package com.harishn28.reactnativesignview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class SignatureView extends View {

    private Paint paint;
    private Path currentPath;
    private ArrayList<Path> paths = new ArrayList<Path>();
    private SignViewCallbacks signViewCallbacks;
    int vl, vr, vt, vb;

    public SignatureView(Context context) {
        super(context);
        commonInit();
    }

    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        commonInit();
    }

    public SignatureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        commonInit();
    }

    private void commonInit(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0xff, 0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
    }

    public void setSignViewCallbacks(SignViewCallbacks signViewCallbacks){
        this.signViewCallbacks = signViewCallbacks;
    }

    public void setSignatureColor(int color){
        this.paint.setColor(color);
        invalidate();
    }


    public void setStrokeWidth(int strokeWidth){
        this.paint.setStrokeWidth(strokeWidth);
        invalidate();
    }

    public void clearSignature(){
        this.paths = new ArrayList<>();
        this.invalidate();
        this.updateSignAvailability();
    }

    public String getSignature (){
        if(paths.size() > 0){
            Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);

            this.draw(c);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            return encoded;
        } else{
            return null;
        }
    }

    /**
     * Used to inform the callback users that something has changed in canvas.
     *
     * THis methods should be called whenever there is a signature available/Modified
     * i.e., it should be called on every TouchUp/TouchCanceled event.
     *
     * It should also be called when user clears the signature.
     */
    private void updateSignAvailability(){
        if(signViewCallbacks != null){
            signViewCallbacks.onSignAvailable(getContext(), getId(), getSignature());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float ex = event.getX();
        float ey = event.getY();


        int action = event.getAction();


        if(action == MotionEvent.ACTION_DOWN){
            currentPath = new Path();
            currentPath.setLastPoint(event.getX(), event.getY());
            paths.add(currentPath);
        } else if(action == MotionEvent.ACTION_UP){
            if(ex > vl && ex < vr && ey > vt && ey < vb){
                RectF rec = new RectF();
                currentPath.computeBounds(rec, true);

                if(Math.abs(rec.right - rec.left) <= 1){
                    currentPath.addCircle(event.getX(), event.getY(), 5, Path.Direction.CW);
                }

                this.updateSignAvailability();
            }

        }
        else if(action == MotionEvent.ACTION_MOVE){
            if(ex > vl && ex < vr && ey > vt && ey < vb){
                currentPath.lineTo(event.getX(), event.getY());
            }
        }

        invalidate();
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        vl = 0;
        vr = right - left;
        vt = 0;
        vb = bottom - top;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(int i=0; i<paths.size(); i+=1){
            canvas.drawPath(paths.get(i), paint);
        }
    }
}