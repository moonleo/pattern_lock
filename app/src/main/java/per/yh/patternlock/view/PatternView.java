package per.yh.patternlock.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
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

    private static final int mPointNum = 3;//图形行列图案的个数

    private int mScreenWidth, mScreenHeight;//屏幕宽高
    private Point[][] mPoints = new Point[mPointNum][mPointNum];//图案
    private Paint mPaint = new Paint();//画笔

    private Bitmap mNormalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_normal);//正常图片
    private Bitmap mPressedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_pressed);//按下图片
    private Bitmap mErrorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_error);//错误图片

    private int mBitmapRadius = mNormalBitmap.getWidth() >> 1;//圆形图片半径

    private Bitmap mPressedLine = BitmapFactory.decodeResource(getResources(), R.drawable.line_pressed);//按下连线
    private Bitmap mErrorLine = BitmapFactory.decodeResource(getResources(), R.drawable.line_error);//错误连线

    private boolean isPatternInit;//是否已经初始化
    private boolean isBegin;//是否已经开始绘制图案
    private boolean isFinished;//是否已经结束绘制图案

    private float mScreenX, mScreenY;//手指滑动时屏幕上点坐标

    private Point mLastSelectedPoint;//上一次选中的点
    private List<Point> mSelectedPoints = new ArrayList<>();//已经选择的点

    public static final int MIN_POINT_NUM = 4;//绘制所需的最少点数

    private OnPatternChangedListener onPatternChangedListener;//画图监听器

    private String password;//保存设置的密码

    public PatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        setPassword("");
        Log.d(TAG, "gouzao fangfa");
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!isPatternInit) {
            initPoints();
            isPatternInit = true;
        }

        //画九宫格
        drawPattern(canvas);

        //画点与点之间的连线
        drawLinePoint2Point(canvas);

        //如果绘制没有结束
        if(isBegin) {
            //画最后一个点与鼠标之间的连线
            drawLinePoint2Mouse(canvas);
        }
    }

    /**
     * 画九宫格
     * @param canvas 画布
     */
    private void drawPattern(Canvas canvas) {
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
     * 画最后一个点和鼠标之间的连线
     * @param canvas 画布
     */
    private void drawLinePoint2Mouse(Canvas canvas) {
        if(null != mLastSelectedPoint)
            drawLine(canvas, mLastSelectedPoint, new Point(mScreenX, mScreenY));
    }

    /**
     * 画点到点之间的线
     * @param canvas 画布
     */
    private void drawLinePoint2Point(Canvas canvas) {
        int size = mSelectedPoints.size();
        Point start = null;
        if(size > 0) {
            start = mSelectedPoints.get(0);
            Point end;
            for (int i = 1; i < mSelectedPoints.size(); i++) {
                end = mSelectedPoints.get(i);
                drawLine(canvas, start, end);
                start = end;
            }
        }
        mLastSelectedPoint = start;
    }

    /**
     * 画线
     * @param canvas 画布
     * @param start 起始点
     * @param end 结束点
     */
    private void drawLine(Canvas canvas, Point start, Point end) {
        Matrix matrix = new Matrix();
        double dis = start.distance(end.getX(), end.getY());
        float degree = (float)start.degree(end.getX(), end.getY());
        canvas.rotate(degree, start.getX(), start.getY());//旋转画布
        matrix.setScale((float)dis/mPressedLine.getWidth(), 1);//拉伸图片
        matrix.postTranslate(start.getX(), start.getY());
        if(start.getStatus().equals(Status.pressed))
            canvas.drawBitmap(mPressedLine, matrix, mPaint);
        else
            canvas.drawBitmap(mErrorLine, matrix, mPaint);
        canvas.rotate(-degree, start.getX(), start.getY());//将画布旋转回来
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
                //设置点的坐标和索引
                mPoints[i][j] = new Point(paddingLeft + j * patternSpace,
                        paddingTop + i * patternSpace);
                mPoints[i][j].setIndex(i * mPointNum + j);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        //获取屏幕上的坐标
        mScreenX = event.getX();
        mScreenY = event.getY();
        Point p;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                reset();//重置所有点
                if(null != onPatternChangedListener) {
                    onPatternChangedListener.patternStart(true);
                }
                if(!isFinished) {
                    for(int i=0; i<mPointNum; i++) {
                        for(int j=0; j<mPointNum; j++) {
                            p = mPoints[i][j];
                            if(!p.getStatus().equals(Status.pressed)) {
                                if(p.intersect(mScreenX, mScreenY, mNormalBitmap.getWidth() >> 1)) {
                                    if(!mSelectedPoints.contains(p)) {
                                        p.setStatus(Status.pressed);
                                        mSelectedPoints.add(p);
                                        mLastSelectedPoint = p;
                                        isBegin = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                for(int i=0; i<mPointNum; i++) {
                    for(int j=0; j<mPointNum; j++) {
                        p = mPoints[i][j];
                        if(!p.getStatus().equals(Status.pressed)) {
                            if(p.intersect(mScreenX, mScreenY, mBitmapRadius)) {
                                if(p.getStatus().equals(Status.normal)) {
                                    //两点之间的点
                                    if(null != mLastSelectedPoint) {
                                        int sumIndex = mLastSelectedPoint.getIndex() + p.getIndex();
                                        Point mid = mPoints[(sumIndex>>1)/mPointNum][(sumIndex>>1)%mPointNum];
                                        if(mid.intersect((mLastSelectedPoint.getX()+p.getX())/2,
                                                (mLastSelectedPoint.getY()+p.getY())/2, mBitmapRadius)) {
                                            mid.setStatus(Status.pressed);
                                            mSelectedPoints.add(mid);
                                        }
                                    }
                                    p.setStatus(Status.pressed);
                                    mSelectedPoints.add(p);
                                    mLastSelectedPoint = p;
                                    isBegin = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //停止绘制
                isBegin = false;
                isFinished = true;
                //停止绘制，检查点数是否满足要求
                checkPattern();
        }
        postInvalidate();
        return true;
    }

    /**
     * 停止绘制，检查点数是否满足要求
     */
    private void checkPattern() {
        if(mSelectedPoints.size() <= 1) {
            reset();
        }
        //选择的点数小于所需最少点数
        else if(mSelectedPoints.size() > 1 && mSelectedPoints.size() < MIN_POINT_NUM) {
            //将已选择点的状态都设为错误
            setSelectPoints2Error();
            if(null != onPatternChangedListener) {
                onPatternChangedListener.patternChanged(null);
            }
        } else {
            if(null != onPatternChangedListener) {
                String pw = "";
                for(Point p: mSelectedPoints) {
                    pw += p.getIndex();
                }
                if(TextUtils.isEmpty(getPassword())) {
                    setPassword(pw);
                    onPatternChangedListener.passwordSetted(pw);
                } else {
                    if(!pw.equals(getPassword()))
                        setSelectPoints2Error();
                    onPatternChangedListener.patternChanged(pw);
                }

            }
        }
    }

    /**
     * 将选中的点状态置为error
     */
    private void setSelectPoints2Error() {
        for(Point p: mSelectedPoints) {
            p.setStatus(Status.error);
        }
    }

    /**
     * 重置已选点，重置图形
     */
    private void reset() {
        for(Point p: mSelectedPoints) {
            p.setStatus(Status.normal);
        }
        mSelectedPoints.clear();
    }

    public void resetPoints() {
        reset();
        setPassword("");
        onPatternChangedListener.passwordSetted(getPassword());
        postInvalidate();
    }

    public void setOnPatternChangedListener(OnPatternChangedListener onPatternChangedListener) {
        if(null != onPatternChangedListener) {
            this.onPatternChangedListener = onPatternChangedListener;
        }
    }
}

/**
 * 九宫格中的点
 */
class Point {
    private float x;//点中心点的x坐标
    private float y;//点中心点的y坐标

    private Status status;//点的状态

    private int index;//点的索引

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
        this.status = Status.normal;
    }

    public float getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public float getY() {
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
     * 屏幕上某点与该点之间的夹角
     * @param screenX x坐标
     * @param screenY y坐标
     * @return 夹角
     */
    public double degree(float screenX, float screenY) {
        float offsetX = screenX - x;
        float offsetY = screenY - y;
        double dis = distance(screenX, screenY);
        double rad;
        if(offsetX >= 0) {
            rad = Math.asin((offsetY) / (dis));
        } else {
            rad = - Math.asin((offsetY)/(dis)) + Math.PI;
        }
        return rad / Math.PI * 180;
    }

    /**
     * 屏幕上某点与该点之间的距离
     * @param screenX x坐标
     * @param screenY y坐标
     * @return 距离
     */
    public double distance(float screenX, float screenY) {
        return Math.sqrt(Math.pow(Math.abs(screenX - x), 2) + Math.pow(Math.abs(screenY - y), 2));
    }

    /**
     * 判断屏幕上的点是否在以该点为圆心的圆内
     * @param screenX x坐标
     * @param screenY y坐标
     * @param radius 半径
     * @return true表示在范围内，否则不在
     */
    public boolean intersect(float screenX, float screenY, float radius) {
        return radius > distance(screenX, screenY);
    }
}

/**
 * 点的状态
 */
enum Status {
    normal, pressed, error
}
