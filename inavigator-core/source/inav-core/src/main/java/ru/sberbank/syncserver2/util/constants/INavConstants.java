package ru.sberbank.syncserver2.util.constants;

public class INavConstants {
    public static final String RU_SBERBANK_SYNCSERVER2_SERVICE_CONFIG_CONFIGSERVICE_CHECK_APP_VERSION = "((select app_version from CONFSERVER_VERSIONS where app_id in (select app_id from CONFSERVER_APPS where app_bundle=?) and app_version =? union all "
            + "select (select top 1 app_version from CONFSERVER_VERSIONS where app_id in (select app_id from CONFSERVER_APPS where app_bundle=?) order by app_version desc)))";
    public static final String RU_SBERBANK_SYNCSERVER2_SERVICE_CONFIG_CONFIGSERVICE_GET_PROPERTIES = "select property_code property, property_value value\n" +
            "  from CONFSERVER_PROPERTY_VALUES\n" +
            "  where app_id in (select app_id from CONFSERVER_APPS where app_bundle=?)\n" +
            "    and app_version=?\n";
    public static final String RU_SBERBANK_SYNCSERVER2_SERVICE_SQL_SQLTEMPLATELOADER_LOADALL1 = "SELECT TEMPLATE_CODE, TEMPLATE_SQL FROM SQL_TEMPLATES";
    public static final String RU_SBERBANK_SYNCSERVER2_SERVICE_SQL_SQLTEMPLATELOADER_LOADALL2 = "SELECT SUBST_CODE, SUBST_VALUE FROM SUBST_DICT";
    public static final String RU_SBERBANK_SYNCSERVER2_SERVICE_GENERATOR_CLUSTERMANAGER_DORUN = "exec SP_IS_HOST_ACTIVE ?";

}
