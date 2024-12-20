package com.charity_hub.services;

import com.charity_hub.domain.contracts.ILogger;
import com.charity_hub.domain.exceptions.AppException;
import com.charity_hub.domain.models.account.Account;

public class AccountCreationService {
    private final ILogger logger;

    public AccountCreationService(ILogger logger) {
        this.logger = logger;
    }

    public Account createNewAccount(String mobileNumber, String deviceType, String deviceId, boolean hasInvitation, boolean isAdmin) {
        assertIsAdminOrInvited(mobileNumber, isAdmin, hasInvitation);
        return Account.newAccount(mobileNumber, isAdmin, deviceType, deviceId);
    }

    private void assertIsAdminOrInvited(String mobileNumber, boolean isAdmin, boolean hasInvitation) {
        if (!isAdmin && !hasInvitation) {
            logger.errorLog(String.format("Account not invited: %s", mobileNumber));
            throw new AppException.RequirementException("Account not invited to use the App");
        }
    }


}
