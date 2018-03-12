package com.davipviana.restservice.webservices;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.davipviana.restservice.Constants;
import com.davipviana.restservice.R;

import org.json.JSONObject;

/**
 * Created by daviv on 12/03/2018.
 */

public abstract class WebServiceTask extends AsyncTask<Void, Void, Boolean> {
    public static final String TAG = WebServiceTask.class.getName();
    public abstract void showProgress();

    public abstract boolean performRequest();

    public abstract void performSuccessfulOperation();

    public abstract void hideProgress();

    private String message;
    private Context context;

    public WebServiceTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        showProgress();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if(!WebServiceUtils.hasInternetConnection(context)) {
            message = Constants.CONNECTION_MESSAGE;
            return false;
        }
        return performRequest();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        hideProgress();
        if(success) {
            performSuccessfulOperation();
        }
        if(message != null && !message.isEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        hideProgress();
    }

    public boolean hasError(JSONObject object) {
        if(object != null) {
            int status = object.optInt(Constants.STATUS);
            Log.d(TAG, "Response: " + object.toString());
            message = object.optString(Constants.MESSAGE);

            if(status == Constants.STATUS_ERROR || status == Constants.STATUS_UNAUTHORIZED) {
                return true;
            } else {
                return false;
            }
        }
        message = context.getString(R.string.error_url_not_found);
        return true;
    }
}
