package com.lxw.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lxw.reggie.dto.SetmealDto;
import com.lxw.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    void removeWithDish(List<Long> ids);

    void updateWithFlavor(SetmealDto setmealDto);
}
