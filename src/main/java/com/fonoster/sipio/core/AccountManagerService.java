package com.fonoster.sipio.core;

import com.fonoster.sipio.core.model.Gateway;
import com.fonoster.sipio.repository.GateWayRepository;
import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.UserCredentials;

import javax.sip.ClientTransaction;

public class AccountManagerService {


    public AccountManagerService() {

    }

    public Gateway getGateway(ClientTransaction ct) {
        String gwRef = ct.getRequest().getHeader("GwRef").toString();
        Gateway gateway = GateWayRepository.getGateway(gwRef);
        return gateway;
    }

    public AccountManager getAccountManager() {
        return new AccountManager() {
            @Override
            public UserCredentials getCredentials(ClientTransaction challengedTransaction, String realm) {
                return new UserCredentials() {
                    @Override
                    public String getUserName() {
                        return getGateway(challengedTransaction).getUserName();
                    }

                    @Override
                    public String getPassword() {
                        return getGateway(challengedTransaction).getSecret();
                    }

                    @Override
                    public String getSipDomain() {
                        return getGateway(challengedTransaction).getHost();
                    }
                };
            }
        };
    }
}
