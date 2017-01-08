package com.dc.scribe.fragments;


import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dc.scribe.CustomUtility;
import com.dc.scribe.R;
import com.dc.scribe.activities.LoadPostActivity;
import com.dc.scribe.activities.MainActivity;
import com.dc.scribe.model.ScribeCard;
import com.dc.scribe.model.ScribeComment;
import com.dc.scribe.model.ScribeCommentAuthor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Comment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String feedTypeUrl = "/myfeed";
    private Boolean isGlobal = false;

    private FeedAdapter adapter;
    private ArrayList<ScribeCard> mCards = new ArrayList<>();
    private int descriptionViewFullHeight;
    private TextView emptyView, errorView;
    private RecyclerView recyclerView;
    private int containerFragId = R.id.fragment_home_container;
    private long mLastClickTime = 0;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().getString("globalFeed") != null) {
                feedTypeUrl = "/feed";
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("CreatingView", "Home");
        if (getArguments() != null) {
            Log.d("Args", getArguments().getString("globalFeed"));
            if (getArguments().getString("globalFeed") != null) {
                feedTypeUrl = "/feed";
                isGlobal = true;
                containerFragId = R.id.fragment_search_container;
            }
        }
        // Async API call for feed data
        getLocalFeedApi();

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.content_feed);
        adapter = new FeedAdapter(mCards);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        emptyView = (TextView)view.findViewById(R.id.empty_view);
        errorView = (TextView)view.findViewById(R.id.error_view);

        return view;
    }

    public void getLocalFeedApi(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                    //Log.v("MAIN ACTIVITY ASYNC", "SUCCESS: " + MainActivity.getToken());
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .header("authorization", MainActivity.getToken())
                            .url(MainActivity.getURL_BASE() + feedTypeUrl)
                            .build();

                    try {
                        okhttp3.Response response = MainActivity.getOkHttpClient().newCall(request).execute();
                        String jsonData = response.body().string();
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        } else {
                                errorView.setVisibility(View.GONE);
                                Log.v("JSON", "Creating OBJ: " + jsonData);
                                ScribeCard[] cards = MainActivity.getGson().fromJson(jsonData, ScribeCard[].class);
                                if (cards != null) {
                                    mCards.clear();
                                    for (int i = 0; i < cards.length; i++) {
                                        if(isGlobal) {
                                            if(cards[i].getAuthorId() != MainActivity.getCurrentlyLoggedInUser()) {
                                                mCards.add(cards[i]);
                                            }
                                        } else {
                                            mCards.add(cards[i]);
                                        }
                                    }

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //stuff that updates ui
                                            adapter.notifyDataSetChanged();
                                            if (mCards.isEmpty()) {
                                                recyclerView.setVisibility(View.GONE);
                                                emptyView.setVisibility(View.VISIBLE);
                                            } else {
                                                recyclerView.setVisibility(View.VISIBLE);
                                                emptyView.setVisibility(View.GONE);
                                            }
                                        }
                                    });

                                }
                        }
                    } catch (Exception e) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //stuff that updates ui
                                    errorView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                            e.printStackTrace();
                            return;
                    }
                    return;
                }
    });
    }


    public void addComment(final String postId, final String commentContent, final CommentAdapter adapter){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.v("Comment" , "" + commentContent);
                RequestBody formBody = new FormBody.Builder()
                        .add("comment", commentContent)
                        .add("isPersistent", "true")
                        .add("setCookie", "true")
                        .add("withCredentials", "true")
                        .build();;


                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/comments/" + postId)
                        .post(formBody)
                        .build();

                try {
                    okhttp3.Response response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        errorView.setVisibility(View.GONE);
                        Log.v("JSON", "Sent Comment: " + jsonData);
//                        ScribeCard[] cards = MainActivity.getGson().fromJson(jsonData, ScribeCard[].class);
//                        if (cards != null) {
//                            mCards.clear();
//                            for (int i = 0; i < cards.length; i++) {
//                                mCards.add(cards[i]);
//                            }
                        JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                        JsonObject jsonObject = obj.getAsJsonObject("comment");
                        final ScribeComment newComment = MainActivity.getGson().fromJson(jsonObject, ScribeComment.class);
                        Log.v("JSON", "Recv Comment: " + newComment.getText());
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.addComment(newComment);
                                    //stuff that updates ui
                                    Toast.makeText(getContext(),"Comment posted!", Toast.LENGTH_SHORT).show();
                                }
                            });
//
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                return;
            }
        });
    }

    public class FeedAdapter extends RecyclerView.Adapter<FeedViewHolder> {
        ArrayList<ScribeCard> scribeCards;
        int descriptionViewFullHeight;
        RecyclerView commentsList;
        View card;

//        private void toggleCommentHeight() {
//
//            int descriptionViewMinHeight = 100;
//            Log.v("Comment", "Height " + commentsList.getHeight());
//            if (descriptionViewFullHeight > descriptionViewMinHeight) {
//                Log.v("Comment", "Height entered");
//                // expand
//                ValueAnimator anim = ValueAnimator.ofInt(commentsList.getMeasuredHeightAndState(),
//                        card.getHeight() + descriptionViewFullHeight);
//                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                        int val = (Integer) valueAnimator.getAnimatedValue();
//                        ViewGroup.LayoutParams layoutParams = card.getLayoutParams();
//                        layoutParams.height = val;
//                        card.setLayoutParams(layoutParams);
//                    }
//                });
//                anim.start();
//            } else {
//                // collapse
//                ValueAnimator anim = ValueAnimator.ofInt(card.getMeasuredHeightAndState(),
//                        descriptionViewMinHeight);
//
//                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                        int val = (Integer) valueAnimator.getAnimatedValue();
//                        ViewGroup.LayoutParams layoutParams = card.getLayoutParams();
//                        layoutParams.height = val;
//                        card.setLayoutParams(layoutParams);
//                    }
//                });
//                anim.start();
//            }
//        }

        public FeedAdapter(ArrayList<ScribeCard> scribeCard) {
            this.scribeCards = scribeCard;
        }

        @Override
        public int getItemCount() {
            if(scribeCards != null && !scribeCards.isEmpty()) {
                return scribeCards.size();
            } else {
                return 0;
            }
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_feed, parent, false);
            commentsList = (RecyclerView) card.findViewById(R.id.content_card_comment_list);
//            card.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
//                    .OnPreDrawListener() {
//                @Override
//                public boolean onPreDraw() {
//                    commentsList.getViewTreeObserver().removeOnPreDrawListener(this);
//                    // save the full height
//                    descriptionViewFullHeight = commentsList.getHeight();
//                    Log.v("Comment", "Height MAX" + commentsList.getHeight());
//                    // initially changing the height to min height
//                    ViewGroup.LayoutParams layoutParams = commentsList.getLayoutParams();
//                    layoutParams.height = 100;
//                    commentsList.setLayoutParams(layoutParams);
//
//                    return true;
//                }
//            });
//            card.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Log.v("COMMENT", "CLICKED");
//                    toggleCommentHeight();
//                }
//            });
            return new FeedViewHolder(card);
        }

        @Override
        public void onBindViewHolder(FeedViewHolder holder, int position) {
            if(scribeCards != null && !scribeCards.isEmpty()) {
                ScribeCard scribeCard = scribeCards.get(position);

                // Retrieve the latest comments
                ScribeComment[] newComments = scribeCards.get(position).getComments();
                ArrayList<ScribeComment> convertedComments = new ArrayList<>();

                // Convert array to arraylist
                if(newComments != null){
                    for(int i = 0; i < newComments.length; i++) {
                        convertedComments.add(newComments[i]);
                    }
                }

                // This is necessary to update the commentAdapter contents after the first initialization
                holder.commentAdapter.setComments(convertedComments);

                //holder.setCommentList(convertedComments);

                // Update the UI
                holder.updateUI(scribeCard);
            }
        }
    }


    public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private ArrayList<ScribeComment> comments = new ArrayList<>();

        public CommentAdapter(ArrayList<ScribeComment> newComments) {
            comments = newComments;
        }

        // For comment updates
        public void setComments(ArrayList<ScribeComment> newComment) {
            if(comments != newComment) {
                comments = newComment;
                notifyDataSetChanged();
            }
        }

        public void addComment(ScribeComment newComment) {
            comments.add(0, newComment);
            notifyDataSetChanged();
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View comment = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_comment_list, parent, false);
            return new CommentViewHolder(comment);
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            //holder.setIsRecyclable(false);
            ScribeComment scribeComment = comments.get(position);
            holder.updateUI(scribeComment);
            holder.itemView.setTag(position);
        }
    }


    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView commentAuthorImage;
        private TextView commentAuthorName;
        private TextView commentText;
        private TextView commentDate;

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentAuthorImage = (ImageView) itemView.findViewById(R.id.comment_author_image);
            commentAuthorName = (TextView) itemView.findViewById(R.id.comment_author_name);
            commentText = (TextView) itemView.findViewById(R.id.comment_text);
            commentDate = (TextView) itemView.findViewById(R.id.comment_date);
        }

        public void updateUI(ScribeComment comment) {
            ScribeCommentAuthor commentAuthor = comment.getCommentAuthor();
            if(commentAuthor != null) {
                // Log.v("AUTHOR", "author: " + commentAuthor);
                commentAuthorName.setText(commentAuthor.getFullName());
                if(commentAuthor.getProfilePic() != null && commentAuthor.getProfilePic() != "") {
                    Picasso.with(getContext())
                            .load(commentAuthor.getProfilePic())
                            .placeholder(R.drawable.profile_btn)
                            .into(commentAuthorImage);
                } else {
                    commentAuthorImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_btn));
                }
            } else {
                commentAuthorName.setText("");
            }
            commentDate.setText("");
            commentText.setText("");
            if(comment.getDate() != null) {
                commentDate.setText(CustomUtility.timeSince(comment.getDate()));
            }

            if(comment.getText() != null) {
                commentText.setText(comment.getText());
            }
        }
    }


    public class FeedViewHolder extends RecyclerView.ViewHolder {
        private ImageView cardImage, likeBtn;
        private ImageView cardAuthorImage;
        private TextView cardTitle;
        private TextView cardDate;
        private TextView cardLikesCount;
        private TextView cardCommentsCount;
        private TextView commentContent;
        private ImageView commentSendBtn;
        private CommentAdapter commentAdapter;
        private ArrayList<ScribeComment> commentList = new ArrayList<>();
        private RecyclerView recyclerView;
        private LinearLayout cardHeader;
        private Button cardFollowBtn;

        public void setCommentList(ArrayList<ScribeComment> commentList) {
            this.commentList = commentList;
        }

        public FeedViewHolder(View itemView) {
            super(itemView);
            cardImage = (ImageView)itemView.findViewById(R.id.card_image);
            cardAuthorImage = (ImageView)itemView.findViewById(R.id.card_author_image);
            cardTitle = (TextView)itemView.findViewById(R.id.card_title);
            cardDate = (TextView)itemView.findViewById(R.id.card_date);
            cardLikesCount = (TextView) itemView.findViewById(R.id.card_likes_count);
            cardCommentsCount = (TextView)itemView.findViewById(R.id.card_comments_count);
            commentContent = (TextView)itemView.findViewById(R.id.comment_send_text);
            commentSendBtn = (ImageView)itemView.findViewById(R.id.comment_send_btn);
            likeBtn = (ImageView)itemView.findViewById(R.id.card_like);
            cardHeader = (LinearLayout)itemView.findViewById(R.id.card_header);
            cardFollowBtn = (Button)itemView.findViewById(R.id.cardFollowBtn);

            // Set up inner recycler view
            recyclerView = (RecyclerView)itemView.findViewById(R.id.content_card_comment_list);
            commentAdapter = new CommentAdapter(commentList);
            recyclerView.setAdapter(commentAdapter);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);

        }

        public void updateUI(final ScribeCard card) {
            // Set the view
            if(card.getFilename() != null && card.getFilename() != "") {
                Picasso.with(getContext())
                        .load("https://s3-us-west-2.amazonaws.com/photogriddemo/" + card.getFilename())
                        .resize(950,500)
                        .centerCrop()
                        .into(cardImage);
            } else {
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_btn));
            }

            cardImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), LoadPostActivity.class);
                    intent.putExtra("commentId", card.get_id());
                    startActivity(intent);
                }
            });

            if(card.getAuthorPic() != null && card.getAuthorPic() != "") {
                Picasso.with(getContext())
                        .load(card.getAuthorPic())
                        .into(cardAuthorImage);
                        // cardAuthorImage.setImageDrawable(null);
                        // new DownloadImageTask(cardAuthorImage).execute(card.getAuthorPic());
            } else {
                cardAuthorImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_btn));
            }

            cardTitle.setText(card.getAuthorName());
            cardHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment nextFrag = new ProfileFragment();
                    Bundle args = new Bundle();
                    args.putString("userId", "/" + card.getAuthorId());
                    nextFrag.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(containerFragId, nextFrag)
                            .addToBackStack(null)
                            .commit();
                }
            });
            cardDate.setText(CustomUtility.timeSince(card.getDate()));

            ScribeComment[] scribeComments = card.getComments();

            if(card.getLikes() != 0){
                cardLikesCount.setText(Integer.toString(card.getLikes()));
            } else {
                cardLikesCount.setText("");
            }

            if(scribeComments != null && card.getComments().length != 0){
                cardCommentsCount.setText(Integer.toString(card.getComments().length));
//                commentList.clear();
//                for(int i = 0; i < scribeComments.length; i++) {
//                    commentList.add(scribeComments[i]);
//                }
            } else {
                cardCommentsCount.setText("");
            }

            if (MainActivity.getLikedList().contains(card.get_id())) {
                //likeBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_red));
                likeBtn.setColorFilter(getContext().getResources().getColor(R.color.colorLiked));
            } else {
                likeBtn.clearColorFilter();
            }

            commentSendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.notify(card.getAuthorId(), "commented", MainActivity.getCurrentlyLoggedInUserName(), commentContent.getText().toString(), card.get_id());
                    addComment(card.get_id(), commentContent.getText().toString(), commentAdapter);
                    commentContent.setText("");
                }
            });

            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 700){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    if(MainActivity.getLikedList().contains(card.get_id())) {
                        Log.d("like", "clicked");
                        int count = cardLikesCount.getText().toString() == "" ? 0 : Integer.parseInt(cardLikesCount.getText().toString());
                        if(count != 0) {
                            count--;
                        }
                        cardLikesCount.setText(Integer.toString(count));
                        likeBtn.clearColorFilter();
                        MainActivity.unlikePost(card.get_id());
                    } else {
                        int count = cardLikesCount.getText().toString() == "" ? 0 : Integer.parseInt(cardLikesCount.getText().toString());
                        count++;
                        Log.d("like", "clicked");
                        cardLikesCount.setText(Integer.toString(count));
                        likeBtn.setColorFilter(getContext().getResources().getColor(R.color.colorLiked));
                        MainActivity.likePost(card.get_id());
                        MainActivity.notify(card.getAuthorId(), "liked", MainActivity.getCurrentlyLoggedInUserName(), "", card.get_id());
                    }
                }
            });

            if(isGlobal) {
                cardFollowBtn.setVisibility(View.VISIBLE);
                if (!MainActivity.getFollowingList().contains(card.getAuthorId())) {
                    cardFollowBtn.setText("Follow");
                } else {
                    cardFollowBtn.setText("Unfollow");
                }
                cardFollowBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String text = ((Button) view).getText().toString();
                        if (!MainActivity.getFollowingList().contains(card.getAuthorId())) {
                            ((Button) view).setText("Unfollow");
                            MainActivity.followUser(card.getAuthorId());
                            MainActivity.notify(card.getAuthorId(), "followed", MainActivity.getCurrentlyLoggedInUserName(), "", "");
                        } else {
                            ((Button) view).setText("Follow");
                            MainActivity.unfollowUser(card.getAuthorId());
                        }
                    }
                });
            }
            //commentAdapter.notifyDataSetChanged();
        }


    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
