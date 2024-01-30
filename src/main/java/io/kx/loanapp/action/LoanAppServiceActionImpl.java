package io.kx.loanapp.action;

import com.google.protobuf.Empty;
import io.kx.loanapp.api.LoanAppApi;
import kalix.javasdk.action.ActionCreationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/kx/loanapp/action/loan_app_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class LoanAppServiceActionImpl extends AbstractLoanAppServiceAction {

  private static Logger logger = LoggerFactory.getLogger(LoanAppServiceActionImpl.class);

  public LoanAppServiceActionImpl(ActionCreationContext creationContext) {}

  @Override
  public Effect<Empty> submitLoanApplication(LoanAppApi.SubmitCommand submitCommand) {
    CompletionStage timerRegistration = timers().startSingleTimer(submitCommand.getLoanAppId(),
            Duration.ofDays(365),
            components().loanAppServiceActionImpl().expireAction(
                    LoanAppApi.DeclineCommand.newBuilder()
                            .setLoanAppId(submitCommand.getLoanAppId())
                            .setReason("Declined after 20 seconds if not approved within")
                            .build()
            )
    );

    return effects().asyncReply(
            timerRegistration.thenCompose(done -> components().loanAppEntity().submit(submitCommand).execute())
                    .thenApply(any -> Empty.getDefaultInstance())
    );
  }
  @Override
  public Effect<Empty> expireAction(LoanAppApi.DeclineCommand declineCommand) {
    logger.info("----------------#######-----------");
    logger.info("Expiring loan app " + declineCommand.getLoanAppId());
    logger.info("----------------#######-----------");
    return effects().forward(components().loanAppEntity().decline(declineCommand));
  }
}
