package com.cargoco.controller;

import com.cargoco.common.Result;
import com.cargoco.entity.Banner;
import com.cargoco.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerMapper bannerMapper;

    @GetMapping("/list")
    public Result<List<Banner>> list() {
        return Result.success(bannerMapper.findActive());
    }
}
