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
public class WaterBillIntegrationFlowConfig {
    private final FileTypeTransformer fileTypeTransformer;

    IntegrationFlow waterBillProcessFlow(){
        return flow -> flow
                .split()
                .filter(f -> f.toString().contains(BillType.AGUA.getBillType()))
                .enrichHeaders(h -> h.headerExpression("file_path","payload",true))
                .transform(fileTypeTransformer.toFileType())
                .<String, String>route(p -> p, m -> m
                        .resolutionRequired(false)
                        .subFlowMapping(FileType.PDF.getFileType(), sf -> sf.channel("pdfWaterBillPrecessFlow.input"))
                        .subFlowMapping(FileType.TXT.getFileType(), sf -> sf.channel("txtWaterBillPrecessFlow.input"))
                        .defaultSubFlowMapping(dsf -> dsf.channel("pdfWaterBillPrecessFlow.input"))
                );
    }

    @Bean
    IntegrationFlow pdfWaterBillPrecessFlow(){
        return f -> f
                .wireTap(w -> w.handle(h -> log.info("Utilizando o mapeamento para contas de água em PDF")))
                .wireTap(w -> w.handle(h -> log.info("Conta de água "  + h.getHeaders().get("file_path") + " em PDF processada com sucesso \n\n")));
    }

    @Bean
    IntegrationFlow txtWaterBillPrecessFlow(){
        return f -> f
                .wireTap(w -> w.handle(h -> log.info("Utilizando o mapeamento para contas de água em TXT")))
                .wireTap(w -> w.handle(h -> log.info("Conta de água " + h.getHeaders().get("file_path") + " em TXT processada com sucesso \n\n")));
    }
}
