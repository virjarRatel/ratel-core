package com.virjar.ratel.manager.ui;

import android.app.ListFragment;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.virjar.ratel.buildsrc.Constants;
import com.virjar.ratel.manager.BuildConfig;
import com.virjar.ratel.manager.ManagerInitiazer;
import com.virjar.ratel.manager.R;
import com.virjar.ratel.manager.model.RatelApp;
import com.virjar.ratel.manager.model.RatelModule;
import com.virjar.ratel.manager.repo.RatelAppRepo;
import com.virjar.ratel.manager.repo.RatelModuleRepo;

import java.text.Collator;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.addAll(candidateRatelApps);
                            final Collator col = Collator.getInstance(Locale.getDefault());
                            mAdapter.sort(new Comparator<RatelApp>() {
                                @Override
                                public int compare(RatelApp lhs, RatelApp rhs) {
                                    return col.compare(lhs.getAppName(), rhs.getAppName());
                                }
                            });
                            mAdapter.notifyDataSetChanged();
                        }
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
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onOptionsItemSelected(mClickedMenuItem);
                        }
                    }, 500);
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
        String packageName = (String) v.getTag();
        if (packageName == null)
            return;

        if (packageName.equals(NOT_ACTIVE_NOTE_TAG)) {
            ((WelcomeActivity) getActivity()).switchFragment(0);
            return;
        }


//        Intent intent = new Intent(getActivity(), RatelAppDetailActivity.class);
//        intent.putExtra("ratelAppPackage", packageName);
//        getActivity().startActivity(intent);
        //TODO
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
            view.setTag(item.getPackageName());

            ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(item.getOrLoadIcon(getContext()));

            CheckBox checkbox = view.findViewById(R.id.checkbox);
            checkbox.setVisibility(View.GONE);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(Constants.TAG, "apk path: " + item.getPackageInfo().applicationInfo.sourceDir);
                }
            });
            return view;
        }
    }

}
