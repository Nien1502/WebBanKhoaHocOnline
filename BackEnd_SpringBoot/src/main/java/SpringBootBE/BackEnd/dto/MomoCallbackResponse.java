package SpringBootBE.BackEnd.dto;

import lombok.Data;

@Data
public class MomoCallbackResponse {
    private String message;
    private String orderId;
    private String requestId;
    private String transId;
    private Integer resultCode;
    private String status;
    private boolean validSignature;
}

