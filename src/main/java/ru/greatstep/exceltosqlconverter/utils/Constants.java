package ru.greatstep.exceltosqlconverter.utils;

import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String ERROR_DELIMITER = " and ";

    public static class HeaderColumnConstants {

        public static final String NUMBER = "[number]";
        public static final String DATE = "[date]";
        public static final String TIMESTAMP = "[timestamp]";
        public static final String ENUM = "[enum:";
        public static final String FKEY = "[fkey:";
        public static final String TYPE_NAME = "type_name=";
        public static final String IS_NUMBER = "is_number";
        public static final String REFERENCE_COLUMN = "reference_column=";
        public static final String SEARCH_COLUMN = "search_column=";
        public static final String SEARCH_IS_NUMBER = "search_is_number";
        public static final String OUT_BRACKET = "]";
        public static final String IN_BRACKET = "[";
        public static final String SEMICOLON = ";";
        public static final String POINT = ".";

    }

    public static class PostgresFunc {

        public static final String CURRENT_DATE = "current_date";
        public static final String CURRENT_TIMESTAMP = "current_timestamp";

        public static List<String> getAll() {
            return List.of(
                    CURRENT_DATE,
                    CURRENT_TIMESTAMP
            );
        }

    }

    public static class SqlPatterns {

        public static final String DO_END_TEMPLATE = """
                        DO
                        $$
                            BEGIN
                            %s
                            END
                        $$
                """;
        public static final String DO_DECLARE_END_TEMPLATE = """
                        DO
                        $$
                            DECLARE
                            %s
                            BEGIN
                            %s
                            END
                        $$
                """;
        public static final String CREATE_VARIABLE_PATTERN = "%s bigint = %s;";
        public static final String INSERT_PATTERN = "INSERT INTO %s ( %s )\nVALUES\n";
        public static final String VALUE_PATTERN = "(%s),\n";
        public static final String SUB_SELECT_TEMPLATE = "(SELECT %s FROM %s WHERE %s = '%s')";
        public static final String SUB_SELECT_NUMBER_TEMPLATE = "(SELECT %s FROM %s WHERE %s = %s)";

    }

    public static class SpecialValues {

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

        public static List<String> getAll() {
            return List.of(
                    RANDOM_FULL_NAME,
                    RANDOM_FIRST_NAME,
                    RANDOM_MIDDLE_NAME,
                    RANDOM_LAST_NAME
            );
        }

    }

}
