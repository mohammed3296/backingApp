package com.example.mohammedabdullah3296.bakingapp.ui;

import android.app.NotificationManager;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mohammedabdullah3296.bakingapp.R;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StepDetailsActivity extends AppCompatActivity
        implements View.OnClickListener, ExoPlayer.EventListener {
    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mPlayerView;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private static final String TAG = StepDetailsActivity.class.getSimpleName();
    @BindView(R.id.previous_btn)
    Button mDetailStepPrev;

    @BindView(R.id.next_btn)
    Button mDetailStepNext;
    public static int pos;
    public static String reName;
    private long position;
    private boolean playWhenReadyField;
    private Uri url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_details);
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();

        pos = bundle.getInt("position");
        reName = bundle.getString("recipeName");

        if (pos == 0) {
            mDetailStepPrev.setVisibility(View.GONE);
        }
        if (pos == RecipeDetailsActivity.steps.size() - 1) {
            mDetailStepNext.setVisibility(View.GONE);
        }
        if (bundle.getInt("position") != 0) {
            getSupportActionBar().setTitle(bundle.getString("recipeName") + " Step: " + bundle.getInt("position"));
        } else {
            getSupportActionBar().setTitle(bundle.getString("recipeName") + " Introduction");
        }

        ImageView imageView = (ImageView) findViewById(R.id.detail_step_image);
        if ( TextUtils.isEmpty(bundle.getString("ThumbnailURL"))) {
            Picasso.with(this).load(bundle.getString("ThumbnailURL")).into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }

        TextView textView = (TextView) findViewById(R.id.step_description);
        textView.setText(bundle.getString("Description"));


        // Initialize the player view.
        mPlayerView = (SimpleExoPlayerView) findViewById(R.id.detail_step_video);
        // Load the question mark as the background image until the user answers the question.
        mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource
                (getResources(), R.drawable.question_mark));
        position = C.TIME_UNSET;
        if (savedInstanceState != null) {

            position = savedInstanceState.getLong("VideoCurrentTime", C.TIME_UNSET);
            playWhenReadyField = savedInstanceState.getBoolean("playWhenReadyField");
        }
        else {
            playWhenReadyField = true;
        }
        if (TextUtils.isEmpty(bundle.getString("VideoURL"))) {
            mPlayerView.setVisibility(View.GONE);
        } else {
            initializeMediaSession();
            url = Uri.parse(bundle.getString("VideoURL")) ;
            initializePlayer(Uri.parse(bundle.getString("VideoURL")));
        }
    }

    /**
     * Initializes the Media Session to be enabled with media buttons, transport controls, callbacks
     * and media controller.
     */
    private void initializeMediaSession() {

        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(this, TAG);
        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());


        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);

    }

    /**
     * Initialize ExoPlayer.
     *
     * @param mediaUri The URI of the sample to play.
     */
    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);

            // Set the ExoPlayer.EventListener to this activity.
            mExoPlayer.addListener(this);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(this, "ClassicalMusicQuiz");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    this, userAgent), new DefaultExtractorsFactory(), null, null);
            if (position != C.TIME_UNSET) mExoPlayer.seekTo(position);
            mExoPlayer.prepare(mediaSource);
            //   mExoPlayer.setPlayWhenReady(true);
        }
    }


    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
        // mNotificationManager.cancelAll();
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
            initializePlayer(url);
        }
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(playWhenReadyField);
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        playWhenReadyField = playWhenReady ;
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
    }

    @Override
    public void onPositionDiscontinuity() {
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     * Media Session Callbacks, where all external clients control the player.
     */
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return (super.onOptionsItemSelected(item));
    }

    @OnClick(R.id.previous_btn)
    public void prev() {
        Step step = RecipeDetailsActivity.steps.get(pos - 1);
        // Put this information in a Bundle and attach it to an Intent that will launch an AndroidMeActivity
        Bundle b = new Bundle();
        b.putString("ThumbnailURL", step.getThumbnailURL());
        b.putString("VideoURL", step.getVideoURL());
        b.putString("Description", step.getDescription());
        b.putString("recipeName", reName);
        b.putInt("position", pos - 1);
        // Attach the Bundle to an intent
        Intent intent = new Intent(this, StepDetailsActivity.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    @OnClick(R.id.next_btn)
    public void next() {
        Step step = RecipeDetailsActivity.steps.get(pos + 1);
        // Put this information in a Bundle and attach it to an Intent that will launch an AndroidMeActivity
        Bundle b = new Bundle();
        b.putString("ThumbnailURL", step.getThumbnailURL());
        b.putString("VideoURL", step.getVideoURL());
        b.putString("Description", step.getDescription());
        b.putString("recipeName", reName);
        b.putInt("position", pos + 1);
        // Attach the Bundle to an intent
        Intent intent = new Intent(this, StepDetailsActivity.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("VideoCurrentTime",position );
        outState.putBoolean("playWhenReadyField", playWhenReadyField);
    }
}
