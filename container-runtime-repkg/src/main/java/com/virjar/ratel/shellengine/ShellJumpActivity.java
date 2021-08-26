package com.virjar.ratel.shellengine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.engines.EngineShell;
import com.virjar.ratel.runtime.RatelRuntime;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import external.org.apache.commons.io.output.ByteArrayOutputStream;

@SuppressLint("Registered")
public class ShellJumpActivity extends Activity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //她妈的，可能背景黑色，然后我们的控件如果是透明，看起来像卡屏一样
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams layoutParamsRoot = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsRoot.gravity = Gravity.CENTER;

        linearLayout.setLayoutParams(layoutParamsRoot);


        ProgressBar progressBar = new ProgressBar(this);

        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        progressBar.setBackgroundColor(Color.YELLOW);
        progressBar.setBottom(8);

        linearLayout.addView(progressBar);

        TextView textView = new TextView(this);
        textView.setText("ratel engine loading");
        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textViewLayoutParams.topMargin = 50;
        textView.setLayoutParams(textViewLayoutParams);


        linearLayout.addView(textView);


        setContentView(linearLayout);

        final long loadStart = System.currentTimeMillis();

        EngineShell.installRatelShellEngine(new InstallCallback() {
            @Override
            public void onInstallFailed(Throwable throwable) {
                throwable.printStackTrace();
                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                throwable.printStackTrace(new PrintStream(byteArrayOutputStream));
                ShellJumpActivity.this.runOnUiThread(() -> textView.setText(byteArrayOutputStream.toString(StandardCharsets.UTF_8))
                );
            }

            @Override
            public void onInstallSucced() {

                new Thread() {
                    @Override
                    public void run() {
//                        long sleep = loadStart + 4000 - System.currentTimeMillis();
//                        if (sleep > 0) {
//                            try {
//                                Thread.sleep(sleep);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
                        ShellJumpActivity.this.runOnUiThread(() -> {
                            Intent intent = new Intent(RatelRuntime.realApplication,
                                    RposedHelpers.findClass(
                                            RatelRuntime.originLaunchActivity,
                                            RatelRuntime.realApplication.getClassLoader()
                                    )
                            );
                            ShellJumpActivity.this.startActivity(intent);
                            ShellJumpActivity.this.finish();
                        });
                    }
                }.start();


            }
        });
    }
}
