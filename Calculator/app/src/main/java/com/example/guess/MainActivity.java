package com.example.guess;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editTextAnswer = findViewById(R.id.editTextAnswer);
        Button buttonAnswer = findViewById(R.id.buttonAnswer);
        TextView incorrectAnswer = findViewById(R.id.textViewIncorrectAnswer);
        TextView correctAnswer = findViewById(R.id.textViewCorrectAnswer);

        TextView newQuest = findViewById(R.id.textViewExample);

        newQuest.setText("20 + 10 = ?");


        buttonAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    int text = Integer.parseInt(editTextAnswer.getText().toString());
                    //String text = editTextAnswer.getText().toString();

                if(text == 30) {
                    correctAnswer.setVisibility(View.VISIBLE);
                    incorrectAnswer.setVisibility(View.GONE);
                } else {
                    correctAnswer.setVisibility(View.GONE);
                    incorrectAnswer.setVisibility(View.VISIBLE);
                }
            }
        });

    }
}