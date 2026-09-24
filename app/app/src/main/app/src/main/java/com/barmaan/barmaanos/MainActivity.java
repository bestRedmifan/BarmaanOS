package com.barmaan.barmaanos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private GridLayout appGrid;
    private EditText searchBox;
    private TextView clockText;
    private TextView dateText;
    private TextView appCount;
    private final Handler handler = new Handler();
    private final List<AppItem> apps = new ArrayList<>();

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

        clockText = findViewById(R.id.clockText);
        dateText = findViewById(R.id.dateText);
        searchBox = findViewById(R.id.searchBox);
        appGrid = findViewById(R.id.appGrid);
        appCount = findViewById(R.id.appCount);

        Button settingsButton = findViewById(R.id.settingsButton);

        searchBox.addTextChangedListener(
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
                        renderApps(s.toString());
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                }
        );

        settingsButton.setOnClickListener(v -> {
            try {
                startActivity(
                        new Intent(Settings.ACTION_SETTINGS)
                );
            } catch (Exception ignored) {
            }
        });

        loadApps();
        handler.post(clockRunnable);
    }

    private void updateClock() {
        Date now = new Date();

        SimpleDateFormat timeFormat =
                new SimpleDateFormat("HH:mm", Locale.getDefault());

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "EEEE, MMMM d",
                        Locale.getDefault()
                );

        clockText.setText(timeFormat.format(now));
        dateText.setText(dateFormat.format(now));
    }

    private void loadApps() {
        apps.clear();

        PackageManager pm = getPackageManager();

        Intent launcherIntent = new Intent(
                Intent.ACTION_MAIN,
                null
        );

        launcherIntent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        List<ApplicationInfo> installed =
                pm.getInstalledApplications(
                        PackageManager.GET_META_DATA
                );

        for (ApplicationInfo info : installed) {

            if (info.packageName.equals(getPackageName())) {
                continue;
            }

            Intent launchIntent =
                    pm.getLaunchIntentForPackage(
                            info.packageName
                    );

            if (launchIntent == null) {
                continue;
            }

            String name =
                    pm.getApplicationLabel(info).toString();

            apps.add(
                    new AppItem(
                            name,
                            info.packageName,
                            info.loadIcon(pm)
                    )
            );
        }

        Collections.sort(
                apps,
                (a, b) ->
                        a.name.compareToIgnoreCase(b.name)
        );

        renderApps("");
    }

    private void renderApps(String query) {
        appGrid.removeAllViews();

        String q =
                query == null
                        ? ""
                        : query.trim().toLowerCase(
                                Locale.getDefault()
                        );

        int visible = 0;

        for (AppItem app : apps) {

            if (!q.isEmpty()
                    && !app.name.toLowerCase(
                            Locale.getDefault()
                    ).contains(q)) {
                continue;
            }

            appGrid.addView(createAppButton(app));
            visible++;
        }

        appCount.setText(
                visible + " apps"
        );
    }

    private View createAppButton(AppItem app) {

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setGravity(
                Gravity.CENTER
        );

        int padding = dp(8);

        container.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        ImageView icon =
                new ImageView(this);

        icon.setImageDrawable(app.icon);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(
                        dp(54),
                        dp(54)
                );

        container.addView(
                icon,
                iconParams
        );

        TextView name =
                new TextView(this);

        name.setText(app.name);
        name.setTextColor(
                Color.WHITE
        );
        name.setTextSize(12);
        name.setGravity(
                Gravity.CENTER
        );
        name.setMaxLines(2);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        textParams.topMargin = dp(5);

        container.addView(
                name,
                textParams
        );

        GridLayout.LayoutParams params =
                new GridLayout.LayoutParams();

        params.width = 0;
        params.height =
                ViewGroup.LayoutParams.WRAP_CONTENT;

        params.columnSpec =
                GridLayout.spec(
                        GridLayout.UNDEFINED,
                        1f
                );

        params.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        container.setLayoutParams(params);

        container.setOnClickListener(v -> {
            try {
                Intent intent =
                        getPackageManager()
                                .getLaunchIntentForPackage(
                                        app.packageName
                                );

                if (intent != null) {
                    startActivity(intent);
                }
            } catch (Exception ignored) {
            }
        });

        container.setOnLongClickListener(v -> {

            try {
                Intent intent =
                        new Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        );

                intent.setData(
                        android.net.Uri.parse(
                                "package:" + app.packageName
                        )
                );

                startActivity(intent);

            } catch (Exception ignored) {
            }

            return true;
        });

        return container;
    }

    private int dp(int value) {
        return Math.round(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (appGrid != null) {
            loadApps();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(
                clockRunnable
        );

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Stay on the launcher.
    }

    private static class AppItem {

        final String name;
        final String packageName;
        final android.graphics.drawable.Drawable icon;

        AppItem(
                String name,
                String packageName,
                android.graphics.drawable.Drawable icon
        ) {
            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
        }
    }
  }
