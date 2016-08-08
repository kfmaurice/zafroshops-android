package dynamite.zafroshops.app.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class FullMobileZop extends MobileZop {

    @SerializedName("openingHours")
    public ArrayList<MobileOpeningHour> OpeningHours;
    @SerializedName("services")
    public ArrayList<MobileZopService> Services;

    @SerializedName("confirmationBeenCount")
    public int ConfirmationBeenCount;
    @SerializedName("confirmationKnowCount")
    public int ConfirmationKnowCount;
    @SerializedName("confirmationDontCount")
    public int ConfirmationDontCount;

    public FullMobileZop() {

    }

    public  FullMobileZop(MobileZop zop) {
        super.id = zop.id;
        super.Name = zop.Name;
        super.Location = zop.Location;
        super.CountryID = zop.CountryID;
        super.City = zop.City;
        super.Street = zop.Street;
        super.StreetNumber = zop.StreetNumber;
        super.PhoneNumber = zop.PhoneNumber;
        super.Details = zop.Details;
        super.Type = zop.Type;
        super.Origin = zop.Origin;
        super.CountryName = zop.CountryName;
        super.CountryPhoneCode = zop.CountryPhoneCode;
        super.Rating = zop.Rating;
        super.Count = zop.Count;
        super.Distance = zop.Distance;
        super.DataVersion = zop.DataVersion;
    }

    public ArrayList<MobileOpeningHourData> getGroupedOpeningHours()
    {
        ArrayList<MobileOpeningHourData> groupedOpeningHours = new ArrayList<>();

        ArrayList<MobileOpeningHourData> temp = new ArrayList<MobileOpeningHourData>() {{
            add(new MobileOpeningHourData((byte)0));
            add(new MobileOpeningHourData((byte)1));
            add(new MobileOpeningHourData((byte)2));
            add(new MobileOpeningHourData((byte)3));
            add(new MobileOpeningHourData((byte)4));
            add(new MobileOpeningHourData((byte)5));
            add(new MobileOpeningHourData((byte)6));
        }};

        if (OpeningHours != null) {
            for (MobileOpeningHour moh : OpeningHours) {
                temp.get(moh.Day).Hours.add(moh);
            }
        }

        for(int i = 0; i < temp.size(); i++)
        {
            Collections.sort(temp.get(i).Hours);
        }

        for (MobileOpeningHourData mohd : temp) {
            if (mohd.Hours.size() > 0) {
                groupedOpeningHours.add(mohd);
            }
        }
        return groupedOpeningHours;
    }
}