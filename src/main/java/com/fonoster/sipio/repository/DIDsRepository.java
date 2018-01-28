package com.fonoster.sipio.repository;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.model.DID;

import javax.sip.address.TelURL;

public class DIDsRepository {

    public static DID getDIDByTelUrl(TelURL telUrl) {
        String url = "tel:" + telUrl.getPhoneNumber();
        for (DID did : ConfigManager.getDIDs()) {
            if (did.getTelUrl().equals(url)) {
                return did;
            }
        }
        return null;
    }

    public static boolean didExist(String ref) {
        return getDID(ref) != null;
    }

    public static DID getDID(String ref) {
        for (DID did : ConfigManager.getDIDs()) {
            if (did.getRef().equals(ref)) {
                return did;
            }
        }
        return null;
    }

    public static boolean didExistByTelUrl(TelURL telUrl) {
        return getDIDByTelUrl(telUrl) != null;
    }
}
