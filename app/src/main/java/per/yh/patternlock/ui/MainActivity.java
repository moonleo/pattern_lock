package per.yh.patternlock.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import per.yh.patternlock.R;
import per.yh.patternlock.view.OnPatternChangedListener;
import per.yh.patternlock.view.PatternView;


public class MainActivity extends Activity implements OnPatternChangedListener {

    private TextView tipText;//提示文字
    private PatternView patternView;//图案锁

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tipText = (TextView) findViewById(R.id.tv);
        patternView = (PatternView) findViewById(R.id.pattern);
        patternView.setOnPatternChangedListener(this);
    }

    @Override
    public void patternChanged(String password) {
        if(TextUtils.isEmpty(password)) {
            tipText.setText("请至少绘制"+PatternView.MIN_POINT_NUM+"个点");
        } else {
            tipText.setText(password);
        }
    }

    @Override
    public void patternStart(boolean start) {
        if(start) {
            tipText.setText("请绘制图案");
        }
    }
}
