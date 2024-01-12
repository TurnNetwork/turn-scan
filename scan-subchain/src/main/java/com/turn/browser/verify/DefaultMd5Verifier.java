package com.turn.browser.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultMd5Verifier implements Verifier {

    @Override
    public boolean verify(ApiParam apiParam, String secret) {
        boolean isSame = false;
        String signCode = apiParam.fatchSignAndRemove();

        if (signCode != null) {
            try {
                String clientSign = buildSign(apiParam, secret);
                isSame = signCode.equals(clientSign);
            } catch (IOException e) {
            }
        }

        return isSame;
    }

    public String buildSign(Map<String, ?> paramsMap, String secret) throws IOException {
        Set<String> keySet = paramsMap.keySet();
        List<String> paramNames = new ArrayList<String>(keySet);

        Collections.sort(paramNames);

        StringBuilder paramNameValue = new StringBuilder();

        for (String paramName : paramNames) {
            paramNameValue.append(paramName).append(paramsMap.get(paramName));
        }

        String source = secret + paramNameValue.toString() + secret;

        return encrypt(source);
    }

    /**
     * Generate md5, all uppercase.
     *
     * @param source
     * @return Returns MD5 in all uppercase letters
     */
    public String encrypt(String source) {
        Encrypter encrypter = new ApiEncrypter();;
        return encrypter.md5(source).toUpperCase();
    }

}
