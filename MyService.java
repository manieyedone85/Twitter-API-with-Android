package com.manig.twitterlike;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.SearchService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;


/**
 * Created by hai on 06-Feb-16.
 */
public class MyService extends Service {
    // constant
    public static final long NOTIFY_INTERVAL = 30*60* 1000; // 30 min

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;
    private Thread t;
    private String jsonStr;
    String response;
    String responseret;

    String response1;
    TwitterApiClient twitterApiClient;
    int localtagindex=0;

    int tagsearchindex=0;
    JSONObject authToken;

    JSONObject loginuser;

    long tuserid;
    String screenname;
    TwitterSession session;
    TwitterAuthToken mytoken;
    TwitterSession session1;
    TwitterAuthToken authToken1;
    String token1;
    String secret1;
    JSONObject jp;

    JSONArray jarr = null;

    JSONObject jsonObj = null;

    List<Tweet> tweets = null;


   // String url = "http://192.168.1.15:81/ManiPs/ADMIN/webservice.php";

    String twitaccess;

    String luserid;
    String login;

    Boolean twitterflag=false;

    HttpClient hc;
    public static final String SHARED_PREFERENCES = "likepreferences";
    public static final String USER_ID = "user_id";


    public static final Boolean TWITTERFLAG = false;

    public static final String TWITTER_USER_LOGIN = "twitter_user_login";
    public static final String TWITTER_USER_ACCESS = "twitter_user_access";

    public static final String TWITTER_CONSUMER_KEY ="twitter key";
    public static final String TWITTER_CONSUMER_SECRET ="twitter secrete key";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // cancel if already existed

        TwitterAuthConfig authConfig =  new TwitterAuthConfig(TWITTER_CONSUMER_KEY,TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);

        Boolean twitterinfo=sharedPreferences.getBoolean("TWITTERFLAG", false);

        if(twitterinfo == true ) {
            luserid = sharedPreferences.getString(USER_ID, "");
            login = sharedPreferences.getString(TWITTER_USER_LOGIN, "");
            twitaccess = sharedPreferences.getString(TWITTER_USER_ACCESS, "");
            twitterflag = sharedPreferences.getBoolean("TWITTERFLAG", false);
        }

        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread



            mHandler.post(new Runnable() {

                @Override
                public void run() {


                    //////// oauth start

                    Log.e(" Success ", " innr service");



                    if (twitterflag == true ) {
                        try {
                            authToken = new JSONObject(twitaccess);

                            String token = authToken.getString("accessTokenKey");
                            String secret = authToken.getString("accessTokenSecret");
                            loginuser = new JSONObject(login);
                            tuserid = Integer.parseInt(loginuser.getString("id"));
                            screenname = loginuser.getString("screen_name");
                            mytoken = new TwitterAuthToken(token, secret);
                            session = new TwitterSession(mytoken, tuserid, screenname);
                            Twitter.getSessionManager().setActiveSession(session);
                            session1 = Twitter.getSessionManager().getActiveSession();
                            authToken1 = session1.getAuthToken();

                            token1 = authToken1.token;
                            secret1 = authToken1.secret;

                            Log.e(" Access Token ", token1);
                            Log.e(" Access secret  ", secret1);


                        } catch (JSONException e) {
                        }

                        /////////   oauth  end


                        Log.e("Success", "my service fn end oauth");
                        responseret = getTags("Twitter");

                        Log.e(" Return Value ", responseret.toString());

                        try {
                            jsonObj = new JSONObject(responseret);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            jarr = new JSONArray(jsonObj.getString("curr_media"));
                            Log.e(" Return Value ", "Length local tag " + jarr.length());
                            tagSearch();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }

            });
        }

        /*private String getDateTime() {
            // get date time in custom format
            SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd - HH:mm:ss]");
            return sdf.format(new Date());
        }*/

    }

    public String getTags(String media) // here i get my saved tags from my database
    {
        try {
            //Toast.makeText(getApplicationContext(), "Get TAG fun success from "+media, Toast.LENGTH_LONG).show();
            // http client
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            // Checking http request method type

            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            postParams.add(new BasicNameValuePair("opt", "gettags"));
            postParams.add(new BasicNameValuePair("id", luserid));
            postParams.add(new BasicNameValuePair("smedia", media));
            // adding post params

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(postParams));
            }catch (UnsupportedEncodingException ue) {
                Log.d(" Unsupported Encoding ", " Excepti");
            }

            try {

                httpResponse = httpClient.execute(httpPost);

            }catch (IOException ioee){

            }

            httpEntity = httpResponse.getEntity();
            response1 = EntityUtils.toString(httpEntity);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        }

        Log.e(" Return value ", response1);
        /////****************** tages end ******************/////////////

        return response1;
    }
    public void start ()
    {

        if (t == null)
        {
            t = new Thread (String.valueOf(this));
            t.start ();
        }
    }

    public void tagSearch() throws JSONException {

        Log.e("Success"," inner search");
        //Toast.makeText(getApplicationContext(), "inside twitter tag search", Toast.LENGTH_LONG).show();
        twitterApiClient = TwitterCore.getInstance().getApiClient(session1);
        SearchService ser = twitterApiClient.getSearchService();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        //System.out.println(dateFormat.format(date));
        Log.e("Success", dateFormat.format(date));
        if(jarr.length()>0) {
            jp = jarr.getJSONObject(localtagindex);
            String search="%23"+jp.getString("socialtag");
            //ser.tweets("%23cskcoolsenthil", null,null,null,"mixed",15,"2016-03-14",0L,0L,false,new Callback<Search>() {
            //ser.tweets(search, null, null, null, "mixed", 15, dateFormat.format(date), 0L, 0L, false, new Callback<Search>() {
            ser.tweets(search, null, null, null, "mixed", 15, null, 0L, 0L, false, new Callback<Search>() {
                @Override
                public void success(Result<Search> result) {
                    //Do something with result, which provides a Tweet inside of result.data
                    Log.e("Success", "search success");
                    //Toast.makeText(getApplicationContext(), "twitter tag search success", Toast.LENGTH_LONG).show();
                   /* try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    tweets = result.data.tweets;
                    postLike();


                }

                public void failure(TwitterException exception) {
                    //Do something on failure
                    Log.e("Failure", "search failure");
                   // Toast.makeText(getApplicationContext(), "twitter tag search failure", Toast.LENGTH_LONG).show();
                    tagsearchindex=0;
                    localtagindex++;
                    if(jarr.length()>localtagindex) {


                        try {
                            tagSearch();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{
                        localtagindex=0;
                    }
                }
            });

        }

    }

    public void postLike()
    {
        Log.e("Success", "innr fav fn");
       // for (int i = 0; i < tweets.size(); i++) {
       // Toast.makeText(getApplicationContext(), "inside twitter like fun", Toast.LENGTH_LONG).show();
            if(tweets.size()>tagsearchindex){
            Long ids = tweets.get(tagsearchindex).getId();
            FavoriteService favoriteService = twitterApiClient.getFavoriteService();
            favoriteService.create(ids, null, new Callback<Tweet>() {
                @Override
                public void success(Result<Tweet> result) {
                    //Do something with result, which provides a Tweet inside of result.data
                    Log.e("Success", "fav success");
                    //Toast.makeText(getApplicationContext(), "inside twitter like success", Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), "twitter fav success", Toast.LENGTH_LONG).show();
                   /* try {
                        Thread.sleep(60*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    tagsearchindex++;
                    if(tweets.size()>tagsearchindex){

                        postLike();

                    }else {
                        tagsearchindex=0;

                        localtagindex++;
                        if(jarr.length()>localtagindex) {


                            try {
                                tagSearch();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }else{
                            localtagindex=0;
                        }
                    }

                }

                public void failure(TwitterException exception) {
                    //Do something on failure
                    Log.e("Failure", "fav failure");
                   // Toast.makeText(getApplicationContext(), "inside twitter like failure", Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), "twitter fav failure", Toast.LENGTH_LONG).show();
                    tagsearchindex++;
                    if(tweets.size()>tagsearchindex){

                        postLike();

                    }else {
                        tagsearchindex=0;

                        localtagindex++;
                        if(jarr.length()>localtagindex) {


                            try {
                                tagSearch();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }else{
                            localtagindex=0;
                        }
                    }

                }
            });

        }else{
                Log.e("Failure", "cant access tag");
               // Toast.makeText(getApplicationContext(), "over limit oauth", Toast.LENGTH_LONG).show();
               // Toast.makeText(getApplicationContext(), "Cano't access tag", Toast.LENGTH_LONG).show();
                tagsearchindex=0;

                localtagindex++;
                if(jarr.length()>localtagindex) {


                    try {
                        tagSearch();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    localtagindex=0;
                }
            }
    }




}
