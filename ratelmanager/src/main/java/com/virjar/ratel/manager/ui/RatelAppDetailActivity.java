package com.virjar.ratel.manager.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.VirtualEnv;
import com.virjar.ratel.manager.AppDaemonTaskManager;
import com.virjar.ratel.manager.R;
import com.virjar.ratel.manager.RatelManagerApp;
import com.virjar.ratel.manager.bridge.IRatelRemoteControlHandler;
import com.virjar.ratel.manager.bridge.MultiUserBundle;
import com.virjar.ratel.manager.component.AppWatchDogService;
import com.virjar.ratel.manager.model.RatelApp;
import com.virjar.ratel.manager.repo.RatelAppRepo;
import com.virjar.ratel.manager.util.NavUtil;
import com.virjar.ratel.manager.util.ThemeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RatelAppDetailActivity extends XposedBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTheme(this);
        setContentView(R.layout.activity_container);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.nav_item_ratel_apps);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setFloating(toolbar, R.string.nav_item_ratel_apps);

        String ratelAppPackage = getIntent().getStringExtra("ratelAppPackage");
        if (TextUtils.isEmpty(ratelAppPackage)) {
            finish();
            return;
        }

        RatelApp ratelApp = RatelAppRepo.findByPackage(ratelAppPackage);
        if (ratelApp == null) {
            Log.w(RatelManagerApp.TAG, "can the app: " + ratelAppPackage + " is not a ratel app");
            finish();
            return;
        }

        PackageInfo packageInfo;
        try {
            packageInfo = getPackageManager().getPackageInfo(ratelAppPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(RatelManagerApp.TAG, "the app: " + ratelAppPackage + " is not installed now");
            finish();
            return;
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(
                    R.id.container,
                    new RatelAppDetailFragment()
                            .setRatelApp(ratelApp)
                            .setPackageInfo(packageInfo)
            ).commit();
        }
    }

    public static class RatelAppDetailFragment extends Fragment {
        private RatelApp ratelApp;
        private PackageInfo packageInfo;

        public RatelAppDetailFragment setRatelApp(RatelApp ratelApp) {
            this.ratelApp = ratelApp;
            return this;
        }

        public RatelAppDetailFragment setPackageInfo(PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
            return this;
        }

        private void setTimestampTextView(View root, int id, String key, Properties properties) {
            String ratelEngineBuildTimestamp = properties.getProperty(key);
            if (TextUtils.isEmpty(ratelEngineBuildTimestamp)) {
                ratelEngineBuildTimestamp = "unknown";
            } else {
                ratelEngineBuildTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(Long.parseLong(ratelEngineBuildTimestamp)));
            }
            ThemeUtil.setTextView(root, id, ratelEngineBuildTimestamp);
        }


        private Properties setMetaWithRatelConfigFile(View rootView) throws IOException {
            ZipFile zipFile = new ZipFile(packageInfo.applicationInfo.sourceDir);
            ZipEntry entry = zipFile.getEntry("assets/ratelConfig.properties");
            Properties properties = new Properties();
            InputStream inputStream = zipFile.getInputStream(entry);
            properties.load(inputStream);
            inputStream.close();
            zipFile.close();

            //ratel engine
            ThemeUtil.setTextView(rootView, R.id.engine_version, properties.getProperty("ratel_engine_versionName", "unknown"));
            setTimestampTextView(rootView, R.id.ratel_engine_build_time_content, "ratel_engine_buildTimestamp", properties);
            //build meta
            ThemeUtil.setTextView(rootView, R.id.ratel_app_build_id_content, properties.getProperty("ratel_serialNo", "unknown"));
            ThemeUtil.setTextView(rootView, R.id.certificate_id_content, properties.getProperty("ratel_certificate_id", "unknown"));
            setTimestampTextView(rootView, R.id.rate_app_build_time_id, "ratel_buildTimestamp", properties);
            setTimestampTextView(rootView, R.id.certificate_expire_content, "ratel_certificate_expire", properties);
            return properties;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.tab_ratel_app_detail, container, false);
            ImageView imageView = view.findViewById(R.id.icon);
            imageView.setImageDrawable(ratelApp.getOrLoadIcon(getActivity()));

            ThemeUtil.setTextView(view, R.id.ratel_source_dir, packageInfo.applicationInfo.sourceDir);
            TextView sourceDirTextVIew = view.findViewById(R.id.ratel_source_dir);
            sourceDirTextVIew.setOnClickListener(v -> Log.i(Constants.TAG, "apk path: " + packageInfo.applicationInfo.sourceDir));

            ThemeUtil.setTextView(view, R.id.app_package, ratelApp.getPackageName());
            ThemeUtil.setTextView(view, R.id.app_name, ratelApp.getAppName());
            ThemeUtil.setTextView(view, R.id.app_version, ratelApp.getVersionName());
            view.findViewById(R.id.app_meta_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtil.startApp(getActivity(), ratelApp.getPackageName());
                }
            });
            Properties ratelApkRatelConfig = new Properties();
            try {
                ratelApkRatelConfig = setMetaWithRatelConfigFile(view);
            } catch (IOException e) {
                Log.e(RatelManagerApp.TAG, "failed to load ratelConfig.properties from ratel apk sourceApk", e);
            }

            SwitchCompat switchCompat = view.findViewById(R.id.ratel_daemon_switch);
            switchCompat.setChecked(ratelApp.isDaemon());

            final Properties finalRatelApkRatelConfig = ratelApkRatelConfig;
            String ratelEngineVersionCode = finalRatelApkRatelConfig.getProperty("ratel_engine_versionCode");
            int ratelEngineVersionCodeInt = -1;
            if (ratelEngineVersionCode != null) {
                try {
                    ratelEngineVersionCodeInt = Integer.parseInt(ratelEngineVersionCode);
                } catch (NumberFormatException e) {
                    Log.w(RatelManagerApp.TAG, "some one edit a wrong ratelConfig??", e);
                }
            }

            final int finalRatelEngineVersionCodeInt = ratelEngineVersionCodeInt;
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        if (finalRatelEngineVersionCodeInt <= 2) {
                            Toast.makeText(getActivity(), "this app use lower ratelEngine,can not support watch process,please upgrade ratel engine ", Toast.LENGTH_SHORT).show();
                            SwitchCompat switchCompat = view.findViewById(R.id.ratel_daemon_switch);
                            switchCompat.setChecked(false);

                            new AlertDialog.Builder(getActivity())
                                    .setMessage("this app:" + ratelApp.getPackageName() + "  build by a lower version of ratelEngine" +
                                            ",and can not support watched  process by ratel manager ,process watch only supported before" +
                                            " android 7.0 or with ratel engine version greater than 1.1,please upgrade ratel engine!!")
                                    .setTitle("can not watch")
                                    .setPositiveButton(R.string.md_done_label, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).create().show();
                            return;
                        }

                    }
                    ratelApp.setDaemon(isChecked);
                    RatelAppRepo.addRatelApp(ratelApp);
                    AppDaemonTaskManager.updateDaemonStatus(ratelApp.getPackageName(), isChecked);
                }
            });

            Button killAppBtn = view.findViewById(R.id.ratel_force_stop_app_btn);
            killAppBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        IRatelRemoteControlHandler ratelRemoteControlHandler = AppWatchDogService.queryRemoteHandler(ratelApp.getPackageName());
                        if (ratelRemoteControlHandler == null) {
                            Toast.makeText(getActivity(), "app not running", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ratelRemoteControlHandler.killMe();
                        NavUtil.startApp(getActivity(), ratelApp.getPackageName());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "call failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            if (ratelEngineVersionCodeInt >= 5) {
                initMultiUserPanel(view, ratelApp);
            } else {
                view.findViewById(R.id.multi_user_container_card).setVisibility(View.GONE);
            }
            return view;
        }

        private void initMultiUserPanel(View fragmentView, RatelApp ratelApp) {
            RecyclerView recyclerView = fragmentView.findViewById(R.id.multi_user_container);
            StaggeredGridLayoutManager staggeredGridLayoutManager =
                    new StaggeredGridLayoutManager(3,
                            StaggeredGridLayoutManager.VERTICAL);

            recyclerView.setLayoutManager(staggeredGridLayoutManager);

            SwitchCompat apiSwitch = fragmentView.findViewById(R.id.ratel_disable_api_switch);
            SwitchCompat hotmoduleSwitch = fragmentView.findViewById(R.id.ratel_hotmodle_switch);
            SwitchCompat switchEnvModel = fragmentView.findViewById(R.id.ratel_turn_on_multi);

            Button button = fragmentView.findViewById(R.id.ratel_add_devices);

            myAdapter = new MyAdapter(fragmentView.getContext(),
                    (CardView) fragmentView.findViewById(R.id.multi_user_container_card),
                    ratelApp,
                    apiSwitch,
                    button,
                    hotmoduleSwitch,
                    switchEnvModel
            );
            recyclerView.setAdapter(myAdapter);

            AppWatchDogService.addRemoteControlHandlerStatusListener(ratelApp.getPackageName(), myAdapter);


        }

        MyAdapter myAdapter;

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            AppWatchDogService.removeRemoteControlHandlerListener(ratelApp.getPackageName(), myAdapter);

        }
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements AppWatchDogService.RemoteControlHandlerStatusChangeEvent {

        private Context context;
        private CardView cardView;
        private RatelApp ratelApp;
        private SwitchCompat apiSwitch;
        private MultiUserBundle multiUserBundle;
        private Button createDeviceBtn;
        private SwitchCompat hotModuleSwitch;
        private SwitchCompat switchEnvModel;

        public MyAdapter(Context context, CardView cardView, RatelApp ratelApp, SwitchCompat apiSwitch,
                         Button createDeviceBtn, SwitchCompat hotModuleSwitch, SwitchCompat switchEnvModel) {
            this.context = context;
            this.cardView = cardView;
            this.ratelApp = ratelApp;
            this.apiSwitch = apiSwitch;
            this.createDeviceBtn = createDeviceBtn;
            this.hotModuleSwitch = hotModuleSwitch;
            this.switchEnvModel = switchEnvModel;

            switchEnvModel.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!buttonView.isPressed()) {
                    return;
                }
                IRatelRemoteControlHandler iRatelRemoteControlHandler = AppWatchDogService.queryRemoteHandler(MyAdapter.this.ratelApp.getPackageName());
                if (iRatelRemoteControlHandler == null) {
                    Toast.makeText(MyAdapter.this.context, "process detached", Toast.LENGTH_SHORT).show();
                    buttonView.setChecked(!isChecked);
                } else if (!isChecked) {
                    Toast.makeText(MyAdapter.this.context, "can't turn off multi", Toast.LENGTH_LONG).show();
                    buttonView.setChecked(true);
                } else {
                    try {
                        boolean success = iRatelRemoteControlHandler.switchEnvModel(VirtualEnv.VirtualEnvModel.MULTI.name());
                        buttonView.setChecked(success);
                        if (success) {
                            Toast.makeText(MyAdapter.this.context, "turn on multi success, restart the target app to take effect", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MyAdapter.this.context, "RatelManager not match RatelEngine, Please check!", Toast.LENGTH_LONG).show();
                        }
                    } catch (RemoteException e) {
                        Toast.makeText(MyAdapter.this.context, "operate failed:ratelEngine>5.0?", Toast.LENGTH_SHORT).show();
                        Log.e(Constants.TAG, "operate failed", e);
                    }
                }
            });

            hotModuleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                IRatelRemoteControlHandler iRatelRemoteControlHandler = AppWatchDogService.queryRemoteHandler(MyAdapter.this.ratelApp.getPackageName());
                if (iRatelRemoteControlHandler == null) {
                    Toast.makeText(MyAdapter.this.context, "process detached", Toast.LENGTH_SHORT).show();
                    buttonView.setChecked(!isChecked);
                    return;
                }
                try {
                    iRatelRemoteControlHandler.updateHotmoduleStatus(!isChecked);
                } catch (RemoteException e) {
                    Toast.makeText(MyAdapter.this.context, "operate failed:ratelEngine>5.0?", Toast.LENGTH_SHORT).show();
                    Log.e(Constants.TAG, "operate failed", e);
                }
            });

            apiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    IRatelRemoteControlHandler iRatelRemoteControlHandler = AppWatchDogService.queryRemoteHandler(MyAdapter.this.ratelApp.getPackageName());
                    if (iRatelRemoteControlHandler == null) {
                        Toast.makeText(MyAdapter.this.context, "process detached", Toast.LENGTH_SHORT).show();
                        buttonView.setChecked(!isChecked);
                        return;
                    }

                    if (!multiUserBundle.isMultiVirtualEnv()) {
                        Toast.makeText(MyAdapter.this.context, "not running on multi user mode", Toast.LENGTH_SHORT).show();
                        buttonView.setChecked(!isChecked);
                        return;
                    }

                    try {
                        iRatelRemoteControlHandler.updateMultiEnvAPISwitchStatus(!isChecked);
                        multiUserBundle.setDisableMultiUserAPiSwitch(isChecked);
                    } catch (RemoteException e) {
                        Toast.makeText(MyAdapter.this.context, "operate failed", Toast.LENGTH_SHORT).show();
                        Log.e(Constants.TAG, "operate failed", e);
                        buttonView.setChecked(!isChecked);
                    }

                }
            });

            createDeviceBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IRatelRemoteControlHandler iRatelRemoteControlHandler = AppWatchDogService.queryRemoteHandler(MyAdapter.this.ratelApp.getPackageName());
                    if (iRatelRemoteControlHandler == null) {
                        Toast.makeText(MyAdapter.this.context, "process detached", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!multiUserBundle.isMultiVirtualEnv()) {
                        Toast.makeText(MyAdapter.this.context, "not running on multi user mode", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final EditText editText = new EditText(v.getContext());
                    //editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    //editText.setMinLines(5);
                    //editText.setSingleLine(false);
                    new AlertDialog.Builder(v.getContext())
                            .setView(editText)
                            .setTitle(R.string.ratel_add_devices_str)
                            .setNegativeButton(R.string.md_cancel_label, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(R.string.md_done_label, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String trim = editText.getText().toString().trim();
                                    doAddNewDevices(trim);
                                }
                            }).create().show();

                }
            });

        }

        private void doAddNewDevices(String newUserId) {
            if (multiUserBundle.getAvailableUserSet().contains(newUserId)) {
                Toast.makeText(MyAdapter.this.context, "user existed", Toast.LENGTH_SHORT).show();
                return;
            }

            IRatelRemoteControlHandler iRatelRemoteControlHandler = AppWatchDogService.queryRemoteHandler(MyAdapter.this.ratelApp.getPackageName());
            if (iRatelRemoteControlHandler == null) {
                Toast.makeText(MyAdapter.this.context, "process detached", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                iRatelRemoteControlHandler.addUser(newUserId);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //刷新界面
                        onRemoteHandlerStatusChange(ratelApp.getAppName(), AppWatchDogService.queryRemoteHandler(ratelApp.getPackageName()));
                    }
                });
            } catch (RemoteException e) {
                Toast.makeText(MyAdapter.this.context, "operate failed", Toast.LENGTH_SHORT).show();
                Log.e(RatelManagerApp.TAG, "operate failed", e);
            }
        }

        private List<String> accountList = new ArrayList<>();

        private Set<String> newData = null;

        private String nowActiveUser = null;

        private boolean multiModel = false;

        private boolean hotModuleStatus = false;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_mult_account, parent, false);
            //return new ViewHolder(new TextView(parent.getContext()));

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.textView.setText(accountList.get(position));
            holder.userId = accountList.get(position);

            if (holder.userId.equals(nowActiveUser)) {
                holder.textView.setTextColor(holder.textView.getResources().getColor(R.color.darker_green));
            } else {
                holder.textView.setTextColor(holder.textView.getResources().getColor(R.color.app_background_black));
            }

            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.multi_account_popup, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            IRatelRemoteControlHandler iRatelRemoteControlHandler = null;
                            switch (menuItem.getItemId()) {
                                case R.id.mult_user_active:
                                    if (holder.userId.equals(nowActiveUser)) {
                                        //当前用户就是
                                        return true;
                                    }
                                    if (!multiUserBundle.isDisableMultiUserAPiSwitch()) {
                                        Toast.makeText(v.getContext(), "multi user api switch not disable,manager switch can reset by ratel module maybe!!", Toast.LENGTH_SHORT).show();
                                    }
                                    iRatelRemoteControlHandler = AppWatchDogService.queryRemoteHandler(ratelApp.getPackageName());
                                    if (iRatelRemoteControlHandler == null) {
                                        Toast.makeText(v.getContext(), "process detached", Toast.LENGTH_SHORT).show();
                                        return true;
                                    }
                                    try {
                                        //理论上app会自杀
                                        iRatelRemoteControlHandler.switchEnv(holder.userId);
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                NavUtil.startApp(holder.textView.getContext(), ratelApp.getPackageName());
                                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        NavUtil.startApp(holder.textView.getContext(), ratelApp.getPackageName());
                                                    }
                                                }, 5000);
                                            }
                                        });


                                    } catch (RemoteException e) {
                                        Toast.makeText(v.getContext(), "operate failed", Toast.LENGTH_SHORT).show();
                                        Log.e(RatelManagerApp.TAG, "operate failed", e);
                                        return true;
                                    }
                                    break;
                                case R.id.multi_user_delete:
                                    if (holder.userId.equals(nowActiveUser)) {
                                        Toast.makeText(v.getContext(), "can not remote active device", Toast.LENGTH_SHORT).show();
                                        return true;
                                    }
                                    iRatelRemoteControlHandler = AppWatchDogService.queryRemoteHandler(ratelApp.getPackageName());
                                    if (iRatelRemoteControlHandler == null) {
                                        Toast.makeText(v.getContext(), "process detached", Toast.LENGTH_SHORT).show();
                                        return true;
                                    }
                                    try {
                                        iRatelRemoteControlHandler.removeUser(holder.userId);
                                        //刷新界面
                                        onRemoteHandlerStatusChange(ratelApp.getAppName(), iRatelRemoteControlHandler);
                                    } catch (RemoteException e) {
                                        Toast.makeText(v.getContext(), "operate failed", Toast.LENGTH_SHORT).show();
                                        Log.e(RatelManagerApp.TAG, "operate failed", e);
                                        return true;
                                    }
                                    break;
                                default:
                                    Log.w(RatelManagerApp.TAG, "unknown menu item");
                            }

                            return true;
                        }
                    });
                    popup.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return accountList.size();
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());

        @Override
        public void onRemoteHandlerStatusChange(String appPackage, IRatelRemoteControlHandler iRatelRemoteControlHandler) {
            if (iRatelRemoteControlHandler == null) {
                Log.w(RatelManagerApp.TAG, "iRatelRemoteControlHandler change to null!!");
                newData = null;
            } else {
                try {
                    multiUserBundle = iRatelRemoteControlHandler.multiUserStatus();
                    if (!multiUserBundle.isMultiVirtualEnv()) {
                        newData = null;
                        multiModel = false;
                    } else {
                        List<String> availableUserSet = multiUserBundle.getAvailableUserSet();
                        newData = new HashSet<>(availableUserSet);
                        nowActiveUser = multiUserBundle.getNowUser();
                        multiModel = true;
                    }
                } catch (RemoteException e) {
                    mainHandler.post(() -> {
                        Toast.makeText(context, "sync availableUserSet failed!!", Toast.LENGTH_LONG).show();
                    });
                    newData = null;
                    Log.w(RatelManagerApp.TAG, "sync availableUserSet failed!!");
                } catch (Throwable throwable) {
                    Log.w(RatelManagerApp.TAG, "sync availableUserSet failed!!", throwable);
                }
                try {
                    hotModuleStatus = iRatelRemoteControlHandler.getHotmoduleStatus();
                } catch (RemoteException e) {
                    mainHandler.post(() -> {
                        Toast.makeText(context, "sync hotModuleStatus failed!!", Toast.LENGTH_LONG).show();
                    });
                    Log.w(RatelManagerApp.TAG, "sync hotModuleStatus failed!!");
                } catch (Throwable throwable) {
                    Log.w(RatelManagerApp.TAG, "sync hotModuleStatus failed!!", throwable);
                }
            }
            mainHandler.post(() -> {
                accountList = new ArrayList<>();
                if (newData != null) {
                    if (cardView.getVisibility() == View.GONE) {
                        cardView.setVisibility(View.VISIBLE);
                    }
                    accountList.addAll(newData);
                    apiSwitch.setClickable(true);
                    apiSwitch.setChecked(multiUserBundle.isDisableMultiUserAPiSwitch());
                } else {
                    cardView.setVisibility(View.GONE);
                    apiSwitch.setClickable(false);
                }

                final Collator col = Collator.getInstance(Locale.getDefault());
                Collections.sort(accountList, new Comparator<String>() {
                    @Override
                    public int compare(String lhs, String rhs) {
                        return col.compare(lhs, rhs);
                    }
                });
                switchEnvModel.setChecked(multiModel);
                hotModuleSwitch.setChecked(!hotModuleStatus);
                MyAdapter.this.notifyDataSetChanged();
            });
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            String userId;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.multi_user_text);
            }
        }
    }


}
