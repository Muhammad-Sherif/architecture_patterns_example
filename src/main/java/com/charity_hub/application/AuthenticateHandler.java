package com.charity_hub.application;

import com.charity_hub.domain.contracts.IAccountRepo;
import com.charity_hub.domain.contracts.IAuthProvider;
import com.charity_hub.domain.contracts.IJWTGenerator;
import com.charity_hub.domain.contracts.ILogger;
import com.charity_hub.domain.models.account.Tokens;
import com.charity_hub.domain.models.account.Account;
import com.charity_hub.services.AccountCreationService;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AuthenticateHandler {
    private final IAccountRepo accountRepo;
    private final IAuthProvider authProvider;
    private final IJWTGenerator jwtGenerator;
    private final AccountCreationService accountCreationService;

    private final ILogger logger ;
    public AuthenticateHandler(
            IAccountRepo accountRepo,
            IAuthProvider authProvider,
            IJWTGenerator jwtGenerator, AccountCreationService accountCreationService, ILogger logger
    ) {
        this.accountRepo = accountRepo;
        this.authProvider = authProvider;
        this.jwtGenerator = jwtGenerator;
        this.accountCreationService = accountCreationService;
        this.logger = logger;
    }

    public CompletableFuture<AuthenticateResponse> handle(Authenticate command) {
        return CompletableFuture.supplyAsync(() -> {
            logger.log("Handling authentication for idToken: {}");

            String mobileNumber = authProvider.getVerifiedMobileNumber(command.idToken()).join();

            logger.log("check for account: {}");

            Account account = accountCreationService.existingAccountOrNewAccount(mobileNumber, command.deviceType(),command.deviceId());

            logger.log(" finish check for account: {}");

            Tokens tokens = account.authenticate(jwtGenerator, command.deviceId(), command.deviceType());

            accountRepo.save(account);
            logger.log("Authentication successful for account: {}");

            return new AuthenticateResponse(tokens.accessToken(), tokens.refreshToken());
        });
    }


}
