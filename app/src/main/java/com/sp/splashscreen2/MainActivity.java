package com.sp.splashscreen2;

import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private  static int SPLASH_SCREEN = 5000; //5 SECONDS

    //Variables
    Animation topAnim, bottomAnim;
    ImageView image;
    TextView logo, slogan;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //Media (intro music)
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.happyintro);
        mediaPlayer.start();

        //Animations
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);     //which context we r using the animation and which animation
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        //Hooks
        image=findViewById(R.id.imageView);
        logo=findViewById(R.id.textView3);
        slogan=findViewById(R.id.textView4);

        //Assign animations to image and text
        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);
        slogan.setAnimation(bottomAnim);

        FirebaseApp.initializeApp(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
               // startActivity(intent);
               // finish();
                mediaPlayer.stop();
                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(image, "logo_image"); //animation
                pairs[1] = new Pair<View, String>(logo, "logo_text");  //animation texts

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, pairs);
                startActivity(intent, options.toBundle()); //calls next screen and adds in animation options

            }
        },SPLASH_SCREEN);


    }
}
