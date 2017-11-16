package com.example.fcastellano.bookmastergeneral;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
// import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cz.msebera.android.httpclient.Header;

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener,AdapterView.OnItemClickListener {

    private static final String QUERY_URL = "http://openlibrary.org/search.json?q=";
    TextView mainTextView;
    EditText mainEditText;
    Button mainButton;
    ListView mainListView;
    JSONAdapter mJSONAdapter = null;
    ShareActionProvider mShareActionProvider;
    // part-2 aveva: ArrayAdapter() e ArrayList()
    private static final String PREFS = "prefs";
    private static final String PREF_NAME = "name";
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO passare a ProgressBar?
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mainTextView = (TextView) findViewById(R.id.main_textView);
        mainButton = (Button) findViewById(R.id.main_button);
        mainEditText = (EditText) findViewById(R.id.main_editText);
        mainButton.setOnClickListener(this);
        mainListView = (ListView) findViewById(R.id.main_listView);

        if (mJSONAdapter != null) {
            mJSONAdapter = new JSONAdapter(this, getLayoutInflater());
        }
        mainListView.setAdapter(mJSONAdapter);
        mainListView.setOnItemClickListener(this);
        displayWelcome();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        if (shareItem != null) {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        }

        setShareIntent();
        return true;
    }

    private void setShareIntent() {

        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mainTextView.getText());
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void queryBooks(String searchString){

        String urlString = "";

        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        AsyncHttpClient client = new AsyncHttpClient();

        setProgressBarIndeterminateVisibility(true);

        client.get(QUERY_URL + urlString, null, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject){
                setProgressBarIndeterminateVisibility(false);
                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();

                try {
                    mJSONAdapter.updateData(jsonObject.optJSONArray("docs"));
                } catch (Exception e) {
                    e.printStackTrace ();
                    Log.d(getString(R.string.app_name), "Something wrong with " + jsonObject.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject error) {
                setProgressBarIndeterminateVisibility(false);
                Toast.makeText(
                        getApplicationContext(),
                        "Error: " + String.valueOf(statusCode) + " - " + throwable.getMessage(),
                        Toast.LENGTH_LONG).show();
                //throwable.printStackTrace();
                Log.e(getString(R.string.app_name),
                        "Error: " + String.valueOf(statusCode) + " - " + throwable.getMessage());
            }

        });
    }

    @Override
    public void onClick(View view) {
        queryBooks(mainEditText.getText().toString());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        JSONObject jsonObject = mJSONAdapter.getItem(position);
        String coverID = jsonObject.optString("cover_i", "");
        Intent detailIntent = new Intent(this, DetailActivity.class);
        detailIntent.putExtra("coverID", coverID);
        // Add any other data you may want to put as extras
        startActivity(detailIntent);
    }

    public void displayWelcome() {
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        String name = mSharedPreferences.getString(PREF_NAME, "");
        if (name.length() > 0) {
            Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_LONG).show();
        } else {

            // otherwise, show a dialog to ask for their name
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.alert_welcome);
            alert.setMessage(R.string.alert_question);

            // Create EditText for entry
            final EditText input = new EditText(this);
            alert.setView(input);

            // Make an "OK" button to save the name
            alert.setPositiveButton(R.string.alert_ok_button, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                // Grab the EditText's input
                String inputName = input.getText().toString();

                // Put it into memory (don't forget to commit!)
                SharedPreferences.Editor e = mSharedPreferences.edit();
                e.putString(PREF_NAME, inputName);
                e.commit();

                // Welcome the new user
                Toast.makeText(getApplicationContext(),
                        getString(R.string.intro_welcome) + inputName + "!",
                        Toast.LENGTH_LONG).show();
            }
        });

        // Make a "Cancel" button
        // that simply dismisses the alert
        alert.setNegativeButton(R.string.alert_cancel_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });

        alert.show();
        }
    }
}