package com.virjar.ratel.envmock.idfake;

public class ImeiSignUpdater extends LuhnSignUpdater {

    public static ImeiSignUpdater imeiSignUpdaterInstance = new ImeiSignUpdater();

    @Override
    public String updateSign(String input) {
        if (input == null || input.length() < 15) {
            //imei 必须>=15
            return input;
        }
        if (input.length() == 15) {
            return super.updateSign(input);
        }
        String signArea = input.substring(0, 15);
        return super.updateSign(signArea) + input.substring(15);
    }
}
