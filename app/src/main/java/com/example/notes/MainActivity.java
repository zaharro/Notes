package com.example.notes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
import android.os.CountDownTimer;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPref = null;
    public static final String KEY = "key";
    final ArrayList<CardData> userNotes = new ArrayList<>();
    public static final String TITLE_KEY = "TITLE";
    static final String TITLE_BACK_KEY = "TITLE_BACK";
    static final String DESCRIPTION_KEY = "DESCRIPTION";
    static final String DESCRIPTION_BACK_KEY = "DESCRIPTION_BACK";

    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final int NOTIFICATION_ID = 42;
    public String returnTitle;
    public String returnDescription;
    private static final int MY_DEFAULT_DURATION = 1000;
    private CardSource data;
    private RecyclerView recyclerView;
    private MyAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sharedPref = getPreferences(MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.toolbar));

        if (savedInstanceState == null) {
            initView();
            setSplashScreenLoadingParameters();
        } else getDataFromSharedPreferences();
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

            Intent intent = new Intent(this, SecondActivity.class);
            intent.putExtra(TITLE_KEY, data.getCardData(position).getTitle());
            intent.putExtra(DESCRIPTION_KEY, data.getCardData(position).getDescription());

            mStartForUpdateNote.launch(intent);


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
        mStartForAddNote.launch(intent);
    }

    //Запуск SecondActivity для добавления новой заметки
    ActivityResultLauncher<Intent> mStartForAddNote = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        returnTitle = intent.getStringExtra(MainActivity.TITLE_BACK_KEY);
                        returnDescription = intent.getStringExtra(MainActivity.DESCRIPTION_BACK_KEY);

                        if (data.size() == 0) initView();

                        data.addCardData(new CardData(returnTitle, returnDescription));
                        saveSharedPreferences(returnTitle, returnDescription);

                        adapter.notifyItemInserted(data.size() - 1);
                        recyclerView.smoothScrollToPosition(data.size() - 1);
                    }
                }
            });


    //Запуск SecondActivity для редактирования существующей заметки
    ActivityResultLauncher<Intent> mStartForUpdateNote = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    int position = adapter.getMenuPosition();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        returnTitle = intent.getStringExtra(MainActivity.TITLE_BACK_KEY);
                        returnDescription = intent.getStringExtra(MainActivity.DESCRIPTION_BACK_KEY);

                        data.updateCardData(position, new CardData(returnTitle, returnDescription));
                        saveSharedPreferences(returnTitle, returnDescription);
                        adapter.notifyItemChanged(position);
                    }
                }
            });


    public void saveSharedPreferences(String returnTitle, String returnDescription) {

        userNotes.add(new CardData(returnTitle, returnDescription));

        String jsonNote = new GsonBuilder().create().toJson(userNotes);
        sharedPref.edit().putString(KEY, jsonNote).apply();
        Toast.makeText(this, "Saved to shared preferences", Toast.LENGTH_SHORT).show();
        showNotification();
    }

    public void getDataFromSharedPreferences() {

        String savedNotes = sharedPref.getString(KEY, null);
        if (savedNotes == null || savedNotes.isEmpty()) {
            Toast.makeText(this, "Saved notes empty", Toast.LENGTH_SHORT).show();
        } else {
            try {
// final ArrayList<CardData> userNotes = new ArrayList<>();
                Type type = new TypeToken<CardSourceImpl>() {
                }.getType();
                data.addCardData(new GsonBuilder().create().fromJson(savedNotes, type));
            } catch (JsonSyntaxException e) {
                Toast.makeText(this, "Error uploading saved notes", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void setSplashScreenLoadingParameters() {
        final Boolean[] isHideSplashScreen = {false};
        CountDownTimer countDownTimer = new CountDownTimer(2_000, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                isHideSplashScreen[0] = true;
            }
        }.start();


        // Set up an OnPreDrawListener to the root view.
        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // Check whether the initial data is ready.
                        if (isHideSplashScreen[0]) {
                            // The content is ready. Start drawing.
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        } else {
                            // The content isn't ready. Suspend.
                            return false;
                        }
                    }
                });
    }

    private void showNotification() {

        // Создаем NotificationChannel, но это делается только для API 26+
        // Потому что NotificationChannel -- это новый класс и его нет в support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        // Все цветные иконки отображаются только в оттенках серого
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Notes")
                .setContentText("New note has been created")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_ID); // константа вашего выбора
            }
            return;

        }
        // Display the notification
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_ID && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Пользователь разрешил показывать уведомления
            showNotification(); // Можно показать уведомление повторно
        } else {
            // Пользователь отказался давать разрешение
            Toast.makeText(this, "Разрешение на уведомления не предоставлено.", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        String name = "Name";
        String descriptionText = "Description";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(descriptionText);

        // Регистрируем канал в системе
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}

//https://www.geeksforgeeks.org/create-an-expandable-notification-containing-some-text-in-android/
//https://metanit.com/java/android/2.11.php







