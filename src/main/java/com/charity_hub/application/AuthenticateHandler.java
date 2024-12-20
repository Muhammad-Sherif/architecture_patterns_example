package com.charity_hub.application;

import com.charity_hub.domain.contracts.*;
import com.charity_hub.domain.models.account.Tokens;
import com.charity_hub.services.AccountCreationService;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AuthenticateHandler {
    private final IAccountRepo accountRepo;
    private final IAuthProvider authProvider;
    private final IInvitationRepo invitationRepo;
    private final IJWTGenerator jwtGenerator;
    private final AccountCreationService accountCreationService;

    private final ILogger logger ;
    public AuthenticateHandler(
            IAccountRepo accountRepo,
            IAuthProvider authProvider, IInvitationRepo invitationRepo,
            IJWTGenerator jwtGenerator, AccountCreationService accountCreationService, ILogger logger
    ) {
        this.accountRepo = accountRepo;
        this.authProvider = authProvider;
        this.invitationRepo = invitationRepo;
        this.jwtGenerator = jwtGenerator;
        this.accountCreationService = accountCreationService;
        this.logger = logger;
    }

    public CompletableFuture<AuthenticateResponse> handle(Authenticate command) {
        return CompletableFuture.supplyAsync(() -> {
            logger.log("Handling authentication" );

            String mobileNumber = authProvider.getVerifiedMobileNumber(command.idToken()).join();


            var account = accountRepo.getByMobileNumber(mobileNumber).join();

            if(account == null) {
                boolean hasInvitation = invitationRepo.hasInvitation(mobileNumber).join();
                boolean isAdmin = accountRepo.isAdmin(mobileNumber).join();
                account = accountCreationService.
                        createNewAccount(mobileNumber,command.deviceType(),command.deviceId(),
                                         hasInvitation,isAdmin);
            }


            Tokens tokens = account.authenticate(jwtGenerator, command.deviceId(), command.deviceType());

            accountRepo.save(account);
            logger.log("Authentication successful for ");

            return new AuthenticateResponse(tokens.accessToken(), tokens.refreshToken());
        });
    }


}
