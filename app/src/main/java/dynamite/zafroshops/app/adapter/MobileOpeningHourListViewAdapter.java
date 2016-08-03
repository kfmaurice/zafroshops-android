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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.MobileOpeningHour;
import dynamite.zafroshops.app.data.MobileOpeningHourData;

public class MobileOpeningHourListViewAdapter extends ArrayAdapter<MobileOpeningHourData> {
    private ArrayList<MobileOpeningHourData> objects;

    public MobileOpeningHourListViewAdapter(Context context, int resource, ArrayList<MobileOpeningHourData> objects) {
        super(context, resource, objects);
        this.setObjects(objects);
    }

    class ViewHolder {
        TextView day;
        TextView hour;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public MobileOpeningHourData getItem(int position) {
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
            convertView = la.inflate(R.layout.opening_hour_item, null);

            viewHolder = new ViewHolder();
            viewHolder.day = (TextView) convertView.findViewById(R.id.openingHoursDay);
            viewHolder.hour = (TextView) convertView.findViewById(R.id.openingHoursHour);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if (objects != null) {
            MobileOpeningHourData item = this.objects.get(position);

            try {
                viewHolder.day.setText(R.string.class.getField("day" + item.Day).getInt(null));
            } catch (IllegalAccessException ignored) {

            } catch (NoSuchFieldException e) {
                viewHolder.day.setText("");
            }

            String hour = "";
            for (MobileOpeningHour moh : item.Hours) {
                hour += ((hour.length() > 0) ? ", " : "") + moh.toString();
            }
            viewHolder.hour.setText(hour);
        }

        return convertView;
    }

    public void setObjects(ArrayList<MobileOpeningHourData> objects) {
        if (this.objects == null) {
            this.objects = new ArrayList<>();
        }
        else {
            this.objects.clear();
        }
        if (objects != null) {
            this.objects.addAll(objects);
        }
    }
}
