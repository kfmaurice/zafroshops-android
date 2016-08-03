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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;

import java.util.Calendar;

import dynamite.zafroshops.app.MainActivity;
import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.Location;
import dynamite.zafroshops.app.data.MobileConfirmation;

public class ReviewDialogFragment extends DialogFragment {
    public static final String DIALOG_ZOP_ID= "dialog_confirmation_data";
    public static final String DIALOG_ZOP_LOCATION= "dialog_confirmation_location";

    public static int rating;
    private String zopID;
    private Location location;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        rating = 0;
        zopID = getArguments().getString(DIALOG_ZOP_ID);
        location = (Location)getArguments().getSerializable(DIALOG_ZOP_LOCATION);

        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.review_dialog, null);

        builder.setView(view)
                .setTitle(R.string.app_name)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MobileConfirmation confirmation = new MobileConfirmation();
                        Boolean confirmed = false;
                        int id = ((RadioGroup) view.findViewById(R.id.review_confirmation_group)).getCheckedRadioButtonId();

                        if (id == view.findViewById(R.id.review_confirmation_know).getId()) {
                            confirmed = true;
                        } else if (id == view.findViewById(R.id.review_confirmation_been).getId()) {
                            confirmed = false;
                        }

                        confirmation.ZopID = Integer.parseInt(zopID);
                        confirmation.Comments = ((EditText) view.findViewById(R.id.review_comments)).getText().toString();
                        confirmation.Confirmed = confirmed;
                        confirmation.ConfirmedBy = "Android";
                        confirmation.ConfirmedDate = Calendar.getInstance().getTime();
                        confirmation.Rating = rating;

                        // submit
                        ListenableFuture<JsonElement> result = MainActivity.MobileClient.invokeApi("mobileConfirmation", confirmation, JsonElement.class);
                        Futures.addCallback(result, new FutureCallback<JsonElement>() {
                            @Override
                            public void onSuccess(JsonElement result) {
                                ((MenuItem) view.findViewById(R.id.menu_zop_review)).setEnabled(false);
                            }

                            @Override
                            public void onFailure(@NonNull Throwable t) {

                            }
                        });
                    }
                })
        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.review_confirmation_group);

        view.findViewById(R.id.review_rating).setVisibility(View.INVISIBLE);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.review_confirmation_been) {
                    view.findViewById(R.id.review_rating).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.review_rating).setVisibility(View.INVISIBLE);
                }
            }
        });

        return builder.create();
    }
}
