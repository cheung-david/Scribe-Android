package com.dc.scribe.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.dc.scribe.R;
import com.dc.scribe.activities.LoadPostActivity;
import com.dc.scribe.activities.MainActivity;
import com.dc.scribe.model.ScribeCard;
import com.dc.scribe.model.ScribeFollowers;
import com.dc.scribe.model.ScribeImage;
import com.dc.scribe.model.ScribeUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String userId = "";

    final int PERMISSION_READ_EXTERNAL = 111;
    private ArrayList<ScribeCard> images = new ArrayList<>();
    private ScribeUser userCard = null;
    private ScribeFollowers followers;
    //private ImageView selectedImage;
    private ImagesAdapter adapter;
    private TextView emptyView;
    private RecyclerView recyclerView;
    private ImageView profileImage;
    private TextView profileTitle, profileBio, profileFollowers, profileFollowing;
    private Button profileFollowBtn;
    private LinearLayout profileLayout;
    private int followerSize = 0;
    private long mLastClickTime = 0;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case PERMISSION_READ_EXTERNAL: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    retrieveAndSetImages();
                }
            }
        }
    }


    public void retrieveAndSetImages() {
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
                            .url(MainActivity.getURL_BASE() + "/posts" + userId)
                            .build();
                    okhttp3.Response response = null;
                    try {
                        response = MainActivity.getOkHttpClient().newCall(request).execute();
                        String jsonData = response.body().string();
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        } else {
                                //Log.v("JSON", "Creating OBJ: " + jsonData);
                                JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                                JsonArray arr = obj.getAsJsonArray("posts");
                                //Log.v("JSON", "Creating ARR: " + arr);
                                ScribeCard[] cards = MainActivity.getGson().fromJson(arr, ScribeCard[].class);
                                if (cards != null && cards.length != 0) {
                                    images.clear();
                                    for (int i = 0; i < cards.length; i++) {
                                        images.add(cards[i]);
                                    }
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //stuff that updates ui
                                                adapter.notifyDataSetChanged();
                                                if (images.isEmpty()) {
                                                    recyclerView.setVisibility(View.GONE);
                                                    emptyView.setVisibility(View.VISIBLE);
                                                } else {
                                                    recyclerView.setVisibility(View.VISIBLE);
                                                    emptyView.setVisibility(View.GONE);
                                                }
//                                            Picasso.with(getContext())
//                                                    .load("https://s3-us-west-2.amazonaws.com/photogriddemo/" + images.get(0).getFilename())
//                                                    .resize(800,800)
//                                                    .centerCrop()
//                                                    .into(selectedImage);
                                            }
                                        });
                                    }
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

    public void retrieveUser() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/user" + userId)
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient2().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        //Log.v("RETRIEVE USER", "Creating OBJ: " + jsonData);
                        JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                        JsonObject jsonObject = obj.getAsJsonObject("user");
                        //Log.v("JSON", "Creating ARR: " + jsonObject);
                        final ScribeUser userCard = MainActivity.getGson().fromJson(jsonObject, ScribeUser.class);
                        if (userCard != null) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (userCard.getProfilePic() != null && userCard.getProfilePic() != "") {
                                            Log.v("PIC", "" + userCard.getProfilePic());
                                            Picasso.Builder builder = new Picasso.Builder(getContext());
                                            builder.listener(new Picasso.Listener()
                                            {
                                                @Override
                                                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
                                                {
                                                    picasso
                                                        .load(uri)
                                                        .placeholder(R.drawable.profile_btn)
                                                        .into(profileImage);
                                                    //exception.printStackTrace();
                                                }
                                            });
                                            builder.build()
                                                    .load(userCard.getProfilePic())
                                                    .resize(150,150)
                                                    .centerCrop()
                                                    .into(profileImage);
                                        } else {
                                            Picasso.with(getActivity())
                                                    .load(R.drawable.profile_btn)
                                                    .resize(150, 150)
                                                    .centerCrop()
                                                    .into(profileImage);
                                        }

                                        if(!MainActivity.getFollowingList().contains(userCard.get_id())) {
                                            profileFollowBtn.setText("Follow");
                                        } else {
                                            profileFollowBtn.setText("Unfollow");
                                        }

                                        profileFollowBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (SystemClock.elapsedRealtime() - mLastClickTime < 700){
                                                    return;
                                                }
                                                mLastClickTime = SystemClock.elapsedRealtime();
                                                if(!MainActivity.getFollowingList().contains(userCard.get_id())) {
                                                    ((Button)view).setText("Unfollow");
                                                    MainActivity.followUser(userCard.get_id());
                                                    MainActivity.notify(userCard.get_id(), "followed", MainActivity.getCurrentlyLoggedInUserName(), "", "");
                                                    if(followers != null && followers.getFollowers() != null) {
                                                        followerSize++;
                                                        profileFollowers.setText("Followers: " + followerSize);
                                                        //retrieveFollowers();
                                                    }
                                                } else {
                                                    ((Button)view).setText("Follow");
                                                    MainActivity.unfollowUser(userCard.get_id());
                                                    if(followers != null && followers.getFollowers() != null) {
                                                        followerSize--;
                                                        if(followerSize < 0) {
                                                            followerSize = 0;
                                                        }
                                                        profileFollowers.setText("Followers: " + followerSize);
                                                        //retrieveFollowers();
                                                    }
                                                }
                                            }


                                        });

                                        profileTitle.setText(userCard.getFullName());
                                        if (userCard.getDescription() != null && userCard.getDescription() != "") {
                                            profileBio.setText(userCard.getDescription());
                                        } else {
                                            profileBio.setText("Feature not implemented in mobile yet. Check out the web version to update your profile and bio.");
                                        }

                                        if (userCard.getFollowing() != null) {
                                            profileFollowing.setText("Following: " + userCard.getFollowing().size());
                                        }
                                    }
                                });
                            }
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

    public void retrieveFollowers() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/followlist" + userId)
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient2().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        Log.v("JSON", "Creating OBJ PROFILE: " + jsonData);
                        JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                        JsonArray arr = obj.getAsJsonArray("followers");
                        Log.v("JSON", "Creating ARR PROFILE: " + arr);
                        followers = MainActivity.getGson().fromJson(obj, ScribeFollowers.class);
                        if (followers != null) {
                            if(getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        followerSize = followers.getFollowers().size();
                                        profileFollowers.setText("Followers: " + followers.getFollowers().size());
                                    }
                                });
                            }
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        //selectedImage = (ImageView)view.findViewById(R.id.selected_image);
        profileImage = (ImageView)view.findViewById(R.id.profile_image);
        profileBio = (TextView)view.findViewById(R.id.profile_bio);
        profileTitle = (TextView)view.findViewById(R.id.profile_title);
        profileFollowers = (TextView)view.findViewById(R.id.profile_followers);
        profileFollowing = (TextView)view.findViewById(R.id.profile_following);
        profileFollowBtn = (Button) view.findViewById(R.id.profile_followBtn);
        profileLayout = (LinearLayout)view.findViewById(R.id.profile_layour_container);
        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                return;
            }
        });

//        Bundle bundle = getActivity().getIntent().getExtras();
//        if(bundle != null) {
//            userId = bundle.getString("userId");
//
//        }

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            String temp = userId.substring(1);
            if(!MainActivity.getCurrentlyLoggedInUser().equals(temp)){
                profileFollowBtn.setVisibility(View.VISIBLE);
            } else {
                profileFollowBtn.setVisibility(View.GONE);
            }
        }

        recyclerView = (RecyclerView)view.findViewById(R.id.content_images);
        adapter = new ImagesAdapter(images);

        recyclerView.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 4);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

        emptyView = (TextView) view.findViewById(R.id.empty_view);

        retrieveUser();
        retrieveAndSetImages();
        retrieveFollowers();

//        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL);
//        } else {
//            retrieveAndSetImages();
//        }


        // Inflate the layout for this fragment
        return view;
    }

    public class ImagesAdapter extends RecyclerView.Adapter<ImageViewHolder> {

        private ArrayList<ScribeCard> images;


        public ImagesAdapter(ArrayList<ScribeCard> images) {
            this.images = images;
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_image, parent, false);
            return new ImageViewHolder(card);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            if(images != null && !images.isEmpty()) {
                final ScribeCard image = images.get(position);
                holder.updateUI(image);

                final ImageViewHolder vHolder = holder;

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), LoadPostActivity.class);
                        intent.putExtra("commentId", image.get_id());
                        startActivity(intent);
                        //selectedImage.setImageDrawable(vHolder.imageView.getDrawable());

                    }
                });
            }
        }
    }



    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image_thumb);
        }

        public void updateUI(ScribeCard image) {
            // Convert url or json data to image
            Picasso.with(getContext())
                    .load("https://s3-us-west-2.amazonaws.com/photogriddemo/" + image.getFilename())
                    .resize(160,160)
                    .centerCrop()
                    .into(imageView);
            //DecodeBitmap task = new DecodeBitmap(imageView, image);
            //task.execute();
        }
    }


    class DecodeBitmap extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<ImageView> mImageViewWeakReference;
        private ScribeImage image;

        public DecodeBitmap(ImageView imageView, ScribeImage image) {
            mImageViewWeakReference = new WeakReference<ImageView>(imageView);
            this.image = image;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return decodeURI(image.getImgResourceUrl().getPath());
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            final ImageView img = mImageViewWeakReference.get();

            if(img != null) {
                img.setImageBitmap(bitmap);
            }
        }
    }


    public Bitmap decodeURI(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        Boolean scaleByHeight = Math.abs(options.outHeight - 100) >= Math.abs(options.outWidth - 100);
        if(options.outHeight * options.outWidth * 2 >= 16384) {
            // Load, scaling to smallest power of 2
            double sampleSize = scaleByHeight
                    ? options.outHeight / 1000
                    : options.outWidth / 1000;
            options.inSampleSize =
                    (int)Math.pow(2d, Math.floor(
                            Math.log(sampleSize)/Math.log(2d)
                    ));
        }

        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[512];
        Bitmap output = BitmapFactory.decodeFile(filePath, options);
        return output;
    }
}
