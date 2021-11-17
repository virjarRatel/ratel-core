package com.virjar.ratel.manager.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.virjar.ratel.manager.BuildConfig;
import com.virjar.ratel.manager.ManagerInitiazer;
import com.virjar.ratel.manager.R;
import com.virjar.ratel.manager.engine.RatelRepkger;
import com.virjar.ratel.manager.model.RatelApp;
import com.virjar.ratel.manager.model.RatelModule;
import com.virjar.ratel.manager.repo.RatelAppRepo;
import com.virjar.ratel.manager.repo.RatelModuleRepo;

import java.io.File;
import java.text.Collator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import external.org.apache.commons.io.FileUtils;

import static com.virjar.ratel.manager.RatelManagerApp.WRITE_EXTERNAL_PERMISSION;

public class CandidateAppFragment extends ListFragment implements RatelAppRepo.RatelAppListener {
    private static final String NOT_ACTIVE_NOTE_TAG = "NOT_ACTIVE_NOTE";
    private RatelAdapter mAdapter = null;
    private Runnable reloadRatelApps = new Runnable() {

        private Set<RatelApp> reloadInternal() {
            Set<String> needFilterApps = new HashSet<>();
            for (RatelApp ratelApp : RatelAppRepo.installedApps()) {
                needFilterApps.add(ratelApp.getPackageName());
            }

            for (RatelModule ratelModule : RatelModuleRepo.installedModules()) {
                needFilterApps.add(ratelModule.getPackageName());
            }

            needFilterApps.add(BuildConfig.APPLICATION_ID);
            //virtualXposed
            needFilterApps.add("io.va.exposed");
            //taichi
            needFilterApps.add("me.weishu.exp");
            //天鉴
            needFilterApps.add("com.sk.spatch");
            //闪电盒子
            needFilterApps.add("c.l.a");
            //360分身大师x版
            needFilterApps.add("com.qihoo.magic.xposed");
            //VA开源版
            needFilterApps.add("io.virtualapp");
            //VA商业版
            needFilterApps.add("io.busniess.va");
            //VA商业版
            needFilterApps.add("io.virtualapp.addon.arm64");

            List<PackageInfo> installedPackages = ManagerInitiazer.getSContext().getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);

            Set<RatelApp> candidateRatelApps = new HashSet<>();

            for (PackageInfo packageInfo : installedPackages) {
                if (needFilterApps.contains(packageInfo.packageName)) {
                    continue;
                }
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    //系统应用无法处理
                    continue;
                }
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    continue;
                }
                RatelApp ratelApp = new RatelApp();
                candidateRatelApps.add(ratelApp);

                ratelApp.setPackageName(packageInfo.applicationInfo.packageName);
                ratelApp.setVersionName(packageInfo.versionName);
                ratelApp.setVersionCode(packageInfo.versionCode);
                ratelApp.setAppName(packageInfo.applicationInfo.loadLabel(ManagerInitiazer.getSContext().getPackageManager()).toString());

            }

            return candidateRatelApps;
        }

        public void run() {

            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            new Thread() {
                @Override
                public void run() {
                    final Set<RatelApp> candidateRatelApps = reloadInternal();
                    runOnUiThread(() -> {
                        mAdapter.addAll(candidateRatelApps);
                        final Collator col = Collator.getInstance(Locale.getDefault());
                        mAdapter.sort((lhs, rhs) -> col.compare(lhs.getAppName(), rhs.getAppName()));
                        mAdapter.notifyDataSetChanged();
                    });
                }
            }.start();
        }
    };
    private MenuItem mClickedMenuItem = null;


    private static void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new RatelAdapter(getActivity());
        reloadRatelApps.run();
        setListAdapter(mAdapter);
        setEmptyText(getActivity().getString(R.string.no_ratel_apps_found));
        registerForContextMenu(getListView());

        RatelAppRepo.addListener(this);

        ActionBar actionBar = ((WelcomeActivity) getActivity()).getSupportActionBar();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int sixDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, metrics);
        int eightDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, metrics);
        assert actionBar != null;
        int toolBarDp = actionBar.getHeight() == 0 ? 196 : actionBar.getHeight();

        getListView().setDivider(null);
        getListView().setDividerHeight(sixDp);
        getListView().setPadding(eightDp, toolBarDp + eightDp, eightDp, eightDp);
        getListView().setClipToPadding(false);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO maybe enable again after checking the implementation
        //inflater.inflate(R.menu.menu_modules, menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
        if (requestCode == WRITE_EXTERNAL_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mClickedMenuItem != null) {
                    new Handler().postDelayed(() -> onOptionsItemSelected(mClickedMenuItem), 500);
                }
            } else {
                Toast.makeText(getActivity(), R.string.permissionNotGranted, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        RatelAppRepo.removeListener(this);
        setListAdapter(null);
        mAdapter = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        RatelApp ratelApp = (RatelApp) v.getTag();
        if (ratelApp == null)
            return;
        String packageName = ratelApp.getPackageName();
        if (packageName.equals(NOT_ACTIVE_NOTE_TAG)) {
            ((WelcomeActivity) getActivity()).switchFragment(0);
            return;
        }
        Log.d("repkg", "run task " + packageName);

        ScrollView sv = new ScrollView(getActivity());
        TextView logTv = new TextView(getActivity());
        logTv.setPadding(10, 4, 10, 10);
        sv.setPadding(0, 0, 0, 6);
        sv.addView(logTv);
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(sv)
                .setTitle("任务执行中")
                .setCancelable(false)
//                .setPositiveButton(R.string.text_confirm, null)
                .show();
        new Thread() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    logTv.append("[ui] info: 任务开始\n");
                });
                try {
                    File workDir = new File(getActivity().getFilesDir().getAbsolutePath() + File.separator + "repkg" + File.separator + packageName);
                    FileUtils.deleteDirectory(workDir);
                    workDir.mkdirs();
                    File originApkFile = new File(ratelApp.getPackageInfo().applicationInfo.publicSourceDir);
                    File outFile = File.createTempFile("ratel_output", ".apk", new File(getActivity().getFilesDir().getAbsolutePath() + File.separator + "repkg"));
                    RatelRepkger.mainShell(getActivity(), new String[]{"-s", "-w", workDir.getAbsolutePath(), "-e", "rebuildDex", "-o", outFile.getAbsolutePath(), originApkFile.getAbsolutePath()}, new RatelRepkger.LogCallback() {
                        @Override
                        public void onLog(String msg) {
                            runOnUiThread(() -> {
                                logTv.append("[repkg] info: " + msg + "\n");
                                if (msg.contains("task finish")) {
                                    boolean isSuccess = msg.contains("task finish success");
                                    logTv.append("[ui] info: 任务结束," + (isSuccess ? "任务成功" : "任务失败") + "\n");
                                    dialog.setCancelable(true);
                                    if (isSuccess) {
                                        dialog.dismiss();
                                        AlertDialog dialog2 = new AlertDialog.Builder(getActivity())
                                                .setTitle("感染成功的提示")
                                                .setMessage("由于签名机制，请先卸载原应用")
                                                .setCancelable(false)
                                                .setNeutralButton("取消", null)
                                                .setNegativeButton("去卸载", null)
                                                .setPositiveButton("我已经卸载,去安装", null).create();
                                        dialog2.setOnShowListener(d -> {
                                            dialog2.getButton(AlertDialog.BUTTON_NEGATIVE)
                                                    .setOnClickListener(v1 -> {
                                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                        intent.setData(Uri.fromParts("package", packageName, null));
                                                        startActivity(intent);
                                                    });
                                            dialog2.getButton(AlertDialog.BUTTON_POSITIVE)
                                                    .setOnClickListener(v1 -> {
                                                        installApk(getActivity(), outFile.getAbsolutePath());
                                                        dialog2.dismiss();
                                                    });
                                        });
                                        dialog2.show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError(String msg) {
                            runOnUiThread(() -> {
                                logTv.append("[repkg] error: " + msg + "\n");
                                logTv.append("[ui] info: 任务结束," + "任务失败" + "\n");
                                dialog.setCancelable(true);
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("repkg", e.getMessage(), e);
                    runOnUiThread(() -> {
                        logTv.append("[ui] error: " + e.getMessage() + "\n");
                        logTv.append("[ui] info: 任务结束," + "任务失败" + "\n");
                        dialog.setCancelable(true);
                    });
                }
            }
        }.start();
    }

    public void installApk(Context context, String filePath) {
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(
                    context
                    , "com.virjar.ratel.installer.fileprovider"
                    , apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    @Override
    public void onRatelAppReload(String packageName) {
        runOnUiThread(reloadRatelApps);
    }

    private class RatelAdapter extends ArrayAdapter<RatelApp> {
        RatelAdapter(Context context) {
            super(context, R.layout.list_item_module, R.id.title);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            RatelApp item = getItem(position);
            if (item == null) {
                throw new IllegalStateException("can not get ratel module item from index: " + position);
            }

            TextView version = view.findViewById(R.id.version_name);

            version.setText(item.getVersionName());

            TextView description = view.findViewById(R.id.description);
            description.setText(item.getPackageName());

            // Store the package name in some views' tag for later access
            view.findViewById(R.id.checkbox).setTag(item.getPackageName());
            view.setTag(item);

            ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(item.getOrLoadIcon(getContext()));

            CheckBox checkbox = view.findViewById(R.id.checkbox);
            checkbox.setVisibility(View.GONE);

            return view;
        }
    }

}
