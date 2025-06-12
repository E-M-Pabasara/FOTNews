package com.example.fotnews;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryNewsActivity extends AppCompatActivity {

    private static final String TAG = "CategoryNewsActivity";

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<News> newsList;
    private DatabaseReference databaseReference;

    private String category;
    private String categoryTitle;
    private TextView titleTextView;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_news);


        recyclerView = findViewById(R.id.recycler_view_news);
        titleTextView = findViewById(R.id.tv_category_title);
        backButton = findViewById(R.id.back_button);


        category = getIntent().getStringExtra("category");
        categoryTitle = getIntent().getStringExtra("categoryTitle");


        if (categoryTitle != null) {
            titleTextView.setText(categoryTitle);
        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(this, newsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(newsAdapter);


        databaseReference = FirebaseDatabase.getInstance().getReference("news");


        loadNewsData();
    }

    private void loadNewsData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsList.clear();

                for (DataSnapshot newsSnapshot : snapshot.getChildren()) {
                    News news = newsSnapshot.getValue(News.class);
                    if (news != null && category.equals(news.getCategory())) {
                        newsList.add(news);
                        Log.d(TAG, "Added news: " + news.getTitle() + " - Category: " + news.getCategory());
                    }
                }

                newsAdapter.notifyDataSetChanged();

                if (newsList.isEmpty()) {
                    Toast.makeText(CategoryNewsActivity.this, "No news found for " + categoryTitle, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(CategoryNewsActivity.this, "Failed to load news", Toast.LENGTH_SHORT).show();
            }
        });
    }
}