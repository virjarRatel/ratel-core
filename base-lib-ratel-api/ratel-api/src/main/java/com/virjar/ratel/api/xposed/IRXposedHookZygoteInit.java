package com.virjar.ratel.api.xposed;

public interface IRXposedHookZygoteInit {
    /**
     * Called very early during startup of Zygote.
     *
     * @param startupParam Details about the module itself and the started process.
     * @throws Throwable everything is caught, but will prevent further initialization of the module.
     */
    void initZygote(StartupParam startupParam) throws Throwable;

    /**
     * Data holder for {@link #initZygote}.
     */
     class StartupParam {
        public StartupParam() {
        }

        /**
         * The path to the module's APK.
         */
        public String modulePath;

        /**
         * Always {@code true} on 32-bit ROMs. On 64-bit, it's only {@code true} for the primary
         * process that starts the system_server.
         */
        public boolean startsSystemServer;
    }
}
