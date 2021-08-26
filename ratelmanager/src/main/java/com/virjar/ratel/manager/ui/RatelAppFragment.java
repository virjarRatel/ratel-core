package com.virjar.ratel.manager.ui;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.virjar.ratel.manager.R;
import com.virjar.ratel.manager.RatelManagerApp;
import com.virjar.ratel.manager.model.RatelApp;
import com.virjar.ratel.manager.repo.RatelAppRepo;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.virjar.ratel.manager.RatelManagerApp.WRITE_EXTERNAL_PERMISSION;

public class RatelAppFragment extends ListFragment implements RatelAppRepo.RatelAppListener {
    public static final String SETTINGS_CATEGORY = "de.robv.android.xposed.category.MODULE_SETTINGS";
    private static final String NOT_ACTIVE_NOTE_TAG = "NOT_ACTIVE_NOTE";
    private RatelAdapter mAdapter = null;
    private Runnable reloadRatelApps = new Runnable() {
        public void run() {
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.addAll(RatelAppRepo.installedApps());
            final Collator col = Collator.getInstance(Locale.getDefault());
            mAdapter.sort(new Comparator<RatelApp>() {
                @Override
                public int compare(RatelApp lhs, RatelApp rhs) {
                    return col.compare(lhs.getAppName(), rhs.getAppName());
                }
            });
            mAdapter.notifyDataSetChanged();
        }
    };
    private MenuItem mClickedMenuItem = null;


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


        Intent intent = new Intent(getActivity(), RatelAppDetailActivity.class);
        intent.putExtra("ratelAppPackage", packageName);
        getActivity().startActivity(intent);
    }


    @Override
    public void onRatelAppReload(String packageName) {
        getActivity().runOnUiThread(reloadRatelApps);
    }

    private class RatelAdapter extends ArrayAdapter<RatelApp> {
        RatelAdapter(Context context) {
            super(context, R.layout.list_item_module, R.id.title);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            if (convertView == null) {
                // The reusable view was created for the first time, set up the
                // listener on the checkbox
                ((CheckBox) view.findViewById(R.id.checkbox)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String packageName = (String) buttonView.getTag();
                        RatelApp ratelModule = RatelAppRepo.findByPackage(packageName);
                        if (ratelModule == null) {
                            Log.w(RatelManagerApp.TAG, "ratel app " + packageName + " not existed!!");
                            Toast.makeText(getContext(), "ratel app " + packageName + " not existed!!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        ratelModule.setEnabled(isChecked);
                        ratelModule.update();
                    }
                });
            }

            RatelApp item = getItem(position);


            if (item == null) {
                throw new IllegalStateException("can not get ratel module item from index: " + position);
            }

            TextView version = view.findViewById(R.id.version_name);

            version.setText(item.getVersionName());

            // Store the package name in some views' tag for later access
            view.findViewById(R.id.checkbox).setTag(item.getPackageName());
            view.setTag(item.getPackageName());

            ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(item.getOrLoadIcon(getContext()));

//            TextView descriptionText = view.findViewById(R.id.description);
//            if (!item.getDescription().isEmpty()) {
//                descriptionText.setText(item.getDescription());
//                descriptionText.setTextColor(ThemeUtil.getThemeColor(getContext(), android.R.attr.textColorSecondary));
//            } else {
//                descriptionText.setText(getString(R.string.module_empty_description));
//                descriptionText.setTextColor(getResources().getColor(R.color.warning));
//            }

            CheckBox checkbox = view.findViewById(R.id.checkbox);
            checkbox.setChecked(item.isEnabled());
            return view;
        }
    }

}
