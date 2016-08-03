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

package dynamite.zafroshops.app.adapter;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import dynamite.zafroshops.app.MainActivity;
import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.MobileZop;

public class TypedZopListViewAdapter extends ArrayAdapter<MobileZop> {

    private ArrayList<MobileZop> objects;
    private static Random rng;
    private Ordering<MobileZop> ordering;

    public TypedZopListViewAdapter(Context context, int resource, ArrayList<MobileZop> objects) {
        super(context, resource, objects);
        this.setObjects(objects);
        rng = (rng == null) ? new Random() : rng;

        ordering = Ordering.from(new Comparator<MobileZop>() {
            @Override
            public int compare(MobileZop zop, MobileZop z1) {
                if (zop.Distance < z1.Distance) {
                    return -1;
                } else if (zop.Distance > z1.Distance) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        Collections.sort(objects, ordering);
    }

    class ViewHolder {
        ImageView icon;
        TextView distance;
        ImageView star;
        TextView title;
        TextView confirmation;
        TextView street;
        TextView city;
        TextView country;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public MobileZop getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Context context = this.getContext();

        if (convertView == null) {
            LayoutInflater la = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = la.inflate(R.layout.listview_item, null);

            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.imageViewList);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.distanceList);
            viewHolder.star = (ImageView) convertView.findViewById(R.id.starList);
            viewHolder.title = (TextView) convertView.findViewById(R.id.titleList);
            viewHolder.confirmation = (TextView) convertView.findViewById(R.id.confirmationList);
            viewHolder.street = (TextView) convertView.findViewById(R.id.streetList);
            viewHolder.city = (TextView) convertView.findViewById(R.id.cityList);
            viewHolder.country = (TextView) convertView.findViewById(R.id.countryList);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if (objects != null) {
            MobileZop item = this.objects.get(position);

            if (item.Distance  > 0) {
                viewHolder.distance.setText(String.format("%.0f %s",  item.Distance, context.getString(R.string.km)));
            }
            else {
                viewHolder.distance.setText("");
            }

            if(item.Rating > 0){
                viewHolder.star.setImageResource(R.drawable.star);
                viewHolder.confirmation.setText(Integer.toString(item.Rating));
            }
            viewHolder.title.setText(item.Name);
            viewHolder.street.setText(item.Street + " " + (item.StreetNumber == null ? "" : item.StreetNumber));
            viewHolder.city.setText(item.City);
            viewHolder.country.setText(item.CountryName);

            try {
                int test = (rng.nextInt(7) + 1);
                viewHolder.icon.setImageResource(R.drawable.class.getField("i" + (rng.nextInt(7) + 1)).getInt(null));
            } catch (IllegalAccessException ignored) {

            } catch (NoSuchFieldException e) {
                viewHolder.icon.setImageResource(R.drawable.i2);
            }
        }

        return convertView;
    }

    public void setObjects(ArrayList<MobileZop> objects) {
        if (this.objects == null) {
            this.objects = new ArrayList<>();
        }
        else {
            this.objects.clear();
        }
        if (objects != null) {
            this.objects.addAll(objects);
        }

        if (MainActivity.LastLocation != null) {
            for (int i = 0; i < this.objects.size(); i++) {
                float[] results = new float[1];
                Location.distanceBetween(MainActivity.LastLocation.Latitude, MainActivity.LastLocation.Longitude,
                        this.objects.get(i).Location.Latitude, this.objects.get(i).Location.Longitude, results);
                this.objects.get(i).Distance = results[0] / 1000;
            }
            Collections.sort(objects, ordering);
        }
    }
}