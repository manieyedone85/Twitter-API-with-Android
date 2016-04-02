# Twitter-API-with-Android
twitter api service usind android background service..


#in MainActivity java
  SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
  SharedPreferences.Editor ed = sharedPreferences.edit();
  ed.putString(USER_ID, userid);
  ed.putString(TWITTER_USER_LOGIN, login);
  ed.putString(TWITTER_USER_ACCESS, access);
  ed.putBoolean("TWITTERFLAG", true);
  ed.commit();
  startService(new Intent(getBaseContext(), MyService.class));// for start background service
# in MainActivity onCreate() //function to add following code
  TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
  Fabric.with(this, new Twitter(authConfig));

#global variable
    public static final String SHARED_PREFERENCES = "instantlikepreferences";
    public static final String USER_ID = "user_id";
    public static final Boolean TWITTERFLAG = false;
    public static final String TWITTER_USER_LOGIN = "twitter_user_login";
    public static final String TWITTER_USER_ACCESS = "twitter_user_access";
    public static final String TWITTER_CONSUMER_KEY = "paste your key";
    public static final String TWITTER_CONSUMER_SECRET = "paste your secrete key";
    
#in android manifest
  <service android:name=".MyService"/> // for background service
  <meta-data android:name="io.fabric.ApiKey" android:value="7aea78439bec41a9005c7488bb6751c5e33fe270" /> //for twitter api

#in app gradle

  
buildscript {
    repositories {
        maven { url 'https://maven.fabric.io/public' }
    }

    dependencies {
        classpath 'io.fabric.tools:gradle:1.+'

    }
}

apply plugin: 'android'
apply plugin: 'io.fabric'

android {
    useLibrary 'org.apache.http.legacy'
}
repositories {
    maven { url 'https://maven.fabric.io/public' }
}

*in dependencies
compile('com.twitter.sdk.android:twitter:1.13.0@aar') {
        transitive = true;
    }
    
    *******************enjoy*********************
