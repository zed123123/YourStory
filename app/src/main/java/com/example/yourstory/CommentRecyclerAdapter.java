package com.example.yourstory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder> {

    public List<Comment> commentsList;
    public Context context;
    public List<User> userList;
    public String blogPostId;


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public CommentRecyclerAdapter(List<Comment> commentsList, List<User> userList, String blogPostId) {
        this.commentsList = commentsList;
        this.userList = userList;
        this.blogPostId = blogPostId;

    }

    @NonNull
    @Override
    public CommentRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_item, viewGroup, false);

        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new CommentRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentRecyclerAdapter.ViewHolder viewHolder, int i) {

        viewHolder.setIsRecyclable(false);

        String currentUserId = firebaseAuth.getCurrentUser().getUid();


        loadCommentData(viewHolder, i);




        commentMenu(viewHolder, i, currentUserId);


        sendToUserProfile(viewHolder, i);




    }


    private void sendToUserProfile(ViewHolder viewHolder, int i){

        final String userId = commentsList.get(i).getUserId();

        viewHolder.commentUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userProfile = new Intent(context, OtherUserProfileActivity.class);
                userProfile.putExtra("userId", userId);
                context.startActivity(userProfile);
            }
        });

        viewHolder.commentUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userProfile = new Intent(context, OtherUserProfileActivity.class);
                userProfile.putExtra("userId", userId);
                context.startActivity(userProfile);
            }
        });
    }

    private void commentMenu(final ViewHolder viewHolder, final int i, String currentUserId) {

        String commentUserId = commentsList.get(i).getUserId();
        final String commentId = commentsList.get(i).CommentId;

        if(commentUserId.equals(currentUserId)){

            viewHolder.commentMenu.setVisibility(View.VISIBLE);

            viewHolder.commentMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, viewHolder.commentMenu);
                    popupMenu.inflate(R.menu.comment_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            if(item.getItemId() == R.id.comment_menu_delete){

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage("Xóa bình luận?")
                                .setCancelable(true)
                                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").document(commentId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                commentsList.remove(viewHolder.getAdapterPosition());
                                                userList.remove(viewHolder.getAdapterPosition());
                                                Toast.makeText(context, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                loadCommentData(viewHolder, i);
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                            return false;
                        }
                    });

                    popupMenu.show();
                }
            });

        }
    }

    private void loadCommentData(ViewHolder viewHolder, int i) {

        String commentMessage = commentsList.get(i).getMessage();
        viewHolder.setCommentMessage(commentMessage);


        Timestamp timestamp = commentsList.get(i).getTimestamp();
        if(timestamp != null){
            long millisecond = timestamp.toDate().getTime();
            String dateString = DateFormat.format("dd/MM/yyyy", new Date(millisecond)).toString();
            viewHolder.setCommentDate(dateString);
        }

        String userName = userList.get(i).getUserName();
        String userImage = userList.get(i).getUserImage();
        viewHolder.setUserData(userName, userImage);
    }

    @Override
    public int getItemCount() {
        if(commentsList != null){
            return commentsList.size();
        }
        else {
            return 0;
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }





    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView commentMessage;
        private TextView commentDate;
        private TextView commentUserName;
        private CircleImageView commentUserImage;
        private TextView commentMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            commentMenu = mView.findViewById(R.id.comment_menu);
        }

        public void setCommentMessage(String message){

            commentMessage = mView.findViewById(R.id.comment_message);
            commentMessage.setText(message);

        }

        public void setCommentDate(String date){

            commentDate = mView.findViewById(R.id.comment_date);
            commentDate.setText(date);

        }

        @SuppressLint("CheckResult")
        public void setUserData(String userName, String userImage) {
            commentUserImage = mView.findViewById(R.id.comment_user_image);
            commentUserName = mView.findViewById(R.id.comment_user_name);

            commentUserName.setText(userName);

            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.color.colorOnyx);

            Glide.with(context).load(userImage).into(commentUserImage);
        }
    }
}
