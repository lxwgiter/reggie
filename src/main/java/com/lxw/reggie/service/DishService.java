package com.lxw.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lxw.reggie.dto.DishDto;
import com.lxw.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);

    void deleteWithFlavor(Long id);
}
