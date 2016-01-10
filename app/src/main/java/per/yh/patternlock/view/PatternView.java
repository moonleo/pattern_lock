package per.yh.patternlock.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import per.yh.patternlock.R;

/**
 * Created by MoonLeo on 2016/1/2.
 * 九宫格图形View
 */
public class PatternView extends View {
    private static final String TAG = PatternView.class.getSimpleName();

    private int mScreenWidth, mScreenHeight;//屏幕宽高
    private int mPointNum = 3;//图形行列图案的个数
    private Point[][] mPoints = new Point[mPointNum][mPointNum];//图案
    private Paint mPaint = new Paint();//画笔

    private Bitmap mNormalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_normal);//正常图片
    private Bitmap mPressedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_pressed);//按下图片
    private Bitmap mErrorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_error);//错误图片

    boolean isPatternInit;//是否已经初始化

    boolean isBegin;//是否已经开始绘制图案
    private Point lastPoint;

    private List<Point> mSelectedPoints = new ArrayList<>();//已经选择的点

    public PatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!isPatternInit) {
            initPoints();
            isPatternInit = true;
        }

        //画九宫格
        Point p;
        int radius = mNormalBitmap.getWidth() >> 1;
        for(int i=0; i<mPointNum; i++) {
            for(int j=0; j<mPointNum; j++) {
                p = mPoints[i][j];
                if (p.getStatus() == Status.normal) {
                    canvas.drawBitmap(mNormalBitmap, p.getX() - radius, p.getY() - radius, mPaint);
                } else if (p.getStatus() == Status.pressed) {
                    canvas.drawBitmap(mPressedBitmap, p.getX() - radius, p.getY() - radius, mPaint);
                } else if (p.getStatus() == Status.error) {
                    canvas.drawBitmap(mErrorBitmap, p.getX() - radius, p.getY() - radius, mPaint);
                }
            }
        }

    }

    /**
     * 初始化各点
     */
    private void initPoints() {
        int patternSpace;//各点之间距离
        int paddingLeft, paddingTop;//九宫格左边距、上边距
        if(mScreenWidth < mScreenHeight) {
            patternSpace = mScreenWidth / (mPointNum + 1);
            paddingLeft = patternSpace;
            paddingTop = (mScreenHeight - (mPointNum - 1) * patternSpace) >> 1;
        } else {
            patternSpace = mScreenHeight / (mPointNum + 1);
            paddingTop = patternSpace;
            paddingLeft = (mScreenWidth - (mPointNum - 1) * patternSpace) >> 1;
        }
        for(int i=0; i<mPointNum; i++) {
            for(int j=0; j<mPointNum; j++) {
                mPoints[i][j] = new Point(paddingLeft + j * patternSpace,
                        paddingTop + i * patternSpace);
                mPoints[i][j].setIndex(i * mPointNum + j + 1);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Point p;
                for(int i=0; i<mPointNum; i++) {
                    for(int j=0; j<mPointNum; j++) {
                        p = mPoints[i][j];
                        if(p.intersect(x, y, mNormalBitmap.getWidth() >> 1)) {
                            p.setStatus(Status.pressed);
                            mSelectedPoints.add(p);
                            isBegin = true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                isBegin = false;//停止绘制
                break;
        }
        postInvalidate();
        return true;
    }
}

/**
 * 九宫格中的点
 */
class Point {
    private int x;//点中心点的x坐标
    private int y;//点中心点的y坐标

    private Status status;//点的状态

    private int index;//点的索引

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        this.status = Status.normal;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * 判断屏幕上的点是否在以该点为圆心的园内
     * @param screenX x坐标
     * @param screenY y坐标
     * @param radius 半径
     * @return true表示在范围内，否则不在
     */
    public boolean intersect(float screenX, float screenY, float radius) {
        float disX = Math.abs(screenX - x);
        float disY = Math.abs(screenY - y);
        double dis = Math.sqrt(Math.pow(disX, 2) + Math.pow(disY, 2));
        return radius > dis ? true : false;
    }
}

/**
 * 点的状态
 */
enum Status {
    normal, pressed, error
}
