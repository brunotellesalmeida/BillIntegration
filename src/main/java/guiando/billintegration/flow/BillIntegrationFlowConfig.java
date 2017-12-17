package guiando.billintegration.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.ftp.dsl.Ftp;
import org.springframework.integration.ftp.session.AbstractFtpSessionFactory;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;

import static org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway.Command.LS;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BillIntegrationFlowConfig
{
    public static final String DEFAULTFILETYPE = "application/pdf";
    private final FtpOperationsFlowConfig ftpOperationsFlowConfig;

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
                .transform(ftpOperationsFlowConfig.getFilePath())
                .<List<String>>filter(p -> !p.isEmpty(),
                        c -> c.discardFlow(dcf -> dcf
                                .wireTap(w -> w.handle(h -> log.info("Nenhum arquivo encontrado no FTP do cliente.")))
                                .channel(new NullChannel())
                        )
                )
                .publishSubscribeChannel(ps -> ps
                    .subscribe(this.waterBillProcessFlow())
                    .subscribe(s -> s
                    .wireTap(w -> w.handle(h -> log.info("Integração finalizada"))))
                )
                .get();
    }

    IntegrationFlow waterBillProcessFlow(){
        return flow -> flow
                .split()
                .filter(f -> f.toString().contains("agua"))
                .enrichHeaders(h -> h.headerExpression("file_path","payload",true))
                .transform(this.toFileType())
                .<String, String>route(p -> p.toString(), m -> m
                .subFlowMapping("application/pdf", sf -> sf.channel("pdfWaterBillPrecessFlow.input"))
                );
    }


    @Bean
    IntegrationFlow pdfWaterBillPrecessFlow(){
        return f -> f
                .wireTap(w -> w.handle(h -> log.info("Conta de água processada com sucesso")));
    }

    @Transformer
    private MessageProcessor<String> toFileType(){
        return (Message<?> message) -> {
            try {
                return Files.probeContentType((Paths.get(message.getPayload().toString())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return DEFAULTFILETYPE;
        };
    }
}
