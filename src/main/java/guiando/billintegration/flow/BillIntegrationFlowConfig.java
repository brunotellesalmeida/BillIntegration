package guiando.billintegration.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.ftp.dsl.Ftp;
import org.springframework.integration.ftp.session.AbstractFtpSessionFactory;
import org.springframework.messaging.MessageChannel;

import java.util.List;
import java.util.concurrent.Executors;

import static org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway.Command.LS;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BillIntegrationFlowConfig
{
    private final FtpOperationsFlowConfig ftpOperationsFlowConfig;
    private final EnergyBillIntegrationFlowConfig energyBillIntegrationFlowConfig;
    private final WaterBillIntegrationFlowConfig watterBillIntegrationFlowConfig;

    @Bean
    AbstractFtpSessionFactory<FTPClient> sessionFactory() {
        return ftpOperationsFlowConfig.assembleFtpSessionFactory();
    }

    @Bean
    MessageChannel startChannel() {
        return MessageChannels.executor(Executors.newSingleThreadExecutor()).get();
    }

    @Bean
    IntegrationFlow buildFlow(){
        return IntegrationFlows
                .from(this.startChannel())
                .transform("payload.folder")
                .wireTap(w -> w.handle(h -> log.info("Listando arquivos no diretório")))
                .handle(Ftp.outboundGateway(this.sessionFactory(), LS, "payload")
                .get()
                )
                .transform(ftpOperationsFlowConfig.getAllFilePath())
                .<List<String>>filter(p -> !p.isEmpty(),
                        c -> c.discardFlow(dcf -> dcf
                                .wireTap(w -> w.handle(h -> log.info("Nenhum arquivo encontrado no FTP do cliente.")))
                                .channel(new NullChannel())
                        )
                )
                .publishSubscribeChannel(ps -> ps
                    .subscribe(this.watterBillIntegrationFlowConfig.waterBillProcessFlow())
                    .subscribe(this.energyBillIntegrationFlowConfig.energyBillProcessFlow())
                    .subscribe(s -> s
                    .wireTap(w -> w.handle(h -> log.info("Integração finalizada"))))
                )
                .get();
    }
}
