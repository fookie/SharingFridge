package android.assignment.sharingfridge;

import android.content.Intent;

import com.daimajia.androidanimations.library.Techniques;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

/**
 * This activity is used to show a launch screen with animated elements
 *<br/>
 *<br/>
 * Used external library: AwesomeSplash
 * Copyright (c) 2015 Viktor Arsovski
 * @see <a href="https://github.com/ViksaaSkool/AwesomeSplash">AwesomeSplash</a>
 */
public class LaunchScreen extends AwesomeSplash {
    @Override
    public void initSplash(ConfigSplash configSplash) {
        configSplash.setBackgroundColor(R.color.colorPrimary);
        configSplash.setAnimCircularRevealDuration(500);
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);  //or Flags.REVEAL_LEFT
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM); //or Flags.REVEAL_TOP

        configSplash.setLogoSplash(R.raw.splash_logo); //or any other drawable
        configSplash.setAnimLogoSplashDuration(1000);
        configSplash.setAnimLogoSplashTechnique(Techniques.FadeIn);

        configSplash.setTitleSplash("SharingFridge");
        configSplash.setTitleTextColor(R.color.green_50);
        configSplash.setTitleTextSize(25f); //float value
        configSplash.setAnimTitleDuration(2000);
        configSplash.setAnimTitleTechnique(Techniques.FadeInDown);
    }

    @Override
    public void animationsFinished() {
        startActivity(new Intent(LaunchScreen.this, HomeActivity.class));
        finishAfterTransition();//Make sure the launch screen only showed once.
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        int pid = android.os.Process.myPid(); //Totally exit the app. Prevent starting HomeActivity. Not a recommended practice though. ref: http://stackoverflow.com/a/2034238/5351002
//        android.os.Process.killProcess(pid);
//        System.exit(0);
//    }
}
