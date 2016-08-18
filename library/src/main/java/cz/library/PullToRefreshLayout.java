package cz.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import cz.library.header.DisplayHeader;
import cz.library.header.FlipHeader;
import cz.library.header.IndicatorHeader;
import cz.library.header.MaterialHeader;
import cz.library.header.RefreshHeader;
import cz.library.headerstrategy.HeaderFollowStrategy;
import cz.library.headerstrategy.HeaderFrontStrategy;
import cz.library.headerstrategy.HeaderOverlapStrategy;
import cz.library.headerstrategy.HeaderScrollStrategy;
import cz.library.headerstrategy.HeaderStrategy;

/**
 * Created by cz on 2016/8/13.
 * refresh header mode: static/linear/background
 */
public class PullToRefreshLayout<V extends View> extends ViewGroup {
    private static final String TAG = "PullToRefreshBase";
    private final float HEADER_MAX_PULL_HEIGHT = 400f;
    private static final boolean DEBUG=true;

    public static final int HEADER_INDICATOR=0x00;
    public static final int HEADER_FLIP=0x01;
    public static final int HEADER_MATERIAL=0x02;
    public static final int HEADER_DISPLAY=0x03;



    @IntDef(value={HEADER_INDICATOR,HEADER_FLIP,HEADER_MATERIAL,HEADER_DISPLAY})
    public @interface HeaderType{
    }

    public static final int STRATEGY_FOLLOW=0x00;
    public static final int STRATEGY_OVERLAP=0x01;
    public static final int STRATEGY_FRONT=0x02;
    public static final int STRATEGY_SCROLL=0x03;

    @IntDef(value={STRATEGY_FOLLOW,STRATEGY_OVERLAP,STRATEGY_FRONT,STRATEGY_SCROLL})
    public @interface Strategy{
    }

    public static final int VERTICAL=0;
    public static final int HORIZONTAL=1;

    private int orientation;
    private Scroller scroller;
    private RefreshState refreshState;
    private float resistance;
    private boolean isIntercept;
    private boolean isReDispatch;
    private boolean isReleasing;//release the refreshing
    private int scrollDuration;
    private float lastX,lastY;
    private float distanceX,distanceY;
    private float pullMaxHeight;
    private VelocityTracker velocityTracker;
    private int scaledMaximumFlingVelocity;
    private int scaledMinimumFlingVelocity;
    private OnPullToRefreshListener listener;
    private RefreshHeader refreshHeader;
    private HeaderStrategy headerStrategy;
    private int displayId;
    private int containerId;
    protected V targetView;
    private RefreshMode refreshMode;



    public PullToRefreshLayout(Context context) {
        this(context,null,0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new Scroller(context);
        orientation = VERTICAL;
        refreshState = RefreshState.NONE;

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        scaledMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        scaledMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefreshLayout);
        setResistance(a.getFloat(R.styleable.PullToRefreshLayout_pull_resistance, 2f));
        setScrollDuration(a.getInteger(R.styleable.PullToRefreshLayout_pull_duration, 300));
        setPullMaxHeight(a.getDimension(R.styleable.PullToRefreshLayout_pull_pullMaxSize, HEADER_MAX_PULL_HEIGHT));
        setDisplayResourceId(a.getResourceId(R.styleable.PullToRefreshLayout_pull_displayResourceId,NO_ID));
        setContainerResourceId(a.getResourceId(R.styleable.PullToRefreshLayout_pull_containerResourceId,NO_ID));
        setHeaderTypeInner(a.getInt(R.styleable.PullToRefreshLayout_pull_headerType, HEADER_INDICATOR));
        setHeaderStrategyInner(a.getInt(R.styleable.PullToRefreshLayout_pull_headerStrategy, STRATEGY_FOLLOW));
        setRefreshModeInner(a.getInt(R.styleable.PullToRefreshLayout_pull_refreshMode,0));
        a.recycle();
    }



    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        View contentView=null;
        if(NO_ID!=displayId&&NO_ID!=containerId){
            View headerView = findViewById(displayId);
            View containerView = findViewById(containerId);
            if(null==headerView||null==containerView){
                throw new NullPointerException("the header or container id is invalid,can't found view!");
            } else if(!(refreshHeader instanceof DisplayHeader)){
                throw new IllegalArgumentException("if use display resource id, the header type must use display!!!");
            } else {
                DisplayHeader refreshHeader = (DisplayHeader) this.refreshHeader;
                refreshHeader.setHeaderView(headerView);
                targetView = (V) contentView;
            }
        } else if(1==childCount){
            targetView = (V) getChildAt(0);
        } else if(2==childCount){
            if(!(refreshHeader instanceof DisplayHeader)){
                throw new IllegalArgumentException("two children but not a display header mode, what do you want to do?");
            } else {
                DisplayHeader refreshHeader = (DisplayHeader) this.refreshHeader;
                View headerView = getChildAt(0);
                View containerView = getChildAt(1);
                removeViewAt(0);
                refreshHeader.setHeaderView(headerView);
                targetView = (V) containerView;
            }
        } else if(1<childCount){
            throw new IllegalArgumentException("layout can only add one view in it!");
        } else if(null==targetView){
            targetView=getTargetView();
            if(null==targetView){
                throw new NullPointerException("the  container is null,must have a container view!");
            }
        }
        //this method will add header and target view and layout them
        setRefreshHeader(refreshHeader);
    }


    @Override
    public void postInvalidate() {
        if(hasWindowFocus())  super.postInvalidate();
    }

    @Override
    public void invalidate() {
        if(hasWindowFocus()) super.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            measureChild(getChildAt(i),widthMeasureSpec,heightMeasureSpec);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        if(null!=headerStrategy){
            headerStrategy.onLayout(b,left,top,right,bottom);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (!scroller.isFinished()&&scroller.computeScrollOffset()) {
            scrollTo(0, scroller.getCurrY());
            postInvalidate();
        } else if(isReleasing){
            isReleasing=false;
            if(RefreshState.PULL_START==refreshState){
                refreshHeader.onRefreshStateChange(refreshState =RefreshState.NONE);
            }
        }
    }

    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    public void dealMulTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                ;
                final int pointerIndex = ev.getActionIndex();

                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                lastX = x;
                lastY = y;
                activePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                final float x = ev.getX( pointerIndex);
                final float y = ev.getY(pointerIndex);
                distanceX = x - lastX;
                distanceY = y - lastY;
                lastX = x;
                lastY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId != activePointerId) {
                    lastX = ev.getX(pointerIndex);
                    lastY = ev.getY(pointerIndex);
                    activePointerId = ev.getPointerId(pointerIndex);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastX = ev.getX(newPointerIndex);
                    lastY = ev.getY(newPointerIndex);
                    activePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        dealMulTouchEvent(ev);
        int action = ev.getActionMasked();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                if (!headerRefresh()&&(isChildScrollToTop()||isChildScrollToBottom())) {
                    isIntercept = false;
                }
                Log.e(TAG,"dispatchTouchEvent:"+isIntercept+" isTop:"+isChildScrollToTop()+" isBottom:"+isChildScrollToBottom());
                break;
            case MotionEvent.ACTION_MOVE:
                if(null==targetView||Math.abs(distanceY)<Math.abs(distanceX)) {
                    isIntercept=false;
                } else {
                    isIntercept=headerStrategy.isIntercept(distanceY);
                }
                //Log.e(TAG,"dispatchTouchEvent:"+isIntercept+" isReDispatch:"+isReDispatch);
                if(isIntercept&&!isReDispatch){
                    isReDispatch =true;
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    MotionEvent ev2 = MotionEvent.obtain(ev);
                    dispatchTouchEvent(ev);
                    ev2.setAction(MotionEvent.ACTION_DOWN);
                    return dispatchTouchEvent(ev2);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e(TAG," onInterceptTouchEvent:"+isIntercept);
        return (isIntercept)&&refreshMode.enableHeader();
    }

    /**
     * check header refresh status is drag
     * @return
     */
    private boolean headerRefresh(){
        return RefreshState.RELEASE_START==refreshState||RefreshState.START_REFRESHING==refreshState;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(null==targetView) return false;
        if(null==velocityTracker) velocityTracker=VelocityTracker.obtain();
        velocityTracker.addMovement(event);
        int action = event.getActionMasked();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                lastX=  x;
                lastY=  y;
                stopScrollAnimation();
                //when current state is not refreshing,set pull start
                if(RefreshState.START_REFRESHING!=refreshState){
                    refreshHeader.onRefreshStateChange(refreshState=RefreshState.PULL_START);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isIntercept){
                    if(RefreshState.NONE==refreshState){
                        refreshHeader.onRefreshStateChange( refreshState =RefreshState.PULL_START);
                    }
                    headerStrategy.onMoveOffset(distanceY);
                    postInvalidate();
                } else if (0!=distanceY&& headerStrategy.isMoveToTop()) {
                    Log.e(TAG,"move top");
                    headerStrategy.onResetRefresh(RefreshState.PULL_START);
                    event.setAction(MotionEvent.ACTION_DOWN);
                    dispatchTouchEvent(event);
                    isReDispatch =false;
                }
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000,scaledMaximumFlingVelocity);
                if(HORIZONTAL==orientation){

                } else if(VERTICAL==orientation){
                    Log.e(TAG,"action up:"+refreshState);
                    isReDispatch =false;
                    if(null!=listener&&RefreshState.RELEASE_START==refreshState){
                        Log.e(TAG,"refresh");
                        listener.onRefresh();
                    }
                    headerStrategy.onResetRefresh(refreshState);
                }
                releaseVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                stopScrollAnimation();
                break;
        }
        return true;
    }

    private void releaseVelocityTracker() {
        if(null!=velocityTracker){
            velocityTracker.recycle();
            velocityTracker=null;
        }
    }


    private void stopScrollAnimation(){
        if(!scroller.isFinished()){
            scroller.abortAnimation();
        }
    }


    public void refreshStateChanged(float fraction) {
        if(RefreshState.PULL_START==refreshState&&1.0f<=fraction){
            refreshHeader.onRefreshStateChange(refreshState=RefreshState.RELEASE_START);
        } else if(RefreshState.RELEASE_START==refreshState&&1.0f>fraction){
            refreshHeader.onRefreshStateChange(refreshState=RefreshState.PULL_START);
        } else if(RefreshState.START_REFRESHING==refreshState&&1.0f>fraction){
            refreshHeader.onRefreshStateChange(refreshState=RefreshState.RELEASE_REFRESHING_START);
        } else if(RefreshState.RELEASE_REFRESHING_START==refreshState&&1.0f<=fraction){
            //when current state is release refreshing and fraction greater then 1.0 set refresh state start refreshing
            refreshHeader.onRefreshStateChange(refreshState=RefreshState.START_REFRESHING);
        }
    }

    public void onRefreshComplete(){
        isIntercept=false;
        if(null!=headerStrategy){
            headerStrategy.onRefreshComplete();
        }
    }

    public void autoRefreshing(boolean anim){
        if(null!=headerStrategy){
            headerStrategy.autoRefreshing(anim);
        }
    }

    public boolean isChildScrollToTop() {
        return !ViewCompat.canScrollVertically(targetView, -1);
    }


    public boolean isChildScrollToBottom() {
        return !ViewCompat.canScrollVertically(targetView, 1);
    }

    /**
     * set display id and the header mode must display
     * @param id
     */
    private void setDisplayResourceId(int id) {
        this.displayId=id;
    }

    /**
     * set the container view id,if doesn't has display resource id,only one view is container view
     * @param id
     */
    private void setContainerResourceId(int id) {
        this.containerId=id;
    }

    /**
     * get pull to refresh container view
     * child must implement
     * @return
     */
    protected  V getTargetView(){
        return null;
    }


    public void setHeaderStrategy(@Strategy  int strategy){
        setHeaderStrategyInner(strategy);
    }

    /**
     * set header type
     * @see #HEADER_INDICATOR,#HEADER_FLIP,#HEADER_MATERIAL
     * @param type
     */
    public void setHeaderType(@HeaderType int type) {
        setHeaderTypeInner(type);
    }

    private void setHeaderTypeInner(int type){
        Context context = getContext();
        switch (type){
            case HEADER_INDICATOR:
                refreshHeader=new IndicatorHeader(context,this);
                break;
            case HEADER_FLIP:
                refreshHeader=new FlipHeader(context,this);
                break;
            case HEADER_MATERIAL:
                refreshHeader=new MaterialHeader(context,this);
                break;
            case HEADER_DISPLAY:
                refreshHeader=new DisplayHeader(context,this);
                break;
        }
    }

    /**
     * set header strategy
     * @see #STRATEGY_FOLLOW,#STRATEGY_FRONT,#STRATEGY_OVERLAP,#STRATEGY_SCROLL
     * STRATEGY_FOLLOW default strategy
     * STRATEGY_FRONT use material style
     * STRATEGY_OVERLAP some other refresh ,the header is static.
     * STRATEGY_SCROLL no refresh action, scroll show header release scroll back
     * @param strategy
     */
    private void setHeaderStrategyInner(int strategy){
        switch (strategy){
            case STRATEGY_FOLLOW:
                setHeaderStrategy(new HeaderFollowStrategy(this),false);
                break;
            case STRATEGY_FRONT:
                setHeaderStrategy(new HeaderFrontStrategy(this),false);
                break;
            case STRATEGY_OVERLAP:
                setHeaderStrategy(new HeaderOverlapStrategy(this),false);
                break;
            case STRATEGY_SCROLL:
                setHeaderStrategy(new HeaderScrollStrategy(this),false);
                break;
        }
    }

    private void setHeaderStrategy(HeaderStrategy headerStrategy,boolean initRefreshHeader){
        this.headerStrategy=headerStrategy;
        if(initRefreshHeader){
            this.headerStrategy.onInitRefreshHeader(refreshHeader);
            requestLayout();
        }
    }

    /**
     * set header strategy
     * @param headerStrategy
     */
    public void setHeaderStrategy(HeaderStrategy headerStrategy){
        setHeaderStrategy(headerStrategy,true);
    }

    public void setRefreshHeader(RefreshHeader newHeader){
        this.refreshHeader=newHeader;
        this.headerStrategy.onInitRefreshHeader(newHeader);
    }

    public RefreshHeader getRefreshHeader(){
        return refreshHeader;
    }

    public void setRefreshMode(RefreshMode mode){
        this.refreshMode=mode;
    }

    public RefreshMode getRefreshMode(){
        return this.refreshMode;
    }

    private void setRefreshModeInner(int mode) {
        setRefreshMode(RefreshMode.values()[mode]);
    }

    public View getRefreshView(){
        return targetView;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        invalidate();
    }

    public void setPullMaxHeight(float pullMaxHeight) {
        this.pullMaxHeight = pullMaxHeight;
    }

    public float getPullMaxHeight() {
        return pullMaxHeight;
    }

    public void setReleasing(boolean releasing) {
        isReleasing = releasing;
    }

    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    public float getResistance() {
        return resistance;
    }

    public void setScrollDuration(int duration) {
        this.scrollDuration=duration;
    }

    public int getScrollDuration() {
        return scrollDuration;
    }

    public void setRefreshState(RefreshState state){
        this.refreshState =state;
    }

    public RefreshState getRefreshState(){
        return refreshState;
    }

    public boolean isRefreshState(RefreshState state){
        return refreshState==state;
    }

    public VelocityTracker getVelocityTracker(){
        return velocityTracker;
    }

    public int getScaledMinimumFlingVelocity() {
        return scaledMinimumFlingVelocity;
    }

    public void startScroll(int startX,int startY,int dx,int dy,int duration) {
        scroller.startScroll(startX,startY,dx,dy,duration);
    }


    public void setOnPullToRefreshListener(OnPullToRefreshListener listener){
        this.listener=listener;
    }

    /**
     * pull to refresh listener
     */
    public interface OnPullToRefreshListener {
        void onRefresh();
    }

}
