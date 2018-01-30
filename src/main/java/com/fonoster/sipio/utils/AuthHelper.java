package com.fonoster.sipio.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.URI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Random;

import static gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper.toHexString;

public class AuthHelper {

    String domain = "192.168.1.188";
    String realm = "sipio";
    HeaderFactory headerFactory;
    static final Logger logger = LogManager.getLogger();
    static final String DEFAULT_ALGORITHM = "MD5";
    private MessageDigest messageDigest;

    public AuthHelper() throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
    }



    public String calculateResponse(String userName, String secret, String realm, String nonce, String nc,
                                    String cnonce, String uri, String method, String gop) {
        logger.info("calculateResponse : username: {} , password : {} ,  realm : {} , nonce : {} , nc : {} , cnonce : {} , uri : {} ,  method : {} , gop : {}", userName, secret, realm, nonce, nc, cnonce, uri, method, gop);
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
        wwwAuthHeader.setNonce(generateNonce());
        wwwAuthHeader.setAlgorithm(DEFAULT_ALGORITHM);

        return wwwAuthHeader;
    }

    public WWWAuthenticateHeader generateChallenge() throws ParseException, NoSuchAlgorithmException {
        return generateChallenge(headerFactory);
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
        return DigestUtils.md5Hex(mdbytes);
    }
}
