package ru.greatstep.exceltosqlconverter.utils;

import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static class HeaderColumnConstants {

        public static final String NUMBER = "[number";
        public static final String DATE = "[date";
        public static final String TIMESTAMP = "[timestamp";
        public static final String ENUM = "[enum:";
        public static final String FKEY = "[fkey:";
        public static final String TABLE_NAME = "table_name=";
        public static final String IS_NUMBER = "is_number=";
        public static final String REFERENCE_COLUMN = "reference_column=";
        public static final String SEARCH_COLUMN = "search_column=";
        public static final String SEARCH_IS_NUMBER = "search_is_number=";
        public static final String OUT_BRACKET = "]";
        public static final String SEMICOLON = ";";

    }

    public static class SpecialValues {

        public static final String CURRENT_DATE = "current_date";
        public static final String CURRENT_TIMESTAMP = "current_timestamp";
        public static final String RANDOM_FULL_NAME = "random_full_name";
        public static final String RANDOM_FIRST_NAME = "random_first_name";
        public static final String RANDOM_MIDDLE_NAME = "random_middle_name";
        public static final String RANDOM_LAST_NAME = "random_last_name";
        public static final String RANDOM_PHONE = "random_phone";
        public static final String RANDOM_LOGIN = "random_login";
        public static final String RANDOM_EMAIL = "random_email";
        public static final String RANDOM_DATE_OF_BIRTH = "random_date_of_birth";
        public static final String RANDOM_PASSPORT = "random_passport";
        public static final String RANDOM_PASSPORT_NUMBER = "random_passport_number";
        public static final String RANDOM_PASSPORT_SERIAL = "random_passport_serial";
        public static final String RANDOM_FULL_ADDRESS = "random_full_address";
        public static final String RANDOM_COUNTRY = "random_country";
        public static final String RANDOM_REGION = "random_region";
        public static final String RANDOM_CITY = "random_city";
        public static final String RANDOM_STREET = "random_street";
        public static final String RANDOM_HOUSE = "random_house";
        public static final String RANDOM_APARTMENT = "random_apartment";

        public List<String> getAll() {
            return List.of(CURRENT_DATE, CURRENT_TIMESTAMP, RANDOM_FULL_NAME, RANDOM_FIRST_NAME, RANDOM_MIDDLE_NAME,
                    RANDOM_LAST_NAME, RANDOM_PHONE, RANDOM_LOGIN, RANDOM_EMAIL, RANDOM_DATE_OF_BIRTH, RANDOM_PASSPORT,
                    RANDOM_PASSPORT_NUMBER, RANDOM_PASSPORT_SERIAL, RANDOM_FULL_ADDRESS, RANDOM_COUNTRY, RANDOM_REGION,
                    RANDOM_CITY, RANDOM_STREET, RANDOM_HOUSE, RANDOM_APARTMENT);
        }

    }

}
