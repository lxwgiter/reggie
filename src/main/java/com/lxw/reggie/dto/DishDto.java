package com.lxw.reggie.dto;

import com.lxw.reggie.entity.DishFlavor;
import com.lxw.reggie.entity.Dish;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 由于页面提交的菜品，包括菜品名称和几种不同的口味要求，我们用DishDto继承Dish实现封装两个实体
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
