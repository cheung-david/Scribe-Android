package com.dc.scribe.fragments;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dc.scribe.CustomUtility;
import com.dc.scribe.R;
import com.dc.scribe.activities.CameraActivity;
import com.dc.scribe.activities.LoadPostActivity;
import com.dc.scribe.activities.MainActivity;
import com.dc.scribe.model.ScribeNotification;
import com.dc.scribe.model.ScribeUser;
import com.dc.scribe.model.ScribeUserList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedActivityFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ArrayList<ScribeNotification> notificationList = new ArrayList<>();
    private TextView emptyView;


    public FeedActivityFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FeedActivityFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedActivityFragment newInstance() {
        FeedActivityFragment fragment = new FeedActivityFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getNotifications();
        View view = inflater.inflate(R.layout.fragment_feed_activity, container, false);

        recyclerView = (RecyclerView)view.findViewById(R.id.content_activity);
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
        emptyView = (TextView)view.findViewById(R.id.empty_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        // Inflate the layout for this fragment
        return view;
    }

    class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {
        private ArrayList<ScribeNotification> notifications = new ArrayList<>();

        public NotificationAdapter(ArrayList<ScribeNotification> notifications) {
            this.notifications = notifications;
        }

        @Override
        public void onBindViewHolder(NotificationViewHolder holder, int position) {
            if(!notifications.isEmpty()) {
                final ScribeNotification notification = notifications.get(position);
                holder.updateUI(notification);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), LoadPostActivity.class);
                        intent.putExtra("commentId", notification.getCommentId());
                        startActivity(intent);
                    }
                });
            }
        }

        @Override
        public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_notification, parent, false);
            return new NotificationViewHolder(card);
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView notificationAuthorImage;
        private TextView notificationTitle, notificationDate, notificationContent;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            notificationAuthorImage = (ImageView)itemView.findViewById(R.id.card_notification_author_image);
            notificationTitle = (TextView) itemView.findViewById(R.id.card_notification_user_title);
            notificationDate = (TextView) itemView.findViewById(R.id.card_notification_date);
            notificationContent = (TextView) itemView.findViewById(R.id.card_notification_user_content);
        }

        void updateUI(final ScribeNotification notification) {
            if(notification.getProfilePic() != null && notification.getProfilePic() != "") {
                Picasso.with(getContext())
                        .load(notification.getProfilePic())
                        .placeholder(R.drawable.profile_btn)
                        .into(notificationAuthorImage);
            } else {
                notificationAuthorImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_btn));
            }
            notificationTitle.setText(notification.getFrom() + " has " + notification.getAction() + " your post.");

            if(notification.getContent().length() > 120) {
                notificationContent.setText(notification.getContent().substring(0, 121) + "...");
            } else {
                notificationContent.setText(notification.getContent());
            }

            notificationDate.setText(CustomUtility.timeSince(notification.getDate()));
        }
    }

    public void getNotifications() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/notify")
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                        JsonArray innerObj = obj.getAsJsonArray("notifications");
                        ScribeNotification[] userNotifications = MainActivity.getGson().fromJson(innerObj, ScribeNotification[].class);
                        Log.d("ActivityFrag", jsonData);
                        if(userNotifications != null) {
                            notificationList.clear();
                            for(int i = 0; i < userNotifications.length; i++) {
                                notificationList.add(userNotifications[i]);
                            }
                        }
                        if(getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //stuff that updates ui
                                    adapter.notifyDataSetChanged();
                                    if (notificationList.isEmpty()) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }

}
