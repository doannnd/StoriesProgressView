package com.nguyendinhdoan.storiesprogressview;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nguyendinhdoan.storiesprogressview.model.Movies;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

/*
 * Hello everybody, Today I will show you how to working with Stories Progress with Firebase Firestore
 *
 * Now, write Model and declare interface to listen load event from Firebase firestore
 * */

public class MainActivity extends AppCompatActivity{

    public static final String TAG = "MAIN_ACTIVITY";
    public static final String FIREBASE_DATABASE_NAME = "Movies";
    public static final long STORIES_PROGRESS_VIEW_DURATION = 1000L;

    private StoriesProgressView storiesProgressView;
    private ImageView avatarImageView;
    private Button loadButton;

    private CollectionReference moviesCollection;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        addEvents();
    }

    private void initViews() {
        storiesProgressView = findViewById(R.id.stories_progress_view);
        avatarImageView = findViewById(R.id.avatar_image_view);
        loadButton = findViewById(R.id.load_button);
    }

    private void addEvents() {
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupLoadDataFromFirebase();
            }
        });
    }

    private void setupLoadDataFromFirebase() {
        moviesCollection = FirebaseFirestore.getInstance().collection(FIREBASE_DATABASE_NAME);
        moviesCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Log.d(TAG, "data: " + task.getResult());
                    List<Movies> moviesList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentMovies : task.getResult()) {
                        Movies movies = documentMovies.toObject(Movies.class);
                        moviesList.add(movies);
                    }
                    loadImageWithPicasso(moviesList);
                } else {
                    Log.d(TAG, "error: " + task.getException());
                }
            }
        });
    }

    private void loadImageWithPicasso(final List<Movies> moviesList) {
        storiesProgressView.setStoriesCount(moviesList.size());
        storiesProgressView.setStoryDuration(STORIES_PROGRESS_VIEW_DURATION);
        Picasso.get().load(moviesList.get(0).getImage()).into(avatarImageView, new Callback() {
            @Override
            public void onSuccess() {
                storiesProgressView.startStories();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        storiesProgressView.setStoriesListener(new StoriesProgressView.StoriesListener() {
            @Override
            public void onNext() {
                if (count < moviesList.size()) {
                    count ++;
                    Picasso.get().load(moviesList.get(count).getImage()).into(avatarImageView);
                }
            }

            @Override
            public void onPrev() {
                if (count > 0) {
                    count --;
                    Picasso.get().load(moviesList.get(count).getImage()).into(avatarImageView);
                }
            }

            @Override
            public void onComplete() {
                count = 0;
                Toast.makeText(MainActivity.this, "Load Image Done", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (storiesProgressView != null) {
            storiesProgressView.destroy();
        }
        super.onDestroy();
    }
}
