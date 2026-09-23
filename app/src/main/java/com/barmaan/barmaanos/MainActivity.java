package com.barmaan.barmaanos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private GridLayout appGrid;
    private EditText search;
    private TextView clock;
    private TextView date;
    private TextView appCount;

    private final List<AppItem> allApps = new ArrayList<>();

    private final android.os.Handler handler =
            new android.os.Handler();

    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        clock = findViewById(R.id.clock);
        date = findViewById(R.id.date);
        search = findViewById(R.id.search);
        appGrid = findViewById(R.id.appGrid);
        appCount = findViewById(R.id.appCount);

        Button settingsButton =
                findViewById(R.id.settingsButton);

        settingsButton.setOnClickListener(v -> {
            try {
                startActivity(
                        new Intent(Settings.ACTION_SETTINGS)
                );
            } catch (Exception ignored) {
            }
        });

        search.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        filterApps(s.toString());
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                });

        loadApps();
        handler.post(clockRunnable);
    }

    private void updateClock() {

        SimpleDateFormat timeFormat =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault());

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "EEEE, MMMM d",
                        Locale.getDefault());

        Date now = new Date();

        clock.setText(timeFormat.format(now));
        date.setText(dateFormat.format(now));
    }

    private void loadApps() {

        allApps.clear();

        PackageManager pm = getPackageManager();

        Intent intent = new Intent(
                Intent.ACTION_MAIN,
                null);

        intent.addCategory(
                Intent.CATEGORY_LAUNCHER);

        List<ApplicationInfo> installed =
                pm.getInstalledApplications(
                        PackageManager.GET_META_DATA);

        for (ApplicationInfo info : installed) {

            Intent launchIntent =
                    pm.getLaunchIntentForPackage(
                            info.packageName);

            if (launchIntent == null) {
                continue;
            }

            if (info.packageName.equals(
                    getPackageName())) {
                continue;
            }

            String name = pm.getApplicationLabel(
                    info).toString();

            allApps.add(
                    new AppItem(
                            name,
                            info.packageName));
        }

        Collections.sort(
                allApps,
                (a, b) ->
                        a.name.compareToIgnoreCase(
                                b.name));

        filterApps("");
    }

    private void filterApps(String query) {

        appGrid.removeAllViews();

        String text = query
                .trim()
                .toLowerCase(Locale.getDefault());

        int shown = 0;

        for (AppItem app : allApps) {

            if (!text.isEmpty()
                    && !app.name
                    .toLowerCase(
                            Locale.getDefault())
                    .contains(text)) {

                continue;
            }

            addAppButton(app);
            shown++;
        }

        appCount.setText(
                shown + " apps");
    }

    private void addAppButton(AppItem app) {

        TextView button =
                new TextView(this);

        button.setText(app.name);
        button.setTextColor(Color.WHITE);
        button.setTextSize(13);
        button.setGravity(Gravity.CENTER);
        button.setPadding(6, 12, 6, 12);

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.rgb(28, 28, 38));

        background.setCornerRadius(22);

        button.setBackground(background);

        GridLayout.LayoutParams params =
                new GridLayout.LayoutParams();

        params.width = 0;
        params.height = 92;

        params.columnSpec =
                GridLayout.spec(
                        GridLayout.UNDEFINED,
                        1f);

        params.setMargins(5, 5, 5, 5);

        button.setLayoutParams(params);

        button.setOnClickListener(v ->
                openApp(app));

        button.setOnLongClickListener(v -> {

            showAppInfo(app);
            return true;
        });

        appGrid.addView(button);
    }

    private void openApp(AppItem app) {

        try {

            Intent intent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    app.packageName);

            if (intent != null) {
                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            }

        } catch (Exception ignored) {
        }
    }

    private void showAppInfo(AppItem app) {

        new AlertDialog.Builder(this)
                .setTitle(app.name)
                .setMessage(
                        "Package:\n"
                                + app.packageName)
                .setPositiveButton(
                        "App info",
                        (dialog, which) -> {

                            try {

                                Intent intent =
                                        new Intent(
                                                Settings
                                                        .ACTION_APPLICATION_DETAILS_SETTINGS);

                                intent.setData(
                                        android.net.Uri
                                                .parse(
                                                        "package:"
                                                                + app.packageName));

                                startActivity(intent);

                            } catch (Exception ignored) {
                            }
                        })
                .setNegativeButton(
                        "Cancel",
                        null)
                .show();
    }

    @Override
    protected void onResume() {

        super.onResume();

        loadApps();
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(
                clockRunnable);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        // Keep BarmaanOS on the Home screen.
    }

    private static class AppItem {

        final String name;
        final String packageName;

        AppItem(
                String name,
                String packageName) {

            this.name = name;
            this.packageName =
                    packageName;
        }
    }
                          }
