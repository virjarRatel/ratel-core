package com.virjar.ratel.manager.ui;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.virjar.ratel.manager.R;
import com.virjar.ratel.manager.RatelManagerApp;
import com.virjar.ratel.manager.model.RatelCertificate;
import com.virjar.ratel.manager.repo.RatelCertificateRepo;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class CertificateFragment extends ListFragment implements RatelCertificateRepo.RatelCertificateListener {
    private CertificateAdapter mAdapter = null;
    private Runnable reloadModules = new Runnable() {
        public void run() {
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.add(new RatelCertificate());
            mAdapter.addAll(RatelCertificateRepo.storedCertificate());
//            mAdapter.addAll(RatelModuleRepo.installedModules());
            final Collator col = Collator.getInstance(Locale.getDefault());
            mAdapter.sort(new Comparator<RatelCertificate>() {
                @Override
                public int compare(RatelCertificate lhs, RatelCertificate rhs) {
                    if (lhs.getLicenceId() == null) {
                        return -1;
                    } else if (rhs.getLicenceId() == null) {
                        return 1;
                    }
                    return col.compare(lhs.getLicenceId(), rhs.getLicenceId());
                }
            });
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new CertificateAdapter(getActivity());
        reloadModules.run();
        setListAdapter(mAdapter);
        setEmptyText(getActivity().getString(R.string.no_certificate_imported));
        registerForContextMenu(getListView());

        RatelCertificateRepo.addListener(this);

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
    public void onRatelCertificateReload() {
        getActivity().runOnUiThread(reloadModules);
    }

    private class CertificateAdapter extends ArrayAdapter<RatelCertificate> {

        CertificateAdapter(Context context) {
            super(context, R.layout.list_item_certificate, R.id.certificate_id);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            RatelCertificate item = getItem(position);

            if (item == null) {
                throw new IllegalStateException("can not get RatelCertificate item from index: " + position);
            }

            if (item.getLicenceId() == null) {
                View importContainer = view.findViewById(R.id.certificate_import_button_container);
                importContainer.setVisibility(View.VISIBLE);
                Button button = view.findViewById(R.id.certificate_import_button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final EditText editText = new EditText(getActivity());
                        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                        editText.setMinLines(5);
                        editText.setSingleLine(false);
                        new AlertDialog.Builder(getActivity())
                                .setView(editText)
                                .setTitle(R.string.import_)
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
                                        importCertificate(trim);
                                    }
                                }).create().show();
                    }
                });
                view.findViewById(R.id.certificate_container).setVisibility(View.GONE);
                return view;
            }

            TextView version = view.findViewById(R.id.certificate_version);
            version.setText(String.valueOf(item.getLicenceVersion()));

            TextView account = view.findViewById(R.id.certificate_account);
            account.setText(item.getAccount());

            TextView extra = view.findViewById(R.id.certificate_extra);
            extra.setText(item.getExtra());

            view.setTag(item.getLicenceId());
            return view;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        RatelCertificateRepo.removeListener(this);
        setListAdapter(null);
        mAdapter = null;
    }


    private void importCertificate(String payload) {
        if (TextUtils.isEmpty(payload)) {
            return;
        }
        Log.i(RatelManagerApp.TAG, "import a new certificate: " + payload);
        String errorMessage = RatelCertificateRepo.importCertificate(payload);
        if (TextUtils.isEmpty(errorMessage)) {
            return;
        }
        new AlertDialog.Builder(getActivity())
                .setTitle("certificate import failed")
                .setMessage(errorMessage)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String licenceId = (String) v.getTag();
        if (licenceId == null)
            return;

        Intent intent = new Intent(getActivity(), CertificateDetailActivity.class);
        intent.putExtra("licenceId", licenceId);
        getActivity().startActivity(intent);
    }
}
