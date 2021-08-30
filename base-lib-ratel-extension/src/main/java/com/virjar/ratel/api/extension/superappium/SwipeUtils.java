package com.virjar.ratel.api.extension.superappium;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import static android.view.MotionEvent.TOOL_TYPE_FINGER;

/**
 * <pre>
 *     author : xiaweizi
 *     class  : com.example.touch.GestureTouchUtils
 *     e-mail : 1012126908@qq.com
 *     time   : 2018/05/08
 *     desc   : 模拟手势的触摸滑动操作
 * </pre>
 * migrated from :https://github.com/xiaweizi/ScollDemo/blob/master/touch/src/main/java/com/example/touch/GestureTouchUtils.java
 */

public class SwipeUtils {

    public static final int HIGH = 10;
    public static final int NORMAL = 100;
    public static final int LOW = 1000;

    private static final long DEFAULT_DURATION = 1000;

    /**
     * 模拟手势滑动
     *
     * @param view   滑动的 view
     * @param startX 起始位置 x
     * @param startY 起始位置 y
     * @param endX   终点位置 x
     * @param endY   终点位置 y
     */
    public static void simulateScroll(ViewImage view, int startX, int startY, int endX, int endY) {
        simulateScroll(view, startX, startY, endX, endY, DEFAULT_DURATION);
    }


    /**
     * 模拟手势滑动
     *
     * @param view     滑动的 view
     * @param startX   起始位置 x
     * @param startY   起始位置 y
     * @param endX     终点位置 x
     * @param endY     终点位置 y
     * @param duration 滑动时长 单位：ms
     */
    public static void simulateScroll(ViewImage view, int startX, int startY, int endX, int endY, long duration) {
        simulateScroll(view, startX, startY, endX, endY, duration, NORMAL);
    }


    /**
     * 模拟手势滑动
     *
     * @param view     滑动的 view
     * @param startX   起始位置 x
     * @param startY   起始位置 y
     * @param endX     终点位置 x
     * @param endY     终点位置 y
     * @param duration 滑动时长 单位：ms
     * @param period   滑动周期
     *                 {@link #LOW} 慢
     *                 {@link #NORMAL} 正常
     *                 {@link #HIGH} 高
     */
    public static void simulateScroll(ViewImage view, int startX, int startY, int endX, int endY, long duration, int period) {
        dealSimulateScroll(view, startX, startY, endX, endY, duration, period);
    }

    /**
     * 模拟手势滑动
     *
     * @param activity 当前的 activity
     * @param startX   起始位置 x
     * @param startY   起始位置 y
     * @param endX     终点位置 x
     * @param endY     终点位置 y
     * @param duration 滑动时长 单位 ms
     * @param period   滑动周期
     *                 {@link #LOW} 慢
     *                 {@link #NORMAL} 正常
     *                 {@link #HIGH} 高
     */
    public static void simulateScroll(ViewImage activity, float startX, float startY, float endX, float endY, long duration, int period) {
        dealSimulateScroll(activity, startX, startY, endX, endY, duration, period);
    }

    private static boolean isScrolling = false;

    private static void dealSimulateScroll(ViewImage object, float startX, float startY, float endX, float endY, long duration, int period) {
        if (isScrolling) {
            Log.i(SuperAppium.TAG, "scroll task is running..");
            return;
        }
        isScrolling = true;
        Handler handler = new ViewHandler(object);

//        //重置相对偏移
//        int[] loca = new int[2];
//        object.rootViewImage().getOriginView().getLocationOnScreen(loca);
//
//        startX -= loca[0];
//        endX -= loca[0];
//
//        startY -= loca[1];
//        endY -= loca[1];


        object.dispatchPointerEvent(genFingerEvent(MotionEvent.ACTION_DOWN, startX, startY));
        GestureBean bean = new GestureBean(startX, startY, endX, endY, duration, period);
        Message.obtain(handler, 1, bean).sendToTarget();
    }


    private static MotionEvent genFingerEvent(int action, float x, float y) {
        //Log.i("HD_HOOK", "genFingerEvent action: " + action + " point:(" + x + " , " + y + ")");
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
        pointerCoords.x = x;
        pointerCoords.y = y;
        MotionEvent.PointerProperties pointerProperties = new MotionEvent.PointerProperties();
        pointerProperties.id = 0;
        pointerProperties.toolType = TOOL_TYPE_FINGER;
        MotionEvent.PointerProperties[] pointerPropertiesArray = new MotionEvent.PointerProperties[]{pointerProperties};
        MotionEvent.PointerCoords[] pointerCoordsArray = new MotionEvent.PointerCoords[]{pointerCoords};
        return MotionEvent.obtain(
                downTime, eventTime, action,
                1, pointerPropertiesArray, pointerCoordsArray,
                0, 0, 0, 0, 4, 0, 0x1002, 0);

    }

    static class ViewHandler extends Handler {
        ViewImage mView;

        ViewHandler(ViewImage activity) {
            super(Looper.getMainLooper());
            mView = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            // 1.7 需要final
            final ViewImage theView = mView;

            if (theView == null) {
                Log.e(SuperAppium.TAG, "scroll view has bean destroyed");
                isScrolling = false;
                return;
            }

            final GestureBean bean = (GestureBean) msg.obj;
            long count = bean.count;
            if (count >= bean.totalCount) {
                theView.dispatchPointerEvent(genFingerEvent(MotionEvent.ACTION_MOVE, bean.endX, bean.endY));
                new Handler(Looper.myLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                theView.dispatchPointerEvent(genFingerEvent(MotionEvent.ACTION_UP, bean.endX, bean.endY));
                            }
                        }
                );
                isScrolling = false;

            } else {
                float x = bean.startX + bean.ratioX * count;
                float y = bean.startY + bean.ratioY * count;
                boolean upEarly = false;
                if (x < 5) {
                    x = 5;
                    upEarly = true;
                }
                if (y < 5) {
                    y = 5;
                    upEarly = true;
                }
                if (x > theView.rootViewImage().getOriginView().getWidth() - 5) {
                    x = theView.rootViewImage().getOriginView().getWidth() - 5;
                    upEarly = true;
                }
                if (y > theView.rootViewImage().getOriginView().getHeight() - 5) {
                    y = theView.rootViewImage().getOriginView().getHeight() - 5;
                    upEarly = true;
                }

                if (upEarly) {
                    theView.dispatchPointerEvent(genFingerEvent(MotionEvent.ACTION_MOVE, x, y));
                    final float finalX = x;
                    final float finalY = y;
                    new Handler(Looper.myLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    theView.dispatchPointerEvent(genFingerEvent(MotionEvent.ACTION_UP, finalX, finalY));
                                }
                            }
                    );
                    isScrolling = false;
                    return;
                }

                theView.dispatchPointerEvent(genFingerEvent(MotionEvent.ACTION_MOVE, bean.startX + bean.ratioX * count, bean.startY + bean.ratioY * count));
                bean.count++;
                Message message = new Message();
                message.obj = bean;
                sendMessageDelayed(message, bean.period);
            }
        }
    }

    static class GestureBean {

        /**
         * 起始位置 X
         */
        float startX;
        /**
         * 起始位置 Y
         */
        float startY;
        /**
         * 终点位置 X
         */
        float endX;
        /**
         * 终点位置 Y
         */
        float endY;
        /**
         * 每个周期 x 移动的位置
         */
        float ratioX;
        /**
         * 每个周期 y 移动的位置
         */
        float ratioY;
        /**
         * 总共周期
         */
        long totalCount;
        /**
         * 当前周期
         */
        long count = 0;
        int period = NORMAL;

        GestureBean(float startX, float startY, float endX, float endY, long duration, int speed) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.period = speed;
            totalCount = duration / speed;
            ratioX = (endX - startX) / totalCount;
            ratioY = (endY - startY) / totalCount;
        }
    }

}
