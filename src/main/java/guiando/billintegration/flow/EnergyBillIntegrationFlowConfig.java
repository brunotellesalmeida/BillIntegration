package guiando.billintegration.flow;

import guiando.billintegration.model.BillType;
import guiando.billintegration.model.FileType;
import guiando.billintegration.transform.FileTypeTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EnergyBillIntegrationFlowConfig {
    private final FileTypeTransformer fileTypeTransformer;

    IntegrationFlow energyBillProcessFlow(){
        return flow -> flow
                .split()
                .filter(f -> f.toString().contains(BillType.ENERGIA.getBillType()))
                .enrichHeaders(h -> h.headerExpression("file_path","payload",true))
                .transform(fileTypeTransformer.toFileTypeApacheTika())
                .<String, String>route(p -> p, m -> m
                        .resolutionRequired(false)
                        .subFlowMapping(FileType.PDF.getFileType(), sf -> sf.channel("pdfEnergyBillPrecessFlow.input"))
                        .subFlowMapping(FileType.TXT.getFileType(), sf -> sf.channel("txtEnergyBillPrecessFlow.input"))
                        .defaultSubFlowMapping(dsf -> dsf.channel("pdfEnergyBillPrecessFlow.input"))
                );
    }

    @Bean
    IntegrationFlow pdfEnergyBillPrecessFlow(){
        return f -> f
                .wireTap(w -> w.handle(h -> log.info("Utilizando o mapeamento para contas de energia em PDF")))
                .wireTap(w -> w.handle(h -> log.info("Conta de energia " + h.getHeaders().get("file_path") + " em PDF processada com sucesso \n\n")));
    }

    @Bean
    IntegrationFlow txtEnergyBillPrecessFlow(){
        return f -> f
                .wireTap(w -> w.handle(h -> log.info("Utilizando o mapeamento para contas de energia em TXT")))
                .wireTap(w -> w.handle(h -> log.info("Conta de energia " + h.getHeaders().get("file_path") + " em TXT processada com sucesso \n\n")));
    }
}
