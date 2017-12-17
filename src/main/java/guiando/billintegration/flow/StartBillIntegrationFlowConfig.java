package guiando.billintegration.flow;

import guiando.billintegration.properties.BillIntegrationProperties;
import guiando.billintegration.properties.FtpProperties;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.RequestMethod;

@Setter
@Configuration
@RequiredArgsConstructor
public class StartBillIntegrationFlowConfig {

    private final MessageChannel startChannel;
    private final FtpProperties ftpProperties;
    private final BillIntegrationProperties fileintegrationProperties;

    private MessageSource<FtpProperties> messageSource() {
        return () -> new GenericMessage<>(this.ftpProperties);
    }

    @Bean
    DirectChannel requestChannel() {
        return MessageChannels.direct("input").get();
    }

    @Bean
    IntegrationFlow setupRestStartEndpoint() {
        return IntegrationFlows.from(Http.inboundChannelAdapter(this.fileintegrationProperties.getStartUrl())
                .requestChannel(this.requestChannel())
                .requestMapping(rm -> rm.methods(HttpMethod.POST))
                .crossOrigin(co -> co.origin("*")
                        .method(RequestMethod.POST)
                        .maxAge(3600)))
                .get();
    }

    @Bean
    IntegrationFlow restStartFlow() {
        return IntegrationFlows.from(this.requestChannel())
                .handle(this.messageSource())
                .headerFilter("*", true)
                .channel(this.startChannel)
                .get();
    }

    @Bean
    IntegrationFlow schedulerStartFlow() {
        return IntegrationFlows.from(this.messageSource(), c -> c
                .poller(Pollers.cron(this.fileintegrationProperties.getCronExpression())))
                .channel(this.startChannel)
                .get();
    }
}