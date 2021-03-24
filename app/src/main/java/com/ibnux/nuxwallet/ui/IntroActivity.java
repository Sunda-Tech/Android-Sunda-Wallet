package com.ibnux.nuxwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_intro_title_1),
                getString(R.string.app_intro_description_1),
                R.drawable.intro_1_crypto
        ));
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_intro_title_2),
                getString(R.string.app_intro_description_2),
                R.drawable.intro_2_blockchain
        ));
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_intro_title_3),
                getString(R.string.app_intro_description_3),
                R.drawable.intro_3_wallet
        ));
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_intro_title_4),
                getString(R.string.app_intro_description_4),
                R.drawable.intro_4_publickey
        ));
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_intro_title_5),
                getString(R.string.app_intro_description_5),
                R.drawable.intro_5_privatekey
        ));
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_intro_title_6),
                getString(R.string.app_intro_description_6),
                R.drawable.intro_6_passphrase
        ));
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_intro_title_7),
                getString(R.string.app_intro_description_7),
                R.drawable.intro_7_fee
        ));
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_intro_title_8),
                getString(R.string.app_intro_description_8),
                R.drawable.nuxcoin
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_intro_title_9),
                getString(R.string.app_intro_description_9),
                R.drawable.nuxcoin
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_intro_title_10),
                getString(R.string.app_intro_description_10),
                R.drawable.airdrop_crypto
        ));
    }

    @Override
    protected void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Aplikasi.sp.edit().putBoolean("isFirst",false).apply();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Aplikasi.sp.edit().putBoolean("isFirst",false).apply();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}