package guiando.billintegration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BillType {
    AGUA("agua"),
    ENERGIA("energia"),
    TAXI("taxi"),;

    private final String billType;
}
