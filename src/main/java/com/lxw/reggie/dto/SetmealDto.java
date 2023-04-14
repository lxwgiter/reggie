package com.lxw.reggie.dto;

import com.lxw.reggie.entity.Setmeal;
import com.lxw.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
