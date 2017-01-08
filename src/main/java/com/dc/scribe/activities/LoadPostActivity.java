package com.dc.scribe.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dc.scribe.CustomUtility;
import com.dc.scribe.R;
import com.dc.scribe.model.ScribeCard;
import com.dc.scribe.model.ScribeComment;
import com.dc.scribe.model.ScribeCommentAuthor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class LoadPostActivity extends AppCompatActivity {
    private TextView errorView;
    private RecyclerView recyclerView;
    private FeedAdapter adapter;
    private ArrayList<ScribeCard> mCard = new ArrayList<>();

    //TODO: This should be refactored
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
                        JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                        JsonObject jsonObject = obj.getAsJsonObject("comment");
                        final ScribeComment newComment = MainActivity.getGson().fromJson(jsonObject, ScribeComment.class);
                        Log.v("JSON", "Recv Comment: " + newComment.getText());
                            runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.addComment(newComment);
                                //stuff that updates ui
                                Toast.makeText(getBaseContext(),"Comment posted!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                return;
            }
        });
    }

    public void getImage(final String id){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //Log.v("MAIN ACTIVITY ASYNC", "SUCCESS: " + MainActivity.getToken());
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/image/" + id)
                        .build();

                try {
                    okhttp3.Response response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        errorView.setVisibility(View.GONE);
                        Log.v("JSON LOAD IMAGE", "Creating OBJ: " + jsonData);
                        JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                        JsonObject jsonObject = obj.getAsJsonObject("image");
                        ScribeCard loadedCard = MainActivity.getGson().fromJson(jsonObject, ScribeCard.class);
                        if (loadedCard != null) {
                            mCard.clear();
                            mCard.add(loadedCard);
                           runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //stuff that updates ui
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        }
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //stuff that updates ui
                            errorView.setVisibility(View.VISIBLE);
                        }
                    });
                    e.printStackTrace();
                    return;
                }
                return;
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        String commentId = intent.getExtras().getString("commentId");
        getImage(commentId);

        recyclerView = (RecyclerView)findViewById(R.id.content_feed);
        errorView = (TextView)findViewById(R.id.error_view);
        errorView.setVisibility(View.GONE);
        adapter = new FeedAdapter(mCard);

        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
        Log.d("LoadPost", "Back");
        super.onBackPressed();
        finish();
    }

    //TODO: This should be refactored
    public class FeedAdapter extends RecyclerView.Adapter<FeedViewHolder> {
        ArrayList<ScribeCard> scribeCards;
        RecyclerView commentsList;
        View card;

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
            card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_feed_extra, parent, false);
            commentsList = (RecyclerView) card.findViewById(R.id.content_card_comment_list);
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
                    Picasso.with(getBaseContext())
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

            // Set up inner recycler view
            recyclerView = (RecyclerView)itemView.findViewById(R.id.content_card_comment_list);
            commentAdapter = new CommentAdapter(commentList);
            recyclerView.setAdapter(commentAdapter);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);

        }

        public void updateUI(final ScribeCard card) {
            // Set the view
            if(card.getFilename() != null && card.getFilename() != "") {
                Picasso.with(getBaseContext())
                        .load("https://s3-us-west-2.amazonaws.com/photogriddemo/" + card.getFilename())
                        .into(cardImage);
            } else {
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_btn));
            }

            if(card.getAuthorPic() != null && card.getAuthorPic() != "") {
                Picasso.with(getBaseContext())
                        .load(card.getAuthorPic())
                        .into(cardAuthorImage);
                // cardAuthorImage.setImageDrawable(null);
                // new DownloadImageTask(cardAuthorImage).execute(card.getAuthorPic());
            } else {
                cardAuthorImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_btn));
            }

            cardTitle.setText(card.getAuthorName());
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
                likeBtn.setColorFilter(getBaseContext().getResources().getColor(R.color.colorLiked));
            } else {
                likeBtn.clearColorFilter();
            }

            commentSendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addComment(card.get_id(), commentContent.getText().toString(), commentAdapter);
                    MainActivity.notify(card.getAuthorId(), "commented", MainActivity.getCurrentlyLoggedInUserName(), commentContent.getText().toString(), card.get_id());
                    commentContent.setText("");
                }
            });

            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(MainActivity.getLikedList().contains(card.get_id())) {
                        Log.d("like", "clicked");
                        int count = Integer.parseInt(cardLikesCount.getText().toString());
                        count--;
                        cardLikesCount.setText(Integer.toString(count));
                        likeBtn.clearColorFilter();
                        MainActivity.unlikePost(card.get_id());
                    } else {
                        int count = cardLikesCount.getText().toString() == "" ? 0 : Integer.parseInt(cardLikesCount.getText().toString());
                        count++;
                        Log.d("like", "clicked");
                        cardLikesCount.setText(Integer.toString(count));
                        likeBtn.setColorFilter(getBaseContext().getResources().getColor(R.color.colorLiked));
                        MainActivity.likePost(card.get_id());
                        MainActivity.notify(card.getAuthorId(), "liked", MainActivity.getCurrentlyLoggedInUserName(), "", card.get_id());
                    }
                }
            });
            //commentAdapter.notifyDataSetChanged();
        }


    }
}
