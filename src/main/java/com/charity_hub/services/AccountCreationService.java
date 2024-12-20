package com.charity_hub.services;

import com.charity_hub.domain.helper.CreateOperation;
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
            logger.log("account founded , no need to create new one");
            return account;
        }
        return createNewAccount(mobileNumber, deviceType, deviceId);

    }

    private  boolean accountExists(Account existingAccount) {
        return existingAccount != null;
    }

    private Account createNewAccount(String mobileNumber, String deviceType, String deviceId) {
        logger.log("account not found, trying to create new one");
        CreateOperation<Account> createAccountOperation = Account.newAccount(accountRepo,invitationRepo, mobileNumber, deviceType, deviceId);
        validateCreateAccountOperationIsSuccess(createAccountOperation, mobileNumber);
        logger.log("account created successfully");
        return createAccountOperation.createdData();
    }


    private void validateCreateAccountOperationIsSuccess(CreateOperation<Account> createAccountOperation,String mobileNumber) {
        if(!createAccountOperation.success()) {
            logger.errorLog("failed to create account");
            logger.errorLog(String.format("Account not invited: %s", mobileNumber));
            throw new AppException.RequirementException("Account not invited to use the App");
        }
    }

}
