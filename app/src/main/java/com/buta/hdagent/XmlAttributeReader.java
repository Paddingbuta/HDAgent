package com.buta.hdagent;

import android.app.AlertDialog;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class XmlAttributeReader {

    public static Map<String, String> queryXmlAttributes(String folderName) {
        String directoryPath = "/data/data/com.buta.hdagent/files/profiles/" + folderName;
        File xmlFile = new File(directoryPath, "storage_new.xml");

        Map<String, String> attributes = new HashMap<>();

        if (!xmlFile.exists()) {
            System.err.println("File does not exist: " + xmlFile.getAbsolutePath());
            return attributes;
        }

        try (FileInputStream fileInputStream = new FileInputStream(xmlFile)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileInputStream, "UTF-8");

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "string".equals(parser.getName())) {
                    // 获取 name 属性值
                    String name = parser.getAttributeValue(null, "name");
                    // 如果 name 匹配我们需要的属性，则获取文本内容
                    if ("passToken_env3".equals(name) ||
                            "higher_env3".equals(name) ||
                            "language_code_env3".equals(name) ||
                            "lower_env3".equals(name)) {
                        String value = parser.nextText();
                        attributes.put(name, value);
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attributes;
    }

    public static Map<String, String> querySCXmlAttributes(String folderName) {
        String directoryPath = "/data/data/com.buta.hdagent/files/sc_profiles/" + folderName;
        File xmlFile = new File(directoryPath, "storage_new.xml");

        Map<String, String> attributes = new HashMap<>();

        if (!xmlFile.exists()) {
            System.err.println("File does not exist: " + xmlFile.getAbsolutePath());
            return attributes;
        }

        try (FileInputStream fileInputStream = new FileInputStream(xmlFile)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileInputStream, "UTF-8");

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "string".equals(parser.getName())) {
                    // 获取 name 属性值
                    String name = parser.getAttributeValue(null, "name");
                    // 如果 name 匹配我们需要的属性，则获取文本内容
                    if ("Token".equals(name) ||
                            "SCID".equals(name) ||
                            "SCToken".equals(name)) {
                        String value = parser.nextText();
                        attributes.put(name, value);
                    }
                    else if ("ID".equals(name)) {
                        String value = parser.nextText();
                        String high = "";
                        String low = "";
                        String[] idParts = value.split("-");
                        if (idParts.length == 2) {
                            high = idParts[0];
                            low = idParts[1];
                            attributes.put("high", high);
                            attributes.put("low", low);
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attributes;
    }

    public static Map<String, String> queryHDAttributes(){

        String directoryPath = "/data/data/com.supercell.hayday/shared_prefs/storage_new.xml";
        File xmlFile = new File(directoryPath);
        Map<String, String> attributes = new HashMap<>();

        if (!isFileExistWithRoot(directoryPath)) {
            System.err.println("File does not exist: " + xmlFile.getAbsolutePath());
            return attributes;
        }


        String fileContent = readFileWithRoot(directoryPath);
        if (fileContent == null || fileContent.isEmpty()) {
            System.err.println("Failed to read file content or file is empty: " + directoryPath);
            return attributes;
        }

        try {
            // Create the XML parser
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new java.io.StringReader(fileContent));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "string".equals(parser.getName())) {
                    // 获取 name 属性值
                    String name = parser.getAttributeValue(null, "name");
                    // 如果 name 匹配我们需要的属性，则获取文本内容
                    if (AESUtils.encryptECB("passToken_env3").trim().equals(name.trim())) {
                        String value = parser.nextText();
                        attributes.put("passToken_env3", AESUtils.decryptCBC(value));
                    } else if (AESUtils.encryptECB("higher_env3").trim().equals(name.trim())) {
                        String value = parser.nextText();
                        attributes.put("higher_env3", AESUtils.decryptCBC(value));
                    } else if (AESUtils.encryptECB("language_code_env3").trim().equals(name.trim())) {
                        String value = parser.nextText();
                        attributes.put("language_code_env3", AESUtils.decryptCBC(value));
                    } else if (AESUtils.encryptECB("lower_env3").trim().equals(name.trim())) {
                        String value = parser.nextText();
                        attributes.put("lower_env3", AESUtils.decryptCBC(value));
                    } else if (AESUtils.encryptECB("SCID_PROD_CURRENT_ACCOUNT_SUPERCELL_ID").trim().equals(name.trim())) {
                        String value = parser.nextText();
                        attributes.put("SCID_PROD_CURRENT_ACCOUNT_SUPERCELL_ID", AESUtils.decryptCBC(value));
                    } else if (AESUtils.encryptECB("SCID_PROD_ACCOUNTS").trim().equals(name.trim())) {
                        String value = parser.nextText();
                        attributes.put("SCID_PROD_ACCOUNTS", AESUtils.decryptCBC(value));
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attributes;
    }
    /**
     * Checks if a file exists using root permissions.
     * @param filePath The file path.
     * @return True if the file exists, false otherwise.
     */
    private static boolean isFileExistWithRoot(String filePath) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("[ -f " + filePath + " ] && echo \"exists\" || echo \"not_exists\"\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            return "exists".equals(result);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads a file's content using root permissions.
     * @param filePath The file path.
     * @return The content of the file as a string.
     */
    private static String readFileWithRoot(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("cat " + filePath + "\n");
            os.writeBytes("exit\n");
            os.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
