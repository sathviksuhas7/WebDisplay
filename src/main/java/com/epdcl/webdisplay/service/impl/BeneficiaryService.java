
package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.BeneficiaryDto;
import java.util.List;

public interface BeneficiaryService {

    List<BeneficiaryDto> getMyBeneficiaries(Long userId);

    BeneficiaryDto addBeneficiary(
            Long userId, String accountNumber, String nickname);

    void deleteBeneficiary(Long userId, Long beneficiaryId);
}
