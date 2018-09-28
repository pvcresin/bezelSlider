package com.resin.bezelslider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder surfaceHolder;
    Thread thread;

    int width, height, frameCount = 0;
    boolean isAlive = false;

    public MySurfaceView(Context context) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isAlive = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isAlive = true;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                paint = new Paint();

                while(isAlive){
                    try{
                        canvas = surfaceHolder.lockCanvas();

                        frameCount++;

                        if (frameCount == 1) setup();

                        draw();

                        surfaceHolder.unlockCanvasAndPost(canvas);

                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    Canvas canvas;
    Paint paint;

    Paint fill(int r, int g, int b, int a) {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.argb(a, r, g, b));
        return paint;
    }
    Paint fill(int r, int g, int b) {
        return fill(r, g, b, 255);
    }
    Paint fill(int gray, int a) {
        return fill(gray, gray, gray, a);
    }
    Paint fill(int gray) {
        return fill(gray, 255);
    }
    Paint stroke(int r, int g, int b) {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.argb(255, r, g, b));
        return paint;
    }
    Paint stroke(int gray) {
        return stroke(gray, gray, gray);
    }
    Paint noFill() {
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    void ellipse(float x, float y, float w, float h) {
        canvas.drawOval(x - w/2, y - h/2, x + w/2, y + h/2, paint);
    }

    void line(float x1, float y1, float x2, float y2){
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawLine(x1, y1, x2, y2, paint);
    }

    void background(int c) {
        fill(c);
        canvas.drawRect(0, 0, width, height, paint);
    }

    void println(Object o) {
        System.out.println(o);
    }

    void text(String s, float x, float y) {
        paint.setTextSize(50);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText(s, x, y, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        touchCount = event.getPointerCount();

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                println(touchCount + " down");
                break;

            case MotionEvent.ACTION_UP:
                touchCount = 0;
//                println(touchCount + " up");
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
//                println(touchCount + " p down");
                if (touchCount == 3) {
                    if (s.overDrag) s.mouseReleased();
                    else s.mousePressed();
                }
                else if (touchCount == 4) debug = !debug;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                touchCount--;
//                println(touchCount + " p up");
                break;

            case MotionEvent.ACTION_MOVE:
//                println(touchCount + " move");
                break;
        }

        if (touchCount == 2) {
            mx[0] = event.getX(0);
            my[0] = event.getY(0);
            mx[1] = event.getX(1);
            my[1] = event.getY(1);
            s.mouseDragged();
        } else if (touchCount <= 1) {
            s.mouseReleased();
        }

        return true;
    }

    ////
    int touchCount = 0;

    Slider s;
    Point t1, t2;
    boolean debug = false;
    float[] mx, my;
    float watchSize;

    void setup() {
//        size(600, 600);
//        width = 1000;
        height = width;
        mx = new float [2];
        my = new float [2];
        watchSize = (float) (width * 0.7);
        t1 = new Point(0, 0);
        t2 = new Point(0, 0);
        float len = watchSize * (float)0.5;
        s = new Slider(width/2 - len/2, height/2 + height/6, width/2 + len/2, height/2 + height/6);
    }

    void draw() {
        background(120);

        fill(255);
        ellipse(width/2, height/2, watchSize, watchSize);
        ellipse(width/2, height/2, 3, 3);

        s.drawBar();
        s.drawSlider();

        Point cross = lines2intersect(points2line(t1, t2), points2line(s.p1, s.p2));

        stroke(255, 0, 0);
        if (!s.overDrag) {
            line(t1.x, t1.y, t2.x, t2.y);
        } else {
            float tmpCx = s.prog.x + s.tmpDx, tmpCy = s.prog.y;
            line(t1.x, t1.y, tmpCx, tmpCy);
            line(tmpCx, tmpCy, t2.x, t2.y);
        }
        stroke(0);
        noFill();

        if (touchCount >= 2) {
            Vector ct1 = new Vector(width / 2 - mx[0], height / 2 - my[0]);
            float x1 = ct1.normalize().x * watchSize / 2;
            float y1 = ct1.normalize().y * watchSize / 2;

            Vector ct2 = new Vector(width / 2 - mx[1], height / 2 - my[1]);
            float x2 = ct2.normalize().x * watchSize / 2;
            float y2 = ct2.normalize().y * watchSize / 2;

            t1.update(width / 2 - x1, height / 2 - y1);
            t2.update(width / 2 - x2, height / 2 - y2);

            if (debug) {
                float tr = 30;
                ellipse(mx[0], my[0], tr, tr);
                ellipse(mx[1], my[1], tr, tr);
                line(width / 2, height / 2, mx[0], my[0]);
                line(width / 2, height / 2, mx[1], my[1]);
                ellipse(width / 2 - x1, height / 2 - y1, 10, 10);
                ellipse(width / 2 - x2, height / 2 - y2, 10, 10);
                ellipse(cross.x, cross.y, 10, 10);  // intersection
            }
        }
    }

    // move(default) -> press (2 : finger)
    // press -> click (3)
    // drag -> move (2)
    // release -> release (0)

    class Slider {
        float cx, cy, r = 50, tmpDx, tmpDy;
        float progress = 50;
        boolean online = false, selected = false, overDrag = false;
        //        color notC = color(120, 125), onC = color(255, 255, 0, 125), selectC = color(255, 0, 0, 125);
        Point p1, p2, prog;

        Slider(float x1, float y1, float x2, float y2) {
            p1 = new Point(x1, y1);
            p2 = new Point(x2, y2);
            float m = progress, n = 100 - progress;
            cx = (n * x1 + m * x2) / (m + n);
            cy = (n * y1 + m * y2) / (m + n);
            prog = new Point(cx, cy);
        }

        void drawBar() {
            stroke(0);
            line(p1.x, p1.y, p2.x, p2.y);
        }
        void drawSlider() {
            float d = distLineFromPoint(points2line(t1, t2), prog);

            online = d < r/2;

            if (selected) fill(255, 0, 0, 125);
            else if (online) fill(255, 255, 0, 125);
            else fill(120, 125);

            ellipse(prog.x, prog.y, r, r);
            fill(0);
            text("" + progress, prog.x - r/2, prog.y - r);
        }

        void mousePressed() {
            if (online) {
                Point cross = lines2intersect(points2line(t1, t2), points2line(p1, p2));
                tmpDx = cross.x - prog.x;
                tmpDy = cross.y = prog.y;
                selected = true;
            } else {
                selected = false;
            }
        }

        void mouseDragged() {
            if (selected) {
                Point cross = getIntersect();

                if (cross.x - tmpDx <= p1.x) {
                    updateProgress(0);
                    overDrag = true;
                } else if (p2.x <= cross.x - tmpDx) {
                    updateProgress(100);
                    overDrag = true;
                } else {
                    overDrag = false;
                    updateProgress(cross.x - tmpDx, cross.y);
                }

            }
        }

        void mouseReleased() {
            selected = false;
            overDrag = false;
            tmpDx = 0;
            tmpDy = 0;
        }

        void updateProgress(float progress) {
            this.progress = progress;
            float m = progress, n = 100 - progress;
            cx = (n * p1.x + m * p2.x) / (m + n);
            cy = (n * p1.y + m * p2.y) / (m + n);
            prog.update(cx, cy);
        }

        void updateProgress(float cx, float cy) {
            prog.update(cx, cy);
            progress = 100 * (cx - p1.x) / (p2.x - p1.x);
        }


        Point getIntersect() {
            return lines2intersect(points2line(t1, t2), points2line(p1, p2));
        }
    }

    class Vector {
        float x, y, l;
        Vector(float x, float y) {
            this.x = x;
            this.y = y;
            this.l = (float)(Math.sqrt(x * x + y * y));
        }

        Vector normalize() {
            return new Vector(x/l, y/l);
        }
    }

    class Point {
        float x, y;
        Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
        void update(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public class Line {
        float a, b, c;
        Line(float a, float b, float c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    float distLineFromPoint(Line line, Point p) {
        float a = line.a, b = line.b, c = line.c;
        return (float)(Math.abs( (a * p.x + b * p.y + c) / Math.sqrt(a * a + b * b) ));
    }

    Line points2line(Point p1, Point p2) {
        float a = (p2.y - p1.y) / (p2.x - p1.x);
        float b = -1;
        float c = p1.y - (a * p1.x);
        return new Line(-a, - b, -c); // ax + by + c = 0
    }

    Point lines2intersect(Line line1, Line line2) {
        float a1 = line1.a, c1 = line1.c;
        float a2 = line2.a, c2 = line2.c;
        return new Point((c1 - c2) / (a2 - a1), (a1 * c2 - a2 * c1) / (a2 - a1));
    }
}