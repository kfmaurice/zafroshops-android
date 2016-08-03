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

import java.util.ArrayList;

public class MobileOpeningHourData implements Comparable<MobileOpeningHourData> {
    public byte Day;
    public ArrayList<MobileOpeningHour> Hours;

    public MobileOpeningHourData() {
        this.Hours = new ArrayList<>();
    }

    public MobileOpeningHourData(byte day) {
        this();
        this.Day = day;
    }

    public MobileOpeningHourData Copy() {
        MobileOpeningHourData copy = new MobileOpeningHourData();

        copy.Day = Day;
        for (int i = 0; i < Hours.size(); i++) {
            copy.Hours.add(Hours.get(i).Copy());
        }

        return copy;
    }

    @Override
    public int compareTo(@NonNull MobileOpeningHourData another) {
        if(Day < another.Day) {
            return  -1;
        }
        if(Day > another.Day) {
            return  1;
        }
        return 0;
    }
}

