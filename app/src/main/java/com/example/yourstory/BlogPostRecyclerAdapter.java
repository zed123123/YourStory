package com.example.yourstory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogPostRecyclerAdapter extends RecyclerView.Adapter<BlogPostRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blogPostList;
    public Context context;
    public List<User> userList;

    public FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    public BlogPostRecyclerAdapter(List<BlogPost> blog_list, List<User> userList){

        this.blogPostList = blog_list;
        this.userList = userList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View  view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_item, viewGroup, false);

        context = viewGroup. getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        viewHolder.setIsRecyclable(false);

        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        if(currentUserId != null && userList != null){
            final String blogPostId = blogPostList.get(i).BlogPostId;
            // Load Blog Data
            loadData(viewHolder, i);



            // Blog Delete Menu
            deleteMenu(viewHolder, i, blogPostId, currentUserId);




            //Blog Like Feature
            blogLikeFeature(viewHolder, blogPostId, currentUserId);


            // Get Likes Count
            countLikeCount(viewHolder, blogPostId);


            // Get Likes
            blogLikeChangeIcon(viewHolder, blogPostId, currentUserId);




            // Bookmark Feature
            blogPostBookmarkFeature(viewHolder, blogPostId, currentUserId);



            // Get Bookmark
            blogPostBookmarkChangeIcon(viewHolder, blogPostId, currentUserId);




            // Comment Activity
            sendToCommentActivity(viewHolder, blogPostId);




            // Get Comments Count
            blogPostCommentCount(viewHolder, blogPostId);



            // Other user profile
            sendToOtherUserProfile(viewHolder, i);
        }



    }

    private void sendToOtherUserProfile(final ViewHolder viewHolder, final int i) {

        final String userId = blogPostList.get(i).getUserId();

        viewHolder.blogUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imgUserIntent = new Intent(context, OtherUserProfileActivity.class);
                imgUserIntent.putExtra("userId", userId);
                context.startActivity(imgUserIntent);
            }
        });

        viewHolder.blogUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nameUserIntent = new Intent(context, OtherUserProfileActivity.class);
                nameUserIntent.putExtra("userId", userId);
                context.startActivity(nameUserIntent);
            }
        });
    }


    private void loadData(ViewHolder viewHolder, int i) {
        String desc_data = blogPostList.get(i).getBlogTxt();
        viewHolder.setDescText(desc_data);

        String image_url = blogPostList.get(i).getBlogImageUrl();
        String thumbUri = blogPostList.get(i).getBlogImageThumbnail();
        viewHolder.setBlogImage(image_url, thumbUri);


        // Show User data
        String userName = userList.get(i).getUserName();
        String userImage = userList.get(i).getUserImage();
        viewHolder.setUserData(userName, userImage);



        // Hiển thị thời gian đăng bài
        Timestamp timestamp = blogPostList.get(i).getTimestamp();
        long millisecond = timestamp.toDate().getTime();
        String dateString = DateFormat.format("dd/MM/yyyy", new Date(millisecond)).toString();
        viewHolder.setTime(dateString);
    }

    private void blogPostCommentCount(final ViewHolder viewHolder, String blogPostId) {
        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            int count = queryDocumentSnapshots.size();

                            viewHolder.updateCommentsCount(count);

                        }
                        else{

                            viewHolder.updateCommentsCount(0);

                        }

                    }
                });
    }

    private void sendToCommentActivity(ViewHolder viewHolder, final String blogPostId) {
        viewHolder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentActivity.class);
                commentIntent.putExtra("blog_post_id", blogPostId);
                context.startActivity(commentIntent);
            }
        });
    }

    private void blogPostBookmarkChangeIcon(final ViewHolder viewHolder, String blogPostId, String currentUserId) {
        firebaseFirestore.collection("Users/" + currentUserId + "/Bookmark")
                .document(blogPostId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if(documentSnapshot.exists()){

                            viewHolder.bookmarkBtn.setImageDrawable(context.getDrawable(R.drawable.ic_bookmark));

                        }
                        else {

                            viewHolder.bookmarkBtn.setImageDrawable(context.getDrawable(R.drawable.ic_bookmark_outline));

                        }

                    }
                });
    }

    private void blogPostBookmarkFeature(ViewHolder viewHolder, final String blogPostId, final String currentUserId) {
        viewHolder.bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Users/" + currentUserId + "/Bookmark")
                        .document(blogPostId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if(!task.getResult().exists()){

                                    Map<String, Object> bookmarkMap = new HashMap<>();
                                    bookmarkMap.put("timestamp", FieldValue.serverTimestamp());

                                    // Lưu Post ID vào User
                                    firebaseFirestore.collection("Users/" + currentUserId + "/Bookmark")
                                            .document(blogPostId)
                                            .set(bookmarkMap);
                                    Toast.makeText(context, "Đã lưu bài", Toast.LENGTH_SHORT).show();
                                }
                                else{

                                    firebaseFirestore.collection("Users/" + currentUserId + "/Bookmark")
                                            .document(blogPostId)
                                            .delete();

                                    Toast.makeText(context, "Đã xóa bookmark", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });
            }
        });
    }

    private void blogLikeChangeIcon(final ViewHolder  viewHolder, String blogPostId, String currentUserId) {
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                .document(currentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if(documentSnapshot.exists()){

                            viewHolder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_like));

                        }
                        else {

                            viewHolder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_like_outline));

                        }

                    }
                });
    }

    private void countLikeCount(final ViewHolder viewHolder, String blogPostId) {
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            int count = queryDocumentSnapshots.size();

                            viewHolder.updateLikesCount(count);

                        }
                        else{

                            viewHolder.updateLikesCount(0);

                        }

                    }
                });
    }

    private void blogLikeFeature(ViewHolder viewHolder, final String blogPostId, final String currentUserId) {
        viewHolder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                        .document(currentUserId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                // Kiểm tra User có like bài chưa. Chưa thì add còn có rồi thì click lần nữa bỏ like
                                if(!task.getResult().exists()){

                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());

                                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                                            .document(currentUserId)
                                            .set(likesMap);

                                    // Lưu Post ID vào User
                                    firebaseFirestore.collection("Users/" + currentUserId + "/Likes")
                                            .document(blogPostId)
                                            .set(likesMap);
                                }
                                else{

                                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                                            .document(currentUserId)
                                            .delete();

                                    firebaseFirestore.collection("Users/" + currentUserId + "/Likes")
                                            .document(blogPostId)
                                            .delete();

                                }

                            }
                        });



            }
        });
    }

    private void deleteMenu(final ViewHolder viewHolder, final int i, final String blogPostId, String currentUserId) {
        String blog_user_id = blogPostList.get(i).getUserId();
        if(blog_user_id.equals(currentUserId)){

            viewHolder.blogPostMenuTxt.setVisibility(View.VISIBLE);

            viewHolder.blogPostMenuTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, viewHolder.blogPostMenuTxt);
                    popupMenu.inflate(R.menu.post_card_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId() == R.id.post_card_delete){

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage("Bạn muốn xóa bài?")
                                        .setCancelable(true)
                                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                firebaseFirestore.collection("Posts").document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        blogPostList.remove(viewHolder.getAdapterPosition());
                                                        userList.remove(viewHolder.getAdapterPosition());

                                                        Toast.makeText(context, "Đã xóa bài", Toast.LENGTH_SHORT).show();

                                                    }
                                                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        loadData(viewHolder, i);
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

    @Override
    public int getItemCount() {
        if(blogPostList != null){
            return blogPostList.size();
        }
        else{
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;

        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;

        private TextView blogUserName;
        private CircleImageView blogUserImage;

        private ImageView blogLikeBtn;
        private TextView blogLikeCount;

        private ImageView blogCommentBtn;
        private TextView blogCommentCount;

        private ImageView bookmarkBtn;

        private TextView blogPostMenuTxt;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.blog_post_like_btn);
            blogCommentBtn = mView.findViewById(R.id.blog_post_comment_btn);
            bookmarkBtn = mView.findViewById(R.id.blog_post_bookmark_btn);
            blogPostMenuTxt = mView.findViewById(R.id.blog_post_menu);
        }

        public void setDescText(String descText){
            descView = mView.findViewById(R.id.single_post_text);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri, String thumbUri){

            blogImageView = mView.findViewById(R.id.blog_post_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.color.colorOnyx);

            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(downloadUri)
                    .thumbnail(Glide.with(context).load(thumbUri))
                    .into(blogImageView);

        }

        public void setTime(String date){

            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

        public void setUserData(String name, String image){

            blogUserImage = mView.findViewById(R.id.user_profile_image);
            blogUserName = mView.findViewById(R.id.user_name);

            blogUserName.setText(name);

            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.color.colorOnyx);

            Glide.with(context).load(image).into(blogUserImage);

        }

        public void updateLikesCount(int count){
            blogLikeCount = mView.findViewById(R.id.blog_post_like_text);
            blogLikeCount.setText(count+"");
        }

        public void updateCommentsCount(int count){
            blogCommentCount = mView.findViewById(R.id.blog_post_comment_text);
            blogCommentCount.setText(count + "");
        }
    }
}
