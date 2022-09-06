package com.era.onlinesignature.model.contractfile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractFileResponse {
    private Long id;
    private String nameLinkContract;
    private String urlContract;
    private String fileExtension;

    public ContractFileResponse(Long id, String nameLinkContract, String urlContract, String fileExtension) {
        this.id = id;
        this.nameLinkContract = nameLinkContract;
        this.urlContract = urlContract;
        this.fileExtension = fileExtension;
    }
}
