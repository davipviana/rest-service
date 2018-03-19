package com.davipviana.restservice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.davipviana.restservice.data.User;
import com.davipviana.restservice.webservices.WebServiceTask;

public class MainActivity extends AppCompatActivity {

    private UserInfoTask userInfoTask = null;
    private UserEditTask userEditTask = null;
    private UserResetTask userResetTask = null;
    private UserDeleteTask userDeleteTask = null;

    private EditText emailText;
    private EditText passwordText;
    private EditText nameText;
    private EditText phoneNumberText;
    private EditText noteText;

    private interface ConfirmationListener {
        void onConfirmation(boolean isConfirmed);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        showProgress(true);
        userInfoTask = new UserInfoTask();
        userInfoTask.execute();
    }

    private void initViews() {
        emailText = (EditText) findViewById(R.id.email);
        passwordText = (EditText) findViewById(R.id.password);
        nameText = (EditText) findViewById(R.id.name);
        phoneNumberText = (EditText) findViewById(R.id.phoneNumber);
        noteText = (EditText) findViewById(R.id.note);
    }

    private void showProgress(boolean isShow) {
        findViewById(R.id.progress).setVisibility(isShow ? View.VISIBLE : View.GONE);
        findViewById(R.id.info_form).setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    private void populateText() {
        User user = RESTServiceApplication.getInstance().getUser();
        emailText.setText(user.getEmail());
        passwordText.setText(user.getPassword());
        nameText.setText(user.getName() == null ? "" : user.getName());
        phoneNumberText.setText(user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
        noteText.setText(user.getNote() == null ? "" : user.getNote());
    }

    private abstract class ActivityWebServiceTask extends WebServiceTask {
        public ActivityWebServiceTask(WebServiceTask webServiceTask) {
            super(MainActivity.this);
        }

        @Override
        public void showProgress() {
            MainActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            MainActivity.this.showProgress(false);
        }

        @Override
        public void performSuccessfulOperation() {
            populateText();
        }
    }

    private class UserInfoTask extends ActivityWebServiceTask {
        public UserInfoTask() {
            super(userInfoTask);
        }

        public boolean performRequest() {
            return true;
        }
    }

    private class UserEditTask extends ActivityWebServiceTask {
        public UserEditTask() {
            super(userEditTask);
        }

        public boolean performRequest() {
            return true;
        }
    }

    private class UserResetTask extends ActivityWebServiceTask {
        public UserResetTask() {
            super(userResetTask);
        }

        public boolean performRequest() {
            return true;
        }
    }

    private class UserDeleteTask extends ActivityWebServiceTask {
        public UserDeleteTask() {
            super(userDeleteTask);
        }

        public boolean performRequest() {
            return true;
        }
    }
}
