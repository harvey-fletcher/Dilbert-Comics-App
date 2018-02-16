package uk.co.reassured.dilbertapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class home extends AppCompatActivity {

    //This is the date object
    public Date date = new Date();

    //This object gets used to format the date in the right format
    public SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    //This is the date for the dilbert comic we are displaying
    public String CurrentDate = null;

    //This is where the API is located
    public String APIHost = "http://82.10.188.99/dilbert-app/api.php";

    //This is where we will store the data from our api
    public JSONObject DilbertDetails = new JSONObject();

    //This is the title.
    public TextView dilbert_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //We need to link the title object to something on the layout so it can be seen in the GUI, and also text set in the code.
        dilbert_title = findViewById(R.id.dilbert_title);

        //Make sure that the default comic that gets loaded is today's
        CurrentDate = sdf.format(date.getTime());

        //Load the comic
        GetDilbertDetails(CurrentDate);

        //The button to load the previous comic
        ImageView PrevComic = findViewById(R.id.dilbert_previous);
        PrevComic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreviousImage();
            }
        });

        //The button to load the next comic
        ImageView NextComic = findViewById(R.id.dilbert_next);
        NextComic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NextImage();
            }
        });
    }

    public void PreviousImage(){
        try{
            //Set the title so the user knows the page is loading.
            dilbert_title.setText("Loading...");

            //Get the current date
            Date StringAsDate = sdf.parse(CurrentDate);

            //Setup a calendar so we can go back in time
            Calendar cal = Calendar.getInstance();
            cal.setTime(StringAsDate);
            cal.add(Calendar.DAY_OF_YEAR, -1);

            //Save that as the current date
            CurrentDate = sdf.format(cal.getTime());

            //Connect to our API and retrieve the details for that date's comic
            GetDilbertDetails(CurrentDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void NextImage(){
        try{
            //Set the title so the user knows the page is loading.
            dilbert_title.setText("Loading...");

            //Get the current date
            Date StringAsDate = sdf.parse(CurrentDate);

            //Setup a calendar so we can go back in time
            Calendar cal = Calendar.getInstance();
            cal.setTime(StringAsDate);
            cal.add(Calendar.DAY_OF_YEAR, +1);

            //Save that as the current date
            CurrentDate = sdf.format(cal.getTime());

            //Connect to our API and retrieve the details for that date's comic
            GetDilbertDetails(CurrentDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GetDilbertDetails(String date){
        //We need to get the daily comic data from our API
        String url = APIHost + "?date=" + CurrentDate;

        PerformGetRequest(new OnJSONResponseCallback() {
            @Override
            public JSONObject onJSONResponse(boolean success, JSONObject response) {
                try{
                    if(response.getInt("status") == 200) {

                        DisplayDilbertImage(new OnBitmapResponseCallback() {
                            @Override
                            public Bitmap onBitmapResponse(boolean success, final Bitmap bitmap) {
                                ImageView dilbert_comic = findViewById(R.id.dilbert_comic);
                                dilbert_comic.setImageBitmap(bitmap);

                                //Change the page title to the date of the comic, in UK date format
                                try {
                                    SimpleDateFormat UKDF = new SimpleDateFormat("dd-MM-yyyy");
                                    Date ComicDate = sdf.parse(CurrentDate);
                                    String ComicDateString = UKDF.format(ComicDate);
                                    dilbert_title.setText(ComicDateString);
                                } catch (Exception e){
                                    Toast.makeText(home.this, "Unexpected Error!", Toast.LENGTH_SHORT);
                                }

                                dilbert_comic.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        //We need to compress the image into a byte array
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                                        byte[] b=baos.toByteArray();
                                        String ImageString = Base64.encodeToString(b, Base64.DEFAULT);

                                        //Transfer the image to the new activity
                                        try{
                                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(home.this).edit();
                                            editor.putString("ImageString", ImageString);
                                            editor.commit();
                                        } catch (Exception e){
                                            e.printStackTrace();
                                            return;
                                        }

                                        //Create an intent for the new activity
                                        Intent FullScreenComic = new Intent(home.this, fullscreen.class);

                                        //Start the new activity.
                                        startActivity(FullScreenComic);
                                    }
                                });

                                return null;
                            }
                        }, response.getString("url").replace("\\/", "/"));

                    } else {
                        Toast.makeText(home.this, "There is no image available.", Toast.LENGTH_SHORT).show();
                        Date date = new Date();
                        CurrentDate = sdf.format(date.getTime());

                        //Change the page title to the date of the comic, in UK date format
                        try {
                            SimpleDateFormat UKDF = new SimpleDateFormat("dd-MM-yyyy");
                            Date ComicDate = sdf.parse(CurrentDate);
                            String ComicDateString = UKDF.format(ComicDate);
                            dilbert_title.setText(ComicDateString);
                        } catch (Exception e){
                            Toast.makeText(home.this, "Unexpected Error!", Toast.LENGTH_SHORT);
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    public void DisplayDilbertImage(final OnBitmapResponseCallback callback, String url){
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                InputStream is = new ByteArrayInputStream(responseBody);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                callback.onBitmapResponse(true, bitmap);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println("Error: " + statusCode);
            }
        });
    }

    public interface OnBitmapResponseCallback{
        public Bitmap onBitmapResponse(boolean success, Bitmap bitmap);
    }

    public interface OnJSONResponseCallback {
        public JSONObject onJSONResponse(boolean success, JSONObject response);
    }

    public void PerformGetRequest(final OnJSONResponseCallback callback) {
        //This is the client we will use to make the request.
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(APIHost + "?date=" + CurrentDate, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String ResponseString = new String(responseBody);
                    JSONObject ResponseObject = new JSONObject(ResponseString);
                    callback.onJSONResponse(true, ResponseObject);
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on success: " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    Toast.makeText(home.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on failure: " + e.toString());
                }
            }
        });
    }
}
