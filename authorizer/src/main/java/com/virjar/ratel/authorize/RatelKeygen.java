package com.virjar.ratel.authorize;

import com.alibaba.fastjson.JSONObject;
import com.virjar.ratel.authorize.encrypt.RatelLicenceEncryptor;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RatelKeygen {
    public static void main(String[] args) throws ParseException, IOException {
        final Options options = new Options();
        options.addOption(new Option("a", "account", true, "the user account,can not be null"));
        options.addOption(new Option("d", "destination", true, "save file"));
        options.addOption(new Option("e", "expire", true, "expire at yyyy-MM-dd"));
        options.addOption(new Option("h", "help", false, "print help message"));
        options.addOption(new Option("i", "info", false, "show the licence content info"));
        options.addOption(new Option("l", "licenceId", true, "setup a licence Id"));
        options.addOption(new Option("o", "origin", true, "origin licence"));
        options.addOption(new Option("p", "package", true, "only support some special package"));
        options.addOption(new Option("s", "serial", true, "the device serial number,only support some special devices"));

        options.addOption(new Option("E", "extra", true, "extra message"));
        options.addOption(new Option("V", "licenceVersion", true, "licenceVersion"));
        options.addOption(new Option("P", "licenceProtocolVersion", true, "licenceProtocolVersion"));
        options.addOption(new Option("T", "licenceType", true, "licence type:(AuthorizeTypeDebug,AuthorizeTypePerson,AuthorizeTypeTest,AuthorizeTypeMiniGroup,AuthorizeTypeMediumGroup,AuthorizeTypeBigGroup,AuthorizeTypeSlave" +
                "AuthorizeTypeSelfExplosion,AuthorizeTypeB-H)"));


        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);
            hf.printHelp("ratel-keygen", options);
            return;
        }

        RatelLicence.RatelLicenceBuilder ratelLicenceBuilder = RatelLicence.RatelLicenceBuilder.create();

        if (cmd.hasOption('o')) {
            String certificateData = cmd.getOptionValue('o').trim();
            try {
                File certificateDataFile = new File(certificateData);
                if (certificateDataFile.exists() && certificateDataFile.canRead()) {
                    FileInputStream fileInputStream = new FileInputStream(certificateDataFile);
                    byte[] ret = new byte[(int) certificateDataFile.length()];
                    fileInputStream.read(ret);
                    fileInputStream.close();
                    certificateData = new String(ret, StandardCharsets.UTF_8);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            String containerJsonString = new String(RatelLicenceEncryptor.standardRSADecrypt(certificateData), StandardCharsets.UTF_8);
            JSONObject containerJson = JSONObject.parseObject(containerJsonString);
            RatelLicenceContainer ratelLicenceContainer = containerJson.toJavaObject(RatelLicenceContainer.class);
            RatelLicence ratelLicence = RatelLicence.decode(RatelLicenceEncryptor.decrypt(ratelLicenceContainer.getPayload()));
            ratelLicenceBuilder.mirror(ratelLicence);

            if (cmd.hasOption('i')) {
                System.out.println(getLicenceDes(ratelLicence));
            }
        }


        if (cmd.hasOption('l')) {
            ratelLicenceBuilder.setLicenceId(cmd.getOptionValue('l').trim());
        }
        if (cmd.hasOption('V')) {
            ratelLicenceBuilder.setLicenceVersion(Integer.parseInt(cmd.getOptionValue('V')));
        }
        if (cmd.hasOption('P')) {
            ratelLicenceBuilder.setLicenceProtocolVersion(Integer.parseInt(cmd.getOptionValue('P')));
        }
        if (cmd.hasOption('e')) {
            String expireString = cmd.getOptionValue('e');
            try {
                long l = Long.parseLong(expireString);
                ratelLicenceBuilder.setExpire(l);
            } catch (Exception e) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                try {
                    Date date = simpleDateFormat.parse(expireString);
                    ratelLicenceBuilder.setExpire(date.getTime());
                } catch (java.text.ParseException e1) {
                    throw new IllegalStateException(e1);
                }
            }
        }
        if (cmd.hasOption('T')) {
            String licenceTypes = cmd.getOptionValue('T');
            String[] split = licenceTypes.split(",");
            for (String licenceTypeStr : split) {
                licenceTypeStr = licenceTypeStr.trim();
                ratelLicenceBuilder.addLicenceTypeMask(RatelLicence.AuthorizeType.valueOf(licenceTypeStr));
            }
        }
        if (cmd.hasOption('a')) {
            ratelLicenceBuilder.setAccount(cmd.getOptionValue('a').trim());
        }

        if (cmd.hasOption('p')) {
            String packageList = cmd.getOptionValue('p');
            String[] split = packageList.split(",");
            for (String packageString : split) {
                packageString = packageString.trim();
                if (packageString.isEmpty()) {
                    continue;
                }
                ratelLicenceBuilder.addPackage(packageString);
            }
        }

        if (cmd.hasOption('s')) {
            String serialList = cmd.getOptionValue('s');
            String[] split = serialList.split(",");
            for (String serialString : split) {
                serialString = serialString.trim();
                ratelLicenceBuilder.addDevice(serialString);
            }
        }

        if (cmd.hasOption('E')) {
            ratelLicenceBuilder.setExtra(cmd.getOptionValue('E'));
        }

        RatelLicence ratelLicence = ratelLicenceBuilder.build();
        String licenceString = dumpLicence(ratelLicence);

        if (cmd.hasOption('d')) {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(cmd.getOptionValue('d')));
            fileOutputStream.write(licenceString.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();
        } else {
            System.out.println(licenceString);
        }
    }

    private static String getLicenceDes(RatelLicence ratelLicence) {
        StringBuilder sb = new StringBuilder();
        sb.append("account:").append(ratelLicence.getAccount()).append("\n");
        sb.append("devicesId:").append(ratelLicence.getLicenceId()).append("\n");
        sb.append("licenceVersion:").append(ratelLicence.getLicenceVersion()).append("\n");
        sb.append("licenceProtocolVersion:").append(ratelLicence.getLicenceProtocolVersion()).append("\n");
        sb.append("expire:").append(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(ratelLicence.getExpire()))).append("\n");
        sb.append("authorited app:").append("\n");
        String[] packageList = ratelLicence.getPackageList();
        if (packageList.length == 0) {
            packageList = new String[]{"none"};
        }
        for (String app : packageList) {
            sb.append("    ").append(app).append("\n");
        }
        sb.append("authorited devices:").append("\n");
        String[] deviceList = ratelLicence.getDeviceList();
        if (deviceList.length == 0) {
            deviceList = new String[]{"none"};
        }
        for (String device : deviceList) {
            sb.append("    ").append(device).append("\n");
        }

        sb.append("licence type:").append("\n");
        int licenceType = ratelLicence.getLicenceType();
        for (RatelLicence.AuthorizeType authorizeType : RatelLicence.AuthorizeType.values()) {
            if ((authorizeType.typeMask & licenceType) != 0) {
                sb.append("    ").append(authorizeType.name()).append("\n");
            }
        }
        sb.append("extra:").append(ratelLicence.getExtra()).append("\n");
        return sb.toString();
    }

    private static String dumpLicence(RatelLicence ratelLicence) {
        RatelLicenceContainer ratelLicenceContainer = new RatelLicenceContainer(ratelLicence);
        //序列化出来
        String jsonString = JSONObject.toJSONString(ratelLicenceContainer);
        //标准的ras加密
        return RatelLicenceEncryptor.standardRSAEncrypt(jsonString.getBytes(StandardCharsets.UTF_8));
    }
}
