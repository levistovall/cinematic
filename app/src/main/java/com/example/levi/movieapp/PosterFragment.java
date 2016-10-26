package com.example.levi.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractSet;
import java.util.Iterator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PosterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PosterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PosterFragment extends Fragment {
    private ImageAdapter mPosterAdapter;
    private JSONArray moviesArray;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final String LOG_TAG = "LEVI_DEBUG:";

    private OnFragmentInteractionListener mListener;

    public PosterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PosterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PosterFragment newInstance(String param1, String param2) {
        PosterFragment fragment = new PosterFragment();
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
    public void onStart(){
        super.onStart();

        // get shared preferences and pass the stored sort mode to the task as a parameter
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String sortKey = getString(R.string.pref_key_sortmode);
        String defSortVal = getString(R.string.pref_default_sortmode);
        FetchMovieTask task = new FetchMovieTask();
        task.execute(sharedPrefs.getString(sortKey, defSortVal));
    }

    public class FetchMovieTask extends AsyncTask<String, Void, String[]>{

        // returns an array of the poster urls obtained from JSON
        public String[] getMovieDataFromJson(String moviesJsonStr) throws JSONException{

            //store the labels of the json items which need to be collected
            final String TMDB_RESULTS = "results";
            final String TMDB_POSTER_PATH = "poster_path";

            // assign array of results to global variable moviesArray
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

            // make sure posters appear in threes only
            int numPosters = moviesArray.length();
            if(numPosters > 54){
                numPosters = 3*(54/3);
            }else if(numPosters>3){
                numPosters = 3*(numPosters/3);
            }else if(numPosters==0){
                Log.v("CAUGHT!", "there are no results");
            }


            // construct poster urls one by one
            String[] resultStrs = new String[numPosters];
            Log.v("CAUGHT!", resultStrs.toString());
            for(int i = 0; i < numPosters; i++) {
                String imgUrl = "http://image.tmdb.org/t/p/w185/";

                // Get the JSON object representing the movie
                try{
                    JSONObject movie = moviesArray.getJSONObject(i);

                    imgUrl = imgUrl + movie.getString(TMDB_POSTER_PATH);

                    resultStrs[i] = imgUrl;
                }catch(JSONException e){
                    Log.e(LOG_TAG, e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Image URL: " + s);
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params){
            if(params.length == 0){
                Log.v("CAUGHT!", "params length zero");
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            // store my api key, the desired number of results, and the minimum number of votes
            // that a returned movie should have
            String key = "219acc9d6329e6e614e7cf14ccda6944";
            String num_results = "54";
            String min_vote_count = "48";

            // attempt to get the movie results from the api
            try{
                // store base url and query parameters
                final String TMDB_BASE_URL = "https://api.themoviedb.org/3/discover/movie";
                final String SORTMODE_PARAM = "sort_by";
                final String VOTECOUNT_PARAM = "vote_count.gte";
                final String RESULTS_PARAM = "total_results";
                final String GENRE_PARAM = "with_genres";
                final String KEY_PARAM = "api_key";
                final String CAST_PARAM = "with_cast";
                final String START_YEAR_PARAM = "primary_release_date.gte";
                final String END_YEAR_PARAM = "primary_release_date.lte";

                // built the part of the uri which will be used regardless of preferences
                // (sortmode has already been passed in as a parameter of task.execute())
                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendQueryParameter(RESULTS_PARAM, num_results)
                        .appendQueryParameter(SORTMODE_PARAM, params[0])
                        .appendQueryParameter(VOTECOUNT_PARAM, min_vote_count)
                        .appendQueryParameter(KEY_PARAM, key)
                        .build();

                // get shared preferences and prepare to determine whether to add genre and year
                // parameters
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String genreKey = getString(R.string.pref_key_genre);
                String yearsKey = getString(R.string.pref_key_year_range);

                // a set to pass in as a default for the
                AbstractSet dummySet = new AbstractSet() {
                    @Override
                    public Iterator iterator() {
                        return null;
                    }

                    @Override
                    public int size() {
                        return 0;
                    }
                };


                // if a genre is selected and saved to the preferences, add it to the query
                Log.v("detected pref change?", sharedPrefs.getStringSet(genreKey, dummySet).toString());
                if(!(sharedPrefs.getStringSet(genreKey, dummySet).isEmpty())){
                    String genreSelections = sharedPrefs
                            .getStringSet(genreKey, dummySet)
                            .toString()
                            .substring(1, sharedPrefs.getStringSet(genreKey, dummySet).toString().length()-1);
                    builtUri = builtUri.buildUpon().appendQueryParameter(GENRE_PARAM, genreSelections).build();
                }

                // if a year range is selected, add it to the query
                Log.v("SAVED YEAR RANGE", sharedPrefs.getString(yearsKey, ""));
                if(!(sharedPrefs.getString(yearsKey, "").equals(""))){
                    String[] yearRange = sharedPrefs
                            .getString(yearsKey, "")
                            .split("-");
                    Log.v("START YEAR", yearRange[0]);
                    Log.v("End YEAR", yearRange[1]);
                    builtUri = builtUri.buildUpon()
                            .appendQueryParameter(START_YEAR_PARAM, yearRange[0] + "-01-01")
                            .appendQueryParameter(END_YEAR_PARAM, yearRange[1] + "-12-31")
                            .build();
                }

                // if steve buscemi movies are desired, add his code to the query
                String castKey = getString(R.string.pref_key_cast);
                Boolean defCastVal = false;
                if(sharedPrefs.getBoolean(castKey, defCastVal)){
                    builtUri = builtUri.buildUpon().appendQueryParameter(CAST_PARAM, "884").build();
                }

                // get a url from the uri
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URL" + builtUri.toString());

                // Create the request to TheMovieDatabase, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Movies JSON String:" + moviesJsonStr);
            } catch (IOException e) {
                Log.e("PosterFragment", "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PosterFragment", "Error closing stream", e);
                    }
                }
            }

            try{
                return getMovieDataFromJson(moviesJsonStr);
            }catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
                return null;
            }

        }
        @Override
        protected void onPostExecute(String[] result) {

            if(result != null){
                mPosterAdapter.clear();
                if(result.length > 0){
                    for(String s : result){
                        mPosterAdapter.add(s);
                    }
                } else{
                    mPosterAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "Sadly, no movies match your unreasonably specific preferences.",
                            Toast.LENGTH_LONG).show();
                }

            }else{
                Log.v("CAUGHT!", "results null");
            }
        }
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_poster, container, false);

        mPosterAdapter = new ImageAdapter(this.getActivity());

        GridView gridview = (GridView) rootview.findViewById(R.id.gridview_poster);
        gridview.setAdapter(mPosterAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getActivity(), "" + position,
                        Toast.LENGTH_SHORT).show();
                try{
                    String movie = moviesArray.getJSONObject(position).toString();
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, movie);
                    startActivity(intent);
                }catch(JSONException e){
                    Log.e(LOG_TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        return rootview;
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
        Log.v(LOG_TAG, "attached");
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
