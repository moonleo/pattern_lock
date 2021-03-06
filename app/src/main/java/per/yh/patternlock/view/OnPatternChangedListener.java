package per.yh.patternlock.view;

/**
 * Created by MoonLeo on 2016/1/16.
 * 图案变化监听器
 */
public interface OnPatternChangedListener {
    void patternChanged(String password);
    void patternStart(boolean start);
    void passwordSetted(String password);
}
