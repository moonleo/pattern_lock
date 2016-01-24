package per.yh.patternlock.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import per.yh.patternlock.R;
import per.yh.patternlock.view.OnPatternChangedListener;
import per.yh.patternlock.view.PatternView;


public class MainActivity extends Activity implements OnPatternChangedListener {

    private TextView tipText;//提示文字
    private PatternView patternView;//图案锁
    private TextView passwordTextView;//当前密码
    private Button resetBtn;//重置密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tipText = (TextView) findViewById(R.id.tv);
        patternView = (PatternView) findViewById(R.id.pattern);
        patternView.setOnPatternChangedListener(this);
        passwordTextView = (TextView) findViewById(R.id.tv_password);
        resetBtn = (Button) findViewById(R.id.btn_reset);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patternView.resetPoints();
            }
        });
    }

    @Override
    public void patternChanged(String password) {
        if(TextUtils.isEmpty(password)) {
            tipText.setText("请至少绘制"+PatternView.MIN_POINT_NUM+"个点");
        } else {
            if(password.equals(patternView.getPassword())) {
                tipText.setText("密码正确");
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
                this.finish();
            } else {
                tipText.setText("密码错误");
            }
        }
    }

    @Override
    public void patternStart(boolean start) {
        if(start) {
            if(passwordTextView.getText().equals("")) {
                tipText.setText("请设置密码");
            } else {
                tipText.setText("请输入密码");
            }
        }
    }

    /**
     * 设置密码时调用
     * @param password
     */
    @Override
    public void passwordSetted(String password) {
        passwordTextView.setText(password);
        tipText.setText("设置成功");
    }
}
