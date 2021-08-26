package com.virjar.ratel.manager.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.virjar.ratel.manager.R;
import com.virjar.ratel.manager.model.RatelCertificate;
import com.virjar.ratel.manager.repo.RatelCertificateRepo;
import com.virjar.ratel.manager.util.ThemeUtil;
import com.virjar.ratel.manager.widget.MyListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class CertificateDetailActivity extends XposedBaseActivity {
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
            ab.setTitle(R.string.nav_certificate_detail);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setFloating(toolbar, R.string.nav_certificate_detail);

        String licenceId = getIntent().getStringExtra("licenceId");
        if (TextUtils.isEmpty(licenceId)) {
            finish();
            return;
        }

        RatelCertificate ratelCertificate = RatelCertificateRepo.queryById(licenceId);
        if (ratelCertificate == null) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new CertificateDetailFragment().setRatelCertificate(ratelCertificate)).commit();
        }
    }

    private enum AuthorizeType {
        AuthorizeTypeDebug(0x0001),
        AuthorizeTypePerson(0x0002),
        AuthorizeTypeQunar(0x0004),
        AuthorizeTypeTest(0x0008),
        AuthorizeTypeMiniGroup(0x0010),
        AuthorizeTypeMediumGroup(0x0020),
        AuthorizeTypeBigGroup(0x0040),
        AuthorizeTypeSlave(0x0080),
        AuthorizeTypeA(0x0100),
        AuthorizeTypeB(0x0200),
        AuthorizeTypeC(0x0400),
        AuthorizeTypeD(0x0800),
        AuthorizeTypeE(0x1000),
        AuthorizeTypeF(0x2000),
        AuthorizeTypeG(0x4000),
        AuthorizeTypeH(0x8000),
        ;
        public int typeMask;

        AuthorizeType(int typeMask) {
            this.typeMask = typeMask;
        }
    }

    private static String join(String[] strings) {
        if (strings == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            sb.append(str).append(",");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static class CertificateDetailFragment extends Fragment {
        private RatelCertificate ratelCertificate;


        public CertificateDetailFragment setRatelCertificate(RatelCertificate ratelCertificate) {
            this.ratelCertificate = ratelCertificate;
            return this;
        }


        private void setListView(View root, int id, String[] content) {
            MyListView certificateTypeListView = root.findViewById(id);


            ArrayList<String> typeEnum = new ArrayList<>(Arrays.asList(content));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, typeEnum);
            certificateTypeListView.setAdapter(adapter);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.tab_certificate_detail, container, false);

            ThemeUtil.setTextView(v, R.id.certificate_id_content, ratelCertificate.getLicenceId());
            ThemeUtil.setTextView(v, R.id.certificate_account_content, ratelCertificate.getAccount());
            ThemeUtil.setTextView(v, R.id.certificate_version_content, String.valueOf(ratelCertificate.getLicenceVersion()));
            ThemeUtil.setTextView(v, R.id.certificate_protocol_version_content, String.valueOf(ratelCertificate.getLicenceProtocolVersion()));
            ThemeUtil.setTextView(v, R.id.certificate_expire_content, new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(ratelCertificate.getExpire())));

            int licenceType = ratelCertificate.getLicenceType();
            ArrayList<String> typeEnum = new ArrayList<>();
            for (AuthorizeType authorizeType : AuthorizeType.values()) {
                if ((licenceType & authorizeType.typeMask) != 0) {
                    typeEnum.add(authorizeType.name());
                }
            }
            setListView(v, R.id.certificate_type_content, typeEnum.toArray(new String[]{}));
            if (ratelCertificate.getPackageList() != null) {
                setListView(v, R.id.certificate_package_list_content, ratelCertificate.getPackageList().split(","));
            }

            if (ratelCertificate.getDeviceList() != null) {
                setListView(v, R.id.certificate_devices_list_content, ratelCertificate.getDeviceList().split(","));
            }
            ThemeUtil.setTextView(v, R.id.certificate_extra_content, ratelCertificate.getExtra());
            ThemeUtil.setTextView(v, R.id.certificate_payload_content, ratelCertificate.getPayload());
            return v;
        }
    }
}
