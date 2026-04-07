package SpringBootBE.BackEnd.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class MomoConfig {

    @Value("${momo.partner-code:MOMO}")
    private String partnerCode;

    @Value("${momo.access-key:ACCESS_KEY}")
    private String accessKey;

    @Value("${momo.secret-key:SECRET_KEY}")
    private String secretKey;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpoint;

    @Value("${momo.redirect-url:http://localhost:3000/payment-result}")
    private String redirectUrl;

    @Value("${momo.ipn-url:http://localhost:8080/api/payment/momo/ipn}")
    private String ipnUrl;

    @Value("${momo.request-type:captureWallet}")
    private String requestType;

    @Value("${momo.lang:vi}")
    private String lang;

    @Value("${momo.order-info-prefix:Thanh toan khoa hoc}")
    private String orderInfoPrefix;
}