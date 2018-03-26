package com.davipviana.restservice;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.davipviana.restservice.data.User;
import com.davipviana.restservice.webservices.WebServiceTask;
import com.davipviana.restservice.webservices.WebServiceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

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
        findViewById(R.id.info_form).setVisibility(isShow ? View.GONE : View.VISIBLE);
    }

    private void populateText() {
        User user = RESTServiceApplication.getInstance().getUser();
        emailText.setText(user.getEmail());
        passwordText.setText(user.getPassword());
        nameText.setText(user.getName() == null ? "" : user.getName());
        phoneNumberText.setText(user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
        noteText.setText(user.getNote() == null ? "" : user.getNote());
    }

    public void clickUpdateButton(View view) {
        if(passwordText.getText().toString().trim().length() >= 5) {
            showProgress(true);
            userEditTask = new UserEditTask();
            userEditTask.execute();
        } else {
            Toast.makeText(this, R.string.error_password_lenght, Toast.LENGTH_LONG).show();
        }
    }

    public void clickDeleteButton(View view) {
        showConfirmationDialog(new ConfirmationListener(){
            @Override
            public void onConfirmation(boolean isConfirmed) {
                if(isConfirmed) {
                    showProgress(true);
                    userDeleteTask = new UserDeleteTask();
                    userDeleteTask.execute();
                }
            }
        });
    }

    public void clickResetButton(View view) {
        showConfirmationDialog(new ConfirmationListener() {
            @Override
            public void onConfirmation(boolean isConfirmed) {
                if(isConfirmed) {
                    showProgress(true);
                    userResetTask = new UserResetTask();
                    userResetTask.execute();
                }
            }
        });
    }

    public void clickSignOutButton(View view) {
        showLoginScreen();
    }

    private void showLoginScreen() {
        Intent intent = new Intent(MainActivity.this, LoginRegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showConfirmationDialog(final ConfirmationListener confirmationListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure? This operation cannot be undone.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                confirmationListener.onConfirmation(true);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                confirmationListener.onConfirmation(false);
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
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
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.ACCESS_TOKEN,
                    RESTServiceApplication.getInstance().getAccessToken());

            JSONObject object = WebServiceUtils.requestJSONObject(Constants.INFO_URL,
                    WebServiceUtils.METHOD.GET, contentValues, null);

            if(!hasError(object)) {
                JSONArray jsonArray = object.optJSONArray(Constants.INFO);
                JSONObject jsonObject = jsonArray.optJSONObject(0);
                user.setName(jsonObject.optString(Constants.NAME));
                if(user.getName().equalsIgnoreCase("null")) {
                    user.setName(null);
                }

                user.setPhoneNumber(jsonObject.optString(Constants.PHONE_NUMBER));
                if(user.getPhoneNumber().equalsIgnoreCase("null")) {
                    user.setPhoneNumber(null);
                }

                user.setNote(jsonObject.optString(Constants.NOTE));
                if(user.getNote().equalsIgnoreCase("null")) {
                    user.setNote(null);
                }

                user.setId(jsonObject.optLong(Constants.ID_INFO));
                return true;
            }
            return false;
        }
    }

    private class UserEditTask extends ActivityWebServiceTask {
        public UserEditTask() {
            super(userEditTask);
        }

        public boolean performRequest() {
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.NAME, nameText.getText().toString());
            contentValues.put(Constants.PASSWORD, passwordText.getText().toString());
            contentValues.put(Constants.PHONE_NUMBER, phoneNumberText.getText().toString());
            contentValues.put(Constants.NOTE, noteText.getText().toString());

            ContentValues urlValues = new ContentValues();
            urlValues.put(Constants.ACCESS_TOKEN, RESTServiceApplication.getInstance().getAccessToken());

            JSONObject object = WebServiceUtils.requestJSONObject(Constants.UPDATE_URL,
                    WebServiceUtils.METHOD.POST, urlValues, contentValues);

            if(!hasError(object)) {
                JSONArray jsonArray = object.optJSONArray(Constants.INFO);
                JSONObject jsonObject = jsonArray.optJSONObject(0);
                user.setName(jsonObject.optString(Constants.NAME));
                user.setPhoneNumber(jsonObject.optString(Constants.PHONE_NUMBER));
                user.setNote(jsonObject.optString(Constants.NOTE));
                user.setPassword(jsonObject.optString(Constants.PASSWORD));
                return true;
            }
            return false;
        }
    }

    private class UserResetTask extends ActivityWebServiceTask {
        public UserResetTask() {
            super(userResetTask);
        }

        public boolean performRequest() {
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.ACCESS_TOKEN,
                    RESTServiceApplication.getInstance().getAccessToken());
            JSONObject object = WebServiceUtils.requestJSONObject(Constants.RESET_URL,
                    WebServiceUtils.METHOD.POST, contentValues, null);
            if(!hasError(object)) {
                user.setName("");
                user.setPhoneNumber("");
                user.setNote("");
                return true;
            }
            return false;
        }
    }

    private class UserDeleteTask extends ActivityWebServiceTask {
        public UserDeleteTask() {
            super(userDeleteTask);
        }

        @Override
        public void performSuccessfulOperation() {
            showLoginScreen();
        }

        public boolean performRequest() {
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.ACCESS_TOKEN,
                    RESTServiceApplication.getInstance().getAccessToken());
            JSONObject object = WebServiceUtils.requestJSONObject(Constants.DELETE_URL,
                    WebServiceUtils.METHOD.DELETE, contentValues, null);

            if(!hasError(object)) {
                RESTServiceApplication.getInstance().setUser(null);
                return true;
            }
            return false;
        }
    }
}
