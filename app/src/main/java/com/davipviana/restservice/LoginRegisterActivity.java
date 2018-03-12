package com.davipviana.restservice;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import com.davipviana.restservice.data.User;
import com.davipviana.restservice.webservices.WebServiceTask;
import com.davipviana.restservice.webservices.WebServiceUtils;

import org.json.JSONObject;

import java.util.regex.Pattern;

public class LoginRegisterActivity extends AppCompatActivity {
    private UserLoginRegisterTask userLoginRegisterTask = null;
    private EditText emailView;
    private EditText passwordView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        initViews();
    }

    private void initViews() {
        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
    }

    public void attemptLoginRegister(View view) {
        if(userLoginRegisterTask != null) {
            return;
        }

        emailView.setError(null);
        passwordView.setError(null);

        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_password_lenght));
            focusView = passwordView;
            cancel = true;
        }
        if(TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if(!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        if(cancel) {
            focusView.requestFocus();
        } else {
            userLoginRegisterTask = new UserLoginRegisterTask(email, password, view.getId() == R.id.email_sign_in_button);
            userLoginRegisterTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showProgress(final boolean isShow) {
        findViewById(R.id.login_progress).setVisibility(isShow ? View.VISIBLE : View.GONE);
        findViewById(R.id.login_form).setVisibility(isShow ? View.GONE : View.VISIBLE);
    }

    private class UserLoginRegisterTask extends WebServiceTask {
        private final ContentValues contentValues = new ContentValues();
        private boolean isLogin;

        UserLoginRegisterTask(String email, String password, boolean isLogin) {
            super(LoginRegisterActivity.this);
            contentValues.put(Constants.EMAIL, email);
            contentValues.put(Constants.PASSWORD, password);
            contentValues.put(Constants.GRANT_TYPE, Constants.CLIENT_CREDENTIALS);
            this.isLogin = isLogin;
        }

        @Override
        public void showProgress() {
            LoginRegisterActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            LoginRegisterActivity.this.showProgress(false);
        }

        @Override
        public boolean performRequest() {
            JSONObject object = WebServiceUtils.requestJSONObject(
                    isLogin ? Constants.LOGIN_URL : Constants.SIGNUP_URL,
                    WebServiceUtils.METHOD.POST,
                    contentValues,
                    true
            );
            userLoginRegisterTask = null;
            if(!hasError(object)) {
                if(isLogin) {
                    User user = new User();
                    user.setId(object.optLong(Constants.ID));
                    user.setEmail(contentValues.getAsString(Constants.EMAIL));
                    user.setPassword(contentValues.getAsString(Constants.PASSWORD));

                    RESTServiceApplication.getInstance().setUser(user);
                    RESTServiceApplication.getInstance().setAccessToken(
                            object.optJSONObject(Constants.ACCESS).optString(Constants.ACCESS_TOKEN)
                    );
                    return true;
                } else {
                    isLogin = true;
                    performRequest();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
