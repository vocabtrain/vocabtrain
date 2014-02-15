package org.devwork.vocabtrain;

import java.text.SimpleDateFormat;

import android.content.pm.ActivityInfo;

public interface Constants
{
	public static final String PACKAGE_NAME = "org.devwork.vocabtrain";

	public static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";

	public static final String DATABASE_NAME = "books.db";
	public static final String DATABASE_PATH = "/data/data/org.devwork.vocabtrain/databases/" + DATABASE_NAME;
	public static final int DATABASE_VERSION = 3;
	// public static final String TAG = "Database";

	public static final int DEFAULT_PREFERRED_FONTSIZE = 20;
	public static final int DEFAULT_MINIMUM_FONTSIZE = 5;

	public static final String SERVER_QUERY_VERSION = "http://devwork.org/vocabtrain/sync.php?q";
	public static final String SERVER_DOWNLOAD = "http://devwork.org/vocabtrain/sync.php";

	// public static final int SSRF_SESSION_LOOKUPS = 7;

	public static final int REQUEST_VOICE_REC_FOR_TEXTCARD = 1234;
	public static final int REQUEST_CARD_EDIT = 1235;
	public static final int REQUEST_TTS = 1236;
	public final static int REQUEST_FILEMANAGER_FOR_PORTING = 1237;
	public final static int REQUEST_LICENSE_ACCEPTING = 1238;
	public static final int REQUEST_CARD_VIEW = 1239;
	public static final int REQUEST_FILEMANAGER_FOR_TYPEFACE = 1240;
	public static final int REQUEST_FILEMANAGER_FOR_DATABASE = 1241;
	public static final int REQUEST_FILEMANAGER_FOR_SKIN = 1242;
	public static final int REQUEST_ENABLE_BLUETOOTH = 1243;

	public static final String BLUETOOTH_UID = "8dbb2d70-80f6-11e1-b0c4-0800200c9a66";
	public static final String BLUETOOTH_CAPTION = "Vocabtrain";

	public final static String ACCOUNT_TYPE = "org.devwork.vocabtrain.account";
	public static final String AUTHTOKEN_TYPE = ACCOUNT_TYPE;

	public final static String ACCOUNT_INTENT = "org.devwork.vocabtrain.ACCOUNT";

	public final static int ORIENTATIONS[] = { ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
			ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT };

	//public final static String SERVER_ROOT = "http://www.devwork.org/vocabtrain/";
	//public final static String SERVER_ROOT = "http://web403.webbox555.server-home.org/drake/vocabtrain/";
	//public final static String SERVER_ROOT = "http://ursaminor.informatik.uni-augsburg.de/koeppldo/vocabtrain/";
	public final static String SERVER_ROOT = "http://137.250.169.80:4000/vocabtrain/mobile/";
	//public final static String SERVER_ROOT = "http://ursamajor.informatik.uni-augsburg.de:2235/vocabtrain/mobile/";
	//public final static String SERVER_ROOT = "http://fb-sagittarius.informatik.uni-augsburg.de/vocabtrain/mobile/";
	// public final static String SERVER_ROOT = "http://192.168.178.10/ludwig/vocabtrain/";
	public final static String SERVER_QUERY = SERVER_ROOT + "books";
	public final static String SERVER_AUTH = SERVER_ROOT + "auth";
	public static final String SERVER_CHANGES_UPLOAD = SERVER_ROOT + "delta";
	public static final String SERVER_FILING_UPLOAD = SERVER_ROOT + "filing/upload";
	public static final String SERVER_FILING_DOWNLOAD = SERVER_ROOT + "filing/download";
	public static final String SERVER_BOOKS_DOWNLOAD = SERVER_ROOT + "download";
	public static final String SERVER_VEECHECK = SERVER_ROOT + "veecheck.xml";

	public final static SimpleDateFormat jsonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	public final static SimpleDateFormat sqliteDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
}
