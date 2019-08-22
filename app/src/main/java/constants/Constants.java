package constants;

import applications.Meili;
import id.ac.stis.meili.R;

public class Constants {
    public static final String locationTable = "Location_table";
    public static final String adminTable = "Admin_table";
    public static final String annotationTable = "Annotation_table";
    public static final String simpleLocationTable = "Simple_Location_table";
    public static final String accelerometerTable = "Accelerometer_table";
    public static final String databaseName = "Mobility_collector";
    public static final String servletName = "/ConnectToDatabase";

    public static String serviceColumnName = "isServiceOn";
    public static String userIdColumnName = "userId";
    public static String urlColumnName = "urlCurrent";
    public static String speedThresholdColumnName = "speedTresh";

    public static String titleText = Meili.getInstance().getString(R.string.app_name);
    public static String regularEmailText = Meili.getInstance().getString(R.string.regularEmailText);
    public static String regularUsernameText = Meili.getInstance().getString(R.string.regularUsernameText);
    public static String regularPasswordText = Meili.getInstance().getString(R.string.regularPasswordText);
    public static String registerNewUserText = Meili.getInstance().getString(R.string.registerNewUserText);

    /*
     * General
     */
    public static String loginText = Meili.getInstance().getString(R.string.loginText);
    public static String registerAndLoginButton = Meili.getInstance().getString(R.string.registerAndLoginButton);
    public static String titleControlCenter = Meili.getInstance().getString(R.string.titleControlCenter);
    /*
     * For the About Section
     */
    public static String subTitleText = Meili.getInstance().getString(R.string.subTitleText);
    public static String versionText = Meili.getInstance().getString(R.string.versionText);
    public static String contentText = Meili.getInstance().getString(R.string.contentText);
    public static String closeButtonText = Meili.getInstance().getString(R.string.closeButtonText);
    /*
     * For the Admin Section
     */
    public static String adminTitleText = Meili.getInstance().getString(R.string.adminTitleText);
    public static String sudoEmailText = Meili.getInstance().getString(R.string.sudoEmailText);
    public static String sudoPasswordText = Meili.getInstance().getString(R.string.sudoPasswordText);
    public static String sudoCredentialsWrongMessage = Meili.getInstance().getString(R.string.sudoCredentialsWrongMessage);
    /*
     * Menu
     */
    public static String menuAboutText = Meili.getInstance().getString(R.string.menuAboutText);
    public static String menuAdminText = Meili.getInstance().getString(R.string.menuAdminText);
    public static String menuStatusText = Meili.getInstance().getString(R.string.menuStatusText);
    public static String menuUploadText = Meili.getInstance().getString(R.string.menuUploadText);
    /*
     * Dialogs
     */
    public static String yesText = Meili.getInstance().getString(R.string.yesText);
    public static String noText = Meili.getInstance().getString(R.string.noText);
    public static String enableGPSBody = Meili.getInstance().getString(R.string.enableGPSBody);
    public static String enableGPSTitle = Meili.getInstance().getString(R.string.enableGPSTitle);
    public static String serviceDisableBody = Meili.getInstance().getString(R.string.serviceDisableBody);
    public static String serviceDisableTitle = Meili.getInstance().getString(R.string.serviceDisableTitle);
    public static String stopCollectionText = Meili.getInstance().getString(R.string.stopCollectionText);
    public static String startCollectionText = Meili.getInstance().getString(R.string.startCollectionText);
    /*
     * Notifications
     */
    public static String notificationTitle = Meili.getInstance().getString(R.string.notificationTitle);
    public static String notificationActionText = Meili.getInstance().getString(R.string.notificationActionText);
    public static String confirmUpload = Meili.getInstance().getString(R.string.confirmUpload);
    public static String infirmUpload = Meili.getInstance().getString(R.string.infirmUpload);
    /*
     * Toast warnings
     */
    public static String deactivateGPSWarning = Meili.getInstance().getString(R.string.deactivateGPSWarning);
    public static String internetConnectionWarning = Meili.getInstance().getString(R.string.internetConnectionWarning);
    public static String credentialsMissmatchWarning = Meili.getInstance().getString(R.string.credentialsMissmatchWarning);
    public static String passwordEmpty = Meili.getInstance().getString(R.string.passwordEmpty);
    public static String confirmPassword = Meili.getInstance().getString(R.string.confirmPassword);
    public static String passwordError = Meili.getInstance().getString(R.string.passwordError); // register only
    public static String emailWarning = Meili.getInstance().getString(R.string.emailWarning);
    public static String regNoWarning = Meili.getInstance().getString(R.string.regNoWarning);
    public static String phoneNumberWarning = Meili.getInstance().getString(R.string.phoneNumberWarning);
    public static String noUsernameWarning = Meili.getInstance().getString(R.string.noUsernameWarning);
    public static String phoneNumber= Meili.getInstance().getString(R.string.phoneNumber);
    public static String regNumber= Meili.getInstance().getString(R.string.regNumber);

}
