package android.assignment.sharingfridge;

import android.content.Intent;

import com.daimajia.androidanimations.library.Techniques;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

public class LaunchScreen extends AwesomeSplash {
    @Override
    public void initSplash(ConfigSplash configSplash) {
        configSplash.setBackgroundColor(R.color.orange);
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
        finish();
    }
}
