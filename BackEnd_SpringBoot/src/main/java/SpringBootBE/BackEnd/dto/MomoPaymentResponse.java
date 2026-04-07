package SpringBootBE.BackEnd.dto;

import lombok.Data;

@Data
public class MomoPaymentResponse {
    private String message;
    private Integer orderId;
    private String momoOrderId;
    private String requestId;
    private Long amount;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    private Integer resultCode;
    private String status;
}
