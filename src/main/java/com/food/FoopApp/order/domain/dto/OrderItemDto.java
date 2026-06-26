package com.food.FoopApp.order.domain.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.food.FoopApp.menu.domain.dto.MenuDto;
import com.food.FoopApp.order.domain.entity.Order;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemDto {
    private Long id;
    private Long menuId;
    private int quantity;
    private MenuDto menu;
    private BigDecimal pricePerUnit;
    private BigDecimal subTotal;
}
