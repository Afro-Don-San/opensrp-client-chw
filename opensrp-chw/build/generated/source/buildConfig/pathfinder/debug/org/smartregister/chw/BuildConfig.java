/**
 * Automatically generated file. DO NOT MODIFY
 */
package org.smartregister.chw;

public final class BuildConfig {
  public static final boolean DEBUG = Boolean.parseBoolean("true");
  public static final String APPLICATION_ID = "org.smartregister.chw.pf";
  public static final String BUILD_TYPE = "debug";
  public static final String FLAVOR = "pathfinder";
  public static final int VERSION_CODE = 2;
  public static final String VERSION_NAME = "1.0.2";
  // Fields from build type: debug
  public static final int BASE_PNC_CLOSE_MINUTES = 1440;
  public static final int DATA_SYNC_DURATION_MINUTES = 15;
  public static final int HOME_VISIT_MINUTES = 60;
  public static final int IMAGE_UPLOAD_MINUTES = 15;
  public static final int OPENMRS_UNIQUE_ID_BATCH_SIZE = 15;
  public static final int OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE = 30;
  public static final int OPENMRS_UNIQUE_ID_SOURCE = 2;
  public static final int PULL_UNIQUE_IDS_MINUTES = 15;
  public static final int REPORT_INDICATOR_GENERATION_MINUTES = 120;
  public static final int SCHEDULE_SERVICE_MINUTES = 360;
  public static final int STOCK_USAGE_REPORT_MINUTES = 1440;
  public static final boolean TIME_CHECK = false;
  public static final boolean USE_PATHFINDERS_FP_MODULE = true;
  public static final boolean USE_UNIFIED_REFERRAL_APPROACH = true;
  public static final int VACCINE_SYNC_PROCESSING_MINUTES = 30;
  // Fields from product flavor: pathfinder
  public static final String[] ALLOWED_LOCATION_LEVELS = {"Ward" , "Health Facility", "Village", "Village Sublocations"};
  public static final String[] ALLOWED_LOCATION_LEVELS_DEBUG = {"Health Facility" , "Village"};
  public static final int DATABASE_VERSION = 16;
  public static final String DEFAULT_LOCATION = "Village Sublocations";
  public static final String DEFAULT_LOCATION_DEBUG = "Village";
  public static final String[] LOCATION_HIERACHY = {"Country","Zone","Region","Council","Ward" , "Health Facility", "Village", "Village Sublocations"};
  public static final String guidebooks_url = "https://opensrp.s3.amazonaws.com/media/ba/";
  public static final String opensrp_url = "https://boresha-afya.smartregister.org/opensrp/";
  public static final String opensrp_url_debug = "http://172.105.87.198:8082/opensrp/";
  // Fields from default config.
  public static final long BUILD_TIMESTAMP = 1592250977443L;
  public static final boolean IS_SYNC_SETTINGS = false;
  public static final long MAX_SERVER_TIME_DIFFERENCE = 1800000l;
  public static final int MAX_SYNC_RETRIES = 3;
  public static final String SYNC_TYPE = "teamId";
}
