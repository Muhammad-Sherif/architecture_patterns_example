package com.charity_hub.services;

import com.charity_hub.domain.contracts.IAccountRepo;
import com.charity_hub.domain.contracts.IInvitationRepo;
import com.charity_hub.domain.contracts.ILogger;
import com.charity_hub.domain.exceptions.AppException;
import com.charity_hub.domain.models.account.Account;

public class AccountCreationService {
    private final IAccountRepo accountRepo;
    private final IInvitationRepo invitationRepo;
    private final ILogger logger;

    public AccountCreationService(IAccountRepo accountRepo, IInvitationRepo invitationRepo, ILogger logger) {
        this.accountRepo = accountRepo;
        this.invitationRepo = invitationRepo;
        this.logger = logger;
    }

    public Account existingAccountOrNewAccount(String mobileNumber, String deviceType, String deviceId) {
        logger.log("get the account by mobile number");
        var account = accountRepo.getByMobileNumber(mobileNumber).join();
        if (accountExists(account)) {
            return account;
        }
        return createNewAccount(mobileNumber, deviceType, deviceId);
    }


    private static boolean accountExists(Account existingAccount) {
        return existingAccount != null;
    }


    private Account createNewAccount(String mobileNumber, String aDeviceType, String aDeviceId) {
        boolean isAdmin = accountRepo.isAdmin(mobileNumber).join();
        boolean hasNoInvitations = !invitationRepo.hasInvitation(mobileNumber).join();
        assertIsAdminOrInvited(mobileNumber, isAdmin, hasNoInvitations);

        return Account.newAccount(mobileNumber, isAdmin, aDeviceType, aDeviceId);
    }

    private void assertIsAdminOrInvited(String mobileNumber, boolean isAdmin, boolean hasNoInvitations) {
        if (!isAdmin && hasNoInvitations) {
            logger.errorLog(String.format("Account not invited: %s", mobileNumber));
            throw new AppException.RequirementException("Account not invited to use the App");
        }
    }


}
