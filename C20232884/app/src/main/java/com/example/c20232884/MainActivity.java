package com.example.c20232884;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.c20232884.databinding.ActivityMainBinding;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private String userType = "병사";
    private int startYear = 2025;
    private int startMonth = 5; // 6월 (0이 1월)
    private int endYear = 2026;
    private int endMonth = 11; // 12월 (0이 1월)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 복무 및 권한 설정 버튼
        binding.btnMainSetting.setOnClickListener(v -> showEnlistmentDatePicker());

        binding.btnMainToCalender.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            intent.putExtra("userType", userType);
            intent.putExtra("startYear", startYear);
            intent.putExtra("startMonth", startMonth);
            intent.putExtra("endYear", endYear);
            intent.putExtra("endMonth", endMonth);
            startActivity(intent);
        });

        binding.btnMainToBoard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PostActivity.class);
            intent.putExtra("userType", userType);
            startActivity(intent);
        });
    }

    private void showEnlistmentDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    this.startYear = year;
                    this.startMonth = month;
                    String enlistDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    Toast.makeText(this, "입대일: " + enlistDate, Toast.LENGTH_SHORT).show();
                    showDischargeDatePicker(year, month, enlistDate);
                },
                startYear, startMonth, 16
        );
        dialog.setTitle("입대일을 선택하세요");
        dialog.show();
    }

    private void showDischargeDatePicker(int enlistYear, int enlistMonth, String enlistDate) {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    this.endYear = year;
                    this.endMonth = month;
                    String dischargeDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    Toast.makeText(this, "전역일: " + dischargeDate, Toast.LENGTH_SHORT).show();
                    showUserTypeSelectionDialog();
                },
                enlistYear + 1, enlistMonth + 6, 15
        );
        dialog.setTitle("전역일을 선택하세요");
        dialog.show();
    }

    private void showUserTypeSelectionDialog() {
        String[] options = {"병사", "간부"};
        final int[] selectedChoice = {"간부".equals(userType) ? 1 : 0};

        new AlertDialog.Builder(this)
                .setTitle("사용자 유형을 선택하세요")
                .setSingleChoiceItems(options, selectedChoice[0], (dialog, which) -> {
                    selectedChoice[0] = which;
                })
                .setPositiveButton("확인", (dialog, which) -> {
                    userType = options[selectedChoice[0]];
                    Toast.makeText(this, "유저 타입: " + userType + "(으)로 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("취소", null)
                .setCancelable(false)
                .show();
    }
}
