package me.rlxu.flixster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.rlxu.flixster.models.Movie;

public class MovieDetailsActivity extends AppCompatActivity {

    // Constants
    // API base URL
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    // API key parameter name
    public final static String API_KEY_PARAM = "api_key";
    // tag for logging from this activity
    public final static String TAG = "MovieDetailsActivity";

    // Movie to be displayed
    Movie movie;
    // instance fields
    AsyncHttpClient client;
    // movie video id
    String videoId;

    // declare view objects and resolve with Butterknife
    @BindView(R.id.tvTitle) TextView tvTitle;
    @BindView(R.id.tvOverview) TextView tvOverview;
    @BindView(R.id.rbVoteAverage) RatingBar rbVoteAverage;
    @BindView(R.id.ivBackdropImg) ImageView ivBackdropImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        // start Butterknife
        ButterKnife.bind(this);
        client = new AsyncHttpClient();

        // unwrap and assign movie passed in
        movie = Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // set title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = (float) movie.getVoteAverage();
        rbVoteAverage.setRating(voteAverage > 0 ? voteAverage / 2.0f : voteAverage);

        // get correct placeholder and imageView for the orientation
        String imageUrl = Parcels.unwrap(getIntent().getParcelableExtra("backdrop image"));;
        int placeholderId = R.drawable.flicks_backdrop_placeholder;
        ImageView imageView = ivBackdropImg;

        Glide.with(this)
                .load(imageUrl)
                .apply(
                        RequestOptions.placeholderOf(placeholderId)
                                .error(placeholderId)
                                .fitCenter()
                                .transform(new RoundedCornersTransformation(20, 0))

                ).into(imageView);

        // get video id of movie
        getTrailerVideos(movie.id);

        // setup onClickListener to play movie
        ivBackdropImg.setOnClickListener(new VideoClick());
    }

    class VideoClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (videoId != null) {
                Intent intent = new Intent(getBaseContext(), MovieTrailerActivity.class);
                intent.putExtra("video id", Parcels.wrap(videoId));
                getBaseContext().startActivity(intent);
            }
        }
    }

    // get the video for the movie from API
    private void getTrailerVideos(Integer id) {
        // create the URL
        String url = API_BASE_URL + "/movie/" + id + "/videos";
        // set the request params
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); // API key always required
        // execute GET request and expect JSON response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    if (results != null) {
                        videoId = results.getJSONObject(0).getString("key");
                    }
                    Log.i(TAG, String.format("Loaded trailer video"));
                } catch (JSONException e) {
                    logError("Failed to parse trailer video", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get trailer for movie", throwable, true);
            }


        });
    }

    // handle errors, log and alert user
    private void logError(String message, Throwable error, boolean alertUser) {
        // always log the error
        Log.e(TAG, message, error);
        // alert the user to avoid silent errors
        if (alertUser) {
            // long toast
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

}
