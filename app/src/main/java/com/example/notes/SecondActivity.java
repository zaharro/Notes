package com.example.notes;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle extras = getIntent().getExtras();
        String title = extras.getString(MainActivity.TITLE_KEY);
        String description = extras.getString(MainActivity.DESCRIPTION_KEY);

        TextView titl = findViewById(R.id.title);
        titl.setText(title);
        TextView descr = findViewById(R.id.description);
        descr.setText(description);

    }


    public void onSaveChanges(View view) {

        EditText enteredTitle = findViewById(R.id.title);
        EditText enteredDescription = findViewById(R.id.description);
        String title = enteredTitle.getText().toString();
        String description = enteredDescription.getText().toString();
        sendMessage(title, description);
    }

    private void sendMessage(String title, String description){

        Intent data = new Intent();
        data.putExtra(MainActivity.TITLE_BACK_KEY, title);
        data.putExtra(MainActivity.DESCRIPTION_BACK_KEY, description);
        setResult(RESULT_OK, data);
        finish();
    }
}