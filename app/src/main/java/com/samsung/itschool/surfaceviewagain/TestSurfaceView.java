package com.samsung.itschool.surfaceviewagain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class TestSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    final String TAG = "mytag";
    int curX = 500, curY = 500;
    int motionX, motionY;
    float axisX, axisY;
    SurfaceHolder holder;
    int color = Color.RED;
    DrawThread thread;
    ArrayList<Circle> circles = new ArrayList<>();
    Display display ;
    Point size = new Point();


    class DrawThread extends Thread {

        int currentColor;
        @Override
        public void run() {
            super.run();
            Random r = new Random();

            display.getSize(size);
            int width = size.x ;
            int height = size.y - 270;
            for(int i = 0; i < 4; i++)
            {
                circles.add(new Circle(r.nextInt(width), r.nextInt(height), 30));
                circles.get(circles.size() - 1).setScreenResolution(width,height);
            }

            // задание: реализовать плавную смену цвета
            // палитру выбирайте сами

            Paint p = new Paint();

            while (true)
            {
                Canvas c = holder.lockCanvas();
                if (c != null) {

                    c.drawColor(Color.BLACK);
                    int winColor = circles.get(0).curColor;
                    int counter = 0;
                    for (Circle ci : circles)
                    {
                        p.setStrokeWidth(1f);
                        p.setColor(ci.curColor);
                        c.drawCircle(ci.x, ci.y, ci.radius, p);
                        ci.move();
                        p.setStrokeWidth(0.7f);
                        c.drawLine(ci.x, ci.y, ci.x + ci.movementVec[0] * 30, ci.y + ci.movementVec[1] * 30, p);
                        if (ci.curColor == winColor)
                            counter++;
                    }
                    if (counter == circles.size())
                    {
                        c.drawColor(Color.BLACK);
                        p.setColor(Color.WHITE);
                        p.setTextSize(100);
                        c.drawText("ЭТО ПОБЕДА!",30, 200, p);
                        holder.unlockCanvasAndPost(c);
                        break;
                    }
                    p.setColor(Color.RED);
                    c.drawRect(curX, curY , curX + 100, curY + 100, p);
                    holder.unlockCanvasAndPost(c);
                    try {
                        this.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }



    }

    class Circle
    {
        int x, y;
        int radius;
        int curColor;
        int [] palette = new int[7];
        int [] movementVec = new int[2];
        int screenW, screenH;
        public Circle(int x, int y, int radius) {
            palette[0] = Color.GREEN;
            palette[1] = Color.BLUE;
            palette[2] = Color.CYAN;
            palette[3] = Color.RED;
            palette[4] = Color.GRAY;
            palette[5] = Color.YELLOW;
            palette[6] = Color.MAGENTA;
            this.x = x;
            this.y = y;
            this.radius = radius;
            curColor = palette[new Random().nextInt(7)];
            movementVec[0] = new Random().nextInt(15) - 15;
            movementVec[1] = new Random().nextInt(15) - 15;

        }

        void setScreenResolution(int w, int h)
        {
            screenW = w;
            screenH = h;
        }

        void nextColor()
        {
            if (curColor == palette[6])
            {
                curColor = palette[0];
                return;
            }
            for (int i = 0; i < 6;  i++)
            {
                if (curColor == palette[i])
                {
                    curColor = palette[i + 1];
                    return;
                }
            }
        }

        void move()
        {
            x += movementVec[0];
            y += movementVec[1];
            detectObstacle();
            //Log.d(TAG, "move: " + screenW + ' ' + screenH + " Coords: " + x + ' ' + y);
        }

        void detectObstacle()
        {
            if (x < 0 || x > screenW)
            {
                movementVec[0] = -movementVec[0];
            }
            if (y < 0 || y > screenH)
            {
                movementVec[1] = -movementVec[1];
            }
            for (Circle c: circles)
            {
                if (c != this)
                {
                    if (x + radius/2 - 2 >= c.x - radius/2 - 2 && x - radius/2 -2 <= c.x + radius/2 - 2 &&
                            y + radius/2 - 2 >= c.y - radius/2 - 2 && y - radius/2 -2 <= c.y + radius/2 - 2)
                    {
                        movementVec[1] = -movementVec[1];
                        movementVec[0] = -movementVec[0];
                    }
                }
            }
            if (x + radius/2 >= curX && x - radius/2 <= curX + 100 && y + radius/2 >= curY && y - radius/2 <= curY + 100)
            {
                movementVec[1] = -movementVec[1];
                movementVec[0] = -movementVec[0];
                nextColor();
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            try {
                axisX = event.getAxisValue(0) - event.getHistoricalAxisValue(MotionEvent.AXIS_X, 0);
                axisY = event.getAxisValue(1) - event.getHistoricalAxisValue(MotionEvent.AXIS_Y, 0);
            }
            catch (IllegalArgumentException e){}
            curX = curX + (int)axisX;
            curY = curY + (int)axisY;
            Log.d(TAG, "onTouchEvent: " + axisX);
        }
        return true;
    }

    public TestSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        // этот класс является обработчиком событий с поверхностью
        getHolder().addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // запустить поток отрисовки

        holder = surfaceHolder;
        thread = new DrawThread();
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // перезапустить поток
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // остановить поток
    }
}
