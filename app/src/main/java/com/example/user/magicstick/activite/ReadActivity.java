package com.example.user.magicstick.activite;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.user.magicstick.R;

/**
 * Created by user on 2018/4/23.
 */


public class ReadActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private static final int[] ETID = new int[]{R.id.et1, R.id.et2, R.id.et3, R.id.et4, R.id.et5,};
    private Button mButton;
    private LinearLayout readLL;
    private int sum;
    private TextView mPView;
    private LinearLayout readLL2;

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
        initView();
    }

    private void initView() {
        readLL = findViewById(R.id.read_ll);
        readLL2 = findViewById(R.id.read_ll2);
        for (int i = 0; i < sum; i++) {
            //EditText et = ((View.inflate(this, R.layout.ic_item, null)).findViewById(R.id.et));
            View mItemV = View.inflate(this, R.layout.ic_item, null);
            ConstraintLayout constraintLayout = mItemV.findViewById(R.id.ic_cl);
            mPView = mItemV.findViewById(R.id.p);
            mPView.setText("P" + (i + 1) + " :");
            constraintLayout.setVisibility(View.VISIBLE);
            if (i < 4 || i == 29)
                readLL.addView(mItemV);
            else
                readLL2.addView(mItemV);

            if (sum > 3 && i == 3) {
                mButton = findViewById(R.id.showMore);
                mButton.setVisibility(View.VISIBLE);
            }
            switch (i) {
                case 0:
                case 1:
                    EditText text1 = mItemV.findViewById(ETID[ETID.length - 1]);
                    setEditText(text1, mItemV);
                    break;
                case 2:
                case 3:
                    for (int j = ETID.length - 1; j > ETID.length - i - 3; j--) {
                        EditText text = mItemV.findViewById(ETID[j]);
                        setEditText(text, mItemV);
                    }
                    break;
                case 29:
                    mPView.setText("P" + 30 + " :");
                    for (int j = ETID.length - 1; j > ETID.length - 3; j--) {
                        EditText text2 = mItemV.findViewById(ETID[j]);
                        setEditText(text2, mItemV);
                    }
                    break;
                default:
                    for (int j = 0; j < ETID.length; j++) {
                        EditText text3 = mItemV.findViewById(ETID[j]);
                        setEditText(text3, mItemV);
                    }
                    break;
            }

        }


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

    private void setEditText(final EditText editText, final View itemV) {
        boolean isfrist = false;
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
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((editText.getText().toString().trim().replaceAll("\\s*", "")).toCharArray().length == 2) {
                    System.out.println(finalI);
                    if (finalI <4){
                        EditText editText1 = itemV.findViewById(ETID[finalI +1]);
                        editText1.requestFocus();
                        editText1.setSelection(editText1.getText().length());
                    }else
                        return;
                }else if (editText.getText().equals("")){

                }
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String str = editText.getText().toString().trim();
                    if (str.toCharArray().length == 1) {
                        editText.setText("0" + str);
                    }
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
