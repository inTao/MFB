package com.example.user.magicstick.activite;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.user.magicstick.R;

import java.util.ArrayList;

/**
 * Created by user on 2018/4/23.
 */


public class ReadActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private static final int[] ETID = new int[]{R.id.et1, R.id.et2, R.id.et3, R.id.et4, R.id.et5,};
    private Button mButton;
    private LinearLayout readLL;
    private int sum;
    private LinearLayout readLL2;
    private View mItemV;
    private ArrayList<String[]> mPDateList = new ArrayList<>();
    private String[] mPData;
    private boolean isP1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sum = 30;
        isP1 = true;
        mPData = new String[]{"00", "00", "00", "00", "00"};
        //初始化view
        initView();
    }

    //初始化view方法
    private void initView() {
        //获得俩个装P的布局
        readLL = findViewById(R.id.read_ll);//放 P1~P4  P30
        readLL2 = findViewById(R.id.read_ll2);//放 P5~p29
        //更具sum加载p的数目
        for (int i = 0; i < sum; i++) {
            mPDateList.add(mPData);
            //获取p的item
            mItemV = View.inflate(this, R.layout.ic_item, null);
            //获取P的textView
            TextView pView = mItemV.findViewById(R.id.p);
            pView.setText("P" + (i + 1) + " :");
            Button wBt = mItemV.findViewById(R.id.wBt);
            wBt.setTag(i);
            wBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StringBuffer stringBuffer = new StringBuffer();
                    int i = (int) v.getTag();
                    for (String s:
                    mPDateList.get(i) ) {
                        System.out.println(s);
                    }

                }
            });

            //p1~4 一定会有 所以加到布局1 显示 当有 30时 也要显示 其它的放在布局2中
            if (i < 4 || i == 29)
                readLL.addView(mItemV);
            else
                readLL2.addView(mItemV);

            //如果sum>3时 需要“显示更多”的按钮
            if (sum > 3 && i == 3) {
                mButton = findViewById(R.id.showMore);
                mButton.setVisibility(View.VISIBLE);
            }
            //不同的p有不同的edittext个数
            switch (i) {
                case 0:
                    if (isP1){
                        Spinner spinner = mItemV.findViewById(R.id.pSp);
                        spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,new String[]{"11","21","31","41"}));
                        spinner.setVisibility(View.VISIBLE);
                    }
                    break;
                case 1://p1、p2 有1个edittext p3有4个 p5 有5个 p30有2个 其它都为5个
                    EditText text1 = mItemV.findViewById(ETID[0]);
                    //设置editText
                    setEditText(text1, mItemV, i);
                    break;
                case 2:
                case 3:
                    for (int j = 0; j < ETID.length - (3 - i); j++) {
                        EditText text = mItemV.findViewById(ETID[j]);
                        setEditText(text, mItemV, i);
                    }
                    break;
                case 29:
                    for (int j = 0; j < 2; j++) {
                        EditText text2 = mItemV.findViewById(ETID[j]);
                        setEditText(text2, mItemV, i);
                    }
                    break;
                default:
                    for (int j = 0; j < ETID.length; j++) {
                        EditText text3 = mItemV.findViewById(ETID[j]);
                        setEditText(text3, mItemV, i);
                    }
                    break;
            }

        }

//设置显示更多的点击事件
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mButton.getText().equals(getString(R.string.showMore))) {
                    readLL2.setVisibility(View.VISIBLE);
                    mButton.setText(getString(R.string.packUp));
                } else {
                    readLL2.setVisibility(View.GONE);
                    mButton.setText(getString(R.string.showMore));
                }

            }
        });

    }

    //设置editText
    private void setEditText(final EditText editText, final View itemV, final int p) {
        int i = 0;
        switch (editText.getId()) {
            case R.id.et2:
                i = 1;
                break;
            case R.id.et3:
                i = 2;
                break;
            case R.id.et4:
                i = 3;
                break;
            case R.id.et5:
                i = 4;
                break;
        }
        editText.setVisibility(View.VISIBLE);
        editText.setTransformationMethod(new AutoCaseTransformationMethod());
        final int finalI = i;

        //文字改变监听
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("CharSequence" + s.toString() + "start:" + start);
                //如果输入满后 跳到下一个editText
                if ((s.toString().trim().replaceAll("\\s*", "")).toCharArray().length == 2) {
                    mPDateList.get(p)[finalI] = (s.toString()).toUpperCase();
                    if (start != 0) {
                        if (finalI < 4) {
                            EditText editText1 = itemV.findViewById(ETID[finalI + 1]);
                            editText1.requestFocus();
                            editText1.setSelection(editText1.getText().length());
                        } else
                            return;
                    }
                    //如果删除没后  跳到上一个
                } else if (editText.getText().toString().trim().equals("")) {

                    if (finalI != 0) {
                        EditText editText2 = itemV.findViewById(ETID[finalI - 1]);
                        editText2.requestFocus();
                        editText2.setSelection(editText2.getText().length());
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //失去焦点监听
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String str = ((EditText) v).getText().toString().trim();
                if (!hasFocus) {
                    if (str.toCharArray().length == 1) {
                        ((EditText) v).setText("0" + str);
                    }
                    mPDateList.get(p)[finalI] = ((EditText) v).getText().toString();
                }
            }
        });

    }

    public class AutoCaseTransformationMethod extends ReplacementTransformationMethod {
        /**
         * 获取要改变的字符。
         *
         * @return 将你希望被改变的字符数组返回。
         */
        @Override
        protected char[] getOriginal() {
            return new char[]{'a', 'b', 'c', 'd', 'e',
                    'f', 'g', 'h', 'i', 'j', 'k', 'l',
                    'm', 'n', 'o', 'p', 'q', 'r', 's',
                    't', 'u', 'v', 'w', 'x', 'y', 'z'};
        }

        /**
         * 获取要替换的字符。
         *
         * @return 将你希望用来替换的字符数组返回。
         */
        @Override
        protected char[] getReplacement() {
            return new char[]{'A', 'B', 'C', 'D', 'E',
                    'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        }
    }
}
