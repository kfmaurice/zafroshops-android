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

package dynamite.zafroshops.app.data;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;

public class MobileOpeningHour implements Comparable<MobileOpeningHour>
{
    @SerializedName("zopId")
    public int ZopID;

    @SerializedName("day")
    public byte Day;

    @SerializedName("startTimeHour")
    public Byte StartTimeHour;
    @SerializedName("startTimeMinute")
    public byte StartTimeMinute;
    @SerializedName("endTimeHour")
    public Byte EndTimeHour;
    @SerializedName("endTimeMinute")
    public byte EndTimeMinute;


    @Override
    public int compareTo(@NonNull MobileOpeningHour another) {
        if(Day < another.Day) {
            return -1;
        }
        if(Day > another.Day) {
            return 1;
        }
        if (StartTimeHour < another.StartTimeHour) {
            return -1;
        }
        if (StartTimeHour > another.StartTimeHour) {
            return 1;
        }
        return 0;
    }

    public MobileOpeningHour Copy() {
        MobileOpeningHour copy = new MobileOpeningHour();

        copy.Day = Day;
        copy.StartTimeHour = StartTimeHour;
        copy.StartTimeMinute = StartTimeMinute;
        copy.EndTimeHour = EndTimeHour;
        copy.EndTimeMinute = EndTimeMinute;

        return copy;
    }

    @Override
    public String toString() {
        if (StartTimeHour != null && EndTimeHour != null) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            return String.format("%02d:%02d - %02d:%02d", StartTimeHour, StartTimeMinute, EndTimeHour, EndTimeMinute);
        }
        return "";
    }
}
