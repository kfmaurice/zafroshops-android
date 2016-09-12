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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;

import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.MobileOpeningHour;
import dynamite.zafroshops.app.data.MobileOpeningHourData;
import dynamite.zafroshops.app.data.StorageKeys;

public class HelpDialogFragment extends DialogFragment {
    public static final String NEW_ZOP_OPENINGS = "new_zop_openings";
    public static final int NEW_ZOP_OPENINGS_RESULT_CODE = 0;

    private TextView openingHoursLabel;
    private LinearLayout openingHoursContainer;
    private Button openingHoursDeleteButton;

    private ArrayList<MobileOpeningHourData> data;
    private ArrayList<MobileOpeningHourData> tempData;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.help_dialog, null);
        SharedPreferences preferences = activity.getPreferences(0);
        final SharedPreferences.Editor editor = preferences.edit();

        builder.setView(view).setTitle(R.string.help_title)
        .setPositiveButton(R.string.help_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        Switch locationToggle = (Switch) view.findViewById(R.id.help_toggle);
        locationToggle.setChecked(preferences.getBoolean(StorageKeys.HELP_TOGGLE_KEY, false));
        locationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(StorageKeys.HELP_TOGGLE_KEY, b);
                editor.commit();
            }
        });

        return builder.create();
    }
}
