package com.example.levi.movieapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;


public class DetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String LOG_TAG = "DEETS_LOG";

    //define String for JSON movie data
    private String mMovieStr;

    //define strings for the poster path and the labels of the detail views
    String movieTitle = "The Case of the Missing Movie Data";
    String movieSubtitle = "";
    String movieOverview = "A frustrated app user clicks on" +
            " a movie poster hoping for more information but" +
            " finds that something has come between his or her" +
            " phone and any further data.";
    String posterPath="";
    String movieRating="Prolly like 1";
    String releaseDate="";
    String backdropPath="";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(String param1, String param2) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // store intent
        Intent intent = getActivity().getIntent();
        Log.v(LOG_TAG, "Intent gotten.");

        // inflate the detail layout
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        //process movie data contained in intent
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            Log.v(LOG_TAG, "Intent content detected.");
            mMovieStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            try{
                //convert movie data string back to JSON and get label strings
                JSONObject movie = new JSONObject(mMovieStr);
                movieTitle = movie.getString("title");
                if(movieTitle.split(":").length > 1){
                    movieSubtitle = movieTitle.split(": ")[1];
                    Log.v("SUBTITLE", movieSubtitle);
                    movieTitle = movieTitle.split(":")[0] + ":";
                }
                movieOverview = movie.getString("overview");
                posterPath = movie.getString("poster_path");
                movieRating = movie.getString("vote_average") + "/10";
                releaseDate = getNiceDate(movie.getString("release_date"));
                backdropPath = movie.getString("backdrop_path");
            }catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }

            Log.v(LOG_TAG, mMovieStr);

            // set the top section's background color and set the text color any child textviews of the title frame
            ((LinearLayout)rootView.findViewById(R.id.top_section)).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            for(int i = 0; i<((LinearLayout)rootView.findViewById(R.id.title_frame)).getChildCount(); i++){
                try{
                    ((TextView)((LinearLayout)rootView.findViewById(R.id.title_frame)).getChildAt(i)).setTextColor(
                            getResources().getColor(R.color.colorLightText)
                    );
                }catch(ClassCastException e){
                    Log.v("Whoops: ", "not a text view");
                }
            }
            //populate the title view
            ((TextView)rootView.findViewById(R.id.detail_title_view)).clearComposingText();
            ((TextView)rootView.findViewById(R.id.detail_title_view)).setText(movieTitle);

            //populate subtitle view
            if(!(movieSubtitle.equals(""))){
                ((TextView)rootView.findViewById(R.id.subtitle_view)).clearComposingText();
                ((TextView)rootView.findViewById(R.id.subtitle_view)).setText(movieSubtitle);
            }

            //give background color to the bar that will contain the rating and release date
            ((LinearLayout)rootView.findViewById(R.id.specs_box)).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            for(int i=0; i<((LinearLayout)rootView.findViewById(R.id.specs_box)).getChildCount(); i++){
                try{
                    ((TextView)((LinearLayout)rootView.findViewById(R.id.specs_box)).getChildAt(i)).setTextColor(
                            getResources().getColor(R.color.colorLightText)
                    );
                }catch(ClassCastException e){
                    Log.v("Whoops: ", "not a text view");
                }
            }

            //the date label was replaced with a calendar icon I made in paint
            /**
            //populate the date label view
            ((TextView)rootView.findViewById(R.id.bold_date_label)).clearComposingText();
            ((TextView)rootView.findViewById(R.id.bold_date_label)).setText(getString(R.string.detail_release_date));*/

            // load the calendar icon into the imageview to the left of the view for the release date
            Picasso
                    .with(this.getActivity())
                    .load(R.mipmap.homemade_calendar_white)
                    .resize(32, 32)
                    .into((ImageView)rootView.findViewById(R.id.calendar_icon));

            //populate the date view
            ((TextView)rootView.findViewById(R.id.date_body)).clearComposingText();
            ((TextView)rootView.findViewById(R.id.date_body)).setText(releaseDate);

            //the rating label was replaced with a star icon I made in paint
            /**
            //populate the rating label view
            ((TextView)rootView.findViewById(R.id.bold_rating_label)).clearComposingText();
            ((TextView)rootView.findViewById(R.id.bold_rating_label)).setText(getString(R.string.detail_user_rating));*/

            // load the star icon into the imageview left of the rating textview
            Picasso
                    .with(this.getActivity())
                    .load(R.mipmap.homemade_star_white)
                    .resize(36, 36)
                    .into((ImageView)rootView.findViewById(R.id.star_icon));

            //populate the rating view
            ((TextView)rootView.findViewById(R.id.rating_body)).clearComposingText();
            ((TextView)rootView.findViewById(R.id.rating_body)).setText(movieRating);

            //populate the overview label view
            ((TextView)rootView.findViewById(R.id.bold_overview_label)).clearComposingText();
            ((TextView)rootView.findViewById(R.id.bold_overview_label)).setText(getString(R.string.detail_overview));

            //populate the overview
            ((TextView)rootView.findViewById(R.id.overview_body)).clearComposingText();
            ((TextView)rootView.findViewById(R.id.overview_body)).setText(movieOverview);
            ((TextView)rootView.findViewById(R.id.overview_body)).setMovementMethod(ScrollingMovementMethod.getInstance());

            //build image url and populate movie poster view
            String imgUrl = "http://image.tmdb.org/t/p/w342/" + posterPath;
            ImageView imageView = (ImageView)rootView.findViewById(R.id.movie_poster_view);
            Picasso
                    .with(this.getActivity())
                    .load(imgUrl)
                    .resize(280, 421)
                    .into(imageView);
            imageView.setBackground(getResources().getDrawable(R.drawable.border_image));

            //build backdrop url and populate movie backdrop view
            String backdropUrl = "http://image.tmdb.org/t/p/w780/" + backdropPath;
            ImageView backdropView = (ImageView)rootView.findViewById(R.id.movie_backdrop_view);
            Picasso.with(this.getActivity()).load(backdropUrl).into(backdropView);

            Log.v(LOG_TAG, ((TextView) rootView.findViewById(R.id.detail_title_view)).getText().toString());
        }
        return rootView;
    }

    //A function to turn the JSON date into a nice date
    public String getNiceDate(String date){
        //define an array of month names
        String[] months = new String[12];
        months[0] = "January";
        months[1] = "February";
        months[2] = "March";
        months[3] = "April";
        months[4] = "May";
        months[5] = "June";
        months[6] = "July";
        months[7] = "August";
        months[8] = "September";
        months[9] = "October";
        months[10] = "November";
        months[11] = "December";

        //split the JSON date into year, day, and month
        String[] datePieces = date.split("-");
        Log.v("DATE PIECES LENGTH: ", Integer.toString(datePieces.length));

        //retrun the formatted date
        return months[Integer.parseInt(datePieces[1]) - 1] + " " + datePieces[2] +
                ", " + datePieces[0];
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
