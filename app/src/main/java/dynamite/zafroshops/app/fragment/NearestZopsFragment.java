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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import dynamite.zafroshops.app.MainActivity;
import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.adapter.TypedZopListViewAdapter;
import dynamite.zafroshops.app.data.MobileZop;
import dynamite.zafroshops.app.data.ZopType;

public class NearestZopsFragment extends Fragment {

    private static final String ARG_ZOP_COUNT = "zop_count";
    private int count;
    private boolean loading;
    private TypedZopListViewAdapter adapter;
    public ArrayList<MobileZop> nearestZops;

    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static NearestZopsFragment newInstance(int count) {
        NearestZopsFragment fragment = new NearestZopsFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_ZOP_COUNT, count);

        fragment.setArguments(args);
        return fragment;
    }

    public NearestZopsFragment() {
        nearestZops = new ArrayList<MobileZop>() { };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MainActivity activity = (MainActivity)getActivity();
        activity.restoreActionBar();

        // swipe to refresh
        swipeRefreshLayout = (SwipeRefreshLayout) activity.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                activity.refreshNearest();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        count = getArguments().getInt(ARG_ZOP_COUNT, 5);

        setZops();
    }

    private void setZops() {
        adapter = new TypedZopListViewAdapter(getActivity(), R.id.listViewItem, nearestZops);

        final ArrayList<Pair<String, String>> parameters;
        if (MainActivity.LastLocation != null) {
            parameters = new ArrayList<Pair<String, String>>() {{
                add(new Pair<>("latitude", Double.toString(MainActivity.LastLocation.Latitude)));
                add(new Pair<>("longitude", Double.toString(MainActivity.LastLocation.Longitude)));
                add(new Pair<>("$top", Integer.toString(count)));
            }};
        } else {
            parameters = new ArrayList<Pair<String, String>>() {{
                add(new Pair<>("$top", Integer.toString(count)));
            }};
        }

        loading = true;
        ListenableFuture<JsonElement> result = MainActivity.MobileClient.invokeApi("mobileZop", "GET", parameters);

        Futures.addCallback(result, new FutureCallback<JsonElement>() {
            Activity activity = getActivity();

            @Override
            public void onSuccess(JsonElement result) {
                JsonArray typesAsJson = result.getAsJsonArray();
                if (typesAsJson != null) {
                    nearestZops = new Gson().fromJson(result, new TypeToken<ArrayList<MobileZop>>() {
                    }.getType());
                }

                loading = false;
                if (adapter != null) {
                    adapter.setObjects(nearestZops);
                    resetVisibility(false);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                ListView zops = (ListView) activity.findViewById(R.id.listViewZops);
                RelativeLayout loader = (RelativeLayout) activity.findViewById(R.id.relativeLayoutLoader);
                LinearLayout noZops = (LinearLayout) activity.findViewById(R.id.noZops);

                resetVisibility(zops, noZops, loader, false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setObjects(nearestZops);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_typed_zops, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listViewZops);

        resetVisibility(listView, (LinearLayout) rootView.findViewById(R.id.noZops), (RelativeLayout) rootView.findViewById(R.id.relativeLayoutLoader), loading);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MobileZop item = (MobileZop)parent.getAdapter().getItem(position);
                MainActivity activity = (MainActivity)getActivity();

                if (activity.AdsActive && activity.Interstitial.isLoaded()) {
                    activity.Interstitial.show();
                }
                activity.setCurrentItem(item.id, item);
                activity.nextMenu(ZopItemFragment.newInstance(item.id), true, 100);
            }
        });

        listView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void resetVisibility(boolean showLoader) {
        Activity activity = getActivity();
        ListView zops = (ListView) activity.findViewById(R.id.listViewZops);
        LinearLayout noZops = (LinearLayout) activity.findViewById(R.id.noZops);
        RelativeLayout loader = (RelativeLayout) activity.findViewById(R.id.relativeLayoutLoader);

        resetVisibility(zops, noZops, loader, showLoader);
    }

    private void resetVisibility(ListView zops, LinearLayout noZops, RelativeLayout loader, boolean showLoader) {
        Activity activity = getActivity();

        if (nearestZops.size() == 0) {
            noZops.setVisibility(View.VISIBLE);
            zops.setVisibility(View.INVISIBLE);
        } else {
            noZops.setVisibility(View.INVISIBLE);
            zops.setVisibility(View.VISIBLE);
        }

        if(showLoader) {
            zops.setVisibility(View.INVISIBLE);
            noZops.setVisibility(View.INVISIBLE);
            loader.setVisibility(View.VISIBLE);
        }
        else {
            loader.setVisibility(View.INVISIBLE);
        }
    }
}
