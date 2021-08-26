package com.virjar.ratel.runtime;

import android.os.Process;

import com.virjar.ratel.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class DeadLockKiller {
    static void killAppIfDeadLock() throws IOException {

        int pid = Process.myPid();

        File dir = RatelEnvironment.deadLoadKillerShellDir();

        File shellFile = new File(dir, RatelRuntime.processName.replaceAll(":", "_").replaceAll("\\.", "_") + ".sh");

        File flagFile = new File(dir, "kill_flag_" + pid);
        flagFile.deleteOnExit();

        String scriptContent = "sleep 30s\n" +
                "if [  -f \"" + flagFile.getAbsolutePath() + "\" ] ;then\n" +
                "    echo \"process ANR ,kill process\"\n" +
                "    kill -9 " + pid + "\n" +
                "fi";

        FileUtils.writeToFile(scriptContent.getBytes(StandardCharsets.UTF_8), shellFile);
        shellFile.setExecutable(true);

        Runtime.getRuntime().exec("sh " + shellFile.getAbsolutePath());

        new Thread("dead-lock-watcher") {
            @Override
            public void run() {

                try {
                    Thread.sleep(15 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                if (flagFile.exists()) {
                    flagFile.delete();
                }
            }
        }.start();
    }
}

/*

sleep 20s
if [  -f "path to flag file" ] ;then
    echo "process ANR ,kill process"
    kill -9 pid
fi
 **/