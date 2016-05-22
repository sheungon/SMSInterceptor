package com.sheungon.smsinterceptor.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A class to run log cat so record current app's log
 * @author John
 */
public class LogcatUtil {

    public static final String LOGCAT_FILE_SIZE = "256"; // KB
    public static final String LOGCAT_FORMAT = "threadtime";
    public static final String LOGCAT_MAX_NO_OF_LOG_FILES = "1";

    private static final String APP_LINUX_USER_NAME = "AppLinuxUserName";

    private static final String REGEX_SPACE_GAP = "\\s+";

    private static final String PS_COL_USER = "USER";
    private static final String PS_COL_PID = "PID";
    private static final String PS_COL_NAME = "NAME";


    /**
     * Start a logcat process and log the log to {@code logFile}.
     * Only one concurrent logcat process will be created even call this method multiple times.
     * @param logFile The designation of the log file.
     *
     * @return {@code true} if a logcat process created successfully or a logcat process already running before.
     * */
    public static synchronized boolean startLogcatAt(@NonNull Context context, @NonNull File logFile) {

        String username = getAppRunByUser(context);
        if (username == null) {
            return false;
        }

        if (isLogcatRunningBy(username)) {
            return true;
        }

        // Run logcat here
        ProcessBuilder processBuilder = new ProcessBuilder("logcat",
                "-f", logFile.getAbsolutePath(),
                "-r", LOGCAT_FILE_SIZE,
                "-n", LOGCAT_MAX_NO_OF_LOG_FILES,
                "-v", LOGCAT_FORMAT);
        processBuilder.redirectErrorStream(true);
        try {
            processBuilder.start();
            Log.v("Started logcat");
            return true;
        } catch (Exception e) {
            Log.e("Error on starting logcat", e);
        }

        return false;
    }

    /**
     * Stop any running logcat instance
     * @return {@code true} if a logcat instance is stopped by this.
     * */
    public static boolean stopLogcat(@NonNull Context context) {

        String username = getAppRunByUser(context);
        if (username == null) {
            return false;
        }

        String pid = getLogcatPIDRunningBy(username);
        ProcessBuilder processBuilder = new ProcessBuilder("kill", pid);
        try {
            Process process = processBuilder.start();
            int exitCode = process.exitValue();
            Log.v("Stopped logcat exit code : " + exitCode);
            return true;
        } catch (Exception e) {
            Log.e("Error on kill logcat", e);
        }

        return false;
    }

    /**
     * @return The app executed by which Linux user.
     * */
    @Nullable
    private static String getAppRunByUser(@NonNull Context context) {

        String myUserName = PrivatePrefUtil.getString(APP_LINUX_USER_NAME);

        if (TextUtils.isEmpty(myUserName)) {
            String packageName = context.getPackageName();
            Log.d("Retrieving application username. ApplicationPackage = " + packageName);

            /*Don't user `grep` as it could be not available on some devices.*/
            // Execute `ps`
            ProcessBuilder psBuilder = new ProcessBuilder("ps");
            Process ps;
            try {
                ps = psBuilder.start();
            } catch (IOException e) {
                Log.e("Not able to run command on this device!!!", e);
                return null;
            }

            // Read the output
            InputStream is = ps.getInputStream();
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            try {

                Log.d("======`ps` output start======");

                // Read the first line and find the target column
                String line = bf.readLine();
                if (line == null) {
                    Log.e("'ps' no output?!");
                    return null;
                }
                Log.d(line);

                // Split by space
                String[] columns = line.split(REGEX_SPACE_GAP);
                int nameColumn = 0;
                for (; ; nameColumn++) {
                    if (nameColumn >= columns.length) {
                        Log.e("Cannot find column `NAME` in ps response");
                        return null;
                    }
                    if (PS_COL_NAME.equalsIgnoreCase(columns[nameColumn])) {
                        break;
                    }
                }

                while ((line = bf.readLine()) != null) {
                    Log.d(line);
                    // Split by space
                    columns = line.split(REGEX_SPACE_GAP);

                    if (packageName.equals(columns[nameColumn])) {
                        myUserName = columns[nameColumn];
                        Log.d("Application username = " + myUserName);
                        break;
                    }
                }
                Log.d("======`ps` output end======");

                if (TextUtils.isEmpty(myUserName)) {
                    Log.e("Cannot find the owner of current app...");
                } else {
                    // Cache the user name in preference as it remind the same since installed
                    PrivatePrefUtil.set(APP_LINUX_USER_NAME, myUserName);
                }
            } catch (IOException e) {
                Log.e("Error on reading output from 'ps'", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // Don't care
                }
                try {
                    ps.destroy();
                } catch (Exception e) {
                    Log.e("Error on destroy ps", e);
                }
            }
        }

        return myUserName;
    }

    @Nullable
    private static String getLogcatPIDRunningBy(@NonNull String user) {

        /*Don't user `grep` as it could be not available on some devices.*/
        // Execute `ps logcat` to find all logcat process
        ProcessBuilder processBuilder = new ProcessBuilder("ps", "logcat");
        Process ps;
        try {
            ps = processBuilder.start();
        } catch (IOException e) {
            Log.e("Not able to run command on this device!!!", e);
            return null;
        }

        // Read the output
        InputStream is = ps.getInputStream();
        BufferedReader bf = new BufferedReader(new InputStreamReader(is));
        try {

            Log.d("======`ps logcat` output start======");

            // Read the first line and find the target column
            String line = bf.readLine();
            if (line == null) {
                Log.e("'ps' no output?!");
                return null;
            }
            Log.d(line);

            // Split by space
            String[] columns = line.split(REGEX_SPACE_GAP);
            int userColumn = -1;
            int pidColumn = -1;
            for (int i = 0; i < columns.length; i++) {
                if (PS_COL_USER.equalsIgnoreCase(columns[i])) {
                    userColumn = i;
                } else if (PS_COL_PID.equalsIgnoreCase(columns[i])) {
                    pidColumn = i;
                }
            }
            if (userColumn == -1 ||
                    pidColumn == -1) {
                Log.e("Some column cannot be found from output.");
                return null;
            }

            while ((line = bf.readLine()) != null) {
                Log.d(line);
                // Split by space
                columns = line.split(REGEX_SPACE_GAP);

                if (user.equals(columns[userColumn])) {
                    // Found the current user's process
                    String pid = columns[pidColumn];
                    Log.v("Logcat is already running by user [" + user + "] pid : " + pid);
                    return pid;
                }
            }
            Log.d("======`ps logcat` output end======");
        } catch (IOException e) {
            Log.e("Error on reading output from 'ps'", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Don't care
            }
            try {
                ps.destroy();
            } catch (Exception e) {
                Log.e("Error on destroy ps", e);
            }
        }

        return null;
    }

    private static boolean isLogcatRunningBy(@NonNull String user) {

        return getLogcatPIDRunningBy(user) != null;
    }
}
