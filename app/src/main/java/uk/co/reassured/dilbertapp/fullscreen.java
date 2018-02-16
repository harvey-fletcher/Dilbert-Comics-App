package uk.co.reassured.dilbertapp;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Display;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by hfletcher on 16/02/2018.
 */

public class fullscreen extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        //The image is stored in the shared preferences, we need this so we can access those.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(fullscreen.this);

        //An image that is blank.
        Bitmap bitmap = null;

        try{
            //Decode the image
            byte[] encodeByte = Base64.decode(sharedPreferences.getString("ImageString",""),Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e){
            e.printStackTrace();
        }

        //Load the image into the imageview
        ImageView comic = findViewById(R.id.FullScreenComicView);
        comic.setImageBitmap(bitmap);

        //Measure the height of the screen so we can position the comic in the center
        Display display = getWindowManager().getDefaultDisplay();
        int ScreenHeight = display.getHeight();

        //Rotate image
        comic.measure(0,0);
        comic.setRotationX(0);
        comic.setRotationY(0);
        comic.setRotation(90);

        //Position image
        comic.setY((ScreenHeight / 2) - (comic.getMeasuredHeight() / 2));

        //Resize the image so it is easier to read.
        comic.setScaleX((float)1.6);
        comic.setScaleY((float)1.6);

        //For some reason we need layout params. It works better this way.
        RelativeLayout.LayoutParams comp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        comic.setLayoutParams(comp);
    }
}
