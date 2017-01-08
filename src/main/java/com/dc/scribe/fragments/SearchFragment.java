package com.dc.scribe.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dc.scribe.R;
import com.dc.scribe.activities.MainActivity;
import com.dc.scribe.model.ScribeCard;
import com.dc.scribe.model.ScribeUser;
import com.dc.scribe.model.ScribeUserList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import android.support.v4.app.FragmentManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Button exploreBtn;
    private RecyclerView recyclerView;
    private UserCardAdapter adapter;
    private ArrayList<ScribeUser> userList = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        exploreBtn = (Button)view.findViewById(R.id.exploreBtn);
        recyclerView = (RecyclerView)view.findViewById(R.id.content_search);
        adapter = new UserCardAdapter(userList);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        retrieveUserList();

        exploreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment nextFrag = new HomeFragment();
                Bundle args = new Bundle();
                args.putString("globalFeed", "/feed");
                nextFrag.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_search_container, nextFrag)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    public void retrieveUserList() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
//                Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
//                if(cursor != null) {
//                    cursor.moveToFirst();
//
//                    for(int i = 0; i < cursor.getCount(); i++) {
//                        cursor.moveToPosition(i);
//                        Log.v("Test", "URL:" + cursor.getString(1));
//                        ScribeImage img = new ScribeImage(Uri.parse(cursor.getString(1)));
//                        images.add(img);
//                    }
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/users")
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        Log.v("JSON", "Creating OBJ: search: " + jsonData);
//                        JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
//                        JsonArray arr = obj.getAsJsonArray("posts");
//                        Log.v("JSON", "Creating ARR: " + arr);
                        ScribeUserList userCards = MainActivity.getGson().fromJson(jsonData, ScribeUserList.class);

                        if (userCards != null && userCards.getUsers() != null && userCards.getUsers().length != 0) {
                            userList.clear();
                            ArrayList<ScribeUser> temp = new ArrayList<>();
                            for (int i = 0; i < userCards.getUsers().length; i++) {
                                if(MainActivity.getFollowingList().contains(userCards.getUsers()[i].get_id())) {
                                    temp.add((userCards.getUsers()[i]));
                                } else {
                                    userList.add(userCards.getUsers()[i]);
                                }
                            }
                            for(int j = 0; j < temp.size(); j++) {
                                userList.add(temp.get(j));
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //stuff that updates ui
                                    adapter.notifyDataSetChanged();
//                                    if (images.isEmpty()) {
//                                        recyclerView.setVisibility(View.GONE);
//                                    }
//                                    else {
//                                        recyclerView.setVisibility(View.VISIBLE);
//                                    }
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }

    public class UserCardAdapter extends RecyclerView.Adapter<UserCardViewHolder> {

        private ArrayList<ScribeUser> cardUserList = new ArrayList();

        public UserCardAdapter(ArrayList<ScribeUser> cardUserList) {
//            ArrayList<ScribeUser> temp = new ArrayList<>();
//            ArrayList<ScribeUser> result = new ArrayList<>();
//            for(int i = 0; i < cardUserList.size(); i++) {
//                if(MainActivity.getFollowingList().contains(cardUserList.get(i).get_id())) {
//                    temp.add(cardUserList.get(i));
//                } else {
//                    result.add(cardUserList.get(i));
//                }
//            }
//            for(int j = 0; j < temp.size(); j++) {
//                result.add(temp.get(j));
//            }
            this.cardUserList = cardUserList;
        }

        @Override
        public int getItemCount() {
            return cardUserList.size();
        }

        @Override
        public void onBindViewHolder(UserCardViewHolder holder, int position) {
            if(!cardUserList.isEmpty()) {
                final ScribeUser card = cardUserList.get(position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Fragment nextFrag = new ProfileFragment();
                        Bundle args = new Bundle();
                        args.putString("userId", "/" + card.get_id());
                        nextFrag.setArguments(args);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_search_container, nextFrag)
                                .addToBackStack(null)
                                .commit();
//                        FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
//                        FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
//                        fragmentTransaction2.addToBackStack("xyz");
//                        fragmentTransaction2.hide(SearchFragment.this);
//                        fragmentTransaction2.add(R.id.fragment_search_container, nextFrag);
//                        fragmentTransaction2.commit();
                    }
                });
                holder.updateUI(card);
            }
        }

        @Override
        public UserCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user_search, parent, false);
            return new UserCardViewHolder(card);
        }
    }

    public class UserCardViewHolder extends RecyclerView.ViewHolder {
        private ImageView authorImage;
        private TextView cardAuthorName, cardBio;
        private Button followBtn;

        public UserCardViewHolder(View itemView) {
            super(itemView);
            authorImage = (ImageView)itemView.findViewById(R.id.card_user_author_image);
            cardAuthorName = (TextView)itemView.findViewById(R.id.card_user_title);
            cardBio = (TextView)itemView.findViewById(R.id.card_user_bio);
            followBtn = (Button)itemView.findViewById(R.id.followBtn);
        }

        public void updateUI(final ScribeUser userCard) {
            if(userCard.getProfilePic() != null && userCard.getProfilePic() != "") {
                Picasso.with(getContext())
                        .load(userCard.getProfilePic())
                        .placeholder(R.drawable.profile_btn)
                        .into(authorImage);
                // cardAuthorImage.setImageDrawable(null);
                // new DownloadImageTask(cardAuthorImage).execute(card.getAuthorPic());
            } else {
                authorImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_btn));
            }
            cardAuthorName.setText(userCard.getFullName());
            if(userCard.getDescription() != null) {
                if (userCard.getDescription().length() > 20) {
                    cardBio.setText(userCard.getDescription().substring(0, 21) + "...");
                } else {
                    cardBio.setText(userCard.getDescription());
                }
            } else {
                cardBio.setText(userCard.getFullName());
            }
            if(!MainActivity.getFollowingList().contains(userCard.get_id())) {
                followBtn.setText("Follow");
            } else {
                followBtn.setText("Unfollow");
            }
            followBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = ((Button) view).getText().toString();
                    if(!MainActivity.getFollowingList().contains(userCard.get_id())) {
                        ((Button)view).setText("Unfollow");
                        MainActivity.followUser(userCard.get_id());
                        MainActivity.notify(userCard.get_id(), "followed", MainActivity.getCurrentlyLoggedInUserName(), "", "");
                    } else {
                        ((Button)view).setText("Follow");
                        MainActivity.unfollowUser(userCard.get_id());
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        Log.d("SearchFrag", "Resuming");
        super.onResume();
    }
}


