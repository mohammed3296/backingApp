package com.example.mohammedabdullah3296.bakingapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohammedabdullah3296.bakingapp.R;
import com.example.mohammedabdullah3296.bakingapp.fragments.StepsListFragment;
import com.example.mohammedabdullah3296.bakingapp.models.Ingredient;
import com.example.mohammedabdullah3296.bakingapp.models.Recipe;
import com.example.mohammedabdullah3296.bakingapp.models.Step;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailsActivity extends AppCompatActivity implements
        StepsListFragment.OnStepClickListener, View.OnClickListener, ExoPlayer.EventListener {
    private List<Recipe> recipe;
    String recipeName;
    public static List<Step> steps;
    public static List<Ingredient> ingredients;
    private boolean mTwoPane;

    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mPlayerView;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private static final String TAG = RecipeDetailsActivity.class.getSimpleName();
    private static String currentVideo;
    private static String currentPicture;
    private static String currentDescription;
    private long position;
    private boolean playWhenReadyField;
    private Uri url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        Bundle data = getIntent().getExtras();
        recipe = new ArrayList<>();
        recipe = data.getParcelableArrayList("SelectedRecipe");
        recipeName = recipe.get(0).getName();
        getSupportActionBar().setTitle(recipeName);
        steps = recipe.get(0).getSteps();
        ingredients = recipe.get(0).getIngredients();

        getSupportFragmentManager().beginTransaction().add(R.id.laoyoutfor, new StepsListFragment(), "steps").commit();
        if (findViewById(R.id.step_details_video_desc) != null) {// This LinearLayout will only initially exist in the two-pane tablet case
            mTwoPane = true;
            ///////

            position = C.TIME_UNSET;
            ////
            if (savedInstanceState != null) {
                mPlayerView = (SimpleExoPlayerView) findViewById(R.id.detail_step_video);
                mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource
                        (getResources(), R.drawable.question_mark));


                //...your code...
                position = savedInstanceState.getLong("VideoCurrentTime", C.TIME_UNSET);
                playWhenReadyField = savedInstanceState.getBoolean("playWhenReadyField");
                initializeMediaSession();
                url =Uri.parse(savedInstanceState.getString("currentVideo")) ;
                        initializePlayer(Uri.parse(savedInstanceState.getString("currentVideo")));
                Toast.makeText(this, savedInstanceState.getLong("VideoCurrentTime") + "", Toast.LENGTH_SHORT).show();
                ImageView imageView = (ImageView) findViewById(R.id.detail_step_image);
                if (!TextUtils.isEmpty(savedInstanceState.getString("currentPicture"))) {
                    Picasso.with(this).load(savedInstanceState.getString("currentPicture")).into(imageView);
                } else {
                    imageView.setVisibility(View.GONE);
                }

                TextView textView = (TextView) findViewById(R.id.step_description);
                textView.setText(savedInstanceState.getString("currentDescription"));

            } else {
                playWhenReadyField = true;
                mPlayerView = (SimpleExoPlayerView) findViewById(R.id.detail_step_video);
                mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource
                        (getResources(), R.drawable.question_mark));
                initializeMediaSession();
                currentVideo = steps.get(0).getVideoURL();
                currentPicture = steps.get(0).getThumbnailURL();
                currentDescription = steps.get(0).getDescription();
                url = Uri.parse(steps.get(0).getVideoURL()) ;
                initializePlayer(Uri.parse(steps.get(0).getVideoURL()));

                ImageView imageView = (ImageView) findViewById(R.id.detail_step_image);
                if (!TextUtils.isEmpty(steps.get(0).getThumbnailURL())) {
                    Picasso.with(this).load(steps.get(0).getThumbnailURL()).into(imageView);
                } else {
                    imageView.setVisibility(View.GONE);
                }

                TextView textView = (TextView) findViewById(R.id.step_description);
                textView.setText(steps.get(0).getDescription());

            }
        } else {
            // We're in single-pane mode and displaying fragments on a phone in separate activities
            mTwoPane = false;
        }

    }

    @Override
    public void OnStepClickListener(int position) {
        // Toast.makeText(this, "Position clicked ==== " + position, Toast.LENGTH_SHORT).show();
        Step selectedStep = steps.get(position);
        if (mTwoPane) {
            releasePlayer();
            mPlayerView = (SimpleExoPlayerView) findViewById(R.id.detail_step_video);
            mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource
                    (getResources(), R.drawable.question_mark));
            initializeMediaSession();
            currentVideo = selectedStep.getVideoURL();
            currentPicture = selectedStep.getThumbnailURL();
            currentDescription = selectedStep.getDescription();
            initializePlayer(Uri.parse(selectedStep.getVideoURL()));

            ImageView imageView = (ImageView) findViewById(R.id.detail_step_image);
            if (!TextUtils.isEmpty(selectedStep.getThumbnailURL())) {
                Picasso.with(this).load(selectedStep.getThumbnailURL()).into(imageView);
            } else {
                imageView.setVisibility(View.GONE);
            }

            TextView textView = (TextView) findViewById(R.id.step_description);
            textView.setText(selectedStep.getDescription());

        } else {
            // Put this information in a Bundle and attach it to an Intent that will launch an AndroidMeActivity
            Bundle b = new Bundle();
            b.putString("ThumbnailURL", selectedStep.getThumbnailURL());
            b.putString("VideoURL", selectedStep.getVideoURL());
            b.putString("Description", selectedStep.getDescription());
            b.putString("recipeName", recipeName);

            b.putInt("position", position);

            // Attach the Bundle to an intent
            Intent intent = new Intent(this, StepDetailsActivity.class);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    private void initializeMediaSession() {

        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(this, TAG);
        Log.e("<><><><><><><><>", " mMediaSession.getCallingPackage().toString()");

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setMediaButtonReceiver(null);
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(new MySessionCallback());
        mMediaSession.setActive(true);

    }

    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);
            mExoPlayer.addListener(this);
            String userAgent = Util.getUserAgent(this, "ClassicalMusicQuiz");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    this, userAgent), new DefaultExtractorsFactory(), null, null);
            if (position != C.TIME_UNSET) mExoPlayer.seekTo(position);
            mExoPlayer.prepare(mediaSource);
            //   mExoPlayer.setPlayWhenReady(true);
        }
    }

    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayer.seekTo(0);
        }
    }

    /**
     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
     */
    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }

    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mExoPlayer != null) {
            position = mExoPlayer.getCurrentPosition();
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            releasePlayer();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || mExoPlayer == null) {
            if(mTwoPane)
            initializePlayer(url);
        }
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(playWhenReadyField);
        }
    }
/*
    //////////////////
    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        shouldAutoPlay = true;
        clearResumePosition();
        setIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseAdsLoader();
    }*/
    /////////////////

    public void onClick(View v) {

    }

    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    public void onLoadingChanged(boolean isLoading) {

    }

    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        playWhenReadyField = playWhenReady;
        Toast.makeText(this, playWhenReady + "", Toast.LENGTH_SHORT).show();
    }

    public void onPlayerError(ExoPlaybackException error) {

    }

    public void onPositionDiscontinuity() {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTwoPane) {
            outState.putString("currentVideo", currentVideo);
            outState.putString("currentPicture", currentPicture);
            outState.putString("currentDescription", currentDescription);
            outState.putLong("VideoCurrentTime", position);
            outState.putBoolean("playWhenReadyField", playWhenReadyField);
        }
    }
}