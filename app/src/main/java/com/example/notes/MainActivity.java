package com.example.notes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    static final String TITLE_KEY = "TITLE";
    static final String TITLE_BACK_KEY = "TITLE_BACK";
    static final String DESCRIPTION_KEY = "DESCRIPTION";
    static final String DESCRIPTION_BACK_KEY = "DESCRIPTION_BACK";

    public String returnTitle = "a";
    public String returnDescription = "b";

    private static final int MY_DEFAULT_DURATION = 1000;

    private CardSource data;
    private RecyclerView recyclerView;
    private MyAdapter adapter;

    public static final String APP_PREFERENCES_TITLE = "Title";
    public static final String APP_PREFERENCES_DESCRIPTION = "Description";

   /* SharedPreferences titlesSP;
    SharedPreferences descriptionsSP;*/

    public SharedPreferences spTitles;
    public SharedPreferences spDescriptions;


    public Set<String> titlesSP = new LinkedHashSet<String>();
    public Set<String> descriptionsSP = new LinkedHashSet<String>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        setSupportActionBar(findViewById(R.id.toolbar));
        initView();

        getDataFromSharedPreferences();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cards_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            addNote("Заметка " + (data.size() + 1), "Add some text");
            return true;
        } else if (item.getItemId() == R.id.action_clear) {
            data.clearCardData();
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {

        recyclerView = findViewById(R.id.recycler_view_lines);
        // Получим источник данных для списка
        data = new CardSourceImpl(getResources()).init();

        initRecyclerView();
    }


    private void initRecyclerView() {

        // Эта установка служит для повышения производительности системы
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyAdapter(data, this);
        recyclerView.setAdapter(adapter);

        //обавляем декоратор/разделитель карточек
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.separator, null));
        recyclerView.addItemDecoration(itemDecoration);

        // Установим анимацию.
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(MY_DEFAULT_DURATION);
        animator.setRemoveDuration(MY_DEFAULT_DURATION);
        recyclerView.setItemAnimator(animator);

        // Установим слушателя
        adapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onItemClick(View view, int position) {
                /*Toast.makeText(getApplicationContext(), String.format("Позиция - %d", position), Toast.LENGTH_SHORT).show();*/
            }
        });
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.card_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = adapter.getMenuPosition();
        if (item.getItemId() == R.id.action_update) {

            addNote(data.getCardData(position).getTitle(), data.getCardData(position).getDescription());

            data.updateCardData(position,
                    new CardData(returnTitle, returnDescription/*"Кадр " + position,
                            data.getCardData(position).getDescription())*/));
            adapter.notifyItemChanged(position);
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            data.deleteCardData(position);
            adapter.notifyItemRemoved(position);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    public void onAddNotePressed(View view) {
        addNote("Заметка " + (data.size() + 1), "Add some text");
    }

    public void addNote(String title, String description) {

        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra(TITLE_KEY, title);
        intent.putExtra(DESCRIPTION_KEY, description);

        mStartForResult.launch(intent);
        if (description.equals("Add some text")) {
            data.addCardData(new CardData(returnTitle, returnDescription));
            adapter.notifyItemInserted(data.size() - 1);
            recyclerView.smoothScrollToPosition(data.size() - 1);
            saveSharePreferences(returnTitle, returnDescription);
        }

    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if (intent != null) {
                            returnTitle = intent.getStringExtra(MainActivity.TITLE_BACK_KEY);
                            returnDescription = intent.getStringExtra(MainActivity.DESCRIPTION_BACK_KEY);
                        }
                    }
                }
            });

    public void saveSharePreferences(String returnTitle, String returnDescription) {
        spTitles = getSharedPreferences(APP_PREFERENCES_TITLE, Context.MODE_PRIVATE);
        spDescriptions = getSharedPreferences(APP_PREFERENCES_DESCRIPTION, Context.MODE_PRIVATE);

        titlesSP.add(returnTitle);
        descriptionsSP.add(returnDescription);

        SharedPreferences.Editor e1 = spTitles.edit();
        e1.putStringSet("title", titlesSP);
        SharedPreferences.Editor e2 = spDescriptions.edit();
        e2.putStringSet("description", descriptionsSP);
        e1.apply();
        e2.apply();



 /*       titlesSP = getSharedPreferences(APP_PREFERENCES_TITLE, Context.MODE_PRIVATE);
        descriptionsSP = getSharedPreferences(APP_PREFERENCES_DESCRIPTION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorTitle = titlesSP.edit();
        SharedPreferences.Editor editorDescription = descriptionsSP.edit();
        editorTitle.putString(APP_PREFERENCES_TITLE, returnTitle);
        editorDescription.putString(APP_PREFERENCES_DESCRIPTION, returnDescription);
        editorTitle.apply();
        editorDescription.apply();*/
    }

    public void getDataFromSharedPreferences() {

        /*data.clearCardData();*/

        spTitles = getSharedPreferences(APP_PREFERENCES_TITLE, Context.MODE_PRIVATE);
        spDescriptions = getSharedPreferences(APP_PREFERENCES_DESCRIPTION, Context.MODE_PRIVATE);

        Set<String> titles = spTitles.getStringSet("strSetKey", new LinkedHashSet<String>());
        Set<String> descriptions = spDescriptions.getStringSet("strSetKey", new LinkedHashSet<String>());

        Iterator<String> iteratorTitle = titles.iterator();
        Iterator<String> iteratorDescription = descriptions.iterator();

        while (iteratorTitle.hasNext()) {
            data.addCardData(new CardData(iteratorTitle.next(), iteratorDescription.next()));
        }



        /*data.clearCardData();*//*

        titlesSP = getSharedPreferences(APP_PREFERENCES_TITLE, Context.MODE_PRIVATE);
        descriptionsSP = getSharedPreferences(APP_PREFERENCES_DESCRIPTION, Context.MODE_PRIVATE);
        String title;
        String description;

       *//* Set<String> ret = titlesSP.getStringSet(APP_PREFERENCES_TITLE, new HashSet<String>());
        for(String r : ret) {*//*
            title = titlesSP.getString(APP_PREFERENCES_TITLE, "");
            description = descriptionsSP.getString(APP_PREFERENCES_DESCRIPTION, "");
            data.addCardData(new CardData(title, description));
       *//* }*/
    }
}

//startActivityForResult - готовая обработка входящи + кнопка назад (onBackPressed)
//https://metanit.com/java/android/2.11.php







