package SpringBootBE.BackEnd.dto;

import lombok.Data;

import java.util.List;

@Data
public class MomoPaymentRequest {

    private Integer userId;

    private List<Integer> courseIds;

    private Long amount;

    private String orderInfo;

    private String extraData;
}