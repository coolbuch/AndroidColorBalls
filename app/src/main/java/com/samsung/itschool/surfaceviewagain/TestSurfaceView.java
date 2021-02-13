package com.samsung.itschool.surfaceviewagain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class TestSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder holder;
    int color = Color.RED;
    DrawThread thread;
    public void changeColor() {
        Random r = new Random();
        color = Color.rgb(r.nextInt(255),r.nextInt(255),r.nextInt(255));
    }
    class DrawThread extends Thread {
        @Override
        public void run() {
            super.run();
            // задание: реализовать плавную смену цвета
            // палитру выбирайте сами
            Canvas c = holder.lockCanvas();
            if (c != null) {
                c.drawColor(color);
            }
            holder.unlockCanvasAndPost(c);
        }
    }


    public TestSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
