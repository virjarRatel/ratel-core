package com.virjar.ratel.api.extension.superappium;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;

import com.virjar.ratel.api.extension.superappium.traversor.Collector;
import com.virjar.ratel.api.extension.superappium.traversor.Evaluator;
import com.virjar.ratel.api.extension.superappium.traversor.SuperAppiumDumper;
import com.virjar.ratel.api.extension.superappium.xmodel.LazyValueGetter;
import com.virjar.ratel.api.extension.superappium.xmodel.ValueGetters;
import com.virjar.ratel.api.extension.superappium.xpath.XpathParser;
import com.virjar.ratel.api.extension.superappium.xpath.model.XNode;
import com.virjar.ratel.api.extension.superappium.xpath.model.XNodes;
import com.virjar.ratel.api.inspect.Lists;
import com.virjar.ratel.api.rposed.RposedHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static android.view.MotionEvent.TOOL_TYPE_FINGER;

public class ViewImage {
    private View originView;
    private Map<String, LazyValueGetter> attributes;
    private ViewImage parent = null;
    private LazyValueGetter<String> type;
    private LazyValueGetter<String> text;
    private int indexOfParent = -1;
    private ViewImages allElementsCache = null;

    public ViewImage(View originView) {
        this.originView = originView;
        attributes = ValueGetters.valueGetters(this);
        type = attrName(SuperAppium.baseClassName);
        text = attrName(SuperAppium.text);
    }

    public String getType() {
        return type.get();
    }

    public String getText() {
        return text.get();
    }

    @SuppressWarnings("unchecked")
    private <T> LazyValueGetter<T> attrName(String attrName) {
        return attributes.get(attrName);
    }

    public Collection<String> attributeKeys() {
        return attributes.keySet();
    }

    @SuppressWarnings("unchecked")
    public <T> T attribute(String key) {
        LazyValueGetter valueGetter = attributes.get(key);
        if (valueGetter == null) {
            return null;
        }
        return (T) valueGetter.get();
    }

    private int[] location = null;

    public int[] locationOnScreen() {
        if (location != null) {
            return location;
        }
        location = new int[2];
        originView.getLocationOnScreen(location);
        return location;
    }

    public int X() {
        return locationOnScreen()[0];
    }

    public int Y() {
        return locationOnScreen()[1];
    }

    public View getOriginView() {
        return originView;
    }

    private Integer theChildCount = null;

    public int childCount() {
        if (theChildCount != null) {
            return theChildCount;
        }
        if (!(originView instanceof ViewGroup)) {
            return 0;
        }
        ViewGroup viewGroup = (ViewGroup) originView;
        theChildCount = viewGroup.getChildCount();
        return theChildCount;
    }

    private ViewImage[] children;

    public ViewImage childAt(int index) {
        if (childCount() < 0) {
            throw new IllegalStateException("can not parse child node for none ViewGroup object!!");
        }
        if (children == null) {
            children = new ViewImage[childCount()];
        }
        ViewImage viewImage = children[index];
        if (viewImage != null) {
            return viewImage;
        }
        ViewGroup viewGroup = (ViewGroup) originView;
        viewImage = new ViewImage(viewGroup.getChildAt(index));
        viewImage.parent = this;
        viewImage.indexOfParent = index;
        children[index] = viewImage;
        return viewImage;
    }

    public Integer index() {
        return indexOfParent;
    }


    public List<ViewImage> parents() {
        List<ViewImage> ret = Lists.newArrayList();
        ViewImage parent = this.parent;
        while (parent != null) {
            ret.add(parent);
            parent = parent.parent;
        }
        return ret;
    }

    public List<ViewImage> children() {
        if (childCount() <= 0) {
            return Lists.newArrayList();
        }
        List<ViewImage> ret = new ArrayList<>(childCount());
        for (int i = 0; i < childCount(); i++) {
            ret.add(childAt(i));
        }
        return ret;
    }

    public ViewImages getAllElements() {
        if (allElementsCache == null) {
            allElementsCache = Collector.collect(new Evaluator.AllElements(), this);
        }
        return allElementsCache;
    }

    public ViewImage parentNode() {
        return parent;
    }

    public ViewImage parentNode(int n) {
        if (n == 1) {
            return parentNode();
        }
        return parentNode().parentNode(n - 1);
    }

    public ViewImage nextSibling() {
        if (parent == null) {
            //root
            return null;
        }
        int nextSiblingIndex = indexOfParent + 1;
        if (parent.childCount() > nextSiblingIndex) {
            return parent.childAt(nextSiblingIndex);
        }
        return null;
    }

    public ViewImage previousSibling() {
        if (parent == null) {
            //root
            return null;
        }
        int nextSiblingIndex = indexOfParent - 1;
        if (nextSiblingIndex < 0) {
            return null;
        }
        return parent.childAt(nextSiblingIndex);
    }

    public ViewImages siblings() {
        if (parent == null) {
            return new ViewImages();
        }
        int parentChildren = parent.childCount();
        ViewImages viewImages = new ViewImages(parentChildren - 1);
        for (int i = 0; i < parentChildren; i++) {
            ViewImage viewImage = parent.childAt(i);
            if (viewImage == this) {
                continue;
            }
            viewImages.add(viewImage);
        }
        return viewImages;
    }

    public String attributes() {
        JSONObject jsonObject = new JSONObject();
        for (String key : attributeKeys()) {
            try {
                jsonObject.put(key, (Object) attribute(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject.toString();
    }

    public ViewImage rootViewImage() {
        ViewImage parentViewImage = parentNode();
        if (parentViewImage == null) {
            return this;
        }
        return parentViewImage.rootViewImage();
    }

    public ViewImages xpath(String xpath) {
        return XpathParser.compileNoError(xpath).evaluateToElement(new XNodes(XNode.e(this)));
    }

    public String xpath2String(String xpath) {
        return XpathParser.compileNoError(xpath).evaluateToSingleString(new XNodes(XNode.e(this)));
    }

    public ViewImage xpath2One(String xpath) {
        ViewImages viewImages = xpath(xpath);
        if (viewImages.size() == 0) {
            return null;
        }
        return viewImages.get(0);
    }

    private boolean clickAdapterView(AdapterView parent, View mView) {
        final int position = parent.getPositionForView(mView);
        final long itemId = parent.getAdapter() != null
                ? parent.getAdapter().getItemId(position)
                : 0;
        if (position != AdapterView.INVALID_POSITION) {
            return parent.performItemClick(mView, position, itemId);
        }
        return false;
    }

    public boolean clickByXpath(String xpathExpression) {
        ViewImages viewImages = xpath(xpathExpression);
        if (viewImages.size() == 0) {
            return false;
        }
        return viewImages.get(0).click();
    }

    public boolean typeByXpath(String xpathExpression, String content) {
        ViewImages viewImages = xpath(xpathExpression);
        if (viewImages.size() == 0) {
            return false;
        }
        View originView = viewImages.get(0).getOriginView();
        if (!(originView instanceof EditText)) {
            return false;
        }
        EditText editText = (EditText) originView;
        editText.getText().clear();
        editText.setText(content);
        //触发焦点到下一个输入框
        editText.onEditorAction(EditorInfo.IME_ACTION_NEXT);
        return true;
    }

    public boolean click() {
//        if (originView.isClickable()) {
//            if (originView.performClick()) {
//                return true;
//            }
//        }
//        ViewImage parentViewImage = parentNode();
//        if (parentViewImage != null) {
//            View parentOriginView = parentViewImage.getOriginView();
//            if (parentOriginView instanceof AdapterView) {
//                if (!originView.performClick()) {
//                    if (clickAdapterView((AdapterView) parentOriginView, originView)) {
//                        return true;
//                    }
//                }
//            }
//        }
        return clickV2();
    }


    @SuppressLint("NewApi")
    public boolean clickByPoint(final float x, final float y) {
        View rootView = rootViewImage().getOriginView();
        int[] loca = new int[2];
        rootView.getLocationOnScreen(loca);

        final float locationOnRootViewX = x - loca[0];
        final float locationOnRootViewY = y - loca[1];

        if (locationOnRootViewX < 0 || locationOnRootViewY < 0) {
            //点击到屏幕外面了
            return false;
        }
        if (locationOnRootViewX > rootView.getWidth() || locationOnRootViewY > rootView.getHeight()) {
            return false;
        }


        if (!dispatchInputEvent(genMotionEvent(MotionEvent.ACTION_DOWN, new float[]{locationOnRootViewX, locationOnRootViewY}))) {
            return false;
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                dispatchInputEvent(genMotionEvent(MotionEvent.ACTION_UP, new float[]{locationOnRootViewX, locationOnRootViewY}));
            }
        }, ThreadLocalRandom.current().nextInt(25) + 10);
        return true;
    }


    public boolean dispatchInputEvent(InputEvent inputEvent) {
        View rootView = rootViewImage().getOriginView();
        final Object mViewRootImpl = RposedHelpers.callMethod(rootView, "getViewRootImpl");
        if (mViewRootImpl == null) {
            Log.w(SuperAppium.TAG, "can not find RootViewImpl to dispatch event");
            return false;
        }
        RposedHelpers.callMethod(mViewRootImpl, "dispatchInputEvent", inputEvent);
        return true;
    }

    public boolean dispatchPointerEvent(MotionEvent inputEvent) {
        View rootView = rootViewImage().getOriginView();
        return rootView.dispatchTouchEvent(inputEvent);
    }


    public boolean clickV2() {
        float[] floats = measureClickPoint();
        return clickByPoint(floats[0], floats[1]);
    }

    private static Random random = new Random();

    private float[] measureClickPoint() {
        int[] locs = new int[2];
        originView.getLocationOnScreen(locs);
        float x = locs[0];//+ ((float) originView.getWidth() / 4) + random.nextInt(originView.getWidth() / 4);
        float y = locs[1];//+ ((float) originView.getHeight() / 4) + random.nextInt(originView.getHeight() / 4);

        if (originView.getWidth() > 5) {
            x += ((float) originView.getWidth() / 4) + random.nextInt(originView.getWidth() / 4);
        }
        if (originView.getHeight() > 5) {
            y += ((float) originView.getHeight() / 4) + random.nextInt(originView.getHeight() / 4);
        }

        float[] ret = new float[2];
        ret[0] = x;
        ret[1] = y;
        return ret;
    }

    public void swipe(int fromX, int fromY, int toX, int toY) {
        SwipeUtils.simulateScroll(this, fromX, fromY, toX, toY);
    }

    @SuppressLint("NewApi")
    public void swipeDown(int height) {

        int[] locs = new int[2];
        originView.getLocationOnScreen(locs);

        int viewWidth = originView.getWidth();
        int viewHeight = originView.getHeight();

        int fromX = (int) (locs[0] + viewWidth * (ThreadLocalRandom.current().nextDouble(0.4) - 0.2));
        if (fromX < 2) {
            fromX = 2;
        }
        int toX = (int) (fromX + viewWidth * (ThreadLocalRandom.current().nextDouble(0.1)));


        int fromY, toY;
        if (height > 0) {
            fromY = (int) (locs[1] + viewHeight * ThreadLocalRandom.current().nextDouble(0.1));
            if (fromY < 2) {
                fromY = 2;
            }
            toY = fromY + height;
        } else {
            fromY = (int) (locs[1] + viewHeight * (ThreadLocalRandom.current().nextDouble(0.1) + 0.9));
            toY = fromY + height;
            if (toY < 2) {
                toY = 2;
            }
        }
        SwipeUtils.simulateScroll(this, fromX, fromY, toX, toY, 400, 50);

    }

    /**
     * 向右滑动
     *
     * @param width 滑动宽度，如果为负数，则向左滑动
     */
    @SuppressLint("NewApi")
    public void swipeRight(int width) {
        int[] locs = new int[2];
        originView.getLocationOnScreen(locs);

        int viewWidth = originView.getWidth();
        int viewHeight = originView.getHeight();

        int fromY = (int) (locs[1] + viewHeight * (ThreadLocalRandom.current().nextDouble(0.05) - 0.025 + 0.5));
        if (fromY < 2) {
            fromY = 2;
        }
        int toY = (int) (fromY + viewHeight * (ThreadLocalRandom.current().nextDouble(0.008)));

        int fromX, toX;

        if (width > 0) {
            fromX = (int) (locs[0] + viewWidth * ThreadLocalRandom.current().nextDouble(0.1));
            if (fromX < 2) {
                fromX = 2;
            }
            toX = fromX + width;
        } else {
            fromX = (int) (locs[0] + viewWidth * (ThreadLocalRandom.current().nextDouble(0.1) + 0.9));
            toX = fromX + width;
            if (toX < 2) {
                toX = 2;
            }
        }
//        Log.i(SuperAppium.TAG, "location on screen: (" + locs[0] + "," + locs[1] + ")  from loc:("
//                + fromX + "," + fromY + ") to loc:(" + toX + "," + toY + ") with and height: (" + viewWidth + "," + viewHeight + ")");
        SwipeUtils.simulateScroll(this, fromX, fromY, toX, toY, 300, 30);
    }

    private MotionEvent genMotionEvent(int action, float[] point) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
        pointerCoords.x = point[0];
        pointerCoords.y = point[1];
        MotionEvent.PointerProperties pointerProperties = new MotionEvent.PointerProperties();
        pointerProperties.id = 0;
        pointerProperties.toolType = TOOL_TYPE_FINGER;
        MotionEvent.PointerProperties[] pointerPropertiesArray = new MotionEvent.PointerProperties[]{pointerProperties};
        MotionEvent.PointerCoords[] pointerCoordsArray = new MotionEvent.PointerCoords[]{pointerCoords};
        return MotionEvent.obtain(
                downTime, eventTime, action,
                1, pointerPropertiesArray, pointerCoordsArray,
                0, 0, 0, 0, 8, 0, 4098, 0
        );
    }


    public WebView findWebViewIfExist() {
        ViewImages webViews = Collector.collect(new Evaluator() {

            @Override
            public boolean matches(ViewImage root, ViewImage element) {
                return element.getOriginView() instanceof WebView;
            }

            @Override
            public boolean onlyOne() {
                return true;
            }
        }, this);
        if (webViews.size() == 0) {
            return null;
        }
        return (WebView) webViews.get(0).getOriginView();
    }


    @Override
    public String toString() {
        return SuperAppiumDumper.dumpToJson(this);
    }
}
