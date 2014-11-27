package net.nonylene.photolinkviewer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.Key;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Twitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TOAuth extends Activity {

    private AsyncTwitter twitter;
    private RequestToken requestToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toauth);
        Button button1 = (Button) findViewById(R.id.oAuthButton);
        button1.setOnClickListener(new Button1ClickListener());
        setListView();
    }

    private void setListView() {
        try {
            MySQLiteOpenHelper sqLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
            SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
            Cursor cursor = database.rawQuery("select rowid _id, * from accounts", null);
            MyCursorAdapter myCursorAdapter = new MyCursorAdapter(getApplicationContext(), cursor, true);
            ListView listView = (ListView) findViewById(R.id.accounts_list);
            listView.setAdapter(myCursorAdapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            final SharedPreferences sharedPreferences = getSharedPreferences("preference", Context.MODE_PRIVATE);
            listView.setItemChecked(sharedPreferences.getInt("account", 1) -1 , true);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    sharedPreferences.edit().putInt("account",position+1).apply();
                }
            });
            database.close();
        } catch (SQLiteException e) {
            Log.e("SQLite", e.toString());
        }
    }

    private TwitterListener twitterListener = new TwitterAdapter() {

        @Override
        public void onException(TwitterException exception, TwitterMethod method) {
            Log.e("twitter", exception.toString());
        }

        @Override
        public void gotOAuthRequestToken(RequestToken token) {
            requestToken = token;
            Uri uri = Uri.parse(requestToken.getAuthorizationURL());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

        @Override
        public void gotOAuthAccessToken(AccessToken token) {
            try {
                Log.v("hoge","hoge");
                String apikey = (String) getText(R.string.twitter_key);
                String apisecret = (String) getText(R.string.twitter_secret);
                Twitter twitterNotAsync = new TwitterFactory().getInstance();
                twitterNotAsync.setOAuthConsumer(apikey, apisecret);
                twitterNotAsync.setOAuthAccessToken(token);
                final String screenName = twitterNotAsync.getScreenName();
                Long myId = twitterNotAsync.getId();
                // encrypt twitter tokens by key
                Key key = Encryption.generate();
                String twitter_token = Encryption.encrypt(token.getToken().getBytes("UTF-8"), key);
                String twitter_tsecret = Encryption.encrypt(token.getTokenSecret().getBytes("UTF-8"), key);
                String keys = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
                // save encrypted keys
                ContentValues values = new ContentValues();
                values.put("userName", screenName);
                values.put("userId", myId);
                values.put("token", twitter_token);
                values.put("token_secret", twitter_tsecret);
                values.put("key", keys);
                // open database
                MySQLiteOpenHelper sqLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
                SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
                database.execSQL("create table if not exists accounts (userName unique, userId integer unique, token, token_secret, key)");
                database.beginTransaction();
                database.delete("accounts", "userId = " + String.valueOf(myId), null);
                database.insert("accounts", null, values);
                database.setTransactionSuccessful();
                database.endTransaction();
                database.close();
                // set oauth_completed frag
                SharedPreferences preferences = getSharedPreferences("preference", MODE_PRIVATE);
                preferences.edit().putBoolean("authorized", true).apply();
                preferences.edit().putString("screen_name", screenName).apply();
                //putting cue to UI Thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TOAuth.this, getString(R.string.toauth_succeeded_token) + " " + screenName, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (UnsupportedEncodingException e) {
                Log.e("gettoken", e.toString());
                //putting cue to UI Thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TOAuth.this, getString(R.string.toauth_failed_encode), Toast.LENGTH_LONG).show();
                    }
                });
                finish();
            } catch (TwitterException e) {
                Log.e("gettoken", e.toString());
                //putting cue to UI Thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TOAuth.this, getString(R.string.toauth_failed_twitter4j), Toast.LENGTH_LONG).show();
                    }
                });
                finish();
            } catch (SQLiteException e) {
                Log.w("SQLite", e.toString());
            }
        }

    };


    class Button1ClickListener implements View.OnClickListener {
        public void onClick(View v) {
            try {
                twitter = new AsyncTwitterFactory().getInstance();
                String apikey = (String) getText(R.string.twitter_key);
                String apisecret = (String) getText(R.string.twitter_secret);
                twitter.setOAuthConsumer(apikey, apisecret);
                twitter.addListener(twitterListener);
                twitter.getOAuthRequestTokenAsync("plviewer://callback");
            } catch (Exception e) {
                Log.e("twitter", e.toString());
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            String oauth = uri.getQueryParameter("oauth_verifier");
            if (oauth != null) {
                twitter.getOAuthAccessTokenAsync(requestToken, oauth);
            } else {
                Toast.makeText(TOAuth.this, getString(R.string.toauth_failed_token), Toast.LENGTH_LONG).show();
            }
        }
    }
}