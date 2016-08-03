/*
 * Copyright 2016 Maurice Kenmeue Fonwe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dynamite.zafroshops.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.adapter.ZopServiceAdapter;
import dynamite.zafroshops.app.data.ZopServiceType;

public class ZopServicesFragment extends android.support.v4.app.DialogFragment {
    public static final String NEW_ZOP_SERVICES = "new_zop_services";
    public static final int NEW_ZOP_SERVICES_RESULT_CODE = 1;
    public static final String NEW_ZOP_SERVICES_COUNT_KEY = "new_zop_services_count";

    public static ArrayList<ZopServiceType> data;
    public static ArrayList<ZopServiceType> tempData;
    ZopServiceAdapter zopServiceAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        data = (ArrayList<ZopServiceType>) getArguments().get(NEW_ZOP_SERVICES);
        tempData = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            tempData.add(data.get(i));
        }

        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.services_dialog, null);

        builder.setView(view).setTitle(R.string.services_editing)
                .setPositiveButton(R.string.button_submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        data.clear();
                        for (int i = 0; i < tempData.size(); i++) {
                            data.add(tempData.get(i));
                        }

                        Intent intent = new Intent();
                        intent.putExtra(NEW_ZOP_SERVICES_COUNT_KEY, data.size());
                        getTargetFragment().onActivityResult(getTargetRequestCode(), NEW_ZOP_SERVICES_RESULT_CODE, intent);
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        ListView listView = (ListView) view.findViewById(R.id.servicesContainer);

        zopServiceAdapter = new ZopServiceAdapter(activity, R.id.serviceItem, tempData);
        listView.setAdapter(zopServiceAdapter);

        return builder.create();
    }
}



