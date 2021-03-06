package com.jay.bihu.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.jay.bihu.R;
import com.jay.bihu.config.ApiConfig;
import com.jay.bihu.utils.HttpUtils;
import com.jay.bihu.utils.ToastUtils;
import com.jay.bihu.view.LoginDialog;

public class LoginActivity extends BaseActivity {
    private LoginDialog mDialog;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPreferences = getSharedPreferences("account", Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();

        mDialog = new LoginDialog(this);
        initDialog();
    }

    private void initDialog() {
        mDialog.show();
        mDialog.getMessageTextView().setText(R.string.welcome);
        mDialog.setRegisterButton(R.string.register, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mDialog.getUsernameWrapper().getEditText().getText().toString();
                String password = mDialog.getPasswordWrapper().getEditText().getText().toString();
                register(username, password);
            }
        });
        mDialog.setLoginButton(R.string.login, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mDialog.getUsernameWrapper().getEditText().getText().toString();
                String password = mDialog.getPasswordWrapper().getEditText().getText().toString();
                login(username, password);
            }
        });

        mDialog.addUsernameWrapper(R.string.hint_username);
        mDialog.addPasswordWrapper(R.string.hint_password);

        String username = mPreferences.getString("username", "");
        String password = mPreferences.getString("password", "");
        mDialog.getUsernameWrapper().getEditText().setText(username);
        mDialog.getPasswordWrapper().getEditText().setText(password);
        boolean isLogin = mPreferences.getBoolean("isLogin", false);
        if (isLogin)
            login(username, password);
    }

    private void register(String username, String password) {
        if (username.length() < 1) {
            mDialog.getUsernameWrapper().setError("用户名过短");
            return;
        }
        if (password.length() < 5) {
            mDialog.getPasswordWrapper().setError("密码过短");
            return;
        }
        loginOrRegister(ApiConfig.REGISTER, username, password);
    }

    private void login(String username, String password) {
        loginOrRegister(ApiConfig.LOGIN, username, password);
    }

    private void loginOrRegister(String address, String username, String password) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("登录中...");
        progressDialog.show();

        HttpUtils.sendHttpRequest(address, "username=" + username + "&password=" + password, new HttpUtils.Callback() {
            @Override
            public void onResponse(final HttpUtils.Response response) {
                checkResponseStatusCode(response.getStatusCode(), response);
                progressDialog.dismiss();
            }

            @Override
            public void onFail(Exception e) {
                e.printStackTrace();
                ToastUtils.showError(e.toString());
                progressDialog.dismiss();
            }
        });
    }

    private void checkResponseStatusCode(int statusCode, final HttpUtils.Response response) {
        switch (statusCode) {
            case 200:
                mDialog.dismiss();
                ToastUtils.showHint("欢迎来到逼乎社区");
                String username = mDialog.getUsernameWrapper().getEditText().getText().toString();
                String password = mDialog.getPasswordWrapper().getEditText().getText().toString();
                mEditor.putString("username", username);
                mEditor.putString("password", password);
                mEditor.putBoolean("isLogin", true);
                mEditor.apply();

                actionStart(response.bodyString());
                break;
            case 400:
                mDialog.getPasswordWrapper().setError(response.getInfo());
                break;
            default:
                ToastUtils.showError(response.message());
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            mDialog.show();
        return super.onTouchEvent(event);
    }

    private void actionStart(String data) {
        Intent intent = new Intent(this, QuestionListActivity.class);
        intent.putExtra("data", data);
        startActivity(intent);
        finish();
    }
}