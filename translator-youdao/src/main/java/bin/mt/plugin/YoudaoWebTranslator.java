package bin.mt.plugin;

import org.json2.JSONArray;
import org.json2.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 有道翻译Web版
 *
 * @author Bin
 */
@SuppressWarnings("CharsetObjectCanBeUsed")
public class YoudaoWebTranslator {
    private static String cookie;

    public static void main(String[] args) throws IOException {
        System.out.println(translate("apple", "en", "zh")); // 苹果
        System.out.println(translate("apple", "zh", "en")); // apple
        System.out.println(translate("苹果", "zh", "en")); // apple
    }

    public static String translate(String query, String from, String to) throws IOException {
        if (from.equals("zh")) {
            from = "zh-CHS";
        }
        if (to.equals("zh")) {
            to = "zh-CHS";
        }
        if (cookie == null) {
            HttpURLConnection conn = HttpUtils.get("http://m.youdao.com/translate")
                    .createConnection();
            String str = conn.getHeaderField("Set-Cookie");
            if (str == null) {
                throw new IOException("Can not get cookie");
            }
            int i = str.indexOf(';');
            cookie = i == -1 ? str : str.substring(0, i);
        }
        String salt = System.currentTimeMillis() + "1";
        String sign = md5(("fanyideskweb" + query + salt + "Ygy_4c=r#e#4EX^NUGUc5").getBytes("UTF-8"));
        JSONObject result = HttpUtils.post("https://fanyi.youdao.com/translate_o")
                .header("Referer", "https://fanyi.youdao.com/")
                .header("Cookie", cookie)
                .formData("i", query)
                .formData("from", from)
                .formData("to", to)
                .formData("client", "fanyideskweb")
                .formData("salt", salt)
                .formData("sign", sign)
                .formData("doctype", "json")
                .formData("version", "1.0")
                .formData("keyfrom", "fanyi.web")
                .executeToJson();
        switch (result.getInt("errorCode")) {
            case 0:
                JSONArray array = result.getJSONArray("translateResult");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < array.length(); i++) {
                    sb.append(array.getString(i));
                }
                return sb.toString();
            case 50:
                cookie = null;
                break;
        }
        throw new IOException(result.toString());
    }

    private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String toHex(byte[] digest) {
        char[] str = new char[digest.length * 2];
        int k = 0;
        for (byte b : digest) {
            str[k++] = hexDigits[b >>> 4 & 0xf];
            str[k++] = hexDigits[b & 0xf];
        }
        return new String(str);
    }

    private static String md5(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return toHex(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
