package com.fonoster.sipio.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.header.WWWAuthenticateHeader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

public class AuthHelper {

    String domain = "sip.io";
    String realm = "sipio";
    HeaderFactory headerFactory;
    static final Logger logger = LogManager.getLogger();
    static final String DEFAULT_ALGORITHM = "MD5";

    public AuthHelper(HeaderFactory headerFactory)  {
        this.headerFactory = headerFactory;
    }
    public AuthHelper() throws PeerUnavailableException {
        this.headerFactory = SipFactory.getInstance().createHeaderFactory();
    }

    public String calculateResponse(String userName, String secret, String realm, String nonce, String nc,
                                    String cnonce, String uri, String method, String gop) {
        String a1 = userName + ":" + realm + ":" + secret;
        String a2 = method.toUpperCase() + ":" + uri;
        String ha1 = DigestUtils.md2Hex(a1);
        String ha2 = DigestUtils.md2Hex(a2);
        String result;
        if (gop != null && gop.equals("auth")) {
            result = DigestUtils.md2Hex(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + gop + ":" + ha2);
        } else {
            result = DigestUtils.md2Hex(ha1 + ":" + nonce + ":" + ha2);
        }
        logger.trace("A1: {} , A2: {} ,  HA1: {} , HA2 : {} , Result : {}", a1, a2, ha1, ha2, result);
        return result;
    }


    public WWWAuthenticateHeader generateChallenge(HeaderFactory headerFactory) throws ParseException, NoSuchAlgorithmException {
        WWWAuthenticateHeader wwwAuthHeader = headerFactory.createWWWAuthenticateHeader("Digest");
        wwwAuthHeader.setDomain(domain);
        wwwAuthHeader.setRealm(realm);
        wwwAuthHeader.setQop("auth");
        wwwAuthHeader.setOpaque("");
        wwwAuthHeader.setStale(false);
        wwwAuthHeader.setNonce(this.generateNonce());
        wwwAuthHeader.setAlgorithm(DEFAULT_ALGORITHM);

        return wwwAuthHeader;
    }

    public WWWAuthenticateHeader generateChallenge() throws ParseException, NoSuchAlgorithmException {
        return generateChallenge(headerFactory);
    }

    public String generateNonce() throws NoSuchAlgorithmException {
        LocalDateTime date = LocalDateTime.now();
        LocalTime time = date.toLocalTime();
        long pad = new Random().nextLong();
        String nonceString = (new Long(time.getSecond())).toString() + Long.toString(pad);
        MessageDigest messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
        byte[] mdBytes = messageDigest.digest(nonceString.getBytes());
        return DigestUtils.md5Hex(mdBytes);

    }
}
