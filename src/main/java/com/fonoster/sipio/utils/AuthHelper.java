package com.fonoster.sipio.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import javax.sip.header.HeaderFactory;
import javax.sip.header.WWWAuthenticateHeader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;

public class AuthHelper {

    String domain = "192.168.1.188";
    HeaderFactory headerFactory;
    static final Logger logger = LoggerFactory.getLogger(AuthHelper.class);
    static final String DEFAULT_ALGORITHM = "MD5";
    private MessageDigest messageDigest;

    public AuthHelper() throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
    }



    public String calculateResponse(String userName, String secret, String realm, String nonce, String nc,
                                    String cnonce, String uri, String method, String gop) {
        logger.debug("calculateResponse : username: {} , password : {} ,  realm : {} , nonce : {} , nc : {} , cnonce : {} , uri : {} ,  method : {} , gop : {}", userName, secret, realm, nonce, nc, cnonce, uri, method, gop);
        String a1 = userName + ":" + realm + ":" + secret;
        String a2 = method.toUpperCase() + ":" + uri;
        String ha1 = DigestUtils.md5DigestAsHex(a1.getBytes());
        String ha2 = DigestUtils.md5DigestAsHex(a2.getBytes());
        String result;
        if (gop != null && gop.equals("auth")) {
            result = DigestUtils.md5DigestAsHex((ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + gop + ":" + ha2).getBytes());
        } else {
            result = DigestUtils.md5DigestAsHex((ha1 + ":" + nonce + ":" + ha2).getBytes());
        }
        logger.trace("A1: {} , A2: {} ,  HA1: {} , HA2 : {} , Result : {}", a1, a2, ha1, ha2, result);
        return result;
    }



    public WWWAuthenticateHeader generateChallenge(HeaderFactory headerFactory,String realm) throws ParseException, NoSuchAlgorithmException {
        WWWAuthenticateHeader wwwAuthHeader = headerFactory.createWWWAuthenticateHeader("Digest");
        wwwAuthHeader.setRealm(realm);
        wwwAuthHeader.setQop("auth");
        wwwAuthHeader.setOpaque("");
        wwwAuthHeader.setStale(false);
        wwwAuthHeader.setNonce(generateNonce());
        wwwAuthHeader.setAlgorithm(DEFAULT_ALGORITHM);

        return wwwAuthHeader;
    }



    private String generateNonce() throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
        // Get the time of day and run MD5 over it.
        Date date = new Date();
        long time = date.getTime();
        Random rand = new Random();
        long pad = rand.nextLong();
        String nonceString = (new Long(time)).toString() + (new Long(pad)).toString();
        byte mdbytes[] = messageDigest.digest(nonceString.getBytes());
        // Convert the mdbytes array into a hex string.
        return DigestUtils.md5DigestAsHex(mdbytes);
    }
}
